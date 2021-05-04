package com.example.snapship.Account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.snapship.MainActivity;
import com.example.snapship.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    private EditText ed_mail,ed_pas;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ed_mail=findViewById(R.id.ed_mail_in);
        ed_pas=findViewById(R.id.ed_pas_in);
        checkBox=findViewById(R.id.checkBox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked())
                {
                    ed_pas.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                else
                {
                    ed_pas.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                }
            }
        });
    }

    public void onClickSignIn(View view) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(ed_mail.getText().toString(),ed_pas.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(SignInActivity.this, "Вы вошли как:"+FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                    Intent i=new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(i);
                }
            }
        });
    }
}