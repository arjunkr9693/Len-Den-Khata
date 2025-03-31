package com.arjun.len_denkhata.data.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserSession {
    private val _observablePhoneNumber = MutableStateFlow<String?>(null)
    val observablePhoneNumber: StateFlow<String?> = _observablePhoneNumber.asStateFlow()

    val phoneNumber: String?
        get() = _observablePhoneNumber.value

    var isContactPickerShowing = false

    fun initialize(phoneNumber: String?) {
        _observablePhoneNumber.value = phoneNumber
    }

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun login(phoneNumber: String) {
        _observablePhoneNumber.value = phoneNumber
        _isLoggedIn.value = true
    }

    fun clear() {
        _observablePhoneNumber.value = null
    }
}