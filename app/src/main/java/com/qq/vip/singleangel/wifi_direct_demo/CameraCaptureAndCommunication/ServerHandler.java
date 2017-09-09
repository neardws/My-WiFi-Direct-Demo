package com.qq.vip.singleangel.wifi_direct_demo.CameraCaptureAndCommunication;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by singl on 2017/8/30.
 */

public class ServerHandler extends Handler{
    private final WeakReference<ReciveVideoActivity> mActivity;

    public ServerHandler(ReciveVideoActivity activity) {
        mActivity = new WeakReference<ReciveVideoActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        ReciveVideoActivity activity = mActivity.get();
        if (activity != null) {
            try {
                activity.mLastFrame = (Bitmap) msg.obj;
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    }
}
