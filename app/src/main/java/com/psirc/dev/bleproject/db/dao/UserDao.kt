package com.psirc.dev.bleproject.db.dao

import androidx.room.*
import com.psirc.dev.bleproject.db.User


@Dao
interface UserDao {

    @get:Query("SELECT * FROM User ")
    val all: List<User>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(task: User): Long

    @Delete
    fun delete(task: User)

    @Update
    fun update(task: User)

    @Query("SELECT * FROM User where name =:fullname AND password = :pass")
    fun getUser(fullname: String, pass: String): User?


}