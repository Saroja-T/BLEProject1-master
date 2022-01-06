package com.psirc.dev.bleproject;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.psirc.dev.bleproject.db.DataBaseClient;
import com.psirc.dev.bleproject.db.User;
import com.psirc.dev.bleproject.new_pack.MacAddressActivity;
import com.psirc.dev.bleproject.new_pack.SharedPreferencesManager;

import java.util.Locale;

public class BaseLoginActivity extends AppCompatActivity implements LocationListener {

    private EditText userNameText;
    private EditText userPassword;
    static BaseLoginActivity baseLoginActivity;

    Context mContext;
    String usernamee, passwordd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_login_n);
        baseLoginActivity = this;
        mContext = this;
        userNameText = (EditText) findViewById(R.id.email);
        userPassword = (EditText) findViewById(R.id.password);
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle("Login");
        getdetailss();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            checkPermission();
        }
      //  locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    private void checkPermission() {
        Log.d("TAG", "checkPermission: ");
        ActivityCompat.requestPermissions(BaseLoginActivity.this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
               // Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.PROCESS_OUTGOING_CALLS
        }, 121);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                if ("xiaomi".equals(Build.MANUFACTURER.toLowerCase(Locale.ROOT))) {
                    final Intent intent =new Intent("miui.intent.action.APP_PERM_EDITOR");
                    intent.setClassName("com.miui.securitycenter",
                            "com.miui.permcenter.permissions.PermissionsEditorActivity");
                    intent.putExtra("extra_pkgname", getPackageName());
                    new AlertDialog.Builder(this,R.style.MyDialogTheme)
                            .setTitle("Please enable the additional permissions")
                            .setMessage("You will not receive notifications while the app is in background if you disable these permissions")
                            .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(intent);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setCancelable(false)
                            .show();
                }else {

                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                    intent.setData(uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    Intent overlaySettings = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(overlaySettings, 101);
                }
            }
        }
        /*if (checkDrawOverlayPermission()) {

        }*/
    }

    public boolean checkDrawOverlayPermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 101);
            return false;
        } else {
            return true;
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (Settings.canDrawOverlays(this)) {
            }
        }
    }

    private void getUser(final String name, final String password) {
        class getUserData extends AsyncTask<Void, Void, User> {

            @Override
            protected User doInBackground(Void... voids) {
              /*  DataBaseClient.getInstance(mContext).getAppDatabase()
                        .taskDao()
                        .getGetUser();*/
                return DataBaseClient.getInstance(mContext).getAppDatabase()
                        .taskDao()
                        .getUser(name, password);
            }

            @Override
            protected void onPostExecute(User user) {
                super.onPostExecute(user);

                    Log.e("values","if");
                    if (user != null) {
                        SharedPreferences shared1 = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                        SharedPreferences.Editor editor1 = shared1.edit();
                        editor1.putBoolean("isLogin",true);
                        editor1.commit();
                        SharedPreferences preferences  = baseLoginActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
                        String macAddress = preferences.getString("mac_address","")!=null?preferences.getString("mac_address",""):"";
                        Log.e("maccccc",macAddress);
                        if(macAddress!=""&&macAddress!=null){
                            Intent intent = new Intent(BaseLoginActivity.this, LaunchActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Log.e("values","if");
                            Intent i = new Intent(getApplicationContext(), MacAddressActivity.class);
                            startActivity(i);
                            finish();
                        }

                    } else {
                        Toast.makeText(mContext, "Login Failed", Toast.LENGTH_SHORT).show();
                    }


            }
        }

        new getUserData().execute();
    }

    public void onLoginPressed(View view) {

        getUser(userNameText.getText().toString(), userPassword.getText().toString());

     /*   getdetailss();

        Log.w("LOGIN","login");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_name",userNameText.getText().toString());
            jsonObject.put("password",userPassword.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Emerald.setValue(BaseLoginActivity.this,Emerald.LOGIN,true);
        Emerald.setValue(BaseLoginActivity.this,Emerald.LOGIN_OBJ,"user");//resultObject.getJSONObject("user").toString());
//            Emerald.setValue(BaseLoginActivity.this,Emerald.LOGIN,true);
//          Emerald.setValue(BaseLoginActivity.this,Emerald.LOGIN_OBJ,resultObject.getJSONObject("user").toString());

        //if(userNameText.getText().toString().equals( ))


        if(userNameText.getText().toString().equals(usernamee)&&userPassword.getText().toString().equals(passwordd))
        {
            Intent intent = new Intent(BaseLoginActivity.this,LaunchActivity.class);
            startActivity(intent);
            finish();
            Emerald.display(BaseLoginActivity.this,"Successfully logged in");
        }
        else
        {
            Emerald.display(BaseLoginActivity.this,"Enter valid user name and password");
        }




//        ServerConnect serverConnect = new ServerConnect(this,jsonObject,null) {
//            @Override
//            public void getResult(String result, RecyclerView.ViewHolder view) {
//                Log.w("RE",result);
//                JSONObject resultObject = null;
//                try {
//                    resultObject = new JSONObject(result);
//                    if (resultObject.getString("status").equals("OK")){
//                        Emerald.setValue(BaseLoginActivity.this,Emerald.LOGIN,true);
//                        Emerald.setValue(BaseLoginActivity.this,Emerald.LOGIN_OBJ,resultObject.getJSONObject("user").toString());
//                        Intent intent = new Intent(BaseLoginActivity.this,LaunchActivity.class);
//                        startActivity(intent);
//                        finish();
//                        Emerald.display(BaseLoginActivity.this,"Successfully logged in");
//                    }else{
//                        Emerald.display(BaseLoginActivity.this,"Login Failed");
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        };
//
////        serverConnect.executeJsonPayLoad(Emerald.BASE_URL+"devices/check_device");
//
//        serverConnect.executeJsonPayLoad(Emerald.BASE_URL+"devices/check_device");
*/
    }

    public void forgetpassord(View view) {
        Intent intent = new Intent(BaseLoginActivity.this, PasswordChange.class);
        startActivity(intent);
        finish();

    }


    public void getdetailss() {
        Databasepassword database = new Databasepassword(this);
        Cursor cursor = database.getdetails();

        if (cursor.getCount() == 0) {
            //  Emerald.display(BaseLoginActivity.this,"Not");
            boolean i = database.adddetails("testuser", "123");

        } else {
            while (cursor.moveToNext()) {
                usernamee = cursor.getString(0);
                passwordd = cursor.getString(1);
                // Emerald.display(BaseLoginActivity.this,usernamee+passwordd);

            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
