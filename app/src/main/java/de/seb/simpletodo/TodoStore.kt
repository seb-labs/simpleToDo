package de.seb.simpletodo

import org.json.JSONArray
import org.json.JSONObject

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
    val array = JSONArray(raw)
    buildList {
        for (index in 0 until array.length()) {
            val obj = array.getJSONObject(index)
            add(
                TodoItem(
                    id = obj.optLong("id", System.currentTimeMillis() + index),
                    text = obj.optString("text", "").trim(),
                    done = obj.optBoolean("done", false),
                ),
            )
        }
    }.filter { it.text.isNotBlank() }
}.getOrDefault(emptyList())
