package com.psirc.dev.bleproject;

import android.view.View;

import java.util.HashMap;

public interface DeviceClickListener {
     void onDeviceClicked(HashMap<String,String> device);
     void onDeviceStatusChanged(View view);

}
