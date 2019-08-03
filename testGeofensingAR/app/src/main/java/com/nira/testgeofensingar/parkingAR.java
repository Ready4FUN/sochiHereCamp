package com.nira.testgeofensingar;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

public class parkingAR extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ModelRenderable andyRenderable;
    private ArFragment arFragment;
    private AnchorNode anchorNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_ar);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);


        ModelRenderable.builder()
                .setSource(this, R.raw.archer)
                .build()
                .thenAccept(renderable -> andyRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Log.e(TAG, "Unable to load Renderable.", throwable);
                            return null;
                        });

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onSceneUpdate);


    }


    private void onSceneUpdate(FrameTime frameTime) {
        // Let the fragment update its state first.
        arFragment.onUpdate(frameTime);

        // If there is no frame then don't process anything.
        if (arFragment.getArSceneView().getArFrame() == null) {
            return;
        }

        // If ARCore is not tracking yet, then don't process anything.
        if (arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        // Place the anchor 1m in front of the camera if anchorNode is null.
        if (this.anchorNode == null) {
            Session session = arFragment.getArSceneView().getSession();
            float[] pos = { 0,-2,-2 };
            float[] rotation = {0,0,0,1};
            Anchor anchor =  session.createAnchor(new Pose(pos, rotation));

            anchorNode = new AnchorNode(anchor);
            anchorNode.setRenderable(andyRenderable);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
        }
    }

}
