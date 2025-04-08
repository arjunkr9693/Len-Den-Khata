package com.arjun.len_denkhata.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.arjun.len_denkhata.data.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    var isLoggedIn = false
//        private set // Prevent external modification

    var mobileNumber: String? = null
//        private set // Prevent external modification

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    private lateinit var firestore: FirebaseFirestore

//    init {
//        mobileNumber = getMobileNumber()
//        isLoggedIn = mobileNumber != null
//    }

    suspend fun login(mobileNumber: String): String? {

        UserSession.login(mobileNumber)

        if (!::firestore.isInitialized) {
            firestore = FirebaseFirestore.getInstance()
        }

        return try {
            val docRef = firestore.collection("users").document(mobileNumber)
            val doc = docRef.get().await()

            if (doc.exists()) {
                sharedPreferences.edit().putString("mobileNumber", mobileNumber).apply()
                this.mobileNumber = mobileNumber
                isLoggedIn = true
                mobileNumber
            } else {
                sharedPreferences.edit().putString("mobileNumber", mobileNumber).apply()
                firestore.collection("users").document(mobileNumber).set(mapOf("id" to mobileNumber)).await()
                this.mobileNumber = mobileNumber
                isLoggedIn = true
                mobileNumber
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getUser(): String? {
        return sharedPreferences.getString("mobileNumber", null)
    }

    fun logout() {
        sharedPreferences.edit().clear().apply()
        mobileNumber = null
        isLoggedIn = false
    }
}