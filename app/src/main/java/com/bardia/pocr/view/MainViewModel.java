package com.bardia.pocr.view;

import android.app.Application;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.bardia.pocr.model.TextObject;
import com.bardia.pocr.model.User;
import com.bardia.pocr.repository.Repository;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    Repository repository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository();
    }

    public MutableLiveData<ArrayList<User>> getUser(String email) {
        return repository.returnGetUser(email);
    }

    public MutableLiveData<User> postUser(User user) {
        return repository.returnPostUser(user);
    }


    public MutableLiveData<ArrayList<TextObject>> getTexts(Integer userid) {
        return repository.returnGetTexts(userid);
    }

    public MutableLiveData<String> postText(TextObject text) {
        return repository.returnPostText(text);
    }

    public MutableLiveData<Integer> deleteText(String id) {
        return repository.returnDeleteText(id);
    }

    public MutableLiveData<Integer> postMultipleText(List<TextObject> objects) {
        return repository.returnPostMultipleTexts(objects);
    }

}
