# Warehouse — Android (Picker)

Мобильное приложение кладовщика для сборки отгрузок. Работает поверх REST API [`warehouse-api`](../warehouse-desktop/../warehouse-api/) — общего сервера для desktop и Android.

## Возможности

- 🔐 Авторизация через `/api/v1/auth/login` с JWT
- 📋 Список отгрузок (OUTGOING-накладные) с прогрессом и статусами
- 🎯 Лист сборки: позиции, ячейки, ввод фактического количества (`PATCH /api/v1/invoices/items/{id}/actual`)
- 📷 **Встроенный сканер штрих-кодов** (CameraX + Google ML Kit) — автоматически прокручивает к нужной позиции при совпадении кода
- 🎨 Material 3 в фирменной палитре (тёмно-синий), поддержка светлой и тёмной темы

## Конфигурация

Адрес сервера — в [`RetrofitClient.kt`](app/src/main/java/com/example/warehouse/data/api/RetrofitClient.kt):

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"   // Android emulator → host
// Для физического устройства указать LAN IP машины с warehouse-api, напр.:
// "http://192.168.1.50:8080/"
```

`10.0.2.2` — стандартный alias эмулятора Android Studio для `localhost` хост-машины.

В `AndroidManifest.xml` оставлен `android:usesCleartextTraffic="true"` для разработки. В проде поднимите HTTPS и уберите этот флаг.

## Архитектура

```
ui/screens/  ← Compose-экраны (Login, Orders, PickSheet)
ui/scanner/  ← BarcodeScannerScreen + BarcodeAnalyzer (ML Kit)
viewmodel/   ← AuthViewModel, PickViewModel (StateFlow)
data/api/    ← Retrofit ApiService + клиент
data/model/  ← DTO API + domain (PickOrder/PickTask)
data/repository/ ← маппит ApiInvoice → PickOrder для удобства экрана
```

Доменный слой (`PickOrder`/`PickTask`) намеренно отделён от DTO `ApiInvoice`/`ApiInvoiceItem` — экраны и тесты не зависят от формата ответа сервера, репозиторий выполняет преобразование.

## Сканер штрих-кодов

- Используется `androidx.camera:camera-*` 1.3.4 + `com.google.mlkit:barcode-scanning` 17.3.0.
- Поддерживаемые форматы: **EAN-8/13, UPC-A/E, Code 128/39, ITF, QR, Data Matrix**.
- При сканировании приложение возвращается к экрану сборки и прокручивает список к позиции, чей `barcode` совпал с распознанным значением. Если совпадений нет — показывает Snackbar.
- Permissions: `CAMERA` (запрашивается через Accompanist Permissions).
- В манифест добавлена директива установки модели ML Kit при инсталляции, чтобы первая попытка распознавания не тянула модель из сети:

```xml
<meta-data
    android:name="com.google.mlkit.vision.DEPENDENCIES"
    android:value="barcode"/>
```

## Запуск

1. Поднять `warehouse-api` (`docker compose up -d` или `mvn spring-boot:run` в каталоге проекта API).
2. В эмуляторе Android Studio собрать и запустить проект — `BASE_URL=http://10.0.2.2:8080/` уже настроен.
3. Войти учётной записью из таблицы `users` БД (роли `MANAGER` или `PICKER`).

## Тесты

```bash
./gradlew test
```

`ViewModelTest` покрывает `AuthViewModel` и `PickViewModel` без сетевых вызовов через Mockito-kotlin.
