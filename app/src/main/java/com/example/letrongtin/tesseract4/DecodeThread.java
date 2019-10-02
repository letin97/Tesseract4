package com.example.letrongtin.tesseract4;

import android.os.Handler;
import android.os.Looper;

import com.example.letrongtin.tesseract4.activity.CaptureActivity;

import java.util.concurrent.CountDownLatch;

class DecodeThread extends Thread {

    private final CaptureActivity activity;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    DecodeThread(CaptureActivity activity) {
        this.activity = activity;
        handlerInitLatch = new CountDownLatch(1);
    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(activity);
        handlerInitLatch.countDown();
        Looper.loop();
    }
}
