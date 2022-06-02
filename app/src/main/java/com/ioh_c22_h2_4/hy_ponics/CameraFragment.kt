package com.ioh_c22_h2_4.hy_ponics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentCameraBinding
import com.ioh_c22_h2_4.hy_ponics.util.Util.createFile
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private val permissions = listOf(Manifest.permission.CAMERA)

    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }

    private val imageCapture by lazy {
        val rotation = binding.viewFinder.display.rotation
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(RATIO_4_3)
            .setTargetRotation(rotation)
            .build()
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
        val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)
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
                            "Image capture scanned into media store: $uri",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d("CameraFragment", "photo captured failed: ${exception.message}")
                    Toast.makeText(
                        requireContext(),
                        "photo captured failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onResume() {
        super.onResume()
        if (!hasPermissions(requireContext())) {
            requestPermissions(permissions.toTypedArray(), 10)
        } else {
            bindCameraUseCases()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
            Log.d("$this", "permissions granted")
            bindCameraUseCases()
        } else {
            Log.d("$this", "permissions denied")
        }
    }


    private fun bindCameraUseCases() = binding.viewFinder.post {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            val preview = Preview.Builder()
                .setTargetAspectRatio(RATIO_4_3)
                .setTargetRotation(binding.viewFinder.display.rotation)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.apply {
            shutdown()
            awaitTermination(1000, TimeUnit.MILLISECONDS)
        }
    }

    companion object {
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
    }
}