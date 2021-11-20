/*Copyright 2021 Ana Milenkovic
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
*/
package com.example.traveller;

import androidx.annotation.GuardedBy;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.traveller.common.QuestionDialogFragment;
import com.example.traveller.common.cloudanchor.CloudAnchorManager;
import com.example.traveller.common.cloudanchor.FirebaseManager;
import com.example.traveller.common.helpers.DisplayRotationHelper;
import com.example.traveller.common.helpers.FullScreenHelper;
import com.example.traveller.common.helpers.SnackbarHelper;
import com.example.traveller.common.helpers.TrackingStateHelper;
import com.example.traveller.common.rendering.BackgroundRenderer;
import com.example.traveller.common.rendering.ObjectRenderer;
import com.example.traveller.common.rendering.PlaneRenderer;
import com.example.traveller.common.rendering.PointCloudRenderer;
import com.example.traveller.models.Treasure;
import com.example.traveller.util.CameraPermissionHelper;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.common.base.Preconditions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CloudAnchorActivity extends AppCompatActivity
        implements GLSurfaceView.Renderer, PrivacyNoticeDialogFragment.NoticeDialogListener {

    private static final String TAG = CloudAnchorActivity.class.getSimpleName();
    // the color of some parts of the object, depending on the .obj model
    //this color is a pretty brown called Driftwood (Threepwood would be good as well)
    private static final float[] OBJECT_COLOR = new float[] {162.0f, 123.0f, 77.0f, 255.0f};
    private boolean isAdmin=false;
    private boolean isForAddOrEditTreasure=false;
    private String username="";
    //Treasure preko kojeg vracam podatke, moze i svaki podatak pojedinacno
    private Treasure treasureToReturn=new Treasure();

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private static final String CHILD_TREASURES="treasures";

    private enum HostResolveMode {
        NONE,
        HOSTING,
        RESOLVING,
    }

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;
    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final ObjectRenderer virtualObject = new ObjectRenderer();
    private final ObjectRenderer virtualObjectShadow = new ObjectRenderer();
    private final PlaneRenderer planeRenderer = new PlaneRenderer();
    private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();

    private boolean installRequested;

    // Temporary matrices allocated here to reduce number of allocations for each frame.
    private final float[] anchorMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];

    // Locks needed for synchronization
    private final Object singleTapLock = new Object();
    private final Object anchorLock = new Object();

    // Tap handling and UI.
    private GestureDetector gestureDetector;
    private final SnackbarHelper snackbarHelper = new SnackbarHelper();
    private DisplayRotationHelper displayRotationHelper;
    private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);
    private Button hostButton;
    private Button resolveButton;
    private TextView roomCodeText;
    private SharedPreferences sharedPreferences;
    private static final String PREFERENCE_FILE_KEY = "allow_sharing_images";
    private static final String ALLOW_SHARE_IMAGES_KEY = "ALLOW_SHARE_IMAGES";

    @GuardedBy("singleTapLock")
    private MotionEvent queuedSingleTap;

    private Session session;

    @GuardedBy("anchorLock")
    private Anchor anchor;

    // Cloud Anchor Components.
    private FirebaseManager firebaseManager;
    private final CloudAnchorManager cloudManager = new CloudAnchorManager();
    private HostResolveMode currentMode;
    private RoomCodeAndCloudAnchorIdListener hostListener;
    FoundTreasuresAndActiveTHListener ftl=new FoundTreasuresAndActiveTHListener();
    private TreasurePointsQuestionAnswerListener tpqaListener=new TreasurePointsQuestionAnswerListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_anchor);

        Intent i=getIntent();
        isAdmin=i.getBooleanExtra("isAdmin",false);
        isForAddOrEditTreasure=i.getBooleanExtra("isForAddOrEditTreasure",false);
        username=i.getStringExtra("username");
        Button btnSave=findViewById(R.id.btnSave_cloud_anchor);
        firebaseManager = new FirebaseManager(this);
        if(!isAdmin){
            Button btnHost=findViewById(R.id.host_button);
            btnHost.setVisibility(View.GONE);
            btnHost.setEnabled(false);
            btnSave.setVisibility(View.GONE);
            btnSave.setEnabled(false);
            firebaseManager.getActiveTreasureHunt(username,ftl);
            firebaseManager.getFoundTreasures(username,ftl);
            firebaseManager.getCompletedTreasureHunts(username,ftl);
        }
        if(isForAddOrEditTreasure){
            Button btnResolve=findViewById(R.id.resolve_button);
            btnResolve.setEnabled(false);
            btnResolve.setVisibility(View.GONE);
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result",treasureToReturn);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            });
        }
        else{
            Button btnHost=findViewById(R.id.host_button);
            btnHost.setVisibility(View.GONE);
            btnHost.setEnabled(false);
            btnSave.setVisibility(View.GONE);
            btnSave.setEnabled(false);
            //pribavljanje podataka koji ce mi trebati unapred
            //da bi stigli da budu dostupni kada mi budu trebali

            firebaseManager.getActiveTreasureHunt(username,ftl);
            firebaseManager.getFoundTreasures(username,ftl);
            firebaseManager.getCompletedTreasureHunts(username,ftl);
        }

        surfaceView = findViewById(R.id.surfaceview);
        displayRotationHelper = new DisplayRotationHelper(this);

        // Set up touch listener.
        gestureDetector =
                new GestureDetector(
                        this,
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                synchronized (singleTapLock) {
                                    if (currentMode == HostResolveMode.HOSTING) {
                                        queuedSingleTap = e;
                                    }
                                }
                                return true;
                            }

                            @Override
                            public boolean onDown(MotionEvent e) {
                                return true;
                            }
                        });
        surfaceView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Set up renderer.
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setWillNotDraw(false);
        installRequested = false;

        // Initialize UI components.
        hostButton = findViewById(R.id.host_button);
        hostButton.setOnClickListener((view) -> onHostButtonPress());
        resolveButton = findViewById(R.id.resolve_button);
        resolveButton.setOnClickListener((view) -> onResolveButtonPress());
        roomCodeText = findViewById(R.id.room_code_text);
        Button backButton=findViewById(R.id.btnBack_cloud_anchor);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        // Initialize Cloud Anchor variables.
        //ovde je pre bilo firebaseManager = new FirebaseManager(this);
        currentMode = HostResolveMode.NONE;
        sharedPreferences = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

    }

    @Override
    protected void onDestroy() {
        // Clear all registered listeners.
        resetMode();

        if (session != null) {
            // Explicitly close ARCore Session to release native resources.
            // Review the API reference for important considerations before calling close() in apps with
            // more complicated lifecycle requirements:
            // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
            session.close();
            session = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sharedPreferences.getBoolean(ALLOW_SHARE_IMAGES_KEY, false)) {
            createSession();
        }
        if(isForAddOrEditTreasure)
            snackbarHelper.showMessage(this, "Press Host to enter hosting mode");
        else
            snackbarHelper.showMessage(this, "Press Dig to search for treasure!");
        surfaceView.onResume();
        displayRotationHelper.onResume();
    }

    private void createSession() {
        if (session == null) {
            Exception exception = null;
            int messageId = -1;
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }
                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }
                session = new Session(this);
            } catch (UnavailableArcoreNotInstalledException e) {
                messageId = R.string.snackbar_arcore_unavailable;
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                messageId = R.string.snackbar_arcore_too_old;
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                messageId = R.string.snackbar_arcore_sdk_too_old;
                exception = e;
            } catch (Exception e) {
                messageId = R.string.snackbar_arcore_exception;
                exception = e;
            }

            if (exception != null) {
                snackbarHelper.showError(this, getString(messageId));
                Log.e(TAG, "Exception creating session", exception);
                return;
            }

            // Create default config and check if supported.
            Config config = new Config(session);
            config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
            session.configure(config);

            // Setting the session in the HostManager.
            cloudManager.setSession(session);
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            snackbarHelper.showError(this, getString(R.string.snackbar_camera_unavailable));
            session = null;
            return;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    /**
     * Handles the most recent user tap.
     *
     * <p>We only ever handle one tap at a time, since this app only allows for a single anchor.
     *
     * @param frame the current AR frame
     * @param cameraTrackingState the current camera tracking state
     */
    private void handleTap(Frame frame, TrackingState cameraTrackingState) {
        // Handle taps. Handling only one tap per frame, as taps are usually low frequency
        // compared to frame rate.
        synchronized (singleTapLock) {
            synchronized (anchorLock) {
                // Only handle a tap if the anchor is currently null, the queued tap is non-null and the
                // camera is currently tracking.
                if (anchor == null
                        && queuedSingleTap != null
                        && cameraTrackingState == TrackingState.TRACKING) {
                    Preconditions.checkState(
                            currentMode == HostResolveMode.HOSTING,
                            "We should only be creating an anchor in hosting mode.");
                    for (HitResult hit : frame.hitTest(queuedSingleTap)) {
                        if (shouldCreateAnchorWithHit(hit)) {
                            Anchor newAnchor = hit.createAnchor();
                            Preconditions.checkNotNull(hostListener, "The host listener cannot be null.");
                            cloudManager.hostCloudAnchor(newAnchor, hostListener);
                            setNewAnchor(newAnchor);
                            snackbarHelper.showMessage(this, getString(R.string.snackbar_anchor_placed));
                            break; // Only handle the first valid hit.
                        }
                    }
                }
            }
            queuedSingleTap = null;
        }
    }

    /** Returns {@code true} if and only if the hit can be used to create an Anchor reliably. */
    private static boolean shouldCreateAnchorWithHit(HitResult hit) {
        Trackable trackable = hit.getTrackable();
        if (trackable instanceof Plane) {
            // Check if the hit was within the plane's polygon.
            return ((Plane) trackable).isPoseInPolygon(hit.getHitPose());
        } else if (trackable instanceof Point) {
            // Check if the hit was against an oriented point.
            return ((Point) trackable).getOrientationMode() == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL;
        }
        return false;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread(this);
            planeRenderer.createOnGlThread(this, "models/trigrid.png");
            pointCloudRenderer.createOnGlThread(this);

            //ovde zameni svoj model, vrv neki treasure chest, ako nadjes
            virtualObject.createOnGlThread(this, "models/chest_obj.obj", "models/chest.png");
            virtualObject.setMaterialProperties(0.5f, 1.0f, 0.5f, 6.0f);

            //virtualObjectShadow.createOnGlThread(
                  // this, "models/andy_shadow.obj", "models/andy_shadow.png");
            //virtualObjectShadow.setBlendMode(ObjectRenderer.BlendMode.Shadow);
            //virtualObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);
        } catch (IOException ex) {
            Log.e(TAG, "Failed to read an asset file", ex);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            Frame frame = session.update();
            Camera camera = frame.getCamera();
            TrackingState cameraTrackingState = camera.getTrackingState();

            // Notify the cloudManager of all the updates.
            cloudManager.onUpdate();

            // Handle user input.
            handleTap(frame, cameraTrackingState);

            // If frame is ready, render camera preview image to the GL surface.
            backgroundRenderer.draw(frame);

            // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
            trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

            // If not tracking, don't draw 3d objects.
            if (cameraTrackingState == TrackingState.PAUSED) {
                return;
            }

            // Get camera and projection matrices.
            camera.getViewMatrix(viewMatrix, 0);
            camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f);

            // Visualize tracked points.
            // Use try-with-resources to automatically release the point cloud.
            try (PointCloud pointCloud = frame.acquirePointCloud()) {
                pointCloudRenderer.update(pointCloud);
                pointCloudRenderer.draw(viewMatrix, projectionMatrix);
            }

            // Visualize planes.
            planeRenderer.drawPlanes(
                    session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projectionMatrix);

            // Check if the anchor can be visualized or not, and get its pose if it can be.
            boolean shouldDrawAnchor = false;
            synchronized (anchorLock) {
                if (anchor != null && anchor.getTrackingState() == TrackingState.TRACKING) {
                    // Get the current pose of an Anchor in world space. The Anchor pose is updated
                    // during calls to session.update() as ARCore refines its estimate of the world.
                    anchor.getPose().toMatrix(anchorMatrix, 0);
                    shouldDrawAnchor = true;
                }
            }

            // Visualize anchor.
            if (shouldDrawAnchor) {
                float[] colorCorrectionRgba = new float[4];
                frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

                // Update and draw the model and its shadow.
                float scaleFactor = 1.0f;
                virtualObject.updateModelMatrix(anchorMatrix, scaleFactor);
                //virtualObjectShadow.updateModelMatrix(anchorMatrix, scaleFactor);
                virtualObject.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, OBJECT_COLOR);
                //virtualObjectShadow.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, OBJECT_COLOR);

            }
        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    /** Sets the new value of the current anchor. Detaches the old anchor, if it was non-null. */
    private void setNewAnchor(Anchor newAnchor) {
        synchronized (anchorLock) {
            if (anchor != null) {
                anchor.detach();
            }
            anchor = newAnchor;
        }
    }

    /** Callback function invoked when the Host Button is pressed. */
    private void onHostButtonPress() {
        if (currentMode == HostResolveMode.HOSTING) {
            resetMode();
            return;
        }

        if (!sharedPreferences.getBoolean(ALLOW_SHARE_IMAGES_KEY, false)) {
            showNoticeDialog(this::onPrivacyAcceptedForHost);
        } else {
            onPrivacyAcceptedForHost();
        }
    }

    private void onPrivacyAcceptedForHost() {
        if (hostListener != null) {
            return;
        }
        resolveButton.setEnabled(false);
        hostButton.setText(R.string.cancel);
        snackbarHelper.showMessageWithDismiss(this, getString(R.string.snackbar_on_host));

        hostListener = new RoomCodeAndCloudAnchorIdListener();
        firebaseManager.getNewRoomCode(hostListener);
        synchronized (singleTapLock) {
            // Change currentMode to HOSTING after receiving the room code (not when the 'Host' button
            // is tapped), to prevent an anchor being placed before we know the room code and able to
            // share the anchor ID.
            currentMode = HostResolveMode.HOSTING;
        }
    }

    /** Callback function invoked when the Resolve Button is pressed. */
    private void onResolveButtonPress() {
        if (currentMode == HostResolveMode.RESOLVING) {
            resetMode();
            return;
        }

        if (!sharedPreferences.getBoolean(ALLOW_SHARE_IMAGES_KEY, false)) {
            showNoticeDialog(this::onPrivacyAcceptedForResolve);
        } else {
            onPrivacyAcceptedForResolve();
        }
    }

    private void onPrivacyAcceptedForResolve() {
        ResolveDialogFragment dialogFragment = new ResolveDialogFragment();
        dialogFragment.setOkListener(this::onTreasureNameEntered);
        dialogFragment.show(getSupportFragmentManager(), "ResolveDialog");
    }

    /** Resets the mode of the app to its initial state and removes the anchors. */
    private void resetMode() {
        hostButton.setText(R.string.host_button_text);
        hostButton.setEnabled(true);
        resolveButton.setText(R.string.resolve_button_text);
        resolveButton.setEnabled(true);
        roomCodeText.setText(R.string.initial_treasure_name);
        currentMode = HostResolveMode.NONE;
        firebaseManager.clearRoomListener();
        hostListener = null;
        setNewAnchor(null);
        snackbarHelper.hide(this);
        cloudManager.clearListeners();
    }

    /** Callback function invoked when the user presses the OK button in the Resolve Dialog. */
    private void onTreasureNameEntered(String treasureName) {

        if(!hasActiveTreasureHunt()){
            Toast.makeText(getApplicationContext(),"Activate a treasure hunt first, then dig for treasure.",Toast.LENGTH_LONG).show();
            return;
        }

        if(treasureAlreadyFound(treasureName)) {
            Toast.makeText(getApplicationContext(),"You have already found this treasure!",Toast.LENGTH_LONG).show();
            return;
        }

        if(!treasureIsPartOfTreasureHunt(treasureName)){
            Toast.makeText(getApplicationContext(),"The treasure you entered is not a part of your active treasure hunt.",Toast.LENGTH_LONG).show();
            return;
        }

        firebaseManager.getTreasureQuestion(treasureName, tpqaListener);
        firebaseManager.getTreasureAnswer(treasureName, tpqaListener);
        firebaseManager.getTreasureWrongAnswers(treasureName,tpqaListener);
        firebaseManager.getTreasurePoints(treasureName, tpqaListener);
        currentMode = HostResolveMode.RESOLVING;
        hostButton.setEnabled(false);
        resolveButton.setText(R.string.cancel);
        roomCodeText.setText(String.valueOf(treasureName));
        snackbarHelper.showMessageWithDismiss(this, getString(R.string.snackbar_on_resolve));

        // Register a new listener for the given treasure name
        firebaseManager.registerNewListenerForTreasureName(
                treasureName,
                cloudAnchorId -> {
                    // When the cloud anchor ID is available from Firebase.
                    CloudAnchorResolveStateListener resolveListener =
                            new CloudAnchorResolveStateListener(treasureName);
                    Preconditions.checkNotNull(resolveListener, "The resolve listener cannot be null.");
                    cloudManager.resolveCloudAnchor(
                            cloudAnchorId, resolveListener, SystemClock.uptimeMillis());
                });
    }

    /**
     * Listens for both a new room code and an anchor ID, and shares the anchor ID in Firebase with
     * the room code when both are available.
     */
    private final class RoomCodeAndCloudAnchorIdListener
            implements CloudAnchorManager.CloudAnchorHostListener, FirebaseManager.RoomCodeListener {

        private Long roomCode;
        private String cloudAnchorId;

        @Override
        public void onNewRoomCode(/*Long newRoomCode*/) {
            /*Preconditions.checkState(roomCode == null, "The room code cannot have been set before.");
            roomCode = newRoomCode;
            roomCodeText.setText(String.valueOf(roomCode));
            snackbarHelper.showMessageWithDismiss(
                    CloudAnchorActivity.this, getString(R.string.snackbar_room_code_available));*/
            checkAndMaybeShare();
            synchronized (singleTapLock) {
                // Change currentMode to HOSTING after receiving the room code (not when the 'Host' button
                // is tapped), to prevent an anchor being placed before we know the room code and able to
                // share the anchor ID.
                currentMode = HostResolveMode.HOSTING;
            }
        }

        @Override
        public void onError(DatabaseError error) {
            Log.w(TAG, "A Firebase database error happened.", error.toException());
            snackbarHelper.showError(
                    CloudAnchorActivity.this, getString(R.string.snackbar_firebase_error));
        }

        @Override
        public void onCloudTaskComplete(Anchor anchor) {
            Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
            if (cloudState.isError()) {
                Log.e(TAG, "Error hosting a cloud anchor, state " + cloudState);
                snackbarHelper.showMessageWithDismiss(
                        CloudAnchorActivity.this, getString(R.string.snackbar_host_error, cloudState));
                return;
            }
            Preconditions.checkState(
                    cloudAnchorId == null, "The cloud anchor ID cannot have been set before.");
            cloudAnchorId = anchor.getCloudAnchorId();
            setNewAnchor(anchor);
            checkAndMaybeShare();
        }

        private void checkAndMaybeShare() {
            if (/*roomCode == null ||*/ cloudAnchorId == null) {
                return;
            }
            //ovde cuva u firebase po sample modelu, obrisi ovo, ovde treba da sacuva u treasure i da taj treasure vrati kroz ActivityResult
            //firebaseManager.storeAnchorIdInRoom(roomCode, cloudAnchorId);
            treasureToReturn.hostedAnchorID=cloudAnchorId;
            //treasureToReturn.roomCode= roomCode.intValue();
            treasureToReturn.updatedAtTimestamp=System.currentTimeMillis();
            snackbarHelper.showMessageWithDismiss(
                    CloudAnchorActivity.this, "Anchor is placed.");
            //Intent returnIntent = new Intent();
            //returnIntent.putExtra("result", treasureToReturn);
            //setResult(Activity.RESULT_OK, returnIntent);
            //finish();
        }
    }

    private final class CloudAnchorResolveStateListener
            implements CloudAnchorManager.CloudAnchorResolveListener {
        private final String treasureName;

        CloudAnchorResolveStateListener(String treasureName) {
            this.treasureName = treasureName;
        }

        @Override
        public void onCloudTaskComplete(Anchor anchor) {
            // When the anchor has been resolved, or had a final error state.
            Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
            if (cloudState.isError()) {
                Log.w(
                        TAG,
                        "The treasure "
                                + treasureName
                                + " could not be resolved. The error state was "
                                + cloudState);
                snackbarHelper.showMessageWithDismiss(
                        CloudAnchorActivity.this, getString(R.string.snackbar_resolve_error, cloudState));
                return;
            }
            snackbarHelper.showMessageWithDismiss(
                    CloudAnchorActivity.this, getString(R.string.snackbar_resolve_success));
            setNewAnchor(anchor);
            onTreasureFound(treasureName);
        }

        @Override
        public void onShowResolveMessage() {
            snackbarHelper.setMaxLines(4);
            snackbarHelper.showMessageWithDismiss(
                    CloudAnchorActivity.this, getString(R.string.snackbar_resolve_no_result_yet));
        }
    }

    public void showNoticeDialog(PrivacyNoticeDialogFragment.HostResolveListener listener) {
        DialogFragment dialog = PrivacyNoticeDialogFragment.createDialog(listener);
        dialog.show(getSupportFragmentManager(), PrivacyNoticeDialogFragment.class.getName());
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (!sharedPreferences.edit().putBoolean(ALLOW_SHARE_IMAGES_KEY, true).commit()) {
            throw new AssertionError("Could not save the user preference to SharedPreferences!");
        }
        createSession();
    }

    public boolean treasureAlreadyFound(String tName){
        for (String treasure : ftl.foundTreasures) {
            if(treasure.equals(tName))
                return true;
        }
        return false;
    }

    public boolean hasActiveTreasureHunt(){
        if(ftl.activeTH==null)
            return false;
        return true;
    }
    public boolean treasureIsPartOfTreasureHunt(String treasureName){
        for (String treasure: ftl.treasureOfActiveTH) {
            if(treasure.equals(treasureName)) return true;
        }
        return false;
    }

    private class FoundTreasuresAndActiveTHListener implements FirebaseManager.FoundTreasuresListener,
            FirebaseManager.ActiveTreasureHuntListener, FirebaseManager.TreasuresOfATreasureHuntListener,
            FirebaseManager.CompletedTreasureHuntsListener {

        public String activeTH;
        public ArrayList<String> foundTreasures=new ArrayList<>();
        public ArrayList<String> treasureOfActiveTH=new ArrayList<>();
        public ArrayList<String> completedTreasureHunts=new ArrayList<>();
        //public String treasureToCheck;

        @Override
        public void onActiveTreasureHunt(String activeTH) {
            //activeTH is null if there is no active treasure hunt
            if(activeTH==null) return;
            this.activeTH=activeTH;
            firebaseManager.getTreasuresOfATreasureHunt(this.activeTH,this);
        }

        @Override
        public void onFoundTreasures(ArrayList<String> foundTreasures) {
            this.foundTreasures=foundTreasures;
        }

        @Override
        public void onTreasuresOfATreasureHunt(ArrayList<String> thTreasures) {
            treasureOfActiveTH=thTreasures;
        }

        @Override
        public void onCompletedTreasureHunts(ArrayList<String> completedTHs) {
            if(completedTHs!=null)
                this.completedTreasureHunts=completedTHs;
        }
    }

    public void onTreasureFound(String treasureName){

        firebaseManager.addPointsToUser(username, tpqaListener.treasurePoints);
        ftl.foundTreasures.add(treasureName);

        if(ftl.foundTreasures.size()==ftl.treasureOfActiveTH.size()){
            //ako je broj nadjenih treasures jednak ukupnom broju treasures
            //to znaci da je ovaj TH sad completed
            ftl.completedTreasureHunts.add(ftl.activeTH);
            firebaseManager.makeTreasureHuntCompleted(username, ftl.completedTreasureHunts);
            firebaseManager.DeactivateTreasureHunt(username,ftl.activeTH);
        }
        else{
            firebaseManager.addTreasureToFoundTreasures(username,treasureName);
        }

        //otvoriti dijalog sa pitanjem, i cekati da korisnik unese odgovor, vrv neki listener
        //prvo pribavim iz tpqa pogresne odgovore
        String wrongAns1,wrongAns2,wrongAns3;
        wrongAns1=tpqaListener.wrongAnswers.get(0);
        wrongAns2=tpqaListener.wrongAnswers.get(1);
        wrongAns3=tpqaListener.wrongAnswers.get(2);

        QuestionDialogFragment newFragment= QuestionDialogFragment.newInstance(tpqaListener.treasureQuestion,
                tpqaListener.treasureAnswer,wrongAns1,wrongAns2,wrongAns3);
        newFragment.setOkListener(this::onQuestionAnswered);
        newFragment.show(getSupportFragmentManager(),"dialog");

    }

    private class TreasurePointsQuestionAnswerListener implements FirebaseManager.TreasureQuestionListener,
            FirebaseManager.TreasureAnswerListener, FirebaseManager.TreasurePointsListener,
            FirebaseManager.TreasureWrongAnswersListener {

        public String treasureQuestion;
        public String treasureAnswer;
        public ArrayList<String> wrongAnswers=new ArrayList<>();
        public int treasurePoints;

        @Override
        public void onTreasureQuestion(String tQuestion) {
            this.treasureQuestion=tQuestion;
        }

        @Override
        public void onTreasureAnswer(String tAnswer) {
            this.treasureAnswer=tAnswer;
        }

        @Override
        public void onTreasurePoints(int points) {
            treasurePoints=points;
        }

        @Override
        public void onTreasureWrongAnswers(ArrayList<String> wrongAnswers) {
            this.wrongAnswers=wrongAnswers;
        }
    }

    public void onQuestionAnswered(boolean isCorrect){
        if(isCorrect){
            //the user picked the right answer
            firebaseManager.addPointsToUser(username,10);
            snackbarHelper.showMessageWithDismiss(CloudAnchorActivity.this,getString(R.string.snackbar_correct_answer));
        }
        else {
            snackbarHelper.showMessageWithDismiss(CloudAnchorActivity.this,getString(R.string.snackbar_wrong_answer));
        }
    }

}