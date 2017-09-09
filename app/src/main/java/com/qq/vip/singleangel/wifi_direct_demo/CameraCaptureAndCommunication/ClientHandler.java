package com.qq.vip.singleangel.wifi_direct_demo.CameraCaptureAndCommunication;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

/**
 * Created by singl on 2017/8/31.
 */

public class ClientHandler extends Handler{
    private int width = 320;
    private int height = 240;
    private final WeakReference<CameraCaptureActivity> activity;

    public ClientHandler(CameraCaptureActivity activity){
        this.activity = new WeakReference<CameraCaptureActivity>(activity);
    }

    @Override
    public void handleMessage(Message message){
        byte[] data = (byte[]) message.obj;
        CameraCaptureActivity cameraCaptureActivity = activity.get();
        try{
            //convert YuvImage(NV21) to JPEG Image data
            YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21,this.width,this.height,null);
            System.out.println("WidthandHeight"+yuvimage.getHeight()+"::"+yuvimage.getWidth());
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0,0,this.width,this.height),100,baos);
            cameraCaptureActivity.setByte(baos);
            Thread cThread = new Thread(new MyClientThread(cameraCaptureActivity,
                    "192.168.49.1",23333));
            cThread.start();
        }catch(Exception e){
            Log.d("parse","errpr");
        }
    }
}
