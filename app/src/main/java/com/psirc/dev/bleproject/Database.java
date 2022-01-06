package com.psirc.dev.bleproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database {

    public static final int VER = 1;
    public static String DB_NAME = "mydb.db";
    public static String TBL_NAME = "myrecord";
    public static String ITEM_ID = "_id";
    public static String ITEM_NAME = "item_name";
    public static String ITEM_PRICE = "item_price";
    public static String ITEM_QTY = "item_qty";
    public static String EXTRA = "_extra";
    public static String ID = "_id";
    public static String CONTACT_NAME = "contact_name";
    public static String MAC_ID = "mac_id";

    public static String CONTACT_PHONE = "contact_phone";
    public static String CORD_LAT = "_lat";
    public static String CORD_LNG = "_lng";
    public static String NEW_CONTACTS_TBL = "new_contacts_tbl";
    public static String CONTACTS_TBL = "_contacts_tbl";
    public static String MAC_TBL = "_mac_tbl";

    public static String CORDS_TBL = "_cords_tbl";

    public static String[] CONTACTS_PROJECTION = new String[]{ID,CONTACT_NAME,CONTACT_PHONE,EXTRA};
    public static String[] CORDS_PROJECTION = new String[]{ID,CORD_LAT,CORD_LNG,EXTRA};

    public static String CREATE_CONTACTS_SCHEMA = "CREATE TABLE "+CONTACTS_TBL+" ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+CONTACT_NAME+
            " TEXT NOT NULL,"+CONTACT_PHONE+" TEXT NOT NULL,"+EXTRA+" TEXT NOT NULL)";

   // public static String CREATE_MAC_SCHEMA = "CREATE TABLE "+MAC_TBL+"("+MAC_ID+" TEXT,)";


    public static String CREATE_CORDS_SCHEMA = "CREATE TABLE "+CORDS_TBL+" ("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+CORD_LAT+
            " DOUBLE NOT NULL,"+CORD_LNG+" DOUBLE NOT NULL,"+EXTRA+" TEXT NOT NULL)";

    public static String DROP_CONTACTS = "DROP TABLE IF EXISTS "+CONTACTS_TBL;
    public static String DROP_CORDS = "DROP TABLE IF EXISTS "+CORDS_TBL;

    private SQLiteDatabase sqLiteDatabase;
    private DbHelper dbHelper;

    public Database(Context context){
        dbHelper = new DbHelper(context);
    }

    public void openDb(){
        sqLiteDatabase = dbHelper.getWritableDatabase();
    }

    public void closeDb(){
        sqLiteDatabase.close();
        sqLiteDatabase = null;
    }

    public long addContact(String name,String phone, String extra){
        Cursor cursor = getContactByPhone(phone);
        long id = -1;
        if (cursor.getCount() == 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CONTACT_NAME, name);
            contentValues.put(CONTACT_PHONE, phone);
            contentValues.put(EXTRA, extra);
            id = sqLiteDatabase.insert(CONTACTS_TBL, null, contentValues);
        }
        cursor.close();
        return id;
    }


//    public long addContact(String name,String phone, String extra){
//        Cursor cursor = getContactByPhone(phone);
//        long id = -1;
//        if (cursor.getCount() == 0) {
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(CONTACT_NAME, name);
//            contentValues.put(CONTACT_PHONE, phone);
//            contentValues.put(EXTRA, extra);
//            id = sqLiteDatabase.insert(NEW_CONTACTS_TBL, null, contentValues);
//        }
//        cursor.close();
//        return id;
//    }




    public void addCords(Double lat,Double lng, String extra){
        ContentValues contentValues = new ContentValues();
        contentValues.put(CORD_LAT,lat);
        contentValues.put(CORD_LNG,lng);
        contentValues.put(EXTRA,extra);
        sqLiteDatabase.insert(CORDS_TBL,null,contentValues);
    }

    public Cursor getContactByPhone(String phone){
        Cursor cursor = sqLiteDatabase.query(CONTACTS_TBL,CONTACTS_PROJECTION,CONTACT_PHONE+ " = ?",new String[]{phone},null,null,null);
        return cursor;
    }

    public Cursor getContacts(){
        Cursor cursor = sqLiteDatabase.query(CONTACTS_TBL,CONTACTS_PROJECTION,null,null,null,null,null);
        return cursor;
    }

    public Cursor getCords(){
        Cursor cursor = sqLiteDatabase.query(CORDS_TBL,CORDS_PROJECTION,null,null,null,null,null);
        return cursor;
    }

    public void deleteContactByPhone(String phone){
        sqLiteDatabase.delete(CONTACTS_TBL,CONTACT_PHONE+ " = ?", new String[]{phone});
    }

    public void deleteContactById(long contact_id){
        sqLiteDatabase.delete(CONTACTS_TBL,ID+ " = ?", new String[]{Long.toString(contact_id)});
    }

    public void deleteCords(){
        sqLiteDatabase.delete(CORDS_TBL,null, null);
    }
/*
    public void updateItem(long itemId,String itemName,String itemPrice, String itemQty){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ITEM_NAME,itemName);
        contentValues.put(ITEM_PRICE,itemPrice);
        contentValues.put(ITEM_QTY,itemQty);
        sqLiteDatabase.update(TBL_NAME,contentValues,ITEM_ID+ " = ?",new String[]{Long.toString(itemId)});
    }

    */

    class DbHelper extends SQLiteOpenHelper{

        public DbHelper(Context context) {
            super(context, DB_NAME, null, VER);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_CONTACTS_SCHEMA);
            sqLiteDatabase.execSQL(CREATE_CORDS_SCHEMA);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(DROP_CONTACTS);
            sqLiteDatabase.execSQL(DROP_CORDS);
            onCreate(sqLiteDatabase);
        }
    }
}