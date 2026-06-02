<div align="center">

<img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
<img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white"/>
<img src="https://img.shields.io/badge/Material_3-757575?style=for-the-badge&logo=material-design&logoColor=white"/>
<img src="https://img.shields.io/badge/minSdk_26-Android_8.0+-green?style=for-the-badge"/>

# 🏫 Maktab

**O'zbekiston maktablari uchun zamonaviy boshqaruv tizimi**

*Android · Kotlin · Jetpack Compose · Material Design 3*

</div>

---

## 📱 Loyiha haqida

**Maktab** — o'quv jarayonini raqamlashtiruvchi Android mobil ilovasi. Har bir rol uchun alohida panel: o'qituvchi darslarini, ota-ona farzandini, oshpaz kafeteriyani — barchasini bitta ilovada boshqaradi.

---

## ✨ Xususiyatlar

### 🔐 Autentifikatsiya
- **Role-based login** — backend `role` field qaytaradi, mos dashboardga yo'naltiradi
- **30-kunlik PIN arxitekturasi** — bir marta login → PIN o'rnat → kunlik PIN bilan kir
- **EncryptedSharedPreferences** — AES-256 bilan shifrlangan xavfsiz saqlash
- **5 xato urinish** → barcha ma'lumotlar tozalanadi

### 🧭 Navigation
- **Drawer Navigation** — 3 rol uchun hamburger menyu (har xil rangda)
- **BackHandler** — drawer/sozlamalar yopilishi, ilovadan chiqish dialogi

### 👥 Rollar

| Rol | Rang | Bo'limlar |
|-----|------|-----------|
| 🎓 O'qituvchi | Yashil (Teal) | Jadval, Jurnal, Dars kontenti, Davomat, Baholash, Sinflarim |
| 👨‍👩‍👦 Ota-ona | Ko'k (Blue) | Jadval, Davomat, Baholar, Uyga vazifa, Xulq-atvor, Shikoyat |
| 🍳 Oshpaz | Sariq (Amber) | Bosh panel, Ombor, Ingredientlar, Retseptlar, Menyu, Stock, Analitika |

---

## 📸 Ekranlar

```
Splash → Rol tanlash → Login → PIN o'rnatish → Dashboard
                                     ↓
                              PIN kiritish (qayta kirish)
```

| Ekran | Tavsif |
|-------|--------|
| **Splash** | Animatsiyali logo, session tekshiruvi |
| **LandingScreen** | 3 ta rol kartochkasi |
| **LoginScreen** | Username/parol, rol badge, rang aksenti |
| **PinSetupScreen** | 4 xonali PIN o'rnatish + tasdiqlash |
| **PinEntryScreen** | Kunlik kirish, shake animatsiya |
| **DrawerApp** | Teacher/Parent uchun drawer navigation |
| **ChefApp** | Oshpaz uchun amber rang drawer |
| **SozlamalarScreen** | Profil, tungi rejim, til (UZ/RU/EN) |

---

## 🗂️ Loyiha tuzilmasi

```
app/src/main/java/com/maktab/app/
│
├── MainActivity.kt
│
├── ui/
│   ├── MaktabApp.kt              ← Asosiy navigation + DrawerApp + ChefApp
│   │
│   ├── theme/
│   │   ├── Color.kt              ← Rang palitrasi (Teal, Blue, Amber, ...)
│   │   └── Theme.kt              ← MaktabTheme, dark mode, til
│   │
│   ├── components/
│   │   └── Components.kt         ← Umumiy komponentlar (StatCard, StatusChip, ...)
│   │
│   └── screens/
│       ├── SplashScreen.kt
│       ├── LandingScreen.kt      ← Rol tanlash (Teacher / Parent / Chef)
│       ├── LoginScreen.kt        ← Kirish ekrani
│       ├── PinScreens.kt         ← PIN o'rnatish va kiritish
│       ├── SozlamalarScreen.kt   ← Sozlamalar
│       │
│       ├── TeacherScreens.kt     ← O'qituvchi paneli (6 bo'lim)
│       ├── ParentScreens.kt      ← Ota-ona paneli (6 bo'lim)
│       ├── ChefScreens.kt        ← Oshpaz paneli (7 bo'lim)
│       │
│       ├── JurnalScreen.kt
│       ├── BaholashScreen.kt
│       ├── SinflarimScreen.kt
│       ├── ScheduleScreens.kt
│       └── DarsKontentiScreen.kt
│
└── data/
    └── MockData.kt               ← Mock ma'lumotlar (API tayyor bo'lgunga qadar)
```

---

## 🛠️ Texnik stack

| Texnologiya | Versiya | Maqsad |
|-------------|---------|--------|
| Kotlin | 1.9.x | Asosiy til |
| Jetpack Compose | BOM 2024.02 | UI framework |
| Material Design 3 | latest | Komponentlar |
| EncryptedSharedPreferences | 1.1.0-alpha06 | Xavfsiz saqlash |
| Lifecycle ViewModel | 2.7.0 | MVVM |
| Activity Compose | 1.8.2 | BackHandler |
| Retrofit | *(kelasi versiyada)* | API integratsiya |

**Arxitektura:** MVVM · Single Activity · Compose Navigation

---

## 🚀 Ishga tushirish

### Talablar
- Android Studio Hedgehog (2023.1.1) yoki yangi
- JDK 17+
- Android 8.0+ qurilma yoki emulator (API 26+)

### Qadamlar

```bash
# 1. Reponi clone qiling
git clone https://github.com/JasurbekRuzimov/Maktab.git

# 2. Android Studio da oching
# File → Open → Maktab papkasini tanlang

# 3. build.gradle.kts ga qo'shing (dependencies ichiga)
implementation("androidx.security:security-crypto:1.1.0-alpha06")

# 4. Gradle sync (avtomatik boshlanadi)

# 5. Run ▶
```

---

## 🔌 API integratsiyasi

Backend hozir Railway da joylashgan:

```
https://maktab-backend-production.up.railway.app/
```

API endpointlar tayyorlanmoqda. Hozircha barcha network chaqiruvlar **mock delay** bilan simulyatsiya qilingan — Retrofit qo'shilganda `// TODO: API call` deb belgilangan joylarga almashtiriladi.

**Login oqimi (backend tayyor bo'lgandan keyin):**
```kotlin
// LoginScreen.kt → doLogin() funksiyasi
// Hozir: delay(1500) simulyatsiya
// Keyin: val result = authRepo.login(username, password, role)
//        savedRole = result.role  ← backend dan keladi
```

---

## 🌐 Ko'p tilli qo'llab-quvvatlash

| Til | Kod | Holati |
|-----|-----|--------|
| O'zbekcha | `uz` | ✅ To'liq |
| Русский | `ru` | ✅ To'liq |
| English | `en` | ✅ To'liq |

---

## 🗺️ Yo'l xaritasi

- [x] Autentifikatsiya oqimi (Login → PIN → Dashboard)
- [x] Teacher paneli (6 bo'lim)
- [x] Parent paneli (6 bo'lim)
- [x] Chef paneli (7 bo'lim)
- [x] Drawer navigation (3 rol, 3 rang)
- [x] EncryptedSharedPreferences
- [x] Dark mode
- [x] Ko'p til (UZ / RU / EN)
- [ ] Retrofit API integratsiya
- [ ] Student paneli
- [ ] HR paneli
- [ ] Push notification
- [ ] Offline rejim
- [ ] iOS versiya (Flutter yoki KMP)

---

## 👨‍💻 Dasturchi

**Jasurbek Ruzimov**

[![GitHub](https://img.shields.io/badge/GitHub-JasurbekRuzimov-181717?style=flat&logo=github)](https://github.com/JasurbekRuzimov)

---

## 📄 Litsenziya

```
MIT License — erkin foydalaning, o'zgartiring va tarqating.
```

---

<div align="center">
<sub>Maktab · Android · Kotlin · Jetpack Compose · 2026</sub>
</div>