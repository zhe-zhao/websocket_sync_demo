package com.example.demo.repository

import com.example.demo.ContactBook
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ContactRepository : MongoRepository<ContactBook, String>
