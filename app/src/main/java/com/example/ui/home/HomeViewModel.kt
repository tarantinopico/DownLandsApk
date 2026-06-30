package com.example.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.DownLandsApp
import com.example.data.preferences.SettingsRepository
import com.example.data.preferences.SortOrder
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.ViewMode
import com.example.data.repository.CategoryRepository
import com.example.data.repository.FileRepository
import com.example.domain.Category
import com.example.domain.FileItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DateRange { ALL, LAST_24H, LAST_7D, LAST_30D }

data class FilterState(
    val query: String = "",
    val activeTypes: Set<String> = emptySet(),
    val activeCategories: Set<String> = emptySet(),
    val dateRange: DateRange = DateRange.ALL
)

sealed class HomeUiState {
    object Loading : HomeUiState()
    object NeedsPermission : HomeUiState()
    data class Success(
        val treeUri: String,
        val files: List<FileItemUI>,
        val categories: List<Category>,
        val currentRoute: String,
        val viewMode: ViewMode,
        val groupByCategory: Boolean,
        val sortOrder: SortOrder,
        val filters: FilterState,
        val themeMode: ThemeMode
    ) : HomeUiState()
}

class HomeViewModel(
    private val fileRepository: FileRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            categoryRepository.initDefaultCategoriesIfEmpty()
        }
    }

    private val filterStateFlow = MutableStateFlow(FilterState())

    private data class CombinedSettings(
        val currentRoute: String,
        val viewMode: ViewMode,
        val groupByCategory: Boolean,
        val sortOrder: SortOrder,
        val filters: FilterState,
        val themeMode: ThemeMode
    )

    private val settingsFlow1 = combine(
        settingsRepository.currentRouteFlow,
        settingsRepository.viewModeFlow,
        settingsRepository.groupByCategoryFlow
    ) { route, viewMode, groupBy -> 
        Triple(route, viewMode, groupBy)
    }

    private val settingsFlow2 = combine(
        settingsRepository.sortOrderFlow,
        filterStateFlow,
        settingsRepository.themeModeFlow
    ) { sortOrder, filters, themeMode ->
        Triple(sortOrder, filters, themeMode)
    }

    private val settingsFlow = combine(settingsFlow1, settingsFlow2) { t1, t2 ->
        CombinedSettings(t1.first, t1.second, t1.third, t2.first, t2.second, t2.third)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.treeUriFlow,
        fileRepository.allFiles,
        categoryRepository.allCategories,
        settingsFlow
    ) { treeUri, files, categories, settings ->
        if (treeUri == null) {
            HomeUiState.NeedsPermission
        } else {
            val filesWithCategories = files.map { file ->
                val matchingCats = categories.filter { it.matches(file) }
                FileItemUI(file, matchingCats)
            }
            
            var filtered = filesWithCategories

            // 1. Route filter
            if (settings.currentRoute == "recent") {
                val oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
                filtered = filtered.filter { it.file.dateModified >= oneWeekAgo }
            } else if (settings.currentRoute == "category:uncategorized") {
                filtered = filtered.filter { it.matchingCategories.isEmpty() }
            } else if (settings.currentRoute.startsWith("category:")) {
                val catId = settings.currentRoute.removePrefix("category:")
                filtered = filtered.filter { item -> item.matchingCategories.any { it.id == catId } }
            }

            // 2. Search query filter
            if (settings.filters.query.isNotBlank()) {
                val q = settings.filters.query.lowercase()
                filtered = filtered.filter { it.file.displayName.lowercase().contains(q) }
            }

            // 3. Types filter
            if (settings.filters.activeTypes.isNotEmpty()) {
                filtered = filtered.filter { settings.filters.activeTypes.contains(it.file.mimeType.substringBefore("/")) }
            }

            // 4. Categories filter (from filter sheet)
            if (settings.filters.activeCategories.isNotEmpty()) {
                filtered = filtered.filter { item -> 
                    item.matchingCategories.any { settings.filters.activeCategories.contains(it.id) }
                }
            }

            // 5. Date Range filter
            if (settings.filters.dateRange != DateRange.ALL) {
                val now = System.currentTimeMillis()
                val threshold = when (settings.filters.dateRange) {
                    DateRange.LAST_24H -> now - (24L * 60 * 60 * 1000)
                    DateRange.LAST_7D -> now - (7L * 24 * 60 * 60 * 1000)
                    DateRange.LAST_30D -> now - (30L * 24 * 60 * 60 * 1000)
                    else -> 0L
                }
                filtered = filtered.filter { it.file.dateModified >= threshold }
            }

            // 6. Sort
            filtered = when (settings.sortOrder) {
                SortOrder.NEWEST -> filtered.sortedByDescending { it.file.dateModified }
                SortOrder.OLDEST -> filtered.sortedBy { it.file.dateModified }
                SortOrder.NAME_ASC -> filtered.sortedBy { it.file.displayName.lowercase() }
                SortOrder.NAME_DESC -> filtered.sortedByDescending { it.file.displayName.lowercase() }
                SortOrder.SIZE_DESC -> filtered.sortedByDescending { it.file.sizeBytes }
                SortOrder.SIZE_ASC -> filtered.sortedBy { it.file.sizeBytes }
                SortOrder.TYPE -> filtered.sortedBy { it.file.extension }
            }
            
            HomeUiState.Success(
                treeUri = treeUri,
                files = filtered,
                categories = categories,
                currentRoute = settings.currentRoute,
                viewMode = settings.viewMode,
                groupByCategory = settings.groupByCategory,
                sortOrder = settings.sortOrder,
                filters = settings.filters,
                themeMode = settings.themeMode
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    fun onTreeUriGranted(uri: Uri) {
        viewModelScope.launch {
            settingsRepository.setTreeUri(uri.toString())
            refreshFiles(uri.toString())
        }
    }
    
    fun refresh(uriString: String) {
        viewModelScope.launch {
            refreshFiles(uriString)
        }
    }
    
    fun navigateTo(route: String) {
        viewModelScope.launch {
            settingsRepository.setCurrentRoute(route)
        }
    }

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch {
            settingsRepository.setViewMode(mode)
        }
    }

    fun toggleGroupByCategory() {
        viewModelScope.launch {
            val currentState = (uiState.value as? HomeUiState.Success)?.groupByCategory ?: false
            settingsRepository.setGroupByCategory(!currentState)
        }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            settingsRepository.setSortOrder(order)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun updateSearchQuery(query: String) {
        filterStateFlow.update { it.copy(query = query) }
    }

    fun toggleTypeFilter(type: String) {
        filterStateFlow.update { state ->
            val types = state.activeTypes.toMutableSet()
            if (types.contains(type)) types.remove(type) else types.add(type)
            state.copy(activeTypes = types)
        }
    }

    fun toggleCategoryFilter(categoryId: String) {
        filterStateFlow.update { state ->
            val cats = state.activeCategories.toMutableSet()
            if (cats.contains(categoryId)) cats.remove(categoryId) else cats.add(categoryId)
            state.copy(activeCategories = cats)
        }
    }

    fun setDateRange(range: DateRange) {
        filterStateFlow.update { it.copy(dateRange = range) }
    }
    
    fun clearFilters() {
        filterStateFlow.update { FilterState(query = it.query) } // keep search query
    }

    private suspend fun refreshFiles(treeUriString: String) {
        try {
            fileRepository.refreshFiles(treeUriString)
        } catch (e: Exception) {
            // Handle error
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
                return HomeViewModel(
                    application.container.fileRepository,
                    application.container.categoryRepository,
                    application.container.settingsRepository
                ) as T
            }
        }
    }
}

