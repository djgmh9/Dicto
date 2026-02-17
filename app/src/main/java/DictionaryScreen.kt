import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DictionaryScreen(
    modifier: Modifier = Modifier,
    viewModel: DictionaryViewModel = viewModel()
) {
    // Collect the state flow
    val uiState by viewModel.uiState.collectAsState()

    // Local state for the text field
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Arabic -> English Dictionary",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = textInput,
            onValueChange = {
                textInput = it
                viewModel.onQueryChanged(it)
            },
            label = { Text("Enter word") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.translate() },
            enabled = uiState !is DictionaryUiState.Loading
        ) {
            Text("Translate")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // UI State Handling
        when (val state = uiState) {
            is DictionaryUiState.Idle -> {
                Text("Enter a word to start", style = MaterialTheme.typography.bodyLarge)
            }
            is DictionaryUiState.Loading -> {
                CircularProgressIndicator()
                Text("Downloading model or translating...", modifier = Modifier.padding(top = 8.dp))
            }
            is DictionaryUiState.Success -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Translation:", style = MaterialTheme.typography.labelMedium)
                        Text(text = state.result, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
            is DictionaryUiState.Error -> {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}