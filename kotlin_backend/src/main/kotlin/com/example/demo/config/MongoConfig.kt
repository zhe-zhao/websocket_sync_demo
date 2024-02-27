package com.example.demo.config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

@Configuration
class MongoConfig {
    @Bean
    fun mongoCustomConversions(): MongoCustomConversions {
        return MongoCustomConversions(listOf(JsonNodeToStringConverter()))
    }
}

class JsonNodeToStringConverter : Converter<JsonNode, String> {
    override fun convert(jsonNode: JsonNode): String {
        return jacksonObjectMapper().apply {
            registerModules(JavaTimeModule())
        }.writeValueAsString(jsonNode)
    }
}