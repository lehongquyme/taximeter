package com.teasoft.taxi.meter.databaseprice

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DataPrice:: class ], version = 1,  exportSchema = false)
abstract  class UserDatabase:RoomDatabase()  {
    abstract fun dataPrice(): DataPriceDAO
    companion object {
        @Volatile
        private  var  INSTANCE: UserDatabase?= null;
        private val LOCK = Any()
        operator  fun invoke(context: Context)= INSTANCE ?: synchronized(LOCK){
            INSTANCE?: getData(context).also {
                INSTANCE = it
            }
        }
        private fun getData(context: Context)=Room.databaseBuilder(
            context.applicationContext,
            UserDatabase::class.java,
            "user_database.db"
        ).build()

    }
}