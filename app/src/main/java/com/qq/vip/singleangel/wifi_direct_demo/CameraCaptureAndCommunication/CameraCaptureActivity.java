package com.qq.vip.singleangel.wifi_direct_demo.CameraCaptureAndCommunication;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.qq.vip.singleangel.wifi_direct_demo.R;

/**
 * Created by singl on 2017/8/27.
 */

public class CameraCaptureActivity extends AppCompatActivity{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_capture);

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

    }

    public void startPreview(){
        final CameraPreview preview = new CameraPreview(this);
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
    }

    public void stopPreview(){
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        frameLayout.removeAllViews();
    }

    public void onPause(){
        finish();
        super.onPause();
    }
}
