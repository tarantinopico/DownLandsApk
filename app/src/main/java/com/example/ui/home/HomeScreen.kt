package com.example.ui.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.preferences.ViewMode
import com.example.domain.Category
import com.example.ui.components.DownLandsLogo
import com.example.utils.FormatUtils
import com.example.utils.IconUtils
import kotlinx.coroutines.launch

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCategoryManager: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val treeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)
            viewModel.onTreeUriGranted(it)
        }
    }

    if (uiState is HomeUiState.Success) {
        val successState = uiState as HomeUiState.Success
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DownLandsLogo(modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "DownLands",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider()
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        label = { Text("All Files") },
                        selected = successState.currentRoute == "all",
                        onClick = {
                            viewModel.navigateTo("all")
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) }, // Use a clock icon if available
                        label = { Text("Recent") },
                        selected = successState.currentRoute == "recent",
                        onClick = {
                            viewModel.navigateTo("recent")
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        label = { Text("Uncategorized") },
                        selected = successState.currentRoute == "category:uncategorized",
                        onClick = {
                            viewModel.navigateTo("category:uncategorized")
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "Categories",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    successState.categories.forEach { category ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = IconUtils.getIconByName(category.iconName),
                                    contentDescription = null,
                                    tint = Color(category.color)
                                )
                            },
                            label = { Text(category.name) },
                            selected = successState.currentRoute == "category:${category.id}",
                            onClick = {
                                viewModel.navigateTo("category:${category.id}")
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Manage Categories") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToCategoryManager()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "Theme",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val modes = listOf(
                            com.example.data.preferences.ThemeMode.SYSTEM to "System",
                            com.example.data.preferences.ThemeMode.LIGHT to "Light",
                            com.example.data.preferences.ThemeMode.DARK to "Dark"
                        )
                        modes.forEach { (mode, label) ->
                            FilterChip(
                                selected = successState.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(label) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        ) {
            HomeContent(
                uiState = successState,
                viewModel = viewModel,
                onMenuClick = { scope.launch { drawerState.open() } },
                treeLauncher = { treeLauncher.launch(null) }
            )
        }
    } else {
        Scaffold { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (uiState) {
                    is HomeUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is HomeUiState.NeedsPermission -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Welcome to DownLands",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "To manage your downloaded files, DownLands needs access to your Downloads folder.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { treeLauncher.launch(null) }) {
                                Text("Grant Access")
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    uiState: HomeUiState.Success,
    viewModel: HomeViewModel,
    onMenuClick: () -> Unit,
    treeLauncher: () -> Unit
) {
    val context = LocalContext.current
    var isSearchActive by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val selectedIds = remember { mutableStateListOf<String>() }
    val isSelectionMode = selectedIds.isNotEmpty()
    
    val title = when {
        uiState.currentRoute == "recent" -> "Recent"
        uiState.currentRoute == "category:uncategorized" -> "Uncategorized"
        uiState.currentRoute.startsWith("category:") -> {
            val catId = uiState.currentRoute.removePrefix("category:")
            uiState.categories.find { it.id == catId }?.name ?: "Category"
        }
        else -> "All Files"
    }

    if (showSortSheet) {
        SortBottomSheet(
            currentSort = uiState.sortOrder,
            onSortSelected = { viewModel.setSortOrder(it) },
            onDismiss = { showSortSheet = false }
        )
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            filters = uiState.filters,
            categories = uiState.categories,
            onToggleType = { viewModel.toggleTypeFilter(it) },
            onToggleCategory = { viewModel.toggleCategoryFilter(it) },
            onSetDateRange = { viewModel.setDateRange(it) },
            onClearFilters = { viewModel.clearFilters() },
            onDismiss = { showFilterSheet = false }
        )
    }

    BackHandler(enabled = isSearchActive || isSelectionMode) {
        if (isSelectionMode) {
            selectedIds.clear()
        } else if (isSearchActive) {
            isSearchActive = false
            viewModel.updateSearchQuery("")
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedIds.clear() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear selection")
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            val uris = uiState.files.filter { selectedIds.contains(it.file.id) }.map { it.file.uri }
                            val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                type = "*/*"
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share files"))
                            val count = selectedIds.size
                            selectedIds.clear()
                            scope.launch {
                                snackbarHostState.showSnackbar("Shared $count files")
                            }
                        }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share selected")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            } else if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = uiState.filters.query,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search files...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            isSearchActive = false
                            viewModel.updateSearchQuery("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (uiState.filters.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                        IconButton(onClick = { showSortSheet = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        
                        var viewModeExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { viewModeExpanded = true }) {
                                val icon = when (uiState.viewMode) {
                                    ViewMode.LIST -> Icons.Default.ViewList
                                    ViewMode.GRID -> Icons.Default.GridView
                                    ViewMode.GALLERY -> Icons.Default.Image
                                }
                                Icon(icon, contentDescription = "View Mode")
                            }
                            DropdownMenu(
                                expanded = viewModeExpanded,
                                onDismissRequest = { viewModeExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("List View") },
                                    leadingIcon = { Icon(Icons.Default.ViewList, null) },
                                    onClick = { viewModel.setViewMode(ViewMode.LIST); viewModeExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Grid View") },
                                    leadingIcon = { Icon(Icons.Default.GridView, null) },
                                    onClick = { viewModel.setViewMode(ViewMode.GRID); viewModeExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Gallery View") },
                                    leadingIcon = { Icon(Icons.Default.Image, null) },
                                    onClick = { viewModel.setViewMode(ViewMode.GALLERY); viewModeExpanded = false }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(if (uiState.groupByCategory) "Ungroup" else "Group by Category") },
                                    onClick = { viewModel.toggleGroupByCategory(); viewModeExpanded = false }
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.files.isEmpty()) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { 
                        isRefreshing = true
                        viewModel.refresh(uiState.treeUri)
                        scope.launch {
                            delay(1000)
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No files found. Pull to refresh.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                val groupedFiles = if (uiState.groupByCategory) {
                    uiState.files.groupBy { it.matchingCategories.firstOrNull()?.name ?: "Uncategorized" }
                } else {
                    mapOf("" to uiState.files)
                }

                val totalSize = uiState.files.sumOf { it.file.sizeBytes }
                val uncategorizedCount = uiState.files.count { it.matchingCategories.isEmpty() }

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { 
                        isRefreshing = true
                        viewModel.refresh(uiState.treeUri)
                        scope.launch {
                            delay(1000)
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        StatsHeader(
                            totalFiles = uiState.files.size,
                            totalSize = totalSize,
                            uncategorizedCount = uncategorizedCount,
                            modifier = Modifier.padding(16.dp)
                        )

                    Crossfade(targetState = uiState.viewMode, label = "ViewMode", modifier = Modifier.weight(1f)) { mode ->
                    when (mode) {
                        ViewMode.LIST -> {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                groupedFiles.forEach { (categoryName, files) ->
                                    if (categoryName.isNotEmpty()) {
                                        item(key = categoryName) {
                                            Text(
                                                text = categoryName,
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    items(files, key = { it.file.id }) { item ->
                                        val isSelected = selectedIds.contains(item.file.id)
                                        val toggleSelection = {
                                            if (isSelected) selectedIds.remove(item.file.id)
                                            else selectedIds.add(item.file.id)
                                        }
                                        FileItemCard(
                                            item = item,
                                            selected = isSelected,
                                            modifier = Modifier.animateItemPlacement(),
                                            onClick = {
                                                if (isSelectionMode) toggleSelection()
                                                else openFile(context, item)
                                            },
                                            onLongClick = { toggleSelection() }
                                        )
                                    }
                                }
                            }
                        }
                        ViewMode.GRID -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                groupedFiles.forEach { (categoryName, files) ->
                                    if (categoryName.isNotEmpty()) {
                                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                                            Text(
                                                text = categoryName,
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    items(files, key = { it.file.id }) { item ->
                                        val isSelected = selectedIds.contains(item.file.id)
                                        val toggleSelection = {
                                            if (isSelected) selectedIds.remove(item.file.id)
                                            else selectedIds.add(item.file.id)
                                        }
                                        FileGridItem(
                                            item = item,
                                            selected = isSelected,
                                            onClick = {
                                                if (isSelectionMode) toggleSelection()
                                                else openFile(context, item)
                                            },
                                            onLongClick = { toggleSelection() }
                                        )
                                    }
                                }
                            }
                        }
                        ViewMode.GALLERY -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                groupedFiles.forEach { (categoryName, files) ->
                                    if (categoryName.isNotEmpty()) {
                                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                            Text(
                                                text = categoryName,
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    items(files, key = { it.file.id }) { item ->
                                        val isSelected = selectedIds.contains(item.file.id)
                                        val toggleSelection = {
                                            if (isSelected) selectedIds.remove(item.file.id)
                                            else selectedIds.add(item.file.id)
                                        }
                                        FileGalleryItem(
                                            item = item,
                                            selected = isSelected,
                                            onClick = {
                                                if (isSelectionMode) toggleSelection()
                                                else openFile(context, item)
                                            },
                                            onLongClick = { toggleSelection() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    } // close PullToRefreshBox
                }
                }
            }
        }
    }
}

private fun openFile(context: android.content.Context, item: FileItemUI) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(item.file.uri, item.file.mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: Exception) {
        // Handle error
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun FileItemCard(item: FileItemUI, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (item.file.mimeType.startsWith("image/") || item.file.mimeType.startsWith("video/")) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.file.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                        contentDescription = "File",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.file.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.file.extension.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = FormatUtils.formatSize(item.file.sizeBytes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = FormatUtils.formatRelativeTime(item.file.dateModified),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.matchingCategories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        item.matchingCategories.forEach { cat ->
                            CategoryChip(category = cat)
                        }
                    }
                }
            }
            
            IconButton(onClick = { /* TODO: Share */ }) {
                Icon(Icons.Outlined.Share, contentDescription = "Share")
            }
        }
    }
}

@Composable
fun CategoryChip(category: Category) {
    Surface(
        color = Color(category.color).copy(alpha = 0.15f),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = IconUtils.getIconByName(category.iconName),
                contentDescription = null,
                tint = Color(category.color),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall,
                color = Color(category.color)
            )
        }
    }
}

