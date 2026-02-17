# Dicto - Arabic-English Dictionary App

Just my own dict. don't use it yet cuz it's still a mess. though it's usable.

## Features

### 🎯 Core Translation
- **Real-time Translation**: Automatic translation with 600ms debounce
- **Full Sentence Translation**: Translates complete sentences
- **Word-by-Word Breakdown**: Individual word translations with parallel processing
- **Phrase Builder**: Select multiple words to create and translate custom phrases

### 📋 Clipboard Auto-Translate ✨
- **Automatic Detection**: Monitors clipboard in real-time (checks every 1 second)
- **Smart Duplicate Prevention**: Only translates new text, avoids re-translating
- **Toggle Control**: Enable/disable clipboard monitoring with a switch
- **Works in Foreground**: Active when app is open on the Translator tab
- **Multi-format Support**: Handles plain text, HTML, and other text formats

### 💾 Saved Words
- **Save Vocabulary**: Star icon to save/unsave words and phrases
- **Persistent Storage**: Uses DataStore for reliable data persistence
- **Saved Words Tab**: Dedicated view for your vocabulary collection
- **Real-time Sync**: Stars update automatically across the app

### 🎨 User Interface
- **Material 3 Design**: Modern, clean interface
- **RTL Support**: Proper right-to-left layout for Arabic text
- **Dark/Light Themes**: Follows system theme
- **Responsive Layout**: Efficient LazyColumn for smooth scrolling

## How to Use

### Translation
1. Open the app on the **Translator** tab
2. Either:
   - **Type** text in the search field, OR
   - **Copy** text from any app (auto-translates if clipboard monitoring is ON)
3. View full translation and word-by-word breakdown
4. Star words you want to save

### Clipboard Auto-Translate
1. Ensure the toggle at the top is **ON** (green)
2. Copy any text from Chrome, Notes, Messages, etc.
3. Switch back to Dicto (or keep it in split-screen)
4. Text automatically appears and translates within 1-2 seconds! 🎉

### Phrase Builder
1. Translate any sentence
2. Tap words in the word list to select them
3. View the combined phrase translation in the card above

### Saved Words
1. Tap the **Saved** tab (bottom navigation)
2. View all your saved vocabulary
3. Tap the star icon to remove items

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with StateFlow
- **Async**: Kotlin Coroutines + Flow
- **Storage**: DataStore (Preferences)
- **Translation API**: LibreTranslate (self-hosted or public instance)

## Build & Run

```bash
# Install dependencies and build
./gradlew assembleDebug

# Install on device/emulator
./gradlew installDebug

# Run the app
adb shell am start -n com.example.dicto/.MainActivity
```

## Debugging

### Check Logs
```bash
# Monitor clipboard and translation logs
adb logcat MainActivity:D DictionaryViewModel:D *:S

# Full app logs
adb logcat -s "MainActivity" "DictionaryViewModel" "TranslationRepository"
```

### Common Issues
- **Clipboard not working**: Make sure toggle is ON and app is in foreground
- **Text not translating**: Check internet connection (requires API access)
- **Duplicate skipping**: This is normal - the app won't re-translate already shown text

## Project Structure

```
app/src/main/java/com/example/dicto/
├── MainActivity.kt              # App entry, clipboard monitoring
├── DictionaryScreen.kt          # Main UI (Translator + Saved tabs)
├── DictionaryViewModel.kt       # State management + business logic
├── PhraseBuilderSection.kt      # Phrase selection UI
├── TranslationRepository.kt     # API communication
└── WordStorage.kt               # DataStore persistence
```

## Future Enhancements
- [ ] Offline translation support
- [ ] Audio pronunciation
- [ ] Export saved words
- [ ] Custom word notes
- [ ] Translation history
- [ ] Background clipboard monitoring (with user permission)

---

Made with ❤️ for learning Arabic


