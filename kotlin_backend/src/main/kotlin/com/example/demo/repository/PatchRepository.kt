package com.example.demo.repository

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface PatchRepository : MongoRepository<PatchDoc, String>

@Document(collection = "patch")
data class PatchDoc(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val ops: JsonNode,
    val createdAt: Instant = Instant.now()
)