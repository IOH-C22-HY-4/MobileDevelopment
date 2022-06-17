package com.ioh_c22_h2_4.hy_ponics.ui.plant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.ioh_c22_h2_4.hy_ponics.SharedViewModel
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentPlantBinding

class PlantFragment : Fragment() {

    private var _binding: FragmentPlantBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.uri.observe(viewLifecycleOwner) {
            Glide.with(binding.ivPlantImage)
                .load(it)
                .into(binding.ivPlantImage)
        }

        sharedViewModel.predictionResult.observe(viewLifecycleOwner) {
            binding.apply {
                tvPlantName.text = it.label
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}