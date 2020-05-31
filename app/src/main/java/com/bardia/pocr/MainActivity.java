package com.bardia.pocr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bardia.pocr.model.User;
import com.bardia.pocr.view.MainViewModel;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout layout;

    Button signUp, toSignIn;
    ImageButton infoButton;

    EditText email, passwd, repeatPasswd;
    TextInputLayout emailLayout, passwdLayout, repeatPasswdLayout;
    MainViewModel viewModel;
    User myUser;
    SharedPreferences sharedPreferences;

    AlertDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = findViewById(R.id.layout);
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(getResources().getString(R.string.user), "");
        myUser = gson.fromJson(json, User.class);

        if (myUser != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }

        signUp = findViewById(R.id.signUp);
        toSignIn = findViewById(R.id.toLogin);
        infoButton = findViewById(R.id.infoButton);

        email = findViewById(R.id.emailET);
        passwd = findViewById(R.id.passwdET);
        repeatPasswd = findViewById(R.id.passwdRepeatET);

        emailLayout = findViewById(R.id.email);
        passwdLayout = findViewById(R.id.passwd);
        repeatPasswdLayout = findViewById(R.id.passwdRepeat);

        toSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getResources().getString(R.string.whyAccount))
                        .setMessage(getResources().getString(R.string.whyAccountInfo))
                        .setNeutralButton(getResources().getString(R.string.ok), null)
                        .show();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("SIGN UP", "Pressed button...");
                if (email.getText().toString().isEmpty()) {
                    emailLayout.setError(getResources().getString(R.string.inputError));
                    return;
                }
                if (passwd.getText().toString().isEmpty()) {
                    passwdLayout.setError(getResources().getString(R.string.inputError));
                    return;
                }
                if (!passwd.getText().toString().equals(repeatPasswd.getText().toString())) {
                    passwdLayout.setError(getResources().getString(R.string.passwdNotSame));
                    repeatPasswdLayout.setError("");
                    return;
                }
                loading = loadingWindow(MainActivity.this).show();
                myUser = new User(0, email.getText().toString().toLowerCase().trim(), passwd.getText().toString());
                viewModel.postUser(myUser).observe(MainActivity.this, observer());
            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emailLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (email.getText().toString().isEmpty()) emailLayout.setError(getResources().getString(R.string.inputError));
            }
        });

        passwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwdLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (passwd.getText().toString().isEmpty()) passwdLayout.setError(getResources().getString(R.string.inputError));
            }
        });

        repeatPasswd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                repeatPasswdLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (repeatPasswd.getText().toString().isEmpty()) repeatPasswdLayout.setError(getResources().getString(R.string.inputError));
            }
        });
    }

    public AlertDialog.Builder loadingWindow(Context context) {
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.loadingTitle))
                .setView(layout)
                .setCancelable(false);
        return builder;
    }

    Observer<User> observer;

    public Observer<User> observer() {
        if (observer == null) {
            observer = new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    Log.v("OBSERVER", "response");
                    if (user != null) {
                        Log.v("OBSERVER", "user created");
                        myUser = user;
                        Log.v("USER", myUser.toString());
                        Gson gson = new Gson();
                        String json = gson.toJson(myUser);
                        sharedPreferences.edit().putString(getResources().getString(R.string.user), json).commit();
                        loading.dismiss();
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.successCreatingUser), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        loading.dismiss();
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getResources().getString(R.string.errorCreatingUserTitle))
                                .setMessage(getResources().getString(R.string.errorCreatingUserDesc))
                                .setNeutralButton(getResources().getString(R.string.ok), null)
                                .show();
                    }
                }
            };
        }
        return observer;
    }
}
