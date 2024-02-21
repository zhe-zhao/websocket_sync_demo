package com.example.demo

import org.springframework.stereotype.Service

@Service
class TodoService {
    private final val todoInstance = Todo()

    fun applyAction(action: Action) {
        todoInstance.apply(action)
    }

    fun getFull(): Todo = todoInstance
}