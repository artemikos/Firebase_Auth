# Secure Auth Lab — Android / Firebase
## Что реализовано

- Регистрация и вход через Firebase Auth Email/Password.
- После регистрации приложение вызывает Android Credential Manager `CreatePasswordRequest`; пароль не записывается в SharedPreferences, базу приложения или Firestore.
- Системный Credential Manager запрос содержит одновременно `GetPasswordOption` и `GetGoogleIdOption`. Выбранный пароль идёт в Firebase Email/Password; Google ID token обменивается на Firebase credential.
- Сессия восстанавливается Firebase Auth после перезапуска. UI показывает защищённую часть только при `currentUser != null`.
- Профиль: имя, email, UID, подтверждение email, способ входа.
