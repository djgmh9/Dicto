# TTS Quick Reference

## For UI Developers - Using TTS in Composables

### Basic Usage

```kotlin
// In your composable that receives viewModel
@Composable
fun MyWordComponent(
    wordResult: WordResult,
    viewModel: DictionaryViewModel
) {
    // Pronounce Arabic word
    IconButton(onClick = {
        viewModel.pronounceOriginal(wordResult.original)
    }) {
        Icon(Icons.Filled.VolumeUp, "Pronounce Arabic")
    }
    
    // Pronounce English translation
    IconButton(onClick = {
        viewModel.pronounceTranslation(wordResult.translation)
    }) {
        Icon(Icons.Filled.VolumeUp, "Pronounce English")
    }
    
    // Stop current pronunciation
    IconButton(onClick = {
        viewModel.stopPronunciation()
    }) {
        Icon(Icons.Filled.Stop, "Stop")
    }
}
```

## ViewModel Public API

```kotlin
// Pronounce original word (Arabic)
viewModel.pronounceOriginal(word: String)

// Pronounce translation (English)
viewModel.pronounceTranslation(translation: String)

// Stop current pronunciation
viewModel.stopPronunciation()
```

## Implementation Details (For Reference)

### TTSManager - Under the Hood

```kotlin
class TTSManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    // Initialize TTS engine
    fun initialize(
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    )
    
    // Speak text in specified language
    fun speak(
        text: String,
        language: Locale = Locale.ENGLISH,
        onComplete: (() -> Unit)? = null
    )
    
    // Stop current speech
    fun stop()
    
    // Check if ready
    fun isReady(): Boolean
    
    // Cleanup resources
    fun shutdown()
}
```

## What's Handled Automatically

✅ **Lifecycle Management**
- ViewModel survives screen rotation
- TTS cleaned up when ViewModel destroyed

✅ **Initialization**
- Lazy initialization on first use
- Auto-retry if not initialized

✅ **Error Handling**
- Graceful fallback to default language
- Error logging for debugging

✅ **Resource Cleanup**
- TTS shutdown on app exit
- No memory leaks

## Common Patterns

### 1. Pronunciation Button
```kotlin
IconButton(onClick = {
    viewModel.pronounceOriginal(word)
}) {
    Icon(Icons.Filled.VolumeUp, "Pronounce")
}
```

### 2. Mute/Unmute Control
```kotlin
var isMuted by remember { mutableStateOf(false) }

IconButton(onClick = {
    if (isMuted) {
        viewModel.pronounceOriginal(word)
        isMuted = false
    } else {
        viewModel.stopPronunciation()
        isMuted = true
    }
}) {
    Icon(
        if (isMuted) Icons.Filled.VolumeOff 
        else Icons.Filled.VolumeUp,
        "Toggle mute"
    )
}
```

### 3. Pronunciation with Feedback
```kotlin
var isPlaying by remember { mutableStateOf(false) }

LaunchedEffect(Unit) {
    // Play pronunciation
    viewModel.pronounceOriginal(word)
    isPlaying = true
    // Wait for completion (simple delay)
    delay(3000)
    isPlaying = false
}

IconButton(
    onClick = { viewModel.pronounceOriginal(word) },
    enabled = !isPlaying
) {
    Icon(
        Icons.Filled.VolumeUp,
        "Pronounce",
        tint = if (isPlaying) Color.Gray else Color.Blue
    )
}
```

## Notes

- **First Pronunciation**: Might take 1-2 seconds (engine initializing)
- **Subsequent**: <100ms (instant)
- **Language Data**: Downloaded to device on first use
- **No Network**: Speech works offline after first initialization

## Files

- `utils/TTSManager.kt` - TTS utility class
- `DictionaryViewModel.kt` - ViewModel with TTS integration

## Questions?

See `TTS_INTEGRATION_GUIDE.md` for detailed documentation.

