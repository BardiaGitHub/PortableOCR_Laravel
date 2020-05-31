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
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bardia.pocr.model.User;
import com.bardia.pocr.view.MainViewModel;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    Button signIn, toSignUp;

    EditText email, passwd;
    TextInputLayout emailLayout, passwdLayout;

    MainViewModel viewModel;
    User myUser;
    SharedPreferences sharedPreferences;
    String password;

    AlertDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signIn = findViewById(R.id.login);
        toSignUp = findViewById(R.id.toRegister);

        email = findViewById(R.id.emailET);
        passwd = findViewById(R.id.passwdET);

        emailLayout = findViewById(R.id.email);
        passwdLayout = findViewById(R.id.passwd);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        toSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading = loadingWindow(LoginActivity.this).show();
                Log.v("SIGN IN", "Pressed button...");
                if (email.getText().toString().isEmpty()) {
                    emailLayout.setError(getResources().getString(R.string.inputError));
                    return;
                }
                if (passwd.getText().toString().isEmpty()) {
                    passwdLayout.setError(getResources().getString(R.string.inputError));
                    return;
                }
                password = passwd.getText().toString();
                viewModel.getUser(email.getText().toString().toLowerCase().trim()).observe(LoginActivity.this, observer());
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
    }

    public AlertDialog.Builder loadingWindow(Context context) {
        LayoutInflater li = LayoutInflater.from(LoginActivity.this);
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.loadingTitle))
                .setView(layout)
                .setCancelable(false);
        return builder;
    }

    public void errorMessage() {
        new AlertDialog.Builder(LoginActivity.this)
                .setTitle(getResources().getString(R.string.errorSigningInTitle))
                .setMessage(getResources().getString(R.string.errorSigningInDesc))
                .setNeutralButton(getResources().getString(R.string.ok), null)
                .show();
    }

    Observer<ArrayList<User>> observer;

    public Observer<ArrayList<User>> observer() {
        if (observer == null) {
            observer = new Observer<ArrayList<User>>() {
                @Override
                public void onChanged(ArrayList<User> users) {
                    Log.v("OBSERVER", "response");
                    if (users != null) {
                        Log.v("OBSERVER", "users not null");
                        if (users.size() > 0) {
                            Log.v("OBSERVER", "user found");
                            myUser = users.get(0);
                            if(myUser.getPassword().equals(password)) {
                                Log.v("OBSERVER", "user`s password correct");
                                Gson gson = new Gson();
                                String json = gson.toJson(myUser);
                                sharedPreferences.edit().putString(getResources().getString(R.string.user), json).commit();
                                loading.dismiss();
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                finish();
                            } else {
                                loading.dismiss();
                                errorMessage();
                            }
                        } else {
                            loading.dismiss();
                            errorMessage();
                        }
                    } else {
                        loading.dismiss();
                        errorMessage();
                    }
                }
            };
        }
        return observer;
    }
}
