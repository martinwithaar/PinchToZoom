package com.bogdwellers.pinchtozoom;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * <p>This class enables easy interpretation of multitouch gestures such as pinching, rotating etc.</p>
 * 
 * TODO Implement convex hull algorithm (static method)
 * TODO Implement touch grouping by evaluating touch proximity
 * TODO Implement get touch numbers ordered by x- or y-axis alignment
 * @author Martin
 *
 */
public class MultiTouchListener implements OnTouchListener {
	
	private static final String TAG = MultiTouchListener.class.getSimpleName();
	
	/*
	 * Attributes
	 */
	
	private List<Integer> pointerIds;
	private SparseArray<PointF> startPoints;
	
	/*
	 * Constructor(s)
	 */
	
	public MultiTouchListener() {
		this.pointerIds = new ArrayList<>(40); // 4 persons with both hands compatible :)
		this.startPoints = new SparseArray<>();
	}
	
	/*
	 * Interface implementations
	 */

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		
		// Separate action and pointer index
		int actionMasked = event.getActionMasked();
		int actionIndex = event.getActionIndex();
		Integer pointerId;
		
		// Handle touch event
		switch (actionMasked) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			pointerId = event.getPointerId(actionIndex);
			PointF startPoint = new PointF(event.getX(actionIndex), event.getY(actionIndex));
			
			// Save the starting point
			startPoints.put(pointerId, startPoint);
			pointerIds.add(pointerId);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			pointerId = event.getPointerId(actionIndex);
			pointerIds.remove(pointerId);
			startPoints.remove(pointerId);
			break;
		case MotionEvent.ACTION_CANCEL:
			clearPointerIds();
			startPoints.clear();
			break;
		}
		return false;
	}
	
	/*
	 * Class methods
	 */
	
	/**
	 * <p>Clears all registered pointer ids.</p>
	 */
	private void clearPointerIds() {
		pointerIds.clear();
	}
	
	/**
	 * <p>Returns the current amount of touch points.</p>
	 * @return
	 */
	public int getTouchCount() {
		return pointerIds.size();
	}
	
	/**
	 * <p>Indicates if one or more touches are currently in progress.</p>
	 * @return
	 */
	public boolean isTouching() {
		return !pointerIds.isEmpty();
	}
	
	/**
	 * <p>Returns the pointer id for the given index of subsequent touch points.</p>
	 * @param touchNo
	 * @return
	 */
	public int getId(int touchNo) {
		return pointerIds.get(touchNo);
	}
	
	/**
	 * <p>Returns the start point for the given touch number (where the user initially pressed down).</p>
	 * @param touchNo
	 * @return
	 */
	public PointF getStartPoint(int touchNo) {
		return startPoints.get(getId(touchNo));
	}
	
	/**
	 * <p>Updates the start points with the current coordinate configuration.</p>
	 * @param event
	 */
	public void updateStartPoints(MotionEvent event) {
		PointF startPoint;
		Integer pointerId;
		
		for(int i = 0, n = event.getPointerCount(); i < n; i++) {
			pointerId = event.getPointerId(i);
			startPoint = new PointF(event.getX(i), event.getY(i));
			
			// Save the starting point
			startPoints.put(pointerId, startPoint);
		}
	}
	
	/**
	 * <p>Returns an array containing all pointer ids.</p>
	 * @param ids
	 * @return
	 */
	public Integer[] getIdArray(Integer[] ids) {
		return pointerIds.toArray(ids);
	}
	
	/*
	 * Static methods
	 */
	
	/**
	 * 
	 * @param point
	 * @param event
	 * @param id
	 */
	public static final void point(Point point, MotionEvent event, int id) {
		int index = event.findPointerIndex(id);
		point.x = Math.round(event.getX(index));
		point.y = Math.round(event.getY(index));
	}
	
	/**
	 * <p>Calculates the space between two pointers.</p>
	 * @param event
	 * @param pointerA id of pointer A
	 * @param pointerB id of pointer B
	 * @return spacing between both pointers
	 */
	public static final float spacing(MotionEvent event, int pointerA, int pointerB) {
		int indexA = event.findPointerIndex(pointerA);
		int indexB = event.findPointerIndex(pointerB);
		return spacingByIndex(event, indexA, indexB);
	}

	/**
	 *
	 * @param event
	 * @param indexA
	 * @param indexB
     * @return
     */
	private static final float spacingByIndex(MotionEvent event, int indexA, int indexB) {
		float x = event.getX(indexA) - event.getX(indexB);
		float y = event.getY(indexA) - event.getY(indexB);
		return (float) Math.sqrt(x * x + y * y); // Pythagoras
	}

	/**
	 * <p>Calculates the pinch velocity for the last <code>timeWindow</code> milliseconds.</p>
	 * @param event
	 * @param pointerA id of pointer A
	 * @param pointerB id of pointer B
	 * @param timeWindow
	 * @return spacing between both pointers
	 */
	public static final float pinchVelocity(MotionEvent event, int pointerA, int pointerB, long timeWindow) {
		int indexA = event.findPointerIndex(pointerA);
		int indexB = event.findPointerIndex(pointerB);
		long eventTime = event.getEventTime();
		long timeDelta = 0;
		float previousSpacing = spacingByIndex(event, indexA, indexB);
		float scale = 1;
		for(int i = 0, n = event.getHistorySize(); i < n && timeDelta < timeWindow; i++) {
			int index = (n - 1) - i;
			float x = event.getHistoricalX(indexA, index) - event.getHistoricalX(indexB, index);
			float y = event.getHistoricalY(indexA, index) - event.getHistoricalY(indexB, index);
			float spacing = (float) Math.sqrt(x * x + y * y);
			scale *= previousSpacing / spacing;
			previousSpacing = spacing;
			timeDelta = eventTime - event.getHistoricalEventTime(index);
		}
		return (float) Math.pow(Math.pow(scale, 1d / timeWindow), 1000d);
	}

	/**
	 * <p>Calculates the mid point between two pointers.</p>
	 * @param point
	 * @param event
	 * @param pointerA id of pointer A
	 * @param pointerB id of pointer B
	 */
	public static final void midPoint(Point point, MotionEvent event, int pointerA, int pointerB) {
		int indexA = event.findPointerIndex(pointerA);
		int indexB = event.findPointerIndex(pointerB);
		
		float x = event.getX(indexA) + event.getX(indexB);
		float y = event.getY(indexA) + event.getY(indexB);
		point.set(Math.round(x / 2f), Math.round(y / 2f));
	}

	/**
	 * <p>Calculates the mid point between two pointers.</p>
	 * @param point
	 * @param event
	 * @param pointerA id of pointer A
	 * @param pointerB id of pointer B
	 */
	public static final void midPoint(PointF point, MotionEvent event, int pointerA, int pointerB) {
		int indexA = event.findPointerIndex(pointerA);
		int indexB = event.findPointerIndex(pointerB);
		
		float x = event.getX(indexA) + event.getX(indexB);
		float y = event.getY(indexA) + event.getY(indexB);
		point.set(x / 2f, y / 2f);
	}
	
	/**
	 * <p>Calculates the angle between two points.</p>
	 * @param event
	 * @param pointerA id of pointer A
	 * @param pointerB id of pointer B
	 * @param isPointerAPivot indicates if pointer A is considered to be the pivot, else pointer B is. Use {@link #startedLower(PointF, PointF)}
	 * @return angle in degrees
	 */
	public static final float angle(MotionEvent event, int pointerA, int pointerB, boolean isPointerAPivot) {
		// Resolve the indices
		int indexA = event.findPointerIndex(pointerA);
		int indexB = event.findPointerIndex(pointerB);
		
		// Get the x-y displacement
		float x = event.getX(indexA) - event.getX(indexB);
		float y = event.getY(indexA) - event.getY(indexB);
		
		// Calculate the arc tangent
		double atan = Math.atan(x / y);
		
		// Always consider the same pointer the pivot
		if((y < 0f && isPointerAPivot) || (y > 0f && !isPointerAPivot)) {
			atan += Math.PI;
		}
		
		// Convert to float in degrees
		double deg = Math.toDegrees(atan);
		return (float) deg;
	}
	
	/**
	 * <p>Convenience method to determine whether starting point A has a lower y-axis value than starting point B.
	 * Useful in conjunction with {@link #angle(MotionEvent, int, int, boolean)}.</p>
	 * @param pointA
	 * @param pointB
	 * @return
	 */
	public static final boolean startedLower(PointF pointA, PointF pointB) {
		return pointA.y < pointB.y;
	}
}
