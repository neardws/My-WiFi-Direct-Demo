package com.qq.vip.singleangel.wifi_direct_demo.CameraCaptureAndCommunication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by singl on 2017/8/30.
 */

public class ReceiveVideoView extends View{
    public Bitmap mNextFrame;
    public int face_count;
    private FaceDetector faceDetector = new FaceDetector(320,240,10);
    private FaceDetector.Face[] faces = new FaceDetector.Face[10];
    private PointF tmp_point = new PointF();
    private Paint tmp_paint = new Paint();

    public ReceiveVideoView(Context context){
        super(context);
        faces = new FaceDetector.Face[10];
    }

    public ReceiveVideoView(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        faces = new FaceDetector.Face[10];
    }

    public void updateImage(Bitmap frame) {
        // Set internal configuration to RGB_565
        this.mNextFrame = frame;
        face_count = faceDetector.findFaces(mNextFrame, faces);
        Log.d("Face_Detection", "Face Count: " + String.valueOf(face_count));
    }

    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(mNextFrame, 0, 0, null);
        for (int i = 0; i < face_count; i++) {
            FaceDetector.Face face = faces[i];
            tmp_paint.setColor(Color.RED);
            tmp_paint.setAlpha(100);
            face.getMidPoint(tmp_point);
            canvas.drawCircle(tmp_point.x, tmp_point.y, face.eyesDistance(),
                    tmp_paint);
        }
    }
}
