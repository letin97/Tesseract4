package com.example.letrongtin.tesseract4.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.letrongtin.tesseract4.MusicManager;
import com.example.letrongtin.tesseract4.R;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{

    public static final boolean DEFAULT_TOGGLE_MUSIC = true;

    LinearLayout btnStart, btnSetting, btnExit;
    MusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnStart = findViewById(R.id.btn_start);
        btnSetting = findViewById(R.id.btn_setting);
        btnExit = findViewById(R.id.btn_exit);

        btnStart.setOnClickListener(this);
        btnSetting.setOnClickListener(this);
        btnExit.setOnClickListener(this);

        musicManager = new MusicManager(this);
        musicManager.playMusic();
    }

    @Override
    public void onClick(View view) {
        Intent intent;

        musicManager.pauseMusic();

        switch (view.getId()) {
            case R.id.btn_start:
                intent = new Intent(this, ARActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.btn_setting:
                intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_exit:
                finish();
                break;
            default:
                    break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        musicManager.updatePrefs();
        musicManager.playMusic();
    }
}