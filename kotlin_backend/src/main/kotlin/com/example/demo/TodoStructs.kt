package com.example.demo

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import kotlinx.serialization.Serializable


@Serializable
data class Todo(
    var name: String = "",
    val todos: MutableMap<Int, TodoRow> = mutableMapOf()
) {
    fun apply(action: Action) {
        when (action) {
            is AddAction -> {
                val index = if (this.todos.isNotEmpty()) {
                    this.todos.keys.max() + 1
                } else {
                    0
                }
                this.todos[index] = action.row
            }

            is UpdateAction -> {
                this.todos[action.index] = action.row
            }

            is ChangeNameAction -> {
                this.name = action.name
            }

            is RemoveAction -> {
                this.todos.remove(action.index)
            }

            is RemoveCompletedAction -> {
                this.todos.entries.filter { it.value.completed }.forEach { this.todos.remove(it.key) }
            }
        }
    }
}

@Serializable
data class TodoRow(
    val name: String,
    val completed: Boolean
)

@JsonTypeInfo(
    property = "type",
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AddAction::class, name = "Add"),
    JsonSubTypes.Type(value = UpdateAction::class, name = "Update"),
    JsonSubTypes.Type(value = RemoveAction::class, name = "Remove"),
    JsonSubTypes.Type(value = ChangeNameAction::class, name = "ChangeName"),
    JsonSubTypes.Type(value = RemoveCompletedAction::class, name = "RemoveCompleted")
)
sealed class Action(
    val type: Type
) {
    enum class Type {
        Add, Update, ChangeName, Remove, RemoveCompleted
    }
}

data class AddAction(
    val row: TodoRow
) : Action(type = Type.Add)

data class UpdateAction(
    val index: Int,
    val row: TodoRow
) : Action(type = Type.Update)

data class ChangeNameAction(
    val name: String
) : Action(type = Type.ChangeName)

data class RemoveAction(
    val index: Int
) : Action(type = Type.Remove)

data class RemoveCompletedAction(
    val index: Int
) : Action(type = Type.RemoveCompleted)

@JsonTypeInfo(
    property = "type",
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = FullServerMessage::class, name = "Full"),
    JsonSubTypes.Type(value = PatchServerMessage::class, name = "Patch"),
)
sealed class ServerMessage(
    val type: Type
) {
    enum class Type {
        Full, Patch
    }
}

data class FullServerMessage(
    val todo: Todo
) : ServerMessage(type = Type.Full)

data class PatchServerMessage(
    val ops: List<String>
) : ServerMessage(type = Type.Patch)
