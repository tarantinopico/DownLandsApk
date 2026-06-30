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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryManagerUiState(
    val categories: List<CategoryWithMatchCount> = emptyList(),
    val isLoading: Boolean = true
)

data class CategoryWithMatchCount(
    val category: Category,
    val matchCount: Int
)

class CategoryManagerViewModel(
    private val categoryRepository: CategoryRepository,
    private val fileRepository: FileRepository
) : ViewModel() {

    val uiState: StateFlow<CategoryManagerUiState> = combine(
        categoryRepository.allCategories,
        fileRepository.allFiles
    ) { categories, files ->
        val counts = categories.map { cat ->
            CategoryWithMatchCount(
                category = cat,
                matchCount = files.count { cat.matches(it) }
            )
        }
        CategoryManagerUiState(
            categories = counts,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoryManagerUiState(isLoading = true)
    )
    
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY]) as DownLandsApp
                return CategoryManagerViewModel(
                    application.container.categoryRepository,
                    application.container.fileRepository
                ) as T
            }
        }
    }
}
