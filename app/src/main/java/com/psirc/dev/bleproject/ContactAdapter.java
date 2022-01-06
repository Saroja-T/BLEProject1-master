package com.psirc.dev.bleproject;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder>{


    private ArrayList<HashMap<String,String>> items = new ArrayList<HashMap<String, String>>();
    private Context context;
    private View.OnClickListener onClickListener;

    public ContactAdapter(Context context, ArrayList<HashMap<String,String>> items, View.OnClickListener onClickListener){
        this.context = context;
        this.onClickListener = onClickListener;
        this.items = items;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item_layout,viewGroup,false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder deviceViewHolder, int i) {
        deviceViewHolder.itemName.setText(items.get(i).get(Emerald.ITEM_NAME));
        deviceViewHolder.itemContact.setText(items.get(i).get(Emerald.ITEM_CONTACT));
        deviceViewHolder.itemDelete.setTag(R.string.app_name,items.get(i));
        deviceViewHolder.itemDelete.setTag(R.string.add,i);
        deviceViewHolder.itemDelete.setOnClickListener(onClickListener);
        //deviceViewHolder.itemDistance.setText(items.get(i).get(Emerald.ITEM_DISTANCE));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder{

        public View mItem;
        public TextView itemName,itemContact;
        ImageView itemDelete;
        public ContactViewHolder(View itemView) {
            super(itemView);
            mItem = itemView;
            itemName = (TextView)itemView.findViewById(R.id.itemName);
            itemContact = (TextView)itemView.findViewById(R.id.itemContact);
            itemDelete = (ImageView) itemView.findViewById(R.id.itemDelete);
        }


    }
}
