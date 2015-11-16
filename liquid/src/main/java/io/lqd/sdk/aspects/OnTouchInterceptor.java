package io.lqd.sdk.aspects;

import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import io.lqd.sdk.LQLog;
import io.lqd.sdk.Liquid;

@Aspect
public class OnTouchInterceptor {

    public static final String TOUCHEVENT = "execution(* android.view.View.OnTouchListener.onTouch(..))";

    private Rect mRect;
    private boolean shouldClick;


    @Pointcut(TOUCHEVENT)
    public void touchevent() {
    }

    @Before("touchevent()")
    public void onTouchEvent(JoinPoint joinPoint) {

        if (Build.VERSION.SDK_INT >= 16)
            return;

        MotionEvent event = (MotionEvent) joinPoint.getArgs()[1];

        if ( joinPoint.getArgs()[0] instanceof Button) {
            Button b = (Button) joinPoint.getArgs()[0];

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d("OnTouchInterceptor", "ACTION DOWN");
                mRect = new Rect(b.getLeft(), b.getTop(), b.getRight(), b.getBottom());
                shouldClick = true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP && shouldClick) {
                Log.d("OnTouchInterceptor", "CLICKED");
                Liquid.getInstance().track(("Button: " + b.getText()));
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if(!mRect.contains(b.getLeft() + (int) event.getX(), b.getTop() + (int) event.getY())){
                    shouldClick = false;
                }
            }
        }
        else {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                LQLog.warning("This is not a <Button>, so no tracking for this View...");
        }
    }
}
