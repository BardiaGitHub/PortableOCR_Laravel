package com.bardia.pocr;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.google.android.material.snackbar.Snackbar;

public class AccesibilityActivity extends AppCompatActivity {

    Button tips;
    Switch uploadAutomatically;
    SharedPreferences sharedPreferences;
    ConstraintLayout parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accesibility);

        tips = findViewById(R.id.tipsButton);
        //uploadAutomatically = findViewById(R.id.uploadAutomatically);
        parent = findViewById(R.id.parentAccesibility);


        /*
        sharedPreferences = AccesibilityActivity.this.getSharedPreferences(getResources().getString(R.string.packageName), Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean(getResources().getString(R.string.updateAutomatically), false) == true) {
            uploadAutomatically.setChecked(true);
        } else {
            uploadAutomatically.setChecked(false);
        }
        */

        tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = LayoutInflater.from(AccesibilityActivity.this);
                LinearLayout layout = (LinearLayout) layoutInflater.inflate(getResources().getLayout(R.layout.help_layout_1), null);
                new AlertDialog.Builder(AccesibilityActivity.this)
                        .setTitle(getResources().getString(R.string.tipsDialogTitle))
                        .setView(layout)
                        .setNeutralButton(getResources().getString(R.string.ok), null)
                        .show();
            }
        });

        /*
        uploadAutomatically.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPreferences.edit().putBoolean(getResources().getString(R.string.updateAutomatically), b).commit();
                Log.v("SWITCH", String.valueOf(b));
                if (b) {
                    Snackbar.make(parent, getResources().getString(R.string.updateAutoOn), Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(parent, getResources().getString(R.string.updateAutoOff), Snackbar.LENGTH_LONG).show();
                }
            }
        });*/

    }
}
