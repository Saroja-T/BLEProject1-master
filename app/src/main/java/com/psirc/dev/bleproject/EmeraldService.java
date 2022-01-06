package com.psirc.dev.bleproject;

import android.Manifest;
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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.psirc.dev.bleproject.new_pack.SharedPreferencesManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static java.lang.Thread.sleep;

public class EmeraldService extends Service {

    ArrayList<HashMap<String, String>> contacts = new ArrayList<HashMap<String, String>>();

    String maciddd = " ";
    String contactNumbers = " ";
    int flag = 0;
    private final static String TAG = EmeraldService.class.getSimpleName();

    private SoundPool soundPool;
    private int sampleId = -1;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

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
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
        private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e("S-Test","CONNECTED");

                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                checkcon = 1;
                broadcastUpdate(intentAction);
                Log.d(">> EmService", "Connected to GATT server.");
                Log.d(">> EmService Device", "Connected");

               // Emerald.setValue(getBaseContext(), Emerald.STATUS, true);
                stopsounddd();

                Log.w(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e("S-Test","disconnected");
                Log.d(">> EmService Device", "Disconnected");

                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                checkcon = 2;
                for (int j = 0; j <= 5; j++) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (j == 4) {
                        broadcastUpdate(intentAction);
                        if (checkcon == 2) {
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("EmeraldSP", 0); // 0 - for private mode
                            boolean sound = pref.getBoolean("Sound", false);
                                if (!sound) {
                                    startSound();
                                    Log.d(">> Player", "Start");
                                } else {
                                    Log.d(">> Player", "Off");
                                }

                        }
                    }
                }

                Emerald.setValue(getBaseContext(), Emerald.STATUS, false);
                broadcastUpdate(intentAction);
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.e("S-Test","onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e("S-Test","onServicesDiscovered-if");
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                displayGattServices(getSupportedGattServices());

                Emerald.setValue(getBaseContext(), Emerald.STATUS, true);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                Log.w(">> EmService ", "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                Log.w("CH", "on Read");
                Log.w(">> EmService Status", "on Read");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
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

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        EmeraldService getService() {
            return EmeraldService.this;
        }
    }


    private String mac = "80:1F:12:BD:72:4F";

    @Override
    public void onCreate() {
        super.onCreate();
        getmac();

        player = MediaPlayer.create(this, R.raw.audio);
        player.setLooping(true); // Set looping
        player.setVolume(100, 100);


        if (Emerald.getValue(this, Emerald.LOGIN, false)) {
//            String loginObj = Emerald.getValue(this, Emerald.LOGIN_OBJ, "{}");
//            Log.w("OBJ", loginObj);
            try {
//                JSONObject deviceObject = new JSONObject(loginObj);
                startServiceOreoCondition();
                loadSoundPool();
                initialize();
                //this.mac="80:1F:12:BD:72:4F";
                //connect("80:1F:12:BA:3C:E3");
                Log.d(">> MacId", maciddd);
                if (!maciddd.equals("-1")) {
                    connect(maciddd,contactNumbers);
                }

            } catch (Exception e) {
                Log.e("gggg", e.toString());
                e.printStackTrace();
            }
        } else {
            stopSelf();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {

        //loadSoundPool();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        //close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        // Log.v("initializeeee","initializeeee");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
                //Log.v("initializeeee","initializeeee");
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address,String contact_number) {
        contactNumbers = contact_number;
        Log.e("flow-check","connect");
        Log.d(">> BLE", "Connecting...");
        mBluetoothDeviceAddress = address;
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
                    Log.w(TAG, "Device not found.  Unable to connect.");
                    Log.d(">> BLE", "Device not found.  Unable to connect.");
                    isConnected = false;
                    return isConnected;
                }
                // We want to directly connect to the device, so we are setting the autoConnect
                // parameter to false.

                mBluetoothGatt = device.connectGatt(this, true, mGattCallback);

                // Previously connected device.  Try to reconnect.
                if (mBluetoothGatt != null) {
                    Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                    Log.d(">> BLE", "Trying to use an existing mBluetoothGatt for connection.");
                    if (mBluetoothGatt.connect()) {
                        mConnectionState = STATE_CONNECTING;
                        //int val = mBluetoothManager.getConnectionState(device, mBluetoothGatt.getConnectionState(device));
                        int val = mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
                        Log.d(">> BLE---", String.valueOf(val)+"------");
                        isConnected = true;
                        return isConnected;
                    } else {
                        Log.d(">> BLE", "Connection Failed");
                        isConnected = false;
                        return isConnected;
                    }
                } else {
                    mConnectionState = STATE_DISCONNECTED;
                    Log.d(">> BLE", "Not Connected");
                    isConnected = false;
                    return isConnected;
                }
            }
            return isConnected;

        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic, false);

        Log.e(TAG, "--->" + characteristic.getUuid().toString() + "<----");

        if (characteristic.getUuid().toString().equals("bf3fbd80-063f-11e5-9e69-0002a5d5c503")) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            Log.w("BT", descriptor.getCharacteristic().getUuid().toString());
            //descriptor.setValue( BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            //mBluetoothGatt.writeDescriptor(descriptor);
            //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor); //descriptor write operation successfully started?
        }


        mBluetoothGatt.setCharacteristicNotification(characteristic, true);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor1 = characteristic.getDescriptor(
                    UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
           // descriptor1.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            descriptor1.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor1);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    private void loadSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .build();

        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                //soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
                EmeraldService.this.sampleId = sampleId;
            }
        });
        soundPool.load(this, R.raw.siren, 1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
        //return 1;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("EmeraldSP", 0); // 0 - for private mode
        boolean sound = pref.getBoolean("Sound", false);
        String mac_address = pref.getString("_mac_address","");
        //String mac_address = Emerald.getValue(getApplicationContext(), Emerald.MAC_ADDRESS, "")!=null ?Emerald.getValue(getApplicationContext(), Emerald.MAC_ADDRESS, "") :"";
        if(mac_address!=null && mac_address!=" " && mac_address!=""){
            if (sound) {
                if (player.isPlaying()) {
                    player.pause();
                }
                player.release();
            } else {
                player.release();
            }

            if (mBluetoothGatt != null) {
                close();
            }
        }


    }

    private void startServiceOreoCondition() {
        if (Build.VERSION.SDK_INT >= 26) {

            String CHANNEL_ID = "my_service";
            String CHANNEL_NAME = "My Background Service";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setCategory(Notification.CATEGORY_SERVICE).setSmallIcon(R.drawable.ic_menu_camera).setPriority(1).build();

            startForeground(101, notification);
        }
        enableautostart();

    }

    private void startCallandSMS() {
        Log.e("gghh","fgfggggg");
        flag = 1;
     //   Emerald.setValue(this, Emerald.BREACH, true);
        /*if(PreferenceManager.getDefaultSharedPreferences(this)!=null){
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putBoolean(Emerald.BREACH,true);
            editor.commit();
        }*/
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        contactNumbers = sharedPreferences.getString("contact_numbers","");
        try {
            Log.e("contact",contactNumbers);
            JSONArray jsonArray = new JSONArray(contactNumbers);
            Log.e("gdfdghf","fdgf");
            for (int i = 0; i < jsonArray.length(); i++) {
                HashMap<String, String> item = new HashMap<String, String>();
                item.put(Emerald.ITEM_NAME, "test");
                item.put(Emerald.ITEM_CONTACT, jsonArray.get(i).toString());
                contacts.add(item);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        String location = "https://www.google.com/maps/search/?api=1&query=" + Emerald
                .getValue(this, Emerald.LAT, "0.0") + "," + Emerald.getValue(this, Emerald.LNG, "0.0");
        String message = Emerald.getValue(this, Emerald.MESSAGE, "");
        String text = "";
        if (Emerald.getValue(this, "example_sms", true) &&
                Emerald.getValue(this, "example_location", true)) {
            text = message + " " + location;
        } else if (Emerald.getValue(this, "example_sms", true)) {
            text = message;
        } else if (Emerald.getValue(this, "example_location", true)) {
            text = location;
        }

        if (!text.equals("")) {

            message = message+" "+"https://www.google.com/maps/search/?api=1&query="+Emerald.getValue(this,Emerald.LAT,"0.0")+","+Emerald.getValue(this,Emerald.LNG,"0.0");  ///maps.google.com/maps/place/11.017397,76.957016
            SmsManager smsManager = SmsManager.getDefault();
          //  smsManager.sendTextMessage("+919600672742", null, text, null, null);
            for (int i = 0; i < contacts.size(); i++) {
                smsManager.sendTextMessage(contacts.get(i).get(Emerald.ITEM_CONTACT), null, text, null, null);
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


    public void call() {

        if (Emerald.getValue(this, "example_phone", true)) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            //intent.setData(Uri.parse("tel:" + contacts.get(i).get(Emerald.ITEM_CONTACT)));
            intent.setData(Uri.parse("tel:" + "+919600672742"));
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


        }

        int con_length = contacts.size();

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


            }


            try {
                sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        contacts.clear();
    }


    public void getmac() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        contactNumbers = sharedPreferences.getString("contact_numbers","");
        //contactNumbers = SharedPreferencesManager.getContextNumbers(this);
        maciddd = SharedPreferencesManager.getMacAddress(this);
    }

    public void enableautostart() {

        try {
            final Intent intent = new Intent();
            String manufacturer = android.os.Build.MANUFACTURER;
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
            } else if ("oneplus".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.oneplus.security",
                        "com.oneplus.security.chainlaunch.view.ChainLaunchAppListAct‌​ivity"));
            } else if ("Letv".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
            } else if ("LeMobile".equalsIgnoreCase(manufacturer)) {
                Toast.makeText(getApplicationContext(), "a " + manufacturer, Toast.LENGTH_LONG).show();

                intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
            }

            List<ResolveInfo> list = getApplicationContext().getPackageManager().queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();


        }
    }
}