package com.example.snapship.PushNotifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAdVZKRqQ:APA91bG5bsrScoj91f338g8ZcGT286UxlcPfIUz6_bq6YNrC11pqqBjWFb5A7LSCjfq-lhsCqtPzXGfFeZoAXWCbizuCYH7nxGMRThcbw5cyKmOhp9U6TBDYGwIt8mEjQZg6OCIlNIG5"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
