package com.ioh_c22_h2_4.hy_ponics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentCameraBinding
import com.ioh_c22_h2_4.hy_ponics.util.Constants.FILENAME_FORMAT
import com.ioh_c22_h2_4.hy_ponics.util.Constants.LABELS_PATH
import com.ioh_c22_h2_4.hy_ponics.util.Constants.MODEL_PATH
import com.ioh_c22_h2_4.hy_ponics.util.Constants.PHOTO_EXTENSION
import com.ioh_c22_h2_4.hy_ponics.util.Util.createFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private var imageRotationDegrees: Int = 0

    private val permissions by lazy { arrayOf(Manifest.permission.CAMERA) }

    private val rotation by lazy { binding.viewFinder.display.rotation }

    private lateinit var bitmapBuffer: Bitmap

    private val imageCapture by lazy {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(RATIO_4_3)
            .setTargetRotation(rotation)
            .build()
    }

    private val nnApiDelegate by lazy { NnApiDelegate() }

    private val tflite by lazy {
        Interpreter(
            FileUtil.loadMappedFile(requireContext(), MODEL_PATH),
            Interpreter.Options().addDelegate(nnApiDelegate)
        )
    }

    private val tfInputSize by lazy {
        val inputIndex = 0
        val inputShape = tflite.getInputTensor(inputIndex).shape()
        Size(inputShape[2], inputShape[1])
    }

    private val tfImageBuffer = TensorImage(DataType.FLOAT32)

    private val detector by lazy {
        ObjectDetectionHelper(
            tflite,
            FileUtil.loadLabels(requireContext(), LABELS_PATH)
        )
    }

    private val tfImageProcessor by lazy {
        val cropSize = minOf(bitmapBuffer.width, bitmapBuffer.height)
        ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(
                ResizeOp(
                    tfInputSize.height, tfInputSize.width, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR
                )
            )
            .add(Rot90Op(-imageRotationDegrees / 90))
            .add(NormalizeOp(0f, 1f))
            .build()

    }

    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }

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

    override fun onResume() {
        super.onResume()
        if (!hasPermissions(requireContext())) {
            launcherPermissionRequest.launch(permissions)
        } else {
            bindCameraUseCases()
        }
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
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                            sharedViewModel.setUri(uri)
                            findNavController().navigate(CameraFragmentDirections.actionCameraFragmentToTanamanFragment())
                        }
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

    private fun bindCameraUseCases() = binding.viewFinder.post {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val imagePreview = Preview.Builder()
                .setTargetAspectRatio(RATIO_4_3)
                .setTargetRotation(rotation)
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(RATIO_4_3)
                .setTargetRotation(rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { image ->
                if (!::bitmapBuffer.isInitialized) {
                    imageRotationDegrees = image.imageInfo.rotationDegrees
                    bitmapBuffer = Bitmap.createBitmap(
                        image.width,
                        image.height,
                        Bitmap.Config.ARGB_8888
                    )
                }

                image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }

                val tfImage = tfImageProcessor.process(tfImageBuffer.apply { load(bitmapBuffer) })

                val prediction = detector.predict(tfImage)
                Log.d("CameraFragment", "$prediction")
            }


            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                imagePreview,
                imageCapture,
                imageAnalysis
            )


            imagePreview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

        }, ContextCompat.getMainExecutor(requireContext()))
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
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

    class ObjectDetectionHelper(private val tflite: Interpreter, private val labels: List<String>) {

        /** Abstraction object that wraps a prediction output in an easy to parse way */
        data class ObjectPrediction(val label: String, val score: Float)

        private val scores = arrayOf(FloatArray(OBJECT_COUNT))

        val predictions
            get() = (0 until OBJECT_COUNT).map {
                ObjectPrediction(
                    // SSD Mobilenet V1 Model assumes class 0 is background class
                    // in label file and class labels start from 1 to number_of_classes + 1,
                    // while outputClasses correspond to class index from 0 to number_of_classes
                    label = labels[it],

                    // Score is a single value of [0, 1]
                    score = scores[0][it]
                )
            }

        fun predict(image: TensorImage): List<ObjectPrediction> {
            val byteBuffer = ByteBuffer.allocateDirect(150 * 150 * 3 * 4).apply {
                order(ByteOrder.nativeOrder())
                put(image.buffer)
            }
            tflite.run(byteBuffer, scores)
            return predictions
        }

        companion object {
            const val OBJECT_COUNT = 6
        }
    }

}