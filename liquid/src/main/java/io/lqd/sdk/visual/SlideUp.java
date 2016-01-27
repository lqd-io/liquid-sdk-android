/**
 * Copyright 2014-present Liquid Data Intelligence S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.lqd.sdk.visual;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import io.lqd.sdk.LQLog;
import io.lqd.sdk.Liquid;
import io.lqd.sdk.R;
import io.lqd.sdk.model.LQInAppMessage;


public class SlideUp implements OnTouchListener, InappMessage {

    private final SlideUp mInstance;
    private final LQInAppMessage mSlideModel;
    private boolean mNewSDK;
    private Context mContext;
    private View mRoot;
    private ViewGroup container;
    private int height;
    private PopupWindow mPopupWindow;
    private float mDy;
    private int mCurrentX;
    private int mCurrentY;


    public SlideUp(Context context, View root, LQInAppMessage slideModel) {
        mInstance = this;
        mRoot = root;
        mContext = context;
        mSlideModel = slideModel;

        if (Build.VERSION.SDK_INT < 16)
            mNewSDK = false;
        else
            mNewSDK = true;

        setUpSlideUp();
        setUpButton();
    }


    private void setUpSlideUp() {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        container = (ViewGroup) layoutInflater.inflate(R.layout.activity_slide_up, null);
        TextView mViewMessage = (TextView) container.findViewById(R.id.slideUpText);

        Typeface RegularLato = Typeface.createFromAsset(mContext.getAssets(), "fonts/Lato-Regular.ttf");

        // Set the font
        mViewMessage.setTypeface(RegularLato);

        // Set the message
        mViewMessage.setText(mSlideModel.getMessage());

        // Change Background Color
        container.findViewById(R.id.lowest_layout).setBackgroundColor(Color.parseColor(mSlideModel.getBgColor()));

        // Change Text Color
        ((TextView) container.findViewById(R.id.slideUpText)).setTextColor(Color.parseColor(mSlideModel.getMessageColor()));

        // Get View's height depending on device
        final ViewTreeObserver observer = container.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                height = container.getHeight();
            }
        });

        mPopupWindow = new PopupWindow(container, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        mPopupWindow.setFocusable(false);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Liquid.getInstance().showInAppMessages();
            }
        });
    }

    public void setUpButton() {
        final  ImageView mArrowButton = (ImageView) container.findViewById(R.id.slideUpArrowButton);

        for(final LQInAppMessage.Cta cta : mSlideModel.getCtas()) {

            Drawable myArrow = ContextCompat.getDrawable(mContext, R.drawable.arrow);

            // Change arrow button color
            myArrow.setColorFilter(Color.parseColor(cta.getButtonColor()), PorterDuff.Mode.SRC_IN);
            ((ImageView) container.findViewById(R.id.slideUpArrowButton)).setImageDrawable(myArrow);

            mArrowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mIntent = new Intent(Intent.ACTION_VIEW);
                    if (cta.getDeepLink() != null) {
                        try {
                            mIntent.setData(Uri.parse(cta.getDeepLink()));
                            Liquid.getInstance().trackCta(cta);
                            mContext.startActivity(mIntent);
                        } catch (Exception e) {
                            LQLog.infoVerbose("Canceled or not properly assigned");
                        }
                    }
                    mPopupWindow.dismiss();
                }
            });
        }
    }

    public void show() {
        show(0);
    }

    public void show(int milliseconds){

        mRoot.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mNewSDK)
                    animateNew(0, 0);

                mPopupWindow.setAnimationStyle(R.style.SlideUpAnimation);

                container.setOnTouchListener(mInstance);

                mPopupWindow.showAtLocation(mRoot, Gravity.BOTTOM, 0, 0);
            }
        }, milliseconds);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mDy = mCurrentY - event.getRawY();
        }
        if (isSlidingDown(event))
            animateDown(event);
        if (isCancelingSlideDown(event)) {
            if (mCurrentY * 1.5F >= height / 2)
                dismiss();
            else
                resetPosition();
        }
        return false;
    }

    private boolean isSlidingDown(MotionEvent event) {
        return event.getAction() == MotionEvent.ACTION_MOVE;
    }

    private boolean isCancelingSlideDown(MotionEvent event) {
        return event.getAction() == MotionEvent.ACTION_UP;
    }

    private void animateDown(MotionEvent event) {
        mCurrentY = (int) (event.getRawY() + mDy);
        if (mCurrentY > 0) {
            setTransparency(1 - (float) mCurrentY * 1 / height); //root equation: 0.5F + (float) mCurrentY * 1 / (2 * height)

            if (mNewSDK)
                animateNew(mCurrentY, 0);
            else {
                animateOld(mCurrentX, -mCurrentY, -1, -1, false);
            }
        }
    }

    private void resetPosition() {
        if (mNewSDK) {
            animateNew(0, 0);
        } else {
            animateOld(0, 0, -1, -1, true);
        }
        mCurrentX = 0;
        mCurrentY = 0;
        setTransparency(1);
    }

    private void setTransparency(float slide) {
        ViewHelper.setAlpha(container, slide);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void animateNew(float yCoordinate, int duration) {
        container.animate().y(yCoordinate * 1.5F).setDuration(duration).start();
    }

    private void animateOld(int xCoordinate, int yCoordinate, int width, int height, boolean moveOutside) {
        int yCoordinateUpdated = (int) (yCoordinate * 1.5F);
        mPopupWindow.update(xCoordinate, yCoordinateUpdated, width, height);
        mPopupWindow.setClippingEnabled(moveOutside);
    }

    private void dismiss() {
        Liquid.getInstance().trackDismiss(mSlideModel);
        mPopupWindow.dismiss();
    }
}
