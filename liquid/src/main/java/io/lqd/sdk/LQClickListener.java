package io.lqd.sdk;

import android.app.Activity;
import android.os.Build;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.Toast;

public class LQClickListener {

    private final Activity mActivity;
    private Button mB = null;

    public LQClickListener(Activity activity, View parent) {
        mActivity = activity;
        int i;

        if (parent instanceof Button) {
            mB = (Button) parent;
            final PopupMenu popup = new PopupMenu(mActivity, mB);
            popup.getMenu().add("Track");

            mB.setOnLongClickListener(new View.OnLongClickListener() {
                Boolean wantToTrack = true;

                @Override
                public boolean onLongClick(View v) {
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (wantToTrack) {
                                track();
                                wantToTrack = updateItemTitle(popup, wantToTrack);
                            } else {
                                dontTrack();
                                wantToTrack = updateItemTitle(popup, wantToTrack);
                            }
                            return true;
                        }
                    });
                    popup.show();
                    return false;
                }
            });
        }
        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            for (i = 0; i < group.getChildCount(); i++)
                new LQClickListener(mActivity, group.getChildAt(i));
        }
    }

    public void track() {
        if (Build.VERSION.SDK_INT >= 16) {
            Toast.makeText(mActivity, "Setting Accessibility Delegate", Toast.LENGTH_SHORT).show();

            mB.setAccessibilityDelegate(new View.AccessibilityDelegate() {
                @Override
                public void sendAccessibilityEvent(View host, int eventType) {
                    if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                        Liquid.getInstance().track("Button: " + mB.getText());
                    }
                }
            });
        } else {
            Toast.makeText(mActivity, "Setting OnTouch", Toast.LENGTH_SHORT).show();
            mB.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
        }
    }

    public void dontTrack() {
        if (Build.VERSION.SDK_INT >= 16) {
            Toast.makeText(mActivity, "Removing Accessibility Delegate", Toast.LENGTH_SHORT).show();
            mB.setAccessibilityDelegate(null);
        } else {
            Toast.makeText(mActivity, "Removing onTouch", Toast.LENGTH_SHORT).show();
            mB.setOnTouchListener(null);
        }
    }

    public boolean updateItemTitle(PopupMenu popupMenu, Boolean wantToTrack) {
        MenuItem item = popupMenu.getMenu().getItem(0);
        if (wantToTrack) {
            item.setTitle("Don't track");
            wantToTrack = false;
        } else {
            item.setTitle("Track");
            wantToTrack = true;
        }
        return wantToTrack;
    }
}
