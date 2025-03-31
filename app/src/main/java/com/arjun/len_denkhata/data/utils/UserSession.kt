package com.arjun.len_denkhata.data.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserSession {
    var phoneNumber: String? = null
    var isContactPickerShowing = false

    fun initialize(phoneNumber: String?) {
        this.phoneNumber = phoneNumber
    }

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun login(phoneNumber: String) {
        this.phoneNumber = phoneNumber
        _isLoggedIn.value = true
    }

    fun clear() {
        phoneNumber = null
    }
}