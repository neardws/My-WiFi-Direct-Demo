package com.qq.vip.singleangel.wifi_direct_demo.CameraCapture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.util.Log;

import com.qq.vip.singleangel.wifi_direct_demo.MyFile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by singl on 2017/9/5.
 */

public class AcceptVideoThread extends Thread {
    private static final String TAG = "AcceptViedoThread";
    private int port = 23333;
    private Context context;

    public AcceptVideoThread(Context context){
        this.context = context;
    }

    @Override
    public void run() {
        super.run();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        }catch (IOException e){
            Log.d(TAG, "ServerSocket open failed");
        }
        while (true){
            try {
                Socket socket = serverSocket.accept();

                InputStream inputStream = socket.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                Bitmap bmp = (Bitmap) objectInputStream.readObject();

                AcceptVideoActivity activity = (AcceptVideoActivity) context;
                activity.drawPicture(bmp);

               // ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                //MyFile.copyFile(inputStream,byteArrayOutputStream);
                //byte[] data = byteArrayOutputStream.toByteArray();

               // final YuvImage image = new YuvImage(data, ImageFormat.NV21, w, h, null);
               // ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
                //if(!image.compressToJpeg(new Rect(0, 0, w, h), 100, os)){
                  //  return null;
                //}
                //byte[] tmp = os.toByteArray();
                //Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0,tmp.length);


            }catch (IOException e){
                Log.d(TAG, "Ioexception is "+e.getMessage());
            }catch (ClassNotFoundException e){
                Log.d(TAG, "ClassNotFoundException IS "+ e.getMessage());
            }
        }

    }
}
