# Component Architecture & Best Practices

## Overview

This document explains the component splitting strategy and how it improves code maintainability, reusability, and testability.

## Benefits of Component Splitting

### 1. **Single Responsibility Principle (SRP)**
Each component has one clear purpose:
- `WordRowItem` → Display a word translation
- `PhraseBuilderSection` → Build phrases with word selection
- `PhraseResultCard` → Show phrase translation result
- `StateDisplays` → Display UI states (empty, loading, error)

### 2. **Reusability**
Components are used in multiple places without duplication:

```
WordRowItem
├─ Used in TranslatorContent (word by word results)
└─ Used in SavedWordsContent (saved words library)

PhraseBuilderSection
├─ Used in TranslatorContent
└─ Can be reused in future features

StateDisplays
├─ EmptyStateDisplay → TranslatorContent, SavedWordsContent
├─ LoadingStateIndicator → TranslatorContent
└─ ErrorStateDisplay → TranslatorContent
```

### 3. **Maintainability**
- Changes are isolated to relevant files
- Easy to locate and fix bugs
- Clear file structure mirrors component hierarchy
- Self-documenting code with KDoc comments

### 4. **Testability**
Smaller components are easier to test:

```kotlin
// Before: Monolithic screen, hard to test individual parts
// After: Individual components can be tested independently

@Composable
fun testWordRowItem() {
    WordRowItem(
        wordResult = WordResult("hello", "مرحبا", isSaved = false),
        onToggleSave = { /* test callback */ }
    )
}
```

### 5. **Performance**
- Recomposition is targeted to changed components
- Reduced recomposition of entire screens
- Better memory usage

## File Structure

```
ui/
├── components/
│   ├── Components.kt                 # Package documentation
│   ├── WordRowItem.kt                # Word display + save toggle
│   ├── PhraseBuilderSection.kt       # Word selection for phrases
│   ├── PhraseResultCard.kt           # Phrase translation display
│   ├── StateDisplays.kt              # Empty/Loading/Error states
│   └── TranslationComponents.kt      # Result headers and dividers
│
├── SettingsScreen.kt                 # Settings UI
├── theme/                            # Theme configurations
└── [other UI files]

DictionaryScreen.kt                    # Screen router and main layouts
```

## Component Details

### WordRowItem.kt
**Purpose**: Display a single word translation

**Features**:
- Arabic word (RTL) + English translation (LTR)
- Save/unsave toggle with star icon
- Visual feedback for saved state
- Card-based design

**Responsibilities**:
- Render word data
- Handle save toggle callback
- Manage visual state

**Not Responsible For**:
- Fetching data
- Managing save state
- Navigation

### PhraseBuilderSection.kt
**Purpose**: Allow users to select and combine words into phrases

**Features**:
- RTL layout support for Arabic
- Multiple word selection (chips)
- Automatic phrase ordering
- Visual feedback for selected words

**Components**:
- `PhraseBuilderSection` (public) - Main component
- `WordFilterChip` (private) - Individual selectable chip

**Recomposition**: Only recomputes when word list changes

### PhraseResultCard.kt
**Purpose**: Display phrase translation result

**Features**:
- Styled card with primary container color
- Original phrase and translation
- Save/unsave toggle
- Handles empty state gracefully

### StateDisplays.kt
**Purpose**: Display different UI states

**Components**:
- `LoadingStateIndicator` - Shows progress
- `ErrorStateDisplay` - Shows error messages
- `EmptyStateDisplay` - Shows empty state message

**Benefits**:
- Consistent state display across screens
- Easy to update look and feel
- Reusable everywhere

### TranslationComponents.kt
**Purpose**: Translation-specific UI elements

**Components**:
- `TranslationResultHeader` - Full translation display
- `WordByWordHeader` - Section header

## Usage Pattern

### Before (Monolithic)
```kotlin
@Composable
fun TranslatorContent(viewModel: DictionaryViewModel) {
    Column {
        // Input...
        // State handling...
        // Word row inline...
        Row {
            Icon(...) // Star
            Text(...) // English
            Text(...) // Arabic
        }
        // Phrase builder inline...
        // Result display inline...
    }
}
// Total: 100+ lines, hard to maintain
```

### After (Component-Based)
```kotlin
@Composable
fun TranslatorContent(viewModel: DictionaryViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column {
        // Input...
        // State handling...
        when (uiState) {
            is DictionaryUiState.Success -> {
                ResultsContent(
                    state = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun ResultsContent(...) {
    LazyColumn {
        item { TranslationResultHeader(...) }
        item { PhraseBuilderSection(...) }
        item { PhraseResultCard(...) }
        item { WordByWordHeader() }
        items(words) { WordRowItem(...) }
    }
}
// Total: ~40 lines, clear responsibility
```

## Import Strategy

### Option 1: Import All Components
```kotlin
import com.example.dicto.ui.components.*  // Recommended
```

### Option 2: Import Specific Components
```kotlin
import com.example.dicto.ui.components.WordRowItem
import com.example.dicto.ui.components.PhraseBuilderSection
import com.example.dicto.ui.components.PhraseResultCard
```

## Testing Strategy

### Unit Tests
```kotlin
@Test
fun testWordRowItem() {
    composeTestRule.setContent {
        WordRowItem(
            wordResult = WordResult("test", "اختبار"),
            onToggleSave = { /* test */ }
        )
    }
    // Assert rendering...
}
```

### Composition Tests
```kotlin
@Test
fun testPhraseBuildingFlow() {
    composeTestRule.setContent {
        PhraseBuilderSection(
            words = listOf("hello", "world"),
            onPhraseChanged = { /* test */ }
        )
    }
    // Assert interactions...
}
```

## Extension Guide

### Adding a New Component

1. **Create file** in `ui/components/`
   ```kotlin
   // MyNewComponent.kt
   @Composable
   fun MyNewComponent(
       data: MyData,
       callback: (String) -> Unit,
       modifier: Modifier = Modifier
   ) {
       // Implementation
   }
   ```

2. **Add KDoc**
   ```kotlin
   /**
    * MyNewComponent - Brief description
    *
    * Single Responsibility: What this does
    * Used in: Where it's used
    *
    * @param data Data to display
    * @param callback Callback for user actions
    */
   ```

3. **Test it**
   ```kotlin
   @Test
   fun testMyNewComponent() { /* ... */ }
   ```

4. **Use it** in parent components
   ```kotlin
   @Composable
   fun ParentScreen() {
       // ...
       MyNewComponent(data = ..., callback = { ... })
   }
   ```

## Migration Checklist

When refactoring screens to use components:

- [ ] Extract UI elements into separate components
- [ ] Identify single responsibility for each
- [ ] Move to `ui/components/` directory
- [ ] Add KDoc with purpose and usage
- [ ] Update parent screen to use new components
- [ ] Write unit tests for new components
- [ ] Test integration with parent screen
- [ ] Update documentation

## Performance Optimization

### Recomposition Tracking

```kotlin
// Component recomposes only when these parameters change:
@Composable
fun WordRowItem(
    wordResult: WordResult,  // ← Changes trigger recompose
    onToggleSave: (String) -> Unit  // ← Functions don't trigger (reference stable)
) { }
```

### Best Practices

1. **Use Lambda Parameters Correctly**
   ```kotlin
   // Good: Callback is stable
   onToggleSave: (String) -> Unit
   
   // Bad: Causes recomposition
   onToggleSave: { viewModel.toggleSave(it) }  // Create locally instead
   ```

2. **Extract State Management**
   ```kotlin
   // State stays in parent, component is stateless
   val selectedPhrase by viewModel.selectedPhrase.collectAsState()
   PhraseBuilderSection(words = words, onPhraseChanged = { ... })
   ```

3. **Use Key() for Lists**
   ```kotlin
   items(wordResults, key = { it.original }) { word ->
       WordRowItem(...)
   }
   ```

## Summary

| Aspect | Before | After |
|--------|--------|-------|
| Lines per file | 100+ | 30-50 |
| Code reuse | Low | High |
| Test difficulty | Hard | Easy |
| Maintenance | Difficult | Easy |
| Readability | Poor | Excellent |

By splitting components, we achieve:
✅ Better maintainability
✅ Higher reusability
✅ Easier testing
✅ Clearer code structure
✅ Better performance
✅ Easier to extend

