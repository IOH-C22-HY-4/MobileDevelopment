package com.ioh_c22_h2_4.hy_ponics

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentPlantBinding
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

            val bitmap =
                BitmapFactory.decodeStream(requireActivity().contentResolver.openInputStream(it))
            analyze(bitmap)
        }

    }

    private fun analyze(bitmap: Bitmap) {
//        val model = Model.newInstance(requireContext())

        val scaledBitmap = bitmap.scale(
            150,
            150,
            true
        ).copy(
            Bitmap.Config.ARGB_8888,
            true
        )

        binding.scaled.setImageBitmap(scaledBitmap)

        val tensorImage = TensorImage(DataType.FLOAT32).apply { load(scaledBitmap) }
        val byteBuffer = ByteBuffer.allocateDirect(150 * 150 * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
            put(tensorImage.buffer)
            rewind()
        }

        val input = TensorBuffer.createFixedSize(intArrayOf(1, 150, 150, 3), DataType.FLOAT32)
            .apply {
                loadBuffer(byteBuffer)
            }

//        val outputs = model.process(input)
//        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//
//        var maxValue = 0.0
//        var index = 0
//        for (i in outputFeature0.floatArray.indices) {
//            Log.d("analyze", "${outputFeature0.floatArray[i].toDouble()}")
//            if (maxValue < outputFeature0.floatArray[i]) {
//                maxValue = outputFeature0.floatArray[i].toDouble()
//                index = i
//            }
//        }
//        Log.d("analyze", "maxvalue: $maxValue, index: $index")
//
//
//        val labels = listOf(
//            "Selada Butterhead",
//            "Selada Green Romaine",
//            "Selada Hijau new grand rapids",
//            "Selada Keriting oakleaf green",
//            "Selada Merah red Rapids",
//            "Selada Red Romaine"
//        )
//
//        val lettuceResult = labels[index]
//
//        Log.d("fragment", lettuceResult)
//        binding.tvPlantName.text = lettuceResult
//
//        model.close()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null


    }
}