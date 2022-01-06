package com.psirc.dev.bleproject;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.psirc.dev.bleproject.LaunchActivity.llDeviceStatus;
import static com.psirc.dev.bleproject.LaunchActivity.tvDeviceStatus;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmgyFragment extends Fragment {


    public EmgyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        tvDeviceStatus.setVisibility(View.GONE);
        llDeviceStatus.setVisibility(View.GONE);

        return inflater.inflate(R.layout.fragment_emgy, container, false);
    }

}
