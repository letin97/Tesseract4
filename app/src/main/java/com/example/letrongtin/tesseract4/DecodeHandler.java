package com.example.letrongtin.tesseract4;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.letrongtin.tesseract4.activity.CaptureActivity;
import com.example.letrongtin.tesseract4.view.ViewfinderView;
import com.googlecode.leptonica.android.Pixa;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class DecodeHandler extends Handler {
    private static String TAG = DecodeHandler.class.getSimpleName();
    private final CaptureActivity activity;
    private boolean running = true;
    private final TessBaseAPI baseApi;
    private BeepManager beepManager;
    private Bitmap bitmap;
    private static boolean isDecodePending;
    private long timeRequired;

    public Mat imageMat;
    public Mat imageMat2;


    DecodeHandler(CaptureActivity activity) {
        this.activity = activity;
        baseApi = activity.getBaseApi();
        beepManager = new BeepManager(activity);
        beepManager.updatePrefs();
        imageMat = activity.imageMat;
        imageMat2 = activity.imageMat2;
    }


    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case R.id.ocr_continuous_decode:
                // Only request a decode if a request is not already pending.
                if (!isDecodePending) {
                    isDecodePending = true;
                    ocrContinuousDecode((byte[]) message.obj, message.arg1, message.arg2);
                }
                break;
            case R.id.ocr_decode:
                ocrDecode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case R.id.quit:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    public static void resetDecodeState() {
        isDecodePending = false;
    }

    /**
     *  Launch an AsyncTask to perform an OCR decode for single-shot mode.
     *
     * @param data Image data
     * @param width Image width
     * @param height Image height
     */
    private void ocrDecode(byte[] data, int width, int height) {
        beepManager.playBeepSoundAndVibrate();
        activity.displayProgressDialog();

        // Launch OCR asynchronously, so we get the dialog box displayed immediately
        new OcrRecognizeAsyncTask(activity, baseApi, data, width, height).execute();
    }

    /**
     *  Perform an OCR decode for realtime recognition mode.
     *
     * @param data Image data
     * @param width Image width
     * @param height Image height
     */
    private void ocrContinuousDecode(byte[] data, int width, int height) {
        PlanarYUVLuminanceSource source = activity.getCameraManager().buildLuminanceSource(data, width, height);
        if (source == null) {
            sendContinuousOcrFailMessage();
            return;
        }
        bitmap = source.renderCroppedGreyscaleBitmap();

        OcrResult ocrResult = getOcrResult();
        Handler handler = (Handler) activity.getHandler();
        if (handler == null) {
            return;
        }

        if (ocrResult == null) {
            try {
                sendContinuousOcrFailMessage();
            } catch (NullPointerException e) {
                activity.stopHandler();
            } finally {
                bitmap.recycle();
                baseApi.clear();
            }
            return;
        }

        try {
            Message message = Message.obtain(handler, R.id.ocr_continuous_decode_succeeded, ocrResult);
            message.sendToTarget();
        } catch (NullPointerException e) {
            activity.stopHandler();
        } finally {
            baseApi.clear();
        }
    }

    @SuppressWarnings("unused")
    private OcrResult getOcrResult() {
        OcrResult ocrResult;
        String textResult;
        long start = System.currentTimeMillis();

        try {
//            Utils.bitmapToMat(bitmap, imageMat);
//            Bitmap a = detectText(imageMat);
//            baseApi.setImage(ReadFile.readBitmap(a));
//            textResult = baseApi.getUTF8Text();
            //baseApi.setImage(ReadFile.readBitmap(bitmap));
//            textResult = getOcrOfBitmap();
            //Utils.bitmapToMat(bitmap, imageMat);
            Utils.bitmapToMat(bitmap, imageMat);
            detectText(imageMat);
            textResult = baseApi.getUTF8Text();

            timeRequired = System.currentTimeMillis() - start;

            // Check for failure to recognize text
            if (textResult == null || textResult.equals("")) {
                return null;
            }
            ocrResult = new OcrResult();
            ocrResult.setWordConfidences(baseApi.wordConfidences());
            ocrResult.setMeanConfidence( baseApi.meanConfidence());
            if (ViewfinderView.DRAW_REGION_BOXES) {
                Pixa regions = baseApi.getRegions();
                ocrResult.setRegionBoundingBoxes(regions.getBoxRects());
                regions.recycle();
            }
            if (ViewfinderView.DRAW_TEXTLINE_BOXES) {
                Pixa textlines = baseApi.getTextlines();
                ocrResult.setTextlineBoundingBoxes(textlines.getBoxRects());
                textlines.recycle();
            }
            if (ViewfinderView.DRAW_STRIP_BOXES) {
                Pixa strips = baseApi.getStrips();
                ocrResult.setStripBoundingBoxes(strips.getBoxRects());
                strips.recycle();
            }

            // Always get the word bounding boxes--we want it for annotating the bitmap after the user
            // presses the shutter button, in addition to maybe wanting to draw boxes/words during the
            // continuous mode recognition.
            Pixa words = baseApi.getWords();
            ocrResult.setWordBoundingBoxes(words.getBoxRects());
            words.recycle();

//      if (ViewfinderView.DRAW_CHARACTER_BOXES || ViewfinderView.DRAW_CHARACTER_TEXT) {
//        ocrResult.setCharacterBoundingBoxes(baseApi.getCharacters().getBoxRects());
//      }
        } catch (RuntimeException e) {
            Log.e("OcrRecognizeAsyncTask", "Caught RuntimeException in request to Tesseract. Setting state to CONTINUOUS_STOPPED.");
            e.printStackTrace();
            try {
                baseApi.clear();
                activity.stopHandler();
            } catch (NullPointerException e1) {
                // Continue
            }
            return null;
        }
        timeRequired = System.currentTimeMillis() - start;
        ocrResult.setBitmap(bitmap);
        ocrResult.setText(textResult);
        ocrResult.setRecognitionTimeRequired(timeRequired);
        return ocrResult;
    }

    private void sendContinuousOcrFailMessage() {
        Handler handler = activity.getHandler();
        if (handler != null) {
            Message message = Message.obtain(handler, R.id.ocr_continuous_decode_failed, new OcrResultFailure(timeRequired));
            message.sendToTarget();
        }
    }


    private void detectText(Mat mat){
        Imgproc.cvtColor(imageMat, imageMat2, Imgproc.COLOR_RGB2GRAY);
        Mat mRgba = mat;
        Mat mGray = imageMat2;

        Scalar CONTOUR_COLOR = new Scalar(1, 255, 128, 0);
        MatOfKeyPoint keyPoint = new MatOfKeyPoint();
        List<KeyPoint> listPoint = new ArrayList<>();
        KeyPoint kPoint = new KeyPoint();
        Mat mask = Mat.zeros(mGray.size(), CvType.CV_8UC1);
        int rectanx1;
        int rectany1;
        int rectanx2;
        int rectany2;

        Scalar zeros = new Scalar(0,0,0);
        List<MatOfPoint> contour2 = new ArrayList<>();
        Mat kernel = new Mat(1, 50, CvType.CV_8UC1, Scalar.all(255));
        Mat morByte = new Mat();
        Mat hierarchy = new Mat();

        Rect rectan3 = new Rect();
        int imgSize = mRgba.height() * mRgba.width();

        if(true){
            FeatureDetector detector = FeatureDetector.create(FeatureDetector.MSER);
            detector.detect(mGray, keyPoint);
            listPoint = keyPoint.toList();
            for(int ind = 0; ind < listPoint.size(); ++ind){
                kPoint = listPoint.get(ind);
                rectanx1 = (int ) (kPoint.pt.x - 0.5 * kPoint.size);
                rectany1 = (int ) (kPoint.pt.y - 0.5 * kPoint.size);

                rectanx2 = (int) (kPoint.size);
                rectany2 = (int) (kPoint.size);
                if(rectanx1 <= 0){
                    rectanx1 = 1;
                }
                if(rectany1 <= 0){
                    rectany1 = 1;
                }
                if((rectanx1 + rectanx2) > mGray.width()){
                    rectanx2 = mGray.width() - rectanx1;
                }
                if((rectany1 + rectany2) > mGray.height()){
                    rectany2 = mGray.height() - rectany1;
                }
                Rect rectant = new Rect(rectanx1, rectany1, rectanx2, rectany2);
                Mat roi = new Mat(mask, rectant);
                roi.setTo(CONTOUR_COLOR);
            }
            Imgproc.morphologyEx(mask, morByte, Imgproc.MORPH_DILATE, kernel);
            Imgproc.findContours(morByte, contour2, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
            Bitmap bmp = null;
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i<contour2.size(); ++i){
                rectan3 = Imgproc.boundingRect(contour2.get(i));
                try{
                    Mat croppedPart = mGray.submat(rectan3);
                    bmp = Bitmap.createBitmap(croppedPart.width(), croppedPart.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(croppedPart,bmp);
                } catch (Exception e){
                    Log.d(TAG,"Cropped part error");
                }
                if(bmp != null){
                    baseApi.setImage(ReadFile.readBitmap(bmp));
                }
            }
        }
    }
}
