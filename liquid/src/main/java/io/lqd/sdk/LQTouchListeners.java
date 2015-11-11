package io.lqd.sdk;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class LQTouchListeners {

    public LQTouchListeners(View parent, String indent){
        int i;
        final Button b ;

        if (parent instanceof Button) {
            b = (Button) parent;
            b.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
        }
        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            for (i = 0; i < group.getChildCount(); i++)
                new LQTouchListeners(group.getChildAt(i), indent);
        }
    }
}
