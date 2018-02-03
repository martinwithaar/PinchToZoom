package com.bogdwellers.pinchtozoom;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.bogdwellers.pinchtozoom.animation.FlingAnimatorHandler;
import com.bogdwellers.pinchtozoom.animation.ScaleAnimatorHandler;

/**
 * <p>The <code>ImageMatrixTouchHandler</code> enables pinch-zoom, pinch-rotate and dragging on an <code>ImageView</code>.
 * Registering an instance of this class to an <code>ImageView</code> is the only thing you need to do.</p>
 * 
 * TODO Make event methods (for easy overriding)
 * 
 * @author Martin
 *
 */
public class ImageMatrixTouchHandler extends MultiTouchListener {
	
    public static final int NONE = 0;
    public static final int DRAG = 1;
    public static final int PINCH = 2;
    public static final int MORPH = 3; // TODO For three or more touch points
    private static final float MIN_PINCH_DIST_PIXELS = 10f;
	public static final String TAG = ImageMatrixTouchHandler.class.getSimpleName();
    
    /*
     * Attributes
     */

	private ImageMatrixCorrector corrector;
    private Matrix savedMatrix;
    private int mode;
    private PointF startMid;
    private PointF mid;
    private float startSpacing;
    private float startAngle;
	private float pinchVelocity;
	private boolean rotateEnabled;
	private boolean scaleEnabled;
	private boolean translateEnabled;
    private boolean dragOnPinchEnabled;
	private long doubleTapZoomDuration;
	private long flingDuration;
	private long zoomReleaseDuration;
	private long pinchVelocityWindow;
	private float doubleTapZoomFactor;
	private float doubleTapZoomOutFactor;
	private float flingExaggeration;
	private float zoomReleaseExaggeration;
    private boolean updateTouchState;
	private GestureDetector gestureDetector;
	private ValueAnimator valueAnimator;

    /*
     * Constructor(s)
     */
    
    public ImageMatrixTouchHandler(Context context) {
    	this(context, new ImageViewerCorrector());
    }
    
    public ImageMatrixTouchHandler(Context context, ImageMatrixCorrector corrector) {
		this.corrector = corrector;
		this.savedMatrix = new Matrix();
		this.mode = NONE;
		this.startMid = new PointF();
		this.mid = new PointF();
		this.startSpacing = 1f;
		this.startAngle = 0f;
		this.rotateEnabled = false;
		this.scaleEnabled = true;
		this.translateEnabled = true;
		this.dragOnPinchEnabled = true;
		this.pinchVelocityWindow = 100;
		this.doubleTapZoomDuration = 200;
		this.flingDuration = 200;
		this.zoomReleaseDuration = 200;
		this.zoomReleaseExaggeration = 1.337f;
		this.flingExaggeration = 0.1337f;
		this.doubleTapZoomFactor = 2.5f;
		this.doubleTapZoomOutFactor = 1.4f;
		ImageGestureListener imageGestureListener = new ImageGestureListener();
		this.gestureDetector = new GestureDetector(context, imageGestureListener);
		this.gestureDetector.setOnDoubleTapListener(imageGestureListener);
	}

    
    /*
     * Class methods
     */
    
    /**
     * <p>Returns the mode the handler is currently in.</p> 
     * @return
     */
    public int getMode() {
    	return mode;
    }

	/**
	 * <p>Returns the <code>ImageMatrixCorrector</code> that corrects the image matrix when altered.</p>
	 * @return
	 */
	public ImageMatrixCorrector getImageMatrixCorrector() {
		return corrector;
	}

	/**
	 * <p>Updates the touch state during a touch event. That is, when touch mode is not {@link #NONE} .</p>
	 * <p>Use this when the image in the <code>ImageView</code> or its matrix has been changed.</p>
	 */
	public void updateTouchState() {
		updateTouchState = true;
	}

	/**
	 * <p>Indicates whether rotation is enabled.</p>
	 * @return
     */
	public boolean isRotateEnabled() {
		return rotateEnabled;
	}

	/**
	 ** <p>Sets whether rotation is enabled.</p>
	 * @param rotateEnabled
     */
	public void setRotateEnabled(boolean rotateEnabled) {
		this.rotateEnabled = rotateEnabled;
	}

	/**
	 * <p>Indicates whether scaling is enabled.</p>
	 * @return
     */
	public boolean isScaleEnabled() {
		return scaleEnabled;
	}

	/**
	 * <p>Sets whether scaling is enabled.</p>
	 * @param scaleEnabled
     */
	public void setScaleEnabled(boolean scaleEnabled) {
		this.scaleEnabled = scaleEnabled;
	}

	/**
	 * <p>Indicates whether translation is enabled.</p>
	 * @return
     */
	public boolean isTranslateEnabled() {
		return translateEnabled;
	}

	/**
	 * <p>Sets whether translation is enabled.</p>
	 * @param translateEnabled
     */
	public void setTranslateEnabled(boolean translateEnabled) {
		this.translateEnabled = translateEnabled;
	}

	/**
	 * <p>Indicates whether drag-on-pinch is enabled.</p>
	 * @return
     */
	public boolean isDragOnPinchEnabled() {
		return dragOnPinchEnabled;
	}

	/**
	 * <p>Sets whether drag-on-pinch is enabled.</p>
	 * @param dragOnPinchEnabled
     */
	public void setDragOnPinchEnabled(boolean dragOnPinchEnabled) {
		this.dragOnPinchEnabled = dragOnPinchEnabled;
	}

	/**
	 * <p>Sets the pinch velocity window in milliseconds for determining the pinch velocity.</p>
	 * <p><b>Note:</b> Only touch events in this temporal window are used to calculate pinch velocity.</p>
	 * @param pinchVelocityWindow
     */
	public void setPinchVelocityWindow(long pinchVelocityWindow) {
		this.pinchVelocityWindow = pinchVelocityWindow;
	}

	/**
	 * <p>Sets the double tap zoom animation duration. Setting the duration to <code>0</code> disables the animation altogether.</p>
	 * @param doubleTapZoomDuration
     */
	public void setDoubleTapZoomDuration(long doubleTapZoomDuration) {
		this.doubleTapZoomDuration = doubleTapZoomDuration;
	}

	/**
	 * <p>Sets the fling animation duration. Setting the duration to <code>0</code> disables the animation altogether.</p>
	 * @param flingDuration
	 */
	public void setFlingDuration(long flingDuration) {
		this.flingDuration = flingDuration;
	}

	/**
	 * <p>Sets the zoom release animation duration. Setting the duration to <code>0</code> disables the animation altogether.</p>
	 * @param zoomReleaseDuration
     */
	public void setZoomReleaseDuration(long zoomReleaseDuration) {
		this.zoomReleaseDuration = zoomReleaseDuration;
	}

	/**
	 * <p>Sets the double tap zoom factor.</p>
	 * @param doubleTapZoomFactor
	 */
	public void setDoubleTapZoomFactor(float doubleTapZoomFactor) {
		this.doubleTapZoomFactor = doubleTapZoomFactor;
	}

	/**
	 * <p>Sets the minimum scale factor when double tapping zooms back out instead of in.</p>
	 * @param doubleTapZoomOutFactor
	 */
	public void setDoubleTapZoomOutFactor(float doubleTapZoomOutFactor) {
		this.doubleTapZoomOutFactor = doubleTapZoomOutFactor;
	}

	/**
	 * <p>Sets the fling animation exaggeration factor.</p>
	 * @param flingExaggeration
	 */
	public void setFlingExaggeration(float flingExaggeration) {
		this.flingExaggeration = flingExaggeration;
	}

	/**
	 * <p>Sets the zoom release animation exaggeration factor.</p>
	 * @param zoomReleaseExaggeration
	 */
	public void setZoomReleaseExaggeration(float zoomReleaseExaggeration) {
		this.zoomReleaseExaggeration = zoomReleaseExaggeration;
	}

	/**
	 * <p>Indicates whether the image is being animated.</p>
	 * @return
     */
	public boolean isAnimating() {
		return valueAnimator != null && valueAnimator.isRunning();
	}

	/**
	 * <p>Cancels any running animations.</p>
	 */
	public void cancelAnimation() {
		if(isAnimating()) {
			valueAnimator.cancel();
		}
	}

	/**
	 * <p>Evaluates the touch state.</p>
	 * @param event
	 * @param matrix
	 */
	private void evaluateTouchState(MotionEvent event, Matrix matrix) {

		// Save the starting points
		updateStartPoints(event);
		savedMatrix.set(matrix);

		// Update the mode
		int touchCount = getTouchCount();
		if(touchCount == 0) {
			mode = NONE;
		} else {
			if(isAnimating()) {
				valueAnimator.cancel();
			}
			if(touchCount == 1) {
				if(mode == PINCH) {
					if(zoomReleaseDuration > 0 && !isAnimating()) {
						// Animate zoom release
						float scale = (float) Math.pow(Math.pow(Math.pow(pinchVelocity, 1d / 1000d), zoomReleaseDuration), zoomReleaseExaggeration);
						animateZoom(scale, zoomReleaseDuration, mid.x, mid.y, new DecelerateInterpolator());
					}
				}
				mode = DRAG;
			} else if (touchCount > 1) {
				mode = PINCH;

				// Calculate the start distance
				startSpacing = spacing(event, getId(0), getId(1));
				pinchVelocity = 0f;

				if(startSpacing > MIN_PINCH_DIST_PIXELS) {
					midPoint(startMid, event, getId(0), getId(1));
					startAngle = angle(event, getId(0), getId(1), startedLower(getStartPoint(0), getStartPoint(1)));
				}
			}
		}
	}

	/*
	 * Interface implementations
	 */
	
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		super.onTouch(view, event);
		gestureDetector.onTouchEvent(event);
		ImageView imageView;
		try {
			imageView = (ImageView) view;
		} catch(ClassCastException e) {
			throw new IllegalStateException("View must be an instance of ImageView", e);
		}
		// Get the matrix
		Matrix matrix = imageView.getImageMatrix();
		// Sets the image view
		if(corrector.getImageView() != imageView) {
			corrector.setImageView(imageView);
		} else if(imageView.getScaleType() != ScaleType.MATRIX) {
			imageView.setScaleType(ScaleType.MATRIX);
			corrector.setMatrix(matrix);
		}
		int actionMasked = event.getActionMasked();
		switch (actionMasked) {
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			evaluateTouchState(event, matrix);
			break;
		case MotionEvent.ACTION_MOVE:
			if(updateTouchState) {
				evaluateTouchState(event, matrix);
				updateTouchState = false;
			}
			// Reuse the saved matrix
			matrix.set(savedMatrix);
			if (mode == DRAG) {
				if(translateEnabled) {
					// Get the start point
					PointF start = getStartPoint(0);
					int index = event.findPointerIndex(getId(0));
					float dx = event.getX(index) - start.x;
					dx = corrector.correctRelative(Matrix.MTRANS_X, dx);
					float dy = event.getY(index) - start.y;
					dy = corrector.correctRelative(Matrix.MTRANS_Y, dy);
					matrix.postTranslate(dx, dy);
				}
			} else if (mode == PINCH) {
				// Get the new midpoint
				midPoint(mid, event, getId(0), getId(1));
				// Rotate
				if(rotateEnabled) {
					float deg = startAngle - angle(event, getId(0), getId(1), startedLower(getStartPoint(0), getStartPoint(1)));
					matrix.postRotate(deg, mid.x, mid.y);
				}
				if(scaleEnabled) {
					// Scale
					float spacing = spacing(event, getId(0), getId(1));
					float sx = spacing / startSpacing;
					sx = corrector.correctRelative(Matrix.MSCALE_X, sx);
					matrix.postScale(sx, sx, mid.x, mid.y);
					if(event.getHistorySize() > 0) {
						pinchVelocity = pinchVelocity(event, getId(0), getId(1), pinchVelocityWindow);
					}
				}
				if(dragOnPinchEnabled && translateEnabled) {
					// Translate
					float dx = mid.x - startMid.x;
					float dy = mid.y - startMid.y;
					matrix.postTranslate(dx, dy);
				}
				corrector.performAbsoluteCorrections();
			}
			imageView.invalidate();
			break;
		}
		return true; // indicate event was handled
	}

	/**
	 *
	 */
	private class ImageGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (mode == DRAG) {
				if (flingDuration > 0 && !isAnimating()) {
					float factor = ((float) flingDuration / 1000f) * flingExaggeration;
					float[] values = corrector.getValues();
					float dx = (velocityX * factor) * values[Matrix.MSCALE_X];
					float dy = (velocityY * factor) * values[Matrix.MSCALE_Y];
					PropertyValuesHolder flingX = PropertyValuesHolder.ofFloat(FlingAnimatorHandler.PROPERTY_TRANSLATE_X, values[Matrix.MTRANS_X], values[Matrix.MTRANS_X] + dx);
					PropertyValuesHolder flingY = PropertyValuesHolder.ofFloat(FlingAnimatorHandler.PROPERTY_TRANSLATE_Y, values[Matrix.MTRANS_Y], values[Matrix.MTRANS_Y] + dy);
					valueAnimator = ValueAnimator.ofPropertyValuesHolder(flingX, flingY);
					valueAnimator.setDuration(flingDuration);
					valueAnimator.addUpdateListener(new FlingAnimatorHandler(corrector));
					valueAnimator.setInterpolator(new DecelerateInterpolator());
					valueAnimator.start();
					return true;
				}
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			if (doubleTapZoomFactor > 0 && !isAnimating()) {
				float sx = corrector.getValues()[Matrix.MSCALE_X];
				float innerFitScale = corrector.getInnerFitScale();
				float reversalScale = innerFitScale * doubleTapZoomOutFactor;
				ScaleAnimatorHandler scaleAnimatorHandler = new ScaleAnimatorHandler(corrector, e.getX(), e.getY());
				float scaleTo = sx > reversalScale ? innerFitScale : sx * doubleTapZoomFactor;
				animateZoom(sx, scaleTo, doubleTapZoomDuration, scaleAnimatorHandler, null);
				return true;
			}
			return super.onDoubleTap(e);
		}
	}

	/**
	 * <p>Performs a zoom animation using the given <code>zoomFactor</code>.</p>
	 * @param zoomFactor
	 * @param duration
	 */
	public void animateZoom(float zoomFactor, long duration) {
		float sx = corrector.getValues()[Matrix.MSCALE_X];
		animateZoom(sx, sx * zoomFactor, duration, new ScaleAnimatorHandler(corrector), null);
	}

	/**
	 * <p>Performs a zoom animation using the given <code>zoomFactor</code> and centerpoint coordinates.</p>
	 * @param zoomFactor
	 * @param duration
	 * @param x
	 * @param y
	 */
	public void animateZoom(float zoomFactor, long duration, float x, float y) {
		animateZoom(zoomFactor, duration, x, y, null);
	}

	/**
	 * <p>Performs a zoom animation using the given <code>zoomFactor</code> and centerpoint coordinates.</p>
	 * @param zoomFactor
	 * @param duration
	 * @param x
     * @param y
	 * @param interpolator
     */
	public void animateZoom(float zoomFactor, long duration, float x, float y, Interpolator interpolator) {
		float sx = corrector.getValues()[Matrix.MSCALE_X];
		animateZoom(sx, sx * zoomFactor, duration, new ScaleAnimatorHandler(corrector, x, y), interpolator);
	}

	/**
	 * <p>Performs a zoom out animation so that the image entirely fits within the view.</p>
	 * @param duration
	 */
	public void animateZoomOutToFit(long duration) {
		float sx = corrector.getValues()[Matrix.MSCALE_X];
		animateZoom(sx, corrector.getInnerFitScale(), duration, new ScaleAnimatorHandler(corrector), null);
	}

	/**
	 * <p>Performs a zoom out animation so that the image entirely fits within the view using centerpoint coordinates.</p>
	 * @param duration
	 * @param x
	 * @param y
     */
	public void animateZoomOutToFit(long duration, float x, float y) {
		float sx = corrector.getValues()[Matrix.MSCALE_X];
		animateZoom(sx, corrector.getInnerFitScale(), duration, new ScaleAnimatorHandler(corrector, x, y), null);
	}

	/**
	 * <p>Performs a zoom animation from <code>scaleFrom</code> to <code>scaleTo</code> using the given <code>ScaleAnimatorHandler</code>.</p>
	 * @param scaleFrom
	 * @param scaleTo
	 * @param duration
	 * @param scaleAnimatorHandler
	 * @param interpolator
     */
	private void animateZoom(float scaleFrom, float scaleTo, long duration, ScaleAnimatorHandler scaleAnimatorHandler, Interpolator interpolator) {
		if(isAnimating()) {
			throw new IllegalStateException("An animation is currently running; Check isAnimating() first!");
		}
		valueAnimator = ValueAnimator.ofFloat(scaleFrom, scaleTo);
		valueAnimator.setDuration(duration);
		valueAnimator.addUpdateListener(scaleAnimatorHandler);
		if(interpolator != null) valueAnimator.setInterpolator(interpolator);
		valueAnimator.start();
	}
}
