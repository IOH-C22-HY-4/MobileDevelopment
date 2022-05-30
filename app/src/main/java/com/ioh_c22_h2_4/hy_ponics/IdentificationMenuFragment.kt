package com.ioh_c22_h2_4.hy_ponics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentIdentificationMenuBinding

class IdentificationMenuFragment : Fragment() {

    private var _binding: FragmentIdentificationMenuBinding? = null
    private val binding get() = _binding!!

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
                fabGallery.run {
                    show()
                }
                fabCamera.run {
                    show()
                }
            }
        } else {
            binding.run {
                fabGallery.run {
                    hide()
                }
                fabCamera.run {
                    hide()
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}