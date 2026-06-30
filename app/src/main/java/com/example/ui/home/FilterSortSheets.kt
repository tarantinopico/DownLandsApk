package com.example.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.preferences.SortOrder
import com.example.domain.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    currentSort: SortOrder,
    onSortSelected: (SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Divider()
            
            val sortOptions = listOf(
                SortOrder.NEWEST to "Newest modified",
                SortOrder.OLDEST to "Oldest modified",
                SortOrder.NAME_ASC to "Name (A-Z)",
                SortOrder.NAME_DESC to "Name (Z-A)",
                SortOrder.SIZE_DESC to "Largest first",
                SortOrder.SIZE_ASC to "Smallest first",
                SortOrder.TYPE to "File type"
            )
            
            sortOptions.forEach { (order, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSortSelected(order)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                    if (currentSort == order) {
                        Icon(Icons.Default.Check, contentDescription = "Selected")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    filters: FilterState,
    categories: List<Category>,
    onToggleType: (String) -> Unit,
    onToggleCategory: (String) -> Unit,
    onSetDateRange: (DateRange) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filters", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onClearFilters) {
                    Text("Clear all")
                }
            }
            Divider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Date Range
            Text(
                text = "Date Modified",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ranges = listOf(
                    DateRange.ALL to "Any time",
                    DateRange.LAST_24H to "Last 24h",
                    DateRange.LAST_7D to "Last 7 days",
                    DateRange.LAST_30D to "Last 30 days"
                )
                items(ranges) { (range, label) ->
                    FilterChip(
                        selected = filters.dateRange == range,
                        onClick = { onSetDateRange(range) },
                        label = { Text(label) }
                    )
                }
            }
            
            // Categories
            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = filters.activeCategories.contains(cat.id),
                            onClick = { onToggleCategory(cat.id) },
                            label = { Text(cat.name) }
                        )
                    }
                }
            }
            
            // File Types (basic predefined ones for filtering)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "File Types",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val commonTypes = listOf("image", "video", "audio", "application", "text")
                items(commonTypes) { type ->
                    FilterChip(
                        selected = filters.activeTypes.contains(type),
                        onClick = { onToggleType(type) },
                        label = { Text(type.capitalize()) }
                    )
                }
            }
        }
    }
}
