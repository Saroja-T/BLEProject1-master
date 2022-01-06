package com.psirc.dev.bleproject;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class OutgoingCallReceiver extends BroadcastReceiver {

    boolean hasCallStateRinging = false;
    private ArrayList<HashMap<String, String>> contacts = new ArrayList<HashMap<String, String>>();
    private Context context;
    private int num = 1;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("Intent", intent.toString());
        if(intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            Log.e("aaaaaaaaaaaaaaaa", "out going Call");
            Log.e("CAll-STATUSss", "out going Call-->"+Emerald.getValue(context,Emerald.BREACH,false));
            this.context = context;
            if (Emerald.getValue(context,Emerald.BREACH,false))
            {
                Database database = new Database(context);
                database.openDb();
                Cursor cursor = database.getContacts();
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put(Emerald.ITEM_NAME, cursor.getString(cursor.getColumnIndex(Database.CONTACT_NAME)));
                    item.put(Emerald.ITEM_CONTACT, cursor.getString(cursor.getColumnIndex(Database.CONTACT_PHONE)));
                    contacts.add(item);
                    cursor.moveToNext();
                }
                cursor.close();
                database.closeDb();
            }
        }

        //        Checking for the call status
        try {
            // TELEPHONY MANAGER class object to register one listner
            TelephonyManager tmgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //Create Listner
            MyPhoneStateListener PhoneListener = new MyPhoneStateListener();
            // Register listener for LISTEN_CALL_STATE
            tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    if (hasCallStateRinging)
                        return;
                    else {
                        hasCallStateRinging = true;
                        Log.e("CAll-STATUSss", "CALL_STATE_RINGING");
                         }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if(!hasCallStateRinging) {
                        hasCallStateRinging = true;
                        Log.e( "CAll-STATUSss", "CALL_STATE_OFFHOOK" );
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (hasCallStateRinging) {
                        hasCallStateRinging = false;
                        Log.e("CAll-STATUSsssss", "CALL_STATE_IDLE");
                        if (Emerald.getValue(context,Emerald.BREACH,false)) {
                           //sendCall(context);
                        }
                           }
                    break;
            }
        }
    }

   // @SuppressLint("MissingPermission")
//    private void sendCall(Context context)
//    {
//        Log.e("aaaaaaaaaaa","call  "+num);
//
//
//        if (Emerald.getValue(context, "example_phone", true)) {
//
//            if ((contacts.size()-1) > num) {
//                Intent intent = new Intent(Intent.ACTION_CALL);
//                intent.setData(Uri.parse("tel:" + contacts.get(num).get(Emerald.ITEM_CONTACT)));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
//                context.startActivity(intent);
//                num = num+1;
//            }else {
//                Emerald.setValue(context,Emerald.BREACH,false);
//            }
//        }
//    }
}