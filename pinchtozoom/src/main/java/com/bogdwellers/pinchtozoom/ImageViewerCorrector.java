package com.bogdwellers.pinchtozoom;

import android.graphics.Matrix;
import android.widget.ImageView;

/**
 * <p>This <code>MatrixCorrector</code> implementation defines the default behavior for an image viewer.</p>
 * <p>It works properly only if the following two conditions are met:</p>
 * <ol>
 * <li>There are no rotations</li>
 * <li>The scaling is uniform: <code>sx</code> and <code>sy</code> are always the same value</li>
 * </ol>
 * @author Martin
 *
 */
public class ImageViewerCorrector extends ImageMatrixCorrector {

	public static final String TAG = ImageViewerCorrector.class.getSimpleName();
	
	/*
	 * Attributes
	 */
	
	private float maxScale;
	private boolean maxScaleRelative;
	
	/*
	 * Constructor(s)
	 */
	
	public ImageViewerCorrector() {
		this(null, 4f);
	}
	
	public ImageViewerCorrector(ImageView imageView, float maxScale) {
		super();
		if(imageView != null) setImageView(imageView);
		this.maxScale = maxScale;
	}

	/*
	 * Class methods
	 */

	/**
	 * <p>Returns the maximum allowed scale.</p>
	 * @return
     */
	public float getMaxScale() {
		return maxScale;
	}

	/**
	 * <p>Sets the maximum allowed scale.</p>
	 * @param maxScale
     */
	public void setMaxScale(float maxScale) {
		this.maxScale = maxScale;
	}

	/**
	 * <p>Indicates whether the maximum scale should be relative to the inner fit scale.</p>
	 * @return
     */
	public boolean isMaxScaleRelative() {
		return maxScaleRelative;
	}

	/**
	 * <p>Sets whether the maximum scale should be relative to the inner fit scale.</p>
	 * @param maxScaleRelative
     */
	public void setMaxScaleRelative(boolean maxScaleRelative) {
		this.maxScaleRelative = maxScaleRelative;
	}

	/*
	 * Overrides
	 */
	
	@Override
	public void performAbsoluteCorrections() {
		super.performAbsoluteCorrections();
		
		// Calculate the image's new dimensions
		updateScaledImageDimensions();

		// Correct scale
		//values[Matrix.MSCALE_X] = values[Matrix.MSCALE_Y] = correctAbsolute(Matrix.MSCALE_X, values[Matrix.MSCALE_X]);

		// Correct the translations
		float[] values = getValues();
		values[Matrix.MTRANS_X] = correctAbsolute(Matrix.MTRANS_X, values[Matrix.MTRANS_X]);
		values[Matrix.MTRANS_Y] = correctAbsolute(Matrix.MTRANS_Y, values[Matrix.MTRANS_Y]);

		// Update the matrix
		getMatrix().setValues(values);
	}

	@Override
	public float correctAbsolute(int vector, float x) {
		switch(vector) {
			case Matrix.MTRANS_X:
				return correctTranslation(x, getImageView().getWidth(), getScaledImageWidth());
			case Matrix.MTRANS_Y:
				return correctTranslation(x, getImageView().getHeight(), getScaledImageHeight());
			case Matrix.MSCALE_X:
			case Matrix.MSCALE_Y:
				float innerFitScale = getInnerFitScale();
				float maxScale = maxScaleRelative ? innerFitScale * this.maxScale : this.maxScale;
				return Math.max(Math.min(x, maxScale), innerFitScale);
			default:
				throw new IllegalArgumentException("Vector not supported");
		}
	}

	/*
	 * Static methods
	 */

	/**
	 * <p>Corrects the translation so that it does not exceed the allowed bounds.</p>
	 * @param translation
	 * @param viewDim
	 * @param imgDim
	 * @return
	 */
	public static final float correctTranslation(float translation, float viewDim, float imgDim) {
		if(imgDim < viewDim) {
			// Must center
			translation = (viewDim / 2) - (imgDim / 2);
		} else {
			float diff = imgDim - viewDim;
			translation = Math.max(Math.min(0, translation), -diff);
		}
		return translation;
	}
}
