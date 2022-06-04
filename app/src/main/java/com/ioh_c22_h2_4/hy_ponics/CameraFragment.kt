package com.ioh_c22_h2_4.hy_ponics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentCameraBinding
import com.ioh_c22_h2_4.hy_ponics.util.Constants.ACCURACY_THRESHOLD
import com.ioh_c22_h2_4.hy_ponics.util.Constants.FILENAME_FORMAT
import com.ioh_c22_h2_4.hy_ponics.util.Constants.LABELS_PATH
import com.ioh_c22_h2_4.hy_ponics.util.Constants.MODEL_PATH
import com.ioh_c22_h2_4.hy_ponics.util.Constants.PHOTO_EXTENSION
import com.ioh_c22_h2_4.hy_ponics.util.LettuceDetectionHelper
import com.ioh_c22_h2_4.hy_ponics.util.Util.createFile
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.min

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var bitmapBuffer: Bitmap

    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private val permissions by lazy { arrayOf(Manifest.permission.CAMERA) }

    private val nnApiDelegate by lazy { NnApiDelegate() }

    private var pauseAnalysis = false

    private var imageRotationDegrees: Int = 0

    private val imageCapture by lazy {
        val rotation = binding.viewFinder.display.rotation
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(RATIO_4_3)
            .setTargetRotation(rotation)
            .build()
    }

    private val imageAnalysis by lazy {
        val rotation = binding.viewFinder.display.rotation
        ImageAnalysis.Builder()
            .setTargetAspectRatio(RATIO_4_3)
            .setTargetRotation(rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
    }

    private val imagePreview by lazy {
        val rotation = binding.viewFinder.display.rotation
        Preview.Builder()
            .setTargetAspectRatio(RATIO_4_3)
            .setTargetRotation(rotation)
            .build()
    }

    private val cameraSelector by lazy {
        CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
    }

    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }

    private val tflite by lazy {
        Interpreter(
            FileUtil.loadMappedFile(requireContext(), MODEL_PATH),
            Interpreter.Options().addDelegate(nnApiDelegate)
        )
    }
    private val tfInputSize by lazy {
        val inputIndex = 0
        val inputShape = tflite.getInputTensor(inputIndex).shape()
        Size(inputShape[2], inputShape[1]) // Order of axis is: {1, height, width, 3}
    }

    private val tfImageBuffer by lazy { TensorImage(DataType.FLOAT32) }

    private val tfImageProcessor by lazy {
        val cropSize = minOf(bitmapBuffer.width, bitmapBuffer.height)
        ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(
                ResizeOp(
                    tfInputSize.height,
                    tfInputSize.width,
                    ResizeOp.ResizeMethod.NEAREST_NEIGHBOR
                )
            )
            .add(Rot90Op(-imageRotationDegrees / 90))
            .add(NormalizeOp(0f, 1f))
            .build()
    }

    private val tfImage by lazy {
        tfImageProcessor.process(tfImageBuffer.apply { load(bitmapBuffer) })
    }

    private val lettuceDetector by lazy {
        LettuceDetectionHelper(
            tflite,
            FileUtil.loadLabels(requireContext(), LABELS_PATH)
        )
    }

    private val launcherPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val isGranted = it.value
                if (isGranted) {
                    Log.d("$this", "permissions granted")
                    bindCameraUseCases()
                } else {
                    Log.d("$this", "permissions denied")
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launcherPermissionRequest.launch(permissions)

        bindCameraUseCases()

        binding.switchCamera.setOnClickListener {
            setCameraFacing()
        }

        binding.captureImage.setOnClickListener {
            captureImage()
        }
    }

    private fun captureImage() {
        val outputDirectory = getOutputDirectory(requireContext())
        val photoFile = createFile(outputDirectory, FILENAME_FORMAT, PHOTO_EXTENSION)
        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .setMetadata(metadata)
            .build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                    Log.d("CameraFragment", "Capture Succeeded: $savedUri")

                    val mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(savedUri.toFile().extension)
                    MediaScannerConnection.scanFile(
                        requireContext(),
                        arrayOf(savedUri.toFile().absolutePath),
                        arrayOf(mimeType)
                    ) { _, uri ->
                        Log.d("CameraFragment", "Image capture scanned into media store: $uri")
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.image_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d("CameraFragment", "photo captured failed: ${exception.message}")
                    context?.let {
                        Toast.makeText(
                            it,
                            "photo captured failed: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    override fun onResume() {
        super.onResume()
        if (!hasPermissions(requireContext())) {
            launcherPermissionRequest.launch(permissions)
        } else {
            bindCameraUseCases()
        }
    }

    private fun bindCameraUseCases() = binding.viewFinder.post {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                imagePreview,
                imageCapture
            )

            var frameCounter = 0
            var lastFpsTimestamp = System.currentTimeMillis()

            imageAnalysis.setAnalyzer(cameraExecutor) { image ->
                if (!::bitmapBuffer.isInitialized) {
                    imageRotationDegrees = image.imageInfo.rotationDegrees
                    bitmapBuffer = Bitmap.createBitmap(
                        image.width,
                        image.height,
                        Bitmap.Config.ARGB_8888
                    )
                }

                if (pauseAnalysis) {
                    image.close()
                    return@setAnalyzer
                }

                image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }

                val predictions = lettuceDetector.predict(tfImage)

                reportPrediction(predictions.maxByOrNull { it.score })

                val frameCount = 10
                if (++frameCounter % frameCount == 0){
                    frameCounter = 0
                    val now = System.currentTimeMillis()
                    val delta = now - lastFpsTimestamp
                    val fps = 1000 * frameCount.toFloat() / delta
                    Log.d("CameraFragment", "FPS: ${"%.02f".format(fps)} with tensorSize: ${tfImage.width} x ${tfImage.height}")
                    lastFpsTimestamp = now
                }

            }

            imagePreview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                imagePreview,
                imageCapture,
                imageAnalysis
            )

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun reportPrediction(
        prediction: LettuceDetectionHelper.LettucePrediction?
    ) = binding.viewFinder.post {

        // Early exit: if prediction is not good enough, don't report it
        if (prediction == null || prediction.score < ACCURACY_THRESHOLD) {
            binding.boxPrediction.visibility = View.GONE
            binding.textPrediction.visibility = View.GONE
            return@post
        }

        // Location has to be mapped to our local coordinates
        val location = mapOutputCoordinates(prediction.location)

        // Update the text and UI
        binding.textPrediction.text = "${"%.2f".format(prediction.score)} ${prediction.label}"
        (binding.boxPrediction.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = location.top.toInt()
            leftMargin = location.left.toInt()
            width = min(binding.viewFinder.width, location.right.toInt() - location.left.toInt())
            height = min(binding.viewFinder.height, location.bottom.toInt() - location.top.toInt())
        }

        // Make sure all UI elements are visible
        binding.boxPrediction.visibility = View.VISIBLE
        binding.textPrediction.visibility = View.VISIBLE
    }

    private fun mapOutputCoordinates(location: RectF): RectF {

        // Step 1: map location to the preview coordinates
        val previewLocation = RectF(
            location.left * binding.viewFinder.width,
            location.top * binding.viewFinder.height,
            location.right * binding.viewFinder.width,
            location.bottom * binding.viewFinder.height
        )

        // Step 2: compensate for camera sensor orientation and mirroring
        val isFrontFacing = lensFacing == CameraSelector.LENS_FACING_FRONT
        val correctedLocation = if (isFrontFacing) {
            RectF(
                binding.viewFinder.width - previewLocation.right,
                previewLocation.top,
                binding.viewFinder.width - previewLocation.left,
                previewLocation.bottom
            )
        } else {
            previewLocation
        }

        // Step 3: compensate for 1:1 to 4:3 aspect ratio conversion + small margin
        val margin = 0.1f
        val requestedRatio = 4f / 3f
        val midX = (correctedLocation.left + correctedLocation.right) / 2f
        val midY = (correctedLocation.top + correctedLocation.bottom) / 2f
        return if (binding.viewFinder.width < binding.viewFinder.height) {
            RectF(
                midX - (1f + margin) * requestedRatio * correctedLocation.width() / 2f,
                midY - (1f - margin) * correctedLocation.height() / 2f,
                midX + (1f + margin) * requestedRatio * correctedLocation.width() / 2f,
                midY + (1f - margin) * correctedLocation.height() / 2f
            )
        } else {
            RectF(
                midX - (1f - margin) * correctedLocation.width() / 2f,
                midY - (1f + margin) * requestedRatio * correctedLocation.height() / 2f,
                midX + (1f - margin) * correctedLocation.width() / 2f,
                midY + (1f + margin) * requestedRatio * correctedLocation.height() / 2f
            )
        }
    }


    private fun setCameraFacing() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT
        else
            CameraSelector.LENS_FACING_BACK
        bindCameraUseCases()
    }

    private fun hasPermissions(context: Context) = permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.                                                     externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

    override fun onDestroyView() {
        _binding = null

        cameraExecutor.apply {
            shutdown()
            awaitTermination(1000, TimeUnit.MILLISECONDS)
        }
        tflite.close()
        nnApiDelegate.close()

        super.onDestroyView()
    }
}