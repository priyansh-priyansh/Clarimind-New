# ClariMind - Your Mental Wellness Companion

ClariMind is an Android application that helps users with mental wellness through AI-powered emotion detection and personalized wellness guidance.

## Features

- **Google Sign-In**: Secure authentication using Google One Tap
- **Emotion Detection**: Camera-based emotion analysis
- **Personalized Questions**: AI-driven wellness assessment
- **Modern UI**: Clean, intuitive interface

## Setup Instructions

### Prerequisites

1. Android Studio Arctic Fox or later
2. Android SDK 24 or higher
3. Google Firebase account
4. Google Cloud Console access

### Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing project
3. Add Android app with package name: `com.example.clarimind`
4. Download `google-services.json` and place it in the `app/` directory
5. Enable Google Sign-In in Authentication section
6. Add your SHA-1 fingerprint to the project settings

### Google Cloud Console Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your Firebase project
3. Enable Google Sign-In API
4. Configure OAuth consent screen
5. Create OAuth 2.0 client IDs for Android and Web

### Build and Run

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build the project
5. Run on device or emulator

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/clarimind/
│   │   ├── LoginActivity.kt          # Google Sign-In screen
│   │   ├── SetupWizardActivity.kt    # Initial setup
│   │   ├── MainActivity.kt           # Main app interface
│   │   └── presentation/             # UI components
│   ├── res/
│   │   ├── layout/                   # XML layouts
│   │   ├── drawable/                 # App icons and graphics
│   │   └── values/                   # Strings and themes
│   └── AndroidManifest.xml
├── google-services.json              # Firebase configuration
└── build.gradle.kts                  # App dependencies
```

## Dependencies

- **Firebase**: Authentication and Analytics
- **Google Play Services**: Sign-In functionality
- **Jetpack Compose**: Modern UI framework
- **CameraX**: Camera functionality
- **Navigation**: Screen navigation

## Authentication Flow

1. User opens app → LoginActivity
2. User taps Google Sign-In button
3. Google One Tap UI appears
4. User selects account
5. Firebase authenticates user
6. User is redirected to SetupWizardActivity
7. SetupWizardActivity navigates to MainActivity

## Troubleshooting

### Common Issues

1. **Google Sign-In fails**: Check SHA-1 fingerprint in Firebase console
2. **Build errors**: Ensure all dependencies are synced
3. **Authentication errors**: Verify google-services.json configuration

### SHA-1 Fingerprint

To get your SHA-1 fingerprint:

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
