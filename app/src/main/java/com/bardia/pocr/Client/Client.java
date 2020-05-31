package com.bardia.pocr.Client;

import com.bardia.pocr.model.TextObject;
import com.bardia.pocr.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface Client {
    @GET("users/email/{email}")
    Call<ArrayList<User>> getUser(@Path("email") String email);

    @POST("users")
    Call<User> postUser(@Body User user);

    @GET("texts/user/{userid}")
    Call<ArrayList<TextObject>> getTexts(@Path("userid") Integer userid);

    @POST("texts")
    Call<String> postText(@Body TextObject text);

    @DELETE("texts/{id}")
    Call<Integer> deleteText(@Path("id") String id);

    @POST("texts/multiple")
    Call<Integer> postMultipleTexts(@Body List<TextObject> objects);
}
