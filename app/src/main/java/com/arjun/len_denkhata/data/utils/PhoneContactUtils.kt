package com.arjun.len_denkhata.data.utils

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract

object PhoneContactUtils {

    fun getRecentPhoneNumbers(contentResolver: ContentResolver): List<String> {
        val numbers = mutableListOf<String>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} DESC LIMIT 10")
        cursor?.use {
            val numberColumnIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val number = it.getString(numberColumnIndex)
                numbers.add(number.replace(Regex("[^\\d+]"), ""))
            }
        }
        return numbers
    }
}