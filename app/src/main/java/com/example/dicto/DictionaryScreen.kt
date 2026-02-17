package com.example.dicto

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.TextStyle

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
@Composable
fun DictionaryScreen(
    modifier: Modifier = Modifier,
    viewModel: DictionaryViewModel = viewModel()
) {
    // 0. State to track which tab is selected (0 = Home, 1 = Saved)
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        // 1. Add the Bottom Bar
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Translator") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Saved") },
                    label = { Text("Saved") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { innerPadding ->
        // 2. Switch Content based on tab
        Box(modifier = Modifier.padding(innerPadding)) {
            if (selectedTab == 0) {
                TranslatorContent(viewModel)
            } else {
                SavedWordsContent(viewModel)
            }
        }
    }
}

@Composable
fun TranslatorContent(viewModel: DictionaryViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Move the local textInput state here
    var textInput by remember { mutableStateOf("") }
    // Watch the phrase builder states
    val selectedPhrase by viewModel.selectedPhrase.collectAsState()
    val phraseTranslation by viewModel.phraseTranslation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- INPUT SECTION ---
        Text("Arabic -> English Dictionary", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = textInput,
            onValueChange = {
                textInput = it
                viewModel.onQueryChanged(it)
            },
            label = {
                // We can also align the label to the right
                Text("أدخل جملة (Enter sentence)")
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,

            // --- NEW: FORCE RTL SUPPORT ---
            textStyle = TextStyle(
                textDirection = TextDirection.Rtl, // Forces text to start from the Right
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            ),
            // Optional: If you want the label to always align right too:
            // colors = OutlinedTextFieldDefaults.colors(),
            // but usually just the input text being RTL is enough.
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.translate() },
            enabled = uiState !is DictionaryUiState.Loading,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Translate")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- RESULTS SECTION ---
        when (val state = uiState) {
            is DictionaryUiState.Idle -> {
                Text("Enter text to start", style = MaterialTheme.typography.bodyLarge)
            }
            is DictionaryUiState.Loading -> {
                CircularProgressIndicator()
            }
            is DictionaryUiState.Error -> {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
            is DictionaryUiState.Success -> {
                // We use LazyColumn for the list so it scrolls efficiently
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Full Sentence Result (Existing)
                    item {
                        Text("Full Translation:", style = MaterialTheme.typography.labelLarge)
                        Text(state.fullTranslation, style = MaterialTheme.typography.headlineSmall)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    }

                    // 2. NEW: PHRASE BUILDER SECTION
                    item {
                        // Extract just the original words list from the result
                        val originalWords = state.wordTranslations.map { it.original }

                        PhraseBuilderSection(
                            words = originalWords,
                            onPhraseChanged = { viewModel.onPhraseSelectionChanged(it) }
                        )
                    }

                    // 3. NEW: PHRASE RESULT DISPLAY
                    item {
                        PhraseResultCard(
                            original = selectedPhrase,
                            translation = phraseTranslation,
                            onSave = { viewModel.toggleSave(selectedPhrase) } // You can save phrases too!
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        Text("Word by Word:", style = MaterialTheme.typography.labelLarge)
                    }

                    // 4. Individual Words (Existing)
                    items(state.wordTranslations) { wordItem ->
                        WordRowItem(
                            wordResult = wordItem,
                            onToggleSave = { viewModel.toggleSave(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedWordsContent(viewModel: DictionaryViewModel) {
    val savedWords by viewModel.savedWordsList.collectAsState()

    if (savedWords.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No saved words yet", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "My Vocabulary",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(savedWords) { wordResult ->
                // We reuse the exact same Row Item!
                // Because we pass 'onToggleSave', clicking the star here will UN-SAVE it
                // and it will instantly disappear from this list.
                WordRowItem(
                    wordResult = wordResult,
                    onToggleSave = { viewModel.toggleSave(it) }
                )
            }
        }
    }
}

// Helper composable for a single row
@Composable
fun WordRowItem(
    wordResult: WordResult,
    onToggleSave: (String) -> Unit // Callback function
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Star Icon + English
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onToggleSave(wordResult.original) }) {
                    Icon(
                        imageVector = if (wordResult.isSaved) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Save word",
                        tint = if (wordResult.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = wordResult.translation,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr)
                )
            }

            // RIGHT: Arabic
            Text(
                text = wordResult.original,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textDirection = TextDirection.Rtl
                )
            )
        }
    }
}
