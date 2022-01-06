package com.psirc.dev.bleproject;

import android.content.Intent;
import android.database.Cursor;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PasswordChange extends AppCompatActivity {

    private EditText newusername;
    private EditText newpassword;
    private EditText oldpassword;

    String oldname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_password_change );
        newusername = (EditText)findViewById(R.id.newusername);
        newpassword = (EditText)findViewById(R.id.newpassword);
        oldpassword = (EditText)findViewById(R.id.oldpassword);

        getdetailss();
    }

    public void changepassword(View view)
    {

//        Emerald.display(PasswordChange.this,"-->"+newusername.getText().toString()+
//                newpassword.getText().toString()+oldpassword.getText().toString());

        if(oldpassword.getText().toString().equals(oldname))
        {
            Databasepassword database= new Databasepassword(this);

            boolean i= database.adddetails(newusername.getText().toString(),newpassword.getText().toString());
            if(i)
            {
                Toast.makeText(this, "Sucessfully Changed", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(PasswordChange.this,BaseLoginActivity.class);
                startActivity(intent);
                finish();

            }
            else
            {
                Toast.makeText(this, "Not Changed", Toast.LENGTH_SHORT).show();

            }

        }
        else
        {
            Emerald.display(this,"Enter correct old password");
        }




    }

    public void onclickback(View view)
    {
        Intent in = new Intent(this,BaseLoginActivity.class);
        startActivity(in);

    }

    public void getdetailss()
    {
        Databasepassword database= new Databasepassword(this);
        Cursor cursor = database.getdetails();



        if (cursor.getCount() == 0) {
            //  Emerald.display(BaseLoginActivity.this,"Not");
            boolean i= database.adddetails("testuser","123");


        } else {
            while (cursor.moveToNext()) {
              //  usernamee = cursor.getString(0);
                oldname = cursor.getString(0);
                // Emerald.display(BaseLoginActivity.this,usernamee+passwordd);

            }
        }
    }

}
