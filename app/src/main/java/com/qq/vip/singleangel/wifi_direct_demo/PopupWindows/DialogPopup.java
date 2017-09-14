package com.qq.vip.singleangel.wifi_direct_demo.PopupWindows;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.CycleInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.qq.vip.singleangel.wifi_direct_demo.CameraCapture.CameraActivity;
import com.qq.vip.singleangel.wifi_direct_demo.CameraCapture.CameraPreview;
import com.qq.vip.singleangel.wifi_direct_demo.CameraCapture.VideoActivity;
import com.qq.vip.singleangel.wifi_direct_demo.ClientList;
import com.qq.vip.singleangel.wifi_direct_demo.DeviceDetailFragment;
import com.qq.vip.singleangel.wifi_direct_demo.FileTransferService;
import com.qq.vip.singleangel.wifi_direct_demo.MyFile;
import com.qq.vip.singleangel.wifi_direct_demo.R;

import java.net.InetAddress;

/**
 * Created by singl on 2017/8/25.
 */

public class DialogPopup extends BasePopupWindow{

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private ImageView img_photo;
    private ImageView img_video;
    private String action_Start_Server = "ACTION_START_SERVER";
    private InetAddress inetAddress;
    public static final String isGroupOwner = "IS_GROUP_OWNER";


    public DialogPopup(final Activity context, final DeviceDetailFragment deviceDetailFragment) {
        super(context);

        inetAddress = deviceDetailFragment.getGroupOwnerInet();
        img_photo = (ImageView) findViewById(R.id.img_photo);
        img_video = (ImageView) findViewById(R.id.img_video);

        img_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceDetailFragment.sendPicture();
            }
        });
        img_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                /**
                if (deviceDetailFragment.isGroupowner()){
                    ClientList clientList = MyFile.getClient();
                    for (String s : clientList.getClientIps()){
                        Intent intent = new Intent(getContext(), FileTransferService.class);
                        intent.setAction(FileTransferService.ACTION_START_RECEIVE_VIDEO);
                        intent.putExtra(FileTransferService.EXTRAS_ADDRESS, s);
                        intent.putExtra(FileTransferService.EXTRAS_PORT,9999);
                        context.startService(intent);
                    }
                }else {
                    Intent intent = new Intent(getContext(), FileTransferService.class);
                    intent.setAction(FileTransferService.ACTION_START_RECEIVE_VIDEO);
                    intent.putExtra(FileTransferService.EXTRAS_ADDRESS, "192.168.49.1");
                    intent.putExtra(FileTransferService.EXTRAS_PORT,9999);
                    context.startService(intent);
                }
                 **/

                if (deviceDetailFragment.isGroupowner()){
                    /**
                    List<String> clientIp = deviceDetailFragment.getClientIps();
                    for (String string : clientIp){
                        Intent intent = new Intent();
                        intent.putExtra("SERVERIP",string);
                        context.startActivity(intent);

                    } **/
                    Intent intent = new Intent(getContext(), CameraActivity.class);
                    intent.putExtra(DialogPopup.isGroupOwner, true);
                    context.startActivity(intent);
                }else {

                    Intent serverIntent = new Intent(getContext(), FileTransferService.class);
                    serverIntent.setAction(FileTransferService.ACTION_START_RECEIVE_VIDEO);
                    serverIntent.putExtra(FileTransferService.EXTRAS_ADDRESS,"192.168.49.1");
                    serverIntent.putExtra(FileTransferService.EXTRAS_PORT,22645);
                    context.startService(serverIntent);


                    Intent intent = new Intent(getContext(), CameraActivity.class);
                    intent.putExtra(DialogPopup.isGroupOwner, false);
                    context.startActivity(intent);



                }
            }
        });
    }

    @Override
    protected Animation initShowAnimation() {
        AnimationSet set=new AnimationSet(false);
        Animation shakeAnima=new RotateAnimation(0,15,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        shakeAnima.setInterpolator(new CycleInterpolator(5));
        shakeAnima.setDuration(400);
        set.addAnimation(getDefaultAlphaAnimation());
        set.addAnimation(shakeAnima);
        return set;
    }

    @Override
    public View getClickToDismissView() {
        return getPopupWindowView();
    }

    @Override
    public View onCreatePopupView() {
        return createPopupById(R.layout.popup_selete_source);
    }

    @Override
    public View initAnimaView() {
        return findViewById(R.id.popup_anima);
    }

}