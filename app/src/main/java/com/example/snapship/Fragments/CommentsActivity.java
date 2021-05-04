package com.example.snapship.Fragments;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapship.Adapters.ComAdapter;
import com.example.snapship.PushNotifications.APIService;
import com.example.snapship.PushNotifications.Client;
import com.example.snapship.PushNotifications.Data;
import com.example.snapship.PushNotifications.MyResponse;
import com.example.snapship.PushNotifications.Sender;
import com.example.snapship.PushNotifications.Token;
import com.example.snapship.R;
import com.example.snapship.Uttils.Comments;
import com.example.snapship.Uttils.Notifications;
import com.example.snapship.Uttils.Post;
import com.example.snapship.Uttils.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ComAdapter adapter;
    private EditText ed_cmt;
    private CircleImageView img_profile,img_profile_publisher;
    private TextView post,text_username,text_desc;
    private String postUI,publisherUI;
    private List<Comments> commentsList;
    private APIService apiService;
    private boolean notify=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar toolbar=findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Комментарии");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        apiService= Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        ed_cmt=findViewById(R.id.ed_cmt);
        img_profile=findViewById(R.id.img_profile_cmt);
        img_profile_publisher=findViewById(R.id.img_profile_publisher);
        text_desc=findViewById(R.id.text_desc_cmt);
        text_username=findViewById(R.id.text_username_cmt);

        recyclerView=findViewById(R.id.rec_view_cmt);
        commentsList=new ArrayList<>();
        adapter=new ComAdapter(commentsList,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        Intent i=getIntent();
        postUI=i.getStringExtra("postUI");
        publisherUI=i.getStringExtra("publisherUI");

        getImageProfile();
        getInfoPublisher();
        readComments();

    }

    public void onClickSendCmt(View view) {
        notify=true;
        if(ed_cmt.getText().toString().equals(""))
        {
            Toast.makeText(this, "Вы не можете отправить пустой комментарий", Toast.LENGTH_SHORT).show();
        }
        else {
            sendCmt();
        }
    }
    private void sendCmt()
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Comments").child(postUI);
        Comments comments=new Comments();
        comments.setComment(ed_cmt.getText().toString());
        comments.setPublisher(FirebaseAuth.getInstance().getUid());
        databaseReference.push().setValue(comments);
        addNotificationComment();
        final String msg=ed_cmt.getText().toString();
        ed_cmt.setText("");

        /*DatabaseReference databaseReference1=FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                if(notify)
                {
                    //sendNotification(publisherUI,user.getUsername(),msg);
                }

                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });**/
    }
    /*private void sendNotification(String receiver,String username,String message)
    {
        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Token token=ds.getValue(Token.class);
                    Data data=new Data();
                    data.setUser(FirebaseAuth.getInstance().getUid());
                    data.setIcon(R.mipmap.ic_launcher);
                    data.setBody(username+": "+message);
                    data.setTitle("Новый комментарий.");
                    data.setSented(publisherUI);

                    Sender sender=new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code()==200)
                                    {
                                        if(response.body().success!=1)
                                        {
                                            Toast.makeText(CommentsActivity.this, "Произошла ошибка.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/
    private void addNotificationComment()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Notifications").child(publisherUI);
        Notifications notifications=new Notifications();
        String key=databaseReference.push().getKey();
        notifications.setText("прокомментировал:"+ed_cmt.getText().toString());
        notifications.setIspost(true);
        notifications.setUserui(FirebaseAuth.getInstance().getUid());
        notifications.setPostkey(postUI);
        notifications.setKey(key);
        databaseReference.child(key).setValue(notifications);
    }
    private void readComments()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Comments").child(postUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentsList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Comments comments=ds.getValue(Comments.class);
                    commentsList.add(comments);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }
    private void updateToken(String token)
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1=new Token(token);
        databaseReference.child(FirebaseAuth.getInstance().getUid()).setValue(token1);
    }

    private void getInfoPublisher()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(publisherUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                if(!user.getImageId().equals("empty")){
                    Picasso.get().load(user.getImageId()).into(img_profile_publisher);
                }
                else
                {
                    img_profile_publisher.setImageResource(R.mipmap.ic_launcher_round);
                }
                text_username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference databaseReference1=FirebaseDatabase.getInstance().getReference("Posts").child(postUI);
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post=snapshot.getValue(Post.class);
                text_desc.setText(post.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getImageProfile()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                if(!user.getImageId().equals("empty")){
                    Picasso.get().load(user.getImageId()).into(img_profile);
                }
                else
                {
                    img_profile.setImageResource(R.mipmap.ic_launcher_round);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}