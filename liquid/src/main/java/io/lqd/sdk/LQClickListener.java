package io.lqd.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

public class LQClickListener {

    public static final String PREF_BUTTONS_FILE = "LQButtons";

    private final Activity mActivity;
    private final View mParent;
    private final SharedPreferences mPreferences;
    private Button mB = null;
    HashMap<String, Boolean> hashMap = new HashMap<>();

    public LQClickListener(Activity activity) {
        mActivity = activity;
        mParent = mActivity.getWindow().getDecorView();
        mPreferences = mActivity.getApplicationContext()
                .getSharedPreferences(PREF_BUTTONS_FILE, Context.MODE_PRIVATE);
        hashMap = (HashMap<String, Boolean>) mPreferences.getAll();

        searchForButtons(mParent);
    }

    private void searchForButtons(View parent) {
        if (parent instanceof Button) {
            mB = (Button) parent;
            final PopupMenu popup = new PopupMenu(mActivity, mB);

            if(!wantToTrack(parent))
                popup.getMenu().add("Track");
            else {
                popup.getMenu().add("Don't track");
                track(parent);
            }

            mB.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(final View v) {
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            if (!wantToTrack(v)) {
                                track(v);
                                updateItemTitle(popup, wantToTrack(v));
                            } else {
                                dontTrack(v);
                                updateItemTitle(popup, wantToTrack(v));
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
            for (int i = 0; i < group.getChildCount(); i++)
                searchForButtons(group.getChildAt(i));
        }
    }

    private void track(View v) {
        final Button button = (Button) v;
        if (Build.VERSION.SDK_INT >= 16) {
            button.setAccessibilityDelegate(new View.AccessibilityDelegate() {
                @Override
                public void sendAccessibilityEvent(View host, int eventType) {
                    if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                        Liquid.getInstance().track("\"" + getButtonIdOrText(button) + "\" button pressed");
                    }
                }
            });
        } else {
            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
        }
        save(button);
    }

    private void dontTrack(View v) {
        Button button = (Button) v;
        if (Build.VERSION.SDK_INT >= 16) {
            button.setAccessibilityDelegate(null);
        } else {
            button.setOnTouchListener(null);
        }
        remove(button);
    }

    private void save(Button button) {
        hashMap.put(getButtonIdOrText(button), true);
        mPreferences.edit().putString(getButtonIdOrText(button), hashMap
                .get(getButtonIdOrText(button)).toString()).apply();
    }

    private void remove(Button button) {
        hashMap.remove(getButtonIdOrText(button));
        mPreferences.edit().remove(getButtonIdOrText(button)).apply();
    }

    private void updateItemTitle(PopupMenu popupMenu, Boolean wantToTrack) {
        MenuItem item = popupMenu.getMenu().getItem(0);
        if (wantToTrack) {
            item.setTitle("Don't track");
        } else {
            item.setTitle("Track");
        }
    }

    private boolean wantToTrack(View v) {
        Button button = (Button) v;
        return hashMap.containsKey(getButtonIdOrText(button));
    }

    private String getButtonIdOrText(Button button) {
        if (button.getId() == -1)
            return button.getText().toString();
        else
            return String.valueOf(button.getResources().getResourceEntryName(button.getId()));
    }
}
