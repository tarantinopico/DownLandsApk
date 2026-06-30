package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.DownLandsApp
import com.example.data.preferences.SettingsRepository
import com.example.data.preferences.SortOrder
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.ViewMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.data.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val treeUri: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultViewMode: ViewMode = ViewMode.LIST,
    val defaultSortOrder: SortOrder = SortOrder.NEWEST
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val categoryRepository: CategoryRepository,
    private val backupManager: com.example.data.repository.BackupManager
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.treeUriFlow,
        settingsRepository.themeModeFlow,
        settingsRepository.dynamicColorFlow,
        settingsRepository.viewModeFlow,
        settingsRepository.sortOrderFlow
    ) { treeUri, themeMode, dynamicColor, viewMode, sortOrder ->
        SettingsUiState(treeUri, themeMode, dynamicColor, viewMode, sortOrder)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDynamicColor(enabled) }
    }

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch { settingsRepository.setViewMode(mode) }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch { settingsRepository.setSortOrder(order) }
    }
    
    fun setTreeUri(uri: String) {
        viewModelScope.launch { settingsRepository.setTreeUri(uri) }
    }
    
    suspend fun exportData(): String {
        return backupManager.createBackup()
    }
    
    suspend fun importData(jsonString: String) {
        backupManager.restoreBackup(jsonString)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val container = (application as DownLandsApp).container
                return SettingsViewModel(
                    container.settingsRepository,
                    container.categoryRepository,
                    container.backupManager
                ) as T
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategoryManager: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, flags)
            viewModel.setTreeUri(it.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                Text(
                    text = "Storage",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                ListItem(
                    headlineContent = { Text("Downloads Folder") },
                    supportingContent = { Text(uiState.treeUri?.let { Uri.parse(it).path } ?: "Not selected") },
                    leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                    modifier = Modifier.clickable { folderPickerLauncher.launch(null) }
                )
                HorizontalDivider()
            }
            
            item {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                var showThemeDialog by remember { mutableStateOf(false) }
                if (showThemeDialog) {
                    AlertDialog(
                        onDismissRequest = { showThemeDialog = false },
                        title = { Text("Theme Mode") },
                        text = {
                            Column {
                                ThemeMode.values().forEach { mode ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setThemeMode(mode)
                                                showThemeDialog = false
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(selected = uiState.themeMode == mode, onClick = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") }
                        }
                    )
                }
                
                ListItem(
                    headlineContent = { Text("Theme") },
                    supportingContent = { Text(uiState.themeMode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                    modifier = Modifier.clickable { showThemeDialog = true }
                )
                
                ListItem(
                    headlineContent = { Text("Dynamic Color") },
                    supportingContent = { Text("Use system wallpaper colors (Android 12+)") },
                    leadingContent = { Icon(Icons.Default.ColorLens, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = uiState.dynamicColor,
                            onCheckedChange = { viewModel.setDynamicColor(it) }
                        )
                    }
                )
                HorizontalDivider()
            }
            
            item {
                Text(
                    text = "Organization",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                ListItem(
                    headlineContent = { Text("Manage Categories") },
                    leadingContent = { Icon(Icons.Default.Label, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToCategoryManager() }
                )
                
                HorizontalDivider()
                
                Text(
                    text = "Backup & Restore",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                val scope = rememberCoroutineScope()
                val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
                    uri?.let {
                        scope.launch {
                            val data = viewModel.exportData()
                            context.contentResolver.openOutputStream(it)?.use { out ->
                                out.write(data.toByteArray())
                            }
                        }
                    }
                }

                val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    uri?.let {
                        scope.launch {
                            val json = context.contentResolver.openInputStream(it)?.use { inp ->
                                inp.bufferedReader().readText()
                            }
                            if (json != null) {
                                viewModel.importData(json)
                            }
                        }
                    }
                }

                ListItem(
                    headlineContent = { Text("Export Data") },
                    supportingContent = { Text("Save categories and settings to a JSON file") },
                    leadingContent = { Icon(Icons.Default.Upload, contentDescription = null) },
                    modifier = Modifier.clickable { exportLauncher.launch("downlands_backup.json") }
                )

                ListItem(
                    headlineContent = { Text("Import Data") },
                    supportingContent = { Text("Restore from a previous JSON backup") },
                    leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                    modifier = Modifier.clickable { importLauncher.launch(arrayOf("application/json")) }
                )
            }
        }
    }
}
