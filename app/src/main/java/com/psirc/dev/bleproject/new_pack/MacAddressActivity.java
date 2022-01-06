package com.psirc.dev.bleproject.new_pack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.psirc.dev.bleproject.LaunchActivity;
import com.psirc.dev.bleproject.R;

import org.json.JSONArray;

public class MacAddressActivity extends AppCompatActivity {
    EditText etMacAddress,etContactNumber1,etContactNumber2,etContactNumber3,etSmsContent;
    Button btnSave;
    String etMacAddressStr="",etContactNumber1Str="",etContactNumber2Str="",etContactNumber3Str="",
    etSmsContentStr="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mac_address);


        etMacAddress = (EditText) findViewById(R.id.etMacAddress);
        etContactNumber1 = (EditText) findViewById(R.id.etContactNumber1);
        etContactNumber2 = (EditText) findViewById(R.id.etContactNumber2);
        etContactNumber3 = (EditText) findViewById(R.id.etContactNumber3);
        etContactNumber3 = (EditText) findViewById(R.id.etContactNumber3);
        etSmsContent = (EditText) findViewById(R.id.etSmsContent);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validation();
            }
        });
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

    private void saveValuesToSharedPrefernce() {

        SharedPreferences shared = getSharedPreferences("MySharedPref", MODE_PRIVATE);
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

        SharedPreferences shared1 = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = shared1.edit();
        editor1.putString("contact_number", jsonArray.toString());
        editor1.commit();
        SharedPreferences sharedPref = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sharedPref.edit();
        editor2.putString("sms_content", etSmsContentStr);
        editor2.commit();
        //boolean tempContactNum = SharedPreferencesManager.setContactNumbers(MacAddressActivity.this,jsonArray.toString());


        Intent i = new Intent(getApplicationContext(), LaunchActivity.class);
        startActivity(i);
        finish();


    }
}