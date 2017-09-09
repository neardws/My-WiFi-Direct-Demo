package com.qq.vip.singleangel.wifi_direct_demo.CameraCaptureAndCommunication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.qq.vip.singleangel.wifi_direct_demo.R;

import java.net.ServerSocket;
import java.net.Socket;


/**
 * Created by singl on 2017/8/27.
 */


public class ReciveVideoActivity extends AppCompatActivity {

    private TextView mStatus;
    private ImageView mCameraView;
    public Bitmap mLastFrame;
    private int face_count;
    private final Handler handler = new ServerHandler(this);

    private FaceDetector mFaceDetector = new FaceDetector(320,240,10);
    private FaceDetector.Face[] faces = new FaceDetector.Face[10];
    private PointF tmp_point = new PointF();
    private Paint tmp_paint = new Paint();

    private boolean isRunning; //程序运行标志

    public void close(){
        isRunning = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recive_video);
        mCameraView = (ImageView) findViewById(R.id.camera_preview);
        mStatus = (TextView) findViewById(R.id.textView);

        try {
            //mClient = new MyClientThread(s, handler);
            MyServerThread serverThread = new MyServerThread(handler);
            new Thread(serverThread).start();
            mStatus.setText("Socket accpeted");
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... unused) {
                // Background Code
                try {
                    ServerSocket serverSocket = new ServerSocket(9191);
                    Socket socket = serverSocket.accept();
                    //mClient = new MyClientThread(s, handler);
                    MyServerThread serverThread = new MyServerThread(socket,handler);
                    new Thread(serverThread).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

        }.execute(); **/
        drawPicture.run();

        //new Thread(drawPicture).start();
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        if (source != null){
            Bitmap retVal;

            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
            source.recycle();
            return retVal;
        }
        return null;
    }

    private Runnable drawPicture = new Runnable() {
        @Override
        public void run() {
            try {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mLastFrame!=null){

                            Bitmap mutableBitmap = mLastFrame.copy(Bitmap.Config.RGB_565, true);
                            face_count = mFaceDetector.findFaces(mLastFrame, faces);
                            mStatus.setText("Face Count: " + String.valueOf(face_count));
                            System.out.println("Face Count: " + String.valueOf(face_count));
                            Log.d("Face_Detection", "Face Count: " + String.valueOf(face_count));


                            Canvas canvas = new Canvas(mutableBitmap);

                            for (int i = 0; i < face_count; i++) {
                                FaceDetector.Face face = faces[i];
                                tmp_paint.setColor(Color.RED);
                                tmp_paint.setAlpha(100);
                                face.getMidPoint(tmp_point);
                                canvas.drawCircle(tmp_point.x, tmp_point.y, face.eyesDistance(),
                                        tmp_paint);
                            }

                            mCameraView.setImageBitmap(mutableBitmap);
                        }

                    }
                }); //this function can change value of mInterval.
            } finally {
                handler.postDelayed(drawPicture,1000/15);
            }
        }
    };



}
