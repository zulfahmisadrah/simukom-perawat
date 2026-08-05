package com.zulfahmi.simukomperawat.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.zulfahmi.simukomperawat.model.Question

@Dao
interface UkomDao {
    @Query("SELECT * FROM kumpulansoal WHERE jenis=:type AND paket=:pack")
    fun getSoal(type: String, pack: Int): LiveData<List<Question>>

    @Insert
    fun insertAll(questions: List<Question>)

    @Query("DELETE FROM kumpulansoal WHERE jenis = :type AND paket = :pack")
    fun deleteByTypeAndPack(type: String, pack: Int)

    @Query("SELECT COUNT(*) FROM kumpulansoal WHERE jenis = :type AND paket = :pack")
    fun countByTypeAndPack(type: String, pack: Int): Int

    @Transaction
    fun replaceQuestions(type: String, pack: Int, questions: List<Question>) {
        deleteByTypeAndPack(type, pack)
        insertAll(questions)
    }
}
