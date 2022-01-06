package com.psirc.dev.bleproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.psirc.dev.bleproject.new_pack.GeofencingService;

public class Restarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "Service tried to stop");
        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //context.startForegroundService(new Intent(context, MyJobService.class));
            context.startForegroundService(new Intent(context, GeofencingService.class));
        } else {
            //context.startService(new Intent(context, MyJobService.class));
            context.startService(new Intent(context, GeofencingService.class));
        }
    }
}
