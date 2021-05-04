package com.example.snapship.Adapters;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapship.FollowersActivity;
import com.example.snapship.Fragments.CommentsActivity;
import com.example.snapship.Fragments.ProfileFragment;
import com.example.snapship.MainActivity;
import com.example.snapship.R;
import com.example.snapship.Uttils.Constans;
import com.example.snapship.Uttils.Notifications;
import com.example.snapship.Uttils.Post;
import com.example.snapship.Uttils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolderData> {
    private Context context;
    private List<Post> postList;
    private FirebaseAuth auth;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        auth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.menu_item,parent,false);
        return new ViewHolderData(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolderData holder, int position) {
        holder.setData(postList.get(position));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
         ImageButton b_like,b_com,b_save,b_more;
         CircleImageView img_profile;
         ImageView img_post;
         TextView text_like,text_desc,text_com,text_username,text_user;
        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            img_post=itemView.findViewById(R.id.img_post_main);
            text_com=itemView.findViewById(R.id.text_comments);
            text_like=itemView.findViewById(R.id.text_likes);
            text_desc=itemView.findViewById(R.id.text_desc);
            text_username=itemView.findViewById(R.id.text_username_main);
            img_profile=itemView.findViewById(R.id.img_username);
            b_like=itemView.findViewById(R.id.img_like_b);
            b_com=itemView.findViewById(R.id.img_comments_b);
            b_save=itemView.findViewById(R.id.img_safe_b);
            text_user=itemView.findViewById(R.id.text_user);
            b_more=itemView.findViewById(R.id.post_img_more);
        }

        private void isLiked(String postKey)
        {
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Likes").child(postKey);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(auth.getUid()).exists())
                    {
                        b_like.setImageResource(R.drawable.ic_isliked);
                        b_like.setTag("Понравилось");
                    }
                    else{
                        b_like.setImageResource(R.drawable.ic_like_b);
                        b_like.setTag("Нравится");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        private void addNotification(String postKey,String userUI)
        {
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Notifications").child(userUI);
            Notifications notifications=new Notifications();
            String key=databaseReference.push().getKey();
            notifications.setPostkey(postKey);
            notifications.setText("понравилась ваша публикация.");
            notifications.setUserui(auth.getUid());
            notifications.setIspost(true);
            notifications.setKey(key);
            databaseReference.child(key).setValue(notifications);
        }

        private void setData(Post post)
        {
            if(post.getPublisher().equals(auth.getUid()))
                b_more.setVisibility(View.VISIBLE);
            else
                b_more.setVisibility(View.GONE);
            publisherInfo(post.getPublisher());
            Picasso.get().load(post.getImageid()).placeholder(R.drawable.holder).into(img_post);
            text_desc.setText(post.getDescription());
            isLiked(post.getKey());
            isSaved(post.getKey());
            getNumberOfCom(post.getKey(),text_com);
            numberLikes(post.getKey());
            text_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(context, FollowersActivity.class);
                    i.putExtra(Constans.FOLLOWERS,"Отметки \"Нравится\"");
                    i.putExtra(Constans.PROFILEUI,post.getKey());
                    context.startActivity(i);
                }
            });
            b_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMenu(post);
                }
            });
            img_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor=context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                    editor.putString("profileUI",post.getPublisher());
                    editor.apply();

                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();
                }
            });
            b_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(b_like.getTag().equals("Нравится"))
                    {
                        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Likes").child(post.getKey());
                        databaseReference.child(auth.getUid()).setValue(true);
                        b_like.setImageResource(R.drawable.ic_isliked);
                        b_like.setTag("Понравилось");
                        addNotification(post.getKey(),post.getPublisher());
                    }
                    else {
                        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Likes").child(post.getKey());
                        databaseReference.child(auth.getUid()).removeValue();
                        b_like.setImageResource(R.drawable.ic_like_b);
                        b_like.setTag("Нравится");
                    }
                }
            });
            text_com.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(context, CommentsActivity.class);
                    i.putExtra("postUI",post.getKey());
                    i.putExtra("publisherUI",post.getPublisher());
                    context.startActivity(i);
                }
            });
            b_com.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(context, CommentsActivity.class);
                    i.putExtra("postUI",post.getKey());
                    i.putExtra("publisherUI",post.getPublisher());
                    context.startActivity(i);
                }
            });
            b_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(b_save.getTag().equals("Save"))
                    {
                        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Save").child(auth.getUid());
                        databaseReference.child(post.getKey()).setValue(true);
                        b_save.setTag("Saved");
                        b_save.setImageResource(R.drawable.ic_save_f);
                    }
                    else
                    {
                        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Save").child(auth.getUid());
                        databaseReference.child(post.getKey()).removeValue();
                        b_save.setImageResource(R.drawable.ic_save);
                        b_save.setTag("Save");
                    }
                }
            });
        }


        private void isSaved(String postKey)
        {
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Save").child(auth.getUid());
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(postKey).exists())
                    {
                        b_save.setImageResource(R.drawable.ic_save_f);
                        b_save.setTag("Saved");
                    }
                    else
                    {
                        b_save.setImageResource(R.drawable.ic_save);
                        b_save.setTag("Save");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        private void numberLikes(String postKey)
        {
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Likes").child(postKey);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    text_like.setText( snapshot.getChildrenCount()+" likes");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void publisherInfo(String userUI)
        {

            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Users").child(userUI);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user=snapshot.getValue(User.class);
                    text_username.setText(user.getUsername());
                    if(user.getImageId().equals("empty"))
                        img_profile.setImageResource(R.mipmap.ic_launcher_round);
                    else
                        Picasso.get().load(user.getImageId()).into(img_profile);
                    text_user.setText(user.getUsername());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        private void getNumberOfCom(String postKey,TextView text)
        {
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Comments").child(postKey);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    text.setText("Показать все комментарии("+snapshot.getChildrenCount()+")");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void showMenu(Post post)
        {
            PopupMenu popupMenu=new PopupMenu(context,b_more);
            popupMenu.inflate(R.menu.menu_post);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id=item.getItemId();
                    if(id==R.id.edit)
                    {
                        editPost(post.getKey());
                    }
                    else if(id==R.id.delete)
                    {
                        alertDelete(post);

                    }
                    return true;
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                popupMenu.setForceShowIcon(true);
            }
            popupMenu.show();
        }
        private void deleteImage(Post post)
        {
            StorageReference storageReference= FirebaseStorage.getInstance().getReferenceFromUrl(post.getImageid());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    deletePost(post,getAdapterPosition());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Не удалось удалить картинку.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        private void deletePost(Post post,int position)
        {
            String key=post.getKey();
            deleteNotification(key,post.getPublisher());
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Posts").child(post.getKey());
            databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(context, "Публикация была успешно удалена!", Toast.LENGTH_SHORT).show();
                        postList.remove(position);
                        notifyItemRemoved(position);
                        Intent i=new Intent(context, MainActivity.class);
                        context.startActivity(i);
                    }
                    else
                    {
                        Toast.makeText(context, "Что-то пошло не так...", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        private void  deleteNotification(String key,String userUI)
        {
            List<Notifications >  notificationsList=new ArrayList<>();
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Notifications").child(userUI);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds:snapshot.getChildren())
                    {
                        Notifications notifications=ds.getValue(Notifications.class);
                        if(notifications.getPostkey().equals(key))
                            notificationsList.add(notifications);
                    }
                    for (Notifications notifications:notificationsList)
                    {
                        DatabaseReference databaseReference1=FirebaseDatabase.getInstance().getReference("Notifications").child(userUI);
                        databaseReference1.child(notifications.getKey()).removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        private void  alertDelete(Post post)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            builder.setTitle(R.string.delete);
            builder.setMessage(R.string.alert_delete);
            builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteImage(post);
                }
            });
            builder.show();
        }

        private void editPost(String postKey)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            builder.setTitle(R.string.edit_post);

            EditText editText=new EditText(context);
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
            editText.setLayoutParams(layoutParams);
            builder.setView(editText);
            getText(postKey,editText);
            builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("description",editText.getText().toString());
                    FirebaseDatabase.getInstance().getReference("Posts").child(postKey).updateChildren(hashMap);
                }
            });
            builder.setNegativeButton("Прекратить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }
        private void getText(String postKey,EditText text)
        {
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Posts").child(postKey);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    text.setText(snapshot.getValue(Post.class).getDescription());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }



    }
    public void updateAdapter(List<Post> posts)
    {
        postList.clear();
        postList.addAll(posts);
        notifyDataSetChanged();
    }
}
