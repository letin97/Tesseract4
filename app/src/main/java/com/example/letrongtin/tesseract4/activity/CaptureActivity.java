package com.example.letrongtin.tesseract4.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.ClipboardManager;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.letrongtin.tesseract4.BeepManager;
import com.example.letrongtin.tesseract4.CaptureActivityHandler;
import com.example.letrongtin.tesseract4.DecodeHandler;
import com.example.letrongtin.tesseract4.FinishListener;
import com.example.letrongtin.tesseract4.OcrCharacterHelper;
import com.example.letrongtin.tesseract4.OcrInitAsyncTask;
import com.example.letrongtin.tesseract4.OcrResult;
import com.example.letrongtin.tesseract4.OcrResultFailure;
import com.example.letrongtin.tesseract4.OcrResultText;
import com.example.letrongtin.tesseract4.R;
import com.example.letrongtin.tesseract4.camera.CameraManager;
import com.example.letrongtin.tesseract4.camera.ShutterButton;
import com.example.letrongtin.tesseract4.enums.LanguageNameEnum;
import com.example.letrongtin.tesseract4.language.LanguageCodeHelper;
import com.example.letrongtin.tesseract4.utils.OnSwipeTouchListener;
import com.example.letrongtin.tesseract4.view.RecyclerViewAdapter;
import com.example.letrongtin.tesseract4.view.RecyclerViewClickListener;
import com.example.letrongtin.tesseract4.view.ViewfinderView;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class CaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        ShutterButton.OnShutterButtonListener, RecyclerViewClickListener {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    // Note: These constants will be overridden by any default values defined in preferences.xml.

    /** ISO 639-3 language code indicating the default recognition language. */
    public static final String DEFAULT_SOURCE_LANGUAGE_CODE = "eng";

    /** ISO 639-1 language code indicating the default target language for translation. */
    public static final String DEFAULT_TARGET_LANGUAGE_CODE = "es";

    /** The default online machine translation service to use. */
    public static final String DEFAULT_TRANSLATOR = "Google Translate";

    /** The default OCR engine to use. */
    public static final String DEFAULT_OCR_ENGINE_MODE = "LSTM";

    /** The default page segmentation mode to use. */
    public static final String DEFAULT_PAGE_SEGMENTATION_MODE = "Auto";

    /** Whether to use autofocus by default. */
    public static final boolean DEFAULT_TOGGLE_AUTO_FOCUS = true;

    /** Whether to initially disable continuous-picture and continuous-video focus modes. */
    public static final boolean DEFAULT_DISABLE_CONTINUOUS_FOCUS = true;

    /** Whether to beep by default when the shutter button is pressed. */
    public static final boolean DEFAULT_TOGGLE_BEEP = true;

    /** Whether to initially show a looping, real-time OCR display. */
    public static final boolean DEFAULT_TOGGLE_CONTINUOUS = true;

    /** Whether to initially reverse the image returned by the camera. */
    public static final boolean DEFAULT_TOGGLE_REVERSED_IMAGE = false;

    /** Whether to enable the use of online translation services be default. */
    public static final boolean DEFAULT_TOGGLE_TRANSLATION = true;

    /** Whether the light should be initially activated by default. */
    public static final boolean DEFAULT_TOGGLE_LIGHT = false;

    /** Flag to display the real-time recognition results at the top of the scanning screen. */
    private static final boolean CONTINUOUS_DISPLAY_RECOGNIZED_TEXT = true;

    /** Flag to display recognition-related statistics on the scanning screen. */
    private static final boolean CONTINUOUS_DISPLAY_METADATA = false;

    /** Flag to enable display of the on-screen shutter button. */
    private static final boolean DISPLAY_SHUTTER_BUTTON = false;

    /** Languages for which Cube data is available. */
    public static final String[] CUBE_SUPPORTED_LANGUAGES = {
            "ara", // Arabic
            "eng", // English
            "hin" // Hindi
    };

    /** Languages that require Cube, and cannot run using Tesseract. */
    private static final String[] CUBE_REQUIRED_LANGUAGES = {
            "ara" // Arabic
    };

    /** Resource to use for data file downloads. */
    public static final String DOWNLOAD_BASE = "https://github.com/tesseract-ocr/tessdata_fast/raw/master/";

    /** Download filename for orientation and script detection (OSD) data. */
    public static final String OSD_FILENAME = "tesseract-ocr-3.01.osd.tar";

    /** Destination filename for orientation and script detection (OSD) data. */
    public static final String OSD_FILENAME_BASE = "osd.traineddata";

    /** Minimum mean confidence score necessary to not reject single-shot OCR result. Currently unused. */
    static final int MINIMUM_MEAN_CONFIDENCE = 0; // 0 means don't reject any scored results

    // Context menu
    private static final int SETTINGS_ID = Menu.FIRST;
    private static final int ABOUT_ID = Menu.FIRST + 1;

    // Options menu, for copy to clipboard
    private static final int OPTIONS_COPY_RECOGNIZED_TEXT_ID = Menu.FIRST;
    private static final int OPTIONS_COPY_TRANSLATED_TEXT_ID = Menu.FIRST + 1;
    private static final int OPTIONS_SHARE_RECOGNIZED_TEXT_ID = Menu.FIRST + 2;
    private static final int OPTIONS_SHARE_TRANSLATED_TEXT_ID = Menu.FIRST + 3;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private TextView statusViewBottom;
    private TextView statusViewTop;
    private TextView ocrResultView;
    private TextView translationView;
    private View cameraButtonView;
    private View resultView;
    private View progressView;
    private OcrResult lastResult;
    private Bitmap lastBitmap;
    private boolean hasSurface;
    private BeepManager beepManager;
    private TessBaseAPI baseApi; // Java interface for the Tesseract OCR engine
    private String sourceLanguageCodeOcr; // ISO 639-3 language code
    private String sourceLanguageReadable; // Language name, for example, "English"
    private String sourceLanguageCodeTranslation; // ISO 639-1 language code
    private String targetLanguageCodeTranslation; // ISO 639-1 language code
    private String targetLanguageReadable; // Language name, for example, "English"
    private int pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO_OSD;
    private int ocrEngineMode = TessBaseAPI.OEM_LSTM_ONLY;
    private String characterBlacklist;
    private String characterWhitelist;
    private ShutterButton shutterButton;
    private boolean isTranslationActive; // Whether we want to show translations
    private boolean isContinuousModeActive; // Whether we are doing OCR in continuous mode
    private SharedPreferences prefs;
    private OnSharedPreferenceChangeListener listener;
    private ProgressDialog dialog; // for initOcr - language download & unzip
    private ProgressDialog indeterminateDialog; // also for initOcr - init OCR engine
    private boolean isEngineReady;
    private boolean isPaused;
    private static boolean isFirstLaunch; // True if this is the first time the app is being run
    private Button closeButton, infoButton;
    private TextView notification;

    AlertDialog dialogAnimal;
    private int currentApiVersion;

    // openCV
    public Mat imageMat;
    public Mat imageMat2;

    List<String> animalAfrikaans;
    List<String> animalAlbanian;
    List<String> animalArabic;
    List<String> animalAzeri;
    List<String> animalBasque;
    List<String> animalBelarusian;
    List<String> animalBengali;
    List<String> animalBulgarian;
    List<String> animalCatalan;
    List<String> animalChinese_Sim;
    List<String> animalChinese_Tra;
    List<String> animalCroatian;
    List<String> animalCzech;
    List<String> animalDanish;
    List<String> animalDutch;
    List<String> animalEnglish;
    List<String> animalEstonian;
    List<String> animalFinnish;
    List<String> animalFrench;
    List<String> animalGalician;
    List<String> animalGerman;
    List<String> animalGreek;
    List<String> animalHebrew;
    List<String> animalHindi;
    List<String> animalHungarian;
    List<String> animalIcelandic;
    List<String> animalIndonesian;
    List<String> animalItalian;
    List<String> animalJapanese;
    List<String> animalKannada;
    List<String> animalKorean;
    List<String> animalLatvian;
    List<String> animalLithuanian;
    List<String> animalMacedonian;
    List<String> animalMalay;
    List<String> animalMalayalam;
    List<String> animalMaltese;
    List<String> animalNorwegian;
    List<String> animalPolish;
    List<String> animalPortuguese;
    List<String> animalRomanian;
    List<String> animalRussian;
    List<String> animalSerbian;
    List<String> animalSlovak;
    List<String> animalSlovenian;
    List<String> animalSpanish;
    List<String> animalSwahili;
    List<String> animalSwedish;
    List<String> animalTagalog;
    List<String> animalTamil;
    List<String> animalTelugu;
    List<String> animalThai;
    List<String> animalTurkish;
    List<String> animalUkrainian;
    List<String> animalVietnamese;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                imageMat = new Mat();
                imageMat2 = new Mat();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    public Handler getHandler() {
        return handler;
    }

    public TessBaseAPI getBaseApi() {
        return baseApi;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }


    //
    private RecyclerView recyclerView;
    private List<String> nameAnimals;
    private List<Integer> imageAnimals;
    RecyclerViewAdapter adapter;

    public int TOTAL_LIST_ITEMS = 69;
    public int NUM_ITEMS_PAGE = 6;
    private int noOfBtns;
    private int currentPage = 0;
    private Button[] btns;


    private void Buttonfooter(View v) {
        int val = TOTAL_LIST_ITEMS % NUM_ITEMS_PAGE;
        val = val == 0? 0:1;
        noOfBtns = TOTAL_LIST_ITEMS/NUM_ITEMS_PAGE + val;

        LinearLayout ll = v.findViewById(R.id.btnLay);

        btns =new Button[noOfBtns];

        for(int i=0;i<noOfBtns;i++) {
            btns[i] = new Button(this);
            btns[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
            btns[i].setText(""+(i+1));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            ll.addView(btns[i], lp);

            final int j = i;
            btns[j].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    currentPage = j;
                    loadList(j);
                    checkBtnBackGroud(j);
                }
            });
        }

    }
    /**
     * Method for Checking Button Backgrounds
     */
    private void checkBtnBackGroud(int index)  {

        for(int i=0;i<noOfBtns;i++) {
            if(i==index) {
                //btns[index].setBackgroundDrawable(getResources().getDrawable(R.drawable.custom_button));
                btns[i].setTextColor(getResources().getColor(android.R.color.white));
                btns[i].setTypeface(Typeface.DEFAULT_BOLD);
            }
            else {
                btns[i].setBackgroundColor(getResources().getColor(android.R.color.transparent));
                btns[i].setTextColor(getResources().getColor(android.R.color.black));
            }
        }

    }

    private void loadList(int number) {

        int start = number * NUM_ITEMS_PAGE;

        ArrayList<String> sortName = new ArrayList<String>();
        for(int i=start;i<(start)+NUM_ITEMS_PAGE;i++) {
            if(i<nameAnimals.size()) {
                sortName.add(nameAnimals.get(i));
            }
            else {
                break;
            }
        }

        ArrayList<Integer> sortImage = new ArrayList<Integer>();
        for(int i=start;i<(start)+NUM_ITEMS_PAGE;i++) {
            if(i<imageAnimals.size()) {
                sortImage.add(imageAnimals.get(i));
            }
            else {
                break;
            }
        }

        adapter = new RecyclerViewAdapter(sortName, sortImage, this, this);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        dialogAnimal.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        LayoutInflater factory = LayoutInflater.from(CaptureActivity.this);
        final View view = factory.inflate(R.layout.animal_item, null);
        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setImageResource(imageAnimals.get(currentPage* NUM_ITEMS_PAGE + position));
        EditText editTextName = view.findViewById(R.id.edtName);
        TextView validate = view.findViewById(R.id.validate);
        Button buttonCheckName = view.findViewById(R.id.btnCheckName);
        Button buttonQuit = view.findViewById(R.id.btnQuit);
        buttonQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogAnimal.dismiss();
            }
        });
        ImageView btnClose = view.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogAnimal.dismiss();
            }
        });
        buttonCheckName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate.setVisibility(View.VISIBLE);
                String text = editTextName.getText().toString().trim().toLowerCase();

                if (text.isEmpty()) {
                    validate.setText("You did not enter a name!!!");
                    return;
                }

                if (nameAnimals.get(currentPage * NUM_ITEMS_PAGE + position).trim().contains(text)) {
                    dialogAnimal.dismiss();
                    notification.setVisibility(View.VISIBLE);

                    String nameAnimal = "";
                    nameAnimal = animalEnglish.get(currentPage * NUM_ITEMS_PAGE + position);

                    if (!nameAnimal.isEmpty()) {
                        beepManager.playBeepSoundAndVibrate();
                        Intent intent = new Intent(getApplicationContext(), ARActivity.class);
                        intent.putExtra("RESULT_TEXT", nameAnimals.get(currentPage * NUM_ITEMS_PAGE + position));
                        intent.putExtra("ANIMAL_NAME", nameAnimal);
                        startActivity(intent);
                    } else {
                        validate.setText("Unrecognized error!!!");
                    }

                } else {
                    validate.setText("The answer is not correct!!!");
                }
            }
        });

        dialogAnimal.setContentView(view);
    }


    @SuppressLint({"ServiceCast", "ClickableViewAccessibility", "ResourceType"})
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        checkFirstLaunch();

        if (isFirstLaunch) {
            setDefaultPreferences();
        }

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.capture);

        currentApiVersion = android.os.Build.VERSION.SDK_INT;

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
               @Override
               public void onSystemUiVisibilityChange(int visibility) {
                   if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                   }
               }
            });
        }

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        cameraButtonView = findViewById(R.id.camera_button_view);
        resultView = findViewById(R.id.result_view);

        statusViewBottom = (TextView) findViewById(R.id.status_view_bottom);
        registerForContextMenu(statusViewBottom);
        statusViewTop = (TextView) findViewById(R.id.status_view_top);
        registerForContextMenu(statusViewTop);

        handler = null;
        lastResult = null;
        hasSurface = false;
        beepManager = new BeepManager(this);

        // Camera shutter button
//    if (DISPLAY_SHUTTER_BUTTON) {
//      shutterButton = (ShutterButton) findViewById(R.id.shutter_button);
//      shutterButton.setOnShutterButtonListener(this);
//    }

        ocrResultView = (TextView) findViewById(R.id.ocr_result_text_view);
        registerForContextMenu(ocrResultView);
        translationView = (TextView) findViewById(R.id.translation_text_view);
        registerForContextMenu(translationView);

        progressView = (View) findViewById(R.id.indeterminate_progress_indicator_view);

        cameraManager = new CameraManager(getApplication());
        viewfinderView.setCameraManager(cameraManager);

        //  Set listener to change the size of the viewfinder rectangle.
        viewfinderView.setOnTouchListener(new View.OnTouchListener() {
            int lastX = -1;
            int lastY = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        lastX = -1;
                        lastY = -1;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int currentX = (int) event.getX();
                        int currentY = (int) event.getY();

                        try {
                            Rect rect = cameraManager.getFramingRect();

                            final int BUFFER = 50;
                            final int BIG_BUFFER = 60;
                            if (lastX >= 0) {
                                // Adjust the size of the viewfinder rectangle. Check if the touch event occurs in the corner areas first, because the regions overlap.
                                if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                                        && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                                    // Top left corner: adjust both top and left sides
                                    cameraManager.adjustFramingRect( 2 * (lastX - currentX), 2 * (lastY - currentY));
                                    viewfinderView.removeResultText();
                                } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
                                        && ((currentY <= rect.top + BIG_BUFFER && currentY >= rect.top - BIG_BUFFER) || (lastY <= rect.top + BIG_BUFFER && lastY >= rect.top - BIG_BUFFER))) {
                                    // Top right corner: adjust both top and right sides
                                    cameraManager.adjustFramingRect( 2 * (currentX - lastX), 2 * (lastY - currentY));
                                    viewfinderView.removeResultText();
                                } else if (((currentX >= rect.left - BIG_BUFFER && currentX <= rect.left + BIG_BUFFER) || (lastX >= rect.left - BIG_BUFFER && lastX <= rect.left + BIG_BUFFER))
                                        && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                                    // Bottom left corner: adjust both bottom and left sides
                                    cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (currentY - lastY));
                                    viewfinderView.removeResultText();
                                } else if (((currentX >= rect.right - BIG_BUFFER && currentX <= rect.right + BIG_BUFFER) || (lastX >= rect.right - BIG_BUFFER && lastX <= rect.right + BIG_BUFFER))
                                        && ((currentY <= rect.bottom + BIG_BUFFER && currentY >= rect.bottom - BIG_BUFFER) || (lastY <= rect.bottom + BIG_BUFFER && lastY >= rect.bottom - BIG_BUFFER))) {
                                    // Bottom right corner: adjust both bottom and right sides
                                    cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (currentY - lastY));
                                    viewfinderView.removeResultText();
                                } else if (((currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER) || (lastX >= rect.left - BUFFER && lastX <= rect.left + BUFFER))
                                        && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                                    // Adjusting left side: event falls within BUFFER pixels of left side, and between top and bottom side limits
                                    cameraManager.adjustFramingRect(2 * (lastX - currentX), 0);
                                    viewfinderView.removeResultText();
                                } else if (((currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER) || (lastX >= rect.right - BUFFER && lastX <= rect.right + BUFFER))
                                        && ((currentY <= rect.bottom && currentY >= rect.top) || (lastY <= rect.bottom && lastY >= rect.top))) {
                                    // Adjusting right side: event falls within BUFFER pixels of right side, and between top and bottom side limits
                                    cameraManager.adjustFramingRect(2 * (currentX - lastX), 0);
                                    viewfinderView.removeResultText();
                                } else if (((currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER) || (lastY <= rect.top + BUFFER && lastY >= rect.top - BUFFER))
                                        && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                                    // Adjusting top side: event falls within BUFFER pixels of top side, and between left and right side limits
                                    cameraManager.adjustFramingRect(0, 2 * (lastY - currentY));
                                    viewfinderView.removeResultText();
                                } else if (((currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER) || (lastY <= rect.bottom + BUFFER && lastY >= rect.bottom - BUFFER))
                                        && ((currentX <= rect.right && currentX >= rect.left) || (lastX <= rect.right && lastX >= rect.left))) {
                                    // Adjusting bottom side: event falls within BUFFER pixels of bottom side, and between left and right side limits
                                    cameraManager.adjustFramingRect(0, 2 * (currentY - lastY));
                                    viewfinderView.removeResultText();
                                }
                            }
                        } catch (NullPointerException e) {
                            Log.e(TAG, "Framing rect not available", e);
                        }
                        v.invalidate();
                        lastX = currentX;
                        lastY = currentY;
                        return true;
                }
                return false;
            }
        });

        isEngineReady = false;

        notification = findViewById(R.id.notification);

        closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        animalAfrikaans = Arrays.asList(getResources().getStringArray(R.array.animal_Afrikaans));
        animalAlbanian = Arrays.asList(getResources().getStringArray(R.array.animal_Albanian));
        animalArabic = Arrays.asList(getResources().getStringArray(R.array.animal_Arabic));
        animalAzeri = Arrays.asList(getResources().getStringArray(R.array.animal_Azeri));
        animalBasque = Arrays.asList(getResources().getStringArray(R.array.animal_Basque));
        animalBelarusian = Arrays.asList(getResources().getStringArray(R.array.animal_Belarusian));
        animalBengali = Arrays.asList(getResources().getStringArray(R.array.animal_Bengali));
        animalBulgarian = Arrays.asList(getResources().getStringArray(R.array.animal_Bulgarian));
        animalCatalan = Arrays.asList(getResources().getStringArray(R.array.animal_Catalan));
        animalChinese_Sim = Arrays.asList(getResources().getStringArray(R.array.animal_Chinese_sim));
        animalChinese_Tra = Arrays.asList(getResources().getStringArray(R.array.animal_Chinese_tra));
        animalCroatian = Arrays.asList(getResources().getStringArray(R.array.animal_Croatian));
        animalCzech = Arrays.asList(getResources().getStringArray(R.array.animal_Czech));
        animalDanish = Arrays.asList(getResources().getStringArray(R.array.animal_Danish));
        animalDutch = Arrays.asList(getResources().getStringArray(R.array.animal_Dutch));
        animalEnglish = Arrays.asList(getResources().getStringArray(R.array.animal_English));
        animalEstonian = Arrays.asList(getResources().getStringArray(R.array.animal_Estonian));
        animalFinnish = Arrays.asList(getResources().getStringArray(R.array.animal_Finnish));
        animalFrench = Arrays.asList(getResources().getStringArray(R.array.animal_French));
        animalGalician = Arrays.asList(getResources().getStringArray(R.array.animal_Galician));
        animalGerman = Arrays.asList(getResources().getStringArray(R.array.animal_German));
        animalGreek = Arrays.asList(getResources().getStringArray(R.array.animal_Greek));
        animalHebrew = Arrays.asList(getResources().getStringArray(R.array.animal_Hebrew));
        animalHindi = Arrays.asList(getResources().getStringArray(R.array.animal_Hindi));
        animalHungarian = Arrays.asList(getResources().getStringArray(R.array.animal_Hungarian));
        animalIcelandic = Arrays.asList(getResources().getStringArray(R.array.animal_Icelandic));
        animalIndonesian = Arrays.asList(getResources().getStringArray(R.array.animal_Indonesian));
        animalItalian = Arrays.asList(getResources().getStringArray(R.array.animal_Italian));
        animalJapanese = Arrays.asList(getResources().getStringArray(R.array.animal_Japanese));
        animalKannada = Arrays.asList(getResources().getStringArray(R.array.animal_Kannada));
        animalKorean = Arrays.asList(getResources().getStringArray(R.array.animal_Korean));
        animalLatvian = Arrays.asList(getResources().getStringArray(R.array.animal_Latvian));
        animalLithuanian = Arrays.asList(getResources().getStringArray(R.array.animal_Lithuanian));
        animalMacedonian = Arrays.asList(getResources().getStringArray(R.array.animal_Macedonian));
        animalMalay = Arrays.asList(getResources().getStringArray(R.array.animal_Malay));
        animalMalayalam = Arrays.asList(getResources().getStringArray(R.array.animal_Malayalam));
        animalMaltese = Arrays.asList(getResources().getStringArray(R.array.animal_Maltese));
        animalNorwegian = Arrays.asList(getResources().getStringArray(R.array.animal_Norwegian));
        animalPolish = Arrays.asList(getResources().getStringArray(R.array.animal_Polish));
        animalPortuguese = Arrays.asList(getResources().getStringArray(R.array.animal_Portuguese));
        animalRomanian = Arrays.asList(getResources().getStringArray(R.array.animal_Romanian));
        animalRussian = Arrays.asList(getResources().getStringArray(R.array.animal_Russian));
        animalSerbian = Arrays.asList(getResources().getStringArray(R.array.animal_Serbian));
        animalSlovak = Arrays.asList(getResources().getStringArray(R.array.animal_Slovak));
        animalSlovenian = Arrays.asList(getResources().getStringArray(R.array.animal_Slovenian));
        animalSpanish = Arrays.asList(getResources().getStringArray(R.array.animal_Spanish));
        animalSwahili = Arrays.asList(getResources().getStringArray(R.array.animal_Swahili));
        animalSwedish = Arrays.asList(getResources().getStringArray(R.array.animal_Swedish));
        animalTagalog = Arrays.asList(getResources().getStringArray(R.array.animal_Tagalog));
        animalTamil = Arrays.asList(getResources().getStringArray(R.array.animal_Tamil));
        animalTelugu = Arrays.asList(getResources().getStringArray(R.array.animal_Telugu));
        animalThai = Arrays.asList(getResources().getStringArray(R.array.animal_Telugu));
        animalTurkish = Arrays.asList(getResources().getStringArray(R.array.animal_Turkish));
        animalUkrainian = Arrays.asList(getResources().getStringArray(R.array.animal_Ukrainian));
        animalVietnamese = Arrays.asList(getResources().getStringArray(R.array.animal_Vietnamese));

        beepManager.playBeepSoundAndVibrate();


        // pagination
        LayoutInflater factory = LayoutInflater.from(CaptureActivity.this);
        final View v = factory.inflate(R.layout.pagination, null);
        recyclerView = v.findViewById(R.id.recyclerView);

        GridLayoutManager manager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setOnTouchListener(new OnSwipeTouchListener(CaptureActivity.this) {
            @Override
            public void onSwipeLeft() {
                if (currentPage < noOfBtns - 1) {
                    currentPage += 1;
                    loadList(currentPage);
                    checkBtnBackGroud(currentPage);
                }
            }
            @Override
            public void onSwipeRight() {
                if (currentPage >= 1) {
                    currentPage -= 1;
                    loadList(currentPage);
                    checkBtnBackGroud(currentPage);
                }
            }
        });


        Buttonfooter(v);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String language = prefs.getString(PreferencesActivity.KEY_SOURCE_LANGUAGE_PREFERENCE, "eng");
        LanguageNameEnum languageName = LanguageNameEnum.getLanguageNameEnum(language);

        switch (languageName) {
            case AFRIKAANS:
                nameAnimals = animalAfrikaans;
                break;
            case ALBANIAN:
                nameAnimals = animalAlbanian;
                break;
            case ARABIC:
                nameAnimals = animalArabic;
                break;
            case AZERI:
                nameAnimals = animalAzeri;
                break;
            case BASQUE:
                nameAnimals = animalBasque;
                break;
            case BELARUSIAN:
                nameAnimals = animalBelarusian;
                break;
            case BENGALI:
                nameAnimals = animalBengali;
                break;
            case BULGARIAN:
                nameAnimals = animalBulgarian;
                break;
            case CATALAN:
                nameAnimals = animalCatalan;
                break;
            case CHINESE_SIMPLIFIED:
                nameAnimals = animalChinese_Sim;
                break;
            case CHINESET_TRADITIONAL:
                nameAnimals = animalChinese_Tra;
                break;
            case CROATIAN:
                nameAnimals = animalCroatian;
                break;
            case CZECH:
                nameAnimals = animalCzech;
                break;
            case DANISH:
                nameAnimals = animalDanish;
                break;
            case DUTCH:
                nameAnimals = animalDutch;
                break;
            case ENGLISH:
                nameAnimals = animalEnglish;
                break;
            case ESTONIAN:
                nameAnimals = animalEstonian;
                break;
            case FINNISH:
                nameAnimals = animalFinnish;
                break;
            case FRENCH:
                nameAnimals = animalFrench;
                break;
            case GALICIAN:
                nameAnimals = animalGalician;
                break;
            case GERMAN:
                nameAnimals = animalGerman;
                break;
            case GREEK:
                nameAnimals = animalGreek;
                break;
            case HEBREW:
                nameAnimals = animalHebrew;
                break;
            case HINDI:
                nameAnimals = animalHindi;
                break;
            case HUNGARIAN:
                nameAnimals = animalHungarian;
                break;
            case ICELANDIC:
                nameAnimals = animalIcelandic;
                break;
            case INDONESIAN:
                nameAnimals = animalIndonesian;
                break;
            case ITALIAN:
                nameAnimals = animalItalian;
                break;
            case JAPANESE:
                nameAnimals = animalJapanese;
                break;
            case KANNADA:
                nameAnimals = animalKannada;
                break;
            case KOREAN:
                nameAnimals = animalKorean;
                break;
            case LATVIAN:
                nameAnimals = animalLatvian;
                break;
            case LITHUANIAN:
                nameAnimals = animalLithuanian;
                break;
            case MACEDONIAN:
                nameAnimals = animalMacedonian;
                break;
            case MALAY:
                nameAnimals = animalMalay;
                break;
            case MALAYALAM:
                nameAnimals = animalMalayalam;
                break;
            case MALTESE:
                nameAnimals = animalMaltese;
                break;
            case NORWEGIAN:
                nameAnimals = animalNorwegian;
                break;
            case POLISH:
                nameAnimals = animalPolish;
                break;
            case PORTUGUESE:
                nameAnimals = animalPortuguese;
                break;
            case ROMANIAN:
                nameAnimals = animalRomanian;
                break;
            case RUSSIAN:
                nameAnimals = animalRussian;
                break;
            case SERBIAN:
                nameAnimals = animalSerbian;
                break;
            case SLOVAK:
                nameAnimals = animalSlovak;
                break;
            case SLOVENIAN:
                nameAnimals = animalSlovenian;
                break;
            case SPANISH:
                nameAnimals = animalSpanish;
                break;
            case SWAHILI:
                nameAnimals = animalSwahili;
                break;
            case SWEDISH:
                nameAnimals = animalSwedish;
                break;
            case TAGALOG:
                nameAnimals = animalTagalog;
                break;
            case TAMIL:
                nameAnimals = animalTamil;
                break;
            case TELUGU:
                nameAnimals = animalTelugu;
                break;
            case THAI:
                nameAnimals = animalThai;
                break;
            case TURKISH:
                nameAnimals = animalTurkish;
                break;
            case UKRAINIAN:
                nameAnimals = animalUkrainian;
                break;
            case VIETNAMESE:
                nameAnimals = animalVietnamese;
                break;
        }

        imageAnimals = new ArrayList<>();
        imageAnimals.add(R.drawable.animal_armadillo);
        imageAnimals.add(R.drawable.animal_bear);
        imageAnimals.add(R.drawable.animal_beaver);
        imageAnimals.add(R.drawable.animal_bee);
        imageAnimals.add(R.drawable.animal_bird);
        imageAnimals.add(R.drawable.animal_bison);
        imageAnimals.add(R.drawable.animal_butterfly);
        imageAnimals.add(R.drawable.animal_camel);
        imageAnimals.add(R.drawable.animal_cat);
        imageAnimals.add(R.drawable.animal_chicken);
        imageAnimals.add(R.drawable.animal_cow);
        imageAnimals.add(R.drawable.animal_crab);
        imageAnimals.add(R.drawable.animal_crocodile);
        imageAnimals.add(R.drawable.animal_deer);
        imageAnimals.add(R.drawable.animal_minmi);
        imageAnimals.add(R.drawable.animal_dog);
        imageAnimals.add(R.drawable.animal_dolphin);
        imageAnimals.add(R.drawable.animal_duck);
        imageAnimals.add(R.drawable.animal_elephant);
        imageAnimals.add(R.drawable.animal_ferret);
        imageAnimals.add(R.drawable.animal_fish);
        imageAnimals.add(R.drawable.animal_fox);
        imageAnimals.add(R.drawable.animal_frog);
        imageAnimals.add(R.drawable.animal_gibbon);
        imageAnimals.add(R.drawable.animal_giraffe);
        imageAnimals.add(R.drawable.animal_goat);
        imageAnimals.add(R.drawable.animal_goose);
        imageAnimals.add(R.drawable.animal_gull);
        imageAnimals.add(R.drawable.animal_hawk);
        imageAnimals.add(R.drawable.animal_hippopotamus);
        imageAnimals.add(R.drawable.animal_horse);
        imageAnimals.add(R.drawable.animal_hyena);
        imageAnimals.add(R.drawable.animal_kangaroo);
        imageAnimals.add(R.drawable.animal_kingfisher);
        imageAnimals.add(R.drawable.animal_koala);
        imageAnimals.add(R.drawable.animal_lamb);
        imageAnimals.add(R.drawable.animal_lion);
        imageAnimals.add(R.drawable.animal_lizard);
        imageAnimals.add(R.drawable.animal_mammoth);
        imageAnimals.add(R.drawable.animal_manatee);
        imageAnimals.add(R.drawable.animal_monkey);
        imageAnimals.add(R.drawable.animal_otter);
        imageAnimals.add(R.drawable.animal_panda);
        imageAnimals.add(R.drawable.animal_parot);
        imageAnimals.add(R.drawable.animal_peacock);
        imageAnimals.add(R.drawable.animal_penguin);
        imageAnimals.add(R.drawable.animal_pig);
        imageAnimals.add(R.drawable.animal_rabbit);
        imageAnimals.add(R.drawable.animal_racoon);
        imageAnimals.add(R.drawable.animal_reindeer);
        imageAnimals.add(R.drawable.animal_sea_lion);
        imageAnimals.add(R.drawable.animal_seahorse);
        imageAnimals.add(R.drawable.animal_shark);
        imageAnimals.add(R.drawable.animal_sheep);
        imageAnimals.add(R.drawable.animal_shrimp);
        imageAnimals.add(R.drawable.animal_snail);
        imageAnimals.add(R.drawable.animal_snake);
        imageAnimals.add(R.drawable.animal_squirrel);
        imageAnimals.add(R.drawable.animal_stork);
        imageAnimals.add(R.drawable.animal_swan);
        imageAnimals.add(R.drawable.animal_tapir);
        imageAnimals.add(R.drawable.animal_tiger);
        imageAnimals.add(R.drawable.animal_turtle);
        imageAnimals.add(R.drawable.animal_mouse);
        imageAnimals.add(R.drawable.animal_vulture);
        imageAnimals.add(R.drawable.animal_walrus);
        imageAnimals.add(R.drawable.animal_whale);
        imageAnimals.add(R.drawable.animal_wolf);
        imageAnimals.add(R.drawable.animal_wolverine);

        loadList(0);
        checkBtnBackGroud(0);

        AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this, R.style.DialogTheme);
        builder.setView(v);
        dialogAnimal = builder.create();
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogAnimal.dismiss();
                notification.setVisibility(View.VISIBLE);
            }
        });

        infoButton = findViewById(R.id.info_button);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (v.getParent() != null) {
                    ((ViewGroup) v.getParent()).removeView(v);
                }
                dialogAnimal.setContentView(v);
                dialogAnimal.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                dialogAnimal.show();
                dialogAnimal.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                dialogAnimal.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                notification.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set up openCV
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG,"openCV problem");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        }else{
            Log.d(TAG, "openCV initiated success");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        resetStatusView();

        String previousSourceLanguageCodeOcr = sourceLanguageCodeOcr;
        int previousOcrEngineMode = ocrEngineMode;

        retrievePreferences();

        // Set up the camera preview surface.
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        surfaceHolder = surfaceView.getHolder();
        if (!hasSurface) {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        // Comment out the following block to test non-OCR functions without an SD card

        // Do OCR engine initialization, if necessary
        boolean doNewInit = (baseApi == null) || !sourceLanguageCodeOcr.equals(previousSourceLanguageCodeOcr) ||
                ocrEngineMode != previousOcrEngineMode;
        if (doNewInit) {
            // Initialize the OCR engine
            File storageDirectory = getStorageDirectory();
            if (storageDirectory != null) {
                initOcrEngine(storageDirectory, sourceLanguageCodeOcr, sourceLanguageReadable);
            }
        } else {
            // We already have the engine initialized, so just start the camera.
            resumeOCR();
        }
    }

    /**
     * Method to start or restart recognition after the OCR engine has been initialized,
     * or after the app regains focus. Sets state related settings and OCR engine parameters,
     * and requests camera initialization.
     */
    public void resumeOCR() {
        Log.d(TAG, "resumeOCR()");

        // This method is called when Tesseract has already been successfully initialized, so set
        // isEngineReady = true here.
        isEngineReady = true;

        isPaused = false;

        if (handler != null) {
            handler.resetState();
        }
        if (baseApi != null) {
            baseApi.setPageSegMode(pageSegmentationMode);
            baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, characterBlacklist);
            baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, characterWhitelist);
        }

        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        }
    }

    /** Called when the shutter button is pressed in continuous mode. */
    void onShutterButtonPressContinuous() {
        isPaused = true;
        handler.stop();
        beepManager.playBeepSoundAndVibrate();
        if (lastResult != null) {
            handleOcrDecode(lastResult);
        } else {
//            Toast toast = Toast.makeText(this, "OCR failed. Please try again.", Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.TOP, 0, 0);
//            toast.show();
            resumeContinuousDecoding();
        }
    }

    /** Called to resume recognition after translation in continuous mode. */
    @SuppressWarnings("unused")
    void resumeContinuousDecoding() {
        isPaused = false;
        resetStatusView();
        setStatusViewForContinuous();
        DecodeHandler.resetDecodeState();
        handler.resetState();
        if (shutterButton != null && DISPLAY_SHUTTER_BUTTON) {
            shutterButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated()");

        if (holder == null) {
            Log.e(TAG, "surfaceCreated gave us a null surface");
        }

        // Only initialize the camera if the OCR engine is ready to go.
        if (!hasSurface && isEngineReady) {
            Log.d(TAG, "surfaceCreated(): calling initCamera()...");
            initCamera(holder);
        }
        hasSurface = true;
    }

    /** Initializes the camera and starts the handler to begin previewing. */
    private void initCamera(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "initCamera()");

        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        try {

            // Open and initialize the camera
            cameraManager.openDriver(surfaceHolder);

            // Creating the handler starts the preview, which can also throw a RuntimeException.
            handler = new CaptureActivityHandler(this, cameraManager, isContinuousModeActive);

        } catch (IOException | RuntimeException ioe) {
            showErrorMessage("Error", "Could not initialize camera. Please try restarting device.");
        } // Barcode Scanner has seen crashes in the wild of this variety:
        // java.?lang.?RuntimeException: Fail to connect to camera service

    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
        }

        // Stop using the camera, to avoid conflicting with other camera-based apps
        cameraManager.closeDriver();

        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    public void stopHandler() {
        if (handler != null) {
            handler.stop();
        }
    }

    @Override
    protected void onDestroy() {
        if (baseApi != null) {
            baseApi.end();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            // First check if we're paused in continuous mode, and if so, just unpause.
            if (isPaused) {
                Log.d(TAG, "only resuming continuous recognition, not quitting...");
                resumeContinuousDecoding();
                return true;
            }

            // Exit the app if we're not viewing an OCR result.
            if (lastResult == null) {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            } else {
                // Go back to previewing in regular OCR mode.
                resetStatusView();
                if (handler != null) {
                    handler.sendEmptyMessage(R.id.restart_preview);
                }
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            if (isContinuousModeActive) {
                onShutterButtonPressContinuous();
            } else {
                handler.hardwareShutterButtonClick();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            // Only perform autofocus if user is not holding down the button.
            if (event.getRepeatCount() == 0) {
                cameraManager.requestAutoFocus(500L);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /** Sets the necessary language code values for the given OCR language. */
    private void setSourceLanguage(String languageCode) {
        sourceLanguageCodeOcr = languageCode;
        sourceLanguageCodeTranslation = LanguageCodeHelper.mapLanguageCode(languageCode);
        sourceLanguageReadable = LanguageCodeHelper.getOcrLanguageName(this, languageCode);
    }

    /** Sets the necessary language code values for the translation target language. */
    private void setTargetLanguage(String languageCode) {
        targetLanguageCodeTranslation = languageCode;
        targetLanguageReadable = LanguageCodeHelper.getTranslationLanguageName(this, languageCode);
    }

    /** Finds the proper location on the SD card where we can save files. */
    private File getStorageDirectory() {
        //Log.d(TAG, "getStorageDirectory(): API level is " + Integer.valueOf(android.os.Build.VERSION.SDK_INT));

        String state = null;
        try {
            state = Environment.getExternalStorageState();
        } catch (RuntimeException e) {
            Log.e(TAG, "Is the SD card visible?", e);
            showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable.");
        }

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            // We can read and write the media
            //    	if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 7) {
            // For Android 2.2 and above

            try {
                return getExternalFilesDir(Environment.MEDIA_MOUNTED);
            } catch (NullPointerException e) {
                // We get an error here if the SD card is visible, but full
                Log.e(TAG, "External storage is unavailable");
                showErrorMessage("Error", "Required external storage (such as an SD card) is full or unavailable.");
            }

            //        } else {
            //          // For Android 2.1 and below, explicitly give the path as, for example,
            //          // "/mnt/sdcard/Android/data/edu.sfsu.cs.orange.ocr/files/"
            //          return new File(Environment.getExternalStorageDirectory().toString() + File.separator +
            //                  "Android" + File.separator + "data" + File.separator + getPackageName() +
            //                  File.separator + "files" + File.separator);
            //        }

        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            Log.e(TAG, "External storage is read-only");
            showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable for data storage.");
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            // to know is we can neither read nor write
            Log.e(TAG, "External storage is unavailable");
            showErrorMessage("Error", "Required external storage (such as an SD card) is unavailable or corrupted.");
        }
        return null;
    }

    /**
     * Requests initialization of the OCR engine with the given parameters.
     *
     * @param storageRoot Path to location of the tessdata directory to use
     * @param languageCode Three-letter ISO 639-3 language code for OCR
     * @param languageName Name of the language for OCR, for example, "English"
     */
    private void initOcrEngine(File storageRoot, String languageCode, String languageName) {
        isEngineReady = false;

        // Set up the dialog box for the thermometer-style download progress indicator
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);

        // If we have a language that only runs using Cube, then set the ocrEngineMode to Cube
        if (ocrEngineMode != TessBaseAPI.OEM_LSTM_ONLY) {
            for (String s : CUBE_REQUIRED_LANGUAGES) {
                if (s.equals(languageCode)) {
                    ocrEngineMode = TessBaseAPI.OEM_LSTM_ONLY;
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    prefs.edit().putString(PreferencesActivity.KEY_OCR_ENGINE_MODE, getOcrEngineModeName()).commit();
                }
            }
        }

        // If our language doesn't support Cube, then set the ocrEngineMode to Tesseract
        if (ocrEngineMode != TessBaseAPI.OEM_TESSERACT_ONLY) {
            boolean cubeOk = false;
            for (String s : CUBE_SUPPORTED_LANGUAGES) {
                if (s.equals(languageCode)) {
                    cubeOk = true;
                }
            }
            if (!cubeOk) {
                ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().putString(PreferencesActivity.KEY_OCR_ENGINE_MODE, getOcrEngineModeName()).commit();
            }
        }

        // Display the name of the OCR engine we're initializing in the indeterminate progress dialog box
        indeterminateDialog = new ProgressDialog(this, R.style.MyProgressDialogStyle);
        indeterminateDialog.setIcon(R.drawable.ic_dialog);
        indeterminateDialog.setTitle("Please wait");
        String ocrEngineModeName = getOcrEngineModeName();
        if (ocrEngineModeName.equals("Both")) {
            indeterminateDialog.setMessage("Initializing Cube and Tesseract OCR engines for " + languageName + "...");
        } else {
            indeterminateDialog.setMessage("Initializing " + ocrEngineModeName + " OCR engine for " + languageName + "...");
        }
        indeterminateDialog.setCancelable(false);
        indeterminateDialog.show();

        File tessdataDir = new File(storageRoot.toString() + File.separator + "tessdata");
        if (tessdataDir.exists()) {
            File tesseractTestFile = new File(tessdataDir, languageCode + ".traineddata");
            if (!tesseractTestFile.exists()) {
                indeterminateDialog.dismiss();
            }
        } else {
            indeterminateDialog.dismiss();
        }

        if (handler != null) {
            handler.quitSynchronously();
        }

        // Disable continuous mode if we're using Cube. This will prevent bad states for devices
        // with low memory that crash when running OCR with Cube, and prevent unwanted delays.
        if (ocrEngineMode == TessBaseAPI.OEM_DEFAULT) {
            Log.d(TAG, "Disabling continuous preview");
            isContinuousModeActive = false;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean(PreferencesActivity.KEY_CONTINUOUS_PREVIEW, false);
        }

        // Start AsyncTask to install language data and init OCR
        baseApi = new TessBaseAPI();
        new OcrInitAsyncTask(this, baseApi, dialog, indeterminateDialog, languageCode, languageName, ocrEngineMode)
                .execute(storageRoot.toString());
    }

    /**
     * Displays information relating to the result of OCR, and requests a translation if necessary.
     *
     * @param ocrResult Object representing successful OCR results
     */
    public void handleOcrDecode(OcrResult ocrResult) {
        lastResult = ocrResult;

        // Test whether the result is null
        if (ocrResult.getText() == null || ocrResult.getText().equals("")) {
//            Toast toast = Toast.makeText(this, "OCR failed. Please try again.", Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.TOP, 0, 0);
//            toast.show();
            return;
        }

        // Turn off capture-related UI elements
        shutterButton.setVisibility(View.GONE);
        statusViewBottom.setVisibility(View.GONE);
        statusViewTop.setVisibility(View.GONE);
        cameraButtonView.setVisibility(View.GONE);
        viewfinderView.setVisibility(View.GONE);
        resultView.setVisibility(View.VISIBLE);

        ImageView bitmapImageView = (ImageView) findViewById(R.id.image_view);
        lastBitmap = ocrResult.getBitmap();
        if (lastBitmap == null) {
            bitmapImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_launcher_background));
        } else {
            bitmapImageView.setImageBitmap(lastBitmap);
        }

        // Display the recognized text
        TextView sourceLanguageTextView = (TextView) findViewById(R.id.source_language_text_view);
        sourceLanguageTextView.setText(sourceLanguageReadable);
        TextView ocrResultTextView = (TextView) findViewById(R.id.ocr_result_text_view);
        ocrResultTextView.setText(ocrResult.getText());
        // Crudely scale betweeen 22 and 32 -- bigger font for shorter text
        int scaledSize = Math.max(22, 32 - ocrResult.getText().length() / 4);
        ocrResultTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);

        TextView translationLanguageLabelTextView = (TextView) findViewById(R.id.translation_language_label_text_view);
        TextView translationLanguageTextView = (TextView) findViewById(R.id.translation_language_text_view);
        TextView translationTextView = (TextView) findViewById(R.id.translation_text_view);
//    if (isTranslationActive) {
//      // Handle translation text fields
//      translationLanguageLabelTextView.setVisibility(View.VISIBLE);
//      translationLanguageTextView.setText(targetLanguageReadable);
//      translationLanguageTextView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL), Typeface.NORMAL);
//      translationLanguageTextView.setVisibility(View.VISIBLE);
//
//      // Activate/re-activate the indeterminate progress indicator
//      translationTextView.setVisibility(View.GONE);
//      progressView.setVisibility(View.VISIBLE);
//      setProgressBarVisibility(true);
//
//      // Get the translation asynchronously
//      new TranslateAsyncTask(this, sourceLanguageCodeTranslation, targetLanguageCodeTranslation,
//          ocrResult.getText()).execute();
//    } else {
//      translationLanguageLabelTextView.setVisibility(View.GONE);
//      translationLanguageTextView.setVisibility(View.GONE);
//      translationTextView.setVisibility(View.GONE);
//      progressView.setVisibility(View.GONE);
//      setProgressBarVisibility(false);
//    }
    }

    /**
     * Displays information relating to the results of a successful real-time OCR request.
     *
     * @param ocrResult Object representing successful OCR results
     */
    public void handleOcrContinuousDecode(OcrResult ocrResult) {

        lastResult = ocrResult;

        // Send an OcrResultText object to the ViewfinderView for text rendering
        viewfinderView.addResultText(new OcrResultText(ocrResult.getText(),
                ocrResult.getWordConfidences(),
                ocrResult.getMeanConfidence(),
                ocrResult.getBitmapDimensions(),
                ocrResult.getRegionBoundingBoxes(),
                ocrResult.getTextlineBoundingBoxes(),
                ocrResult.getStripBoundingBoxes(),
                ocrResult.getWordBoundingBoxes(),
                ocrResult.getCharacterBoundingBoxes()));

        int meanConfidence = ocrResult.getMeanConfidence();

        if (CONTINUOUS_DISPLAY_RECOGNIZED_TEXT) {
            // Intent ARActivity
            String resultText = ocrResult.getText().trim().toLowerCase();
            String nameAnimal = "";

            Optional<String> matchingItem;

            matchingItem = nameAnimals.stream()
                    .filter(s -> resultText.contains(s.trim().toLowerCase()))
                    .findFirst();

            if (matchingItem.isPresent()) {
                nameAnimal = animalEnglish.get(nameAnimals.indexOf(matchingItem.get()));
            }

            if (!nameAnimal.isEmpty()) {
                beepManager.playBeepSoundAndVibrate();
                Intent intent = new Intent(getApplicationContext(), ARActivity.class);
                intent.putExtra("RESULT_TEXT", resultText);
                intent.putExtra("ANIMAL_NAME", nameAnimal);
                startActivity(intent);
            }
        }

        if (prefs.getBoolean(PreferencesActivity.KEY_CONTINUOUS_DISPLAY_METADATA, false)) {

            // Display the recognized text on the screen
            statusViewTop.setText(ocrResult.getText());
            int scaledSize = Math.max(22, 32 - ocrResult.getText().length() / 4);
            statusViewTop.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);
            statusViewTop.setTextColor(Color.BLACK);
            statusViewTop.setBackgroundResource(R.color.status_top_text_background);
            statusViewTop.getBackground().setAlpha(meanConfidence * (255 / 100));

            // Display recognition-related metadata at the bottom of the screen
            long recognitionTimeRequired = ocrResult.getRecognitionTimeRequired();
            statusViewBottom.setTextSize(14);
//            statusViewBottom.setText("Language: " + sourceLanguageReadable + " - Mean confidence: " +
//                    meanConfidence + " - Time required: " + recognitionTimeRequired + " ms");
            statusViewBottom.setText("Language: " + sourceLanguageReadable);
        }
    }

    /**
     * Version of handleOcrContinuousDecode for failed OCR requests. Displays a failure message.
     *
     * @param obj Metadata for the failed OCR request.
     */
    public void handleOcrContinuousDecode(OcrResultFailure obj) {
        lastResult = null;
        viewfinderView.removeResultText();

        // Reset the text in the recognized text box.
        statusViewTop.setText("");

        if (prefs.getBoolean(PreferencesActivity.KEY_CONTINUOUS_DISPLAY_METADATA, false)) {
            // Color text delimited by '-' as red.
            statusViewBottom.setTextSize(14);
//            CharSequence cs = setSpanBetweenTokens("Language: " + sourceLanguageReadable + " - No result - Time required: "
//                    + obj.getTimeRequired() + " ms", "-", new ForegroundColorSpan(0xFFFF0000));

            statusViewBottom.setText("Language: " + sourceLanguageReadable);
        }
    }

    /**
     * Given either a Spannable String or a regular String and a token, apply
     * the given CharacterStyle to the span between the tokens.
     *
     * NOTE: This method was adapted from:
     *  http://www.androidengineer.com/2010/08/easy-method-for-formatting-android.html
     *
     * <p>
     * For example, {@code setSpanBetweenTokens("Hello ##world##!", "##", new
     * ForegroundColorSpan(0xFFFF0000));} will return a CharSequence {@code
     * "Hello world!"} with {@code world} in red.
     *
     */
    private CharSequence setSpanBetweenTokens(CharSequence text, String token,
                                              CharacterStyle... cs) {
        // Start and end refer to the points where the span will apply
        int tokenLen = token.length();
        int start = text.toString().indexOf(token) + tokenLen;
        int end = text.toString().indexOf(token, start);

        if (start > -1 && end > -1) {
            // Copy the spannable string to a mutable spannable string
            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            for (CharacterStyle c : cs)
                ssb.setSpan(c, start, end, 0);
            text = ssb;
        }
        return text;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.equals(ocrResultView)) {
            menu.add(Menu.NONE, OPTIONS_COPY_RECOGNIZED_TEXT_ID, Menu.NONE, "Copy recognized text");
            menu.add(Menu.NONE, OPTIONS_SHARE_RECOGNIZED_TEXT_ID, Menu.NONE, "Share recognized text");
        } else if (v.equals(translationView)){
            menu.add(Menu.NONE, OPTIONS_COPY_TRANSLATED_TEXT_ID, Menu.NONE, "Copy translated text");
            menu.add(Menu.NONE, OPTIONS_SHARE_TRANSLATED_TEXT_ID, Menu.NONE, "Share translated text");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        switch (item.getItemId()) {

            case OPTIONS_COPY_RECOGNIZED_TEXT_ID:
                clipboardManager.setText(ocrResultView.getText());
                if (clipboardManager.hasText()) {
//                    Toast toast = Toast.makeText(this, "Text copied.", Toast.LENGTH_LONG);
//                    toast.setGravity(Gravity.BOTTOM, 0, 0);
//                    toast.show();
                }
                return true;
            case OPTIONS_SHARE_RECOGNIZED_TEXT_ID:
                Intent shareRecognizedTextIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareRecognizedTextIntent.setType("text/plain");
                shareRecognizedTextIntent.putExtra(android.content.Intent.EXTRA_TEXT, ocrResultView.getText());
                startActivity(Intent.createChooser(shareRecognizedTextIntent, "Share via"));
                return true;
            case OPTIONS_COPY_TRANSLATED_TEXT_ID:
                clipboardManager.setText(translationView.getText());
                if (clipboardManager.hasText()) {
//                    Toast toast = Toast.makeText(this, "Text copied.", Toast.LENGTH_LONG);
//                    toast.setGravity(Gravity.BOTTOM, 0, 0);
//                    toast.show();
                }
                return true;
            case OPTIONS_SHARE_TRANSLATED_TEXT_ID:
                Intent shareTranslatedTextIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareTranslatedTextIntent.setType("text/plain");
                shareTranslatedTextIntent.putExtra(android.content.Intent.EXTRA_TEXT, translationView.getText());
                startActivity(Intent.createChooser(shareTranslatedTextIntent, "Share via"));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Resets view elements.
     */
    private void resetStatusView() {
        resultView.setVisibility(View.GONE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(PreferencesActivity.KEY_CONTINUOUS_DISPLAY_METADATA, false)) {
            statusViewBottom.setText("");
            statusViewBottom.setTextSize(14);
            statusViewBottom.setTextColor(getResources().getColor(R.color.status_text));
            statusViewBottom.setVisibility(View.VISIBLE);
        }
        if (CONTINUOUS_DISPLAY_RECOGNIZED_TEXT) {
            statusViewTop.setText("");
            statusViewTop.setTextSize(14);
            statusViewTop.setVisibility(View.VISIBLE);
        }
        viewfinderView.setVisibility(View.VISIBLE);
        cameraButtonView.setVisibility(View.VISIBLE);
        if (DISPLAY_SHUTTER_BUTTON) {
            shutterButton.setVisibility(View.VISIBLE);
        }
        lastResult = null;
        viewfinderView.removeResultText();
    }

    /** Displays a pop-up message showing the name of the current OCR source language. */
    public void showLanguageName() {
//        Toast toast = Toast.makeText(this, "Language: " + sourceLanguageReadable, Toast.LENGTH_LONG);
//        toast.setGravity(Gravity.TOP, 0, 0);
//        toast.show();
    }

    /**
     * Displays an initial message to the user while waiting for the first OCR request to be
     * completed after starting realtime OCR.
     */
    public void setStatusViewForContinuous() {
        viewfinderView.removeResultText();
        if (prefs.getBoolean(PreferencesActivity.KEY_CONTINUOUS_DISPLAY_METADATA, false)) {
            statusViewBottom.setText("Language: " + sourceLanguageReadable + " - waiting for OCR...");
        }
    }

    @SuppressWarnings("unused")
    public void setButtonVisibility(boolean visible) {
        if (shutterButton != null && visible && DISPLAY_SHUTTER_BUTTON) {
            shutterButton.setVisibility(View.VISIBLE);
        } else if (shutterButton != null) {
            shutterButton.setVisibility(View.GONE);
        }
    }

    /**
     * Enables/disables the shutter button to prevent double-clicks on the button.
     *
     * @param clickable True if the button should accept a click
     */
    void setShutterButtonClickable(boolean clickable) {
        shutterButton.setClickable(clickable);
    }

    /** Request the viewfinder to be invalidated. */
    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    @Override
    public void onShutterButtonClick(ShutterButton b) {
        if (isContinuousModeActive) {
            onShutterButtonPressContinuous();
        } else {
            if (handler != null) {
                handler.shutterButtonClick();
            }
        }
    }

    @Override
    public void onShutterButtonFocus(ShutterButton b, boolean pressed) {
        requestDelayedAutoFocus();
    }

    /**
     * Requests autofocus after a 350 ms delay. This delay prevents requesting focus when the user
     * just wants to click the shutter button without focusing. Quick button press/release will
     * trigger onShutterButtonClick() before the focus kicks in.
     */
    private void requestDelayedAutoFocus() {
        // Wait 350 ms before focusing to avoid interfering with quick button presses when
        // the user just wants to take a picture without focusing.
        cameraManager.requestAutoFocus(350L);
    }

    static boolean getFirstLaunch() {
        return isFirstLaunch;
    }

    /**
     * We want the help screen to be shown automatically the first time a new version of the app is
     * run. The easiest way to do this is to check android:versionCode from the manifest, and compare
     * it to a value stored as a preference.
     */
    private void checkFirstLaunch() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            int currentVersion = info.versionCode;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int lastVersion = prefs.getInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, 0);
            if (lastVersion == 0) {
                isFirstLaunch = true;
            } else {
                isFirstLaunch = false;
            }
            if (currentVersion > lastVersion) {

                // Record the last version for which we last displayed the What's New (Help) page
                prefs.edit().putInt(PreferencesActivity.KEY_HELP_VERSION_SHOWN, currentVersion).commit();
//                Intent intent = new Intent(this, HelpActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//
//                // Show the default page on a clean install, and the what's new page on an upgrade.
//                String page = lastVersion == 0 ? HelpActivity.DEFAULT_PAGE : HelpActivity.WHATS_NEW_PAGE;
//                intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, page);
//                startActivity(intent);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, e);
        }
    }

    /**
     * Returns a string that represents which OCR engine(s) are currently set to be run.
     *
     * @return OCR engine mode
     */
    public String getOcrEngineModeName() {
        String ocrEngineModeName = "";
        String[] ocrEngineModes = getResources().getStringArray(R.array.ocrenginemodes);
        if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_ONLY) {
            ocrEngineModeName = ocrEngineModes[0];
        } else if (ocrEngineMode == TessBaseAPI.OEM_LSTM_ONLY) {
            ocrEngineModeName = ocrEngineModes[1];
        } else if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_LSTM_COMBINED) {
            ocrEngineModeName = ocrEngineModes[2];
        }
        return ocrEngineModeName;
    }

    /**
     * Gets values from shared preferences and sets the corresponding data members in this activity.
     */
    private void retrievePreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Retrieve from preferences, and set in this Activity, the language preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setSourceLanguage(prefs.getString(PreferencesActivity.KEY_SOURCE_LANGUAGE_PREFERENCE, CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE));
        setTargetLanguage(prefs.getString(PreferencesActivity.KEY_TARGET_LANGUAGE_PREFERENCE, CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE));
        isTranslationActive = prefs.getBoolean(PreferencesActivity.KEY_TOGGLE_TRANSLATION, false);

        // Retrieve from preferences, and set in this Activity, the capture mode preference
        if (prefs.getBoolean(PreferencesActivity.KEY_CONTINUOUS_PREVIEW, CaptureActivity.DEFAULT_TOGGLE_CONTINUOUS)) {
            isContinuousModeActive = true;
        } else {
            isContinuousModeActive = false;
        }

        // Retrieve from preferences, and set in this Activity, the page segmentation mode preference
        String[] pageSegmentationModes = getResources().getStringArray(R.array.pagesegmentationmodes);
        String pageSegmentationModeName = prefs.getString(PreferencesActivity.KEY_PAGE_SEGMENTATION_MODE, pageSegmentationModes[0]);
        if (pageSegmentationModeName.equals(pageSegmentationModes[0])) {
            pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO_OSD;
        } else if (pageSegmentationModeName.equals(pageSegmentationModes[1])) {
            pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_AUTO;
        } else if (pageSegmentationModeName.equals(pageSegmentationModes[2])) {
            pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK;
        } else if (pageSegmentationModeName.equals(pageSegmentationModes[3])) {
            pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR;
        } else if (pageSegmentationModeName.equals(pageSegmentationModes[4])) {
            pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_COLUMN;
        } else if (pageSegmentationModeName.equals(pageSegmentationModes[5])) {
            pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_LINE;
        } else if (pageSegmentationModeName.equals(pageSegmentationModes[6])) {
            pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_WORD;
        } else if (pageSegmentationModeName.equals(pageSegmentationModes[7])) {
            pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT;
        } else if (pageSegmentationModeName.equals(pageSegmentationModes[8])) {
            pageSegmentationMode = TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT;
        }

        // Retrieve from preferences, and set in this Activity, the OCR engine mode
        String[] ocrEngineModes = getResources().getStringArray(R.array.ocrenginemodes);
        String ocrEngineModeName = prefs.getString(PreferencesActivity.KEY_OCR_ENGINE_MODE, ocrEngineModes[1]);
        if (ocrEngineModeName.equals(ocrEngineModes[0])) {
            ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY;
        } else if (ocrEngineModeName.equals(ocrEngineModes[1])) {
            ocrEngineMode = TessBaseAPI.OEM_LSTM_ONLY;
        } else if (ocrEngineModeName.equals(ocrEngineModes[2])) {
            ocrEngineMode = TessBaseAPI.OEM_TESSERACT_LSTM_COMBINED;
        }

        // Retrieve from preferences, and set in this Activity, the character blacklist and whitelist
        characterBlacklist = OcrCharacterHelper.getBlacklist(prefs, sourceLanguageCodeOcr);
        characterWhitelist = OcrCharacterHelper.getWhitelist(prefs, sourceLanguageCodeOcr);

        prefs.registerOnSharedPreferenceChangeListener(listener);

        beepManager.updatePrefs();
    }

    /**
     * Sets default values for preferences. To be called the first time this app is run.
     */
    private void setDefaultPreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Continuous preview
        prefs.edit().putBoolean(PreferencesActivity.KEY_CONTINUOUS_PREVIEW, CaptureActivity.DEFAULT_TOGGLE_CONTINUOUS).commit();

        // Recognition language
        prefs.edit().putString(PreferencesActivity.KEY_SOURCE_LANGUAGE_PREFERENCE, CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE).commit();

        // Translation
        prefs.edit().putBoolean(PreferencesActivity.KEY_TOGGLE_TRANSLATION, CaptureActivity.DEFAULT_TOGGLE_TRANSLATION).commit();

        // Translation target language
        prefs.edit().putString(PreferencesActivity.KEY_TARGET_LANGUAGE_PREFERENCE, CaptureActivity.DEFAULT_TARGET_LANGUAGE_CODE).commit();

        // Translator
        prefs.edit().putString(PreferencesActivity.KEY_TRANSLATOR, CaptureActivity.DEFAULT_TRANSLATOR).commit();

        // OCR Engine
        prefs.edit().putString(PreferencesActivity.KEY_OCR_ENGINE_MODE, CaptureActivity.DEFAULT_OCR_ENGINE_MODE).commit();

        // Autofocus
        prefs.edit().putBoolean(PreferencesActivity.KEY_AUTO_FOCUS, CaptureActivity.DEFAULT_TOGGLE_AUTO_FOCUS).commit();

        // Disable problematic focus modes
        prefs.edit().putBoolean(PreferencesActivity.KEY_DISABLE_CONTINUOUS_FOCUS, CaptureActivity.DEFAULT_DISABLE_CONTINUOUS_FOCUS).commit();

        // Beep
        prefs.edit().putBoolean(PreferencesActivity.KEY_PLAY_BEEP, CaptureActivity.DEFAULT_TOGGLE_BEEP).commit();

        // Music
        prefs.edit().putBoolean(PreferencesActivity.KEY_PLAY_MUSIC, HomeActivity.DEFAULT_TOGGLE_MUSIC).commit();

        // Music
        prefs.edit().putBoolean(PreferencesActivity.KEY_CONTINUOUS_DISPLAY_METADATA, CaptureActivity.CONTINUOUS_DISPLAY_METADATA).commit();

        // Character blacklist
        prefs.edit().putString(PreferencesActivity.KEY_CHARACTER_BLACKLIST,
                OcrCharacterHelper.getDefaultBlacklist(CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE)).commit();

        // Character whitelist
        prefs.edit().putString(PreferencesActivity.KEY_CHARACTER_WHITELIST,
                OcrCharacterHelper.getDefaultWhitelist(CaptureActivity.DEFAULT_SOURCE_LANGUAGE_CODE)).commit();

        // Page segmentation mode
        prefs.edit().putString(PreferencesActivity.KEY_PAGE_SEGMENTATION_MODE, CaptureActivity.DEFAULT_PAGE_SEGMENTATION_MODE).commit();

        // Reversed camera image
        prefs.edit().putBoolean(PreferencesActivity.KEY_REVERSE_IMAGE, CaptureActivity.DEFAULT_TOGGLE_REVERSED_IMAGE).commit();

        // Light
        prefs.edit().putBoolean(PreferencesActivity.KEY_TOGGLE_LIGHT, CaptureActivity.DEFAULT_TOGGLE_LIGHT).commit();
    }

    public void displayProgressDialog() {
        // Set up the indeterminate progress dialog box
        indeterminateDialog = new ProgressDialog(this, R.style.MyProgressDialogStyle);
        indeterminateDialog.setIcon(R.drawable.ic_dialog);
        indeterminateDialog.setTitle("Please wait");
        String ocrEngineModeName = getOcrEngineModeName();
        if (ocrEngineModeName.equals("Both")) {
            indeterminateDialog.setMessage("Performing OCR using Cube and Tesseract...");
        } else {
            indeterminateDialog.setMessage("Performing OCR using " + ocrEngineModeName + "...");
        }
        indeterminateDialog.setCancelable(false);
        indeterminateDialog.show();
    }

    public ProgressDialog getProgressDialog() {
        return indeterminateDialog;
    }

    /**
     * Displays an error message dialog box to the user on the UI thread.
     *
     * @param title The title for the dialog box
     * @param message The error message to be displayed
     */
    public void showErrorMessage(String title, String message) {
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setIcon(R.drawable.error)
                .setTitle(title)
                .setMessage(message)
                .setOnCancelListener(new FinishListener(this))
                .setPositiveButton( "Done", new FinishListener(this))
                .show();
    }

}