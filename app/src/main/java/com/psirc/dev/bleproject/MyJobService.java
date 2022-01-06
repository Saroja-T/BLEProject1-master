package com.psirc.dev.bleproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

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

public class MyJobService extends Service {
  //  ArrayList<HashMap<String, String>> contacts = new ArrayList<HashMap<String, String>>();
    private static final String TAG = "MyJobService";
    private static boolean isConnected = false;
    public int counter=0;
    int flag = 0;
    int alarmFlag = 0;
    //boolean isCalledUser=false;
    public String contactNumbers = "";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
  //  private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    JSONArray contactJsonArray;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private Handler mHandler;
    String connectionStatus = "";
    private static final long SCAN_PERIOD = 10000;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT);

    int checkcon;
    public static MediaPlayer player;
    GeofenceHelper geofenceHelper;



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
        initialize();
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
    }



    private Timer timer;
    private TimerTask timerTask;
    public void startTimer() {
        timer = new Timer();
        mHandler = new Handler();
        timerTask = new TimerTask() {
            @SuppressLint("ResourceAsColor")
            public void run() {
                SharedPreferences preferences  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
                String macAddress = preferences.getString("mac_address","");
                contactNumbers = preferences.getString("contact_number","");
                Log.i("Count", macAddress+"=========  "+ (counter++));
                final BluetoothManager bluetoothManager =
                        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                if(bluetoothManager.getAdapter()!=null){
                    mBluetoothAdapter = bluetoothManager.getAdapter();
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
                    if(connectionStatus == "connected"){
                        launchActivity.tvDeviceStatus.setText("Device Status: Connected");
                        launchActivity.tvStatus.setText("Connected");
                    }else{
                        launchActivity.tvDeviceStatus.setText("Device Status: Disconnected");
                        launchActivity.tvStatus.setText("Disconnected");
                    }
                    connectToDevice(device);
                }


            }
        };
        timer.schedule(timerTask, 10000, 10000); //
    }

    @SuppressLint("NewApi")
    private void scanLeDevice(final boolean enable) {
        Log.e("check","scanLeDevice");
        if (enable) {
            Log.e("check","scanLeDevice-if-enabvled");

            /*mHandler.postDelayed(new Runnable() {
                @SuppressLint("NewApi")
                @Override
                public void run() {*/
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);
                    }
               /* }
            }, SCAN_PERIOD);*/
            if (Build.VERSION.SDK_INT < 21) {
                Log.e("check","scanLeDevice-startLeScan");

                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                Log.e("check","scanLeDevice-startScan");

                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }
    @SuppressLint("NewApi")
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.e("check","scanLeDevice-callBack");

            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice("80:1F:12:BD:72:63");
            //BluetoothDevice btDevice = result.getDevice();
            connectToDevice(device);
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }
        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {

                            Log.i("onLeScan", device.toString());
                            connectToDevice(device);

                }
            };
    @SuppressLint("NewApi")
    public void connectToDevice(BluetoothDevice device) {
        if (isConnected) {
            return;
        }
        Log.e("check","scanLeDevice-connectTo");
        mGatt = device.connectGatt(this, false, gattCallback);

    }
    @SuppressLint("NewApi")
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("NewApi")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    isConnected = true;
                    alarmFlag = 1;
                    gatt.discoverServices();
                    SharedPreferences shared1 = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor1 = shared1.edit();
                    editor1.putBoolean("isCalled",false);
                    editor1.commit();
                    stopsounddd();
                    connectionStatus = "connected";
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(launchActivity);
                    Boolean example_auto =  sharedPreferences.getBoolean("example_auto",true);
                    if(example_auto) {
                        startSound();
                    }
                    connectionStatus = "disconnected";
                    isConnected = false;
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }
        @SuppressLint("NewApi")
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            final List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e("S-Test","onServicesDiscovered-if");
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                displayGattServices(getSupportedGattServices());

                Emerald.setValue(getBaseContext(), Emerald.STATUS, true);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                Log.w(">> EmService ", "onServicesDiscovered received: " + status);
            }
            gatt.readCharacteristic(services.get(1).getCharacteristics().get
                    (0));

        }
        public List<BluetoothGattService> getSupportedGattServices() {
            if (mGatt == null) return null;

            return mGatt.getServices();
        }
        @SuppressLint("NewApi")
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            gatt.disconnect();
           // gatt.close();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.w("CH", "on Change");
            Log.w(">> EmService Status", "on Change");

            final byte[] data = characteristic.getValue();
            Log.w("DATA", data.toString());

            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                Log.w("DATA", stringBuilder.toString());
                //if (stringBuilder.toString().equals("00")){

                Log.v("aaaaaaaaaa", "aaaaaaaaaa");

                if (flag == 0) {

                    Log.v("ppppppppppppp", "aaaaaaaaaaaa");
                    startCallandSMS();

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // this code will be executed after 2 seconds
                            flag = 0;
                            Log.v("aaaaaaaaaaa", "cheack");
                        }
                    }, 30000);
                }
                //}
            }
        }
    };
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        Log.e("display","displayGattSrvices");
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            Log.e("display","GattSrvices");
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put("LIST_NAME", GattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put("LIST_UUID", uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                Log.e("display","displayGattCharSrvices");
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        "LIST_NAME", GattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put("LIST_UUID", uuid);
                if (uuid.equals("bf3fbd80-063f-11e5-9e69-0002a5d5c503")) {
                    setCharacteristicNotification(gattCharacteristic, true);
                }
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mGatt.setCharacteristicNotification(characteristic, false);

        Log.e(TAG, "--->" + characteristic.getUuid().toString() + "<----");

        if (characteristic.getUuid().toString().equals("bf3fbd80-063f-11e5-9e69-0002a5d5c503")) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            Log.w("BT", descriptor.getCharacteristic().getUuid().toString());
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mGatt.writeDescriptor(descriptor); //descriptor write operation successfully started?
        }


        mGatt.setCharacteristicNotification(characteristic, true);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor1 = characteristic.getDescriptor(
                    UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            // descriptor1.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            descriptor1.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mGatt.writeDescriptor(descriptor1);
        }
    }
    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean connect(final String address) {
        Log.e("flow-check",address);

        Log.e("flow-check","connect");
        Log.d(">> BLE", "Connecting...");
        if (mBluetoothAdapter == null || address == null || address.isEmpty()) {
            Log.e("flow-check","connection-failed");
            Log.d(">> BLE", "Connection Failed");
            return false;
        } else {
            boolean isConnected = false;
            Log.e("address",address+"--"+address.length());
            if(address!=null && address!=" "){
                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                if (device == null) {
                    Log.w("TAG", "Device not found.  Unable to connect.");
                    Log.d(">> BLE", "Device not found.  Unable to connect.");
                    isConnected = false;
                    return isConnected;
                }
                // We want to directly connect to the device, so we are setting the autoConnect
                // parameter to false.
               // mBluetoothGatt = device.connectGatt(this, true, mGattCallback);

                // Previously connected device.  Try to reconnect.
               /* if (mBluetoothGatt != null) {
                    Log.d("TAG", "Trying to use an existing mBluetoothGatt for connection.");
                    Log.d(">> BLE", "Trying to use an existing mBluetoothGatt for connection.");
                    if (mBluetoothGatt.connect()) {
                        mConnectionState = STATE_CONNECTING;
                        //int val = mBluetoothManager.getConnectionState(device, mBluetoothGatt.getConnectionState(device));
                        int val = mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
                        Log.d(">> BLE---", String.valueOf(val)+"------");
                        isConnected = true;
                        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
                        return isConnected;
                    } else {
                        Log.d(">> BLE", "Connection Failed");
                        isConnected = false;
                        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
                        return isConnected;
                    }
                } else {
                    mConnectionState = STATE_DISCONNECTED;
                    Log.d(">> BLE", "Not Connected");
                    isConnected = false;
                    mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
                    return isConnected;
                }*/
            }

            return isConnected;

        }
    }

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
         Log.v("initializeeee","initializeeee");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e("TAG", "Unable to initialize BluetoothManager.");
                return false;
                //Log.v("initializeeee","initializeeee");
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e("TAG", "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public void stopsounddd() {
        Log.d(">> Player ", "Sound Stopped");
        try {
            if (player.isPlaying()) {
                player.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            //startCallandSMS();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSounds() {
        try {
            if (!player.isPlaying()) {
                player.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }



    private void startCallandSMS() {
        flag = 1;
        SharedPreferences preferences  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        Boolean isCalledUser = preferences.getBoolean("isCalled",false);
        Log.e("gghh", String.valueOf(isCalledUser));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(launchActivity);
        Boolean example_phone =  sharedPreferences.getBoolean("example_phone",true);

       // if(!isCalledUser){
            Log.e("gghh", String.valueOf(contactNumbers));

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
            /**/


            if (sharedPreferences.getBoolean("example_sms", true) &&
                    sharedPreferences.getBoolean("example_location", true)) {
                text = message + " " + location;
            } else if (sharedPreferences.getBoolean("example_sms", true)) {
                text = message;
            } else if (sharedPreferences.getBoolean("example_location", true)) {
                text = location;
            }

            //text = message+" "+location;
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
            call(example_phone);
      //  }
    }


    public void call(Boolean example_phone) {

        if(example_phone){
            int con_length = contactJsonArray.length();

            for (int i = 0; i < con_length; i++) {
                Log.e("callllllllllllllll", "callllllllllllllll" + con_length);

                Intent intent = new Intent(Intent.ACTION_CALL);
                try {
                    Log.e("call123",String.valueOf(contactJsonArray.get(i)));

                    intent.setData(Uri.parse("tel:" + contactJsonArray.get(i)));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }



        }

//        contacts.clear();
    }

}