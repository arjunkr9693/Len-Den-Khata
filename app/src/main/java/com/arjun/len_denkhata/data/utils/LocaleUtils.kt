package com.arjun.len_denkhata.data.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleUtils {
    fun setLocale(context: Context, language: String){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(language)
        }
        else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
        }
    }
}

fun saveLanguageToPrefs(context: Context, language: String) {

    LocaleUtils.setLocale(context, language)
    val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    sharedPref.edit().putString("selected_language", language).apply()
}

fun getSavedLanguageFromPrefs(context: Context): String? {
    val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    return sharedPref.getString("selected_language", null)
}

