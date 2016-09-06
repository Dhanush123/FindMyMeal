package com.x10host.dhanushpatel.findmymeal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.ArrayList;

public class RecipesViewActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_recipes_view);
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString("Android");
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebChromeClient(new WebChromeClient() {
        });

        Intent i = getIntent();
        ArrayList<String> ingredients = i.getStringArrayListExtra("ingredientsUse");

        final SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String recipesSource = (mSharedPreference.getString("recipesSource", "allrecipes"));
        loadRightSite(recipesSource, ingredients);
    }

    private void loadRightSite(String recipesSource, ArrayList<String> ingredients) {
        String url="";

        switch(recipesSource){
            case "allrecipes":
                url = "http://allrecipes.com/search/results/?ingIncl=";
                for(int x = 0; x < ingredients.size(); x++){
                    if(x < ingredients.size()-1){
                        url = url + ingredients.get(x) + ",";
                    }
                    else{
                        //for very last parameter
                        url = url + ingredients.get(ingredients.size()-1);
                    }
                }
                url = url + "&sort=re";
                break;
            case "yummly":
                url = "http://www.yummly.com/recipes?q=";
                for(int x = 0; x < ingredients.size(); x++){
                    if(x < ingredients.size()-1){
                        url = url + ingredients.get(x) + "%2C+";
                    }
                    else{
                        //for very last parameter
                        url = url + ingredients.get(ingredients.size()-1);
                    }
                }
                break;
            case "nytcooking":
                url = "http://cooking.nytimes.com/search?q=";
                for(int x = 0; x < ingredients.size(); x++){
                    if(x < ingredients.size()-1){
                        url = url + ingredients.get(x) + "%20";
                    }
                    else{
                        //for very last parameter
                        url = url + ingredients.get(ingredients.size()-1);
                    }
                }
                break;
            default:
                //shouldn't go here tho
        }
        Log.i("Loading this recipe url",url);
        webView.loadUrl(url);
    }
}

