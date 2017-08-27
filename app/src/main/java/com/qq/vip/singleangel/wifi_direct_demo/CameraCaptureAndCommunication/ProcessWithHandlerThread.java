package com.qq.vip.singleangel.wifi_direct_demo.CameraCaptureAndCommunication;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * Created by singl on 2017/8/27.
 */

public class ProcessWithHandlerThread extends HandlerThread implements Handler.Callback{

    private static final String TAG = "ProcessWithHandlerThread";
    public static final int WHAT_PROCESS_FRAME = 1;


    public ProcessWithHandlerThread(String name) {
        super(name);
        start();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case WHAT_PROCESS_FRAME:
                byte[] frameData = (byte[]) msg.obj;
                processFrame(frameData);
                return true;
            default:
                return false;
        }
    }

    /**
     * Process with the frame data
     * With the usage of socket to communication whit other device
     * @param frameData
     */
    private void processFrame(byte[] frameData){

    }
}
