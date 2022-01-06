package com.psirc.dev.bleproject;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.psirc.dev.bleproject.db.DataBaseClient;
import com.psirc.dev.bleproject.db.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import droidninja.filepicker.FilePickerBuilder;

import static com.psirc.dev.bleproject.LaunchActivity.appbar_layout;
import static com.psirc.dev.bleproject.LaunchActivity.llDeviceStatus;
import static com.psirc.dev.bleproject.LaunchActivity.llHome;
import static com.psirc.dev.bleproject.LaunchActivity.toolbar_title;
import static com.psirc.dev.bleproject.LaunchActivity.tvDeviceStatus;
import static droidninja.filepicker.FilePickerConst.KEY_SELECTED_MEDIA;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    ArrayList<String> mFilePath = new ArrayList<>();
    Spinner mGender;
    User aUser = null;
    Button save;
    EditText mNameEdit, mEmailEdit, mPassword, mAddress,mInterestedAreas;
  //  CheckBox mMusicC, mTravelC, mOtherC, mMovieC;
    ProfileFragment aContext;
    Context con;
    ImageView mProfilePicImg;

    View aView;

    String name;
    String password;
    String email;
    String address, gender, interstAreas, movie, other, travel, profilepic;
    String[] genderArray = { "Male", "Female"};

    //RadioButton maleRB, femaleRB;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        aContext = this;
        con = getActivity();
        aView = inflater.inflate(R.layout.fragment_profile, container, false);
        init();
        listner();


        ArrayAdapter aa = new ArrayAdapter(getContext(),android.R.layout.simple_spinner_item,genderArray);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        mGender.setAdapter(aa);
        mGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
              //  Toast.makeText(getContext(),genderArray[i] , Toast.LENGTH_LONG).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getTasks();
        tvDeviceStatus.setVisibility(View.GONE);
        llDeviceStatus.setVisibility(View.GONE);

        toolbar_title.setText("Profile");
        AppBarLayout.LayoutParams params = new AppBarLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,200);
// Changes the height and width to the specified *pixels*
//        params.height = 50;
//        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        appbar_layout.setLayoutParams(params);
        appbar_layout.setBackgroundColor(Color.TRANSPARENT);
        llHome.setBackgroundDrawable(getResources().getDrawable(R.drawable.tool_bg));


        // Log.d("Selected ", "onCreateView: "+mGender.getc());
        return aView;
    }

    private void listner() {

        mProfilePicImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilePickerBuilder.getInstance().setMaxCount(1)
                        //.setSelectedFiles(filePaths)
                        .setActivityTheme(R.style.FilePickerTheme)
                        .enableVideoPicker(false)
                        .enableCameraSupport(true)
                        .showGifs(false)
                        .showFolderView(true)
                        .enableImagePicker(true)
                        .pickPhoto(aContext);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }

    private void init() {
        mProfilePicImg = aView.findViewById(R.id.profile_pic);
        mNameEdit = aView.findViewById(R.id.name);
        mEmailEdit = aView.findViewById(R.id.email);
        mPassword = aView.findViewById(R.id.password);
        mAddress = aView.findViewById(R.id.home_address);
        mGender = aView.findViewById(R.id.gender);
        save = aView.findViewById(R.id.btnSave);
        mInterestedAreas = aView.findViewById(R.id.interest_areas);

        /*mMusicC = aView.findViewById(R.id.music);
        mTravelC = aView.findViewById(R.id.travel);
        mMovieC = aView.findViewById(R.id.movie);
        mOtherC = aView.findViewById(R.id.other);
        maleRB = aView.findViewById(R.id.male);
        femaleRB = aView.findViewById(R.id.female);*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 233) {
            if(data!=null){
                mFilePath = data.getStringArrayListExtra(KEY_SELECTED_MEDIA);
                profilepic = mFilePath.get(0);
                Glide.with(aContext)
                        .load(new File(profilepic))
                        .apply(RequestOptions.centerCropTransform()
                                .dontAnimate()
                                .placeholder(R.drawable.holder_profile))
                        .thumbnail(0.5f)
                        .into(mProfilePicImg);
                Log.d("File ", "onActivityResult: " + mFilePath.size());
            }
        }
    }

    public void save() {

        name = getText(mNameEdit);
        password = getText(mPassword);
        email = getText(mEmailEdit);
        address = getText(mAddress);
        interstAreas = getText(mInterestedAreas);
        if (validate()) {

            int checkedRadioButtonId = mGender.getSelectedItemPosition();

            if (checkedRadioButtonId == -1) {
                // No item selected
            } else {
                if (checkedRadioButtonId == 0) {
                    gender = "0";
                } else if (checkedRadioButtonId == 1) {
                    gender = "1";
                }
            }

           /* if (mMusicC.isSelected()) {
                music = "1";
            } else {
                music = "0";
            }

            if (mMovieC.isSelected()) {
                movie = "1";
            } else {
                movie = "0";
            }

            if (mTravelC.isSelected()) {
                travel = "1";
            } else {
                travel = "0";
            }

            if (mOtherC.isSelected()) {
                other = "1";
            } else {
                other = "0";
            }*/

        }

        aUser = new User();
        aUser.setName(name);
        aUser.setEmail(email);
        aUser.setPassword(password);
        aUser.setGender(gender);
        aUser.setAddress(address);

        aUser.setMovie(movie);
        aUser.setMusic(interstAreas);
        aUser.setTravel(travel);
        aUser.setOther(other);

        if (profilepic != null)
            aUser.setProfilepic(profilepic);
        else
            aUser.setProfilepic(null);
        saveUser();

        //    getTasks();
    }

    public boolean validate() {
        if (name.length() == 0) {
            return false;
        } else if (password.length() == 0) {
            return false;
        } else if (email.length() == 0) {
            return false;
        } else if (address.length() == 0) {
            return false;
        }
        return true;
    }

    public String getText(EditText a) {
        return a.getText().toString().trim();
    }

    private void getTasks() {
        class GetTasks extends AsyncTask<Void, Void, List<User>> {

            @Override
            protected List<User> doInBackground(Void... voids) {
                List<User> taskList = DataBaseClient
                        .getInstance(getActivity())
                        .getAppDatabase()
                        .taskDao()
                        .getAll();
                return taskList;
            }

            @Override
            protected void onPostExecute(List<User> tasks) {
                super.onPostExecute(tasks);

                User user = tasks.get(0);
                name = user.getName();
                password = user.getPassword();
                email = user.getEmail();
                address = user.getAddress();
                gender = user.getGender();
                travel = user.getTravel();
                interstAreas = user.getMusic();
                movie = user.getMovie();
                other = user.getOther();
                profilepic = user.getProfilepic();

                mNameEdit.setText(name);
                mPassword.setText(password);
                mEmailEdit.setText(email);
                mAddress.setText(address);
                mInterestedAreas.setText(interstAreas);
                /*if (travel != null && travel.equalsIgnoreCase("1")) {
                    mTravelC.setSelected(true);
                }
                if (mMovieC != null && travel.equalsIgnoreCase("1")) {
                    mMovieC.setSelected(true);
                }
                if (mOtherC != null && travel.equalsIgnoreCase("1")) {
                    mOtherC.setSelected(true);
                }
                if (mMusicC != null && travel.equalsIgnoreCase("1")) {
                    mMusicC.setSelected(true);
                }*/
                if (gender != null) {
                    Log.d("TAG", "onPostExecute: "+gender);
                    if (gender.equalsIgnoreCase("0")) {
                        Log.d("TAG", "onPostExecute:if "+gender);
                        mGender.setSelection(0);
                    } else if (gender.equalsIgnoreCase("1")) {
                        Log.d("TAG", "onPostExecute:else "+gender);
                        mGender.setSelection(1);                    }
                }

                if (profilepic != null) {
                    Glide.with(aContext)
                            .load(new File(profilepic))
                            .apply(RequestOptions.centerCropTransform()
                                    .dontAnimate()
                                    .placeholder(R.drawable.holder_profile))
                            .thumbnail(0.5f)
                            .into(mProfilePicImg);
                }
            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }


    private void saveUser() {
        class SaveUser extends AsyncTask<Void, Void, Long> {

            @Override
            protected Long doInBackground(Void... voids) {
                DataBaseClient.getInstance(getActivity()).getAppDatabase()
                        .taskDao()
                        .insert(aUser);
                return DataBaseClient.getInstance(getActivity()).getAppDatabase()
                        .taskDao()
                        .insert(aUser);
            }

            @Override
            protected void onPostExecute(Long str) {
                super.onPostExecute(str);
                if (str == 0) {
                    Emerald.display(getActivity(), "Updated Successfully");
                }

                // Log.d("Get User", "onPostExecute: " + tasks.size());
            }
        }

        new SaveUser().execute();
    }
}
