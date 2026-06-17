package de.seb.simpletodo

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

fun todoStateToJson(state: ToDoUiState): String {
    val items = JSONArray()
    state.items.forEach { item ->
        items.put(
            JSONObject()
                .put("id", item.id)
                .put("text", item.text)
                .put("done", item.done),
        )
    }

    return JSONObject()
        .put("sortOrder", state.sortOrder.name)
        .put("items", items)
        .toString()
}

fun todoStateFromJson(raw: String): ToDoUiState = runCatching {
    when (val parsed = JSONTokener(raw).nextValue()) {
        is JSONArray -> ToDoUiState(
            items = parsed.toTodoItems(),
            sortOrder = SortOrder.NEWEST,
        )

        is JSONObject -> ToDoUiState(
            items = parsed.optJSONArray("items")?.toTodoItems().orEmpty(),
            sortOrder = SortOrder.fromStoredName(parsed.optString("sortOrder", SortOrder.NEWEST.name)),
        )

        else -> ToDoUiState()
    }
}.getOrDefault(ToDoUiState())

private fun JSONArray.toTodoItems(): List<TodoItem> = buildList {
    for (index in 0 until length()) {
        val obj = getJSONObject(index)
        add(
            TodoItem(
                id = obj.optLong("id", System.currentTimeMillis() + index),
                text = obj.optString("text", "").trim(),
                done = obj.optBoolean("done", false),
            ),
        )
    }
}.filter { it.text.isNotBlank() }
