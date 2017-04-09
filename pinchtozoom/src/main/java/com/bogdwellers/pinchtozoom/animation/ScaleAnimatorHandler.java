package com.bogdwellers.pinchtozoom.animation;

import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.widget.ImageView;

import com.bogdwellers.pinchtozoom.ImageMatrixCorrector;

/**
 * Created by Martin on 12-10-2016.
 */

public class ScaleAnimatorHandler extends AbsCorrectorAnimatorHandler {

    private static final String TAG = ScaleAnimatorHandler.class.getSimpleName();

    private float px;
    private float py;
    private boolean translate;

    public ScaleAnimatorHandler(ImageMatrixCorrector corrector) {
        super(corrector);
        this.translate = false;
    }

    public ScaleAnimatorHandler(ImageMatrixCorrector corrector, float px, float py) {
        super(corrector);
        this.px = px;
        this.py = py;
        this.translate = true;
   }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        ImageMatrixCorrector corrector = getCorrector();
        ImageView imageView = corrector.getImageView();
        if(imageView.getDrawable() != null) {
            Matrix matrix = imageView.getImageMatrix();
            float[] values = getValues();
            matrix.getValues(values);

            float sx = (float) animation.getAnimatedValue();
            sx = corrector.correctAbsolute(Matrix.MSCALE_X, sx) / values[Matrix.MSCALE_X];

            if (translate) {
                matrix.postScale(sx, sx, px, py);
            } else {
                matrix.postScale(sx, sx);
            }
            corrector.performAbsoluteCorrections();
            imageView.invalidate();
        }
    }
}
