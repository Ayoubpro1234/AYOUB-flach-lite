package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FlashlightLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val action: String = "ON",
    val durationSeconds: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null
)
