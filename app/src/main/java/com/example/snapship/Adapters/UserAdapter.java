package com.example.snapship.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapship.Fragments.ProfileFragment;
import com.example.snapship.MainActivity;
import com.example.snapship.R;
import com.example.snapship.Uttils.Constans;
import com.example.snapship.Uttils.Notifications;
import com.example.snapship.Uttils.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolderData> {
    private Context context;
    private List<User> userList;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private boolean isFragment;

    public UserAdapter(Context context, List<User> userList,boolean isFragment) {
        this.context = context;
        this.userList = userList;
        this.isFragment=isFragment;
        firebaseDatabase=FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new ViewHolderData(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolderData holder, int position) {
        holder.SetData(userList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFragment)
                {
                    SharedPreferences.Editor editor=context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                    editor.putString("profileUI",userList.get(position).getUi());
                    editor.apply();
                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();
                }
                else
                {
                    Intent i=new Intent(context, MainActivity.class);
                    i.putExtra(Constans.PROFILEUI,userList.get(position).getUi());
                    context.startActivity(i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
        TextView username,full_name;
        CircleImageView image_profile;
        Button b_follow;
        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            full_name=itemView.findViewById(R.id.text_fullname);
            b_follow=itemView.findViewById(R.id.b_follow);
            username=itemView.findViewById(R.id.text_username);
            image_profile=itemView.findViewById(R.id.img_profile);

        }

        public void SetData(User user)
        {
            firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
            if(firebaseUser.getUid().equals(user.getUi()))
            {
                b_follow.setVisibility(View.GONE);
            }
            else
            {
                b_follow.setVisibility(View.VISIBLE);
            }
            full_name.setText(user.getFullname());
            username.setText(user.getUsername());

            if(user.getImageId().startsWith("http"))
            {
                Picasso.get().load(user.getImageId()).into(image_profile);
            }
            else
            {
                image_profile.setImageResource(R.mipmap.ic_launcher);
            }
            isFollowed(user.getUi(),b_follow);
            b_follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(b_follow.getText().toString().equals("Подписаться"))
                    {
                        DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(firebaseUser.getUid()).child("following");
                        databaseReference.child(user.getUi()).setValue(true);
                        DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(user.getUi()).child("followers");
                        databaseReference1.child(firebaseUser.getUid()).setValue(true);
                        b_follow.setText(R.string.follow_b);
                        addNotificationFollow(user.getUi());
                    }
                    else
                    {
                        AlertCall(user);
                    }
                }
            });

        }
        private void addNotificationFollow(String userUI)
        {
            DatabaseReference databaseReference=firebaseDatabase.getReference("Notifications").child(userUI);
            Notifications notifications=new Notifications();
            String key=databaseReference.push().getKey();
            notifications.setText("подписался на вас.");
            notifications.setIspost(false);
            notifications.setUserui(firebaseUser.getUid());
            notifications.setPostkey("");
            notifications.setKey(key);
            databaseReference.child(key).setValue(notifications);
        }

        private void AlertCall(User user)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            builder.setTitle("Отписка от пользователя.");
            builder.setMessage("Вы уверены ,что хотите отписаться от данного пользователя?");
            builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(firebaseUser.getUid()).child("following");
                    databaseReference.child(user.getUi()).removeValue();
                    DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(user.getUi()).child("followers");
                    databaseReference1.child(firebaseUser.getUid()).removeValue();
                    b_follow.setText(R.string.follow);
                }
            });
            builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }


    }
    private  void isFollowed(String userUI,Button button)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(firebaseUser.getUid()).child("following");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(userUI).exists())
                {
                     button.setText(R.string.follow_b);
                }
                else
                {
                    button.setText(R.string.follow);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void UpdateAdapter(List<User> users)
    {
        userList.clear();
        userList.addAll(users);
        notifyDataSetChanged();
    }


}
