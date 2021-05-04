package com.example.snapship.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapship.Fragments.PostDetailFragment;
import com.example.snapship.Fragments.ProfileFragment;
import com.example.snapship.R;
import com.example.snapship.Uttils.Notifications;
import com.example.snapship.Uttils.Post;
import com.example.snapship.Uttils.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolderData> {
    private Context context;
    private List<Notifications> notificationsList;
    private FirebaseDatabase firebaseDatabase;

    public NotificationAdapter(Context context, List<Notifications> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;
        firebaseDatabase=FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.notification_item,parent,false);
        return new ViewHolderData(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolderData holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notificationsList.get(position).isIspost())
                {
                    SharedPreferences.Editor editor=context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                    editor.putString("postKey",notificationsList.get(position).getPostkey());
                    editor.apply();

                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new PostDetailFragment()).commit();
                }
                else
                {
                    SharedPreferences.Editor editor=context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                    editor.putString("profileUI",notificationsList.get(position).getUserui());
                    editor.apply();

                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();
                }

            }
        });
        holder.setData(notificationsList.get(position));
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
        TextView text_username,text_com;
        CircleImageView img_profile;
        ImageView img_post;
        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            text_username=itemView.findViewById(R.id.not_username);
            text_com=itemView.findViewById(R.id.not_comment);
            img_post=itemView.findViewById(R.id.not_img_post);
            img_profile=itemView.findViewById(R.id.not_img_profile);
        }

        private void setData(Notifications notifications){
            getUserInfo(notifications.getUserui());
            text_com.setText(notifications.getText());
            if(notifications.isIspost()){
                img_post.setVisibility(View.VISIBLE);
                getPostImage(notifications.getPostkey());
            }
            else
                img_post.setVisibility(View.GONE);

        }
        private void getUserInfo(String userUI)
        {
            DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(userUI);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user=snapshot.getValue(User.class);
                    text_username.setText(user.getUsername());
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
        private void getPostImage(String postKey)
        {
            DatabaseReference databaseReference=firebaseDatabase.getReference("Posts").child(postKey);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Post post=snapshot.getValue(Post.class);
                    Picasso.get().load(post.getImageid()).into(img_post);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    }
}
