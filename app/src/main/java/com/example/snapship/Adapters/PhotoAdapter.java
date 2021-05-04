package com.example.snapship.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snapship.Fragments.HomeFragment;
import com.example.snapship.Fragments.PostDetailFragment;
import com.example.snapship.R;
import com.example.snapship.Uttils.Post;
import com.squareup.picasso.Picasso;

import java.util.ConcurrentModificationException;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolderData> {
    private Context context;
    private List<Post> postList;

    public PhotoAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PhotoAdapter.ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item,parent,false);
        return new ViewHolderData(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoAdapter.ViewHolderData holder, int position) {
        holder.setData(postList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor=context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("postKey",postList.get(position).getKey());
                editor.apply();
                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new PostDetailFragment()).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
        ImageView img_post;
        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            img_post=itemView.findViewById(R.id.img_adapter);
        }

        private void setData(Post post)
        {
            Picasso.get().load(post.getImageid()).into(img_post);
        }
    }
}
