package io.lqd.sdk.visual;

import android.os.Build;
import android.view.View;
import android.view.animation.Animation;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * A proxy class to allow for modifying post-3.0 view properties on all pre-3.0
 * platforms. <strong>DO NOT</strong> wrap your views with this class if you
 * are using {@code ObjectAnimator} as it will handle that itself.
 */
public final class AnimatorProxy extends Animation {
    /** Whether or not the current running platform needs to be proxied. */
    public static final boolean NEEDS_PROXY = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;

    private final WeakReference<View> mView;

    private float mAlpha = 1;

    private static final WeakHashMap<View, AnimatorProxy> PROXIES =
            new WeakHashMap<View, AnimatorProxy>();

    /**
     * Create a proxy to allow for modifying post-3.0 view properties on all
     * pre-3.0 platforms. <strong>DO NOT</strong> wrap your views if you are
     * using {@code ObjectAnimator} as it will handle that itself.
     *
     * @param view View to wrap.
     * @return Proxy to post-3.0 properties.
     */
    public static AnimatorProxy wrap(View view) {
        AnimatorProxy proxy = PROXIES.get(view);
        // This checks if the proxy already exists and whether it still is the animation of the given view
        if (proxy == null || proxy != view.getAnimation()) {
            proxy = new AnimatorProxy(view);
            PROXIES.put(view, proxy);
        }
        return proxy;
    }

    private AnimatorProxy(View view) {
        setDuration(0); //perform transformation immediately
        setFillAfter(true); //persist transformation beyond duration
        view.setAnimation(this);
        mView = new WeakReference<View>(view);
    }

    public float getAlpha() {
        return mAlpha;
    }
    public void setAlpha(float alpha) {
        if (mAlpha != alpha) {
            mAlpha = alpha;
            View view = mView.get();
            if (view != null) {
                view.invalidate();
            }
        }
    }
}
