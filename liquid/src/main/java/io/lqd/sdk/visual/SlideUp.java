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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;

import io.lqd.sdk.LQLog;
import io.lqd.sdk.Liquid;
import io.lqd.sdk.R;
import io.lqd.sdk.model.LQInAppMessage;


public class SlideUp implements View.OnTouchListener {

    private final SlideUp mInstance;
    private final LQInAppMessage mSlideModel;
    private boolean mNewSDK;
    private Context mContext;
    private View mRoot;
    private ViewGroup container;
    private int height;
    private PopupWindow mPopupWindow;
    private float mDx;
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
       ImageView mArrowButton = (ImageView) container.findViewById(R.id.slideUpArrowButton);

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
                            LQLog.error("No activity to manage deeplink or typo in the deeplink's name!");
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
            mDx = mCurrentX - event.getRawX();
            mDy = mCurrentY - event.getRawY();
        }
        if (isSlidingDown(event))
            animateDown(event);
        if (isCancelingSlideDown(event))
            resetPosition();

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
        if ((mCurrentY * 2) >= height) {
            dismiss();
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
        container.animate().y(yCoordinate * 2).setDuration(duration).start();
    }

    private void animateOld(int xCoordinate, int yCoordinate, int width, int height, boolean moveOutside) {
        mPopupWindow.update(xCoordinate, yCoordinate * 2, width, height);
        mPopupWindow.setClippingEnabled(moveOutside);
    }

    private void dismiss() {
        Liquid.getInstance().trackDismiss(mSlideModel);
        mPopupWindow.dismiss();
    }
}
