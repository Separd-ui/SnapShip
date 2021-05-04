package com.example.snapship.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import com.example.snapship.Adapters.PostAdapter;
import com.example.snapship.R;
import com.example.snapship.Uttils.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class PostDetailFragment extends Fragment {
    private String postKey;
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_post_detail, container, false);

        recyclerView=view.findViewById(R.id.rec_view_post_detail);
        postList=new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter=new PostAdapter(getContext(),postList);
        recyclerView.setAdapter(adapter);
        SharedPreferences prefs=getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        postKey=prefs.getString("postKey","none");
        readPost();
        return view;
    }
    private void readPost()
    {

        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Posts").child(postKey);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                Post post=snapshot.getValue(Post.class);
                postList.add(post);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}