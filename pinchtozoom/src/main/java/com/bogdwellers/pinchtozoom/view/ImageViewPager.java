package com.bogdwellers.pinchtozoom.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * <p><code>ViewPager</code> implementation that allows sideways scrolling only when current image is not zoomed in.</p>
 * Created by Martin on 16-10-2016.
 */
public class ImageViewPager extends ViewPager {

    private static final String TAG = ImageViewPager.class.getSimpleName();
    private static final float DEFAULT_SCALE_THRESHOLD = 1.2f;

    /*
     * Attributes
     */

    private float scaleThreshold;
    private int pointerCount;

	/*
	 * Constructor(s)
	 */

    public ImageViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.scaleThreshold = DEFAULT_SCALE_THRESHOLD;
    }

    /*
     * Class methods
     */

    /**
     * <p>Sets the scale threshold.</p>
     * @param scaleThreshold
     */
    public void setScaleThreshold(float scaleThreshold) {
        this.scaleThreshold = scaleThreshold;
    }

	/*
	 * Overrides
	 */

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        pointerCount = ev.getPointerCount();
        requestDisallowInterceptTouchEvent(pointerCount > 1);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if(v instanceof ImageView) {
            ImageView iv = (ImageView) v;
            Drawable drawable = iv.getDrawable();
            if(drawable != null) {
                float vw = iv.getWidth();
                float vh = iv.getHeight();
                float dw = drawable.getIntrinsicWidth();
                float dh = drawable.getIntrinsicHeight();

                Matrix matrix = iv.getImageMatrix();
                matrix.getValues(VALUES);
                float tx = VALUES[Matrix.MTRANS_X] + dx;
                float sdw = dw * VALUES[Matrix.MSCALE_X];

                //Log.d(TAG, "sdw: " + sdw + " vw: " + vw);

                return VALUES[Matrix.MSCALE_X] / centerInsideScale(vw, vh, dw, dh) > scaleThreshold && !translationExceedsBoundary(tx, vw, sdw) && sdw > vw && pointerCount == 1; // Assumes x-y scales are equal
            }
        }
        return super.canScroll(v, checkV, dx, x, y);
    }

	/*
	 * Static methods
	 */

    /**
     * NOT Thread safe! (But it all happens on the UI thread anyway)
     */
    private static final float[] VALUES = new float[9];

    /**
     * <p>Returns the scale ratio between view and drawable for the longest side.</p>
     * @param vw
     * @param vh
     * @param dw
     * @param dh
     * @return
     */
    public static final float centerInsideScale(float vw, float vh, float dw, float dh) {
        return vw / vh <= dw / dh ? vw / dw : vh / dh;
    }

    /**
     * <p>Determines whether a translation makes the view exceed the boundary of a drawable.</p>
     * @param tx
     * @param vw
     * @param dw
     * @return
     */
    public static final boolean translationExceedsBoundary(float tx, float vw, float dw) {
        return dw >= vw && (tx > 0 || tx < vw - dw);
    }
}