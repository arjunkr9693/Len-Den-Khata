package com.arjun.len_denkhata.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.Screen
import com.arjun.len_denkhata.data.utils.AppLanguage
import com.arjun.len_denkhata.data.utils.saveLanguageToPrefs

@Composable
fun LanguageSelectionScreen(navController: NavController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // Add spacing between items
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                stringResource(R.string.skip),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        saveLanguageToPrefs(context, AppLanguage.ENGLISH.code)
                        navController.navigate(Screen.Login.route) {
                            popUpTo("language_selection") { inclusive = true }
                        }
                    }
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // Push content to the center

        LanguageCard(
            languageCode = AppLanguage.ENGLISH.code,
            languageNameRes = R.string.english,
            onLanguageSelected = { code ->
                saveLanguageToPrefs(context, code)
                navController.navigate(Screen.Login.route) {
                    popUpTo("language_selection") { inclusive = true }
                }
            },
            modifier = Modifier.size(120.dp) // Make card square and smaller
        )

        LanguageCard(
            languageCode = AppLanguage.HINDI.code,
            languageNameRes = R.string.hindi,
            onLanguageSelected = { code ->
                saveLanguageToPrefs(context, code)
                navController.navigate(Screen.Login.route) {
                    popUpTo("language_selection") { inclusive = true }
                }
            },
            modifier = Modifier.size(120.dp) // Make card square and smaller
        )

        Spacer(modifier = Modifier.weight(1f)) // Push content to the center
    }
}

@Composable
fun LanguageCard(
    languageCode: String,
    @StringRes languageNameRes: Int,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onLanguageSelected(languageCode) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(languageNameRes),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}