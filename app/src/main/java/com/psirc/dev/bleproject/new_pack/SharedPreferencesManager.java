package com.psirc.dev.bleproject.new_pack;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String APP_SETTINGS = "APP_SETTINGS";

    // propertis
    private static final String MAC_ADDRESS = "mac_address";
    private static final String CONTACT_NUMBER_JSON = "contact_number_json";
    // other properties...

    private SharedPreferencesManager() {
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
    }

    public static String getMacAddress(Context context) {
        return getSharedPreferences(context).getString(MAC_ADDRESS, null);
    }

    public static boolean setMacAddress(Context context, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(MAC_ADDRESS, newValue);
        editor.commit();
        return true;
    }

    public static String getContextNumbers(Context context) {
        return getSharedPreferences(context).getString(CONTACT_NUMBER_JSON, null);
    }

    public static boolean setContactNumbers(Context context, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(CONTACT_NUMBER_JSON, newValue);
        editor.commit();
        return true;
    }

    // other getters/setters
}
