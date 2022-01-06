package com.psirc.dev.bleproject.new_pack;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class MyService extends JobIntentService {
    private static final String TAG = "MyService";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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