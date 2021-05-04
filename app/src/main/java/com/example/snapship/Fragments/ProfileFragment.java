package com.example.snapship.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapship.Account.StartActivity;
import com.example.snapship.Adapters.PhotoAdapter;
import com.example.snapship.EditActivity;
import com.example.snapship.FollowersActivity;
import com.example.snapship.R;
import com.example.snapship.Uttils.Constans;
import com.example.snapship.Uttils.Notifications;
import com.example.snapship.Uttils.Post;
import com.example.snapship.Uttils.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ProfileFragment extends Fragment {
    private ImageView img_profile;
    private TextView posts_amount,followers_amount,followings_amount,username,bio;
    private RecyclerView recyclerViewMy,recyclerViewSaved;
    private String userUI;
    private Button b_profile;
    private PhotoAdapter adapter,adapterSaved;
    private List<Post> postList,postListSaved;
    private String text_button;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private ImageButton img_my,img_saved,img_options;
    private List<String> saveList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile, container, false);
        init(view);

        return  view;
    }
    private void init(View view)
    {
        recyclerViewMy=view.findViewById(R.id.rec_view_photo);
        recyclerViewMy.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new GridLayoutManager(getContext(),3);
        recyclerViewMy.setLayoutManager(linearLayoutManager);
        postList=new ArrayList<>();
        adapter=new PhotoAdapter(getContext(),postList);
        recyclerViewMy.setAdapter(adapter);

        recyclerViewSaved=view.findViewById(R.id.rec_view_safe);
        recyclerViewSaved.setLayoutManager(new GridLayoutManager(getContext(),3));
        postListSaved=new ArrayList<>();
        adapterSaved=new PhotoAdapter(getContext(),postListSaved);
        recyclerViewSaved.setAdapter(adapterSaved);
        saveList=new ArrayList<>();

        img_options=view.findViewById(R.id.img_options);
        img_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        });
        img_my=view.findViewById(R.id.img_b_my);
        img_saved=view.findViewById(R.id.img_b_saved);
        img_my.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerViewSaved.setVisibility(View.GONE);
                recyclerViewMy.setVisibility(View.VISIBLE);
                showPhoto();
            }
        });
        img_saved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerViewMy.setVisibility(View.GONE);
                recyclerViewSaved.setVisibility(View.VISIBLE);
                SavesList();
            }
        });
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        img_profile=view.findViewById(R.id.profile_image);
        username=view.findViewById(R.id.profile_username);
        bio=view.findViewById(R.id.text_bio);
        posts_amount=view.findViewById(R.id.posts);
        followers_amount=view.findViewById(R.id.followers);
        followings_amount=view.findViewById(R.id.followings);

        b_profile=view.findViewById(R.id.b_profile);

        SharedPreferences prefs=getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        userUI=prefs.getString("profileUI","none");
        followers_amount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent i=new Intent(getContext(), FollowersActivity.class);
                i.putExtra(Constans.FOLLOWERS,"Подписчики");
                i.putExtra(Constans.PROFILEUI,userUI);
                startActivity(i);
            }
        });
        followings_amount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getContext(), FollowersActivity.class);
                i.putExtra(Constans.FOLLOWERS,"Подписки");
                i.putExtra(Constans.PROFILEUI,userUI);
                startActivity(i);

            }
        });
        isFollowed();
        text_button=b_profile.getText().toString();
        getInfo();
        getNumberPosts();
        getNumberFollow();
        showPhoto();
        b_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(text_button.equals("Редактировать"))
                {
                    Intent i=new Intent(getContext(), EditActivity.class);
                    startActivity(i);
                }
                else if(text_button.equals("Подписаться"))
                {
                    DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(auth.getUid()).child("following");
                    databaseReference.child(userUI).setValue(true);
                    DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(userUI).child("followers");
                    databaseReference1.child(auth.getUid()).setValue(true);
                    b_profile.setText(R.string.follow_b);
                    addNotificationFollow();
                    text_button="Подписаны";
                }
                else
                {
                    DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(auth.getUid()).child("following");
                    databaseReference.child(userUI).removeValue();
                    DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(userUI).child("followers");
                    databaseReference1.child(auth.getUid()).removeValue();
                    b_profile.setText(R.string.follow);
                    text_button="Подписаться";
                }
            }
        });

    }
    private void addNotificationFollow()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Notifications").child(userUI);
        Notifications notifications=new Notifications();
        String key=databaseReference.push().getKey();
        notifications.setText("подписался на вас.");
        notifications.setIspost(false);
        notifications.setUserui(auth.getUid());
        notifications.setPostkey("");
        notifications.setKey(key);
        databaseReference.child(key).setValue(notifications);
    }
    private  void getInfo()
    {
        DatabaseReference  databaseReference=firebaseDatabase.getReference("Users").child(userUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                username.setText(user.getUsername());
                bio.setText(user.getBio());
                if(user.getImageId().startsWith("http"))
                {
                    Picasso.get().load(user.getImageId()).into(img_profile);
                }
                else
                {
                    img_profile.setImageResource(R.mipmap.ic_launcher);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getNumberPosts()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count=0;
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Post post=ds.getValue(Post.class);
                    if(post.getPublisher().equals(userUI))
                        count++;
                }
                posts_amount.setText(""+count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void isFollowed()
    {
        if(userUI.equals(auth.getUid()))
            b_profile.setText("Редактировать");
        else
        {
            DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(auth.getUid()).child("following");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(userUI).exists())
                        b_profile.setText("Подписаны");
                    else
                        b_profile.setText("Подписаться");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    private void showPopupMenu()
    {
        PopupMenu popupMenu=new PopupMenu(getContext(),img_options);
        popupMenu.inflate(R.menu.menu_options);
        Menu menu=popupMenu.getMenu();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if(id==R.id.options)
                {
                    Toast.makeText(getContext(), "Options", Toast.LENGTH_SHORT).show();
                }
                else if(id==R.id.sign_out)
                {
                    SignOut();
                }
                return true;
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }
        popupMenu.show();
    }
    private void SignOut()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.change);
        builder.setMessage(R.string.change_mes);
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                auth.signOut();
                Intent i=new Intent(getContext(), StartActivity.class);
                startActivity(i);
            }
        });
        builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }
    /*private boolean menuItemClicked(MenuItem item)
    {
        int id=item.getItemId();
        if(id==R.id.options)
        {
            Toast.makeText(getContext(), "Options", Toast.LENGTH_SHORT).show();
        }
        else if(id==R.id.sign_out)
        {
            SignOut();
        }
        return true;
    }*/

    private void getNumberFollow()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(userUI).child("following");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followings_amount.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(userUI).child("followers");
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers_amount.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showPhoto()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Post post=ds.getValue(Post.class);
                    if(post.getPublisher().equals(userUI))
                        postList.add(post);
                }
                Collections.reverse(postList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void SavesList()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Save").child(userUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                saveList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    saveList.add(ds.getKey());
                }
                showSaved();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showSaved()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postListSaved.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Post post=ds.getValue(Post.class);
                    for(String s:saveList)
                    {
                        if(post.getKey().equals(s))
                            postListSaved.add(post);
                    }
                }
                adapterSaved.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }





}