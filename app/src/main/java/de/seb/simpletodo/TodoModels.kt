package de.seb.simpletodo

data class TodoItem(
    val id: Long,
    val text: String,
    val done: Boolean = false,
)

data class ToDoUiState(
    val items: List<TodoItem> = emptyList(),
    val editingId: Long? = null,
    val editingText: String = "",
)
