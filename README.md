# WearOTP

A TOTP authenticator for Android and Wear OS. Manage your two-factor authentication codes on your phone and view them directly on your wrist.

## Features

- 📷 Add accounts by scanning a QR code or entering details manually
- ⌚ Syncs OTP codes to Wear OS in real time via the Wearable Data Layer API
- 🔐 Tokens at-rest are encrypted with AES via Android Keystore

## Project Structure

```
mobile/   # Android phone app (Jetpack Compose)
wear/     # Wear OS app (Compose for Wear OS)
shared/   # Common logic: OTP generation, crypto, data models
```

## Tech Stack

- **Kotlin**, **Jetpack Compose**, **Compose for Wear OS**
- **ML Kit** + **CameraX** — QR code scanning
- **Android Keystore (AES)** — encrypted token storage
- **Wearable Data Layer API** — phone ↔ watch sync
- **DataStore**, **ViewModel**, **Compose Navigation**

## Requirements

- Android **API 30+**, Target SDK 36
- Wear OS **API 30+**, Target SDK 36
- `CAMERA` permission for QR scanning

