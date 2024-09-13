package com.zulfahmi.simukomperawat.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.zulfahmi.simukomperawat.model.Question

@Dao
interface UkomDao {
    @Query("SELECT * FROM kumpulansoal WHERE jenis=:type AND paket=:pack")
    fun getSoal(type: String, pack: Int): LiveData<List<Question>>
}