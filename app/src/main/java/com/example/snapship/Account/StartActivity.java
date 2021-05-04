package com.example.snapship.Account;

import androidx.annotation.IntRange;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.snapship.MainActivity;
import com.example.snapship.R;
import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    /*@Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            Toast.makeText(this, "Вы вошли как:"+FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(StartActivity.this, MainActivity.class));
        }
    }*/

    public void onClickSignIn(View view) {
        Intent i=new Intent(StartActivity.this,SignInActivity.class);
        startActivity(i);

    }
    public  void onClickSignUp(View view)
    {
        Intent i=new Intent(StartActivity.this,SignUpActivity.class);
        startActivity(i);
    }

}