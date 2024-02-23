package com.example.demo

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document


@Document(collection = "contact")
data class ContactBook(
    @Id
    val id: String,
    var name: String = "",
    val contacts: MutableMap<Int, ContactRow> = mutableMapOf()
) {
    fun apply(action: Action) {
        when (action) {
            is AddAction -> {
                val index = if (this.contacts.isNotEmpty()) {
                    this.contacts.keys.max() + 1
                } else {
                    0
                }
                this.contacts[index] = action.row
            }

            is UpdateAction -> {
                this.contacts[action.index] = action.row
            }

            is ChangeNameAction -> {
                this.name = action.name
            }

            is RemoveAction -> {
                this.contacts.remove(action.index)
            }

            is RemoveCompletedAction -> {
                this.contacts.entries.filter { it.value.nonEngage }.forEach { this.contacts.remove(it.key) }
            }
        }
    }
}

data class ContactRow(
    val name: String,
    val nonEngage: Boolean
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
    val row: ContactRow
) : Action(type = Type.Add)

data class UpdateAction(
    val index: Int,
    val row: ContactRow
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
    val contactBook: ContactBook
) : ServerMessage(type = Type.Full)

data class PatchServerMessage(
    val ops: JsonNode
) : ServerMessage(type = Type.Patch)
