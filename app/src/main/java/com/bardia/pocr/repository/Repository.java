package com.bardia.pocr.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.bardia.pocr.Client.Client;
import com.bardia.pocr.model.TextObject;
import com.bardia.pocr.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Repository {
    public Client client;
    public final String IP = "192.168.0.24";
    Retrofit retrofit;

    private MutableLiveData<User> user_post = new MutableLiveData<>();
    private MutableLiveData<ArrayList<User>> user_get = new MutableLiveData<>();

    private MutableLiveData<ArrayList<TextObject>> text_get = new MutableLiveData<>();
    private MutableLiveData<String> text_post = new MutableLiveData<>();
    private MutableLiveData<Integer> text_delete = new MutableLiveData<>();
    private MutableLiveData<Integer> text_post_multiple = new MutableLiveData<>();

    public Repository() {
        getRetrofit();
        client = retrofit.create(Client.class);
    }

    public Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://" + IP + "/POCR_laravel/public/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private void postUser(User user) {
        Call<User> call = client.postUser(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                user_post.setValue(response.body());
                Log.v("Callback", "success");
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                user_post.setValue(null);
                Log.v("Callback", t.getLocalizedMessage());
            }
        });
    }

    private void getUser(String email) {
        Call<ArrayList<User>> call= client.getUser(email);
        call.enqueue(new Callback<ArrayList<User>>() {
            @Override
            public void onResponse(Call<ArrayList<User>> call, Response<ArrayList<User>> response) {
                user_get.setValue(response.body());
                Log.v("Callback", "success");
            }

            @Override
            public void onFailure(Call<ArrayList<User>> call, Throwable t) {
                user_get.setValue(null);
                Log.v("Callback", t.getLocalizedMessage());
            }
        });
    }

    public MutableLiveData<User> returnPostUser(User user) {
        postUser(user);
        return user_post;
    }

    public MutableLiveData<ArrayList<User>> returnGetUser(String email) {
        getUser(email);
        return user_get;
    }

    private void getTexts(Integer userid) {
        Call<ArrayList<TextObject>> call = client.getTexts(userid);
        call.enqueue(new Callback<ArrayList<TextObject>>() {
            @Override
            public void onResponse(Call<ArrayList<TextObject>> call, Response<ArrayList<TextObject>> response) {
                text_get.setValue(response.body());
                Log.v("Callback", "success");
            }

            @Override
            public void onFailure(Call<ArrayList<TextObject>> call, Throwable t) {
                text_get.setValue(null);
                Log.v("Callback", t.getLocalizedMessage());
            }
        });
    }

    private void postText(TextObject text) {
        Call<String> call = client.postText(text);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                text_post.setValue(response.body());
                Log.v("Callback", "success");
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                text_post.setValue(null);
                Log.v("Callback", t.getLocalizedMessage());
            }
        });
    }

    private void deleteText(String id){
        Call<Integer> call = client.deleteText(id);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                text_delete.setValue(response.body());
                Log.v("Callback", "success");
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                text_delete.setValue(null);
                Log.v("Callback", t.getLocalizedMessage());
            }
        });
    }

    private void postMultipleTexts(List<TextObject> objects) {
        Call<Integer> call = client.postMultipleTexts(objects);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                text_post_multiple.setValue(response.body());
                Log.v("Callback", "success");
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                text_post_multiple.setValue(null);
                Log.v("Callback", t.getLocalizedMessage());
            }
        });
    }

    public MutableLiveData<ArrayList<TextObject>> returnGetTexts(Integer userid) {
        getTexts(userid);
        return text_get;
    }

    public MutableLiveData<String> returnPostText(TextObject text) {
        postText(text);
        return text_post;
    }

    public MutableLiveData<Integer> returnDeleteText(String id) {
        deleteText(id);
        return text_delete;
    }

    public MutableLiveData<Integer> returnPostMultipleTexts(List<TextObject> objects) {
        postMultipleTexts(objects);
        return text_post_multiple;
    }

}
