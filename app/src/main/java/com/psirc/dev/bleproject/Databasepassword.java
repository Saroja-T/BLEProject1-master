package com.psirc.dev.bleproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Databasepassword extends SQLiteOpenHelper {

    public Databasepassword(Context Context) {
        super(Context , "psassword.db", null,1);
    }

    public void onCreate(SQLiteDatabase db) {

        db.execSQL("Create table login(username text,passowrd text)");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean adddetails (String username,String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("username", username);
        contentValues.put("passowrd", password);


        long res= db.insert("login", null, contentValues);

        if(res==-1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }


    public Cursor getdetails() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res =  db.rawQuery( "select * from login ", null);
        return res;
    }



}
