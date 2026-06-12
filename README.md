# Mawaqeeti 🕌

> **A smart, modern Android prayer companion — beautifully designed for both phones and Android TV.**

Mawaqeeti delivers precise daily prayer times, a live home-screen widget, multi-phase alarm reminders, and a full-screen Adhan experience on Android TV — all wrapped in a stunning glassmorphism UI.

![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Android%20TV-brightgreen?style=for-the-badge&logo=android)
![Language](https://img.shields.io/badge/Language-Kotlin-blue?style=for-the-badge&logo=kotlin)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-navy?style=for-the-badge&logo=jetpackcompose)
![Multilingual](https://img.shields.io/badge/Languages-English%20%7C%20Arabic-orange?style=for-the-badge)

---

## ✨ Key Features

| Feature | Description |
|---|---|
| 📺 **Android TV Adhan** | Full-screen Adhan UI auto-launches on TV at prayer time |
| ⏱️ **Live Countdown Widget** | Real-time second-by-second countdown on your home screen |
| 🔔 **Multi-Phase Reminders** | Alerts at 60, 30, 15, 5 min before each prayer |
| 🌍 **Bilingual** | Full English & Arabic support with auto RTL/LTR layout |
| 🧪 **Test Lab** | Schedule a test alarm in 9 seconds or 9 minutes for instant verification |
| 🌊 **Aurora UI** | Dynamic Lava Lamp background that reacts to your prayer progress |
| 📍 **Global Prayer Times** | Powered by the Aladhan API with multiple calculation methods |

---

## 🛠️ Tech Stack

- **Architecture:** MVVM + Repository Pattern
- **Dependency Injection:** Hilt (Dagger)
- **UI Framework:** Jetpack Compose
- **Home Screen Widget:** Glance (Compose-based RemoteViews)
- **Storage:** Jetpack DataStore (Preferences)
- **Async:** Kotlin Coroutines & Flow
- **Network:** Retrofit + Gson (Aladhan API)
- **Background Tasks:** AlarmManager with `setExactAndAllowWhileIdle` + WakeLock

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+
- Android 8.0+ (API 26) device or emulator

### Run Unit Tests
```bash
./gradlew :app:testDebugUnitTest
```

### Install on Device / TV
```bash
./gradlew installDebug
```

---

## 📱 Language Support

Switch between **English** (default) and **Arabic** from **Settings → Language**.  
The app automatically applies the correct text direction:
- **English** → Left-to-Right (LTR)
- **Arabic** → Right-to-Left (RTL)

---

## 📺 Android TV Notes

For the full-screen Adhan to appear automatically on TV, grant the **"Display over other apps"** permission when prompted on first launch. Without this, the system restricts foreground window launching.

---

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

---

*Built with ❤️ by **Amjad** — may it never let you miss a prayer.* 🤲
