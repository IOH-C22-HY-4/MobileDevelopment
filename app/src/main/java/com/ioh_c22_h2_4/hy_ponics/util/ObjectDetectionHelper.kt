package com.ioh_c22_h2_4.hy_ponics.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ObjectDetectionHelper(private val tflite: Interpreter, private val labels: List<String>) {

    /** Abstraction object that wraps a prediction output in an easy to parse way */

    private val scores = arrayOf(FloatArray(OBJECT_COUNT))

    val predictions
        get() = (0 until OBJECT_COUNT).map {
            PredictionResult(
                // SSD Mobilenet V1 Model assumes class 0 is background class
                // in label file and class labels start from 1 to number_of_classes + 1,
                // while outputClasses correspond to class index from 0 to number_of_classes
                label = labels[it],

                // Score is a single value of [0, 1]
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