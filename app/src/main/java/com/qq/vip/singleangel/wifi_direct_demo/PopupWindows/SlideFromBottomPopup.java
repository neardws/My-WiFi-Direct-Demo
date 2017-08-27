package com.qq.vip.singleangel.wifi_direct_demo.PopupWindows;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;


import com.qq.vip.singleangel.wifi_direct_demo.R;

/**
 * Created by singl on 2017/8/25.
 */

public class SlideFromBottomPopup extends BasePopupWindow  {

    private View popupView;
    private ImageButton btn_Photo;
    private ImageButton btn_Video;

    public SlideFromBottomPopup(final Activity context) {
        super(context);
        btn_Photo = (ImageButton) findViewById(R.id.btn_photo);
        btn_Video = (ImageButton) findViewById(R.id.btn_video);

        btn_Photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_Video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



    }

    @Override
    protected Animation initShowAnimation() {
        return getTranslateAnimation(250 * 2, 0, 300);
    }

    @Override
    public View getClickToDismissView() {
        return popupView.findViewById(R.id.click_to_dismiss);
    }

    @Override
    public View onCreatePopupView() {
        popupView = LayoutInflater.from(getContext()).inflate(R.layout.popup_slide_from_bottom, null);
        return popupView;
    }

    @Override
    public View initAnimaView() {
        return popupView.findViewById(R.id.popup_anima);
    }


}
