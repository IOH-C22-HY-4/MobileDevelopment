package com.ioh_c22_h2_4.hy_ponics.ui.plant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.ioh_c22_h2_4.hy_ponics.SharedViewModel
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentPlantBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlantFragment : Fragment() {

    private var _binding: FragmentPlantBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val viewModel: PlantViewModel by viewModels()

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
                viewModel.getBestParameter(it.label)
            }
        }

        viewModel.bestParameter.observe(viewLifecycleOwner) {
            binding.apply {
                phParameter.apply {
                    tvTittleParameter.text = "pH"
                    parameterValue.text = "${it.pHmin} - ${it.pHmax}"
                    Glide.with(ivImageParameter)
                        .load(it.phImage)
                        .into(ivImageParameter)
                }

                ecParameter.apply {
                    tvTittleParameter.text = "EC"
                    parameterValue.text = "${it.ECmin} - ${it.ECmax}"
                    Glide.with(ivImageParameter)
                        .load(it.ecImage)
                        .into(ivImageParameter)
                }

                tdsParameter.apply {
                    tvTittleParameter.text = "TDS"
                    parameterValue.text = "${it.TDSmin} - ${it.TDSmax}"
                    Glide.with(ivImageParameter)
                        .load(it.tdsImage)
                        .into(ivImageParameter)
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}