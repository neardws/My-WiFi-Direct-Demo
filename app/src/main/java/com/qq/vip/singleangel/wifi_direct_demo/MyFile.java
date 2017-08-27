package com.qq.vip.singleangel.wifi_direct_demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by singl on 2017/8/18.
 */

public class MyFile {
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


}