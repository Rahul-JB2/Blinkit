# QuickCommerce Grocery Delivery App

## Overview
An Android quick-commerce grocery delivery app built with Kotlin and Jetpack Compose. Generated from Google AI Studio, it includes a full shopping experience with AI chatbot, real-time order tracking, a spin-wheel loyalty game, and admin dashboard.

## Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Architecture**: MVVM (ViewModel + StateFlow)
- **Database**: Room (local SQLite)
- **Networking**: Retrofit + Moshi
- **Image loading**: Coil
- **Backend**: Firebase (Gemini AI, App Check)
- **Dependency injection**: manual ViewModel factory
- **Build**: Gradle (Kotlin DSL), AGP 8.x, compileSdk 36

## Running the App
This is an Android project — it requires Android Studio + a device/emulator.

1. Open in Android Studio
2. Create `.env` in the project root and set `GEMINI_API_KEY=<your key>`
3. Remove the `signingConfig = signingConfigs.getByName("debugConfig")` line from `app/build.gradle.kts` if prompted
4. Run on an emulator or physical device (minSdk 24)

## Key Screens
| Screen | File |
|--------|------|
| Home (redesigned) | `app/src/main/java/com/example/ui/screens/HomeScreen.kt` |
| Cart | `app/src/main/java/com/example/ui/screens/CartScreen.kt` |
| Payment | `app/src/main/java/com/example/ui/screens/PaymentScreen.kt` |
| Order Tracking | `app/src/main/java/com/example/ui/screens/TrackingScreen.kt` |
| Order History | `app/src/main/java/com/example/ui/screens/OrderHistoryScreen.kt` |
| AI Chatbot | `app/src/main/java/com/example/ui/screens/AIChatbotScreen.kt` |
| Admin Dashboard | `app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt` |

## Navigation
`app/src/main/java/com/example/ui/GroceryApp.kt` — Compose NavHost wiring all screens together.

## Theme / Branding
- `app/src/main/java/com/example/ui/theme/Color.kt` — brand colors (BrandGreen `#108A43`, BrandYellow `#F7C11E`, category palette)
- `app/src/main/java/com/example/ui/theme/Theme.kt` — light/dark MaterialTheme setup

## User Preferences
- Keep the existing MVVM/Room/Compose architecture — no restructuring
- The HTML file at `attached_assets/home_dashboard_1783341465580.html` served as the UI design reference for the HomeScreen redesign
