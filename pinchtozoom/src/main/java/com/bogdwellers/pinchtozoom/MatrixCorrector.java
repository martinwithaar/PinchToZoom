package com.bogdwellers.pinchtozoom;

import android.graphics.Matrix;

/**
 * <p>The <code>MatrixCorrector</code> enforces boundaries in the transformation of a <code>Matrix</code>.</p>
 * 
 * @author Martin
 *
 */
public abstract class MatrixCorrector {
	
	/*
	 * Attributes
	 */

	private Matrix matrix;
	private float[] values;

	/*
	 * Constructor(s)
	 */
	
	public MatrixCorrector() {
		this(null);
	}
	
	public MatrixCorrector(Matrix matrix) {
		this.matrix = matrix;
		this.values = new float[9];
	}
	
	/*
	 * Class methods
	 */

	/**
	 * <p>Does corrections AFTER matrix operations have been applied.</p>
	 * <p>This implementation only copies the values of the matrix into its float array <code>values</code>.</p>
	 */
	public void performAbsoluteCorrections() {

	}

	/**
	 * <p>Returns the corrected value of the given relative vector.</p>
	 * @param vector
	 * @param x
     * @return
     */
	public float correctRelative(int vector, float x) {
		float v = getValues()[vector];
		switch(vector) {
			case Matrix.MTRANS_X:
			case Matrix.MTRANS_Y:
				return correctAbsolute(vector, v + x) - v;
			case Matrix.MSCALE_X:
			case Matrix.MSCALE_Y:
				return correctAbsolute(vector, v * x) / v;
			default:
				throw new IllegalArgumentException("Vector not supported");
		}
	}

	/**
	 * * <p>Returns the corrected value of the given absolute vector.</p>
	 * @param vector
	 * @param x
     * @return
     */
	public float correctAbsolute(int vector, float x) {
		return x;
	}

	/**
	 * * <p>Returns the matrix.</p>
	 * @return
	 */
	public Matrix getMatrix() {
		return matrix;
	}

	/**
	 * * * <p>Sets the matrix.</p>
	 * @param matrix
	 */
	public void setMatrix(Matrix matrix) {
		this.matrix = matrix;
	}

	/**
	 * * * <p>Returns the matrix values.</p>
	 * @return
	 */
	protected float[] getValues() {
		matrix.getValues(values);
		return values;
	}
}
