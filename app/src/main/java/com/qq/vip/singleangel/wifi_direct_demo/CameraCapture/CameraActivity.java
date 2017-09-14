package com.qq.vip.singleangel.wifi_direct_demo.CameraCapture;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.qq.vip.singleangel.wifi_direct_demo.ClientList;
import com.qq.vip.singleangel.wifi_direct_demo.FileTransferService;
import com.qq.vip.singleangel.wifi_direct_demo.MyFile;
import com.qq.vip.singleangel.wifi_direct_demo.R;

public class CameraActivity extends AppCompatActivity {

    private CameraPreview cameraPreview;
    private FrameLayout frameLayout;
    public static final String ISGroupOwner = "IS_GROUP_OWNER";
    private boolean isGroupOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Intent intent = getIntent();
        isGroupOwner = intent.getExtras().getBoolean(ISGroupOwner);

        cameraPreview = new CameraPreview(this, isGroupOwner);
        frameLayout = (FrameLayout) findViewById(R.id.camera_pre);
        frameLayout.addView(cameraPreview);

        final ImageView mediaPreview  = (ImageView)findViewById(R.id.media_preview);
        final Button buttonCapturePhoto = (Button) findViewById(R.id.button_capture_picture);
        buttonCapturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraPreview.takePicture(mediaPreview);
            }
        });
        final Button buttonCaptureVideo = (Button) findViewById(R.id.button_capture_video);
        buttonCaptureVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraPreview.isRecording()) {
                    cameraPreview.stopRecording(mediaPreview);
                    buttonCaptureVideo.setText("录像");
                }else{
                    if(cameraPreview.startRecording()) {
                        buttonCaptureVideo.setText("停止");
                    }
                }
            }
        });
        mediaPreview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CameraActivity.this,ShowPhotoVideo.class);
                intent.setDataAndType(cameraPreview.getOutputMediaFileUri(),cameraPreview.getOutputMediaFileType());
                startActivityForResult(intent,0);
            }
        });



    }

    public void startPreview() {
        cameraPreview = new CameraPreview(this,isGroupOwner);
        frameLayout = (FrameLayout) findViewById(R.id.camera_pre);
        frameLayout.addView(cameraPreview);

        SettingFragment.passCamera(cameraPreview.getCameraInstance());
        PreferenceManager.setDefaultValues(this, R.xml.mypreferences, false);
        SettingFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
        SettingFragment.init(PreferenceManager.getDefaultSharedPreferences(this));

        Button buttonSettings = (Button) findViewById(R.id.button_setting);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.camera_pre, new SettingFragment()).addToBackStack(null).commit();
            }
        });
    }

    public void stopPreview() {
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_pre);
        preview.removeAllViews();
    }

    /**
    private void initCamera(){
        cameraPreview = new CameraPreview(this);
        frameLayout = (FrameLayout) findViewById(R.id.camera_pre);
        frameLayout.addView(cameraPreview);

        SettingFragment.passCamera(cameraPreview.getCameraInstance());
        PreferenceManager.setDefaultValues(this, R.xml.mypreferences, false);
        SettingFragment.setDefault(PreferenceManager.getDefaultSharedPreferences(this));
        SettingFragment.init(PreferenceManager.getDefaultSharedPreferences(this));
    }**/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tools_item,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.atn_video_start:

                startPreview();
               // cameraPreview.startRecording();
                return true;
            case R.id.atn_video_stop:
                stopPreview();
                //final ImageView mediaPreview  = (ImageView)findViewById(R.id.media_preview);
                //cameraPreview.stopRecording(mediaPreview);
                return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        frameLayout = (FrameLayout) findViewById(R.id.camera_pre);
        frameLayout.removeAllViews();

    }

    @Override
    protected void onPause() {
        finish();
        super.onPause();
    }
}
