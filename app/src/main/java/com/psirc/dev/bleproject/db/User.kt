package com.psirc.dev.bleproject.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class User : Serializable {

    @PrimaryKey(autoGenerate = false)
    var id = 0

    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "email")
    var email: String? = null
    @ColumnInfo(name = "password")
    var password: String? = null
    @ColumnInfo(name = "address")
    var address: String? = null


    @ColumnInfo(name = "gender")
    var gender: String? = null

    @ColumnInfo(name = "travel")
    var travel: String? = "0"

    @ColumnInfo(name = "music")
    var music: String? = "0"

    @ColumnInfo(name = "movie")
    var movie: String? = "0"

    @ColumnInfo(name = "other")
    var other: String? = "0"

    @ColumnInfo(name = "profilepic")
    var profilepic: String? = null
}