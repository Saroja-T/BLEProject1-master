package com.psirc.dev.bleproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Databasemacc extends SQLiteOpenHelper {

    public Databasemacc(Context Context) {
        super(Context , "macc.db", null,1);
    }


    public void onCreate(SQLiteDatabase db) {

        db.execSQL("Create table macidd(macc text)");


    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



    public boolean addmac (String maccid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("macc", maccid);

        long res= db.insert("macidd", null, contentValues);

        if(res==-1)
        {
            return false;
        }
        else
        {
            return true;
        }

    }



    public Cursor getmacid() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res =  db.rawQuery( "select * from macidd ", null);
        return res;
    }


}
