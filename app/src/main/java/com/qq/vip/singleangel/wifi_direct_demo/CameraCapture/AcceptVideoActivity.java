package com.qq.vip.singleangel.wifi_direct_demo.CameraCapture;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.qq.vip.singleangel.wifi_direct_demo.R;

/**
 * Created by singl on 2017/9/5.
 */

public class AcceptVideoActivity extends AppCompatActivity {
    private ImageView imageView;
    private Thread receiveThread;
    private static final int MSG_SUCCESS = 0;
    private static final int MSG_FAILED = 1;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_SUCCESS:
                    imageView.setImageBitmap((Bitmap) msg.obj);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    break;

                case MSG_FAILED:
                    imageView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_video);

        imageView = (ImageView) findViewById(R.id.image_video);
        receiveThread = new ReceiveThread(handler);
        receiveThread.start();
    }

}
