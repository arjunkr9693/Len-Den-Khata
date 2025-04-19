package com.arjun.len_denkhata.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.arjun.len_denkhata.R
import com.arjun.len_denkhata.data.utils.AppLanguage
import com.arjun.len_denkhata.data.utils.saveLanguageToPrefs

import com.arjun.len_denkhata.ui.viewmodel.MoreViewModel

@Composable
fun MoreScreen(viewModel: MoreViewModel = hiltViewModel(), navController: NavHostController) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(stringResource(R.string.more), navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDialog = true },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.GTranslate,
                        contentDescription = stringResource(R.string.select_language)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.select_language),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(stringResource(R.string.select_language)) },
                    text = {
                        Column {
                            LanguageOption(
                                languageNameRes = R.string.english,
                                languageCode = AppLanguage.ENGLISH.code,
                                onLanguageSelected = { code ->
                                    saveLanguageToPrefs(context, code)
                                    showDialog = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.GTranslate,
                                        contentDescription = stringResource(R.string.english)
                                    )
                                }
                            )
                            LanguageOption(
                                languageNameRes = R.string.hindi,
                                languageCode = AppLanguage.HINDI.code,
                                onLanguageSelected = { code ->
                                    saveLanguageToPrefs(context, code)
                                    showDialog = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.GTranslate,
                                        contentDescription = stringResource(R.string.hindi)
                                    )
                                }
                            )
                            // Add more language options here if needed
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showDialog = false }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageOption(
    @androidx.annotation.StringRes languageNameRes: Int,
    languageCode: String,
    onLanguageSelected: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLanguageSelected(languageCode) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon?.invoke()
        if (leadingIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(stringResource(languageNameRes))
    }
}