package com.psirc.dev.bleproject;


import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.psirc.dev.bleproject.new_pack.GeofenceHelper;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import android.content.SharedPreferences;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;
import static com.psirc.dev.bleproject.LaunchActivity.appbar_layout;
import static com.psirc.dev.bleproject.LaunchActivity.launchActivity;
import static com.psirc.dev.bleproject.LaunchActivity.llDeviceStatus;
import static com.psirc.dev.bleproject.LaunchActivity.llHome;
import static com.psirc.dev.bleproject.LaunchActivity.toolbar_title;
import static com.psirc.dev.bleproject.LaunchActivity.tvDeviceStatus;


/**
 * A simple {@link Fragment} subclass.
 */
public class GeoFenceFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final String TAG = "GeoFenceFragment";
    private Marker marker;
    private Polygon polygon;
    private ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
    private ArrayList<Polyline> polylines = new ArrayList<Polyline>();
    private ArrayList<Marker> polylineMarkers = new ArrayList<Marker>();
    private GoogleMap mMap;
    private Boolean isReady = false;
 /*   private TextView fenceTextView;
    EditText etSetRadius;
    Button btnSet;*/
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private float GEOFENCE_RADIUS = 0;
    private GeofenceHelper geofenceHelper;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";
    private String etGeofenceRadius = "";
    Switch swLocationSharing;
    Button btnSave;
    EditText etMinutes;

    public boolean smsflag = false;
    private GeofencingClient geofencingClient;
    private LocationService locationService;

    public GeoFenceFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  (View) inflater.inflate(R.layout.fragment_locationsharing, container, false);
        /*fenceTextView = (TextView)view.findViewById(R.id.fenceTextView);
        etSetRadius = (EditText)view.findViewById(R.id.etRadius);
        btnSet = (Button)view.findViewById(R.id.btnSet);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        geofencingClient = LocationServices.getGeofencingClient(getActivity());
        geofenceHelper = new GeofenceHelper(getActivity());*/
        swLocationSharing = view.findViewById(R.id.swLocationSharing);
        etMinutes = view.findViewById(R.id.etMinutes);
        btnSave = view.findViewById(R.id.btnSave);
        tvDeviceStatus.setVisibility(View.GONE);
        llDeviceStatus.setVisibility(View.GONE);
        toolbar_title.setText("Location Sharing");
        AppBarLayout.LayoutParams params = new AppBarLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,200);
// Changes the height and width to the specified *pixels*
//        params.height = 50;
//        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        appbar_layout.setLayoutParams(params);
        appbar_layout.setBackgroundColor(Color.TRANSPARENT);
        llHome.setBackgroundDrawable(getResources().getDrawable(R.drawable.tool_bg));
        SharedPreferences preferences  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = preferences.edit();

        if(preferences.getString("location_sharing","")!=null &&
                preferences.getString("location_sharing","").equals("on")){
            swLocationSharing.setChecked(true);
            etMinutes.setText(preferences.getString("location_sharing_minutes",""));

        }else{
            editor1.putString("location_sharing_minutes", "");
            editor1.putString("location_sharing","off");
        }


        /*SharedPreferences preferences  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);

        GEOFENCE_RADIUS  = preferences.getString("geofence_radius","")!=""?
                Float.parseFloat(preferences.getString("geofence_radius","")):0;

        etSetRadius.setText(preferences.getString("geofence_radius",""));
        if(GEOFENCE_RADIUS==0){
            setGeoFencingRadius();
        }
        mapFragment.getMapAsync(this);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String etRad= "";
                etRad = etSetRadius.getText().toString();
                if(etRad!=""){
                    SharedPreferences sharedPref  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor2 = sharedPref.edit();
                    editor2.putString("geofence_radius", etSetRadius.getText().toString());
                    editor2.commit();
                }else{
                    etSetRadius.setError("Enter the radius");
                }

            }
        });*/
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String minsStr = "";
                minsStr = etMinutes.getText().toString();
                if(swLocationSharing.isChecked()){
                    if(minsStr!=""){
                        SharedPreferences preferences  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor1 = preferences.edit();
                        editor1.putString("location_sharing_minutes", minsStr);
                        editor1.putString("location_sharing", "on");
                        editor1.commit();
                        Toast.makeText(getContext(), "Location sharing is enabled successfully", Toast.LENGTH_SHORT).show();
                        Fragment fragment = new HomeFragment();
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_holder, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        Intent mGeoFencingServiceIntent;
                        mGeoFencingServiceIntent = new Intent(getContext(), locationService.getClass());
                        if(isMyServiceRunning(locationService.getClass())){
                            getContext().stopService(mGeoFencingServiceIntent);
                        }
                        if (!isMyServiceRunning(locationService.getClass())) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Log.d(TAG, "onCreate: LOcationService");
                                getContext().startForegroundService(mGeoFencingServiceIntent);
                            }else{
                                getContext().startService(mGeoFencingServiceIntent);
                            }
                        }
                    }else{
                        etMinutes.setError("Please enter minutes");
                    }
                }else{
                    Toast.makeText(getContext(), "Please enable location sharing", Toast.LENGTH_SHORT).show();
                }

            }
        });
        return view;

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
    void setGeoFencingRadius() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        // ...Irrelevant code for customizing the buttons and title
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText etRadius = (EditText) dialogView.findViewById(R.id.etRadius);
        Button btnSet = (Button) dialogView.findViewById(R.id.btnSet);
        Button btnCancel = (Button) dialogView.findViewById(R.id.btnCancel);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etGeofenceRadius = etRadius.getText().toString();
                if(etGeofenceRadius!="" && etGeofenceRadius!=null){
                    SharedPreferences sharedPref  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor2 = sharedPref.edit();
                    editor2.putString("geofence_radius", etRadius.getText().toString());
                    editor2.commit();
                    GEOFENCE_RADIUS  = Float.parseFloat(sharedPref.getString("geofence_radius",""));
                }

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        SharedPreferences preferences  = launchActivity.getApplicationContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String latitude = preferences.getString("latitude","");
        String longitude = preferences.getString("longitude","");
        Log.d(TAG, "run: Latitude :" +latitude);
        Log.d(TAG, "run: Longitude :" +longitude);

        LatLng fencingLocation = null;
        String geofence_latitude = null;
        String geofence_longitude = null;
        String geofence_radius = null;
        if(latitude!=null && longitude!=null && latitude!="" && longitude!=""){
            fencingLocation = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
            geofence_latitude = preferences.getString("geofence_latitude","");
            geofence_longitude = preferences.getString("geofence_longitude","");
            geofence_radius = preferences.getString("geofence_radius","");
            Log.d(TAG, "onMapReady: geofence_latitude"+geofence_latitude);
            Log.d(TAG, "onMapReady: geofence_longitude"+geofence_longitude);
        }

        if(geofence_latitude!=null && geofence_longitude != null && geofence_radius!=null
         && geofence_latitude!="" && geofence_longitude != "" && geofence_radius!=""){
            LatLng latLng = new LatLng(Double.valueOf(geofence_latitude), Double.valueOf(geofence_longitude));
            addMarker(latLng);
            addCircle(latLng, Float.parseFloat(geofence_radius));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fencingLocation, 16));

        }
        // mMap.addMarker(new MarkerOptions().position(fencingLocation).title("Marker in Sydney"));
        enableUserLocation();
        mMap.setOnMapLongClickListener(this);
    }
    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
           // zoomToUserLocation();
        } else {
            //request for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission
                    .ACCESS_FINE_LOCATION)) {
                //We need to show user a dialog for displaying why permission is needed and then ask for
                // the Permission
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We have the permission
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
              //  zoomToUserLocation();
            } else {
                // We do not have the permission
            }
        }
    }
    private void zoomToUserLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,20));
            }
        });
    }


    public void onLocationChanged(double lat,double lng, boolean flag) {
        if (isReady) {
            if (marker != null) {
                marker.remove();
            }
            LatLng currentLocation = new LatLng(lat, lng);
//            Toast.makeText( getActivity(), "Location", Toast.LENGTH_SHORT ).show();
//            Log.v("aaaaaaaa","Location");
            marker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("MY Location"));
            //Build camera position
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentLocation)
                    .zoom(20).build();

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            /*if (flag){
                Log.w("INSIDE","INSIDE");
                fenceTextView.setText("INSIDE");
                smsflag= false;

            }else {
                Log.w("OUTSIDE","OUTSIDE");
                fenceTextView.setText("OUTSIDE");
                smsflag= false;

            }*/

        }
/*
        if (!Emerald.getValue(getActivity(),"example_auto",true)) {
            Emerald.display(getActivity(),"Please enable Auto theft feature to ON to ENABLE this Feature");
        }
        if (!Emerald.getValue(getActivity(),"example_location",true)) {
            //Emerald.display(getContext(),"Please enable Location sharing to ON to View current location");
        }
        */
    }


    public void Startsms()
    {

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {


                            if(smsflag) {

                                ArrayList<HashMap<String, String>> contacts = new ArrayList<HashMap<String, String>>();
                                Database database = new Database(getContext());
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


                                ////-----------------------------



                                String location = "https://www.google.com/maps/search/?api=1&query=" + Emerald.getValue(getActivity(), Emerald.LAT, "0.0") + "," + Emerald.getValue(getActivity(), Emerald.LNG, "0.0");
                                String message = Emerald.getValue(getActivity(), Emerald.MESSAGE, "");
                                String text = "";
                                if (Emerald.getValue(getActivity(), "example_sms", true) && Emerald.getValue(getActivity(), "example_location", true)) {
                                    text = message + " " + location;
                                } else if (Emerald.getValue(getActivity(), "example_sms", true)) {
                                    text = message;
                                } else if (Emerald.getValue(getActivity(), "example_location", true)) {
                                    text = location;
                                }

                                if (!text.equals("")) {
                                    //message = message+" "+"https://www.google.com/maps/search/?api=1&query="+Emerald.getValue(this,Emerald.LAT,"0.0")+","+Emerald.getValue(this,Emerald.LNG,"0.0");  ///maps.google.com/maps/place/11.017397,76.957016
                                    SmsManager smsManager = SmsManager.getDefault();
                                    for (int i = 0; i < contacts.size(); i++) {
                                        smsManager.sendTextMessage(contacts.get(i).get(Emerald.ITEM_CONTACT), null, text, null, null);
                                    }


                                }

                                //------------------
                            }


                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 120000);

    }



    public void restorePolygone(){
        Database database = new Database(getContext());
        database.openDb();
        Cursor cursor = database.getCords();
        cursor.moveToFirst();
        PolygonOptions polygonOptions = new PolygonOptions();
        for (int i = 0; i < cursor.getCount(); i++) {
            LatLng latLng = new LatLng(cursor.getDouble(cursor.getColumnIndex(Database.CORD_LAT)),cursor.getDouble(cursor.getColumnIndex(Database.CORD_LNG)));
            polygonOptions.add(latLng);
            cursor.moveToNext();
        }

        if (cursor.getCount() > 0) {
            polygon = mMap.addPolygon(polygonOptions.strokeColor(Color.BLUE).strokeWidth(5).fillColor(Color.argb(20, 50, 0, 255)));
        }
        cursor.close();
        database.closeDb();
        getContext().stopService(new Intent(getContext(),LocationService.class));
        getContext().startService(new Intent(getContext(),LocationService.class));

    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        SharedPreferences shared = getContext().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString("geofence_latitude", String.valueOf(latLng.latitude));
        editor.commit();
        editor.putString("geofence_longitude", String.valueOf(latLng.longitude));
        editor.commit();
        editor.putString("geofence_radius", String.valueOf(GEOFENCE_RADIUS));
        editor.commit();

        addMarker(latLng);
        addCircle(latLng, GEOFENCE_RADIUS);
        addGeofence(latLng, GEOFENCE_RADIUS);
    }
    void addGeofence(LatLng latLng, float radius) {
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL |
                        Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "OnFailure" + errorMessage);
                    }
                });

    }

    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }
}
