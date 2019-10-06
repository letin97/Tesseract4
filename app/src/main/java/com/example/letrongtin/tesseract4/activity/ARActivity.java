package com.example.letrongtin.tesseract4.activity;

import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.example.letrongtin.tesseract4.R;
import com.example.letrongtin.tesseract4.enums.AnimalEnum;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ARActivity extends AppCompatActivity {

    // ARCore
    private ArFragment arFragment;
    private ModelRenderable modelRenderable;
    AnchorNode anchorNode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        arFragment = (ArFragment)getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        //arFragment.getPlaneDiscoveryController().hide();
        //arFragment.getPlaneDiscoveryController().setInstructionView(null);

        String nameAnimal;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                nameAnimal = null;
            } else {
                nameAnimal = extras.getString("ANIMAL_NAME");
            }
        } else {
            nameAnimal = (String) savedInstanceState.getSerializable("ANIMAL_NAME");
        }

        setupModel(nameAnimal);

        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());
                createModel(anchorNode);
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

    }

    private void setupModel(String text) {
        AnimalEnum animalEnum = AnimalEnum.getAnimalEnum(text);

        if (animalEnum == null) return;

        switch (animalEnum) {
            case BEAR:
                ModelRenderable.builder()
                        .setSource(this, R.raw.bear)
                        .build().thenAccept(renderable -> modelRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Không thể load model", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
                break;
            case CAT:
                ModelRenderable.builder()
                        .setSource(this, R.raw.cat)
                        .build().thenAccept(renderable -> modelRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Không thể load model", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
                break;
            case COW:
                ModelRenderable.builder()
                        .setSource(this, R.raw.cow)
                        .build().thenAccept(renderable -> modelRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Không thể load model", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
                break;
            case DOG:
                ModelRenderable.builder()
                        .setSource(this, R.raw.dog)
                        .build().thenAccept(renderable -> modelRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Không thể load model", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
                break;
            case ELEPHANT:
                ModelRenderable.builder()
                        .setSource(this, R.raw.elephant)
                        .build().thenAccept(renderable -> modelRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Không thể load model", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
                break;
            case FERRET:
                ModelRenderable.builder()
                        .setSource(this, R.raw.ferret)
                        .build().thenAccept(renderable -> modelRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Không thể load model", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
                break;
            case HIPPOPOTAMUS:
                ModelRenderable.builder()
                        .setSource(this, R.raw.hippopotamus)
                        .build().thenAccept(renderable -> modelRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Không thể load model", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
                break;
            case HORSE:
                ModelRenderable.builder()
                        .setSource(this, R.raw.horse)
                        .build().thenAccept(renderable -> modelRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Không thể load model", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
                break;
            case KOALA:
                ModelRenderable.builder()
                        .setSource(this, R.raw.koala_bear)
                        .build().thenAccept(renderable -> modelRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Không thể load model", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
                break;
            case LION:
                ModelRenderable.builder()
                        .setSource(this, R.raw.lion)
                        .build().thenAccept(renderable -> modelRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Không thể load model", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
                break;
            case REINDEER:
                ModelRenderable.builder()
                        .setSource(this, R.raw.reindeer)
                        .build().thenAccept(renderable -> modelRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Không thể load model", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
                break;
            case WOLVERINE:
                ModelRenderable.builder()
                        .setSource(this, R.raw.wolverine)
                        .build().thenAccept(renderable -> modelRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Không thể load model", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
                break;

            default:
                ModelRenderable.builder()
                        .setSource(this, R.raw.bear)
                        .build().thenAccept(renderable -> modelRenderable = renderable)
                        .exceptionally(
                                throwable -> {
                                    Toast.makeText(this, "Không thể load model", Toast.LENGTH_SHORT).show();
                                    return null;
                                }
                        );
        }
    }

    private void createModel(AnchorNode anchorNode) {
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);
        node.setLocalRotation(Quaternion.axisAngle(new Vector3(0, -1, 1f), 0));
        node.select();
    }

}
