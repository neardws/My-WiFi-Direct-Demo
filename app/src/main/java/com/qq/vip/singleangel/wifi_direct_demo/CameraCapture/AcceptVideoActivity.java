package com.qq.vip.singleangel.wifi_direct_demo.CameraCapture;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.qq.vip.singleangel.wifi_direct_demo.R;

/**
 * Created by singl on 2017/9/5.
 */

public class AcceptVideoActivity extends AppCompatActivity {
    private AcceptVideoThread thread;
    private ImageView imageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_video);

        imageView = (ImageView) findViewById(R.id.image_video);
        thread = new AcceptVideoThread(this);
        new Thread(thread).start();
    }

    public void drawPicture(Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
    }
}
