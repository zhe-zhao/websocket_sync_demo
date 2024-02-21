package com.example.demo

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


data class Todo(
    var name: String = "",
    val todos: MutableMap<Int, TodoRow> = mutableMapOf()
) {
    fun apply(action: Action) {
        when (action) {
            is AddAction -> {
                val index = if(this.todos.size > 0) {
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

data class TodoRow(
    val name: String,
    val completed: Boolean
)

@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = AddAction::class, name = "Add"),
    JsonSubTypes.Type(value = UpdateAction::class, name = "Update"),
    JsonSubTypes.Type(value = RemoveAction::class, name = "Remove"),
    JsonSubTypes.Type(value = ChangeNameAction::class, name = "ChangeName"),
    JsonSubTypes.Type(value = RemoveCompletedAction::class, name = "RemoveCompleted")
)
sealed class Action(
    open val type: Type
) {
    enum class Type {
        Add, Update, ChangeName, Remove, RemoveCompleted
    }
}

data class AddAction(
    override val type: Type = Type.Add,
    val row: TodoRow
) : Action(type = type)

data class UpdateAction(
    override val type: Type = Type.Update,
    val index: Int,
    val row: TodoRow
) : Action(type = type)

data class ChangeNameAction(
    override val type: Type = Type.ChangeName,
    val name: String
) : Action(type = type)

data class RemoveAction(
    override val type: Type = Type.Remove,
    val index: Int
) : Action(type = type)

data class RemoveCompletedAction(
    override val type: Type = Type.RemoveCompleted,
    val index: Int
) : Action(type = type)

@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = FullServerMessage::class, name = "Full"),
    JsonSubTypes.Type(value = PatchServerMessage::class, name = "Patch"),
)
sealed class ServerMessage(
    open val type: Type
) {
    enum class Type {
        Full, Patch
    }
}

data class FullServerMessage(
    override val type: Type = Type.Full,
    val todo: Todo
) : ServerMessage(type = type)

data class PatchServerMessage(
    override val type: Type = Type.Patch,
    val ops: List<String>
) : ServerMessage(type = type)
