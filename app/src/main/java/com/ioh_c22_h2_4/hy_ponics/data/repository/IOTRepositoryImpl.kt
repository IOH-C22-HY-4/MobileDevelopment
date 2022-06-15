package com.ioh_c22_h2_4.hy_ponics.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ioh_c22_h2_4.hy_ponics.data.sensor.SensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class IOTRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
) : IOTRepository {
    override fun getSensorData(): Flow<List<SensorData>> = callbackFlow {
        firebaseDatabase.goOnline()
        try {
            firebaseDatabase.reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val sensorFromFirebase = snapshot.children.first()
                    val sensorData = sensorFromFirebase.children.mapNotNull {
                        it.getValue(SensorData::class.java)
                    }
                    Log.d(this@IOTRepositoryImpl.javaClass.simpleName, "$sensorData")
                    trySend(sensorData)
                }

                override fun onCancelled(error: DatabaseError) {
                    val exception = error.toException()
                    Log.d(this@IOTRepositoryImpl.javaClass.simpleName, "$exception")
                    close(exception)
                }
            })
        } catch (e: Throwable) {
            Log.d(this@IOTRepositoryImpl.javaClass.simpleName, "$e")
            close(e)
        }
        awaitClose { firebaseDatabase.goOffline() }
    }
}