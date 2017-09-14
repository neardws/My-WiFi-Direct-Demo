package com.qq.vip.singleangel.wifi_direct_demo.CameraCapture;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by singl on 2017/9/14.
 */

public class TransferThread extends Thread{

    private byte bytebuff[] = new byte[1024];
    private OutputStream outsocket;
    private ByteArrayOutputStream myoutputstream;
    private String ipaddr;

    public TransferThread(ByteArrayOutputStream myoutputstream, String ipaddr){
        super();
        this.myoutputstream = myoutputstream;
        this.ipaddr = ipaddr;
        try {
            myoutputstream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            Socket tempsocket = new Socket(ipaddr,6789);
            outsocket = tempsocket.getOutputStream();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                        myoutputstream.toByteArray());
            int amount;
            while ((amount = inputStream.read(bytebuff)) != -1){
                outsocket.write(bytebuff, 0, amount);
            }
            myoutputstream.flush();
            myoutputstream.close();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
