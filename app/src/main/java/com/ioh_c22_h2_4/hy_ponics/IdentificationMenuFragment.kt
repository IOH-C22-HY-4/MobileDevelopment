package com.ioh_c22_h2_4.hy_ponics

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentIdentificationMenuBinding

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

    private val launcherIntentGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val uri = it.data?.data as Uri
                val photo = uri.toFile()
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
                view.findNavController()
                    .navigate(
                        IdentificationMenuFragmentDirections.actionIdentificationMenuFragmentToCameraFragment()
                    )
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