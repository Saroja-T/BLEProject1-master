package com.psirc.dev.bleproject;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;


public class MyIntentService extends JobIntentService {
    private static final String TAG = "MyIntentService";

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

        if(geofenceList!=null){
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

                    Log.d(TAG, "onReceive: "+"GEOFENCE_TRANSITION_EXIT");
                    break;
            }
        }
    }
}