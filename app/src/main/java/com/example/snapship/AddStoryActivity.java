package com.example.snapship;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.snapship.Uttils.Story;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;

public class AddStoryActivity extends AppCompatActivity {
    private String imageURI="empty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        CropImage.activity()
                .setAspectRatio(9,16)
                .start(this);
    }
    private void publishStory()
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
            StorageReference storageReference= FirebaseStorage.getInstance().getReference("Story").child(System.currentTimeMillis()+"_image");
            UploadTask uploadTask=storageReference.putBytes(bytes);
            Task<Uri> task=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        imageURI=task.getResult().toString();
                        saveStory(progressDialog);
                    }
                    else{
                        Toast.makeText(AddStoryActivity.this, "Что-то пошло не так.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
        {
            Toast.makeText(this, "Нельзя загрузить пустую фотографию.", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveStory(ProgressDialog dialog)
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Story").child(FirebaseAuth.getInstance().getUid());
        String key=databaseReference.push().getKey();
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("imageid",imageURI);
        hashMap.put("userui",FirebaseAuth.getInstance().getUid());
        hashMap.put("timestart",ServerValue.TIMESTAMP);
        hashMap.put("timeend",System.currentTimeMillis()+86400000);
        hashMap.put("storykey",key);
        databaseReference.child(key).setValue(hashMap);
        dialog.dismiss();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode== CROP_IMAGE_ACTIVITY_REQUEST_CODE )
        {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            assert result != null;
            imageURI=result.getUri().toString();
            publishStory();
        }
    }
}