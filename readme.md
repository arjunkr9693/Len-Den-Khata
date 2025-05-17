$\color{lightgreen}{Len \ Den \ Khata \ - \ Business \ Transaction \ Tracker \ 💰}$


Len Den Khata is an Android app designed to simplify tracking loans, debts, and financial transactions. It offers a clean interface to manage borrowers/lenders, record transactions, set reminders, and generate summaries for better financial management.

$\color{lightgreen}{Features:}$

    - **Track Loans & Debts**: Easily add and categorize entries for money given (Len) or borrowed (Den).
    - **Reminder Alerts**: Schedule reminders for pending payments or dues to avoid missed deadlines.
    - **Transaction History**: View a chronological list of all transactions with timestamps and statuses.
    - **Summary Reports**: Generate summaries to visualize total owed, owed to you, and net balance.
    - **Secure Local Storage**: Data is stored locally using Room Database for privacy and quick access.

$\color{lightgreen}{Screenshots:}$
<!-- <p>
  <img src="https://via.placeholder.com/300x600/5C5C5C/FFFFFF?text=Home+Screen" width="45%" />
  <img src="https://via.placeholder.com/300x600/5C5C5C/FFFFFF?text=Add+Entry" width="45%" />
</p>

<p>
  <img src="https://via.placeholder.com/300x600/5C5C5C/FFFFFF?text=Transaction+History" width="45%" />
  <img src="https://via.placeholder.com/300x600/5C5C5C/FFFFFF?text=Summary+Report" width="45%" />
</p> -->

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