package com.example.photoblog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class splash extends AppCompatActivity {
private ImageView ivsplash;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ivsplash=findViewById(R.id.splash_iv);
        YoYo.with(Techniques.Shake).duration(4000).playOn(ivsplash);
        // YoYo.with(Techniques.FadeIn).duration(4000).playOn(TVSplash);
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    Intent intent=new Intent(splash.this,MainActivity.class);
                    startActivity(intent);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }




    @Override
    protected void onDestroy() {

        super.onDestroy();
        YoYo.with(Techniques.Shake).duration(4000).playOn(ivsplash);
        // YoYo.with(Techniques.FadeIn).duration(4000).playOn(TVSplash);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    Intent intent = new Intent(splash.this, MainActivity.class);
                    startActivity(intent);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


    }
