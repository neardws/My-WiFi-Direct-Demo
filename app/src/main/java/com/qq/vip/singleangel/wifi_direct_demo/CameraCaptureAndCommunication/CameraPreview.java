package com.qq.vip.singleangel.wifi_direct_demo.CameraCaptureAndCommunication;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by singl on 2017/8/27.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{

    private static final String TAG = "CameraPreview";
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    //Handler Thread
    private ProcessWithHandlerThread processFrameHandlerThread;
    private Handler processFrameHandler;

    public CameraPreview(Context context) {
        super(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        processFrameHandlerThread = new ProcessWithHandlerThread("process frame");
        processFrameHandler = new Handler(processFrameHandlerThread.getLooper(),processFrameHandlerThread);
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
        processFrameHandler.obtainMessage(
                ProcessWithHandlerThread.WHAT_PROCESS_FRAME,data);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        getCameraInstance();
        camera.setPreviewCallback(this);
        try {
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        }catch (IOException e){
            Log.d(TAG,"error setting camera preview:"+ e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

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
