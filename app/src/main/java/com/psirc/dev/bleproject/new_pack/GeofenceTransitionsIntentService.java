package com.psirc.dev.bleproject.new_pack;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.psirc.dev.bleproject.R;

import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {
    // ...
    private static final String TAG = "GeofenceTransitionsInte";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
           /* String errorMessage = getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);*/
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
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

            // Send notification and log the transition details.

        } else {
            // Log the error.
            Log.e(TAG, "Error");
        }
    }
}