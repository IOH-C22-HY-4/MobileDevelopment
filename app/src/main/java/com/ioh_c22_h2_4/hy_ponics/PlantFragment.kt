package com.ioh_c22_h2_4.hy_ponics

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.ioh_c22_h2_4.hy_ponics.databinding.FragmentPlantBinding
import com.ioh_c22_h2_4.hy_ponics.ml.Model
import com.ioh_c22_h2_4.hy_ponics.util.Constants.IMAGE_INPUT_SIZE
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
        }

        binding.ivPlantImage.drawable?.let {
            val bitmap = it.toBitmap()
            analyze(bitmap)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun analyze(bitmap: Bitmap) {
        val model = Model.newInstance(requireContext())

        val scaledBitmap = scaleBitmap(bitmap)

        val tensorImage = TensorImage(DataType.FLOAT32).apply { load(scaledBitmap) }
        val byteBuffer =
            ByteBuffer.allocateDirect(IMAGE_INPUT_SIZE * IMAGE_INPUT_SIZE * 3 * 4).apply {
                order(ByteOrder.nativeOrder())
                put(tensorImage.buffer)
                rewind()
            }

        val input = TensorBuffer.createFixedSize(
            intArrayOf(
                1,
                IMAGE_INPUT_SIZE,
                IMAGE_INPUT_SIZE,
                3
            ),
            DataType.FLOAT32
        ).apply {
            loadBuffer(byteBuffer)
        }

        val outputs = model.process(input)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val resultIdentification = identifyPlant(outputFeature0)

        val label = getLabelByIndex(resultIdentification.first)

        Log.d("fragment", label)
        binding.tvPlantName.text = label

        model.close()
    }

    private fun getLabelByIndex(index: Int): String {
        val labels = listOf(
            "Selada Butterhead",
            "Selada Green Romaine",
            "Selada Hijau new grand rapids",
            "Selada Keriting oakleaf green",
            "Selada Merah red Rapids",
            "Selada Red Romaine"
        )

        return labels[index]
    }

    private fun identifyPlant(tensorBuffer: TensorBuffer): Pair<Int, Double> {
        var maxValue = 0.0
        var index = 0
        for (i in tensorBuffer.floatArray.indices) {
            Log.d("analyze", "${tensorBuffer.floatArray[i].toDouble()}")
            if (maxValue < tensorBuffer.floatArray[i]) {
                maxValue = tensorBuffer.floatArray[i].toDouble()
                index = i
            }
        }
        Log.d("analyze", "maxvalue: $maxValue, index: $index")
        return index to maxValue
    }

    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        val resultBitmap = Bitmap.createBitmap(
            IMAGE_INPUT_SIZE,
            IMAGE_INPUT_SIZE,
            Bitmap.Config.ARGB_8888
        )

        val ratioX = IMAGE_INPUT_SIZE / bitmap.width.toFloat()
        val ratioY = IMAGE_INPUT_SIZE / bitmap.height.toFloat()
        val middleX = IMAGE_INPUT_SIZE / 2f
        val middleY = IMAGE_INPUT_SIZE / 2f

        val scaleMatrix = Matrix().apply {
            setScale(ratioX, ratioY, middleX, middleY)
        }

        Canvas(resultBitmap).run {
            setMatrix(scaleMatrix)
            drawBitmap(
                bitmap,
                middleX - bitmap.width / 2,
                middleY - bitmap.height / 2,
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
        }

        return resultBitmap
    }
}