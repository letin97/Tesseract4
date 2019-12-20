package com.example.letrongtin.tesseract4;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.letrongtin.tesseract4.activity.CaptureActivity;
import com.example.letrongtin.tesseract4.activity.HomeActivity;
import com.example.letrongtin.tesseract4.activity.PreferencesActivity;

import java.io.IOException;

public class MusicManager {

    private static final String TAG = MusicManager.class.getSimpleName();

    private static final float BEEP_VOLUME = 0.10f;

    private final Activity activity;
    private MediaPlayer mediaPlayer;
    private boolean playMusic;

    public MusicManager(Activity activity) {
        this.activity = activity;
        this.mediaPlayer = null;
        updatePrefs();
    }

    public void updatePrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        playMusic = prefs.getBoolean(PreferencesActivity.KEY_PLAY_MUSIC, HomeActivity.DEFAULT_TOGGLE_MUSIC);
        if (playMusic && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
            // so we now play on the music stream.
            // activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = buildMediaPlayer(activity);
        }
    }

    public void playMusic() {
        if (playMusic && mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    public void pauseMusic() {
        if (playMusic && mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void stopMusic() {
        if (playMusic && mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.prepareAsync();
        }
    }

    private static MediaPlayer buildMediaPlayer(Context activity) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // When the beep has finished playing, rewind to queue up another one.
//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            public void onCompletion(MediaPlayer player) {
//                player.stop();
//            }
//        });

        AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.boomonline);
        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            file.close();
            //mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            mediaPlayer = null;
        }
        return mediaPlayer;
    }
}
