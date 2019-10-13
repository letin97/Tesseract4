package com.example.letrongtin.tesseract4.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.letrongtin.tesseract4.R;
import com.example.letrongtin.tesseract4.utils.PermissionUtils;

public class SplashActivity extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (!PermissionUtils.checkIsSupportedDeviceOrFinish(this)) {
            finish();
        }

        if (PermissionUtils.checkPermission(this)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent i = new Intent(SplashActivity.this, WelcomeActivity.class);
                    startActivity(i);

                    finish();
                }
            }, SPLASH_TIME_OUT);
        } else {
            PermissionUtils.requestPermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Intent i = new Intent(SplashActivity.this, WelcomeActivity.class);
                                startActivity(i);

                                finish();
                            }
                        }, SPLASH_TIME_OUT);
                    } else {
                        PermissionUtils.shouldShowRequestPermissionRationale(this);
                    }
                }
                break;
            default:
        }
    }
}
