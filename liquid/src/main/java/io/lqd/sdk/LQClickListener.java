package io.lqd.sdk;

import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;

public class LQClickListener {

    public LQClickListener(View parent){
        int i;
        final Button b ;

        if (parent instanceof Button) {
            b = (Button) parent;

            if (Build.VERSION.SDK_INT >= 14) {
                b.setAccessibilityDelegate(new View.AccessibilityDelegate() {
                    @Override
                    public void sendAccessibilityEvent(View host, int eventType) {
                        if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                            Liquid.getInstance().track("Button: " + b.getText());
                        }
                    }
                });
            } else {
                b.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
            }
        }
        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            for (i = 0; i < group.getChildCount(); i++)
                new LQClickListener(group.getChildAt(i));
        }
    }
}
