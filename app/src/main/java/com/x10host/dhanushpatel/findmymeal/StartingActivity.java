package com.x10host.dhanushpatel.findmymeal;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

public class StartingActivity extends AppCompatActivity {

    ImageButton galleryButton, cameraButton, settingsButton;
    private Bitmap chosenBitmap;
    Intent i;

    private static final int TAKE_PICTURE = 1;
    private static final int PICK_PHOTO = 2;
    private boolean permsGiven = false;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_starting);

        galleryButton = (ImageButton) findViewById(R.id.galleryButton);
        galleryButton.setImageResource(R.drawable.galleryimg);
        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        cameraButton.setImageResource(R.drawable.cameraimg);
        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        settingsButton.setImageResource(R.drawable.settingsicon);
    }

    public void photoTake (View v){
        if(permsGiven) {
            i = new Intent(StartingActivity.this, IngredientsActivity.class);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, TAKE_PICTURE);
        }
        else{
            checkPermissions();
        }
//        i.putExtra("whichImageIntent","photoTake");
//        startActivity(i);
    }

    public void photoChoose (View v){
        if(permsGiven) {
            i = new Intent(StartingActivity.this, IngredientsActivity.class);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_PHOTO);
        }
        else{
            checkPermissions();
        }
//        i.putExtra("whichImageIntent","photoChoose");
//        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if ((requestCode ==  TAKE_PICTURE || requestCode ==  PICK_PHOTO) && resultCode == RESULT_OK && intent != null) {

//            InputStream inputStream = null;
//            try {
//                inputStream = getContentResolver().openInputStream(intent.getData());
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            chosenBitmap = BitmapFactory.decodeStream(inputStream);
//
//            //Convert to byte array
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            chosenBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
//            byte[] byteArray = stream.toByteArray();

            Intent i2 = new Intent(StartingActivity.this,IngredientsActivity.class);
            i2.putExtra("imageUri", intent.getData().toString());
            startActivity(i2);

            //------------

        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(StartingActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            permsGiven=false;
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(StartingActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                permissionFailDialogCreate();

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(StartingActivity.this,
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
                                ActivityCompat.requestPermissions(StartingActivity.this,
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


    public void goToSettings (View v){
        i = new Intent(StartingActivity.this,SettingsActivity.class);
        startActivity(i);
    }
}
