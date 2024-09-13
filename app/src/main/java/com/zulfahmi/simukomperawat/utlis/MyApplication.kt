package com.zulfahmi.simukomperawat.utlis

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MyApplication : Application() {
    companion object{
        private lateinit var application: MyApplication
        private lateinit var database: FirebaseDatabase

        fun getInstance() : MyApplication {
            return application
        }

        fun hideSoftInput(context: Context, editText: EditText): MyApplication {
            val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(editText.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
            return application
        }

        fun getFirebaseDatabaseReferences(Reference: String): DatabaseReference {
            return database.getReference(Reference)
        }
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        database = FirebaseDatabase.getInstance()
        database.setPersistenceEnabled(true)
    }

    fun getSharedPreferences(): SharedPreferences {
        return getSharedPreferences("user", Context.MODE_PRIVATE)
    }

}