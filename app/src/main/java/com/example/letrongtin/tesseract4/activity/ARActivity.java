package com.example.letrongtin.tesseract4.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.letrongtin.tesseract4.BeepManager;
import com.example.letrongtin.tesseract4.FinishListener;
import com.example.letrongtin.tesseract4.R;
import com.example.letrongtin.tesseract4.enums.AnimalNameEnum;
import com.example.letrongtin.tesseract4.slider.PrefManager;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ARActivity extends AppCompatActivity {

    private static final String TAG = ARActivity.class.getSimpleName();

    // ARCore
    private ArFragment arFragment;
    private ModelRenderable modelRenderable;
    String resultText;
    String nameAnimal;
    AnchorNode anchorNode = null;
    private Button closeButton, infoButton;
    private PrefManager prefManager;
    private int currentApiVersion;

    Intent intent;
    private static boolean isFirstLaunch = false;
    private static boolean isFirstAnimal = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = new Intent(getApplicationContext(), CaptureActivity.class);
        startActivityForResult(intent, 1);

        setContentView(R.layout.activity_ar);

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

        arFragment = (ArFragment)getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        //arFragment.getPlaneDiscoveryController().hide();
        //arFragment.getPlaneDiscoveryController().setInstructionView(null);


//        if (savedInstanceState == null) {
//            Bundle extras = getIntent().getExtras();
//            if(extras == null) {
//                resultText = null;
//                nameAnimal = null;
//            } else {
//                resultText = extras.getString("RESULT_TEXT");
//                nameAnimal = extras.getString("ANIMAL_NAME");
//            }
//        } else {
//            resultText = (String) savedInstanceState.getSerializable("RESULT_TEXT");
//            nameAnimal = (String) savedInstanceState.getSerializable("ANIMAL_NAME");
//        }

        //setupModel(nameAnimal);

        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());
                createModel(anchorNode);
                isFirstAnimal = true;
            }
        });

//        arFragment.getArSceneView().getScene().addOnUpdateListener(new Scene.OnUpdateListener() {
//            @Override
//            public void onUpdate(FrameTime frameTime) {
//
//                // If there is no frame then don't process anything.
//                if (arFragment.getArSceneView().getArFrame() == null) {
//                    return;
//                }
//
//
//                // If ARCore is not tracking yet, then don't process anything.
//                if (arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING) {
//                    return;
//                }
//
//                // Place the anchor 1m in front of the camera if anchorNode is null.
//                if (anchorNode == null) {
//
//                    Session session = arFragment.getArSceneView().getSession();
//
//            /*float[] position = {0, 0, -1};
//            float[] rotation = {0, 0, 0, 1};
//            Anchor anchor = session.createAnchor(new Pose(position, rotation));*/
//
//                    Vector3 cameraPos = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
//                    Vector3 cameraForward = arFragment.getArSceneView().getScene().getCamera().getForward();
//                    Vector3 position = Vector3.add(cameraPos, cameraForward.scaled(1.0f));
//
//                    // Create an ARCore Anchor at the position.
//                    Pose pose = Pose.makeTranslation(position.x, position.y, position.z).extractTranslation();
//                    Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(pose);
//
//                    anchorNode = new AnchorNode(anchor);
//                    anchorNode.setParent(arFragment.getArSceneView().getScene());
//
//     /* Node node = new Node();
//      node.setRenderable(andyRenderable);
//      node.setParent(mAnchorNode);
//      node.setOnTapListener((hitTestResult, motionEvent) -> {
//      });*/
//
//                    TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
//                    transformableNode.setParent(anchorNode);
//                    transformableNode.setRenderable(bearRenderable);
//                    transformableNode.select();
//                }
//            }
//        });

        closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(intent, 1);
                //finish();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(ARActivity.this, R.style.DialogTheme);
        LayoutInflater factory = LayoutInflater.from(ARActivity.this);
        final View v = factory.inflate(R.layout.introduce, null);
        builder.setView(v);
        AlertDialog alertDialog = builder.create();
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

//        infoButton = findViewById(R.id.info_button);
//        infoButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
////                alertDialog.show();
////                alertDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
////                        | View.SYSTEM_UI_FLAG_FULLSCREEN
////                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
////                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
////                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
////                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
////                alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
//                startActivityForResult(intent, 1);
//            }
//        });

        prefManager = new PrefManager(this);

        if (prefManager.isFirstTimeLaunchAR()) {
            prefManager.setFirstTimeLaunchAR(false);
            alertDialog.show();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){

                if ("".equals(data.getStringExtra("ANIMAL_NAME")) || data.getStringExtra("ANIMAL_NAME") == null) {
                    finish();
                    return;
                }

                nameAnimal = data.getStringExtra("ANIMAL_NAME");
                setupModel(nameAnimal);
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
//                if (!isFirstAnimal) {
//                    finish();
//                    return;
//                }
//
//                if (nameAnimal == null || "".equals(nameAnimal)){
//                    finish();
//                    return;
//                }
            }

            if (isFirstAnimal) {
                if (arFragment != null) {
                    // hiding the plane discovery
                    // arFragment.getPlaneDiscoveryController().hide();
                    // arFragment.getPlaneDiscoveryController().setInstructionView(null);
                    arFragment.getArSceneView().getSession().getConfig().setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
                    //arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arFragment != null)
            arFragment.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (arFragment != null)
            arFragment.onPause();

    }

    private void setupModel(String name) {
        AnimalNameEnum animalEnum = AnimalNameEnum.getAnimalName(name);

        if (animalEnum == null) {
            showErrorMessage("Error", "Could not initialize load model");
            return;
        }

        int idAnimal = R.raw.bear;

        switch (animalEnum) {

            case ARMADILLO:
                idAnimal = R.raw.armadillo;
                break;
            case BEAR:
                idAnimal = R.raw.bear;
                break;
            case BEAVER:
                idAnimal = R.raw.beaver;
                break;
//            case BEE:
//                idAnimal = R.raw.bee;
//                break;
            case BIRD:
                idAnimal = R.raw.bird;
                break;
            case BISON:
                idAnimal = R.raw.bison;
                break;
//            case BUTTERFLY:
//                idAnimal = R.raw.butterfly;
//                break;
            case CAMEL:
                idAnimal = R.raw.camel;
                break;
            case CAT:
                idAnimal = R.raw.cat;
                break;
            case CHICKEN:
                idAnimal = R.raw.chicken;
                break;
            case COW:
                idAnimal = R.raw.cow;
                break;
            case CRAB:
                idAnimal = R.raw.crab;
                break;
            case CROCODILE:
                idAnimal = R.raw.crocodile;
                break;
            case DEER:
                idAnimal = R.raw.deer;
                break;
            case DINOSAUR:
                idAnimal = R.raw.minmi;
                break;
            case DOG:
                idAnimal = R.raw.dog;
                break;
            case DOLPHIN:
                idAnimal = R.raw.dolphin;
                break;
            case DUCK:
                idAnimal = R.raw.duck;
                break;
            case ELEPHANT:
                idAnimal = R.raw.elephant;
                break;
            case FERRET:
                idAnimal = R.raw.ferret;
                break;
            case FISH:
                idAnimal = R.raw.fish;
                break;
            case FOX:
                idAnimal = R.raw.fox;
                break;
            case FROG:
                idAnimal = R.raw.frog;
                break;
            case GIBBON:
                idAnimal = R.raw.gibbon;
                break;
            case GIRAFFE:
                idAnimal = R.raw.giraffe;
                break;
            case GOAT:
                idAnimal = R.raw.lbex;
                break;
            case GOOSE:
                idAnimal = R.raw.goose;
                break;
            case GULL:
                idAnimal = R.raw.gull;
                break;
            case HAWK:
                idAnimal = R.raw.hawk;
                break;
            case HIPPOPOTAMUS:
                idAnimal = R.raw.hippopotamus;
                break;
            case HORSE:
                idAnimal = R.raw.horse;
                break;
            case HYENA:
                idAnimal = R.raw.hyena;
                break;
            case KANGAROO:
                idAnimal = R.raw.kangaroo;
                break;
            case KINGFISHER:
                idAnimal = R.raw.kingfisher;
                break;
            case KOALA:
                idAnimal = R.raw.koala_bear;
                break;
//            case LAMB:
//                idAnimal = R.raw.lamb;
//                break;
            case LION:
                idAnimal = R.raw.lion;
                break;
            case LIZARD:
                idAnimal = R.raw.lizard;
                break;
            case MAMMOTH:
                idAnimal = R.raw.mammoth;
                break;
            case MANATEE:
                idAnimal = R.raw.manatee;
                break;
            case MONKEY:
                idAnimal = R.raw.monkey;
                break;
            case OTTER:
                idAnimal = R.raw.otter;
                break;
//            case PANDA:
//                idAnimal = R.raw.panda;
//                break;
            case PARROT:
                idAnimal = R.raw.parrot;
                break;
//            case PEACOCK:
//                //idAnimal = R.raw.pea
//                break;
            case PENGUIN:
                idAnimal = R.raw.penguin;
                break;
            case PIG:
                idAnimal = R.raw.pig;
                break;
            case RABBIT:
                idAnimal = R.raw.rabbit;
                break;
            case RACOON:
                idAnimal = R.raw.racoon;
                break;
            case REINDEER:
                idAnimal = R.raw.reindeer;
                break;
            case SEAHORSE:
                idAnimal = R.raw.seahorse;
                break;
            case SEA_LION:
                idAnimal = R.raw.sealion;
                break;
            case SHARK:
                idAnimal = R.raw.shark;
                break;
            case SHEEP:
                idAnimal = R.raw.sheep;
                break;
            case SHRIMP:
                idAnimal = R.raw.shrimp;
                break;
//            case SNAIL:
//                //
//                break;
            case SNAKE:
                idAnimal = R.raw.snake;
                break;
            case SQUIRREL:
                idAnimal = R.raw.glider;
                break;
            case STORK:
                idAnimal = R.raw.stork;
                break;
            case SWAN:
                idAnimal = R.raw.swan;
                break;
            case TAPIR:
                idAnimal = R.raw.tapir;
                break;
            case TIGER:
                idAnimal = R.raw.smilodon;
                break;
            case TURTLE:
                idAnimal = R.raw.turtle;
                break;
            case MOUSE:
                idAnimal = R.raw.muskrat;
                break;
            case VULTURE:
                idAnimal = R.raw.vulture;
                break;
            case WALRUS:
                idAnimal = R.raw.walrus;
                break;
            case WHALE:
                idAnimal = R.raw.whale;
                break;
            case WOLF:
                idAnimal = R.raw.worf;
                break;
            case WOLVERINE:
                idAnimal = R.raw.wolverine;
                break;
        }

        ModelRenderable.builder()
                .setSource(this, idAnimal)
                .build().thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast.makeText(this, "Could not initialize load model", Toast.LENGTH_SHORT).show();
                            return null;
                        }
                );
    }

    private void createModel(AnchorNode anchorNode) {
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);
        //node.setLocalScale(new Vector3(2f, 2f, 2f));
        node.setLookDirection(new Vector3(0, 0, 1));
        node.select();
        
        //addName(anchorNode, node, resultText);
    }

    private void addName(AnchorNode anchorNode, TransformableNode node, String name) {

        ViewRenderable.builder()
                .setView(this, R.layout.name_animal)
                .build()
                .thenAccept(viewRenderable -> {
                    TransformableNode nameView = new TransformableNode(arFragment.getTransformationSystem());
                    nameView.setLocalPosition(new Vector3(0f, node.getLocalPosition().y + 0.1f, 0));
                    nameView.setParent(anchorNode);
                    nameView.setRenderable(viewRenderable);

                    TextView txt_name = (TextView) viewRenderable.getView();
                    txt_name.setText(name);
                });

    }

    public void showErrorMessage(String title, String message) {
        new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle(title)
                .setMessage(message)
                .setOnCancelListener(new FinishListener(this))
                .setPositiveButton( "Done", new FinishListener(this))
                .show();
    }
}
