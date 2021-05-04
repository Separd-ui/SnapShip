package com.example.snapship;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.snapship.Account.StartActivity;
import com.example.snapship.Fragments.HomeFragment;
import com.example.snapship.Fragments.NotificationFragment;
import com.example.snapship.Fragments.ProfileFragment;
import com.example.snapship.Fragments.SearchFragment;
import com.example.snapship.Uttils.Constans;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigationView;
    private Fragment selectedFragment;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    private void init()
    {
        bottomNavigationView=findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        auth=FirebaseAuth.getInstance();
        GetIntent();

    }
    private void GetIntent()
    {
        Intent i=getIntent();
        if(i!=null)
        {
            if(i.getStringExtra(Constans.PROFILEUI)!=null)
            {
                SharedPreferences.Editor editor=getSharedPreferences("PREFS",MODE_PRIVATE).edit();
                editor.putString("profileUI",i.getStringExtra(Constans.PROFILEUI));
                editor.apply();

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();
            }
            else
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
            }
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        switch (id)
        {
            case R.id.nav_home:
                selectedFragment=new HomeFragment();
                break;
            case R.id.nav_add:
                selectedFragment=null;
                startActivity(new Intent(MainActivity.this,PostActivity.class));
                break;
            case R.id.nav_notifications:
                selectedFragment=new NotificationFragment();
                break;
            case R.id.nav_me:
                SharedPreferences.Editor editor=getSharedPreferences("PREFS",MODE_PRIVATE).edit();
                editor.putString("profileUI",auth.getUid());
                editor.apply();
                selectedFragment=new ProfileFragment();
                break;
            case R.id.nav_search:
                selectedFragment=new SearchFragment();
                break;
        }
        if(selectedFragment!=null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
        }
        return true;
    }
}