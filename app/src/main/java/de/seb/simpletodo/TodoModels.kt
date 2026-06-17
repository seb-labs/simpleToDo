package de.seb.simpletodo

enum class TodoTab(val label: String) {
    OPEN("Offen"),
    DONE("Erledigt"),
}

data class TodoItem(
    val id: Long,
    val text: String,
    val done: Boolean = false,
)

data class ToDoUiState(
    val items: List<TodoItem> = emptyList(),
    val editingId: Long? = null,
    val editingText: String = "",
) {
    val openItems: List<TodoItem>
        get() = items.filterNot { it.done }

    val doneItems: List<TodoItem>
        get() = items.filter { it.done }

    val openCount: Int
        get() = openItems.size

    val doneCount: Int
        get() = doneItems.size
}

fun List<TodoItem>.normalizedForTabs(): List<TodoItem> {
    val open = filterNot { it.done }
    val done = filter { it.done }
    return open + done
}
