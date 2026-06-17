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
        ) + state.openItems + state.doneItems

        updateItems(nextItems)
    }

    fun moveTodo(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val open = state.openItems.toMutableList()
        if (fromIndex !in open.indices || toIndex !in open.indices) return

        val item = open.removeAt(fromIndex)
        open.add(toIndex, item)
        updateItems(open + state.doneItems)
    }

    fun toggleTodo(id: Long) {
        val open = state.openItems.toMutableList()
        val done = state.doneItems.toMutableList()

        val openIndex = open.indexOfFirst { it.id == id }
        if (openIndex >= 0) {
            val item = open.removeAt(openIndex)
            done.add(item.copy(done = true))
            updateItems(open + done)
            return
        }

        val doneIndex = done.indexOfFirst { it.id == id }
        if (doneIndex >= 0) {
            val item = done.removeAt(doneIndex)
            open.add(0, item.copy(done = false))
            updateItems(open + done)
        }
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
        val next = items.normalizedForTabs()
        state = state.copy(items = next)
        persist(next)
    }

    private fun loadState(): ToDoUiState = runCatching {
        if (!storeFile.exists()) return ToDoUiState()
        ToDoUiState(items = todoItemsFromJson(storeFile.readText()).normalizedForTabs())
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
