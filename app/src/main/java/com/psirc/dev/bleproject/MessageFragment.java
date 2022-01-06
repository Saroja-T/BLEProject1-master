package com.psirc.dev.bleproject;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import static com.psirc.dev.bleproject.LaunchActivity.llDeviceStatus;
import static com.psirc.dev.bleproject.LaunchActivity.tvDeviceStatus;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {


    private EditText messageEdit;

    public MessageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = (View)inflater.inflate(R.layout.fragment_message, container, false);
        messageEdit = (EditText)view.findViewById(R.id.messageEdit);
        Button messageBtn = (Button)view.findViewById(R.id.messageSaveBtn);
        tvDeviceStatus.setVisibility(View.GONE);
        llDeviceStatus.setVisibility(View.GONE);


        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Emerald.setValue(getActivity(),Emerald.MESSAGE,messageEdit.getText().toString());
                Emerald.display(getActivity(),"Message saved succefully");
            }
        });
        return view;
    }

}
