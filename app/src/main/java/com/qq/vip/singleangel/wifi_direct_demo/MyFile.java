package com.qq.vip.singleangel.wifi_direct_demo;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by singl on 2017/8/18.
 */

public class MyFile {

    private static final String TAG = "MyFile";

    private static final String FILE_URL = Environment.getExternalStorageDirectory() + "/"
            + "com.qq.vip.singleangel.wifi_direct_demo"  + "/wificlientip" + ".txt";

    public static ClientList getClient(){
        File file = new File(FILE_URL);
        try {
            InputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            ClientList clientIP = (ClientList) objectInputStream.readObject();
            return clientIP;
        }catch (FileNotFoundException e){
            return null;
        }catch (IOException e){
            return null;
        }catch (ClassNotFoundException e){
            return null;
        }
    }

    public static boolean ClearFile(){  //好像并不需要，每次将文件中内容重写即可
        boolean isSuccess = false;
        File file = new File(FILE_URL);
        if (file.exists()){
            isSuccess = file.delete();
        }
        return isSuccess;
    }

    public static boolean addClient(String clientIp){
        File f = new File(FILE_URL);
        if (!f.exists()){
            boolean isSuccess = false;
            try {
               isSuccess = f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (isSuccess){
                Log.d(TAG, "Create File Success");
            }else {
                Log.d(TAG, "Create File Failed.");
            }

            try {
                OutputStream outputStream = new FileOutputStream(f);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                ClientList clientList = new ClientList();
                clientList.addClient(clientIp);
                objectOutputStream.writeObject(clientList);
                outputStream.close();
                return true;

            }catch (FileNotFoundException e){
                return false;
            }catch (IOException e){
                return false;
            }

        }else {     //file f exited
            try {
                InputStream inputStream = new FileInputStream(f);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                ClientList clientIP = (ClientList) objectInputStream.readObject();
                clientIP.addClient(clientIp);


                OutputStream outputStream = new FileOutputStream(f);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
               // ClientList clientList = new ClientList();
               // clientList.addClient(clientIp);
                objectOutputStream.writeObject(clientIP);
                outputStream.close();
                inputStream.close();
                return true;
            } catch (IOException e) {
                return false;
            } catch (ClassNotFoundException e) {
                return false;
            }

        }
    }



    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean copyFile(BufferedInputStream inputStream, ByteArrayOutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }


}