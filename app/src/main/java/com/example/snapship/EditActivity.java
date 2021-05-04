package com.example.snapship;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.snapship.Account.StartActivity;
import com.example.snapship.Uttils.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;

public class EditActivity extends AppCompatActivity {
    private EditText ed_username,ed_name,ed_bio;
    private CircleImageView img_profile;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private String imageId,oldimageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        init();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
            finish();
        return true;
    }

    private void init()
    {
        Toolbar toolbar=findViewById(R.id.toolbar5);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Комментарии");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        ed_username=findViewById(R.id.edit_username);
        ed_name=findViewById(R.id.edit_name);
        ed_bio=findViewById(R.id.ed_bio);
        img_profile=findViewById(R.id.image_edit);
        imageId="empty";
        oldimageId="empty";

        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(auth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                ed_bio.setText(user.getBio());
                ed_name.setText(user.getFullname());
                ed_username.setText(user.getUsername());
                if(user.getImageId().equals("empty"))
                    img_profile.setImageResource(R.mipmap.ic_launcher_round);
                else
                {
                    Picasso.get().load(user.getImageId()).into(img_profile);
                    oldimageId=user.getImageId();
                    imageId=user.getImageId();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void uploadImage()
    {
        if(imageId.equals(oldimageId))
        {
            saveInf();
        }
        else if(!imageId.equals(oldimageId))
        {
            ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("Идёт загрузка...");
            progressDialog.show();

            Bitmap bitmap=null;
            try {
                bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imageId));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,out);
            byte[] bytes=out.toByteArray();
            StorageReference storageReference;
            if(!oldimageId.equals("empty"))
                storageReference= FirebaseStorage.getInstance().getReferenceFromUrl(oldimageId);
            else
                storageReference=FirebaseStorage.getInstance().getReference("Image_User").child("images_"+System.currentTimeMillis());
            UploadTask uploadTask=storageReference.putBytes(bytes);
            Task<Uri> task=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    imageId=task.getResult().toString();
                    saveInf();
                }
            });
        }

    }

    public void OnCickSaveInf(View view) {
        uploadImage();
    }

    public void OnClickImageChange(View view) {
        CropImage.activity()
                .setAspectRatio(1,1)
                .start(this);
    }

    public void OnClickChangeAcc(View view)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(R.string.change);
        builder.setMessage(R.string.change_mes);
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                auth.signOut();
                Intent i=new Intent(EditActivity.this, StartActivity.class);
                startActivity(i);
            }
        });
        builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

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
            imageId=result.getUri().toString();
            img_profile.setImageURI(result.getUri());
        }
    }
    private void saveInf()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(auth.getUid());
        User user=new User();
        user.setBio(ed_bio.getText().toString());
        user.setFullname(ed_name.getText().toString());
        user.setUsername(ed_username.getText().toString());
        user.setImageId(imageId);
        user.setUi(auth.getUid());

        databaseReference.setValue(user);
        finish();
    }

}