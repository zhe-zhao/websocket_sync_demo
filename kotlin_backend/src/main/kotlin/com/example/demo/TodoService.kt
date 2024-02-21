package com.example.demo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.reidsync.kxjsonpatch.JsonDiff
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.springframework.stereotype.Service

@Service
class TodoService {
    private final val mapper = jacksonObjectMapper()
    private final val todoInstance = Todo()

    fun applyAction(action: Action) {
        // Grab a mutable reference
        // Serialize out the existing JSON for diffing later on
        val existingState = Json.encodeToJsonElement(this.todoInstance.copy())

        // apply changes
        todoInstance.apply(action)

        // Serialize out the new JSON for diffing
        val newState = Json.encodeToJsonElement(this.todoInstance.copy())

        // Get the changes using the `JsonDiff.asJson` method
        val ops = JsonDiff.asJson(existingState, newState);

        println("New Patches: $ops")
    }

    fun getFull(): Todo = todoInstance
}