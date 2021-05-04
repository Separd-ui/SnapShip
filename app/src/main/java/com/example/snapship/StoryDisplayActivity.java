package com.example.snapship;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapship.Uttils.Constans;
import com.example.snapship.Uttils.Story;
import com.example.snapship.Uttils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryDisplayActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {
    private TextView username;
    private View back,next;
    private CircleImageView img_profile;
    private ImageView img_story;
    private int count=0;
    private long press_time=0L;
    private final long limit=500L;
    private StoriesProgressView storiesProgressView;
    private List<String> imageList;
    private List<String> storyKeyList;
    private String userUI;
    private LinearLayout b_seen;
    private ImageButton b_delete;
    private TextView number_views;
    private final View.OnTouchListener onTouchListener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    press_time=System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now=System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit<now-press_time;
            }
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_display);
        init();
    }
    private void init()
    {
        imageList=new ArrayList<>();
        storyKeyList=new ArrayList<>();
        username=findViewById(R.id.story_user);
        img_profile=findViewById(R.id.story_img_profile);
        img_story=findViewById(R.id.story_show_img);
        back=findViewById(R.id.story_back);
        next=findViewById(R.id.story_next);
        storiesProgressView=findViewById(R.id.stories);
        b_delete=findViewById(R.id.story_img_delete);
        b_seen=findViewById(R.id.layout_seen);
        number_views=findViewById(R.id.story_text_views);

        Intent i=getIntent();
        userUI=i.getStringExtra(Constans.PROFILEUI);

        b_seen.setVisibility(View.GONE);
        b_delete.setVisibility(View.GONE);
        if(userUI.equals(FirebaseAuth.getInstance().getUid()))
        {
            b_seen.setVisibility(View.VISIBLE);
            b_delete.setVisibility(View.VISIBLE);
        }
        b_seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(StoryDisplayActivity.this,FollowersActivity.class);
                i.putExtra(Constans.PROFILEUI,userUI);
                i.putExtra("storykey",storyKeyList.get(count));
                startActivity(i);
                finish();
            }
        });
        b_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Story").child(userUI).child(storyKeyList.get(count));
                databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(StoryDisplayActivity.this, "Удаление истории прошло успешно!", Toast.LENGTH_SHORT).show();
                            storyKeyList.remove(count);
                            finish();
                        }
                    }
                });
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        back.setOnTouchListener(onTouchListener);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        next.setOnTouchListener(onTouchListener);
        getStories();
        getUserInfo();
    }
    private void getStories()
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Story").child(userUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageList.clear();
                storyKeyList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Story story=ds.getValue(Story.class);
                    long timecurrent=System.currentTimeMillis();
                    if(timecurrent>story.getTimestart() && timecurrent<story.getTimeend())
                    {
                        imageList.add(story.getImageid());
                        storyKeyList.add(story.getStorykey());
                    }
                }

                storiesProgressView.setStoriesCount(imageList.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryDisplayActivity.this);
                storiesProgressView.startStories(count);

                addView(storyKeyList.get(count));
                seenNumber(storyKeyList.get(count));
                Picasso.get().load(imageList.get(count)).into(img_story);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getUserInfo()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(userUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                username.setText(user.getUsername());
                if(user.getImageId().equals("empty"))
                    img_profile.setImageResource(R.mipmap.ic_launcher_round);
                else
                    Picasso.get().load(user.getImageId()).into(img_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onNext() {
        count++;
        Picasso.get().load(imageList.get(count)).into(img_story);
        addView(storyKeyList.get(count));
        seenNumber(storyKeyList.get(count));
    }

    @Override
    public void onPrev() {
        count--;
        if(count<0)
        {
            count=0;
            return;
        }
        Picasso.get().load(imageList.get(count)).into(img_story);
        seenNumber(storyKeyList.get(count));
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        storiesProgressView.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        storiesProgressView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        storiesProgressView.resume();
    }
    private void addView(String storykey)
    {
        FirebaseDatabase.getInstance().getReference("Story").child(userUI).child(storykey).child("views").child(FirebaseAuth.getInstance().getUid()).setValue(true);
    }
    private void seenNumber(String storykey)
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Story").child(userUI).child(storykey);
        databaseReference.child("views").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                number_views.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}