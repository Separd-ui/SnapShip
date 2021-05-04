package com.example.snapship.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.snapship.Adapters.NotificationAdapter;
import com.example.snapship.R;
import com.example.snapship.Uttils.Notifications;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Notifications> notificationsList;
    private NotificationAdapter adapter;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_notification, container, false);

        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        recyclerView=view.findViewById(R.id.rec_view_tifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationsList=new ArrayList<>();
        adapter=new NotificationAdapter(getContext(),notificationsList);
        recyclerView.setAdapter(adapter);

        readNotifications();
        return view;
    }
    private void readNotifications()
    {
        DatabaseReference databaseReference= firebaseDatabase.getReference("Notifications").child(auth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationsList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Notifications notifications=ds.getValue(Notifications.class);
                    notificationsList.add(notifications);
                }
                Collections.reverse(notificationsList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}