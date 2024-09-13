package com.zulfahmi.simukomperawat.database

import android.app.Application
import androidx.lifecycle.LiveData
import com.zulfahmi.simukomperawat.model.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class UkomRepository(application: Application){
    private val ukomDao: UkomDao?

    init{
        val db = AppDatabase.getDatabase(application.applicationContext)
        ukomDao = db.ukomDao()
    }

    fun getSoal(type: String, pack: Int) : LiveData<List<Question>>? {
        return ukomDao?.getSoal(type, pack)
    }

}