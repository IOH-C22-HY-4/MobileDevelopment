package com.ioh_c22_h2_4.hy_ponics

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentIdentificationMenuBinding
import com.ioh_c22_h2_4.hy_ponics.util.Constants.FILENAME_FORMAT
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class IdentificationMenuFragment : Fragment() {

    private var _binding: FragmentIdentificationMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentPhotoPath: String

    private var clicked: Boolean = false

    private val launcherIntentGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                val uri = activityResult.data?.data as Uri
                val photo = uri.uriToFile(requireContext())
                Log.d("Identification", photo.absolutePath)
                findNavController().navigate(
                    IdentificationMenuFragmentDirections.actionIdentificationMenuFragmentToTanamanFragment(
                        photo.absolutePath
                    )
                )
            }
        }

    private fun Uri.uriToFile(context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = context.createTempFile()

        val inputStream = contentResolver.openInputStream(this) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private fun Context.createTempFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val timeStamp =
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private val rotateOpen by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_open
        )
    }

    private val rotateClose by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_close
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIdentificationMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnAdd.setOnClickListener {
                onAddButtonClicked()
            }
            fabCamera.setOnClickListener {
                view.findNavController()
                    .navigate(
                        IdentificationMenuFragmentDirections.actionIdentificationMenuFragmentToCameraFragment()
                    )
            }
            fabGallery.setOnClickListener {
                startGallery()
            }
        }
    }

    private fun onAddButtonClicked() {
        setVisibility()
        setAnimation()
        clicked = !clicked
    }

    private fun setVisibility() {
        if (!clicked) {
            binding.run {
                fabGallery.show()
                fabCamera.show()
            }
        } else {
            binding.run {
                fabGallery.hide()
                fabCamera.hide()
            }
        }
    }

    private fun setAnimation() {
        if (!clicked) {
            binding.btnAdd.startAnimation(rotateOpen)
        } else {
            binding.btnAdd.startAnimation(rotateClose)
        }
    }

    private fun startGallery() {
        Intent().apply {
            action = ACTION_GET_CONTENT
            type = "image/*"
        }.also {
            val chooser = Intent.createChooser(it, "Choose a picture")
            launcherIntentGallery.launch(chooser)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}