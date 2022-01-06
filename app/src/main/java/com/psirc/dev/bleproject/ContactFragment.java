package com.psirc.dev.bleproject;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;

import static com.psirc.dev.bleproject.LaunchActivity.llDeviceStatus;
import static com.psirc.dev.bleproject.LaunchActivity.tvDeviceStatus;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment {

    private ContactAdapter contactAdapter;

    public ArrayList<HashMap<String,String>> contacts = new ArrayList<HashMap<String, String>>();
    private View.OnClickListener onClickListener,addClickListener;
    public ContactFragment() {
        // Required empty public constructor

    }




    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,  Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recycleView);
        //contactAdapter.notifyDataSetChanged();
        Button addBtn = (Button)view.findViewById(R.id.btn_add);
        tvDeviceStatus.setVisibility(View.GONE);
        llDeviceStatus.setVisibility(View.GONE);


        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ImageView deleteImage = (ImageView)v;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Enter");
                builder.setMessage("Do you really want to delete this contact?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String,String> item = (HashMap<String,String>)deleteImage.getTag(R.string.app_name);
                        Database database = new Database(getActivity());
                        database.openDb();
                        database.deleteContactByPhone(item.get(Emerald.ITEM_CONTACT));
                        database.closeDb();
                        contacts.remove(item);
                        contactAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("Cancel", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

        };
        addClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                callContacts(container);
                contactAdapter.notifyDataSetChanged();
//
//
//


//                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                builder.setTitle("Enter");
//                View contactView = LayoutInflater.from(getContext()).inflate(R.layout.add_item_layout,null,false);
//                final EditText contactName = (EditText)contactView.findViewById(R.id.contactName);
//                final EditText contactNumber = (EditText)contactView.findViewById(R.id.contactNumber);
//                builder.setView(contactView);
//                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (contacts.size() < 6) {
//                            Database database = new Database(getContext());
//                            database.openDb();
//                            if (database.addContact(contactName.getText().toString(), contactNumber.getText().toString(), "") != -1) {
//                                HashMap<String, String> item = new HashMap<String, String>();
//                                item.put(Emerald.ITEM_NAME, contactName.getText().toString());
//                                item.put(Emerald.ITEM_CONTACT, contactNumber.getText().toString());
//                                contacts.add(item);
//                            }else{
//                                Emerald.display(getContext(),"Phone number is already existing");
//                            }
//                            database.closeDb();
//                            contactAdapter.notifyDataSetChanged();
//                        }else{
//                            Emerald.display(getContext(),"Maximum 5 Contacts allowed");
//                        }
//                    }
//                });
//                builder.setNegativeButton("Cancel", null);
//                AlertDialog alertDialog = builder.create();
//                alertDialog.show();
                contactAdapter.notifyDataSetChanged();

            }
        };
        addBtn.setOnClickListener(addClickListener);
        Database database = new Database(getContext());
        database.openDb();
        Cursor cursor = database.getContacts();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            HashMap<String,String> item = new HashMap<String, String>();
            item.put(Emerald.ITEM_NAME,cursor.getString(cursor.getColumnIndex(Database.CONTACT_NAME)));
            item.put(Emerald.ITEM_CONTACT,cursor.getString(cursor.getColumnIndex(Database.CONTACT_PHONE)));
            contacts.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        database.closeDb();
        contactAdapter = new ContactAdapter(getContext(),contacts,onClickListener);
        recyclerView.setAdapter(contactAdapter);
        return view;
    }



        public void callContacts(View c){

            Intent calContctPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            calContctPickerIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(calContctPickerIntent, 1);
        }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data)
    {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode)
        {
            case (1) :
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri contctDataVar = data.getData();

                    Cursor contctCursorVar = getActivity().getApplicationContext().getContentResolver().query(contctDataVar, null,
                            null, null, null);
                    if (contctCursorVar.getCount() > 0)
                    {
                        while (contctCursorVar.moveToNext())
                        {

                            String Name = contctCursorVar.getString(contctCursorVar.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                            Log.i("Names", Name);

                            if (Integer.parseInt(contctCursorVar.getString(contctCursorVar.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                            {
                                // Query phone here. Covered next
                                String Number = contctCursorVar.getString(contctCursorVar.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                Log.i("Number", Number);




                                if (contacts.size() < 3) {
                            Database database = new Database(getContext());
                            database.openDb();
                            if (database.addContact(Name, Number, "") != -1) {
                                HashMap<String, String> item = new HashMap<String, String>();
                                item.put(Emerald.ITEM_NAME, Name);
                                item.put(Emerald.ITEM_CONTACT, Number);
                                contacts.add(item);
                                contactAdapter.notifyDataSetChanged();
                            }else{
                                Emerald.display(getContext(),"Phone number is already existing");
                            }
                            database.closeDb();
                            contactAdapter.notifyDataSetChanged();
                        }else{
                            Emerald.display(getContext(),"Maximum 3 Contacts allowed");
                        }


                            }

                        }
                    }
                }
                break;
        }
    }



}
