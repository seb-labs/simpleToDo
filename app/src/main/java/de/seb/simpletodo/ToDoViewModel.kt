package de.seb.simpletodo

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import java.io.File

class ToDoViewModel(application: Application) : AndroidViewModel(application) {
    private val storeFile = File(application.filesDir, STORE_FILE_NAME)

    var state by mutableStateOf(loadState())
        private set

    fun addTodo(text: String) {
        val cleaned = text.trim()
        if (cleaned.isBlank()) return

        val nextItems = listOf(
            TodoItem(
                id = System.currentTimeMillis(),
                text = cleaned,
                done = false,
            ),
        ) + state.items

        state = state.copy(items = nextItems)
        persist(nextItems)
    }

    fun moveTodo(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val items = state.items.toMutableList()
        if (fromIndex !in items.indices || toIndex !in items.indices) return

        val item = items.removeAt(fromIndex)
        items.add(toIndex, item)
        updateItems(items)
    }

    fun toggleTodo(id: Long) {
        updateItems(
            state.items.map { item ->
                if (item.id == id) item.copy(done = !item.done) else item
            },
        )
    }

    fun deleteTodo(id: Long) {
        updateItems(state.items.filterNot { it.id == id })
        if (state.editingId == id) {
            cancelEdit()
        }
    }

    fun startEditing(id: Long) {
        val item = state.items.firstOrNull { it.id == id } ?: return
        state = state.copy(editingId = id, editingText = item.text)
    }

    fun onEditTextChange(value: String) {
        state = state.copy(editingText = value)
    }

    fun saveEdit() {
        val editingId = state.editingId ?: return
        val text = state.editingText.trim()
        if (text.isBlank()) return

        updateItems(
            state.items.map { item ->
                if (item.id == editingId) item.copy(text = text) else item
            },
        )
        cancelEdit()
    }

    fun cancelEdit() {
        state = state.copy(editingId = null, editingText = "")
    }

    private fun updateItems(items: List<TodoItem>) {
        state = state.copy(items = items)
        persist(items)
    }

    private fun loadState(): ToDoUiState = runCatching {
        if (!storeFile.exists()) return ToDoUiState()
        ToDoUiState(items = todoItemsFromJson(storeFile.readText()))
    }.getOrDefault(ToDoUiState())

    private fun persist(items: List<TodoItem>) {
        runCatching {
            storeFile.writeText(todoItemsToJson(items))
        }
    }

    private companion object {
        const val STORE_FILE_NAME = "todos.json"
    }
}
