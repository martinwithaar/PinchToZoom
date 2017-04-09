package com.bogdwellers.pinchtozoom;

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * 
 * @author Martin
 *
 */
public abstract class ImageMatrixCorrector extends MatrixCorrector {
	
	/*
	 * Attributes
	 */

	private ImageView imageView;
	private float scaledImageWidth;
	private float scaledImageHeight;

	/*
	 * Overrides
	 */

	@Override
	public void setMatrix(Matrix matrix) {
		super.setMatrix(matrix);
		updateScaledImageDimensions();
	}

	/*
	 * Class methods
	 */
	
	/**
	 * <p>Sets the <code>ImageView</code>. This also sets its inner image matrix as this corrector's matrix automatically.</p>
	 * @param imageView
	 */
	public void setImageView(ImageView imageView) {
		// TODO Make a weak reference or set to null in order to avoid memory leaks
		this.imageView = imageView;
		if(imageView != null) {
			setMatrix(imageView.getImageMatrix());
		}
	}

	/**
	 *
	 * @return
     */
	public ImageView getImageView() {
		return imageView;
	}

	/**
	 *
	 * @return
     */
	public float getInnerFitScale() {
		Drawable drawable = imageView.getDrawable();
		float widthRatio = (float) drawable.getIntrinsicWidth() / imageView.getWidth();
		float heightRatio = (float) drawable.getIntrinsicHeight() / imageView.getHeight();
		if(widthRatio > heightRatio) {
			return 1f / widthRatio;
		} else {
			return 1f / heightRatio;
		}
	}
	
	/**
	 * <p>(Re)calculates the image's current dimensions.</p>
	 */
	protected void updateScaledImageDimensions() {
		float[] values = getValues();
		Drawable drawable = imageView.getDrawable();
		if(drawable != null) {
			scaledImageWidth = values[Matrix.MSCALE_X] * drawable.getIntrinsicWidth();
			scaledImageHeight = values[Matrix.MSCALE_Y] * drawable.getIntrinsicHeight();
		} else {
			scaledImageWidth = scaledImageHeight = 0f;
		}
	}

	/**
	 * <p>Returns the width of the scaled image.</p>
	 * @return
     */
	protected float getScaledImageWidth() {
		return scaledImageWidth;
	}

	/**
	 * * <p>Returns the height of the scaled image.</p>
	 * @return
     */
	protected float getScaledImageHeight() {
		return scaledImageHeight;
	}
}
