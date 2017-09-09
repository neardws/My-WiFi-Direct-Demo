package com.qq.vip.singleangel.wifi_direct_demo.CameraCapture;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.util.Log;

import com.qq.vip.singleangel.wifi_direct_demo.DeviceDetailFragment;
import com.qq.vip.singleangel.wifi_direct_demo.WiFiDirectActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * Created by singl on 2017/9/2.
 */

public class ProcessWithThreadPool {
    private static final String TAG = "ThreadPool";
    private static final int KEEP_ALIVE_TIME = 10;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
    private BlockingQueue<Runnable> workQueue;
    private ThreadPoolExecutor mThreadPool;

    private int w = 1080;
    private int h = 1920;

    public ProcessWithThreadPool() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maximumPoolSize = corePoolSize * 2;
        workQueue = new LinkedBlockingQueue<>();
        mThreadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, KEEP_ALIVE_TIME, TIME_UNIT, workQueue);
    }

    public synchronized void post(final byte[] frameData) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                processFrame(frameData);
            }
        });
    }

    private void processFrame(byte[] frameData) {
        Log.i(TAG, "test");
        String host = "192.168.49.1";
        int port = 23333;
        int SOCKET_TIMEOUT = 5000;

        Socket socket = new Socket();
        try {
            Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

            final YuvImage image = new YuvImage(frameData, ImageFormat.NV21, w, h, null);
            ByteArrayOutputStream os = new ByteArrayOutputStream(frameData.length);
            if(!image.compressToJpeg(new Rect(0, 0, w, h), 100, os)){
           //     return null;
            }
            byte[] tmp = os.toByteArray();
            Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0,tmp.length);

            Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
            OutputStream stream = socket.getOutputStream();

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);
            objectOutputStream.writeObject(bmp);
           // ByteArrayInputStream inputStream = new ByteArrayInputStream(frameData);

           // DeviceDetailFragment.copyFile(inputStream, stream);
            Log.d(WiFiDirectActivity.TAG, "Client: Data written");
        } catch (IOException e) {
            Log.e(WiFiDirectActivity.TAG, e.getMessage());
        } finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // Give up
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}

