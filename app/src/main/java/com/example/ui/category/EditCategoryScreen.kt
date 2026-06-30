package com.example.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.MatchType
import com.example.domain.Rule
import com.example.utils.IconUtils
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryScreen(
    categoryId: String?,
    onNavigateBack: () -> Unit,
    viewModel: EditCategoryViewModel = viewModel(factory = EditCategoryViewModel.provideFactory(categoryId))
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (categoryId == null) "New Category" else "Edit Category") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveCategory() }, enabled = !uiState.isSaving && uiState.name.isNotBlank()) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Text("Color", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                val presetColors = listOf(
                    "#F44336", "#E91E63", "#9C27B0", "#673AB7", 
                    "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4", 
                    "#009688", "#4CAF50", "#8BC34A", "#CDDC39", 
                    "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
                ).map { android.graphics.Color.parseColor(it) }
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(presetColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .clickable { viewModel.updateColor(color) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.color == color) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            item {
                Text("Icon", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(IconUtils.allIcons) { iconName ->
                        val isSelected = uiState.iconName == iconName
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.updateIcon(iconName) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconUtils.getIconByName(iconName),
                                contentDescription = iconName,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rules", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Match: ", style = MaterialTheme.typography.bodyMedium)
                        SegmentedButton(
                            selected = uiState.matchType == MatchType.AND,
                            onClick = { viewModel.updateMatchType(MatchType.AND) },
                            text = "ALL"
                        )
                        SegmentedButton(
                            selected = uiState.matchType == MatchType.OR,
                            onClick = { viewModel.updateMatchType(MatchType.OR) },
                            text = "ANY"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Live preview: ${uiState.liveMatchCount} files match these rules",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(uiState.rules, key = { it.id }) { rule ->
                RuleItemCard(
                    rule = rule,
                    onUpdate = viewModel::updateRule,
                    onRemove = { viewModel.removeRule(rule.id) }
                )
            }

            item {
                var showAddRuleMenu by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { showAddRuleMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Rule")
                    }
                    
                    DropdownMenu(
                        expanded = showAddRuleMenu,
                        onDismissRequest = { showAddRuleMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Name contains...") },
                            onClick = {
                                viewModel.addRule(Rule.NameContains(UUID.randomUUID().toString(), ""))
                                showAddRuleMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Extension is...") },
                            onClick = {
                                viewModel.addRule(Rule.ExtensionIs(UUID.randomUUID().toString(), listOf("")))
                                showAddRuleMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Name starts with...") },
                            onClick = {
                                viewModel.addRule(Rule.NameStartsWith(UUID.randomUUID().toString(), ""))
                                showAddRuleMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RuleItemCard(rule: Rule, onUpdate: (Rule) -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val title = when (rule) {
                    is Rule.NameContains -> "Name contains"
                    is Rule.ExtensionIs -> "Extension is (comma separated)"
                    is Rule.NameStartsWith -> "Name starts with"
                    is Rule.NameEndsWith -> "Name ends with"
                }
                Text(title, style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove Rule")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            val value = when (rule) {
                is Rule.NameContains -> rule.text
                is Rule.ExtensionIs -> rule.extensions.joinToString(", ")
                is Rule.NameStartsWith -> rule.text
                is Rule.NameEndsWith -> rule.text
            }
            
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    val updatedRule = when (rule) {
                        is Rule.NameContains -> rule.copy(text = newValue)
                        is Rule.ExtensionIs -> rule.copy(extensions = newValue.split(",").map { it.trim() })
                        is Rule.NameStartsWith -> rule.copy(text = newValue)
                        is Rule.NameEndsWith -> rule.copy(text = newValue)
                    }
                    onUpdate(updatedRule)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SegmentedButton(selected: Boolean, onClick: () -> Unit, text: String) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        shape = CircleShape
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
