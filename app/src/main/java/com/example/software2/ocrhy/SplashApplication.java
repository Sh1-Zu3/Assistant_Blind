package com.example.software2.ocrhy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
public class SplashApplication extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        //mat 3 tieng cho cai nay:(
        getSupportActionBar().hide();
        new Handler().postDelayed(() -> {


            Intent intent = new Intent(SplashApplication.this,  MainActivity.class);

            startActivity(intent);

            finish();
        }, 2000); //time th
    }
}