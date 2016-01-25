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

package io.lqd.sdk.oneline;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import io.lqd.sdk.Liquid;

public class LQOnTouchListener implements View.OnTouchListener {
    private String mEventname;
    private Rect mRect;
    private boolean shouldClick;

    public LQOnTouchListener(String eventname) {
        mEventname = eventname;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mRect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            shouldClick = true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP && shouldClick) {
            Liquid.getInstance().track(mEventname);
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (!mRect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                shouldClick = false;
            }
        }
        return false;
    }
}
