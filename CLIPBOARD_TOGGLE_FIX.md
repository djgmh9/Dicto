# Clipboard Monitoring Persistence Fix

## Problem
The clipboard monitoring toggle (`Auto-Translate from Clipboard`) was always resetting to "on" when the app was reopened. This was because the setting was not being persisted to device storage.

## Root Cause
The original implementation used a `MutableStateFlow(true)` that was initialized every time the ViewModel was created:

```kotlin
// ❌ Before: Always resets to true
private val _clipboardMonitoringEnabled = MutableStateFlow(true)
val clipboardMonitoringEnabled = _clipboardMonitoringEnabled.asStateFlow()

fun toggleClipboardMonitoring() {
    _clipboardMonitoringEnabled.value = !_clipboardMonitoringEnabled.value  // Lost on app restart
}
```

This meant:
1. User toggles clipboard monitoring OFF
2. App closes/restarts
3. ViewModel is recreated
4. `_clipboardMonitoringEnabled` is recreated as `true`
5. Setting is lost ❌

## Solution

### 1. Created PreferencesManager Utility
New file: `utils/PreferencesManager.kt`

```kotlin
class PreferencesManager(private val context: Context) {
    // Read clipboard monitoring preference from DataStore
    val clipboardMonitoringEnabled: Flow<Boolean> = 
        context.preferencesDataStore.data
            .map { preferences ->
                preferences[CLIPBOARD_MONITORING_KEY] ?: DEFAULT_CLIPBOARD_MONITORING
            }
    
    // Save clipboard monitoring preference
    suspend fun setClipboardMonitoringEnabled(enabled: Boolean) {
        context.preferencesDataStore.edit { preferences ->
            preferences[CLIPBOARD_MONITORING_KEY] = enabled
        }
    }
}
```

**Benefits**:
- ✅ Type-safe preference access
- ✅ Observable updates via Flow
- ✅ Easy to extend for new preferences
- ✅ Follows separation of concerns

### 2. Updated DictionaryViewModel
Changed clipboard monitoring from `MutableStateFlow` to persistent `PreferencesManager`:

```kotlin
// ✅ After: Persisted via PreferencesManager
private val preferencesManager = PreferencesManager(application)

val clipboardMonitoringEnabled: StateFlow<Boolean> = 
    preferencesManager
        .clipboardMonitoringEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

fun toggleClipboardMonitoring() {
    val newState = !clipboardMonitoringEnabled.value
    viewModelScope.launch {
        preferencesManager.setClipboardMonitoringEnabled(newState)
    }
}
```

**What changed**:
1. Removed `MutableStateFlow(true)` hardcoded initialization
2. Connected to `PreferencesManager.clipboardMonitoringEnabled` Flow
3. Converted Flow to StateFlow with `.stateIn()`
4. Toggle now persists to device storage

## How It Works

### User Flow
```
1. User toggles OFF
   ↓
2. toggleClipboardMonitoring() called
   ↓
3. preferencesManager.setClipboardMonitoringEnabled(false)
   ↓
4. Value saved to DataStore device storage 💾
   ↓
5. App closed/reopened
   ↓
6. PreferencesManager reads from DataStore
   ↓
7. clipboardMonitoringEnabled still = false ✅
```

### Technical Flow
```
DataStore (Device Storage)
    ↓
    Emits saved value via Flow
    ↓
PreferencesManager.clipboardMonitoringEnabled
    ↓
DictionaryViewModel converts to StateFlow
    ↓
SettingsScreen observes and displays current state
    ↓
User toggles → Saves back to DataStore
```

## Data Flow Diagram

```
┌─────────────────────────────────┐
│   User Toggles Switch           │
│  (SettingsScreen UI)            │
└────────────┬─────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ toggleClipboardMonitoring()     │
│ (DictionaryViewModel)           │
└────────────┬─────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ setClipboardMonitoringEnabled() │
│ (PreferencesManager)            │
└────────────┬─────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│  Save to DataStore              │
│  Device Storage 💾              │
└─────────────────────────────────┘

Next App Launch:
┌─────────────────────────────────┐
│  Read from DataStore            │
│  Device Storage 💾              │
└────────────┬─────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ clipboardMonitoringEnabled Flow │
│ (PreferencesManager)            │
└────────────┬─────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ stateIn() StateFlow             │
│ (DictionaryViewModel)           │
└────────────┬─────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Display in SettingsScreen       │
│ (Correct saved value) ✅        │
└─────────────────────────────────┘
```

## Files Modified

### Created
- `utils/PreferencesManager.kt` - New preferences manager

### Updated  
- `DictionaryViewModel.kt` - Use PreferencesManager instead of MutableStateFlow

## Testing the Fix

### Before Fix
1. Open app
2. Toggle clipboard monitoring OFF
3. Close app
4. Reopen app
5. ❌ Clipboard monitoring is ON (Lost setting)

### After Fix
1. Open app
2. Toggle clipboard monitoring OFF
3. Close app
4. Reopen app
5. ✅ Clipboard monitoring is OFF (Setting persisted!)

## Technical Details

### Storage Backend
- **Provider**: Android DataStore Preferences
- **Location**: Device local storage
- **Persistence**: Survives app restarts and device reboots
- **Thread Safety**: All operations are thread-safe

### Key Preference
```kotlin
CLIPBOARD_MONITORING_KEY = booleanPreferencesKey("clipboard_monitoring_enabled")
DEFAULT_VALUE = true  // Default is ON if never set
```

### Initialization
```kotlin
val clipboardMonitoringEnabled: StateFlow<Boolean> = 
    preferencesManager.clipboardMonitoringEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,  // Start collecting when subscribed
            initialValue = true               // Default while loading
        )
```

## Best Practices Applied

✅ **Separation of Concerns**
- PreferencesManager handles persistence
- ViewModel handles logic
- UI displays state

✅ **Reactive Programming**
- Flow for observing preference changes
- StateFlow for UI consumption
- Automatic updates when data changes

✅ **Coroutine Safety**
- All persistence operations are suspended functions
- Launched in appropriate scopes
- No blocking I/O

✅ **Scalability**
- Easy to add new preferences to PreferencesManager
- Single source of truth for preferences
- Follows established patterns

## Migration Notes

### For Developers
When adding new persistent settings:

```kotlin
// In PreferencesManager.kt
private val MY_NEW_SETTING = booleanPreferencesKey("my_setting")

val myNewSetting: Flow<Boolean> = context.preferencesDataStore.data
    .map { preferences ->
        preferences[MY_NEW_SETTING] ?: DEFAULT_VALUE
    }

suspend fun setMyNewSetting(value: Boolean) {
    context.preferencesDataStore.edit { preferences ->
        preferences[MY_NEW_SETTING] = value
    }
}

// In ViewModel
val myNewSetting: StateFlow<Boolean> = preferencesManager
    .myNewSetting
    .stateIn(viewModelScope, SharingStarted.Lazily, true)

fun toggleMyNewSetting() {
    val newValue = !myNewSetting.value
    viewModelScope.launch {
        preferencesManager.setMyNewSetting(newValue)
    }
}
```

## Verification

✅ Build successful
✅ Code compiles without errors
✅ Logic tested and working
✅ Follows architecture patterns
✅ Production ready

## Summary

The clipboard monitoring toggle now properly persists using:
1. **DataStore** - Android's recommended preferences library
2. **PreferencesManager** - Centralized preference handling
3. **Flow/StateFlow** - Reactive preference updates
4. **Coroutines** - Safe async operations

Users can now toggle clipboard monitoring and the setting will be preserved across app restarts! 🎉

