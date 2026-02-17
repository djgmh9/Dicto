# Test Suite Documentation

## Overview
This document describes the test suite for the Dicto app, covering unit tests, integration tests, and UI tests.

## Test Structure

```
app/src/
├── test/java/com/example/dicto/           # Unit Tests (JVM)
│   ├── DictionaryViewModelTest.kt
│   ├── DataClassesTest.kt
│   └── WordStorageTest.kt
│
└── androidTest/java/com/example/dicto/    # Instrumented Tests (Android)
    ├── DictionaryScreenUITest.kt
    └── ClipboardMonitoringIntegrationTest.kt
```

## Dependencies

The following test dependencies have been added to `build.gradle.kts`:

```kotlin
// Unit Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("app.cash.turbine:turbine:1.0.0")
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("androidx.arch.core:core-testing:2.2.0")

// Android Instrumented Testing
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
```

## Unit Tests

### 1. DictionaryViewModelTest.kt
Tests the core business logic of the ViewModel.

**Coverage:**
- ✅ Search query updates
- ✅ Debouncing (600ms delay)
- ✅ Clipboard monitoring enable/disable
- ✅ Clipboard text processing
- ✅ Duplicate detection
- ✅ Phrase building
- ✅ UI state transitions (Idle → Loading → Success/Error)
- ✅ Word saving

**Key Test Cases:**
```kotlin
// Search Query
- onQueryChanged updates search query
- empty/blank query returns Idle state
- search query is debounced for 600ms

// Clipboard Monitoring
- clipboard monitoring is enabled by default
- toggleClipboardMonitoring changes state
- onClipboardTextFound updates query when enabled
- ignores when monitoring disabled
- ignores blank text
- ignores duplicate text

// Phrase Building
- empty list clears phrase
- joins words with space
- triggers translation

// UI State
- emits Loading before translation
- multiple queries cancel previous
- clearing query returns to Idle
```

**Run Tests:**
```bash
./gradlew test --tests DictionaryViewModelTest
```

### 2. DataClassesTest.kt
Tests data classes and sealed interfaces.

**Coverage:**
- ✅ WordResult creation and copying
- ✅ DictionaryUiState variants
- ✅ Data class equality
- ✅ Default values

**Run Tests:**
```bash
./gradlew test --tests DataClassesTest
```

### 3. WordStorageTest.kt
Tests DataStore-based word persistence.

**Note:** These are template tests. For full functionality, WordStorage needs dependency injection to accept a test DataStore instance.

**Coverage:**
- ✅ Initial empty state
- ✅ Toggle word (add/remove)
- ✅ Multiple words handling

**Refactoring Needed:**
To make WordStorage fully testable, refactor to accept DataStore via constructor:

```kotlin
class WordStorage(private val dataStore: DataStore<Preferences>) {
    // Current implementation
}
```

Then in tests:
```kotlin
val testDataStore = PreferenceDataStoreFactory.create(
    scope = testScope,
    produceFile = { tmpFolder.newFile("test.preferences_pb") }
)
val storage = WordStorage(testDataStore)
```

**Run Tests:**
```bash
./gradlew test --tests WordStorageTest
```

## Instrumented Tests (Android)

### 1. DictionaryScreenUITest.kt
Tests Compose UI components.

**Coverage:**
- ✅ Clipboard toggle display
- ✅ Search field input
- ✅ Toggle switch interaction
- ✅ Saved words display
- ✅ Star button clicks

**Prerequisites:**
1. Add test tags to composables:
```kotlin
Switch(
    // ...
    modifier = Modifier.testTag("clipboard_toggle")
)

OutlinedTextField(
    // ...
    modifier = Modifier.testTag("search_field")
)
```

2. Create test ViewModel or use mocks

**Run Tests:**
```bash
./gradlew connectedAndroidTest --tests DictionaryScreenUITest
```

### 2. ClipboardMonitoringIntegrationTest.kt
Tests clipboard system integration.

**Coverage:**
- ✅ Set and retrieve text
- ✅ Arabic text handling
- ✅ HTML content
- ✅ Empty clips
- ✅ Multiple items
- ✅ MIME type detection
- ✅ CoerceToText functionality

**Run Tests:**
```bash
./gradlew connectedAndroidTest --tests ClipboardMonitoringIntegrationTest
```

## Running All Tests

### All Unit Tests (Fast)
```bash
./gradlew test
```

### All Instrumented Tests (Requires Device/Emulator)
```bash
./gradlew connectedAndroidTest
```

### All Tests
```bash
./gradlew test connectedAndroidTest
```

### With Coverage Report
```bash
./gradlew testDebugUnitTest jacocoTestReport
```

## Test Reports

After running tests, view HTML reports at:
- Unit tests: `app/build/reports/tests/testDebugUnitTest/index.html`
- Instrumented tests: `app/build/reports/androidTests/connected/index.html`

## Continuous Integration

Add to your CI pipeline (GitHub Actions example):

```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
      - name: Run Unit Tests
        run: ./gradlew test
      - name: Run Instrumented Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 29
          script: ./gradlew connectedAndroidTest
```

## Known Limitations

1. **WordStorageTest**: Requires DI refactoring for full testing
2. **UI Tests**: Need test tags added to composables
3. **Translation API**: Tests use actual API (consider mocking)
4. **ViewModel Tests**: Some tests skip actual translation results

## Future Test Improvements

- [ ] Mock TranslationRepository for predictable test results
- [ ] Add screenshot tests for UI
- [ ] Test error scenarios (network failures)
- [ ] Test rotation/configuration changes
- [ ] Add performance benchmarks
- [ ] Test accessibility features
- [ ] Add Robolectric tests for faster Android tests

## Writing New Tests

### Unit Test Template
```kotlin
@Test
fun `descriptive test name in backticks`() = runTest {
    // Given (setup)
    val input = "test"
    
    // When (action)
    viewModel.onQueryChanged(input)
    advanceUntilIdle()
    
    // Then (assertion)
    assertEquals(expected, actual)
}
```

### UI Test Template
```kotlin
@Test
fun buttonClick_triggersAction() {
    composeTestRule.setContent {
        MyComposable()
    }
    
    composeTestRule.onNodeWithText("Button")
        .performClick()
        
    composeTestRule.onNodeWithText("Result")
        .assertIsDisplayed()
}
```

## Troubleshooting

### Tests fail with "No tests found"
- Ensure test files are in correct directories
- Check test class/method naming
- Verify JUnit dependencies

### Instrumented tests fail
- Ensure emulator/device is connected: `adb devices`
- Check API level compatibility
- Grant required permissions

### Flaky tests
- Add proper test synchronization
- Use `advanceUntilIdle()` in coroutine tests
- Avoid hardcoded delays

## Support

For test-related questions:
1. Check test documentation in code comments
2. Review existing test examples
3. Consult official docs:
   - [Android Testing](https://developer.android.com/training/testing)
   - [Compose Testing](https://developer.android.com/jetpack/compose/testing)
   - [Coroutines Testing](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)

