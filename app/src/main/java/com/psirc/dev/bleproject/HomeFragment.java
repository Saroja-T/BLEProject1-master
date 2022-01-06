package com.psirc.dev.bleproject;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.psirc.dev.bleproject.db.DataBaseClient;
import com.psirc.dev.bleproject.db.User;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.psirc.dev.bleproject.LaunchActivity.appbar_layout;
import static com.psirc.dev.bleproject.LaunchActivity.llDeviceStatus;
import static com.psirc.dev.bleproject.LaunchActivity.llHome;
import static com.psirc.dev.bleproject.LaunchActivity.toolbar_title;
import static com.psirc.dev.bleproject.LaunchActivity.tvDeviceStatus;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private DeviceAdapter deviceAdapter;
    public DeviceClickListener deviceClickListener;
    private ArrayList<HashMap<String,String>> items = new ArrayList<HashMap<String, String>>();
    String maciddd=" ";
    public HomeFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
       /* getmac();
        Toast.makeText( getContext(),"bbb",Toast.LENGTH_SHORT ).show();
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recycleView);
        deviceAdapter = new DeviceAdapter(getContext(),items,deviceClickListener);


        recyclerView.setAdapter(deviceAdapter);
        restoreDevices();*/

        tvDeviceStatus.setVisibility(View.GONE);
        llDeviceStatus.setVisibility(View.VISIBLE);
        toolbar_title.setText(getResources().getString(R.string.app_name));
        appbar_layout.setBackground(getResources().getDrawable(R.drawable.home_bg2));
        llHome.setBackgroundColor(Color.WHITE);
        AppBarLayout.LayoutParams params = new AppBarLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,800);
// Changes the height and width to the specified *pixels*
//        params.height = 50;
//        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        appbar_layout.setLayoutParams(params);

        return view;
    }

    private void restoreDevices(){
        String mac_address = Emerald.getValue(getActivity(), Emerald.MAC_ADDRESS, "")!=null ?Emerald.getValue(getActivity(), Emerald.MAC_ADDRESS, "") : "";

        try {
            String loginObj = Emerald.getValue(getContext(),Emerald.LOGIN_OBJ,"{}");
            Log.w("OBJ",loginObj);

            HashMap<String, String> item = new HashMap<String, String>();

            item.put(Emerald.ITEM_NAME,"BLE_Device");
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
            item.put(Emerald.ITEM_TIME, simpleDateFormat.format(calendar.getTime()));
            //item.put(Emerald.ITEM_DISTANCE,"F8:94:C2:E5:17:5C");
            item.put(Emerald.ITEM_DISTANCE,mac_address);
            //item.put(Emerald.ITEM_DISTANCE,"bc:2f:3d:57:b7:7c");
            //item.put(Emerald.ITEM_DISTANCE,maciddd);
            updateItem(item);
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }

    public void updateItem(HashMap<String,String> item){
        boolean isThere = false;
        for (int i = 0; i < items.size(); i++) {
            isThere = false;
            if (items.get(i).get(Emerald.ITEM_DISTANCE).equals(item.get(Emerald.ITEM_DISTANCE))){
                isThere = true;
                break;
            }
        }
        if (!isThere){
            /*changed by aagnia items.add(0,item); is replace items.add(item);*/
            items.add(item);
            //deviceAdapter.notifyDataSetChanged();
        }

    }
    public void getmac()
    {
        Databasemacc database= new Databasemacc(getContext());
        Cursor cursor = database.getmacid();
        if (cursor.getCount() == 0) {
        } else {
            while (cursor.moveToNext()) {
                maciddd = cursor.getString(0);
            }
        }
    }

}
