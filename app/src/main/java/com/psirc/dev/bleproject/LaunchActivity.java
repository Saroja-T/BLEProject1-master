    package com.psirc.dev.bleproject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.psirc.dev.bleproject.db.DataBaseClient;
import com.psirc.dev.bleproject.db.User;
import com.psirc.dev.bleproject.new_pack.GeofenceBroadcastReceiver;
import com.psirc.dev.bleproject.new_pack.GeofencingService;
import com.psirc.dev.bleproject.new_pack.SharedPreferencesManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LaunchActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Runnable, DeviceClickListener {
    ArrayList<HashMap<String, String>> contacts = new ArrayList<HashMap<String, String>>();

    CircleImageView ProfilePic;
    TextView nameTxt, emailTxt;
    static TextView tvDeviceStatus;
    static TextView tvStatus;
    static TextView itemTime;
    static ImageView blueBtn;
    static ImageView redBtn;
    static LinearLayout llDeviceStatus;
    static LinearLayout llHome;
    static AppBarLayout appbar_layout;
    static TextView toolbar_title;
    int flag = 0;
    private FragmentManager fragmentManager;
    private HomeFragment homeFragment;
    private MacFragemnt macfragment;
    private ProfileFragment profileFragment;
    private ContactFragment contactFragment;
    private EmgyFragment emgyFragment;
    private GeoFenceFragment geoFenceFragment;
    private MessageFragment messageFragment;
    private PrivacyFragment privacyFragment;
    private Handler handler = new Handler();
    private ProgressDialog progressDialog;
    private long deviceId;
    private static final int REQUEST_ENABLE_BT = 1;
    String maciddd = "";
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";

    private int step = 0;
    private BluetoothAdapter bluetoothAdapter;

    private boolean mScanning;
    private Handler handlerBlue = new Handler();
    private static final String TAG = "LaunchActivity";
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private View deviceItemView;
    public static String status = "-1";
    public EmeraldService emd = new EmeraldService();
    private MyJobService myJobService;
    private LocationService locationService;
    //private GeofencingService geofencingService;
    public static LaunchActivity launchActivity;
    Context context;
    Intent mServiceIntent;
    Intent mGeoFencingServiceIntent;
    GeofenceBroadcastReceiver geofenceBroadcastReceiver;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        launchActivity = this;
        blueBtn = (ImageView) findViewById(R.id.blueBtn);
        redBtn = (ImageView) findViewById(R.id.redBtn);
        getMacAddress();

        context = this;

        tvDeviceStatus = (TextView) findViewById(R.id.tvDeviceStatus);
        appbar_layout = (AppBarLayout) findViewById(R.id.appbar_layout);
        llHome = (LinearLayout) findViewById(R.id.llHome);
        tvStatus = (TextView) findViewById(R.id.status);
        itemTime = (TextView) findViewById(R.id.itemTime);
        llDeviceStatus = (LinearLayout) findViewById(R.id.llDeviceStatus);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        String currentDateTime = simpleDateFormat.format(calendar.getTime()).toString();
        itemTime.setText(currentDateTime);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        ProfilePic = (CircleImageView) headerView.findViewById(R.id.profileimg);
        nameTxt = (TextView) headerView.findViewById(R.id.name);
        emailTxt = (TextView) headerView.findViewById(R.id.email);
        navigationView.setNavigationItemSelectedListener(this);

        /*ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Emerald BLE");
        actionBar.setSubtitle("Your personal Jewel Tracker");*/
        fragmentManager = getSupportFragmentManager();

        ImageButton menu_ib = findViewById(R.id.menu);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
       /* ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();*/

        menu_ib.setOnClickListener(new View.OnClickListener() {
            //@SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
                //openOptionsMenu();
               // drawer.openDrawer(Gravity.START);
                drawer.openDrawer(Gravity.LEFT);
            }
        });


        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //Called when a drawer's position changes.
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getTasks();
                //Called when a drawer has settled in a completely open state.
                //The drawer is interactive at this point.
                // If you have 2 drawers (left and right) you can distinguish
                // them by using id of the drawerView. int id = drawerView.getId();
                // id will be your layout's id: for example R.id.left_drawer
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // Called when a drawer has settled in a completely closed state.
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Called when the drawer motion state changes. The new state will be one of STATE_IDLE, STATE_DRAGGING or STATE_SETTLING.
            }
        });


        homeFragment = new HomeFragment();
        homeFragment.deviceClickListener = this;
        macfragment = new MacFragemnt();
        profileFragment = new ProfileFragment();
        contactFragment = new ContactFragment();
        emgyFragment = new EmgyFragment();
        geoFenceFragment = new GeoFenceFragment();
        messageFragment = new MessageFragment();
        privacyFragment = new PrivacyFragment();

        loadFragment(homeFragment);

        // This is for bluetooth runtime request
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e("bt--if","bt-if");
            Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
            // Queue the request to pops the bluetooth dialogue
            startActivityForResult( enableBtIntent, REQUEST_ENABLE_BT );
        } else {
            Log.e("bt--if","bt-else");
           /* if (Emerald.getValue(this, Emerald.LOGIN, false) && !maciddd.isEmpty()) {
                startService(new Intent(getApplicationContext(),MyJobService.class));
            }*/
        }

        myJobService = new MyJobService();
        locationService = new LocationService();
        mServiceIntent = new Intent(this, myJobService.getClass());
        mGeoFencingServiceIntent = new Intent(this, locationService.getClass());

        SharedPreferences preferences  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String latitude = preferences.getString("latitude","");
        String longitude = preferences.getString("longitude","");
        String geofence_latitude = preferences.getString("geofence_latitude","");
        String geofence_longitude = preferences.getString("geofence_longitude","");
        String geofence_radius = preferences.getString("geofence_radius","");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean example_phone =  sharedPreferences.getBoolean("example_phone",true);
        Log.d(TAG, "onCreate: "+example_phone);
        statusCheck();
        if (!isMyServiceRunning(myJobService.getClass())) {
//            startService(mServiceIntent);
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(mServiceIntent);
            }*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(mGeoFencingServiceIntent);
                context.startForegroundService(mServiceIntent);
            }else{
                startService(mServiceIntent);
            }
        }
        if (!isMyServiceRunning(locationService.getClass())) {
            // if(geofence_latitude!=null && geofence_longitude!=null && geofence_radius!=null && geofence_radius!="" && geofence_longitude!="" && geofence_latitude!=""){
//            startService(mGeoFencingServiceIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(mGeoFencingServiceIntent);
                Log.d(TAG, "onCreate: LOcationService");
                context.startForegroundService(mGeoFencingServiceIntent);
            }else{
                startService(mGeoFencingServiceIntent);
            }
            //  }
        }


    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }


    @Override
    protected void onDestroy() {
        //stopService(mServiceIntent);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);

        /*Intent broadcastIntent1 = new Intent();
        broadcastIntent1.setAction("restartservice");
        broadcastIntent1.setClass(this, GeofenceBroadcastReceiver.class);
        this.sendBroadcast(broadcastIntent1);*/
        super.onDestroy();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            //IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND );
           // registerReceiver( mReceiver, filter );
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Log.e("onActivity","if");
               // finish();
            } else {
                Log.e("onActivity","else");
                SharedPreferences preferences  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
                Boolean isLogin = preferences.getBoolean("isLogin",false);
                if (isLogin) {
                    /*startService(new Intent(this, LocationService.class));
                    Intent intent = new Intent(this, EmeraldService.class);
                    Notification notificationCompat = new NotificationCompat.Builder(this, "Dev").build();
                    ContextCompat.startForegroundService(this, intent);*/
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.launch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {

            SharedPreferences shared1 = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor editor1 = shared1.edit();
            editor1.putBoolean("isLogin",false);
            editor1.commit();

          //  Emerald.setValue(this, Emerald.LOGIN, false);
           // Emerald.setValue(this, Emerald.LOGIN_OBJ, "{}");
            Intent intent = new Intent(this, EmeraldService.class);
            Notification notificationCompat = new NotificationCompat.Builder(this, "Dev").build();
            stopService(intent);
            Intent loginIntent = new Intent(this, BaseLoginActivity.class);
            startActivity(loginIntent);
            finish();
            return true;
        } else if (id == R.id.action_add) {
            onAddPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_message) {
            loadFragment(messageFragment);
        } else if (id == R.id.nav_contact) {
            contactFragment.contacts = new ArrayList<HashMap<String, String>>();
            loadFragment(contactFragment);
        } else if (id == R.id.nav_geofence) {
            loadFragment(geoFenceFragment);
        } else if (id == R.id.nav_privacy) {
            loadFragment(privacyFragment);
            //startActivity(new Intent(this,SettingsActivity.class));

        } else if (id == R.id.nav_home) {
            loadFragment(homeFragment);
        } else if (id == R.id.nav_profile) {
            loadFragment(profileFragment);
        } else if (id == R.id.nav_mac) {
            loadFragment(macfragment);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void loadFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_holder, fragment);
        fragmentTransaction.commit();

    }

    private void onAddPressed() {
        loadFragment(homeFragment);
        progressDialog = ProgressDialog.show(this, "Scan in Progress", "Searching...", false);
        progressDialog.show();
        handler.postDelayed(this, 5000);

    }

    @Override
    public void run() {
        switch (step) {
            case 0:
                deviceId = Math.round(Math.random() * 100000);
                progressDialog.setMessage("Found " + deviceId);
                handler.postDelayed(this, 2000);
                step = step + 1;
                break;

            case 1:
                progressDialog.setMessage("Adding Device");
                handler.postDelayed(this, 2000);
                step = step + 1;
                break;

            case 2:
                progressDialog.setMessage("Waiting BLE Acknowledge");
                handler.postDelayed(this, 2000);
                step = step + 1;
                break;

            case 3:
                progressDialog.setMessage("Successfully Paired");
                handler.postDelayed(this, 2000);
                step = step + 1;
                HashMap<String, String> item = new HashMap<String, String>();
                item.put(Emerald.ITEM_NAME, deviceId + "");
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
                item.put(Emerald.ITEM_TIME, "added on: " + simpleDateFormat.format(calendar.getTime()));
                item.put(Emerald.ITEM_DISTANCE, "0.5m");
                homeFragment.updateItem(item);
                break;

            case 4:
                progressDialog.dismiss();
                step = 0;
                break;
        }


    }

    @Override
    public void onDeviceClicked(HashMap<String, String> device) {
        Log.w("BT",device.get(Emerald.ITEM_DISTANCE));
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.get(Emerald.ITEM_NAME));
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.get(Emerald.ITEM_DISTANCE));

        startActivity(intent);
    }

    @Override
    public void onDeviceStatusChanged(View view) {
        deviceItemView = view;
        setDeviceStatus("statsi");
    }


   /* @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(locationReceiver);
    }*/

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            geoFenceFragment.onLocationChanged(extras.getDouble(Emerald.LAT), extras.getDouble(Emerald.LNG), extras.getBoolean(Emerald.FENCE));
        }
    };

     void setDeviceStatus(String from) {
        Log.e("connectde",from);

        if (deviceItemView != null) {
            ImageView blueImg = (ImageView) deviceItemView.findViewById(R.id.blueBtn);
            ImageView regImg = (ImageView) deviceItemView.findViewById(R.id.redBtn);
            if (from.equals("Connected")) {
                Log.d(">> Device Status", "Connected");
                blueImg.setVisibility(ImageView.VISIBLE);
                regImg.setVisibility(ImageView.GONE);
                emd.stopsounddd();
            } else if (from.equals("Disconnected")) {
                Log.d(">> Device Status", "Disconnected");
                blueImg.setVisibility(ImageView.GONE);
                regImg.setVisibility(ImageView.VISIBLE);

               /* SharedPreferences pref = getApplicationContext().getSharedPreferences("EmeraldSP", 0); // 0 - for private mode
                boolean sound = pref.getBoolean("Sound", false);
                    if (!sound) {
                        Log.d(">> Handler PF", "running...");
                        emd.startSound();
                        Log.d(">> Player", "Start");
                    } else {
                        Log.d(">> Player", "Off");
                    }*/
                //}

            } else {
                broadcastUpdate();
            }
            Log.w("TAG", "Status Called");
        }
    }

    private void broadcastUpdate() {
        final Intent intent = new Intent(LaunchActivity.ACTION_GATT_DISCONNECTED);
        sendBroadcast(intent);
    }


    /*private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Overrideg
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            Log.e("action",action);
            if (EmeraldService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.w("TAG", "Connected");
                status = "Connected";
            } else if (EmeraldService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.w("TAG", "Disconnected");
                status = "Disconnected";
            } else if (EmeraldService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.w("TAG", "Connected");
                status = "Connected";
            } else if (EmeraldService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.w("TAG", "Data Available");

            }
            setDeviceStatus("broat");
        }
    };*/

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EmeraldService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(EmeraldService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(EmeraldService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(EmeraldService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(geofenceBroadcastReceiver, makeGattUpdateIntentFilter());
     //   registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        //startService(new Intent(getApplicationContext(),EmeraldService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(mGattUpdateReceiver);
    }

    public void getMacAddress() {
        //Edited by Skein
        //get mac address from local storage
//        maciddd = SharedPreferencesManager.getMacAddress(LaunchActivity.this);
        SharedPreferences preferences  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String macAddress = preferences.getString("mac_address","");
        String contactNumbers = preferences.getString("contact_number","");
        Log.e("Print",macAddress);
        Log.e("Print",contactNumbers);
    }


    private void enableAutoStart() {
        if (Build.BRAND.equalsIgnoreCase("xiaomi")) {

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            startActivity(intent);
        }
    }

    private void getTasks() {
        class GetTasks extends AsyncTask<Void, Void, List<User>> {

            @Override
            protected List<User> doInBackground(Void... voids) {
                List<User> taskList = DataBaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .taskDao()
                        .getAll();
                return taskList;
            }

            @Override
            protected void onPostExecute(List<User> tasks) {
                super.onPostExecute(tasks);

                User user = tasks.get(0);

                nameTxt.setText(user.getName());
                emailTxt.setText(user.getEmail());

                if (user.getProfilepic() != null) {
                    Glide.with(getApplicationContext())
                            .load(new File(user.getProfilepic()))
                            .apply(RequestOptions.centerCropTransform()
                                    .dontAnimate()
                                    .placeholder(R.drawable.holder_profile))
                            .thumbnail(0.5f)
                            .into(ProfilePic);
                }
            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }
}
