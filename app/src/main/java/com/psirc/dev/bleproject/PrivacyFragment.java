package com.psirc.dev.bleproject;


import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import static com.psirc.dev.bleproject.LaunchActivity.appbar_layout;
import static com.psirc.dev.bleproject.LaunchActivity.llDeviceStatus;
import static com.psirc.dev.bleproject.LaunchActivity.llHome;
import static com.psirc.dev.bleproject.LaunchActivity.status;
import static com.psirc.dev.bleproject.LaunchActivity.toolbar_title;
import static com.psirc.dev.bleproject.LaunchActivity.tvDeviceStatus;

import com.google.android.material.appbar.AppBarLayout;

public class PrivacyFragment extends Fragment {
View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.settings_activity, container, false);
        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        /*ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }*/
        return view;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private static final String TAG = "PrivacyFragment";
        private Context context;
        public EmeraldService emd = new EmeraldService();
        private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            Log.d("TAG", "onPreferenceChange: String="+stringValue);


            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);


            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                Log.d("TAG", "onPreferenceChange: "+"else");

                preference.setSummary(stringValue);
                if (preference.getKey().equals("example_phone")) {
                    Log.d("TAG", "onPreferenceChange: "+"phone");
                    SharedPreferences shared1 = getContext().getSharedPreferences("MySharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = shared1.edit();
                    editor1.putBoolean("call", true);
                    editor1.commit();
                }

                if (preference.getKey().equals("example_auto")) {
                    if (stringValue.equals("true")) {
                        SharedPreferences pref = getContext().getApplicationContext().getSharedPreferences("EmeraldSP", 0); // 0 - for private mode
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("Sound", false);
                        editor.apply();
                        Log.d(">> Sound", "ON");
                        Log.d(">> Status", status);

                        if (status.equals("Disconnected")) {
                            Log.d(">> Handler PF", "running...");
                            AsyncTask.execute(runnable);
                        }
                    } else {
                        SharedPreferences pref = getContext().getApplicationContext().getSharedPreferences("EmeraldSP", 0); // 0 - for private mode
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("Sound", true);
                        editor.apply();
                        Log.d(">> SoundPrivacy", "OFF");
                        emd.stopsounddd();
                    }
                }
            }
            return true;
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            emd.startSounds();
        }
    };

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        Log.d("TAG", "bindPreferenceSummaryToValue: "+"bind");
        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getBoolean(preference.getKey(), true));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        context = getContext();
        setHasOptionsMenu(true);
        tvDeviceStatus.setVisibility(View.GONE);
        llDeviceStatus.setVisibility(View.GONE);
        toolbar_title.setText("Privacy");

        AppBarLayout.LayoutParams params = new AppBarLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,200);
// Changes the height and width to the specified *pixels*
//        params.height = 50;
//        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        appbar_layout.setLayoutParams(params);
        appbar_layout.setBackgroundColor(Color.TRANSPARENT);
        llHome.setBackgroundDrawable(getResources().getDrawable(R.drawable.tool_bg));

        //  bindPreferenceSummaryToValue(findPreference("example_auto"));
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            //setPreferencesFromResource(R.xml.pref_general, rootKey);
        }
    }
}

/**
 * A simple {@link Fragment} subclass.
 */
//public class PrivacyFragment extends PreferenceFragmentCompat {
//    private static final String TAG = "PrivacyFragment";
//    private Context context;
//    public EmeraldService emd = new EmeraldService();
//
//    public PrivacyFragment() {
//        // Required empty public constructor
//    }
//
//    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
//        @Override
//        public boolean onPreferenceChange(Preference preference, Object value) {
//            String stringValue = value.toString();
//
//            Log.d(TAG, "onPreferenceChange: String="+stringValue);
//
//
//            if (preference instanceof ListPreference) {
//                // For list preferences, look up the correct display value in
//                // the preference's 'entries' list.
//                ListPreference listPreference = (ListPreference) preference;
//                int index = listPreference.findIndexOfValue(stringValue);
//
//                // Set the summary to reflect the new value.
//                preference.setSummary(
//                        index >= 0
//                                ? listPreference.getEntries()[index]
//                                : null);
//
//
//            } else {
//                // For all other preferences, set the summary to the value's
//                // simple string representation.
//                Log.d(TAG, "onPreferenceChange: "+"else");
//
//                preference.setSummary(stringValue);
//                if (preference.getKey().equals("example_phone")) {
//                    Log.d(TAG, "onPreferenceChange: "+"phone");
//                    SharedPreferences shared1 = getContext().getSharedPreferences("MySharedPref", MODE_PRIVATE);
//                    SharedPreferences.Editor editor1 = shared1.edit();
//                    editor1.putBoolean("call", true);
//                    editor1.commit();
//                }
//
//                if (preference.getKey().equals("example_auto")) {
//                    if (stringValue.equals("true")) {
//                        SharedPreferences pref = getContext().getApplicationContext().getSharedPreferences("EmeraldSP", 0); // 0 - for private mode
//                        SharedPreferences.Editor editor = pref.edit();
//                        editor.putBoolean("Sound", false);
//                        editor.apply();
//                        Log.d(">> Sound", "ON");
//                        Log.d(">> Status", status);
//
//                        if (status.equals("Disconnected")) {
//                            Log.d(">> Handler PF", "running...");
//                            AsyncTask.execute(runnable);
//                        }
//                    } else {
//                        SharedPreferences pref = getContext().getApplicationContext().getSharedPreferences("EmeraldSP", 0); // 0 - for private mode
//                        SharedPreferences.Editor editor = pref.edit();
//                        editor.putBoolean("Sound", true);
//                        editor.apply();
//                        Log.d(">> SoundPrivacy", "OFF");
//                        emd.stopsounddd();
//                    }
//                }
//            }
//            return true;
//        }
//    };
//
//    Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            emd.startSounds();
//        }
//    };
//
//    private void bindPreferenceSummaryToValue(Preference preference) {
//        // Set the listener to watch for value changes.
//        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
//        Log.d(TAG, "bindPreferenceSummaryToValue: "+"bind");
//        // Trigger the listener immediately with the preference's
//        // current value.
//        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
//                PreferenceManager
//                        .getDefaultSharedPreferences(preference.getContext())
//                        .getBoolean(preference.getKey(), true));
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        addPreferencesFromResource(R.xml.pref_general);
//        context = getContext();
//        setHasOptionsMenu(true);
//        tvDeviceStatus.setVisibility(View.GONE);
//        llDeviceStatus.setVisibility(View.GONE);
//        AppBarLayout.LayoutParams params = new AppBarLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,200);
//// Changes the height and width to the specified *pixels*
////        params.height = 50;
////        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
//        appbar_layout.setLayoutParams(params);
//        appbar_layout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//      //  bindPreferenceSummaryToValue(findPreference("example_auto"));
//    }
//
//    @Override
//    public void onCreatePreferences(Bundle bundle, String s) {
//
//    }
//
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == android.R.id.home) {
//            startActivity(new Intent(getActivity(), SettingsActivity.class));
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//}
