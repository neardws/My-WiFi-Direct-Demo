package com.qq.vip.singleangel.wifi_direct_demo.CameraCaptureAndCommunication;

import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.qq.vip.singleangel.wifi_direct_demo.FileTransferService;
import com.qq.vip.singleangel.wifi_direct_demo.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import static com.qq.vip.singleangel.wifi_direct_demo.CameraCaptureAndCommunication.SettingFragment.mCamera;

/**
 * Created by singl on 2017/8/27.
 */

public class CameraCaptureActivity extends AppCompatActivity{

    private static final String TAG = "CameraCaptureActivity";

    private InetAddress inetAddress;
    private final Handler handler = new ClientHandler(this);
    public CameraPreview preview;

    private MediaRecorder mediaRecorder;
    private Camera camera;


    private File mOutputFile;
    private boolean isRecording = false;


    //public ClientHandler handler;

    public void setByte(ByteArrayOutputStream buff){
        preview.setmFrameBuffer(buff);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_capture);



        Intent intent = getIntent();
        inetAddress = (InetAddress) intent.getSerializableExtra("intentaddress");

        Button but_StartPreview = (Button) findViewById(R.id.button_start_preview);
        Button but_StopPreview = (Button) findViewById(R.id.button_stop_preview);


        but_StartPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startPreview();
            }
        });
        but_StopPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPreview();
            }
        });

   //    startPreview();

    }

    public void startPreview(){

        preview = new CameraPreview(this, inetAddress,handler);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        frameLayout.addView(preview);

        SettingFragment.passCamera(preview.getCameraInstance());
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SettingFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
        SettingFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

        Button but_Setting = (Button) findViewById(R.id.button_settings);
        but_Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.camera_preview,
                        new SettingFragment()).addToBackStack(null).commit();
            }
        });


        if (isRecording) {
            // BEGIN_INCLUDE(stop_release_media_recorder)

            // stop recording and release camera
            try {
                mediaRecorder.stop();  // stop the recording
            } catch (RuntimeException e) {
                // RuntimeException is thrown when stop() is called immediately after start().
                // In this case the output file is not properly constructed ans should be deleted.
                //Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                //noinspection ResultOfMethodCallIgnored
            }
            releaseMediaRecorder(); // release the MediaRecorder object
            camera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            isRecording = false;
            releaseCamera();
            // END_INCLUDE(stop_release_media_recorder)

        } else {

            // BEGIN_INCLUDE(prepare_start_media_recorder)

            //if (prepareVideoRecorder()){
            //    mediaRecorder.start();
            //}


            new MediaPrepareTask().execute(null, null, null);
            // END_INCLUDE(prepare_start_media_recorder)

        }


        /**
        Thread cThread = new Thread(new MyClientThread(this,
                "192.168.49.1",23333));
        cThread.start();
         **/

    }

    public void stopPreview(){
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        frameLayout.removeAllViews();
        releaseCamera();
        releaseMediaRecorder();
    }

    public void onPause(){
        finish();
        super.onPause();

    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null){
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    private void releaseCamera(){
        if (camera != null){
            camera.release();
            camera = null;
        }
    }

    private boolean prepareVideoRecorder(){
        camera = CameraHelper.getDefaultCameraInstance();
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, preview.getWidth(), preview.getHeight());


        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        camera.setParameters(parameters);

        // END_INCLUDE (configure_preview)

        mediaRecorder = new MediaRecorder();

        //mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);

        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //mediaRecorder.setVideoSize(480, 320);
        //mediaRecorder.setVideoFrameRate(10); // 每秒3帧

        mediaRecorder.setProfile(profile);

        // Step 4: Set output file
        mOutputFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO);
        if (mOutputFile == null) {
            return false;
        }
        mediaRecorder.setOutputFile(mOutputFile.getPath());
        // END_INCLUDE (configure_media_recorder)

        /**
        try {
            Socket receiver = new Socket("192.168.49.1", 23333);
            ParcelFileDescriptor pfd = ParcelFileDescriptor
                    .fromSocket(receiver);
            mediaRecorder.setOutputFile(pfd.getFileDescriptor());
        }catch (IOException e){
            e.printStackTrace();
        }

        Socket receiver = new Socket("192.168.1.149", 9999);
        ParcelFileDescriptor pfd = ParcelFileDescriptor
                .fromSocket(receiver);
        mediaRecorder.setOutputFile(pfd.getFileDescriptor());
         **/

        try {
            mediaRecorder.prepare();
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

    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mediaRecorder.start();

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
                CameraCaptureActivity.this.finish();
            }
            // inform the user that recording has started
           // setCaptureButtonText("Stop");

        }
    }


}
