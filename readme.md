$\color{lightgreen}{Len \ Den \ Khata \ - \ Business \ Transaction \ Tracker \ 💰}$


Len Den Khata is an Android app designed to simplify tracking loans, debts, and financial transactions. It offers a clean interface to manage borrowers/lenders, record transactions, set reminders, and generate summaries for better financial management.

$\color{lightgreen}{Features:}$

    - **Track Loans & Debts**: Easily add and categorize entries for money given (Len) or borrowed (Den).
    - **Reminder Alerts**: Schedule reminders for pending payments or dues to avoid missed deadlines.
    - **Transaction History**: View a chronological list of all transactions with timestamps and statuses.
    - **Summary Reports**: Generate summaries to visualize total owed, owed to you, and net balance.
    - **Secure Local Storage**: Data is stored locally using Room Database for privacy and quick access.

$\color{lightgreen}{\text{Screenshots:}}$

<p align="left">
<img src="https://github.com/user-attachments/assets/dcf75a32-b61a-42be-9da5-380e43e46937" width="45%" hspace="10">
<img src="https://github.com/user-attachments/assets/2392fb29-d50a-43b0-a9c0-922f23a9e991" width="45%">
</p>
<p align="left">
<img src="https://github.com/user-attachments/assets/4e7d134a-a13b-443c-9bb6-880b91a8948b" width="45%" hspace="10">
<img src="https://github.com/user-attachments/assets/15df2175-c403-40d8-a36e-50af288d3a2e" width="45%">
</p>

$\color{lightgreen}{TechStacks:}$

    - **Kotlin**: Primary language for Android development.
    - **Jetpack Compose**: Modern UI toolkit for building dynamic interfaces.
    - **Room Database**: Local storage for transaction and contact data.
    - **WorkManager**: Schedule payment reminders and background tasks.
    - **MPAndroidChart**: Generate interactive financial summary charts.
    - **ViewModel & LiveData**: Manage UI data lifecycle efficiently.

$\color{lightgreen}{Installation:}$
```
    git clone https://github.com/arjunkr9693/Len-Den-Khata.git
```


$\color{lightgreen}{How \ It \ Works:}$

    - **Add Entries**: Input details (amount, date, person) for loans/debts with optional notes.
    - **Track Status**: Mark transactions as paid/pending and filter by date or person.
    - **Set Reminders**: Schedule notifications for upcoming payment deadlines.
    - **Generate Reports**: View pie/bar charts for financial overviews.
    - **Export Data**: Share transaction history as CSV/PDF files.

$\color{lightgreen}{Dependencies:}$
```
implementation "androidx.room:room-runtime:2.5.0"
implementation "androidx.work:work-runtime-ktx:2.8.1"
implementation "com.github.PhilJay:MPAndroidChart:v3.1.0"
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1"
implementation "androidx.compose.material3:material3:1.1.2"
```
