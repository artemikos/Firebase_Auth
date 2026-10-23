# Secure Auth Lab — Android / Firebase

Пример проекта для лабораторной: регистрация и вход Email/Password, Credential Manager, Google ID token → Firebase Auth, профиль, TODO-задачи в Cloud Firestore, выход и серверные правила доступа.

## Перед запуском — обязательная настройка Firebase

Проект нельзя подключить к вашему Firebase без конфигурации, которую создаёт владелец проекта. В репозитории намеренно нет чужого `google-services.json`.

1. Откройте https://console.firebase.google.com/ и создайте Firebase project.
2. Добавьте Android app с package name **`com.example.secureauth`**.
3. Скачайте `google-services.json` и положите его в `app/google-services.json`.
4. В Authentication → Sign-in method включите **Email/Password** и **Google**. Для Google укажите support email.
5. В Android app settings добавьте SHA-1 debug certificate. Из корня проекта выполните `gradlew signingReport` (Windows: `gradlew.bat signingReport`). Для локальной сборки можно использовать SHA-1 из `debug`.
6. После настройки Google скачайте свежий `google-services.json` повторно и замените файл в `app/`.
7. В Firestore Database создайте базу данных.
8. Установите Firebase CLI и опубликуйте правила из `firestore.rules`:

   ```bash
   npm install -g firebase-tools
   firebase login
   firebase use --add
   firebase deploy --only firestore:rules
   ```

   Либо скопируйте содержимое `firestore.rules` в Firebase Console → Firestore Database → Rules и нажмите Publish.
9. Откройте корень проекта в Android Studio, дождитесь Gradle Sync и запустите на эмуляторе/устройстве с Google Play. Требуется JDK 17.

## Что реализовано

- Регистрация и вход через Firebase Auth Email/Password.
- После регистрации приложение вызывает Android Credential Manager `CreatePasswordRequest`; пароль не записывается в SharedPreferences, базу приложения или Firestore.
- Системный Credential Manager запрос содержит одновременно `GetPasswordOption` и `GetGoogleIdOption`. Выбранный пароль идёт в Firebase Email/Password; Google ID token обменивается на Firebase credential.
- Сессия восстанавливается Firebase Auth после перезапуска. UI показывает защищённую часть только при `currentUser != null`.
- Профиль: имя, email, UID, подтверждение email, способ входа.
- CRUD задач в `users/{uid}/tasks`; серверные Firestore Rules проверяют `request.auth.uid == userId`.
- Выход вызывает `FirebaseAuth.signOut()` и `CredentialManager.clearCredentialState()`; приложение использует один экран состояния, поэтому старый защищённый экран не остаётся в back stack.

## Проверка лабораторной

1. Создайте пользователя A, добавьте задачу.
2. Выйдите и зарегистрируйте пользователя B: задача A не должна быть видна.
3. Попробуйте запросить документ A при авторизации B (через Firestore Rules Playground или тест): ожидается `PERMISSION_DENIED`.
4. Перезапустите приложение с активной сессией: откроется экран задач. После выхода и повторного запуска откроется экран входа.
5. Проверьте Google на эмуляторе/устройстве с Google Play, где добавлен аккаунт Google.
6. Для проверки Credential Manager используйте Android-устройство/эмулятор с актуальными Google Play Services и поддерживаемым менеджером учётных данных. Системный интерфейс зависит от версии Android и настроек аккаунта.

## Важные замечания

- `google-services.json`, SHA-1, включённые провайдеры и опубликованные Firestore Rules — обязательные настройки вашей Firebase Console; это невозможно преднастроить в исходниках за вас.
- Текущий проект — учебный минимальный TODO-клиент. Для production стоит добавить валидацию/подтверждение email, обработку восстановления пароля, тесты правил и более строгую схему документов.
- Если у преподавателя обязательна именно база `make-it-so-android/v2`, этот проект реализует требуемый функционал в самостоятельном Compose-приложении, а не является патчем поверх Make It So.
