package com.bogdwellers.pinchtozoom.animation;

import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.widget.ImageView;

import com.bogdwellers.pinchtozoom.ImageMatrixCorrector;

/**
 * Created by Martin on 12-10-2016.
 */

public class FlingAnimatorHandler extends AbsCorrectorAnimatorHandler {

    public static final String PROPERTY_TRANSLATE_X = "translateX";
    public static final String PROPERTY_TRANSLATE_Y = "translateY";

    public FlingAnimatorHandler(ImageMatrixCorrector corrector) {
        super(corrector);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        ImageMatrixCorrector corrector = getCorrector();
        ImageView imageView = corrector.getImageView();
        Matrix matrix = imageView.getImageMatrix();
        float[] values = getValues();
        matrix.getValues(values);

        float dx = (float) animation.getAnimatedValue(PROPERTY_TRANSLATE_X);
        dx = corrector.correctAbsolute(Matrix.MTRANS_X, dx) - values[Matrix.MTRANS_X];

        float dy = (float) animation.getAnimatedValue(PROPERTY_TRANSLATE_Y);
        dy = corrector.correctAbsolute(Matrix.MTRANS_Y, dy) - values[Matrix.MTRANS_Y];

        matrix.postTranslate(dx, dy);
        imageView.invalidate();
    }
}
