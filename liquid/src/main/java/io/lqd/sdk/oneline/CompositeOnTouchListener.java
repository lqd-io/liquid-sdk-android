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

import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CompositeOnTouchListener implements View.OnTouchListener {

    List<View.OnTouchListener> listeners;

    public CompositeOnTouchListener() {
        listeners = new ArrayList<View.OnTouchListener>();
    }

    public void addOnTouchListener(View.OnTouchListener listener) {
        listeners.add(listener);
    }

    public void removeOnTouchListener(int location) {
        listeners.remove(location);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        for (View.OnTouchListener listener : listeners)
            listener.onTouch(v, event);
        return false;
    }
}
