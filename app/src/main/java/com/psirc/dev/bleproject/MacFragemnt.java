package com.psirc.dev.bleproject;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import static android.content.Context.MODE_PRIVATE;
import static com.psirc.dev.bleproject.LaunchActivity.appbar_layout;
import static com.psirc.dev.bleproject.LaunchActivity.llDeviceStatus;
import static com.psirc.dev.bleproject.LaunchActivity.llHome;
import static com.psirc.dev.bleproject.LaunchActivity.toolbar_title;
import static com.psirc.dev.bleproject.LaunchActivity.tvDeviceStatus;

import com.google.android.material.appbar.AppBarLayout;

public class MacFragemnt extends Fragment {

    EditText etMacAddress,etContactNumber1,etContactNumber2,etContactNumber3,etSmsContent;
    Button btnSave;
    String etMacAddressStr="",etContactNumber1Str="",etContactNumber2Str="",etContactNumber3Str="",etSmsContentStr="";
    private MyJobService myJobService;
    Intent mServiceIntent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = (View)inflater.inflate(R.layout.activity_mac_address, container, false);

        etMacAddress = (EditText) view.findViewById(R.id.etMacAddress);
        etContactNumber1 = (EditText) view.findViewById(R.id.etContactNumber1);
        etContactNumber2 = (EditText) view.findViewById(R.id.etContactNumber2);
        etContactNumber3 = (EditText) view.findViewById(R.id.etContactNumber3);
        etSmsContent = (EditText) view.findViewById(R.id.etSmsContent);
        btnSave = (Button) view.findViewById(R.id.btnSave);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validation();
            }
        });
        getmac();

        tvDeviceStatus.setVisibility(View.GONE);
        llDeviceStatus.setVisibility(View.GONE);
        appbar_layout.setVisibility(View.VISIBLE);
        AppBarLayout.LayoutParams params = new AppBarLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,200);
// Changes the height and width to the specified *pixels*
//        params.height = 50;
//        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        toolbar_title.setText(getResources().getString(R.string.app_name));
        appbar_layout.setLayoutParams(params);
        appbar_layout.setBackgroundColor(Color.TRANSPARENT);
        //llHome.setBackgroundColor(Color.GREEN);
        llHome.setBackgroundDrawable(getResources().getDrawable(R.drawable.tool_bg));

        myJobService = new MyJobService();
        mServiceIntent = new Intent(getContext(), myJobService.getClass());

        return view;
    }
    private void validation() {
        etMacAddressStr = etMacAddress.getText().toString();
        etContactNumber1Str = etContactNumber1.getText().toString();
        etContactNumber2Str = etContactNumber2.getText().toString();
        etContactNumber3Str = etContactNumber3.getText().toString();
        etSmsContentStr = etSmsContent.getText().toString();
        if(!etMacAddressStr.isEmpty()){
            if(!etContactNumber1Str.isEmpty()){
                if(etContactNumber1Str.length()==10){
                    if(!etContactNumber2Str.isEmpty() || !etContactNumber3Str.isEmpty()){
                        if(!etContactNumber2Str.isEmpty()){
                            if(etContactNumber2Str.length()==10){
                                if(!etContactNumber3Str.isEmpty()){
                                    if(etContactNumber3Str.length()==10){
                                        if(!etSmsContentStr.isEmpty()){
                                            saveValuesToSharedPrefernce();
                                        }else{
                                            etSmsContent.setError("Please enter the SMS Content");
                                        }
                                    }else{
                                        etContactNumber3.setError("Please enter valid contact number");
                                    }
                                }else{
                                    if(!etSmsContentStr.isEmpty()){
                                        saveValuesToSharedPrefernce();
                                    }else{
                                        etSmsContent.setError("Please enter the SMS Content");
                                    }                                }
                            }else{
                                etContactNumber2.setError("Please enter valid contact number");
                            }
                        }else if(!etContactNumber3Str.isEmpty()){
                            if(etContactNumber3Str.length()==10){
                                if(!etSmsContentStr.isEmpty()){
                                    saveValuesToSharedPrefernce();
                                }else{
                                    etSmsContent.setError("Please enter the SMS Content");
                                }                            }else{
                                etContactNumber3.setError("Please enter valid contact number");
                            }
                        }
                    }else{
                        saveValuesToSharedPrefernce();
                    }
                }else{
                    etContactNumber1.setError("Please enter valid contact number");
                }
            }else{
                etContactNumber1.setError("Please enter the contact number");
            }
        }else{
            etMacAddress.setError("Please enter the mac address");
        }
    }

    private void    saveValuesToSharedPrefernce() {
        getContext().stopService(mServiceIntent);
        SharedPreferences shared = getContext().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString("mac_address", etMacAddressStr);
        editor.commit();
        //  boolean tempMacAddress = SharedPreferencesManager.setMacAddress(MacAddressActivity.this,etMacAddressStr);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("+91"+etContactNumber1Str);
        if(!etContactNumber2Str.isEmpty()){
            jsonArray.put("+91"+etContactNumber2Str);
        }
        if(!etContactNumber3Str.isEmpty()){
            jsonArray.put("+91"+etContactNumber3Str);
        }

        SharedPreferences shared1 = getContext().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = shared1.edit();
        editor1.putString("contact_number", jsonArray.toString());
        editor1.commit();
        SharedPreferences sharedPref = getContext().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sharedPref.edit();
        editor2.putString("sms_content", etSmsContentStr);
        editor2.commit();

        if (!isMyServiceRunning(myJobService.getClass())) {
            getContext().startService(mServiceIntent);
        }
        //boolean tempContactNum = SharedPreferencesManager.setContactNumbers(MacAddressActivity.this,jsonArray.toString());

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

    public void getmac() {
        SharedPreferences preferences  = getContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        etMacAddressStr = preferences.getString("mac_address","");
        etSmsContentStr = preferences.getString("sms_content","");
        String contactNumbers = preferences.getString("contact_number","");

        Log.e("Print",etMacAddressStr);
        Log.e("Print",contactNumbers);
        etMacAddress.setText(etMacAddressStr);
        etSmsContent.setText(etSmsContentStr);

        if(contactNumbers!=null && contactNumbers!=""){
            try {
                JSONArray jsonArray = new JSONArray(contactNumbers);
                int length = jsonArray.length();
                if(length>0){
                    for (int i = 0;i<length;i++){
                        if(i==0){
                            etContactNumber1Str = String.valueOf(jsonArray.get(i)).substring(3,String.valueOf(jsonArray.get(i)).length());
                            etContactNumber1.setText(etContactNumber1Str);
                        }else if(i==1){
                            etContactNumber2Str = String.valueOf(jsonArray.get(i)).substring(3,String.valueOf(jsonArray.get(i)).length());
                            etContactNumber2.setText(etContactNumber2Str);
                        }else if(i==2){
                            etContactNumber3Str = String.valueOf(jsonArray.get(i)).substring(3,String.valueOf(jsonArray.get(i)).length());
                            etContactNumber3.setText(etContactNumber3Str);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }



}
