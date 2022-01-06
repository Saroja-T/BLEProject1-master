package com.psirc.dev.bleproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Emerald {
    public static final String ITEM_ID = "_item_id";
    public static final String ITEM_NAME = "_item_name";
    public static final String ITEM_TIME = "_item_time";
    public static final String ITEM_READ_NAME = "_item_read_name";
    public static final String ITEM_PHOTO = "_item_photo";
    public static final String ITEM_CONTACT = "_item_contact";
    public static final String ITEM_DISTANCE = "_item_distance";
    public static final String LOCATION_ACTION = "com.dev.location.Emerald";
    public static final String LAT = "_lat";
    public static final String LNG = "_lng";
    public static final String FENCE = "_fence";
    public static final String MESSAGE = "_msg";
    public static final String LOGIN = "_login";
    public static final String STATUS = "_status";
    public static final String LOGIN_OBJ = "_login_obj";
    public static final String BREACH = "_breach";
    public static final String MAC_ADDRESS = "_mac_address";
    public static final String BASE_URL = "http://teknosapps.com/securitymgrv2/index.php/";

    public static void setValue(Context context,String key, String value){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key,value);
        editor.commit();
    }

    public static String getValue(Context context, String key, String defaultValue){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key,defaultValue);
    }

    public static void setValue(Context context,String key, boolean value){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(key,value);
        editor.commit();
    }

    public static boolean getValue(Context context, String key, boolean defaultValue){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key,defaultValue);
    }

    public static void display(Context context,String text){
        Toast.makeText(context,text,Toast.LENGTH_LONG).show();
    }
    public static void setMacAddress(Context context,String key, String value){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key,value);
        editor.commit();
    }
    public static String getMacAddress(Context context, String key, String defaultValue){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key,defaultValue);
    }


}
