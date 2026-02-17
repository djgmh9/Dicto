# How to Test Clipboard Auto-Translate

## Testing Steps:

1. **Build and Install the App:**
   - Run `.\gradlew installDebug` or install via Android Studio

2. **Open the App:**
   - Launch Dicto on your device/emulator
   - Make sure you're on the "Translator" tab (the first tab)
   - Check that the clipboard monitoring toggle at the top shows "Auto-translate from clipboard enabled" (green)

3. **Test Clipboard Translation:**
   
   **Method 1: From another app**
   - Open Chrome, Notes, or any text app
   - Copy some text (long press and select "Copy")
   - Switch back to Dicto app
   - Within 1-2 seconds, the copied text should appear in the text field and be translated automatically

   **Method 2: Using ADB (for testing on emulator/device)**
   ```bash
   # Copy text to clipboard via ADB
   adb shell am broadcast -a clipper.set -e text "hello world"
   ```
   Or simply:
   ```bash
   adb shell input text "hello world"
   adb shell input keyevent 277  # KEYCODE_COPY
   ```

4. **Check the Logs:**
   - Run: `adb logcat -s MainActivity:D DictionaryViewModel:D`
   - You should see messages like:
     ```
     MainActivity: Starting clipboard monitoring
     MainActivity: Checking clipboard - monitoring enabled: true
     MainActivity: Found new clipboard text, triggering translation: hello world
     DictionaryViewModel: onClipboardTextFound called with: hello world
     DictionaryViewModel: Setting search query to clipboard text: hello world
     ```

## Troubleshooting:

### If clipboard monitoring doesn't work:

1. **Check the toggle:** Make sure the switch is ON (blue/green)
2. **Check logs:** Look for error messages in logcat
3. **Try switching tabs:** Go to "Saved" tab and back to "Translator" tab
4. **Minimize and restore app:** This triggers the ON_RESUME event

### Common Issues:

- **Android 10+**: Some devices have clipboard privacy features that may delay clipboard access
- **Emulator**: May have issues with clipboard sync - try typing in emulator's text field instead
- **Background apps**: Clipboard is only monitored when the app is in foreground and on Translator tab

## How It Works:

1. When you open the Translator tab, the app starts checking clipboard every 1 second
2. When new text is detected, it's automatically entered into the search field
3. The debounced flow (600ms delay) then triggers the translation
4. You can toggle the feature on/off using the switch at the top

