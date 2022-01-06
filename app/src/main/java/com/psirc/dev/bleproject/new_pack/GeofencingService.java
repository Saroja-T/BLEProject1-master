package com.psirc.dev.bleproject.new_pack;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.psirc.dev.bleproject.Emerald;
import com.psirc.dev.bleproject.R;
import com.psirc.dev.bleproject.Restarter;
import com.psirc.dev.bleproject.new_pack.GeofenceHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.psirc.dev.bleproject.LaunchActivity.launchActivity;
import static java.lang.Thread.sleep;

public class GeofencingService extends JobIntentService {
    private static final String TAG = "GeofencingService";
    //  ArrayList<HashMap<String, String>> contacts = new ArrayList<HashMap<String, String>>();
    public int counter=0;
    int flag = 0;
    int alarmFlag = 0;
    //boolean isCalledUser=false;
    public String contactNumbers = "";
    JSONArray contactJsonArray;
    private Handler mHandler;
    public static MediaPlayer player;
   // private GeofenceHelper geofenceHelper;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";
    PendingIntent pendingIntent;
    public boolean smsflag = false;
    private GeofencingClient geofencingClient;


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        player = MediaPlayer.create(this, R.raw.audio);
        player.setLooping(true); // Set looping
        player.setVolume(100, 100);
        //initialize();

    geofencingClient = LocationServices.getGeofencingClient(launchActivity);
//        geofenceHelper = new GeofenceHelper(launchActivity);
        startTimer();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stoptimertask();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
        /*Intent broadcastIntent1 = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent1.setClass(this, GeofenceBroadcastReceiver.class);
        this.sendBroadcast(broadcastIntent1);*/

    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d(TAG, "onReceive: "+"Gofence Triggered");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if(geofencingEvent.hasError()){
            Log.d(TAG, "onReceive: Error receiving geofence event");
            return;
        }
        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
//        Location location = geofencingEvent.getTriggeringLocation();

        for (Geofence geofence:geofenceList){
            Log.d(TAG, "onReceive: "+geofence.getRequestId());
        }
        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType){
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.d(TAG, "onReceive: "+"GEOFENCE_TRANSITION_ENTER");
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(TAG, "onReceive: "+"GEOFENCE_TRANSITION_DWELL");
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                SharedPreferences shared = launchActivity.getSharedPreferences("MySharedPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = shared.edit();
                editor.putString("geofence_status", "exit");
                editor.commit();
                Log.d(TAG, "onReceive: "+"GEOFENCE_TRANSITION_EXIT");
                break;
        }
    }



    private Timer timer;
    private TimerTask timerTask;
    public void startTimer() {
        timer = new Timer();
        mHandler = new Handler();
        timerTask = new TimerTask() {
            public void run() {
                SharedPreferences preferences  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
                String macAddress = preferences.getString("mac_address","");
                contactNumbers = preferences.getString("contact_number","");
                String latitude = preferences.getString("latitude","");
                String longitude = preferences.getString("longitude","");
                String geofence_latitude = preferences.getString("geofence_latitude","");
                String geofence_longitude = preferences.getString("geofence_longitude","");
                String geofence_radius = preferences.getString("geofence_radius","");
                if(geofence_latitude!=null && geofence_longitude != null && geofence_radius!=null
                        && geofence_latitude!="" && geofence_longitude != "" && geofence_radius!=""){
                    LatLng latLng = new LatLng(Double.valueOf(geofence_latitude), Double.valueOf(geofence_longitude));

                    addGeofence(latLng, Float.parseFloat(geofence_radius));

                }

            }
        };
        timer.schedule(timerTask, 10000, 10000); //
    }
    void addGeofence(LatLng latLng, float radius) {
        Geofence geofence = getGeofence(GEOFENCE_ID, latLng, radius,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL |
                        Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = getGeofencingRequest(geofence);
        PendingIntent pendingIntent = getPendingIntent();
        if (ActivityCompat.checkSelfPermission(launchActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = getErrorString(e);
                        Log.d(TAG, "OnFailure" + errorMessage);
                    }
                });

    }
    public GeofencingRequest getGeofencingRequest(Geofence geofence){
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }
    public Geofence getGeofence(String ID, LatLng latLng, float radius, int transitionTypes){
        return  new Geofence.Builder()
                .setCircularRegion(latLng.latitude,latLng.longitude,radius)
                .setRequestId(ID)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(5000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }
    public PendingIntent getPendingIntent(){
        if(pendingIntent!=null){
            return pendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);

        pendingIntent = PendingIntent.getBroadcast(this,2607,intent
                ,PendingIntent.FLAG_UPDATE_CURRENT);
       /* Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        pendingIntent = PendingIntent
                .getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);*/
        return pendingIntent;
    }
    public String getErrorString(Exception e){
        if(e instanceof ApiException){
            ApiException apiException = (ApiException) e;
            switch (apiException.getStatusCode()){
                case GeofenceStatusCodes
                        .GEOFENCE_NOT_AVAILABLE:
                    return "GEOFENCE_NOT_AVAILABLE";
                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_GEOFENCES:
                    return "GEOFENCE_TOO_MANY_GEOFENCES";
                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "GEOFENCE_TOO_MANY_PENDING_INTENTS";
            }
        }
        return e.getLocalizedMessage();
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class GeofenceBroadcastReceiver1 extends BroadcastReceiver {

        private static final String TAG = "GeofenceBroadcastReceiv";
        @Override
        public void onReceive(Context context, Intent intent) {

            // TODO: This method is called when the BroadcastReceiver is receiving
            // an Intent broadcast.
            Toast.makeText(context, "Gofence Triggered", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onReceive: "+"Gofence Triggered");

            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if(geofencingEvent.hasError()){
                Log.d(TAG, "onReceive: Error receiving geofence event");
                return;
            }
            List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
//        Location location = geofencingEvent.getTriggeringLocation();

            for (Geofence geofence:geofenceList){
                Log.d(TAG, "onReceive: "+geofence.getRequestId());
            }
            int transitionType = geofencingEvent.getGeofenceTransition();

            switch (transitionType){
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onReceive: "+"GEOFENCE_TRANSITION_ENTER");

                    break;
                case Geofence.GEOFENCE_TRANSITION_DWELL:
                    Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onReceive: "+"GEOFENCE_TRANSITION_DWELL");

                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    SharedPreferences shared = context.getSharedPreferences("MySharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("geofence_status", "exit");
                    editor.commit();
                    Log.d(TAG, "onReceive: "+"GEOFENCE_TRANSITION_EXIT");
                   // startCallAndSMS();
                    break;
            }
        }

    }

}
