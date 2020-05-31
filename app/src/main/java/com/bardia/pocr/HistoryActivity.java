package com.bardia.pocr;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bardia.pocr.adapter.HistoryRecyclerViewAdapter;
import com.bardia.pocr.model.TextObject;
import com.bardia.pocr.model.User;
import com.bardia.pocr.view.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    ArrayList<TextObject> textObjects = new ArrayList<>();
    AlertDialog loading, dialog, loading2;
    RecyclerView recyclerView;
    HistoryRecyclerViewAdapter recyclerViewAdapter;
    FloatingActionButton reload;
    SharedPreferences sharedPreferences;
    User myUser;
    public static int num;

    ImageView imageView;
    TextView textView;

    MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.historyRecycler);
        imageView = findViewById(R.id.emptyImg);
        textView = findViewById(R.id.emptyTv);
        reload = findViewById(R.id.reload);

        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(getResources().getString(R.string.user), "");
        myUser = gson.fromJson(json, User.class);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        loading = loadingWindow(HistoryActivity.this).show();
        downloadData();

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_around_center_point);
        reload.startAnimation(animation);

        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading = loadingWindow(HistoryActivity.this).show();
                downloadData();
            }
        });
    }

    public void downloadData() {
        viewModel.getTexts(myUser.getId()).observe(HistoryActivity.this, observer());
    }

    Observer<ArrayList<TextObject>> observer;

    public Observer<ArrayList<TextObject>> observer() {
        if (observer == null) {
            observer = new Observer<ArrayList<TextObject>>() {
                @Override
                public void onChanged(ArrayList<TextObject> textObjects) {
                    HistoryActivity.this.textObjects = textObjects;
                    manageData();
                }
            };
        }
        return observer;
    }

    public void manageData() {
        if (textObjects != null) {
            Log.v("RECYCLERVIEW - data", "Not null");
            recyclerViewAdapter = new HistoryRecyclerViewAdapter(textObjects, itemClickListener(), showItemDetail());
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(HistoryActivity.this);
            recyclerView.setAdapter(recyclerViewAdapter);
            recyclerView.setLayoutManager(layoutManager);
            Log.v("RECYCLERVIEW - data", "ArrayList items: " + textObjects.size()
                    + ", Adapter items: " + recyclerViewAdapter.getItemCount()
                    + ", Recyclerview items: " + recyclerView.getAdapter().getItemCount());
            if (textObjects.size() > 0) {
                Log.v("RECYCLERVIEW - data", "Containing objects");
                imageView.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                loading.dismiss();
            } else {
                Log.v("RECYCLERVIEW - data", "Empty");
                imageView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                loading.dismiss();
            }
        } else {
            Log.v("RECYCLERVIEW - data", "Null");
            imageView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            loading.dismiss();
        }
    }

    private HistoryRecyclerViewAdapter.OnItemClickListener showItemDetail() {
        HistoryRecyclerViewAdapter.OnItemClickListener listener = new HistoryRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TextObject object, int adapterPosition) {
                dialog = itemDetail(HistoryActivity.this, object, adapterPosition).show();
            }
        };
        return listener;
    }


    public AlertDialog.Builder loadingWindow(Context context) {
        LayoutInflater li = LayoutInflater.from(HistoryActivity.this);
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.loadingTitle))
                .setView(layout)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
        return builder;
    }

    public HistoryRecyclerViewAdapter.OnItemClickListener itemClickListener() {
        HistoryRecyclerViewAdapter.OnItemClickListener listener = new HistoryRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TextObject objectDecoded, int adapterPosition) {
                new AlertDialog.Builder(HistoryActivity.this)
                        .setTitle(getResources().getString(R.string.deleteItemTitle))
                        .setMessage(getResources().getString(R.string.deleteItemMessage))
                        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loading2 = loadingWindow2(HistoryActivity.this).show();
                                Log.v("RECYCLERVIEW - adapter", "pos: " + adapterPosition);
                                viewModel.deleteText(objectDecoded.id).observe(HistoryActivity.this, deleteObserver());
                                textObjects.remove(adapterPosition);
                                recyclerViewAdapter.notifyItemRemoved(adapterPosition);
                                recyclerViewAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), null)
                        .show();
            }
        };
        return listener;
    }

    androidx.lifecycle.Observer<Integer> deleteObserver;

    public androidx.lifecycle.Observer<Integer> deleteObserver() {
        if (deleteObserver == null) {
            deleteObserver = new Observer<Integer>() {
                @Override
                public void onChanged(Integer integer) {
                    try {
                        if (integer == 1) {
                            loading2.dismiss();
                            Toast.makeText(HistoryActivity.this, getResources().getString(R.string.deleteSuccess), Toast.LENGTH_LONG).show();
                            if (textObjects.size() == 0) {
                                imageView.setVisibility(View.VISIBLE);
                                textView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            loading2.dismiss();
                            Toast.makeText(HistoryActivity.this, getResources().getString(R.string.deleteFailed), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        loading2.dismiss();
                        Toast.makeText(HistoryActivity.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            };
        }
        return deleteObserver;
    }

    public AlertDialog.Builder itemDetail(Context context, TextObject objectDecoded, int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setNeutralButton(getResources().getString(R.string.ok), null);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        LinearLayout layout = (LinearLayout) layoutInflater.inflate(getResources().getLayout(R.layout.history_item_detail_layout), null);
        TextView textView = layout.findViewById(R.id.historyItemDetailText);
        Button button = layout.findViewById(R.id.historyCopyClipboard);
        Button share = layout.findViewById(R.id.historyShare);

        builder.setTitle(objectDecoded.getDate().substring(0, objectDecoded.getDate().length() - 3));
        textView.setText(objectDecoded.getText());

        builder.setView(layout);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getResources().getString(R.string.app_name), objectDecoded.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(HistoryActivity.this, getResources().getString(R.string.copiedToClipboard), Toast.LENGTH_LONG).show();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, objectDecoded.getText());
                sendIntent.setType(getResources().getString(R.string.sendIntentType));

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }
        });
        Log.v("RECYCLERVIEW - detail", objectDecoded.getId() + " pos: " + adapterPosition);
        dialog = builder.create();
        return builder;
    }

    public AlertDialog.Builder loadingWindow2(Context context) {
        LayoutInflater li = LayoutInflater.from(HistoryActivity.this);
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.loadingTitle))
                .setView(layout)
                .setCancelable(false);
        return builder;
    }
}
