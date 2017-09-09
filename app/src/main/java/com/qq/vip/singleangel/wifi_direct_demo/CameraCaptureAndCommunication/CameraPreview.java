package com.qq.vip.singleangel.wifi_direct_demo.CameraCaptureAndCommunication;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Created by singl on 2017/8/27.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{

    private static final String TAG = "CameraPreview";
    private SurfaceHolder surfaceHolder;
    private Camera camera;


    private int width = 320;
    private int height = 240;

    public ByteArrayOutputStream mFrameBuffer;
    private Context context;

    private Handler handler;

    private InetAddress inetAddress;

    public CameraPreview(Context context,InetAddress inetAddress,Handler handler) {
        super(context);
        this.handler = handler;
        this.context = context;
        this.inetAddress = inetAddress;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }
    public void setmFrameBuffer(ByteArrayOutputStream buffer){
        this.mFrameBuffer = buffer;
    }

    private void openCameraOriginal(){
        try {
            camera = Camera.open();
        }catch (Exception e){
            Log.d(TAG, "Camera is not available.");
        }
    }

    public Camera getCameraInstance(){
        if (camera == null){
            CameraHandlerThead cameraHandlerThead = new CameraHandlerThead("camera thread");
            synchronized (cameraHandlerThead){
                cameraHandlerThead.openCamera();
            }
        }
        return camera;
    }

    private class CameraHandlerThead extends HandlerThread{

        Handler handler;

        public CameraHandlerThead(String name) {
            super(name);
            start();
            handler = new Handler(getLooper());
        }

        synchronized void notifCameraOpened(){
            notify();
        }

        void openCamera(){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    openCameraOriginal();
                    notifCameraOpened();
                }
            });
            try {
                wait();
            }catch (InterruptedException e){
                Log.d(TAG, "wait is interrupted.");
            }
        }

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);
        Message message = new Message();
        message.obj = data;
        handler.sendMessage(message);

        /**
        try{
            //convert YuvImage(NV21) to JPEG Image data
            YuvImage yuvimage=new YuvImage(data,ImageFormat.NV21,this.width,this.height,null);
            System.out.println("WidthandHeight"+yuvimage.getHeight()+"::"+yuvimage.getWidth());
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0,0,this.width,this.height),100,baos);
            mFrameBuffer = baos;
        }catch(Exception e){
            Log.d("parse","errpr");
        }
         **/

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = getCameraInstance();
        try {
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallbackWithBuffer(this);
            camera.addCallbackBuffer(new byte[((width * height)
                    * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);
        }catch (Exception e){
            e.printStackTrace();
        }
        /**
        camera.setPreviewCallback(this);
        try {
            camera.setPreviewDisplay(holder);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(320,240);
            this.width=parameters.getPreviewSize().width;
            this.height=parameters.getPreviewSize().height;
            parameters.setPreviewFormat(ImageFormat.NV21);
            camera.setParameters(parameters);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        }catch (IOException e){
            Log.d(TAG,"error setting camera preview:"+ e.getMessage());
        }
         */
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try{
            camera.stopPreview();
        } catch (Exception e){
            e.printStackTrace();
        }
        try{
            //Configration Camera Parameter(full-size)
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(320,240);
            this.width=parameters.getPreviewSize().width;
            this.height=parameters.getPreviewSize().height;
            parameters.setPreviewFormat(ImageFormat.NV21);
            camera.setParameters(parameters);
            camera.setDisplayOrientation(90);
            camera.setPreviewCallback(this);
            camera.startPreview();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        holder.removeCallback(this);
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;

    }
}
