package com.qq.vip.singleangel.wifi_direct_demo.CameraCapture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by singl on 2017/9/14.
 */

public class ReceiveThread extends Thread {

    private Socket socket;
    private ServerSocket serverSocket;
    private Handler handler;
    private Bitmap bitmap;
    private static final int MSG_SUCCESS = 0;
    private static final int MSG_FAILED = 1;

    public ReceiveThread(Handler handler){
        super();
        this.handler = handler;
    }

    @Override
    public void run() {
        super.run();

        byte[] buffer = new byte[1024*10];
        int len = 0;
        Message message = new Message();
        try {
            serverSocket = new ServerSocket(6789);
        }catch (IOException e){
            e.printStackTrace();
        }

        while (true){
            len = 0;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream ins = null;
            try {
                socket = serverSocket.accept();
                ins = socket.getInputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
            try {
                while ((len = ins.read(buffer)) != -1){
                    outputStream.write(buffer, 0 ,len);
                }
            }catch (IOException e){
                e.printStackTrace();
            }

            byte data[] = outputStream.toByteArray();
            outputStream.reset();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            matrix.setRotate(90);
            if (bitmap != null){
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);
                handler.obtainMessage(MSG_SUCCESS, bitmap).sendToTarget();
            }else {
                handler.obtainMessage(MSG_FAILED).sendToTarget();
            }

            try {
                ins.close();
                outputStream.flush();
                outputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }
}
