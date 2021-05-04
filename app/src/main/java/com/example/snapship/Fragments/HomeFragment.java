package com.example.snapship.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.snapship.Adapters.PostAdapter;
import com.example.snapship.Adapters.StoryAdapter;
import com.example.snapship.R;
import com.example.snapship.Uttils.Post;
import com.example.snapship.Uttils.Story;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.util.GAuthToken;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList;
    private List<String> followersList;
    private ProgressBar progressBar;
    private RecyclerView recyclerView_story;
    private List<Story> storyList;
    private StoryAdapter adapter_story;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_home,container,false);
        progressBar=view.findViewById(R.id.progressBar);
        recyclerView=view.findViewById(R.id.rec_view_home);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList=new ArrayList<>();
        followersList=new ArrayList<>();
        adapter=new PostAdapter(getContext(),postList);
        recyclerView.setAdapter(adapter);

        recyclerView_story=view.findViewById(R.id.rec_view_story);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false);
        recyclerView_story.setLayoutManager(layoutManager);
        storyList=new ArrayList<>();
        adapter_story=new StoryAdapter(getContext(),storyList);
        recyclerView_story.setAdapter(adapter_story);

        checkFollowings();
        return view;
    }
    private void checkFollowings()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Follow").child(FirebaseAuth.getInstance().getUid()).child("following");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followersList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    followersList.add(ds.getKey());
                }
                readPosts();
                readStory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readPosts()
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Post post =ds.getValue(Post.class);
                    for(String ui:followersList)
                    {
                        if(post.getPublisher().equals(ui))
                            postList.add(post);
                    }
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void readStory()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Story");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long time_current=System.currentTimeMillis();
                storyList.clear();
                Story story=new Story();
                story.setImageid("empty");
                story.setStorykey("empty");
                story.setTimeend(0);
                story.setTimestart(0);
                story.setUserui(FirebaseAuth.getInstance().getUid());
                storyList.add(story);

                for(String ui:followersList)
                {
                    int count_story=0;
                    Story story1=null;
                    for(DataSnapshot ds:snapshot.child(ui).getChildren())
                    {
                        story1=ds.getValue(Story.class);
                        if(time_current>story1.getTimestart() && time_current<story1.getTimeend()) {
                            count_story++;
                        }
                    }
                    if(count_story>0)
                        storyList.add(story1);
                }
                adapter_story.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}