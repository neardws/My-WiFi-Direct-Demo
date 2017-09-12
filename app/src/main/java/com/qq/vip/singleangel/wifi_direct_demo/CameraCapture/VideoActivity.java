package com.qq.vip.singleangel.wifi_direct_demo.CameraCapture;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.qq.vip.singleangel.wifi_direct_demo.ClientList;
import com.qq.vip.singleangel.wifi_direct_demo.FileTransferService;
import com.qq.vip.singleangel.wifi_direct_demo.MyFile;
import com.qq.vip.singleangel.wifi_direct_demo.PopupWindows.DialogPopup;
import com.qq.vip.singleangel.wifi_direct_demo.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by singl on 2017/9/12.
 */

public class VideoActivity extends AppCompatActivity {

    private Camera mCamera;
    private TextureView mPreview;
    private MediaRecorder mMediaRecorder;
    private File mOutputFile;


    private float oldDist = 1f;
    private boolean isRecording = false;
    private static final String TAG = "Recorder";

    private boolean isGroupOwner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Intent intent = getIntent();
        isGroupOwner = intent.getExtras().getBoolean(DialogPopup.isGroupOwner);

        mPreview = (TextureView) findViewById(R.id.surface_view);

        mPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount() == 1) {
                    handleFocusMetering(event, mCamera);
                }else {
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_POINTER_DOWN:
                            oldDist = getFingerSpacing(event);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float newDist = getFingerSpacing(event);
                            if (newDist > oldDist) {
                                handleZoom(true, mCamera);
                            } else if (newDist < oldDist) {
                                handleZoom(false, mCamera);
                            }
                            oldDist = newDist;
                            break;
                    }
                }
                return true;
            }
        });
    }
    /**
     * 得到两指间距
     * @param event
     * @return
     */
    private static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void handleZoom(boolean isZoomIn, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            camera.setParameters(params);
        } else {
            Log.i(TAG, "zoom not supported");
        }
    }

    /**
     * 接收触摸点坐标
     * @param x
     * @param y
     * @param coefficient
     * @param width
     * @param height
     * @return
     */
    private static Rect calculateTapArea(float x, float y, float coefficient, int width, int height) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / width * 2000 - 1000);
        int centerY = (int) (y / height * 2000 - 1000);

        int halfAreaSize = areaSize / 2;
        RectF rectF = new RectF(clamp(centerX - halfAreaSize, -1000, 1000)
                , clamp(centerY - halfAreaSize, -1000, 1000)
                , clamp(centerX + halfAreaSize, -1000, 1000)
                , clamp(centerY + halfAreaSize, -1000, 1000));
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private void handleFocusMetering(MotionEvent event, Camera camera) {
        int viewWidth = mPreview.getWidth();
        int viewHeight = mPreview.getHeight();
        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, viewWidth, viewHeight);
        Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f, viewWidth, viewHeight);

        camera.cancelAutoFocus();
        Camera.Parameters params = camera.getParameters();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            Log.i(TAG, "focus areas not supported");
        }
        if (params.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 800));
            params.setMeteringAreas(meteringAreas);
        } else {
            Log.i(TAG, "metering areas not supported");
        }
        final String currentFocusMode = params.getFocusMode();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        camera.setParameters(params);

        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(currentFocusMode);
                camera.setParameters(params);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tools_item,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.atn_video_start:
                onCaptureClick();
                return true;
            case R.id.atn_video_stop:
                onCaptureClick();
                String fileUri = null;
                //fileUri = mOutputFile.getAbsolutePath();
                //fileUri = Uri.fromFile(mOutputFile).toString();
                fileUri = mOutputFile.getAbsolutePath();
                if (isGroupOwner){
                    ClientList clientList = MyFile.getClient();
                    if (clientList.size() > 0){
                        for (String s : clientList.clientIps){
                            Intent intentService = new Intent(this, FileTransferService.class);
                            intentService.setAction(FileTransferService.ACTION_SEND_VIDEO);
                            intentService.putExtra(FileTransferService.EXTRAS_ADDRESS,s);
                            intentService.putExtra(FileTransferService.EXTRAS_PORT,26655);
                            intentService.putExtra(FileTransferService.EXTRAS_FILE_PATH,"file:"+fileUri);
                            startService(intentService);
                        }
                        return true;
                    }
                }else {
                    Intent intentService = new Intent(this, FileTransferService.class);
                    intentService.setAction(FileTransferService.ACTION_SEND_VIDEO);
                    intentService.putExtra(FileTransferService.EXTRAS_ADDRESS,"192.168.49.1");
                    intentService.putExtra(FileTransferService.EXTRAS_PORT,25566);
                    intentService.putExtra(FileTransferService.EXTRAS_FILE_PATH,"file:"+fileUri);
                    startService(intentService);
                    return true;
                }


        }
        return false;
    }

    public void onCaptureClick() {
        if (isRecording) {
            // BEGIN_INCLUDE(stop_release_media_recorder)

            // stop recording and release camera
            try {
                mMediaRecorder.stop();  // stop the recording
            } catch (RuntimeException e) {
                // RuntimeException is thrown when stop() is called immediately after start().
                // In this case the output file is not properly constructed ans should be deleted.
                Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                //noinspection ResultOfMethodCallIgnored
                mOutputFile.delete();
            }
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            isRecording = false;
            releaseCamera();
            // END_INCLUDE(stop_release_media_recorder)

        } else {

            // BEGIN_INCLUDE(prepare_start_media_recorder)

            new MediaPrepareTask().execute(null, null, null);

            // END_INCLUDE(prepare_start_media_recorder)

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // if we are using MediaRecorder, release it first
        releaseMediaRecorder();
        // release the camera immediately on pause event
        releaseCamera();
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    public int getDisplayOrientation() {
        Display display = ((WindowManager) VideoActivity.this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        android.hardware.Camera.CameraInfo camInfo =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);

        int result = (camInfo.orientation - degrees + 360) % 360;
        return result;
    }

    private boolean prepareVideoRecorder(){

        // BEGIN_INCLUDE (configure_preview)
        mCamera = CameraHelper.getDefaultCameraInstance();
        mCamera.setDisplayOrientation(getDisplayOrientation());

        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, mPreview.getWidth(), mPreview.getHeight());

        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mCamera.setParameters(parameters);
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            return false;
        }
        // END_INCLUDE (configure_preview)


        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT );
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(profile);

        // Step 4: Set output file
        mOutputFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO);
        if (mOutputFile == null) {
            return false;
        }
        mMediaRecorder.setOutputFile(mOutputFile.getPath());
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    /**
     * Asynchronous task for preparing the {@link android.media.MediaRecorder} since it's a long blocking
     * operation.
     */
    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();

                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                VideoActivity.this.finish();
            }
            // inform the user that recording has started
        }
    }

}
