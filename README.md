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
- **Android Keystore (AES)** — Encrypted token storage
- **Wearable Data Layer API** — phone ↔ watch sync
- **DataStore**, **ViewModel**, **Compose Navigation**
- **Javax Crypto** — In transit data encryption 
- ...and more

## Requirements

- Android **API 30+**, Target SDK 36
- Wear OS **API 30+**, Target SDK 36
- `CAMERA` permission for QR scanning

## Feature requests and contributions
The app is still in heavy development, but if you have any feature requests or want to contribute, feel free to open an issue or submit a pull request!

## Disclaimer
This app tries to be as secure as possible with both data at rest and data in transit, 
but this security is heavily put at risk if the app runs on a rooted device.
Please, do not use this app on rooted devices or, if you do, make sure to understand the risks and take necessary precautions.

Thank you for your interest in the project!

