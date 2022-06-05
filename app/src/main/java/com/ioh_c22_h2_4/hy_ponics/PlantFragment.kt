package com.ioh_c22_h2_4.hy_ponics

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentPlantBinding
import com.ioh_c22_h2_4.hy_ponics.ml.Lettuceclass03V4Best
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class PlantFragment : Fragment() {

    private var _binding: FragmentPlantBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val model by lazy {
        Lettuceclass03V4Best.newInstance(requireContext())
    }

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

        if (binding.ivPlantImage.drawable != null) {
            var bitmap = binding.ivPlantImage.drawable.toBitmap()
            bitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true)

            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 150, 150, 3), DataType.FLOAT32)
            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)
            val byteBuffer = tensorImage.buffer

            inputFeature0.loadBuffer(byteBuffer)

            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            model.close()

            binding.textView.text = "${outputFeature0.floatArray[0]} \n ${outputFeature0.floatArray[1]} \n ${outputFeature0.floatArray[2]} \n ${outputFeature0.floatArray[3]} \n ${outputFeature0.floatArray[4]} \n ${outputFeature0.floatArray[5]}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null


    }
}