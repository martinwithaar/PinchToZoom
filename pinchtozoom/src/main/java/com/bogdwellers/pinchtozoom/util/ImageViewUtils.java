package com.bogdwellers.pinchtozoom.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Created by Martin on 15-10-2016.
 */

public class ImageViewUtils {

    /**
     *
     * @param imageView
     * @param bitmap
     */
    public static final void updateImageViewMatrix(ImageView imageView, Bitmap bitmap) {
        updateImageViewMatrix(imageView, bitmap.getWidth(), bitmap.getHeight());
    }

    /**
     *
     * @param imageView
     * @param bitmapDrawable
     */
    public static final void updateImageViewMatrix(ImageView imageView, BitmapDrawable bitmapDrawable) {
        updateImageViewMatrix(imageView, bitmapDrawable.getIntrinsicWidth(), bitmapDrawable.getIntrinsicHeight());
    }

    /**
     *
     * @param imageView
     * @param width
     * @param height
     */
    public static final void updateImageViewMatrix(ImageView imageView, float width, float height) {
        Drawable drawable = imageView.getDrawable();
        if(drawable == null) {
            throw new NullPointerException("ImageView drawable is null");
        }
        Matrix matrix = imageView.getImageMatrix();
        if(!matrix.isIdentity()) {
            float[] values = new float[9];
            matrix.getValues(values);

            RectF src = new RectF();
            src.left = 0;
            src.top = 0;
            src.right = width;
            src.bottom = height;

            RectF dst = new RectF();
            dst.left = values[Matrix.MTRANS_X];
            dst.top = values[Matrix.MTRANS_Y];
            dst.right = dst.left + (drawable.getIntrinsicWidth() * values[Matrix.MSCALE_X]);
            dst.bottom = dst.top + (drawable.getIntrinsicHeight() * values[Matrix.MSCALE_Y]);

            matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
        }
    }
}
