# Mawaqeeti | مواقيتي 🕌

تطبيق صلاتي الذكي للأندرويد وتلفاز مخصص (Android TV) يتميز بتصميمه العصري، ويدجت حي، ونظام تنبيهات متقدم لضمان عدم فوات أي صلاة.

![Mawaqeeti Banner](https://img.shields.io/badge/Android-TV%20Support-emerald?style=for-the-badge&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-Stable-blue?style=for-the-badge&logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack-Compose-navy?style=for-the-badge&logo=jetpackcompose)

## ✨ المميزات الرئيسية

*   **📺 أذان التلفاز الذكي:** واجهة أذان كاملة تظهر تلقائياً بملء الشاشة على أجهزة Android TV عند دخول وقت الصلاة.
*   **⏱️ ويدجت حي (Live Widget):** عداد تنازلي حي في الويدجت يعرض الوقت المتبقي للصلاة القادمة ثانية بثانية.
*   **🔔 تنبيهات متعددة المراحل:** تنبيهات قبل الصلاة بـ 60 و 30 و 15 و 5 دقائق لمساعدتك في الاستعداد للصلاة.
*   **🧪 مختبر التنبيهات (Test Lab):** واجهة مخصصة لاختبار جميع أنواع التنبيهات والأذان بشكل فوري أو مجدول لضمان الجودة.
*   **🌙 واجهة Aurora:** تصميم زجاجي عصري مع خلفيات متغيرة تتناسب مع هوية التطبيق.

## 🛠️ التقنيات المستخدمة

- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern.
- **Dependency Injection:** Hilt (Dagger).
- **UI Framework:** Jetpack Compose (Modern and Reactive UI).
- **Widgets:** Android Glance (Compose-based Remote Views).
- **Storage:** Jetpack DataStore (Preferences and Proto).
- **Asynchronous Code:** Kotlin Coroutines & Flow.

## 🚀 كيف تبدأ؟

### المتطلبات الأساسية
*   Android Studio Ladybug (أو أحدث).
*   JDK 17+.
*   تطبيق يعمل على Android 8.0 (API 26) أو أحدث.

### تشغيل الاختبارات
يمكنك تشغيل اختبارات الوحدة للتأكد من سلامة المنطق البرمجي:
```bash
./gradlew test
```

### التثبيت
للتثبيت المباشر على جهازك أو المحاكي:
```bash
./gradlew installDebug
```

## 📝 ملاحظات التشغيل على التلفاز
لكي تظهر واجهة الأذان تلقائياً على التلفاز، يرجى منح التطبيق صلاحية **"الظهور فوق التطبيقات" (Display over other apps)** عند فتحه لأول مرة.

---
تم التطوير بواسطة **أمجد** بكل حب لخدمة المصلين. 🤲
