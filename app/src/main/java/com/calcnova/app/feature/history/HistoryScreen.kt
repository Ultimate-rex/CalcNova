package com.calcnova.app.feature.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.calcnova.app.api.HistoryEntry
import com.calcnova.app.core.JsonHistoryStore

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val store = remember { JsonHistoryStore(context) }
    var entries by remember { mutableStateOf(store.loadAll()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("History", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = {
                store.clearAll()
                entries = store.loadAll()
            }) {
                Text("Clear all")
            }
        }

        Spacer(Modifier.height(8.dp))

        if (entries.isEmpty()) {
            Text("No calculations yet.")
        } else {
            LazyColumn {
                items(entries.size) { index ->
                    HistoryRow(entry = entries[index]) {
                        store.deleteAt(index)
                        entries = store.loadAll()
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(entry.calculatorType, style = MaterialTheme.typography.labelSmall)
            Text(entry.expression, style = MaterialTheme.typography.bodyLarge)
            Text("= ${entry.result}", style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete entry")
        }
    }
}
