package com.x10host.dhanushpatel.findmymeal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SettingsActivity extends AppCompatActivity {
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private Button feedbackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        feedbackButton = (Button) findViewById(R.id.feedbackButton);
        addListeners();

        final SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String recipesSourceGot = (sharedPreference.getString("recipesSource","allrecipes"));

        if(recipesSourceGot.equals("nytcooking")){
            radioGroup.check(R.id.nytcooking);
        }
        else if(recipesSourceGot.equals("yummly")){
            radioGroup.check(R.id.yummly);
        }
        else{
            radioGroup.check(R.id.allrecipes);
        }
        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myChildToolbar);
        myChildToolbar.setTitleTextColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStop(){
        super.onStop();
        savePrefs();
    }
    @Override
    public void onPause() {
        super.onPause();
        savePrefs();
    }

    @SuppressLint("LongLogTag")
    public void savePrefs(){
        int ID = radioGroup.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(ID);
        String recipesSourceTV = (String) radioButton.getText();
        String recipesSource = "";
        switch(recipesSourceTV){
            case "Allrecipes":
                recipesSource = "allrecipes";
                break;
            case "Yummly":
                recipesSource = "yummly";
                break;
            case "NYT Cooking":
                recipesSource = "nytcooking";
                break;
            default:
                //this really shouldn't happen, so dw about it
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("recipesSource", recipesSource);
        editor.commit();
        final SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String debugging = (mSharedPreference.getString("recipesSource","ERROR"));
        Log.i("Current/new recipesSource", debugging);
    }

    public void addListeners() {
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dhanush.patel@ymail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback on Find My Meal app");
                //intent.putExtra(Intent.EXTRA_TEXT, "mail body");
                startActivity(Intent.createChooser(intent, ""));
                Log.i("Feedback button", "pressed");
            }
        });
    }
}
