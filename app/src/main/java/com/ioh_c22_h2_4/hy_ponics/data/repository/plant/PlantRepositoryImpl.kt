package com.ioh_c22_h2_4.hy_ponics.data.repository.plant

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ioh_c22_h2_4.hy_ponics.data.plant.Plant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class PlantRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PlantRepository {
    override fun getBestParameterBy(query: String): Flow<Plant> = callbackFlow {
        firestore.collection("parameter").document(query).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    val plant = snapshot.toObject(Plant::class.java)
                    plant?.let {
                        Log.d(this@PlantRepositoryImpl.javaClass.simpleName, "$it")
                        trySend(it)
                    }
                } else {
                    Log.d(this@PlantRepositoryImpl.javaClass.simpleName, "$snapshot")
                }
            }
            .addOnFailureListener {
                close(it)
            }
        awaitClose { return@awaitClose }
    }
}