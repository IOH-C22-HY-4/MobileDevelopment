package com.ioh_c22_h2_4.hy_ponics

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentIdentificationMenuBinding
import com.ioh_c22_h2_4.hy_ponics.util.Util.createTempFile
import java.io.File

class IdentificationMenuFragment : Fragment() {

    private var _binding: FragmentIdentificationMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentPhotoPath: String

    private var clicked: Boolean = false

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

    private val launcherIntentCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val photo = File(currentPhotoPath)
            }
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
                startCamera()
            }
            fabGallery.setOnClickListener {

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

    @SuppressLint("QueryPermissionsNeeded")
    private fun startCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            if (resolveActivity(requireActivity().packageManager) != null) {
                requireActivity().applicationContext.createTempFile().also {
                    val photoUri: Uri = FileProvider.getUriForFile(
                        requireActivity(),
                        "com.onirutla.storyapp",
                        it
                    )
                    currentPhotoPath = it.absolutePath
                    this.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                }
            }
        }.also {
            launcherIntentCamera.launch(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}