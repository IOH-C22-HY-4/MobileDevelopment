package com.ioh_c22_h2_4.hy_ponics.util

import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ObjectDetectionHelper(private val tflite: Interpreter, private val labels: List<String>) {

    private val scores = arrayOf(FloatArray(OBJECT_COUNT))

    val predictions
        get() = (0 until OBJECT_COUNT).map {
            PredictionResult(
                label = labels[it],
                score = scores[0][it]
            )
        }

    fun predict(image: TensorImage): List<PredictionResult> {
        val byteBuffer = ByteBuffer.allocateDirect(150 * 150 * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
            put(image.buffer)
        }
        tflite.run(byteBuffer, scores)
        return predictions
    }

    companion object {
        const val OBJECT_COUNT = 6
    }
}