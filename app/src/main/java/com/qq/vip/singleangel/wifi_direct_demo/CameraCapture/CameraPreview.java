package com.qq.vip.singleangel.wifi_direct_demo.CameraCapture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.qq.vip.singleangel.wifi_direct_demo.ClientList;
import com.qq.vip.singleangel.wifi_direct_demo.MyFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by singl on 2017/9/1.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback ,Camera.PreviewCallback{

    private static final String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Uri outputMediaFileUri;
    private String outputMediaFileType;
    private MediaRecorder mMediaRecorder;

    private boolean isRecording = false;

    private float oldDist = 1f;

    private TransferThread transferthread;
    private boolean isGroupOwner;


    public CameraPreview(Context context, boolean isGroupOwner){
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        this.isGroupOwner = isGroupOwner;



    }

    private void openCameraOriginal() {
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            Log.d(TAG, "camera is not available");
        }
    }


    public Camera getCameraInstance() {
        if (mCamera == null) {
            CameraHandlerThread mThread = new CameraHandlerThread("camera thread");
            synchronized (mThread) {
                mThread.openCamera();
            }
        }
        return mCamera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = getCameraInstance();
        /**
        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);

        Camera.Parameters parameters = mCamera.getParameters();
        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mCamera.setParameters(parameters);
         **/
        mCamera.setPreviewCallback(this);
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        }catch (IOException e){
            Log.d(TAG, "Error setting camera preview :"+ e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        int rotation = getDisplayOrientation();
        mCamera.setDisplayOrientation(rotation);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRotation(rotation);
        mCamera.setParameters(parameters);
        adjustDisplayRatio(rotation);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder.removeCallback(this);
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    /**
     * 获取输出文件
     * @param type
     * @return
     */
    private File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),TAG);
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed TO CREATE A DIRECTORY");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                        "IMG_"+ timeStamp + ".jpg");
            outputMediaFileType = "image/*";
        }else if (type == MEDIA_TYPE_VIDEO){
            mediaFile = new File(mediaStorageDir.getParent() + File.separator +
                    "VID_" + timeStamp +".mp4");
            outputMediaFileType = "video/*";
        }else {
            return null;
        }
        outputMediaFileUri = Uri.fromFile(mediaFile);
        return  mediaFile;
    }

    public Uri getOutputMediaFileUri(){
        return outputMediaFileUri;
    }

    public String getOutputMediaFileType(){
        return outputMediaFileType;
    }

    /**
     * 拍照功能实现
     */
    public void takePicture(final ImageView view){
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null){
                    Log.d(TAG, "Error creating media file,check storage permission");
                    return;
                }
                new SavePicture(pictureFile,data,view,camera).execute();
            }
        });
    }



    private class SavePicture extends AsyncTask<Void, Void, Void>{
        private File pictureFile;
        private byte[] data;
        private ImageView view;
        private Camera camera;
        public SavePicture(File files,byte[] data, ImageView view, Camera camera){
            super();
            this.pictureFile = files;
            this.data = data;
            this.view = view;
            this.camera = camera;
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            }catch (IOException e){
                Log.d(TAG, "Error accessing file"+ e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            view.setImageURI(getOutputMediaFileUri());
            camera.startPreview();
            super.onPostExecute(aVoid);
        }
    }

    /**
     * 录像功能实现
     * @return
     */
    public boolean startRecording(){
        if (prepareVideoRecorder()){
            new AsyncTask<Void, Void ,Void>(){

                @Override
                protected Void doInBackground(Void... params) {
                    mMediaRecorder.start();
                    isRecording = true;
                    return null;
                }
            }.execute();
            return true;
        }else {
            releaseMediaRecorder();
        }
        return false;
    }

    public void stopRecording(final ImageView view){
        if (mMediaRecorder != null){
            mMediaRecorder.stop();
            releaseMediaRecorder();
            releaseCamera();
            Bitmap thumbnail= ThumbnailUtils.createVideoThumbnail(outputMediaFileUri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
            view.setImageBitmap(thumbnail);
        }
        releaseMediaRecorder();
    }

    public boolean isRecording(){
        return isRecording;
    }

    private boolean prepareVideoRecorder(){
        mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        int rotation = getDisplayOrientation();
        mMediaRecorder.setOrientationHint(rotation);

        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, CameraPreview.this.getWidth(), CameraPreview.this.getHeight());

        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mCamera.setParameters(parameters);

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(profile);

        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).getPath());

        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());

        try {
            mMediaRecorder.prepare();
        }catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing mediarecorder" + e.getMessage());
            releaseMediaRecorder();
            return false;
        }catch (IOException e){
            Log.d(TAG, "IOException preparing mediarecorder"+ e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null){
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }


    public int getDisplayOrientation() {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        android.hardware.Camera.CameraInfo camInfo =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);

        int result = (camInfo.orientation - degrees + 360) % 360;
        return result;
    }

    private void adjustDisplayRatio(int rotation) {
        ViewGroup parent = ((ViewGroup) getParent());
        Rect rect = new Rect();
        parent.getLocalVisibleRect(rect);
        int width = rect.width();
        int height = rect.height();
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        int previewWidth;
        int previewHeight;
        if (rotation == 90 || rotation == 270) {
            previewWidth = previewSize.height;
            previewHeight = previewSize.width;
        } else {
            previewWidth = previewSize.width;
            previewHeight = previewSize.height;
        }

        if (width * previewHeight > height * previewWidth) {
            final int scaledChildWidth = previewWidth * height / previewHeight;

            layout((width - scaledChildWidth) / 2, 0,
                    (width + scaledChildWidth) / 2, height);
        } else {
            final int scaledChildHeight = previewHeight * width / previewWidth;
            layout(0, (height - scaledChildHeight) / 2,
                    width, (height + scaledChildHeight) / 2);
        }
    }

    /**
     * 接收触摸点坐标
     * @param x
     * @param y
     * @param coefficient
     * @param width
     * @param height
     * @return
     */
    private static Rect calculateTapArea(float x, float y, float coefficient, int width, int height) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / width * 2000 - 1000);
        int centerY = (int) (y / height * 2000 - 1000);

        int halfAreaSize = areaSize / 2;
        RectF rectF = new RectF(clamp(centerX - halfAreaSize, -1000, 1000)
                , clamp(centerY - halfAreaSize, -1000, 1000)
                , clamp(centerX + halfAreaSize, -1000, 1000)
                , clamp(centerY + halfAreaSize, -1000, 1000));
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private void handleFocusMetering(MotionEvent event, Camera camera) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, viewWidth, viewHeight);
        Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f, viewWidth, viewHeight);

        camera.cancelAutoFocus();
        Camera.Parameters params = camera.getParameters();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            Log.i(TAG, "focus areas not supported");
        }
        if (params.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 800));
            params.setMeteringAreas(meteringAreas);
        } else {
            Log.i(TAG, "metering areas not supported");
        }
        final String currentFocusMode = params.getFocusMode();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        camera.setParameters(params);

        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(currentFocusMode);
                camera.setParameters(params);
            }
        });
    }

    /**
     * 处理触摸事件
     * @param event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            handleFocusMetering(event, mCamera);
        }else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist) {
                        handleZoom(true, mCamera);
                    } else if (newDist < oldDist) {
                        handleZoom(false, mCamera);
                    }
                    oldDist = newDist;
                    break;
            }
        }
        return true;
    }

    /**
     * 得到两指间距
     * @param event
     * @return
     */
    private static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void handleZoom(boolean isZoomIn, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            camera.setParameters(params);
        } else {
            Log.i(TAG, "zoom not supported");
        }
    }

    /**
     * 处理帧数据
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        Camera.Size size = camera.getParameters().getPreviewSize();
        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
                size.height, null);
        if (image != null) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, size.width, size.height),
                    80, outStream);
            try {
                outStream.flush();
                if (isGroupOwner){
                    ClientList clientList = MyFile.getClient();
                    for (String s : clientList.getClientIps()){
                        transferthread = new TransferThread(outStream, s);
                        transferthread.start();
                    }
                }else {
                    transferthread = new TransferThread(outStream, "192.168.49.1");
                    transferthread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class CameraHandlerThread extends HandlerThread {
        Handler mHandler;

        public CameraHandlerThread(String name) {
            super(name);
            start();
            mHandler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }

        void openCamera() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    openCameraOriginal();
                    notifyCameraOpened();
                }
            });
            try {
                wait();
            } catch (InterruptedException e) {
                Log.w(TAG, "wait was interrupted");
            }
        }
    }


}
