package com.psirc.dev.bleproject.new_pack;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.psirc.dev.bleproject.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.psirc.dev.bleproject.LaunchActivity.launchActivity;
import static java.lang.Thread.sleep;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiv";
    public static MediaPlayer player;
    JSONArray contactJsonArray;
    int flag = 0;
    public String contactNumbers = "";


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

        if(geofenceList!=null){
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
                    Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                    player = MediaPlayer.create(context, R.raw.audio);
                    player.setLooping(true); // Set looping
                    player.setVolume(100, 100);
                    startSound();
                    break;
            }
        }

    }

    public void startSound() {
        Log.d(">> Player ", "Start Sound ");
        try {
            //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            //  preferences.getString(Emerald.MAC_ADDRESS,"");
            //Log.d(">> xkcx ", preferences.getString(Emerald.MAC_ADDRESS,""));
            //SharedPreferences pref = getApplicationContext().getSharedPreferences("EmeraldSP", 0); // 0 - for private mode
            // boolean sound = pref.getBoolean("Sound", false);

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

        if(!isCalledUser){
            SharedPreferences shared1 = launchActivity.getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor editor1 = shared1.edit();
            editor1.putBoolean("isCalled",true);
            editor1.commit();
            Log.e("gghh", String.valueOf(isCalledUser));

            try {
                if(contactNumbers!="" && contactNumbers!=null){
                    Log.e("contact",contactNumbers);
                    Log.e("gdfdghf","fdgf");
                    contactJsonArray = new JSONArray(contactNumbers);
               /* for (int i = 0; i < contactJsonArray.length(); i++) {
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put(Emerald.ITEM_NAME, "test");
                    item.put(Emerald.ITEM_CONTACT, contactJsonArray.get(i).toString());
                    contacts.add(item);
                }*/
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
            /*if (Emerald.getValue(this, "example_sms", true) &&
                    Emerald.getValue(this, "example_location", true)) {
                text = message + " " + location;
            } else if (Emerald.getValue(this, "example_sms", true)) {
                text = message;
            } else if (Emerald.getValue(this, "example_location", true)) {
                text = location;
            }*/

            text = message+" "+location;

            if (!text.equals("")) {
                SmsManager smsManager = SmsManager.getDefault();
                for (int i = 0; i < contactJsonArray.length(); i++) {
                    try {
                        Log.e("sms123",String.valueOf(contactJsonArray.get(i)));
                        smsManager.sendTextMessage(String.valueOf(contactJsonArray.get(i)), null, text, null, null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //   smsManager.sendTextMessage("+919600672742", null, text, null, null);
                }

            }

            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            call();
        }
    }


    public void call() {

       /* if (Emerald.getValue(this, "example_phone", true)) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            //intent.setData(Uri.parse("tel:" + contacts.get(i).get(Emerald.ITEM_CONTACT)));
            intent.setData(Uri.parse("tel:" + "+919600672742"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                //
                // ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public void onRequestPermissionsResult(int requestCode, String[] permissions,
                // int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivity(intent);


        }*/

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
            //intent.setData(Uri.parse("tel:" + "919600672742"));



        /*int con_length = contacts.size();

        for (int i = 0; i < con_length; i++) {
            Log.e("callllllllllllllll", "callllllllllllllll" + con_length);

            if (Emerald.getValue(this, "example_phone", true)) {

                Intent intent = new Intent(Intent.ACTION_CALL);
                //intent.setData(Uri.parse("tel:" + con tacts.get(i).get(Emerald.ITEM_CONTACT)));
                intent.setData(Uri.parse("tel:" + "919600672742"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    //
                    // ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    // public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    // int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(intent);
            }*/


            try {
                sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        contacts.clear();
    }

}