package com.zulfahmi.simukomperawat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.zulfahmi.simukomperawat.database.UkomRepository
import com.zulfahmi.simukomperawat.model.Question

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val ukomRepository = UkomRepository(application)

    fun getSoal(type: String, pack: Int): LiveData<List<Question>>? {
        return ukomRepository.getSoal(type, pack)
    }
}