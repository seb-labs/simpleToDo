package de.seb.simpletodo

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import java.io.File

class ToDoViewModel(application: Application) : AndroidViewModel(application) {
    private val storeFile = File(application.filesDir, STORE_FILE_NAME)

    var state by mutableStateOf(ToDoUiState(items = loadItems()))
        private set

    fun onAddTextChange(value: String) {
        state = state.copy(newTodoText = value)
    }

    fun addTodo() {
        val text = state.newTodoText.trim()
        if (text.isBlank()) return
        val next = listOf(
            TodoItem(
                id = System.currentTimeMillis(),
                text = text,
                done = false,
            ),
        ) + state.items
        state = state.copy(items = next, newTodoText = "")
        persist(next)
    }

    fun toggleTodo(id: Long) {
        updateItems { items ->
            items.map { item -> if (item.id == id) item.copy(done = !item.done) else item }
        }
    }

    fun deleteTodo(id: Long) {
        updateItems { items -> items.filterNot { it.id == id } }
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
        updateItems { items ->
            items.map { item -> if (item.id == editingId) item.copy(text = text) else item }
        }
        cancelEdit()
    }

    fun cancelEdit() {
        state = state.copy(editingId = null, editingText = "")
    }

    private fun updateItems(transform: (List<TodoItem>) -> List<TodoItem>) {
        val next = transform(state.items)
        state = state.copy(items = next)
        persist(next)
    }

    private fun loadItems(): List<TodoItem> = runCatching {
        if (!storeFile.exists()) return emptyList()
        todoItemsFromJson(storeFile.readText())
    }.getOrDefault(emptyList())

    private fun persist(items: List<TodoItem>) {
        runCatching {
            storeFile.writeText(todoItemsToJson(items))
        }
    }

    private companion object {
        const val STORE_FILE_NAME = "todos.json"
    }
}
