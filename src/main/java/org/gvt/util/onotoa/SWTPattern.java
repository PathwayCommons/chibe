package org.gvt.util.onotoa;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Pattern;

/**
 * https://code.google.com/a/eclipselabs.org/p/onotoa/source/browse/de.topicmapslab.tmcledit.diagram/src/de/topicmapslab/tmcledit/diagram/util
 */
public class SWTPattern extends Pattern {

	private float x1;
	private float y1;
	private float x2;
	private float y2;

	private float alpha1;
	private float alpha2;

	private Color color1;
	private Color color2;

	/**
	 *
	 * @param device
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param color1
	 * @param color2
	 */
	public SWTPattern(Device device, float x1, float y1, float x2, float y2,
		Color color1, Color color2) {
		this(device, x1, y1, x2, y2, color1, 0xFF, color2, 0xFF);
	}

	/**
	 *
	 * @param device
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param color1
	 * @param alpha1
	 * @param color2
	 * @param alpha2
	 */
	public SWTPattern(Device device, float x1, float y1, float x2, float y2,
		Color color1, int alpha1, Color color2, int alpha2) {
		super(device, x1, y1, x2, y2, color1, alpha1, color2, alpha2);
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.alpha1 = alpha1;
		this.alpha2 = alpha2;
		this.color1 = color1;
		this.color2 = color2;
	}

	/**
	 * @return the x1
	 */
	public float getX1() {
		return x1;
	}

	/**
	 * @param x1 the x1 to set
	 */
	public void setX1(float x1) {
		this.x1 = x1;
	}

	/**
	 * @return the y1
	 */
	public float getY1() {
		return y1;
	}

	/**
	 * @param y1 the y1 to set
	 */
	public void setY1(float y1) {
		this.y1 = y1;
	}

	/**
	 * @return the x2
	 */
	public float getX2() {
		return x2;
	}

	/**
	 * @param x2 the x2 to set
	 */
	public void setX2(float x2) {
		this.x2 = x2;
	}

	/**
	 * @return the y2
	 */
	public float getY2() {
		return y2;
	}

	/**
	 * @param y2 the y2 to set
	 */
	public void setY2(float y2) {
		this.y2 = y2;
	}

	/**
	 * @return the alpha1
	 */
	public float getAlpha1() {
		return alpha1;
	}

	/**
	 * @param alpha1 the alpha1 to set
	 */
	public void setAlpha1(float alpha1) {
		this.alpha1 = alpha1;
	}

	/**
	 * @return the alpha2
	 */
	public float getAlpha2() {
		return alpha2;
	}

	/**
	 * @param alpha2 the alpha2 to set
	 */
	public void setAlpha2(float alpha2) {
		this.alpha2 = alpha2;
	}

	/**
	 * @return the color1
	 */
	public Color getColor1() {
		return color1;
	}

	/**
	 * @param color1 the color1 to set
	 */
	public void setColor1(Color color1) {
		this.color1 = color1;
	}

	/**
	 * @return the color2
	 */
	public Color getColor2() {
		return color2;
	}

	/**
	 * @param color2 the color2 to set
	 */
	public void setColor2(Color color2) {
		this.color2 = color2;
	}
}
