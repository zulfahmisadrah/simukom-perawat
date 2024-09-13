package com.zulfahmi.simukomperawat.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zulfahmi.simukomperawat.model.Question

@Database(entities = [Question::class], exportSchema = false, version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ukomDao(): UkomDao

    companion object{
        private const val DB_NAME = "ukomperawat"

        @Volatile
        private var INSTANCE: AppDatabase? = null

//        fun getInstance(context: Context): AppDatabase? {
//            if (instance == null) {
//                synchronized(AppDatabase::class) {
//                    instance = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME).build()
//                }
//            }
//
//            return instance
//        }

        fun getDatabase(context: Context): AppDatabase{
            val tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, DB_NAME
                ).createFromAsset("databases/ukomperawat.db").build()
                INSTANCE = instance
                return instance
            }
        }
    }

}