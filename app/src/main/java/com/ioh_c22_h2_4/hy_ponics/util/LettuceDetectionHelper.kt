package com.ioh_c22_h2_4.hy_ponics.util

import android.graphics.RectF
import com.ioh_c22_h2_4.hy_ponics.util.Constants.OBJECT_COUNT
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage

class LettuceDetectionHelper(private val tflite: Interpreter, private val labels: List<String>) {

    data class LettucePrediction(val location: RectF, val label: String, val score: Float)

    private val locations = arrayOf(FloatArray(OBJECT_COUNT))
    private val labelIndices = arrayOf(FloatArray(OBJECT_COUNT))
    private val scores = arrayOf(FloatArray(OBJECT_COUNT))

    private val outputBuffer = mapOf(
        0 to locations,
        1 to labelIndices,
        2 to scores,
        3 to FloatArray(1)
    )

    val predictions
        get() = (0 until OBJECT_COUNT).map { objectCount ->
            LettucePrediction(
                location = locations[objectCount].let {
                    RectF(it[1], it[0], it[3], it[2])
                },

                label = labels[1 + labelIndices[0][objectCount].toInt()],
                score = scores[0][objectCount]
            )
        }

    fun predict(image: TensorImage): List<LettucePrediction> {
        tflite.runForMultipleInputsOutputs(arrayOf(image.buffer), outputBuffer)
        return predictions
    }
}