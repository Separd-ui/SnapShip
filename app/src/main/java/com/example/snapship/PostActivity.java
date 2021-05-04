package com.example.snapship;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.snapship.Uttils.Post;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;

public class PostActivity extends AppCompatActivity {
    private ImageView img_post;
    private EditText ed_desc;
    private String imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        init();
    }
    private void init()
    {
        img_post=findViewById(R.id.img_post);
        ed_desc=findViewById(R.id.ed_desc);
        imageURI="empty";

        CropImage.activity()
                .setAspectRatio(1,1)
                .start(this);
    }

    private void uploadImage()
    {
        if(!imageURI.equals("empty"))
        {
            ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("Идёт загрузка...");
            progressDialog.show();

            Bitmap bitmap=null;
            try {
                bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imageURI));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,out);
            byte[] bytes=out.toByteArray();
            StorageReference storageReference= FirebaseStorage.getInstance().getReference("Images_"+ FirebaseAuth.getInstance().getUid()).child(System.currentTimeMillis()+"_image");
            UploadTask uploadTask=storageReference.putBytes(bytes);
            Task<Uri> task=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    imageURI=task.getResult().toString();
                    savePost();
                }
            });
        }
        else
        {
            Toast.makeText(this, "Должна быть выбрана картинка.", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePost()
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Posts");
        String key=databaseReference.push().getKey();
        Post post=new Post();
        post.setDescription(ed_desc.getText().toString());
        post.setImageid(imageURI);
        post.setKey(key);
        post.setPublisher(FirebaseAuth.getInstance().getUid());

        assert key != null;
        databaseReference.child(key).setValue(post);
        Toast.makeText(this, "Загрузка объявления прошла успешно.", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onClickClose(View view) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(R.string.close);
        builder.setMessage(R.string.close_mes);
        builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode== CROP_IMAGE_ACTIVITY_REQUEST_CODE )
        {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            assert result != null;
            imageURI=result.getUri().toString();
            img_post.setImageURI(result.getUri());
        }
    }

    public void onClickAddNewPost(View view) {
        uploadImage();
    }

    public void onClickAddImage(View view) {
        CropImage.activity()
                .setAspectRatio(1,1)
                .start(this);
    }
}