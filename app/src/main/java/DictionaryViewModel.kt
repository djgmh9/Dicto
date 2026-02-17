import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Represents the different states of our screen
sealed interface DictionaryUiState {
    data object Idle : DictionaryUiState
    data object Loading : DictionaryUiState
    data class Success(val result: String) : DictionaryUiState
    data class Error(val message: String) : DictionaryUiState
}

class DictionaryViewModel : ViewModel() {

    private val repository = TranslationRepository()

    // Backing property to avoid external modification
    private val _uiState = MutableStateFlow<DictionaryUiState>(DictionaryUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private var currentQuery = ""

    fun onQueryChanged(newQuery: String) {
        currentQuery = newQuery
        if (newQuery.isBlank()) {
            _uiState.value = DictionaryUiState.Idle
            return
        }

        // Optimisation: You might want to "Debounce" here (wait for user to stop typing)
        // For simplicity, we translate on button press in the UI
    }

    fun translate() {
        if (currentQuery.isBlank()) return

        _uiState.value = DictionaryUiState.Loading

        viewModelScope.launch {
            val result = repository.translateText(currentQuery)

            result.onSuccess { translatedText ->
                _uiState.value = DictionaryUiState.Success(translatedText)
            }.onFailure { error ->
                _uiState.value = DictionaryUiState.Error(error.localizedMessage ?: "Unknown error")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.close() // Cleanup memory
    }
}