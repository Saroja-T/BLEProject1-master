package com.psirc.dev.bleproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.psirc.dev.bleproject.db.DataBaseClient;
import com.psirc.dev.bleproject.db.User;
import com.psirc.dev.bleproject.new_pack.MacAddressActivity;
import com.psirc.dev.bleproject.new_pack.SharedPreferencesManager;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {

    Context mContext;
    static SplashActivity splashActivity;
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            Handler handler = new Handler();
            handler.postDelayed(loadActivity, 2000);
        }
    };

    private final Runnable loadActivity = new Runnable() {
        @Override
        public void run() {

            SharedPreferences preferences  = splashActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
            String macAddress = preferences.getString("mac_address","");
            String contactNumbers = preferences.getString("contact_number","");
            Boolean isLogin = preferences.getBoolean("isLogin",false);

            Log.e("Print",macAddress);
            Log.e("Print",contactNumbers);
            Log.e("Print",isLogin.toString());
            Log.e("Print", String.valueOf(Emerald.getValue(SplashActivity.this, Emerald.LOGIN, false)));
            if (!isLogin) {
                SharedPreferences shared1 = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                SharedPreferences.Editor editor1 = shared1.edit();
                editor1.putBoolean("isCalled",false);
                editor1.commit();
                Intent intent = new Intent(SplashActivity.this, BaseLoginActivity.class);
                startActivity(intent);
                finish();
            }else {
               // SharedPreferences preferences  = splashActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
               // String macAddress = preferences.getString("mac_address","")!=null?preferences.getString("mac_address",""):"";
                if(macAddress!=""&&macAddress!=null){
                    Intent intent = new Intent(SplashActivity.this, LaunchActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Log.e("values","if");
                    Intent i = new Intent(getApplicationContext(), MacAddressActivity.class);
                    startActivity(i);
                    finish();
                }
            }

        }
    };

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            // mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
splashActivity = this;
        setContentView(R.layout.activity_splash);
        mContext = this;
        mContentView = (ImageView) findViewById(R.id.fullscreen_content);
        getUser();

    }


    private void getUser() {
        class getUserData extends AsyncTask<Void, Void, List<User>> {

            @Override
            protected List<User> doInBackground(Void... voids) {
              /*  DataBaseClient.getInstance(mContext).getAppDatabase()
                        .taskDao()
                        .getGetUser();*/
                List<User> list = DataBaseClient.getInstance(mContext).getAppDatabase()
                        .taskDao()
                        .getAll();

                if (list == null || list.size() == 0) {
                    User aUser = new User();
                    aUser.setName("testuser");
                    aUser.setEmail("");
                    aUser.setPassword("123");
                    aUser.setGender("1");
                    aUser.setAddress("");

                    aUser.setMovie("0");
                    aUser.setMusic("0");
                    aUser.setTravel("0");
                    aUser.setOther("0");
                    aUser.setProfilepic("");
                    DataBaseClient.getInstance(mContext).getAppDatabase()
                            .taskDao()
                            .insert(aUser);
                }
                return list;
            }

            @Override
            protected void onPostExecute(List<User> user) {
                super.onPostExecute(user);

            }
        }

        new getUserData().execute();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }


        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }
//                        Emerald.setValue(BaseLoginActivity.this, Emerald.LOGIN, true);
//                        Toast.makeText(mContext, "Login Success", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(BaseLoginActivity.this, LaunchActivity.class);
//                        startActivity(intent);
//                        finish();

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
