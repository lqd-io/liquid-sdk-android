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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;
import java.util.HashMap;

import io.lqd.sdk.oneline.CompositeOnTouchListener;
import io.lqd.sdk.oneline.LQOnTouchListener;

public class LQClickListener {

    public static final String PREF_UIELEMENTS_FILE = "LQUiElements";

    private final Activity mActivity;
    private final SharedPreferences mPreferences;
    private final ViewGroup mViewGr;
    HashMap<String, String> mHashMap = new HashMap<>();
    HashMap<View, RelativeLayout> mHashMapLayout = new HashMap<>();

    private int i;
    private android.support.v7.widget.PopupMenu mPopupLowerApi;
    private String mElementIdentifier;
    private CompositeOnTouchListener mGroupListener;
    private View.OnTouchListener mReflectionOnTouchListener;

    public LQClickListener(Activity activity) {
        mActivity = activity;
        mPreferences = mActivity.getApplicationContext()
                .getSharedPreferences(PREF_UIELEMENTS_FILE, Context.MODE_PRIVATE);
        mHashMap = (HashMap<String, String>) mPreferences.getAll();
        mViewGr = (ViewGroup) mActivity.getWindow().getDecorView();

        i = 1;
    }

    // Searches for clickable views in the layout
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void searchForButtons(final View parent, Boolean indevelopment) {
        if (parent.isClickable() && parent.getVisibility() == View.VISIBLE
                && !(parent instanceof WebView) && !(parent instanceof EditText)) {

            try {
                mElementIdentifier = parent.getResources().getResourceEntryName(parent.getId());
            } catch (Resources.NotFoundException e) {
                mElementIdentifier = "element" + i;
                i++;
            }

            if (!indevelopment)
                checkIfTracked(parent, mElementIdentifier);
            else {
                PopupMenu mPopup = new PopupMenu(mActivity, parent);

                addPopupMenu(parent, mPopup, mElementIdentifier);
            }
        }

        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            for (int i = 0; i < group.getChildCount(); i++)
                searchForButtons(group.getChildAt(i), indevelopment);
        }
    }

    // Checks if the ui element is tracked
    private void checkIfTracked(View view, String elementidentifier) {
        if(isTracked(elementidentifier))
            track(view, mHashMap.get(getElementsPathName(elementidentifier)));
    }

    private boolean isTracked(String elementidentifier) {
        return mHashMap.containsKey(getElementsPathName(elementidentifier));
    }

    // Returns the path name of the ui element
    private String getElementsPathName(String elementidentifier) {
        return getPckgANDClssName(mActivity) + elementidentifier;
    }

    // Returns the package and class that the ui element is in to
    private String getPckgANDClssName(Activity activity) {
        return activity.getClass().getCanonicalName() + "/";
    }

    // Adds accessibility or onTouchListener to the ui element
    private void track(final View view, final String eventname) {

        if (Build.VERSION.SDK_INT >= 14) {
            addAccessibility(view, eventname);
        } else {
            addOnTouchListener(view, eventname);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void addAccessibility(View view, final String eventname) {
        view.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void sendAccessibilityEvent(View host, int eventType) {
                if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                    Liquid.getInstance().track(eventname);
                }
            }
        });
    }

    private void addOnTouchListener(View view, final String eventname) {

        mReflectionOnTouchListener = getOnTouchListener(view);

        View.OnTouchListener mLQTouchListener = new LQOnTouchListener(eventname);

        mGroupListener = new CompositeOnTouchListener();

        if(mReflectionOnTouchListener != null) {
            if(mReflectionOnTouchListener instanceof  CompositeOnTouchListener ||
                    mReflectionOnTouchListener instanceof LQOnTouchListener) {
                view.setOnTouchListener(mReflectionOnTouchListener);
            } else {
                mGroupListener.addOnTouchListener(mReflectionOnTouchListener);

                mGroupListener.addOnTouchListener(mLQTouchListener);

                view.setOnTouchListener(mGroupListener);
            }
        } else {
            view.setOnTouchListener(mLQTouchListener);
        }
    }

    // Stop tracking the element
    private void dontTrack(View view) {

        if (Build.VERSION.SDK_INT >= 14) {
            mViewGr.removeView(mHashMapLayout.get(view));
            view.setAccessibilityDelegate(null);
        } else {
            mReflectionOnTouchListener = getOnTouchListener(view);

            if (mReflectionOnTouchListener instanceof LQOnTouchListener) {
                view.setOnTouchListener(null);
            } else if (mReflectionOnTouchListener instanceof CompositeOnTouchListener) {
                mGroupListener = (CompositeOnTouchListener) mReflectionOnTouchListener;
                mGroupListener.removeOnTouchListener(1);
                view.setOnTouchListener(mGroupListener);
            }
        }
    }

    // Saves the ui element to be tracked in the shared preferences and
    // to the server.
    private void save(String elementidentifier, String eventname) {
        String identifier = getElementsPathName(elementidentifier);

        mHashMap.put(identifier, eventname);
        mPreferences.edit().putString(identifier, eventname).apply();

        Liquid.getInstance().uiElementAdd(identifier, eventname);
    }

    // Removes the ui element from the shared preferences and from the server list
    private void remove(String elementidentifier) {
        String identifier = getElementsPathName(elementidentifier);

        mHashMap.remove(identifier);
        mPreferences.edit().remove(identifier).apply();

        Liquid.getInstance().uiElementsRemove(identifier);
    }

    // Basically it's remove() then save() followed by element change command
    // for websocket
    private void change(String elementidentifier, String eventname) {
        String identifier = getElementsPathName(elementidentifier);

        mHashMap.remove(identifier);
        mPreferences.edit().remove(identifier).apply();

        mHashMap.put(identifier, eventname);
        mPreferences.edit().putString(identifier, eventname).apply();

        Liquid.getInstance().uiElementsChange(identifier, eventname);
    }

    // Adds border around the trackable ui elements, red if it's tracked
    // liquid's blue if it's not.
    private void addBorder(Activity activity, View view, Boolean tracked) {

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];

        RelativeLayout borderRelativeLayout = new RelativeLayout(activity);
        RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(view.getWidth(), view.getHeight());

        borderRelativeLayout.setLayoutParams(llp);

        GradientDrawable stroke = new GradientDrawable();

        stroke.setColor(0x00000000);

        if (!tracked)
            stroke.setStroke(3, 0xFF3A98FC); //liquid color border with full opacity
        else
            stroke.setStroke(3, 0xFFFF0000); //red color border with full opacity

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            borderRelativeLayout.setBackgroundDrawable(stroke);
        } else {
            borderRelativeLayout.setBackground(stroke);
            borderRelativeLayout.setX(x);
            borderRelativeLayout.setY(y);
        }

        mHashMapLayout.put(view, borderRelativeLayout);
        mViewGr.addView(borderRelativeLayout);
    }

    // Adds popupMenu to the trackable ui elements and the appropriate
    // border around them
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void addPopupMenu(final View view, final PopupMenu popupmenu, final String elementidentifier) {

        if (!isTracked(elementidentifier)) {
            popupmenu.getMenu().add(0, 1, 0, "Add event");
            addBorder(mActivity, view, false);
        }
        else {
            popupmenu.getMenu().add(0, 1, 0, "Remove event");
            popupmenu.getMenu().add(0, 2, 0, "Rename event");
            addBorder(mActivity, view, true);
        }
        view.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(final View v) {

                popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case 1:
                                if (!isTracked(elementidentifier)) {
                                    chooseEventName(v, popupmenu, elementidentifier);
                                } else {
                                    remove(elementidentifier);
                                    popupmenu.getMenu().getItem(0).setTitle("Add event");
                                    popupmenu.getMenu().removeItem(1);
                                }
                                break;
                            case 2:
                                changeEventName(v, elementidentifier);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });

                popupmenu.show();
                return false;
            }
        });
    }

    private void changeEventName(final View v, final String elementidentifier) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
        alert.setTitle("Set the event name");
        alert.setMessage("Element Identifier: " + getElementsPathName(elementidentifier));

        final EditText input = new EditText(mActivity);
        alert.setView(input);
        input.setText(mHashMap.get(getElementsPathName(elementidentifier)));
        alert.setPositiveButton("RENAME", new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void onClick(DialogInterface dialog, int whichButton) {
                change(elementidentifier, input.getText().toString());
            }
        });

        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    // Set the event name to track
    private void chooseEventName(final View v, final PopupMenu popupmenu, final String elementidentifier) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
        final EditText input = new EditText(mActivity);

        alert.setTitle("Set the event name");
        alert.setMessage("Element Identifier: " + getElementsPathName(elementidentifier));
        alert.setView(input);
        alert.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void onClick(DialogInterface dialog, int whichButton) {
                popupmenu.getMenu().getItem(0).setTitle("Remove event");
                save(elementidentifier, input.getText().toString());
            }
        });

        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    // Method to get the OnTouchListener on pre-API 14 devices
    // using reflection
    private View.OnTouchListener getOnTouchListener(View view) {
        View.OnTouchListener retrievedListener = null;
        String viewStr = "android.view.View";
        Field field;

        try {
            field = Class.forName(viewStr).getDeclaredField("mOnTouchListener");
            field.setAccessible(true);
            retrievedListener = (View.OnTouchListener) field.get(view);
        } catch (NoSuchFieldException ex) {
            LQLog.error("Reflection, No Such Field.");
        } catch (IllegalAccessException ex) {
            LQLog.error("Reflection, Illegal Access.");
        } catch (ClassNotFoundException ex) {
            LQLog.error("Reflection , Class Not Found.");
        }

        return retrievedListener;
    }

    public void removeBorderLayouts() {
        for (HashMap.Entry<View, RelativeLayout> entry : mHashMapLayout.entrySet()) {
            mViewGr.removeView(entry.getValue());
        }
        mHashMapLayout.clear();
        i = 1;
    }

    /**
     * Methods for lower API's
     */
    public void searchForButtonsLower(final View parent, Boolean indevelopment) {
        if (parent.isClickable()) {

            mPopupLowerApi = new android.support.v7.widget.PopupMenu(mActivity, parent);

            try {
                mElementIdentifier = parent.getResources().getResourceEntryName(parent.getId());
            } catch (Exception e) {
                mElementIdentifier = "element" + i;
                i++;
            }

            checkIfTracked(parent, mElementIdentifier);
            checkIfInDevelopmentLower(parent, indevelopment);
        }

        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            for (int i = 0; i < group.getChildCount(); i++)
                searchForButtonsLower(group.getChildAt(i), indevelopment);
        }
    }

    private void checkIfInDevelopmentLower(View parent, Boolean indevelopment) {
        if (indevelopment) {
            addPopupMenuLower(parent, mPopupLowerApi, mElementIdentifier);
        }
    }

    private void addPopupMenuLower(View view, final android.support.v7.widget.PopupMenu popupmenu, final String elementidentifier) {

        if (!isTracked(elementidentifier)) {
            popupmenu.getMenu().add(0, 1, 0, "Add event");
        }
        else {
            popupmenu.getMenu().add(0, 1, 0, "Remove event");
            popupmenu.getMenu().add(0, 2, 0, "Rename event");
        }

        view.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(final View v) {

                popupmenu.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case 1:
                                if (!isTracked(elementidentifier)) {
                                    chooseEventNameLower(v, popupmenu, elementidentifier);
                                } else {
                                    remove(elementidentifier);
                                    popupmenu.getMenu().getItem(0).setTitle("Add event");
                                    popupmenu.getMenu().removeItem(1);
                                }
                                break;
                            case 2:
                                changeEventName(v, elementidentifier);
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });

                popupmenu.show();
                return false;
            }
        });
    }

    private void chooseEventNameLower(final View v, final android.support.v7.widget.PopupMenu popupmenu, final String elementidentifier) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
        final EditText input = new EditText(mActivity);

        alert.setTitle("Set the event name");
        alert.setMessage("Element Identifier: " + getElementsPathName(elementidentifier));
        alert.setView(input);
        alert.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                popupmenu.getMenu().getItem(0).setTitle("Remove event");
                save(elementidentifier, input.getText().toString());
            }
        });

        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }
}