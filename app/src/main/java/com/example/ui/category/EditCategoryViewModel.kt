package com.example.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.DownLandsApp
import com.example.data.repository.CategoryRepository
import com.example.data.repository.FileRepository
import com.example.domain.Category
import com.example.domain.MatchType
import com.example.domain.Rule
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class EditCategoryUiState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val color: Int = android.graphics.Color.parseColor("#4CAF50"),
    val iconName: String = "Category",
    val matchType: MatchType = MatchType.OR,
    val rules: List<Rule> = emptyList(),
    val liveMatchCount: Int = 0,
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

class EditCategoryViewModel(
    private val categoryId: String?,
    private val categoryRepository: CategoryRepository,
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditCategoryUiState())
    val uiState: StateFlow<EditCategoryUiState> = _uiState.asStateFlow()

    init {
        if (categoryId != null) {
            viewModelScope.launch {
                val category = categoryRepository.allCategories.first().find { it.id == categoryId }
                if (category != null) {
                    _uiState.update {
                        it.copy(
                            id = category.id,
                            name = category.name,
                            color = category.color,
                            iconName = category.iconName,
                            matchType = category.matchType,
                            rules = category.rules
                        )
                    }
                }
                updateLiveCount()
            }
        } else {
            updateLiveCount()
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
        updateLiveCount()
    }

    fun updateColor(color: Int) {
        _uiState.update { it.copy(color = color) }
    }

    fun updateIcon(iconName: String) {
        _uiState.update { it.copy(iconName = iconName) }
    }
    
    fun updateMatchType(matchType: MatchType) {
        _uiState.update { it.copy(matchType = matchType) }
        updateLiveCount()
    }

    fun addRule(rule: Rule) {
        _uiState.update { it.copy(rules = it.rules + rule) }
        updateLiveCount()
    }

    fun updateRule(updatedRule: Rule) {
        _uiState.update { state ->
            state.copy(rules = state.rules.map { if (it.id == updatedRule.id) updatedRule else it })
        }
        updateLiveCount()
    }

    fun removeRule(ruleId: String) {
        _uiState.update { it.copy(rules = it.rules.filterNot { r -> r.id == ruleId }) }
        updateLiveCount()
    }

    private fun updateLiveCount() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val tempCategory = Category(
                id = currentState.id,
                name = currentState.name,
                color = currentState.color,
                iconName = currentState.iconName,
                matchType = currentState.matchType,
                rules = currentState.rules
            )
            val files = fileRepository.allFiles.first()
            val matchCount = files.count { tempCategory.matches(it) }
            _uiState.update { it.copy(liveMatchCount = matchCount) }
        }
    }

    fun saveCategory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val currentState = _uiState.value
            val category = Category(
                id = currentState.id,
                name = currentState.name.ifBlank { "Unnamed Category" },
                color = currentState.color,
                iconName = currentState.iconName,
                matchType = currentState.matchType,
                rules = currentState.rules
            )
            categoryRepository.saveCategory(category)
            _uiState.update { it.copy(isSaving = false, saved = true) }
        }
    }

    companion object {
        fun provideFactory(categoryId: String?): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY]) as DownLandsApp
                return EditCategoryViewModel(
                    categoryId,
                    application.container.categoryRepository,
                    application.container.fileRepository
                ) as T
            }
        }
    }
}
