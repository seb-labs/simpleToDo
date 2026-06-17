package de.seb.simpletodo

enum class SortOrder(val label: String) {
    NEWEST("Neueste zuerst"),
    OLDEST("Älteste zuerst"),
    ALPHABETICAL("Alphabetisch"),
    DONE_FIRST("Erledigt zuerst");

    fun sort(items: List<TodoItem>): List<TodoItem> = when (this) {
        NEWEST -> items.sortedByDescending { it.id }
        OLDEST -> items.sortedBy { it.id }
        ALPHABETICAL -> items.sortedWith(compareBy<TodoItem> { it.text.lowercase() }.thenByDescending { it.id })
        DONE_FIRST -> items.sortedWith(compareByDescending<TodoItem> { it.done }.thenByDescending { it.id })
    }

    companion object {
        fun fromStoredName(name: String?): SortOrder = entries.firstOrNull { it.name == name } ?: NEWEST
    }
}

data class TodoItem(
    val id: Long,
    val text: String,
    val done: Boolean = false,
)

data class ToDoUiState(
    val items: List<TodoItem> = emptyList(),
    val newTodoText: String = "",
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val editingId: Long? = null,
    val editingText: String = "",
) {
    val sortedItems: List<TodoItem>
        get() = sortOrder.sort(items)
}
