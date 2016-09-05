package com.x10host.dhanushpatel.findmymeal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

public class StartingActivity extends AppCompatActivity {

    ImageButton galleryButton, cameraButton;
    Intent i;

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

        i = new Intent(StartingActivity.this,IngredientsActivity.class);
    }

    public void photoTake (View v){
        i.putExtra("whichImageIntent","photoTake");
        startActivity(i);
    }

    public void photoChoose (View v){
        i.putExtra("whichImageIntent","photoChoose");
        startActivity(i);
    }
}
