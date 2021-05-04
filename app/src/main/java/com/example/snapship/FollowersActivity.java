package com.example.snapship;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.snapship.Adapters.UserAdapter;
import com.example.snapship.Uttils.Constans;
import com.example.snapship.Uttils.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FollowersActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private String title;
    private String profileUI;
    private Boolean trigger;
    private List<User> userList;
    private UserAdapter adapter;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private List<String> follows;
    private String storyKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        GetIntent();
        init();
    }
    private void init()
    {
        recyclerView=findViewById(R.id.rec_view_followers);
        userList=new ArrayList<>();
        follows=new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new UserAdapter(this,userList,false);
        recyclerView.setAdapter(adapter);

        toolbar=findViewById(R.id.toolbar8);
        setSupportActionBar(toolbar);
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if(title.equals("Подписчики")){
            toolbar.setTitle(title);
            getFollowers("followers");
        }
        else if(title.equals("Подписки"))
        {
            toolbar.setTitle(title);
            getFollowers("following");
        }
        else if(title.equals("Просмотры")){
            toolbar.setTitle(title);
            getViewers();
        }
        else {
        toolbar.setTitle(title);
        getLikes();
        }

    }
    private void getViewers()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Story").child(auth.getUid()).child(storyKey).child("views");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                follows.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    follows.add(ds.getKey());
                }
                readFollowers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowers(String follow)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(profileUI).child(follow);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                follows.clear();
                for (DataSnapshot ds:snapshot.getChildren())
                {
                    follows.add(ds.getKey());
                }
                readFollowers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getLikes()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Likes").child(profileUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                follows.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    follows.add(ds.getKey());
                }
                readLikes();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void readLikes()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    User user=ds.getValue(User.class);
                    for(String ui:follows)
                    {
                        if(ui.equals(user.getUi()))
                            userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private  void readFollowers()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    User user=ds.getValue(User.class);
                    for(String ui:follows)
                    {
                        if(ui.equals(user.getUi()))
                            userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GetIntent()
    {
        Intent i=getIntent();
        if(i!=null)
        {
            if(i.getStringExtra(Constans.FOLLOWERS)!=null)
            {
                title=i.getStringExtra(Constans.FOLLOWERS);
                profileUI=i.getStringExtra(Constans.PROFILEUI);
            }
            else
            {
                title="Просмотры";
                storyKey=i.getStringExtra("storykey");
            }
        }

    }

}