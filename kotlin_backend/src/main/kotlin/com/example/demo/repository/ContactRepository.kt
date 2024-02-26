package com.example.demo.repository

import com.example.demo.ContactBook
import com.example.demo.ContactRow
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface ContactRepository : MongoRepository<ContactBookDoc, String>

@Document(collection = "contact")
data class ContactBookDoc(
    @Id
    val id: String,
    var name: String = "",
    val contacts: Map<Int, ContactRowDoc> = mapOf(),
    val updatedAt: Instant = Instant.now()
) {
    fun toContactBook() = ContactBook(
        id = this.id,
        name = this.name,
        contacts = this.contacts.mapValues { it.value.toContactRow() }.toMutableMap()
    )

    companion object {
        fun from(contactBook: ContactBook) = ContactBookDoc(
            id = contactBook.id,
            name = contactBook.name,
            contacts = contactBook.contacts.mapValues { ContactRowDoc.from(it.value) }
        )
    }
}

data class ContactRowDoc(
    val name: String,
    val nonEngage: Boolean
) {
    fun toContactRow() = ContactRow(name = this.name, nonEngage = this.nonEngage)

    companion object {
        fun from(contactRow: ContactRow) = ContactRowDoc(name = contactRow.name, nonEngage = contactRow.nonEngage)
    }
}