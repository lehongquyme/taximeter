package com.teasoft.taxi.meter.databaseprice

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DataPriceDAO {

    @Insert
    fun insert(dataPrice: DataPrice)

    @Update
    fun update(dataPrice: DataPrice)

    @Delete
    fun delete(dataPrice: DataPrice)

    @Query("delete from dataPrice")
    fun deleteAllData()

    @Query("select * from dataPrice order by id desc")
    fun getAllData(): List<DataPrice>

}