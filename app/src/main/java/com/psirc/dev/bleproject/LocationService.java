package com.psirc.dev.bleproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.psirc.dev.bleproject.LaunchActivity.launchActivity;
import static java.lang.Thread.sleep;

public class LocationService extends Service implements LocationListener {

    private static final String TAG = "LocationService";
    ArrayList<HashMap<String, String>> contacts = new ArrayList<HashMap<String, String>>();
    int flag=0;

    private ArrayList<HashMap<String,Double>> locations = new ArrayList<HashMap<String,Double>>();
    private Double[] vertices_y = new Double[0];
    private Double[] vertices_x = new Double[0];
    private LocationManager locationManager;
    private MediaPlayer player;
    private boolean breach = false;
    JSONArray contactJsonArray;
    public String contactNumbers = "";
    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("LOC","No Permission");
            stopSelf();

        }else{
            Log.d(TAG, "onCreate: LocationService calling");
            getFence();
//            locationManager.requestLocationUpdates(
//                    LocationManager.GPS_PROVIDER, 1000, 1, this);
//            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

//        player = MediaPlayer.create(this, R.raw.siren);
//        player.setLooping(true); // Set looping
//        player.setVolume(100,100);
        SharedPreferences preferences  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        Boolean islocationSharingEnbled = preferences.getString("location_sharing","")!=null &&
                preferences.getString("location_sharing","").equals("on")?true:false;
        Log.d(TAG, "onCreateLoc: "+preferences.getString("location_sharing",""));
        Log.d(TAG, "onCreateLoc: "+(preferences.getString("location_sharing","").equals("on")));
        Log.d(TAG, "onCreate: "+(preferences.getString("location_sharing","")!=null));
        Log.d(TAG, "onCreate: "+( preferences.getString("location_sharing","")!=null && preferences.getString("location_sharing","").equals("on")));
        Log.d(TAG, "onCreate:islocationSharingEnbled "+islocationSharingEnbled);


        if(islocationSharingEnbled){
            Log.d(TAG, "onCreate: "+preferences.getString("location_sharing_minutes",""));
            startTimer(preferences.getString("location_sharing_minutes",""));
        }
    }

    private Timer timer;
    private TimerTask timerTask;
    public void startTimer(String location_sharing_minutes) {
        timer = new Timer();
        timerTask = new TimerTask() {
            @SuppressLint("ResourceAsColor")
            public void run() {
                //Date currentTime = Calendar.getInstance().getTime();
               // Log.e("eveve","print"+currentTime.toString());
                startCallandSMS();
            }
        };
        timer.schedule(timerTask, Long.parseLong(location_sharing_minutes)*60000, Long.parseLong(location_sharing_minutes)*60000); //
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(
                Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player.release();
        }
       // unregisterReceiver(myReceiver);
        locationManager.removeUpdates(this);
    }

    public void startSound() {
        Log.d(">> Player ", "Start Sound ");
        try {
            if(player != null) {
                if (!player.isPlaying()) {
                    try {
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    player.start();
                }
            }
            startCallandSMS();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCallandSMS() {
        flag = 1;
        SharedPreferences preferences  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        Boolean isCalledUser = preferences.getBoolean("isCalled",false);
        Log.e("gghh", String.valueOf(isCalledUser));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(launchActivity);
        Boolean example_phone =  sharedPreferences.getBoolean("example_phone",true);

            SharedPreferences shared1 = launchActivity.getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor editor1 = shared1.edit();
            editor1.putBoolean("isCalled",true);
            editor1.commit();
            Log.e("gghh", String.valueOf(isCalledUser));

            try {
                if(contactNumbers!="" && contactNumbers!=null){
                    Log.e("contact",contactNumbers);
                    contactJsonArray = new JSONArray(contactNumbers);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            String latitude = preferences.getString("latitude","0.0");
            String longitude = preferences.getString("longitude","0.0");
            String message = preferences.getString("sms_content","0.0");
            Log.d(TAG, "run: Latitude :" +latitude);
            Log.d(TAG, "run: Longitude :" +longitude);
            String location = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
            String text = "";

            if (sharedPreferences.getBoolean("example_sms", true) &&
                    sharedPreferences.getBoolean("example_location", true)) {
                text = message + " " + location;
            } else if (sharedPreferences.getBoolean("example_sms", true)) {
                text = message;
            } else if (sharedPreferences.getBoolean("example_location", true)) {
                text = location;
            }

            if (!text.equals("")) {

                SmsManager smsManager = SmsManager.getDefault();
                if (contactJsonArray!=null){
                    for (int i = 0; i < contactJsonArray.length(); i++) {
                        try {
                            Log.e("sms123",String.valueOf(contactJsonArray.get(i)));
                            smsManager.sendTextMessage(String.valueOf(contactJsonArray.get(i)), null, text, null, null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
           //    call(example_phone);

    }


    public void call(Boolean example_phone) {
        if(example_phone){
            if (contactJsonArray!=null){
                int con_length = contactJsonArray.length();

                for (int i = 0; i < con_length; i++) {
                    Log.e("callllllllllllllll", "callllllllllllllll" + con_length);

                    Intent intent = new Intent(Intent.ACTION_CALL);
                    try {
                        Log.e("call123",String.valueOf(contactJsonArray.get(i)));
                        intent.setData(Uri.parse("tel:" + contactJsonArray.get(i)));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                        if (ActivityCompat.checkSelfPermission(launchActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        launchActivity.startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }


    }

    public void stopsounddd() {
        Log.d(">> Player ", "Sound Stopped");
        try {
            if (player.isPlaying()) {
                player.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getFence(){
        Database database = new Database(this);
        database.openDb();
        Cursor cursor = database.getCords();
        cursor.moveToFirst();
        locations = new ArrayList<HashMap<String,Double>>();
        vertices_x = new Double[cursor.getCount()];
        vertices_y = new Double[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            HashMap<String,Double> hashMap = new HashMap<String, Double>();
            hashMap.put(Emerald.LAT,cursor.getDouble(cursor.getColumnIndex(Database.CORD_LAT)));
            hashMap.put(Emerald.LNG,cursor.getDouble(cursor.getColumnIndex(Database.CORD_LNG)));
            vertices_y[i] = cursor.getDouble(cursor.getColumnIndex(Database.CORD_LAT));
            vertices_x[i] = cursor.getDouble(cursor.getColumnIndex(Database.CORD_LNG));
            locations.add(hashMap);
            cursor.moveToNext();
        }
        cursor.close();
        database.closeDb();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v(TAG, "IN ON LOCATION CHANGE, lat=" + location.getLatitude() + ", lon=" + location.getLongitude());
        if (location != null){

            double lon = location.getLongitude(); // x
            double lat = location.getLatitude(); //
            // y
            SharedPreferences shared1 = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor editor1 = shared1.edit();
            editor1.putString("latitude", "10.96089991");
            editor1.putString("longitude", "76.9149774");
//            editor1.putString("latitude", String.valueOf(location.getLatitude()));
//            editor1.putString("longitude", String.valueOf(location.getLongitude()));
            editor1.putString("fence", String.valueOf(flag));
            editor1.commit();

            Log.d(TAG, "onLocationChanged: "+shared1.getString("latitude",""));
            Log.d(TAG, "onLocationChanged: "+shared1.getString("longitude",""));

            float[] distance = new float[2];

            String geofence_latitude = shared1.getString("geofence_latitude","");
            String geofence_longitude = shared1.getString("geofence_longitude","");
            String geofence_radius = shared1.getString("geofence_radius","");
            contactNumbers = shared1.getString("contact_number","");


            if(geofence_latitude!=null && geofence_longitude != null && geofence_radius!=null
                    && geofence_latitude!="" && geofence_longitude != "" && geofence_radius!=""){
                LatLng latLng = new LatLng(Double.valueOf(geofence_latitude), Double.valueOf(geofence_longitude));
                Location.distanceBetween( location.getLatitude(), location.getLongitude(),
                        Double.valueOf(geofence_latitude), Double.valueOf(geofence_longitude), distance);
                if( distance[0] > Float.parseFloat(geofence_radius)  ){
                   // Toast.makeText(getBaseContext(), "Outside, distance from center: " + distance[0] + " radius: " + mCircle.getRadius(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onLocationChanged: Outside"+distance[0] + " radius: " + geofence_radius);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(launchActivity);
                    Boolean example_geofence =  sharedPreferences.getBoolean("example_geofence",true);
                    if(example_geofence){
                       // startSound();
                       // startCallandSMS();
                    }
                } else {
//                    Toast.makeText(getBaseContext(), "Inside, distance from center: " + distance[0] + " radius: " + mCircle.getRadius() , Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onLocationChanged: Inside"+distance[0] + " radius: " + geofence_radius);
                    stopsounddd();

                }
            }

        }

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
