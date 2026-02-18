# Visual Architecture Guide

## Component Hierarchy

```
┌─────────────────────────────────────────────────────────────┐
│                       MainActivity                          │
│                    (Lifecycle & Navigation)                 │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                     MainContent                             │
│               (Navigation State Manager)                    │
└────────┬─────────────────────────────┬──────────────────────┘
         │                             │
         ▼                             ▼
    DictionaryScreen           SettingsScreen
    (Tab Router)               (Settings UI)
         │
    ┌────┴────┐
    │         │
    ▼         ▼
   TAB 0    TAB 1
    │         │
    ▼         ▼
Translator  SavedWords
Content     Content
    │         │
    ├─────────┤
    │         │
    ▼         ▼
  Results  Empty/List
  Content
```

## TranslatorContent Component Tree

```
TranslatorContent
│
├── OutlinedTextField (Input)
│
├── Button (Clear)
│
└── StateFlow Selection
    │
    ├── Idle
    │   └── EmptyStateDisplay
    │
    ├── Loading
    │   └── LoadingStateIndicator
    │
    ├── Error
    │   └── ErrorStateDisplay
    │
    └── Success
        └── ResultsContent
            │
            ├── TranslationResultHeader
            │
            ├── PhraseBuilderSection
            │   ├── WordFilterChip[]
            │   └── Selection Logic
            │
            ├── PhraseResultCard
            │
            ├── WordByWordHeader
            │
            └── LazyColumn
                └── WordRowItem[]
                    ├── SaveWordIconButton
                    └── Text (Word + Translation)
```

## File Organization

```
app/src/main/java/com/example/dicto/
│
├── MainActivity.kt                          [Entry Point]
├── DictionaryViewModel.kt                   [State Management]
├── DictionaryScreen.kt                      [Screen Router]
│   ├── TranslatorContent (45 lines)
│   ├── ResultsContent (35 lines)
│   └── SavedWordsContent (30 lines)
│
├── TranslationRepository.kt                 [Translation API]
├── WordStorage.kt                           [Word Persistence]
│
├── ui/
│   ├── SettingsScreen.kt                    [Settings UI]
│   │
│   ├── components/                          [Reusable Components]
│   │   ├── Components.kt                    [Package Docs]
│   │   ├── WordRowItem.kt                   [Word Display]
│   │   ├── PhraseBuilderSection.kt          [Phrase Building]
│   │   ├── PhraseResultCard.kt              [Phrase Display]
│   │   ├── StateDisplays.kt                 [State UIs]
│   │   └── TranslationComponents.kt         [Headers & Dividers]
│   │
│   └── theme/
│       └── Theme files
│
└── utils/
    └── ClipboardMonitor.kt                  [Clipboard Logic]
```

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      User Input                             │
│          (Text field, Button click, Word click)             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   DictionaryViewModel                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Events:                                            │   │
│  │  - onQueryChanged(text)                             │   │
│  │  - onClipboardTextFound(text)                       │   │
│  │  - toggleClipboardMonitoring()                      │   │
│  │  - toggleSave(word)                                 │   │
│  └─────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
    uiState         clipboardMonitoring  savedWordsList
    StateFlow       StateFlow             StateFlow
        │                │                │
        ├────────────────┼────────────────┤
        │                │                │
        ▼                ▼                ▼
  TranslatorContent  SettingsScreen   SavedWordsContent
     [Uses]           [Uses]            [Uses]
        │                │                │
        ├────────────────┼────────────────┤
        │                │                │
        ▼                ▼                ▼
  Composed from      Composed from    Composed from
  Components         Components        Components
        │                │                │
        └────────────────┴────────────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │   Render UI          │
              │   to Screen          │
              └──────────────────────┘
```

## Component Usage Map

```
WordRowItem
├── TranslatorContent
│   └── ResultsContent
│       └── Word Results Loop
└── SavedWordsContent
    └── Saved Words Loop

EmptyStateDisplay
├── TranslatorContent (Idle state)
└── SavedWordsContent (No words)

LoadingStateIndicator
└── TranslatorContent (Loading state)

ErrorStateDisplay
└── TranslatorContent (Error state)

TranslationResultHeader
└── TranslatorContent (Success state)

PhraseBuilderSection
└── TranslatorContent (Success state)

PhraseResultCard
└── TranslatorContent (Success state)

WordByWordHeader
└── TranslatorContent (Success state)
```

## State Management Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Search Query                             │
│                  MutableStateFlow                           │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│              Debounce (600ms)                               │
│                                                             │
│  User stops typing → Wait 600ms → Emit if changed          │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│           FlatMapLatest (Translation)                       │
│                                                             │
│  Blank? → Idle                                              │
│  Not blank? → Loading → Translate → Success/Error          │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│          Combine (With Saved Words)                         │
│                                                             │
│  Mark which words are saved                                │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│            StateIn (Shared State)                           │
│                                                             │
│  Cached flow that multiple screens can observe             │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│      TranslatorContent & Components                         │
│                                                             │
│  Collect and display results                              │
└─────────────────────────────────────────────────────────────┘
```

## Navigation Structure

```
┌────────────────────────────────┐
│     NavigationBar              │
├────────────────────────────────┤
│ [Translator] [Saved] [Settings]│
└────────────────────────────────┘
         │         │         │
         ▼         ▼         ▼
    ┌────────┐ ┌───────┐ ┌──────────┐
    │DictSrc │ │DictSrc│ │Settings  │
    │Tab 0   │ │Tab 1  │ │Screen    │
    └────────┘ └───────┘ └──────────┘
         │         │         │
         ▼         ▼         ▼
    Translator  SavedWords SettingsUI
    Content     Content     (Clipboard
    (Input +    (List +     toggle +
     Results)   Star)       About)
```

## Dependency Graph

```
MainActivity
├── viewModel (DictionaryViewModel)
├── ClipboardMonitor
└── MainContent
    ├── DictionaryScreen
    │   ├── TranslatorContent
    │   │   ├── TranslationResultHeader
    │   │   ├── PhraseBuilderSection
    │   │   ├── PhraseResultCard
    │   │   ├── WordByWordHeader
    │   │   ├── WordRowItem
    │   │   ├── LoadingStateIndicator
    │   │   ├── ErrorStateDisplay
    │   │   └── EmptyStateDisplay
    │   │
    │   └── SavedWordsContent
    │       ├── WordRowItem
    │       └── EmptyStateDisplay
    │
    └── SettingsScreen
        ├── SettingsHeader
        ├── ClipboardMonitoringSettings
        └── AboutCard
```

## Clipboard Monitoring Flow

```
┌─────────────────────────────────────────────────────────────┐
│                 ClipboardMonitor                            │
│            (Isolated Utility Class)                         │
└────────────────┬────────────────────────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
   Check Every        Track Last
   1 Second           Clipboard Text
        │                 │
        └────────┬────────┘
                 │
                 ▼
        ┌──────────────────┐
        │ Text Changed?    │
        └────┬───────┬─────┘
             │       │
             ▼       ▼
            Yes      No
             │       │
             ▼       └→ Ignore
        Invoke
        Callback
             │
             ▼
    ViewModel.onClipboardTextFound(text)
             │
             ▼
    Update searchQuery StateFlow
             │
             ▼
    Triggers Translation Flow
             │
             ▼
    Results displayed in UI
```

## Testing Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Test Pyramid                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│                      Integration Tests                     │
│                    (Full Screens)                          │
│              ┌──────────────────────┐                      │
│              │  Component Tests     │                      │
│              │   (Composition)      │                      │
│    ┌─────────┴──────────────────────┴─────────┐           │
│    │         Unit Tests (Components)         │           │
│    │  ┌────────┬────────┬────────┬────────┐ │           │
│    │  │WordRow │Phrase  │State   │Headers │ │           │
│    │  │Item    │Builder │Display │        │ │           │
│    │  └────────┴────────┴────────┴────────┘ │           │
│    │                                         │           │
│    └─────────────────────────────────────────┘           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Recomposition Optimization

```
State Change in ViewModel
        │
        ▼
    uiState.update()
        │
        ├─→ TranslatorContent Recompose? YES
        │       │
        │       ├─→ ResultsContent Recompose? YES
        │       │   ├─→ TranslationResultHeader ✓ (changed)
        │       │   ├─→ PhraseBuilderSection ✓ (changed)
        │       │   ├─→ PhraseResultCard ✓ (changed)
        │       │   └─→ WordRowItem[] ✓ (changed)
        │       │
        │       └─→ OutlinedTextField ✗ (unchanged)
        │
        └─→ SavedWordsContent Recompose? NO
            └─→ WordRowItem[] ✗ (unchanged)

Result: Only affected components recompose ✅
```

This visual guide helps understand:
- Component hierarchy and relationships
- Data flow through the app
- State management patterns
- Navigation structure
- Testing strategy
- Performance optimization

Print or reference this guide when:
- Onboarding new team members
- Designing new features
- Reviewing component architecture
- Planning performance improvements
- Writing tests

