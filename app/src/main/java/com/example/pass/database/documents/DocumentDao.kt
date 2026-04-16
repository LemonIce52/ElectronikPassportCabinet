package com.example.pass.database.documents

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Insert
    suspend fun saveDocument(document: DocumentEntity)

    @Query("SELECT * FROM Document WHERE typeDocument IN (:typeDocuments) AND nameDocument LIKE '%' || :searchQuery || '%'")
    fun getDocumentsFlow(typeDocuments: List<TypeDocument>, searchQuery: String): Flow<List<DocumentEntity>>

}