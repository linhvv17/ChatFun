package com.example.chatfun.InterfaceApp;

import com.example.chatfun.notifications.MyRespone;
import com.example.chatfun.notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService2 {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAYc2Twf4:APA91bHBUoIYcG2lMP56y-IQH6A1GjKnbki4duK3SozXafznE2mjHauD-VwcwWyT_IukoIqeWeUmiZp3eortKb1ZJlOcUeytefLO1g6oScX9Avp6NO-JTJ82VKwYoBdF4D47Jsfy3Min"
    }
    )
    @POST("fcm/send")
    Call<MyRespone> sendNotification(@Body Sender body);
}
