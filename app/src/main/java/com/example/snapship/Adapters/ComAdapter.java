package com.example.snapship.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapship.R;
import com.example.snapship.Uttils.Comments;
import com.example.snapship.Uttils.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ComAdapter extends RecyclerView.Adapter<ComAdapter.ViewHolderData> {
    private List<Comments> commentsList;
    private Context context;

    public ComAdapter(List<Comments> commentsList, Context context) {
        this.commentsList = commentsList;
        this.context = context;

    }

    @NonNull
    @Override
    public ComAdapter.ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.comments_item,parent,false);
        return new ViewHolderData(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComAdapter.ViewHolderData holder, int position) {
        holder.setData(commentsList.get(position));
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
        CircleImageView img_profile;
        TextView text_username,text_com;
        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            img_profile=itemView.findViewById(R.id.img_comments_profile);
            text_username=itemView.findViewById(R.id.text_username_item);
            text_com=itemView.findViewById(R.id.text_com_item);
        }
        private void setData(Comments comments)
        {
            getInfoPublisher(comments.getPublisher());
            text_com.setText(comments.getComment());
        }
        private void getInfoPublisher(String userUI)
        {
            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Users").child(userUI);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user=snapshot.getValue(User.class);
                    if(!user.getImageId().equals("empty"))
                    {
                        Picasso.get().load(user.getImageId()).into(img_profile);
                    }
                    else
                    {
                        img_profile.setImageResource(R.mipmap.ic_launcher_round);
                    }
                    text_username.setText(user.getUsername());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    }
}
