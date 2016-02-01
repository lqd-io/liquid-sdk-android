package io.lqd.sdk.visual;

import android.view.View;

import static io.lqd.sdk.visual.AnimatorProxy.NEEDS_PROXY;
import static io.lqd.sdk.visual.AnimatorProxy.wrap;

public final class ViewHelper {
    private ViewHelper() {}

    public static float getAlpha(View view) {
        return NEEDS_PROXY ? wrap(view).getAlpha() : Honeycomb.getAlpha(view);
    }

    public static void setAlpha(View view, float alpha) {
        if (NEEDS_PROXY) {
            wrap(view).setAlpha(alpha);
        } else {
            Honeycomb.setAlpha(view, alpha);
        }
    }

    private static final class Honeycomb {
        private static float getAlpha(View view) {
            return view.getAlpha();
        }

        private static void setAlpha(View view, float alpha) {
            view.setAlpha(alpha);
        }
    }
}
