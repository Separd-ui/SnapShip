package com.example.snapship.PushNotifications;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken=FirebaseInstanceId.getInstance().getToken();
        if(user!=null)
        {
            updateToken(refreshToken,user);
        }
    }

    private void updateToken(String refreshToken,FirebaseUser user)
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Tokens");
        Token token=new Token(refreshToken);
        databaseReference.child(user.getUid()).setValue(token);

    }
}
