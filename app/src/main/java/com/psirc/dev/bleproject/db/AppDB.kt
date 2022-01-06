package com.psirc.dev.bleproject.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.psirc.dev.bleproject.db.dao.UserDao


@Database(entities = arrayOf(User::class), version = 4)
public abstract class AppDB : RoomDatabase() {

    abstract fun taskDao(): UserDao?
}