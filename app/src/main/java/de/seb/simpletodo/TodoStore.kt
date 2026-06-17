package de.seb.simpletodo

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

fun todoItemsToJson(items: List<TodoItem>): String {
    val array = JSONArray()
    items.forEach { item ->
        array.put(
            JSONObject()
                .put("id", item.id)
                .put("text", item.text)
                .put("done", item.done),
        )
    }
    return array.toString()
}

fun todoItemsFromJson(raw: String): List<TodoItem> = runCatching {
    when (val parsed = JSONTokener(raw).nextValue()) {
        is JSONArray -> parsed.toTodoItems()
        is JSONObject -> parsed.optJSONArray("items")?.toTodoItems().orEmpty()
        else -> emptyList()
    }
}.getOrDefault(emptyList())

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
