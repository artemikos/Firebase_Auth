MVVM-файлы для проекта com.example.secureauth.

Скопируй файлы в app/src/main/java/com/example/secureauth/ с сохранением папок.

Папки:
- data: Firebase-репозитории
- model: модель задачи
- ui: экраны и ViewModel

Сохрани существующие google-services.json и настройки Firebase.
Код требует существующие зависимости Compose, Firebase Auth/Firestore, Credential Manager,
Google ID, kotlinx-coroutines-play-services и AndroidX Lifecycle ViewModel.
Если androidx.lifecycle.ViewModel/viewModelScope не разрешаются, добавь совместимую
с проектом зависимость lifecycle-viewmodel-ktx.
