package com.bardia.pocr;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bardia.pocr.database.TextObjectEncodedDatabase;
import com.bardia.pocr.methods.Methods;
import com.bardia.pocr.model.TextObject;
import com.bardia.pocr.model.User;
import com.bardia.pocr.view.MainViewModel;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ResultActivity extends AppCompatActivity {

    ImageView imageView;
    EditText editText;
    Button uploadToDatabase, toClipboard;
    SharedPreferences sharedPreferences;
    ConstraintLayout layout;
    User myUser;


    String result = "";
    String imageToBase64;
    Bitmap bitmap;

    Boolean editedOutput = false;
    String nodeId = "";

    AlertDialog dialog;
    private Uri image_uri;
    int IMAGE_PICK_GALLERY_CODE = 2;
    int IMAGE_PICK_CAMERA_CODE = 1;

    TextObjectEncodedDatabase offlineDatabase;

    MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imageView = findViewById(R.id.imagePicked);
        editText = findViewById(R.id.textResultET);
        uploadToDatabase = findViewById(R.id.uploadBtn);
        toClipboard = findViewById(R.id.toClipboard);
        layout = findViewById(R.id.parent);

        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(getResources().getString(R.string.user), "");
        myUser = gson.fromJson(json, User.class);

        nodeId = getIntent().getStringExtra(getResources().getString(R.string.generatedKey));
        Log.v("key_for_node", nodeId);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        sharedPreferences = ResultActivity.this.getSharedPreferences(getResources().getString(R.string.packageName), Context.MODE_PRIVATE);

        offlineDatabase = TextObjectEncodedDatabase.getInstance(ResultActivity.this);

        Uri uri = Uri.parse(getIntent().getStringExtra(getResources().getString(R.string.uri)));
        imageView.setImageURI(uri);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        bitmap = bitmapDrawable.getBitmap();
        Log.v("IMAGE_SIZE", "Height: " + bitmap.getHeight() + ", Width: " + bitmap.getWidth());

        scanText();

        if (result.equals("")) {
            new AlertDialog.Builder(ResultActivity.this)
                    .setTitle(getResources().getString(R.string.textNotDetectedTitle))
                    .setMessage(getResources().getString(R.string.textNotDetectedMessage))
                    .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            optionsForScanning(ResultActivity.this).show();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), null)
                    .show();
        }

        uploadToDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateObject();
            }
        });

        toClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getResources().getString(R.string.app_name), editText.getText().toString());
                clipboard.setPrimaryClip(clip);
                Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.copiedToClipboard), Snackbar.LENGTH_LONG)
                        .setAction(getResources().getString(R.string.share), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, editText.getText().toString());
                                sendIntent.setType(getResources().getString(R.string.sendIntentType));

                                Intent shareIntent = Intent.createChooser(sendIntent, null);
                                startActivity(shareIntent);
                            }
                        }).show();
            }
        });

        /*editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                editedOutput = true;
                if (!result.equals(editText.getText().toString())) {
                    uploadToDatabase.setVisibility(View.VISIBLE);
                    uploadToDatabase.setText(getResources().getString(R.string.updateTextButton));
                }
            }
        });*/
    }

    private void scanText() {
        TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!recognizer.isOperational()) {
            Toast.makeText(this, getResources().getString(R.string.errorRecognizer), Toast.LENGTH_LONG).show();

        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = recognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                TextBlock textBlock = items.valueAt(i);
                stringBuilder.append(textBlock.getValue());
            }
            String scannedText = stringBuilder.toString().replace("\n", " ");
            editText.setText(scannedText);
            result = scannedText;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void generateObject() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String formattedDate = df.format(c);
        String scannedText = editText.getText().toString();
        TextObject objectEncoded = new TextObject(nodeId, myUser.getId(), scannedText, formattedDate);
        Log.v("OBSERVER", objectEncoded.toString());

        if(Methods.checkConnectivity(ResultActivity.this)) {
            viewModel.postText(objectEncoded).observe(ResultActivity.this, observer());
        } else {
            new InsertThread().execute(objectEncoded);
        }
    }

    public AlertDialog optionsForScanning (Context context){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.fromWhere));
        LayoutInflater li = LayoutInflater.from(ResultActivity.this);
        ConstraintLayout layout = (ConstraintLayout)li.inflate(R.layout.camera_or_gallery_dialog, null);
        LinearLayout gallery, camera;
        gallery = layout.findViewById(R.id.fromGallery);
        camera = layout.findViewById(R.id.fromCamera);
        builder.setView(layout);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("GALLERY", "pressed");
                dialog.dismiss();
                pickGallery();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("CAMERA", "pressed");
                dialog.dismiss();
                pickCamera();
            }
        });
        dialog = builder.create();
        return dialog;
    }

    private void pickGallery() {
        Log.v("IMAGE", "Gallery");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(getResources().getString(R.string.type));
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        Log.v("IMAGE", "Camera");
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, R.string.newPic);
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, R.string.app_name);

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                Log.v("IMAGE", "picked from camera");
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            } else if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                Log.v("IMAGE", "picked from gallery");
                image_uri = data.getData();
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Log.v("CropImage", "Image Cropped");
            if (resultCode == RESULT_OK) {
                Log.v("IMAGE", "Crop");
                Uri resultUri = result.getUri();
                startActivity(new Intent(ResultActivity.this, ResultActivity.class)
                        .putExtra(getResources().getString(R.string.uri), resultUri.toString())
                        .putExtra(getResources().getString(R.string.generatedKey), Methods.randomKey()));
                finish();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(ResultActivity.this, getResources().getString(R.string.errorImage), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.scanAnother) {
            optionsForScanning(ResultActivity.this).show();
            return true;
        }
        return false;
    }


    public class InsertThread extends AsyncTask<TextObject, Void, Void> {

        @Override
        protected Void doInBackground(TextObject... textObjects) {
            offlineDatabase.textObjectEncodedDAO().insertEncodedObject(textObjects[0]);
            Log.v("Insert", textObjects[0].toString());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(ResultActivity.this, getResources().getString(R.string.offlineInsert), Toast.LENGTH_LONG).show();
            super.onPostExecute(aVoid);
        }
    }

    androidx.lifecycle.Observer<String> observer;

    public androidx.lifecycle.Observer<String> observer() {
        if (observer == null) {
            observer = new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    if (s != null) {
                        Log.v("OBSERVER", s);
                        Toast.makeText(ResultActivity.this, getResources().getString(R.string.successUpdating), Toast.LENGTH_LONG).show();
                    } else {
                        Log.v("OBSERVER", "null");
                        Toast.makeText(ResultActivity.this, getResources().getString(R.string.failureUpdating), Toast.LENGTH_LONG).show();
                    }
                }
            };
        }
        return observer;
    }

}
