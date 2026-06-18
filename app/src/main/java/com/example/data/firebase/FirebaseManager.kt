package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    
    @Volatile
    var isInitialized = false
        private set

    fun initializeSafe(context: Context, apiKey: String? = null, appId: String? = null, projectId: String? = null) {
        if (isInitialized) return
        
        synchronized(this) {
            if (isInitialized) return
            try {
                // First try: Is dynamic config provided via params or env variables?
                val finalApiKey = apiKey?.takeIf { it.isNotBlank() } ?: try { BuildConfig.FIREBASE_API_KEY.takeIf { it != "UNCONFIGURED" } } catch (e: Exception) { null }
                val finalAppId = appId?.takeIf { it.isNotBlank() } ?: try { BuildConfig.FIREBASE_APP_ID.takeIf { it != "UNCONFIGURED" } } catch (e: Exception) { null }
                val finalProjectId = projectId?.takeIf { it.isNotBlank() } ?: try { BuildConfig.FIREBASE_PROJECT_ID.takeIf { it != "UNCONFIGURED" } } catch (e: Exception) { null }

                if (finalApiKey != null && finalAppId != null && finalProjectId != null) {
                    val options = FirebaseOptions.Builder()
                        .setApiKey(finalApiKey)
                        .setApplicationId(finalAppId)
                        .setProjectId(finalProjectId)
                        .build()
                    FirebaseApp.initializeApp(context, options)
                    isInitialized = true
                    Log.d(TAG, "Firebase initialized manually inside UDC Service")
                    return
                }

                // Second try: Has system automatically initialized Firebase?
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    isInitialized = true
                    Log.d(TAG, "Firebase initialized automatically by Google-Services plugin")
                    return
                }

                Log.w(TAG, "No Firebase configuration found. Fallback to robust offline-first mode.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Firebase dynamically: ${e.message}")
            }
        }
    }

    val auth: FirebaseAuth?
        get() = if (isInitialized) {
            try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        } else null

    val firestore: FirebaseFirestore?
        get() = if (isInitialized) {
            try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
        } else null
}
