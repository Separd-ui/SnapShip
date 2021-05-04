package com.example.snapship.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.snapship.Adapters.UserAdapter;
import com.example.snapship.R;
import com.example.snapship.Uttils.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private EditText ed_search;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_search,container,false);
        recyclerView=view.findViewById(R.id.rec_view_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ed_search=view.findViewById(R.id.ed_search);
        userList=new ArrayList<>();
        adapter=new UserAdapter(getContext(),userList,true);
        recyclerView.setAdapter(adapter);

        readUsers();
        ed_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }
    private void searchUsers(String path)
    {
        Query query= FirebaseDatabase.getInstance().getReference("Users").orderByChild("username")
                .startAt(path)
                .endAt(path+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    User user=ds.getValue(User.class);
                    userList.add(user);
                }
                //adapter.UpdateAdapter(userList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void readUsers()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(ed_search.getText().toString().equals(""))
                {
                    userList.clear();
                    for(DataSnapshot ds:snapshot.getChildren())
                    {
                        User user=ds.getValue(User.class);
                        userList.add(user);
                    }
                    //adapter.UpdateAdapter(userList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}