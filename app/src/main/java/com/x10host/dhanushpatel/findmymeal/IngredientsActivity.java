package com.x10host.dhanushpatel.findmymeal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(Uri.parse(getIntent().getStringExtra("imageUri")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            chosenBitmap = BitmapFactory.decodeStream(inputStream);
        processStartingActivity();

        clarifai = new ClarifaiClient(Constants.APP_ID, Constants.APP_SECRET);
    }

    public void goToRecipeFinder(View v) {
        if (ingredientsSend.size() >= 1) {
            Intent i = new Intent(IngredientsActivity.this, RecipesViewActivity.class);
            i.putStringArrayListExtra("ingredientsUse", ingredientsSend);
            startActivity(i);
        } else {
            Toast.makeText(IngredientsActivity.this, "No ingredients have been selected. Please choose at least 1.", Toast.LENGTH_LONG).show();
        }
    }

    private void addItemsList() {
        listview.setTextFilterEnabled(true);
        listview.setAdapter(new ArrayAdapter<>(this, R.layout.selected_ingredients_list, stringResults));
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
                CheckedTextView v = (CheckedTextView) view;
                Object obj = listview.getItemAtPosition(index);
                String item = obj.toString();
                if (v.isChecked()) {
                    if (!ingredientsSend.contains(item)) {
                        ingredientsSend.add(item);
                    }
                } else {
                    if (ingredientsSend.contains(item)) {
                        ingredientsSend.remove(item);
                    }
                }
            }
        });
//        Toast.makeText(IngredientsActivity.this,"ingredients: "+ingredientsSend,Toast.LENGTH_LONG).show();
    }

    private void clarifaiProcess() {

        if (results.get(0).getTags() != null) {

            List<Tag> tagsFound = results.get(0).getTags();
            String tag = "";
            for (int i = 0; i < tagsFound.size(); i++) {
                tag = tagsFound.get(i).getName();
                stringResults.add(tag);
                if (i == 0) {
                    firstTag = tag;
                    tags = tag;
                } else {
                    tags = tags + ", " + tag;
                }
            }
            addItemsList();
            Log.i("New photo tags are", tags);
            recipeFinder.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(IngredientsActivity.this, "No food items could be found. Please try again.", Toast.LENGTH_LONG).show();
            Log.i("No tags", "could be found...");
        }
    }

    private void processStartingActivity(){
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
