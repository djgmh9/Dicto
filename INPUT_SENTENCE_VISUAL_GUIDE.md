# Input Sentence TTS - Visual Guide

## Feature Overview

### The Speaker Icon Button
Located next to the text input field, allowing users to hear the entire sentence they typed in Arabic.

## Visual Examples

### Example 1: Empty Input Field
```
┌─────────────────────────────────────┐
│                                     │
│ أدخل جملة                          │
│ (Input field - no speaker icon)     │
│                                     │
└─────────────────────────────────────┘

Status: Speaker icon NOT visible (field is empty)
```

### Example 2: User Types Text
```
┌──────────────────────────────────────┐
│                                      │
│ مرحبا كيف حالك        🔊            │
│ (Text input field)  (Speaker button) │
│                                      │
└──────────────────────────────────────┘

Status: Speaker icon VISIBLE (text is present)
User can tap 🔊 to hear the entire sentence
```

### Example 3: With Long Text
```
┌────────────────────────────────────────────┐
│                                            │
│ السلام عليكم ورحمة الله وبركاته   🔊     │
│ (Long sentence)                  (Speaker) │
│                                            │
└────────────────────────────────────────────┘

Status: Speaker icon shows regardless of text length
Always positioned after the text input
```

## Button Styling

### Speaker Icon Specifications
- **Icon**: VolumeUp (from Material Icons)
- **Color**: Primary theme color
- **Size**: 24.dp icon, 48.dp button area
- **Location**: Right side of input field (after text)
- **Visibility**: Conditional (only when text is not empty)

### Visual Appearance
```
Button: [🔊]
       ↑
    Speaker icon
    
Color: Material.Primary
Size: 24.dp icon
Padding: 12.dp around icon (48.dp total)
```

## Interaction Flow

### Step 1: User Opens App
```
┌──────────────────────────────────────┐
│                                      │
│ أدخل جملة                           │
│ (Empty field)                        │
│                                      │
│ → Speaker icon is NOT visible        │
└──────────────────────────────────────┘
```

### Step 2: User Starts Typing
```
┌──────────────────────────────────────┐
│                                      │
│ م                              🔊    │
│ (One letter)  (Speaker appears)      │
│                                      │
└──────────────────────────────────────┘

Transition: Icon fades in as user types first character
```

### Step 3: User Continues Typing
```
┌──────────────────────────────────────┐
│                                      │
│ مرحبا                          🔊    │
│ (More text)   (Button available)     │
│                                      │
└──────────────────────────────────────┘

User can tap 🔊 at any time
```

### Step 4: User Taps Speaker Icon
```
User taps 🔊
    ↓
Animation: Subtle button press feedback
    ↓
Audio: Sentence pronounced in Arabic
    ↓
Duration: 1-5 seconds depending on text length
```

### Step 5: User Clears Text
```
User taps "Clear" button
    ↓
┌──────────────────────────────────────┐
│ أدخل جملة                           │
│ (Empty field)                        │
│ → Speaker icon fades out             │
└──────────────────────────────────────┘

Icon transition: Smooth disappear animation
```

## Layout with RTL Support

### Arabic (RTL) Layout
```
Text flows right to left: ← ← ← ← ←

┌──────────────────────────────────────┐
│ 🔊          مرحبا كيف حالك          │
│ (Speaker)     (Arabic text RTL)      │
└──────────────────────────────────────┘

Button positioned on left (visual right in RTL)
Text fills remaining space
```

### English (LTR) Support
If app were in English:
```
Text flows left to right: → → → → →

┌──────────────────────────────────────┐
│ Hello how are you               🔊   │
│ (English text LTR)         (Speaker) │
└──────────────────────────────────────┘

Button positioned on right (visual right in LTR)
```

## Audio Behavior

### What User Hears When Clicking 🔊

#### Example 1: Short Sentence
```
Input: "مرحبا"
Click: 🔊
Wait: 1-2 seconds (first time only)
Hear: "Mar-haba" (Hello) in Arabic
Duration: ~0.5 seconds
```

#### Example 2: Medium Sentence
```
Input: "مرحبا كيف حالك؟"
Click: 🔊
Wait: <100ms (after initialization)
Hear: "Marhaba kayf haluka?" in Arabic
Duration: ~2 seconds
```

#### Example 3: Long Sentence
```
Input: "السلام عليكم ورحمة الله وبركاته"
Click: 🔊
Wait: <100ms
Hear: Full greeting in Arabic
Duration: ~3-4 seconds
```

## Performance Timeline

### First Use
```
Time | Event
────────────────────────────────────────
0ms  | User taps 🔊
     | TTS Engine initializing
1000ms | TTS Ready
1500ms | Audio starts playing
3000ms | Audio completes
```

### Subsequent Uses
```
Time | Event
────────────────────────────────────────
0ms  | User taps 🔊
<100ms | Audio starts playing
1500ms | Audio completes
```

## Accessibility Features

### Screen Reader
```
Button description: "Pronounce input sentence"
Context: User can understand what button does
Icon: Proper contentDescription provided
```

### Keyboard Navigation
```
Tab → Focuses on speaker button
Enter → Activates pronunciation
Space → Activates pronunciation
```

### Visual Feedback
```
Button states:
- Normal: Primary color 🔊
- Pressed: Darker shade (visual feedback)
- Hidden: Icon not visible when field empty
```

## Responsive Design

### Phone Screen (Small)
```
┌──────────────────────────┐
│ أدخل          🔊        │
│ (Text fits field)        │
│                          │
└──────────────────────────┘
```

### Tablet Screen (Large)
```
┌────────────────────────────────────────┐
│ أدخل جملة (Enter sentence)    🔊      │
│ (More space available)               │
│                                       │
└────────────────────────────────────────┘
```

### Landscape Mode
```
┌────────────────────────────────────────────────────┐
│ السلام عليكم ورحمة الله      🔊                     │
│ (Wide layout - text has more space)                │
└────────────────────────────────────────────────────┘
```

## Integration with Other Features

### With Clear Button
```
┌──────────────────────────────────────┐
│ مرحبا                          🔊    │
│ (Text input)         (Pronounce)    │
│        [Clear] ← Below              │
└──────────────────────────────────────┘

Layout: Input field + Speaker above, Clear button below
```

### With Results Section
```
Full screen layout:
┌──────────────────────────────────────┐
│ أدخل جملة                      🔊    │ ← Input + Speaker
│                                      │
│ [Clear]                             │ ← Clear button
│                                      │
│ Full Translation: ...                │ ← Results start
│ ...                                 │
└──────────────────────────────────────┘
```

## Error Handling Visuals

### Empty Text Clicked
```
User clicks speaker with empty field:
(Icon shouldn't appear in first place)

Current state: Icon hidden ✓
No error shown (graceful handling)
```

### TTS Unavailable
```
Rare case - TTS engine unavailable:

Visual: Button still appears
Tap: No sound (graceful degradation)
App: Still works normally ✓
```

## Summary

### What You See
✅ Speaker icon appears when you type
✅ Icon disappears when field is empty
✅ Clean, integrated design
✅ Responsive to RTL layout

### What You Hear
✅ Clear Arabic pronunciation
✅ Natural speech pace
✅ Immediate after first initialization
✅ Repeatable infinitely

### User Experience
✅ Intuitive (speaker icon is universal)
✅ Accessible (keyboard support)
✅ Responsive (works on all screen sizes)
✅ Professional (polished UI)

The input sentence TTS feature provides a seamless way to hear the entire sentence pronounced! 🎉

