package com.example.clarimind.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseSyncHelper {
    private val db = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "anonymous"
    }

    fun uploadNotificationEvent(event: NotificationEventEntity) {
        val userId = getCurrentUserId()
        db.collection("users")
            .document(userId)
            .collection("notification_events")
            .add(event)
    }

    fun uploadAppUsage(appUsage: AppUsageEntity) {
        val userId = getCurrentUserId()
        db.collection("users")
            .document(userId)
            .collection("app_usage")
            .add(appUsage)
    }
} 