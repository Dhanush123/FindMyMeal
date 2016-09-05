package com.x10host.dhanushpatel.findmymeal;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class IngredientsActivity extends AppCompatActivity {

    ClarifaiClient clarifai;
    private List<RecognitionResult> results;
    private byte[] photoBytes;
    private String tags;
    private String firstTag;
    private ArrayList<String> stringResults;
    private ArrayList<String> ingredientsSend;
    ListView listview;
    ImageView imageView;
    Button recipeFinder;
    private boolean permsGiven = false;
    private static final int TAKE_PICTURE = 1;
    private static final int PICK_PHOTO = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;
    private Bitmap chosenBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_ingredients);

        listview = (ListView) findViewById(R.id.ingredients_listView);
        imageView = (ImageView) findViewById(R.id.photoShow);
        recipeFinder = (Button) findViewById(R.id.recipeFindButton);
        recipeFinder.setVisibility(View.INVISIBLE);

        ingredientsSend = new ArrayList<>();
        stringResults = new ArrayList<>();

        Intent i = getIntent();
        String actionTake = i.getStringExtra("whichImageIntent");
        checkPermissions();
        if (permsGiven){
            if(actionTake.equals("photoTake")){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, TAKE_PICTURE);
            }
            else if(actionTake.equals("photoChoose")){
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_PHOTO);
            }
        }
        else{
            checkPermissions();
        }

        clarifai = new ClarifaiClient(Constants.APP_ID,Constants.APP_SECRET);
    }

    public void goToRecipeFinder (View v){
        if(ingredientsSend.size()>=1){
            Intent i = new Intent(IngredientsActivity.this,RecipesViewActivity.class);
            i.putStringArrayListExtra("ingredientsUse",ingredientsSend);
            startActivity(i);
        }
        else{
            Toast.makeText(IngredientsActivity.this,"No ingredients have been selected. Please choose at least 1.",Toast.LENGTH_LONG).show();
        }
    }

    private void addItemsList(){
        listview.setTextFilterEnabled(true);
        listview.setAdapter(new ArrayAdapter<>(this,R.layout.selected_ingredients_list, stringResults));
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                CheckedTextView v = (CheckedTextView) view;
                Object obj = listview.getItemAtPosition(index);
                String item = obj.toString();
                if(v.isChecked())
                {
                    if(!ingredientsSend.contains(item)){
                        ingredientsSend.add(item);
                    }
                }
                else
                {
                    if(ingredientsSend.contains(item)){
                        ingredientsSend.remove(item);
                    }
                }
            }
        });
//        Toast.makeText(IngredientsActivity.this,"ingredients: "+ingredientsSend,Toast.LENGTH_LONG).show();
    }

    private void clarifaiProcess(){

        if(results.get(0).getTags() != null){

            List<Tag> tagsFound = results.get(0).getTags();
            String tag ="";
            for(int i=0; i < tagsFound.size();i++){
                tag = tagsFound.get(i).getName();
                stringResults.add(tag);
                if (i == 0) {
                    firstTag = tag;
                    tags = tag;
                }
                else {
                    tags = tags + ", " + tag;
                }
            }
            addItemsList();
            Log.i("New photo tags are", tags);
            recipeFinder.setVisibility(View.VISIBLE);
        }
        else{
            Toast.makeText(IngredientsActivity.this,"No food items could be found. Please try again.",Toast.LENGTH_LONG).show();
            Log.i("No tags","could be found...");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if ((requestCode ==  TAKE_PICTURE || requestCode ==  PICK_PHOTO) && resultCode == RESULT_OK && intent != null) {

            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(intent.getData());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            chosenBitmap = BitmapFactory.decodeStream(inputStream);

            imageView.setImageBitmap(chosenBitmap);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            chosenBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            photoBytes = stream.toByteArray();

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    // something you know that will take a few seconds
                    results = clarifai.recognize(new RecognitionRequest(photoBytes).setModel("food-items-v0.1"));
                    return null;
                }

                @Override
                protected void onPostExecute(final Void result) {
                    // continue what you are doing...
                    clarifaiProcess();
                    Log.i("Clarifai","recognition done.");
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
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
