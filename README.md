# SmartConvert - Unit Converter for Android

A production-ready unit conversion application built with modern Android development practices, following MVVM architecture and Material Design 3 guidelines.

## 📱 Features

### ✅ Implemented
- **8 Conversion Categories**: Length, Weight, Temperature, Volume, Area, Speed, Time, Digital Storage
- **60+ Units**: Comprehensive unit support with accurate conversion formulas
- **Real-time Conversion**: Instant results as you type
- **Unit Swapping**: Quick reverse conversion with one tap
- **Modern UI**: Material Design 3 with card-based layout
- **MVVM Architecture**: Clean separation of concerns
- **History & Favorites**: Room database, live search, swipe-to-delete with undo
- **ProGuard/R8**: Code optimization and obfuscation
- **Unit Tests**: Comprehensive test coverage
- **Accessibility**: Full content descriptions and screen reader support

### 🎨 User Experience
- Clean, intuitive interface
- Emoji icons for visual category identification
- Automatic conversion (no "Convert" button needed)
- Error validation with user-friendly messages
- Responsive design for all screen sizes
- Professional color scheme

## 🏗️ Architecture

### Design Patterns Used
1. **MVVM (Model-View-ViewModel)**: Separates UI from business logic
2. **Repository Pattern**: Single source of truth for data operations
3. **Factory Pattern**: UnitProvider for creating units
4. **Singleton Pattern**: Repository and Provider instances
5. **Observer Pattern**: LiveData for reactive UI updates
6. **ViewHolder Pattern**: Efficient RecyclerView implementation
7. **Strategy Pattern**: Different conversion algorithms for unit types

### Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/unitconverter/app/
│   │   │   ├── data/                    # Data layer
│   │   │   │   ├── provider/            # Unit data providers
│   │   │   │   │   └── UnitProvider.java
│   │   │   │   └── repository/          # Business logic
│   │   │   │       └── ConversionRepository.java
│   │   │   ├── domain/                  # Domain layer
│   │   │   │   └── model/               # Core business objects
│   │   │   │       ├── Unit.java
│   │   │   │       ├── UnitCategory.java
│   │   │   │       └── ConversionResult.java
│   │   │   ├── presentation/            # Presentation layer
│   │   │   │   ├── adapter/             # RecyclerView adapters
│   │   │   │   │   └── CategoryAdapter.java
│   │   │   │   └── viewmodel/           # ViewModels
│   │   │   │       ├── MainViewModel.java
│   │   │   │       └── ConversionViewModel.java
│   │   │   ├── util/                    # Utilities
│   │   │   │   └── PreferenceManager.java
│   │   │   ├── MainActivity.java        # Category selection screen
│   │   │   └── ConversionActivity.java  # Conversion screen
│   │   └── res/                         # Resources
│   │       ├── layout/                  # XML layouts
│   │       ├── values/                  # Strings, colors, dimens
│   │       └── drawable/                # Icons and graphics
│   └── test/                            # Unit tests
│       └── java/com/unitconverter/app/
│           └── ExampleUnitTest.java
```

## 🔧 Technical Stack

- **Language**: Java 11
- **Min SDK**: 21 (Android 5.0 Lollipop)
- **Target SDK**: 36 (Android 16)
- **Build System**: Gradle 8.13.0
- **Architecture**: MVVM with LiveData
- **UI**: Material Design 3, ConstraintLayout, RecyclerView
- **Storage**: Room (conversion history)
- **Testing**: JUnit 4, Mockito, Espresso

## 📋 Prerequisites

Before building this project, ensure you have:

1. **JDK 11 or newer** (Required by Gradle 8.13+)
   - Download from: https://adoptium.net/
   - Set JAVA_HOME environment variable

2. **Android Studio** (Latest stable version)
   - Download from: https://developer.android.com/studio

3. **Android SDK**
   - SDK Platform 36 (Android 14)
   - Build Tools 34.0.0+
   - Android Emulator (for testing)

## 🚀 Setup Instructions

### Option 1: Android Studio (Recommended)
1. **Clone or download the project**
2. **Open Android Studio**
3. **File → Open** → Select project folder
4. **Wait for Gradle sync** to complete
5. **Run** → Click the green play button
6. **Select device** (emulator or physical device)

### Option 2: Command Line
```bash
# Navigate to project directory
cd 08_Java_App_Development

# For Windows (PowerShell):
.\gradlew.bat assembleDebug

# For macOS/Linux:
./gradlew assembleDebug

# Install on connected device:
.\gradlew.bat installDebug
```

## 🧪 Running Tests

### Unit Tests
```bash
# Windows:
.\gradlew.bat test

# macOS/Linux:
./gradlew test
```

### Instrumented Tests (Requires connected device/emulator)
```bash
# Windows:
.\gradlew.bat connectedAndroidTest

# macOS/Linux:
./gradlew connectedAndroidTest
```

## 🎯 How to Use the App

### 1. Category Selection
- Launch the app to see 8 category cards
- Each card shows an icon and category name
- Tap any category to start converting

### 2. Converting Units
- **Enter value**: Type in the input field
- **Select from unit**: Choose from dropdown
- **Select to unit**: Choose target unit
- **See result**: Conversion happens automatically!
- **Swap units**: Tap swap button to reverse
- **Clear**: Tap clear button to start over

### 3. Examples
- **Length**: Convert 5 feet to meters → 1.524 m
- **Temperature**: Convert 100°C to Fahrenheit → 212°F
- **Weight**: Convert 1 kg to pounds → 2.20462 lbs

## 🔐 Security Features

1. **No network access, no data collection**
   - The app requests no permissions and never sends data off the device
   - Conversion history stays in a local Room database

2. **ProGuard/R8**
   - Code shrinking removes unused code
   - Obfuscation makes reverse engineering difficult
   - Optimization improves app performance

3. **Input Validation**
   - All user input is validated
   - Protection against invalid numbers (NaN, Infinity)
   - Type-safe enum constants

## 📊 Conversion Algorithms

### Regular Units (Length, Weight, Volume, etc.)
```
Formula: toBase = value × fromUnit.factor
         result = toBase ÷ toUnit.factor
Example: 5 ft to meters
         toBase = 5 × 0.3048 = 1.524 m
         result = 1.524 ÷ 1.0 = 1.524 m
```

### Temperature (Special handling)
```
Formula: kelvin = (value + offset₁) × factor₁
         result = (kelvin ÷ factor₂) - offset₂
Example: 0°C to Fahrenheit
         kelvin = (0 + 273.15) × 1.0 = 273.15 K
         result = (273.15 ÷ 0.5556) - 459.67 = 32°F
```

## 🎨 Customization

### Adding New Units
1. Open `UnitProvider.java`
2. Find the appropriate `initialize...Units()` method
3. Add new unit with conversion factor:
```java
units.add(new Unit("Name", "symbol", Category, conversionFactor));
```

### Adding New Category
1. Add to `UnitCategory.java` enum
2. Create initialization method in `UnitProvider.java`
3. Define units with conversion factors
4. Rebuild and test

## 🐛 Troubleshooting

### Build Fails: "Requires JVM runtime version 11"
**Solution**: Update JAVA_HOME to JDK 11+
```bash
# Windows:
set JAVA_HOME=C:\Program Files\Java\jdk-11

# macOS/Linux:
export JAVA_HOME=/path/to/jdk-11
```

### App Crashes on Launch
1. Check logcat in Android Studio
2. Verify all dependencies are synced
3. Clean and rebuild: Build → Clean Project → Rebuild Project

### UI Not Displaying Correctly
1. Ensure Material Design dependencies are included
2. Check themes.xml for proper Material theme
3. Verify device/emulator Android version ≥ 5.0

## 📱 Testing on Physical Device

1. **Enable Developer Options**
   - Settings → About Phone → Tap "Build Number" 7 times

2. **Enable USB Debugging**
   - Settings → Developer Options → USB Debugging

3. **Connect Device**
   - Connect via USB cable
   - Accept USB debugging prompt on device

4. **Run from Android Studio**
   - Click Run button
   - Select your device from list

## 📦 Building Release APK

### 1. Generate an upload key
```bash
keytool -genkey -v -keystore release-upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

### 2. Configure signing
Copy `keystore.properties.example` to `keystore.properties` (git-ignored) and fill in the
key details. `app/build.gradle` reads it automatically; without the file, release builds are
left unsigned. Never commit the keystore or its passwords.

### 3. Build
```bash
.\gradlew.bat assembleRelease   # app/build/outputs/apk/release/app-release.apk
```

## 🏪 Play Store Deployment

### Checklist
- [ ] App tested on multiple devices
- [ ] All features working correctly
- [ ] ProGuard enabled and tested
- [ ] App icon created (512x512 PNG)
- [ ] Screenshots prepared (multiple screen sizes)
- [ ] Store listing written
- [ ] Privacy policy created
- [ ] Version code incremented
- [ ] Signed with release key

### App Bundle (Preferred)
```bash
.\gradlew.bat bundleRelease
```

AAB location: `app/build/outputs/bundle/release/app-release.aab`

## 🤝 Contributing

This is an educational project. Contributions welcome!

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is created for educational purposes.

## 👨‍💻 Learning Resources

- [Android Developer Guide](https://developer.android.com/guide)
- [MVVM Architecture](https://developer.android.com/jetpack/guide)
- [Material Design 3](https://m3.material.io/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

## 📧 Contact

For questions or feedback about this educational project, feel free to reach out!

---

**Built with ❤️ for learning Android development**
