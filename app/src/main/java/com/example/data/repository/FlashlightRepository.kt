package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.error.OperationType
import com.example.data.error.handleFirestoreError
import com.example.data.model.FlashlightLog
import com.example.data.model.UserSettings
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FlashlightRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserSettings(): Flow<UserSettings?> = flow {
        val uid = requireUserId()
        val path = "users/$uid/settings/current"
        emitAll(
            db.collection("users").document(uid).collection("settings").document("current")
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserSettings::class.java)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveUserSettings(
        defaultMode: String,
        strobeFrequency: Int,
        hapticFeedback: Boolean,
        screenLightColor: String = "#FFFFFF",
        screenBrightness: Double = 1.0
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("settings").document("current")
        try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val data = mapOf(
                    "userId" to uid,
                    "defaultMode" to defaultMode,
                    "strobeFrequency" to strobeFrequency,
                    "hapticFeedback" to hapticFeedback,
                    "screenLightColor" to screenLightColor,
                    "screenBrightness" to screenBrightness,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(data).await()
            } else {
                val updateData = mapOf(
                    "defaultMode" to defaultMode,
                    "strobeFrequency" to strobeFrequency,
                    "hapticFeedback" to hapticFeedback,
                    "screenLightColor" to screenLightColor,
                    "screenBrightness" to screenBrightness,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updateData).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    fun observeRecentLogs(limit: Long = 10): Flow<List<FlashlightLog>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/logs"
        emitAll(
            db.collection("users").document(uid).collection("logs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FlashlightLog::class.java)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addLog(action: String, durationSeconds: Int) {
        val uid = requireUserId()
        val logsRef = db.collection("users").document(uid).collection("logs")
        val payload = mapOf(
            "userId" to uid,
            "action" to action,
            "durationSeconds" to durationSeconds,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            logsRef.add(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, logsRef.path)
            throw e
        }
    }
}
