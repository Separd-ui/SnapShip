package com.example.snapship.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapship.AddStoryActivity;
import com.example.snapship.R;
import com.example.snapship.StoryDisplayActivity;
import com.example.snapship.Uttils.Constans;
import com.example.snapship.Uttils.Story;
import com.example.snapship.Uttils.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryAdapter  extends RecyclerView.Adapter<StoryAdapter.ViewHolderData> {
    private Context context;
    private List<Story> storyList;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private static final int layout_one=0;
    private static final int layout_two=1;

    public StoryAdapter(Context context, List<Story> storyList) {
        this.context = context;
        this.storyList = storyList;
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
    }
    @Override
    public int getItemViewType(int position) {
        if(position==0)
        {
            return layout_one;
        }
        return layout_two;
    }
    @NonNull
    @Override
    public StoryAdapter.ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==layout_one){
            View view= LayoutInflater.from(context).inflate(R.layout.add_story_item,parent,false);
            return new ViewHolderData(view,viewType);
        }
        else
        {
            View view= LayoutInflater.from(context).inflate(R.layout.story_item,parent,false);
            return new ViewHolderData(view,viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull StoryAdapter.ViewHolderData holder, int position) {
        Story story=storyList.get(position);
        holder.setData(story,position);
        /*if(holder.getAdapterPosition()!=layout_one)
        {
            holder.seenStory(story.getUserui());
        }
        if(holder.getAdapterPosition()==layout_one)
        {
            holder.myStory(holder.add_story_text,holder.img_plus,false);
        }*/
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.getAdapterPosition()==0)
                {
                    holder.myStory(holder.add_story_text,holder.img_plus,true);
                }
                else
                {
                    Intent i=new Intent(context, StoryDisplayActivity.class);
                    i.putExtra(Constans.PROFILEUI,story.getUserui());
                    context.startActivity(i);

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
        CircleImageView img_story,img_plus,img_seen,img_add;
        TextView add_story_text,text_username;
        public ViewHolderData(@NonNull View itemView,int viewType) {
            super(itemView);
            if(viewType==0)
            {
                add_story_text=itemView.findViewById(R.id.story_add_text);
                img_add=itemView.findViewById(R.id.story_add);
                img_plus=itemView.findViewById(R.id.story_add_v);
            }
            if(viewType==1)
            {
                img_story=itemView.findViewById(R.id.story_img);
                img_seen=itemView.findViewById(R.id.story_img_seen);
                text_username=itemView.findViewById(R.id.story_username);
            }
        }
        private void setData(Story story,int position)
        {
            getUserInfo(story.getUserui(),position);
            if(position!=0){
                seenStory(story.getUserui());
            }
            else if(position==0)
            {
                myStory(add_story_text,img_plus,false);
            }

        }
        private void getUserInfo(String userUI,int pos)
        {
            DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(userUI);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user=snapshot.getValue(User.class);
                    if(pos!=0)
                    {
                        if(user.getImageId().equals("empty"))
                            img_story.setImageResource(R.mipmap.ic_launcher_round);
                        else
                            Picasso.get().load(user.getImageId()).into(img_story);
                        text_username.setText(user.getUsername());
                    }
                    else
                    {
                        if(user.getImageId().equals("empty"))
                            img_add.setImageResource(R.mipmap.ic_launcher_round);
                        else
                            Picasso.get().load(user.getImageId()).into(img_add);
                    }
                    //Picasso.get().load(user.getImageId()).into(img_story);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        private void myStory(TextView textView, ImageView imageView,boolean click)
        {
            DatabaseReference databaseReference=firebaseDatabase.getReference("Story").child(auth.getUid());
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count=0;
                    long time_current=System.currentTimeMillis();
                    for(DataSnapshot ds:snapshot.getChildren())
                    {
                        Story story=ds.getValue(Story.class);
                        if(time_current>story.getTimestart() && time_current<story.getTimeend())
                        {
                            count++;
                        }

                    }
                    if(click)
                    {
                        if(count>0)
                        {
                            AlertDialog.Builder builder=new AlertDialog.Builder(context);
                            builder.setNegativeButton("Показать историю", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i=new Intent(context, StoryDisplayActivity.class);
                                    i.putExtra(Constans.PROFILEUI,auth.getUid());
                                    context.startActivity(i);
                                }
                            });
                            builder.setPositiveButton("Добавить историю", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i=new Intent(context, AddStoryActivity.class);
                                    context.startActivity(i);
                                }
                            });
                            builder.show();
                        }
                        else
                        {
                            Intent i=new Intent(context,AddStoryActivity.class);
                            context.startActivity(i);
                        }
                    }
                    else
                    {
                        if(count>0)
                        {
                            textView.setText("Мои истории");
                            imageView.setVisibility(View.GONE);
                        }
                        else {
                            textView.setText("Добавить");
                            imageView.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void  seenStory(String userUI)
        {
            DatabaseReference databaseReference=firebaseDatabase.getReference("Story").child(userUI);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int i=0;
                    for(DataSnapshot ds:snapshot.getChildren())
                    {
                        if(!ds.child("views").child(auth.getUid()).exists() && System.currentTimeMillis()<ds.getValue(Story.class).getTimeend())
                        {
                            i++;
                        }
                    }
                    if(i>0)
                    {
                        img_story.setBorderColor(context.getResources().getColor(R.color.red));
                    }
                    else
                    {
                        img_story.setBorderColor(context.getResources().getColor(R.color.dark_white));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }






}
