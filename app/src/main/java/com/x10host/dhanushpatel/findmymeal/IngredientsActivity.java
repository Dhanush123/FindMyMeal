package com.x10host.dhanushpatel.findmymeal;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class IngredientsActivity extends AppCompatActivity {

    ClarifaiClient clarifai;
    private List<RecognitionResult> results;
    private byte[] photoBytes;
    ImageView imageView;
    private boolean permsGiven = false;
    private static final int TAKE_PICTURE = 1;
    private static final int PICK_PHOTO = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_ingredients);

        imageView = (ImageView) findViewById(R.id.photoShow);

        Intent i = getIntent();
        String actionTake = i.getStringExtra("whichImageIntent");
        checkPermissions();
        if (permsGiven){
            if(actionTake.equals("photoTake")){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, TAKE_PICTURE);
            }
            else if(actionTake.equals("photoMake")){

            }
        }
        else{
            checkPermissions();
        }

        clarifai = new ClarifaiClient(Constants.APP_ID,Constants.APP_SECRET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode ==  PICK_PHOTO && resultCode == RESULT_OK && intent != null) {

            Bitmap photo = (Bitmap) intent.getExtras().get("data");
            imageView.setImageBitmap(photo);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 90, stream);
            photoBytes = stream.toByteArray();

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    // something you know that will take a few seconds
                    results = clarifai.recognize(new RecognitionRequest(photoBytes));
                    return null;
                }

                @Override
                protected void onPostExecute(final Void result) {
                    // continue what you are doing...
                    clarifaiUIUpdate();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        }

        else if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK && intent != null) {
            Bitmap photo = (Bitmap) intent.getExtras().get("data");
            imageView.setImageBitmap(photo);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 90, stream);
            photoBytes = stream.toByteArray();
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(IngredientsActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            permsGiven=false;
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(IngredientsActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                permissionFailDialogCreate();

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(IngredientsActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else{
            permsGiven=true;
        }
    }

    private void permissionFailDialogCreate(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You need to allow the permission(s) for the app to interact with photos and videos on your device.")
                .setTitle("Unable to access files")
                .setCancelable(false)
                .setPositiveButton("Enable",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(IngredientsActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permissions granted.
                    permsGiven=true;
                } else {
                    permissionFailDialogCreate();
                }
                return;
            }
        }
    }

}
