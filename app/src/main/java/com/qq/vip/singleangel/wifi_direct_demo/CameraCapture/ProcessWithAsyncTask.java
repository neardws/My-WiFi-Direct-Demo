package com.qq.vip.singleangel.wifi_direct_demo.CameraCapture;
import android.os.AsyncTask;
import android.util.Log;
/**
 * Created by singl on 2017/9/2.
 */

public class ProcessWithAsyncTask extends AsyncTask<byte[], Void, String> {
    private static final String TAG = "AsyncTask";

    @Override
    protected String doInBackground(byte[]... params) {
        processFrame(params[0]);
        return "test";
    }

    private void processFrame(byte[] frameData) {
        Log.i(TAG, "test");
    }
}
