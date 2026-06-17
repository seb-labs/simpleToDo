package de.seb.simpletodo

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoScreen(
    state: ToDoUiState,
    onAddTodo: (String, Boolean) -> Unit,
    onMoveTodo: (Int, Int) -> Unit,
    onToggleTodo: (Long) -> Unit,
    onEditTodo: (Long) -> Unit,
    onDeleteTodo: (Long) -> Unit,
    onEditTextChange: (String) -> Unit,
    onEditImportantChange: (Boolean) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
) {
    val openCount = state.openCount
    val doneCount = state.doneCount
    var selectedTab by remember { mutableStateOf(TodoTab.OPEN) }
    var showAddDialog by remember { mutableStateOf(false) }
    var addDraft by remember { mutableStateOf("") }
    var addImportant by remember { mutableStateOf(false) }

    val listItems = when (selectedTab) {
        TodoTab.OPEN -> state.openItems
        TodoTab.DONE -> state.doneItems
    }
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AddTodoButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HeaderCard()
            TabBar(
                selectedTab = selectedTab,
                openCount = openCount,
                doneCount = doneCount,
                onTabSelected = { selectedTab = it },
            )

            if (listItems.isEmpty()) {
                EmptyStateCard()
            } else {
                if (selectedTab == TodoTab.OPEN) {
                    ReorderableTodoList(
                        items = listItems,
                        listState = listState,
                        onMoveTodo = onMoveTodo,
                        onToggleTodo = onToggleTodo,
                        onEditTodo = onEditTodo,
                        onDeleteTodo = onDeleteTodo,
                    )
                } else {
                    TodoList(
                        items = listItems,
                        onToggleTodo = onToggleTodo,
                        onEditTodo = onEditTodo,
                        onDeleteTodo = onDeleteTodo,
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                addDraft = ""
                addImportant = false
            },
            title = { Text("Aufgabe hinzufügen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = addDraft,
                        onValueChange = { addDraft = it },
                        label = { Text("To-do") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FilterChip(
                        selected = addImportant,
                        onClick = { addImportant = !addImportant },
                        label = { Text("Wichtig") },
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAddTodo(addDraft, addImportant)
                    addDraft = ""
                    addImportant = false
                    showAddDialog = false
                }) {
                    Text("Hinzufügen")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    addDraft = ""
                    addImportant = false
                    showAddDialog = false
                }) {
                    Text("Abbrechen")
                }
            },
        )
    }

    state.editingId?.let { editingId ->
        val item = state.items.firstOrNull { it.id == editingId }
        if (item != null) {
            AlertDialog(
                onDismissRequest = onCancelEdit,
                title = { Text("Eintrag bearbeiten") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = state.editingText,
                            onValueChange = onEditTextChange,
                            label = { Text("Aufgabe") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        FilterChip(
                            selected = state.editingImportant,
                            onClick = { onEditImportantChange(!state.editingImportant) },
                            label = { Text("Wichtig") },
                        )
                    }
                },
                confirmButton = { Button(onClick = onSaveEdit) { Text("Speichern") } },
                dismissButton = { OutlinedButton(onClick = onCancelEdit) { Text("Abbrechen") } },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabBar(
    selectedTab: TodoTab,
    openCount: Int,
    doneCount: Int,
    onTabSelected: (TodoTab) -> Unit,
) {
    TabRow(selectedTabIndex = selectedTab.ordinal) {
        Tab(
            selected = selectedTab == TodoTab.OPEN,
            onClick = { onTabSelected(TodoTab.OPEN) },
            text = { Text("Offen ($openCount)") },
        )
        Tab(
            selected = selectedTab == TodoTab.DONE,
            onClick = { onTabSelected(TodoTab.DONE) },
            text = { Text("Erledigt ($doneCount)") },
        )
    }
}

@Composable
private fun HeaderCard() {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.ListAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Deine Aufgaben", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Einfach eintragen, abhaken und bearbeiten.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTodoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp),
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Aufgabe hinzufügen")
    }
}

@Composable
private fun ReorderableTodoList(
    items: List<TodoItem>,
    listState: LazyListState,
    onMoveTodo: (Int, Int) -> Unit,
    onToggleTodo: (Long) -> Unit,
    onEditTodo: (Long) -> Unit,
    onDeleteTodo: (Long) -> Unit,
) {
    var draggingItemId by remember { mutableStateOf<Long?>(null) }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var draggedDistance by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(items) {
        if (draggingItemId != null) {
            val currentId = draggingItemId
            draggingIndex = items.indexOfFirst { it.id == currentId }.takeIf { it >= 0 }
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            val isDragging = draggingItemId == item.id
            val itemOffset = if (isDragging) draggedDistance.roundToInt() else 0

            TodoItemCard(
                item = item,
                showDragHandle = true,
                isDragging = isDragging,
                dragOffsetY = itemOffset,
                onToggle = onToggleTodo,
                onEdit = onEditTodo,
                onDelete = onDeleteTodo,
                onDragStart = {
                    draggingItemId = item.id
                    draggingIndex = index
                    draggedDistance = 0f
                },
                onDrag = { dragAmount ->
                    val currentIndex = draggingIndex ?: return@TodoItemCard
                    draggedDistance += dragAmount
                    val draggedInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == currentIndex } ?: return@TodoItemCard
                    val draggedCenter = draggedInfo.offset + draggedInfo.size / 2 + draggedDistance
                    val target = listState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
                        info.index != currentIndex && draggedCenter > info.offset && draggedCenter < info.offset + info.size
                    } ?: return@TodoItemCard
                    onMoveTodo(currentIndex, target.index)
                    draggingIndex = target.index
                    draggedDistance = 0f
                },
                onDragEnd = {
                    draggingItemId = null
                    draggingIndex = null
                    draggedDistance = 0f
                },
            )
        }
    }
}

@Composable
private fun TodoList(
    items: List<TodoItem>,
    onToggleTodo: (Long) -> Unit,
    onEditTodo: (Long) -> Unit,
    onDeleteTodo: (Long) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items.size) { index ->
            val item = items[index]
            TodoItemCard(
                item = item,
                showDragHandle = false,
                isDragging = false,
                dragOffsetY = 0,
                onToggle = onToggleTodo,
                onEdit = onEditTodo,
                onDelete = onDeleteTodo,
                onDragStart = {},
                onDrag = {},
                onDragEnd = {},
            )
        }
    }
}

@Composable
private fun TodoItemCard(
    item: TodoItem,
    showDragHandle: Boolean,
    isDragging: Boolean,
    dragOffsetY: Int,
    onToggle: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    val elevation by animateFloatAsState(if (isDragging) 8f else 0f, label = "drag_elevation")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = dragOffsetY.toFloat()
                shadowElevation = elevation
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                item.important && !item.done -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                item.done -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                else -> MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = item.done, onCheckedChange = { onToggle(item.id) })
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (item.important) {
                    Text(
                        text = "WICHTIG",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = item.text,
                    style = if (item.done) {
                        MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (showDragHandle) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .pointerInput(item.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { onDragStart() },
                                onDragEnd = { onDragEnd() },
                                onDragCancel = { onDragEnd() },
                                onDrag = { change, dragAmount ->
                                    change.consumeAllChanges()
                                    onDrag(dragAmount.y)
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "⋮⋮",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
            }
            IconButton(onClick = { onEdit(item.id) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Edit, contentDescription = "Bearbeiten", modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { onDelete(item.id) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Löschen", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Noch keine Aufgaben", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Trag oben einfach den ersten Punkt ein.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
