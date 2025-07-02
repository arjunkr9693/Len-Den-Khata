### 🟢 Len Den Khata - Business Transaction Tracker 💰

**Len-Den-Khata** is a modern Android mobile application for managing finances. It lets you effortlessly **track debts, credits, personal expenses**, and view **monthly summaries**. The app is ideal for individuals and small businesses who want to replace the traditional notebook with a smart digital ledger. With **automatic cloud backup**, **offline-first support**, and **bi-directional transaction updates**, it's your all-in-one solution for safe, smart, and synced money tracking. 💸

### 🟢 Features

- 🔄 **Two-Sided Transaction Sync**: When you make or update a transaction, it reflects on both sides (creditor & debtor).
- 💰 **Debt/Credit Tracker**: Easily manage and view who owes you and whom you owe.
- ☁️ **Auto Backup/Restore**: Automatically backs up your data and restores on reinstall or app reset.
- 📶 **Network-Aware Uploads**: Data uploads happen based on connectivity status using WorkManager.
- 📇 **Contact-Based Transactions**: Add and manage transactions directly with your phone contacts.
- 🕒 **Local Storage**: All data is safely stored locally using Room + SQLite, even offline.

### 🟢 Screenshots

<p align="left">
    <img src="https://github.com/user-attachments/assets/dcf75a32-b61a-42be-9da5-380e43e46937" width="45%" hspace="10">
    <img src="https://github.com/user-attachments/assets/2392fb29-d50a-43b0-a9c0-922f23a9e991" width="45%">
    </p>
    <p align="left">
    <img src="https://github.com/user-attachments/assets/4e7d134a-a13b-443c-9bb6-880b91a8948b" width="45%" hspace="10">
    <img src="https://github.com/user-attachments/assets/15df2175-c403-40d8-a36e-50af288d3a2e" width="45%">
</p>

### 🟢 Tech Stack

- **Kotlin** — Core language for development  
- **Jetpack Compose** — For declarative and modern UI  
- **Room + SQLite** — Local storage of all financial data  
- **Hilt** — Dependency injection for cleaner architecture  
- **Coroutines + Flow** — For managing async and reactive streams  
- **Material 3** — For modern UI design  
- **AlarmManager** — For scheduling daily reminders  
- **WorkManager** — For background tasks like backup, sync, etc.  
- **MVVM Architecture** — Separation of concerns & testable code  
- **Connectivity Manager** — Detects network state for syncing
- 
### 🟢 Installation
```
    git clone https://github.com/arjunkr9693/Len-Den-Khata.git
```


### 🟢 Permissions
```
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```
### 🟢 Dependencies
```
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.lifecycle.runtime.ktx)
implementation(libs.androidx.activity.compose)
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.ui)
implementation(libs.androidx.ui.graphics)
implementation(libs.androidx.ui.tooling.preview)
implementation(libs.androidx.material3)
implementation(libs.firebase.auth)
implementation(libs.firebase.firestore)
implementation(libs.androidx.work.runtime.ktx)
implementation(libs.androidx.lifecycle.process)
implementation(libs.androidx.appcompat)
implementation(libs.material)
implementation(libs.androidx.activity)
testImplementation(libs.junit)
androidTestImplementation(libs.androidx.junit)
androidTestImplementation(libs.androidx.espresso.core)
androidTestImplementation(platform(libs.androidx.compose.bom))
androidTestImplementation(libs.androidx.ui.test.junit4)
debugImplementation(libs.androidx.ui.tooling)
debugImplementation(libs.androidx.ui.test.manifest)

implementation(libs.androidx.room.runtime)
kapt(libs.androidx.room.compiler)
implementation(libs.androidx.room.ktx)
implementation(libs.hilt.android)
kapt(libs.hilt.android.compiler)
implementation(libs.androidx.hilt.navigation.compose)
implementation (libs.gson)

//hilt worker
implementation (libs.androidx.hilt.work)
kapt ("androidx.hilt:hilt-compiler:1.2.0")

implementation("androidx.compose.material:material-icons-extended:1.5.4")
```
