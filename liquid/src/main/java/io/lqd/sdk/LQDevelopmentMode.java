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

package io.lqd.sdk;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.IntentCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class LQDevelopmentMode {

    protected void enterDevelopmentMode(Activity activity, Boolean issplashshown) {
        if (!issplashshown)
            lqSplashScreen(activity);

        liquidColorBorder(activity);
    }

    // Restarts the app
    protected void exitDevelopmentMode(final Activity activity) {
        final Intent i = activity.getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            activity.finishAffinity();
        } else {
            System.exit(0);
        }
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                activity.startActivity(i);
            }
        }, 2000);
    }

    // Adds a frame around app to indicate that the app is running
    // in Event Tracking mode.
    private void liquidColorBorder(Activity activity) {

        ViewGroup viewGr = (ViewGroup) activity.getWindow().getDecorView();
        LinearLayout borderLinearLayout = new LinearLayout(activity);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        borderLinearLayout.setLayoutParams(llp);

        GradientDrawable stroke = new GradientDrawable();

        stroke.setColor(0x00000000);

        stroke.setStroke(3, 0xFF3A98FC); //liquid color border with full opacity

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            borderLinearLayout.setBackgroundDrawable(stroke);
        } else {
            borderLinearLayout.setBackground(stroke);
        }
        borderLinearLayout.setClickable(false);
        viewGr.addView(borderLinearLayout);
    }

    // Splash screen when entering development mode
    private void lqSplashScreen(final Activity activity) {

        final int orientation = getOrientation(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        final View layout = inflater.inflate(R.layout.splash,
                (ViewGroup) activity.findViewById(R.id.splash_layout));

        final PopupWindow pw = new PopupWindow(layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        Button button = (Button) layout.findViewById(R.id.lets_go);

        pw.setAnimationStyle(R.style.DevModeFadeInOutAnimation);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); // Unlocks orientation
                pw.dismiss();
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                activity.setRequestedOrientation(orientation); // Locks the orientation so popup is not dismissed
                pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
            }
        }, 1000);
    }

    private int getOrientation(Activity activity) {
        final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        final int current_orientation = activity.getResources().getConfiguration().orientation;

        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
            if (current_orientation == Configuration.ORIENTATION_PORTRAIT)
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            else
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            if (current_orientation == Configuration.ORIENTATION_PORTRAIT)
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            else
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        }
    }
}