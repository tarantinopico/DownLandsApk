package com.example.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.utils.FormatUtils

@Composable
fun StatsHeader(
    totalFiles: Int,
    totalSize: Long,
    uncategorizedCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Total Files", style = MaterialTheme.typography.labelMedium)
                Text(totalFiles.toString(), style = MaterialTheme.typography.titleLarge)
            }
            Column {
                Text("Total Size", style = MaterialTheme.typography.labelMedium)
                Text(FormatUtils.formatSize(totalSize), style = MaterialTheme.typography.titleLarge)
            }
            Column {
                Text("Uncategorized", style = MaterialTheme.typography.labelMedium)
                Text(uncategorizedCount.toString(), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
