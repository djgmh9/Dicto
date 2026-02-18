# Code Duplication Report

## Issue Found: Old PhraseBuilderSection.kt

**Location**: `app/src/main/java/com/example/dicto/PhraseBuilderSection.kt`
**Status**: DUPLICATE - SHOULD BE DELETED

### Contains:
1. **PhraseBuilderSection** - Old implementation (137 lines total in file)
   - Duplicated in: `app/src/main/java/com/example/dicto/ui/components/PhraseBuilderSection.kt`
   - New version is cleaner with better documentation
   - Currently using the NEW version from ui/components

2. **PhraseResultCard** - Old implementation (lines 91-137)
   - Duplicated in: `app/src/main/java/com/example/dicto/ui/components/PhraseResultCard.kt`
   - New version has proper TTS support
   - Currently using the NEW version from ui/components

## Why This Happened

During refactoring to components:
1. Created new component files in `ui/components/` folder
2. Old files in root package were never deleted
3. DictionaryScreen.kt imports from `com.example.dicto.ui.components.*` (the NEW files)
4. Old files are completely unused

## Current Usage

✅ Using: `com.example.dicto.ui.components.PhraseBuilderSection`
❌ Unused: `com.example.dicto.PhraseBuilderSection`

✅ Using: `com.example.dicto.ui.components.PhraseResultCard`
❌ Unused: `com.example.dicto.PhraseResultCard` (inside old PhraseBuilderSection.kt)

## Other Files Checked

- `Utils.kt` - Used for clipboard utilities, no duplicates found
- All other files are in appropriate locations
- No other duplicates detected

## Solution

Delete the old file:
- `app/src/main/java/com/example/dicto/PhraseBuilderSection.kt`

This file contains obsolete code that has been properly refactored into:
- `app/src/main/java/com/example/dicto/ui/components/PhraseBuilderSection.kt`
- `app/src/main/java/com/example/dicto/ui/components/PhraseResultCard.kt`

