package com.psirc.dev.bleproject;


import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>{


    private ArrayList<HashMap<String,String>> items = new ArrayList<HashMap<String, String>>();
    private Context context;
    private DeviceClickListener deviceClickListener;

    public DeviceAdapter(Context context,ArrayList<HashMap<String,String>> items, DeviceClickListener deviceClickListener){
        this.context = context;
        this.items = items;
        this.deviceClickListener = deviceClickListener;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout,viewGroup,false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder deviceViewHolder, int i) {
        deviceViewHolder.mItem.setTag(items.get(i));
        deviceViewHolder.mItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String,String> device = (HashMap<String, String>)v.getTag();
                deviceClickListener.onDeviceClicked(device);
            }
        });
        deviceViewHolder.itemName.setText(items.get(i).get(Emerald.ITEM_NAME));
        deviceViewHolder.itemTime.setText(items.get(i).get(Emerald.ITEM_TIME));
        deviceViewHolder.itemDistance.setText(items.get(i).get(Emerald.ITEM_DISTANCE));
        deviceViewHolder.redBtn.setVisibility(ImageView.GONE);
        deviceViewHolder.blueBtn.setVisibility(ImageView.GONE);
        Log.e("value123", String.valueOf(Emerald.getValue(context,Emerald.STATUS,false)));

        if (Emerald.getValue(context,Emerald.STATUS,false)){
            Log.e("value123","blue");
            deviceViewHolder.redBtn.setVisibility(ImageView.GONE);
            deviceViewHolder.blueBtn.setVisibility(ImageView.VISIBLE);
        }else{
            Log.e("value123","red");
            deviceViewHolder.redBtn.setVisibility(ImageView.VISIBLE);
            deviceViewHolder.blueBtn.setVisibility(ImageView.GONE);
        }
        deviceClickListener.onDeviceStatusChanged(deviceViewHolder.mItem);

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder{

        public View mItem;
        public TextView itemName,itemTime,itemDistance;
        public ImageView blueBtn,redBtn;
        public DeviceViewHolder(View itemView) {
            super(itemView);

            itemName = (TextView)itemView.findViewById(R.id.itemName);
            itemDistance = (TextView)itemView.findViewById(R.id.itemDistance);
            itemTime = (TextView)itemView.findViewById(R.id.itemTime);
            blueBtn = (ImageView) itemView.findViewById(R.id.blueBtn);
            redBtn = (ImageView) itemView.findViewById(R.id.redBtn);
            mItem = itemView;

        }


    }
}
