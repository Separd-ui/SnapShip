package com.example.snapship.Account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.snapship.MainActivity;
import com.example.snapship.R;
import com.example.snapship.Uttils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private EditText ed_name,ed_username,ed_pas,ed_mail;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();
    }

    private void init()
    {
        ed_mail=findViewById(R.id.ed_mail);
        ed_name=findViewById(R.id.ed_name);
        ed_username = findViewById(R.id.ed_username);
        ed_pas=findViewById(R.id.ed_pas);
        checkBox=findViewById(R.id.checkBox2);
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

        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();


    }

    public void onClickSignUpIn(View view) {
        Intent i=new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(i);
    }

    public void onClickSignUp(View view) {
        if(!TextUtils.isEmpty(ed_mail.getText().toString()) && !TextUtils.isEmpty(ed_pas.getText().toString()) &&
                !TextUtils.isEmpty(ed_username.getText().toString()) && !TextUtils.isEmpty(ed_name.getText().toString()))
        {
            auth.createUserWithEmailAndPassword(ed_mail.getText().toString(),ed_pas.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful() && auth.getUid()!=null)
                    {
                        DatabaseReference databaseReference=database.getReference("Users").child(auth.getUid());
                        User user=new User();
                        user.setBio("");
                        user.setFullname(ed_name.getText().toString());
                        user.setUi(auth.getUid());
                        user.setUsername(ed_username.getText().toString());
                        user.setImageId("empty");
                        databaseReference.setValue(user);
                        assert auth.getCurrentUser()!=null;
                        Toast.makeText(SignUpActivity.this, "Вы вошли как:"+auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                        Intent i=new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(i);
                    }
                    else {
                        Toast.makeText(SignUpActivity.this, "Что-то пошло не так.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            Toast.makeText(this, "Все поля должны быть заполнены.", Toast.LENGTH_SHORT).show();
        }
    }
}