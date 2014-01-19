package org.gvt.util.onotoa;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Stack;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.PathData;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.widgets.Display;

/**
 * https://code.google.com/a/eclipselabs.org/p/onotoa/source/browse/de.topicmapslab.tmcledit.diagram/src/de/topicmapslab/tmcledit/diagram/util/GraphicsToGraphics2DAdaptor.java
 *
 * Modified the code.
 */
public class GraphicsToGraphics2DAdaptor extends Graphics  {

	private static class State {

		/**
		 * translateX
		 */
		public int translateX = 0;

		/**
		 * translateY
		 */
		public int translateY = 0;

		/**
		 * clipping rectangle x coordinate
		 */
		public int clipX = 0;
		/**
		 * clipping rectangle y coordinate
		 */
		public int clipY = 0;
		/**
		 * clipping rectangle width
		 */
		public int clipW = 0;
		/**
		 * clipping rectangle height
		 */
		public int clipH = 0;

		/** Font value **/
		/**
		 * cached font
		 */
		public Font font;

		/**
		 * cache line dash array
		 */
		public float[] lineDash = { 3, 3 };

		/**
		 * cached line style
		 */
		public int lineStyle = LINE_SOLID;
		/**
		 * cached line width
		 */
		public float lineWidth = 1;
		/**
		 * cached xor mode value
		 */
		public boolean XorMode = false;
		/**
		 * cached foreground color
		 */
		public Color fgColor;
		/**
		 * cached background color
		 */
		public Color bgColor;

		/**
		 * cached alpha value
		 */
		public int alpha;

		public Pattern bgPattern;

		int graphicHints;

		/**
		 * Copy the values from a given state to this state
		 *
		 * @param state
		 *            the state to copy from
		 */
		public void copyFrom(State state) {

			translateX = state.translateX;
			translateY = state.translateY;

			clipX = state.clipX;
			clipY = state.clipY;
			clipW = state.clipW;
			clipH = state.clipH;

			font = state.font;
			lineStyle = state.lineStyle;
			lineWidth = state.lineWidth;
			fgColor = state.fgColor;
			bgColor = state.bgColor;
			XorMode = state.XorMode;
			alpha = state.alpha;
			graphicHints = state.graphicHints;
			bgPattern = state.bgPattern;
		}
	}

	static final int FILL_RULE_MASK;
	static final int FILL_RULE_SHIFT;
	static final int FILL_RULE_WHOLE_NUMBER = -1;

	/*
	 * It's consistent with SWTGraphics flags in case some other flags from SWTGraphics need to be here
	 */
	static {
		FILL_RULE_SHIFT = 14;
		FILL_RULE_MASK = 1 << FILL_RULE_SHIFT; //If changed to more than 1-bit, check references!
	}

	private SWTGraphics swtGraphics;
	private Graphics2D graphics2D;
	private BasicStroke stroke;
	private Stack<State> states = new Stack<State>();
	private final State currentState = new State();
	private final State appliedState = new State();

	private Rectangle relativeClipRegion;
	private Rectangle2D relativeClipRegion2D;

	private org.eclipse.swt.graphics.Rectangle viewBox;
	private Image image;

	/**
	 * x coordinate for graphics translation
	 */
	private int transX = 0;
	/**
	 * y coordinate for graphics translation
	 */
	private int transY = 0;

	/**
	 * Constructor
	 *
	 * @param graphics the <code>Graphics2D</code> object that this object is delegating
	 * calls to.
	 * @param viewPort the <code>Rectangle</code> that defines the logical area being rendered
	 * by the graphics object.
	 */
	public GraphicsToGraphics2DAdaptor( Graphics2D graphics, Rectangle viewPort ) {

		this( graphics, new org.eclipse.swt.graphics.Rectangle( viewPort.x,
			viewPort.y,
			viewPort.width,
			viewPort.height) );
	}

	/**
	 * Alternate Constructor that takes an swt Rectangle
	 *
	 * @param graphics the <code>Graphics2D</code> object that this object is delegating
	 * calls to.
	 * @param viewPort the <code>org.eclipse.swt.graphics.Rectangle</code> that defines the logical area
	 * being rendered by the graphics object.
	 */
	public GraphicsToGraphics2DAdaptor(Graphics2D graphics, org.eclipse.swt.graphics.Rectangle viewPort) {

		// Save the ViewPort to add to the root DOM element
		viewBox = viewPort;

		// Create the SWT Graphics Object
		createSWTGraphics();

		// Initialize the SVG Graphics Object
		initSVGGraphics(graphics);

		// Initialize the States
		init();
	}

	/**
	 * This is a helper method used to create the SWT Graphics object
	 */
	private void createSWTGraphics() {

		//we need this temp Rect just to instantiate an swt image in order to keep
		//state, the size of this Rect is of no consequence and we just set it to
		//such a small size in order to minimize memory allocation
		org.eclipse.swt.graphics.Rectangle tempRect = new org.eclipse.swt.graphics.Rectangle(0,
			0,
			10,
			10);
		image = new Image(Display.getCurrent(), tempRect);
		GC gc = new GC(image);
		swtGraphics = new SWTGraphics(gc);
	}

	/**
	 * Create the SVG graphics object and initializes it with the current line
	 * stlye and width
	 */
	private void initSVGGraphics(Graphics2D graphics) {
		this.graphics2D = graphics;

		relativeClipRegion =
			new Rectangle(viewBox.x, viewBox.y, viewBox.width, viewBox.height);

		relativeClipRegion2D =
			new java.awt.Rectangle(viewBox.x, viewBox.y, viewBox.width, viewBox.height);

		// Initialize the line style and width
		stroke =
			new BasicStroke(
				swtGraphics.getLineWidth(),
				BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_ROUND,
				0,
				null,
				0);

		setLineStyle(swtGraphics.getLineStyle());
		setLineWidth(swtGraphics.getLineWidth());
		getGraphics2D().setStroke(stroke);
	}

	/**
	 * This method should only be called by the constructor. Initializes state
	 * information for the currentState
	 */
	private void init() {

		// Initialize drawing styles
		setLineStyle(getLineStyle());
		setLineWidth(1);
		setForegroundColor(getForegroundColor());
		setBackgroundColor(getBackgroundColor());
		setXORMode(getXORMode());

		// Initialize Font
		setFont(getFont());
		currentState.font = appliedState.font = getFont();

		// Initialize translations
		currentState.translateX = appliedState.translateX = transX;
		currentState.translateY = appliedState.translateY = transY;

		// Initialize Clip Regions
		currentState.clipX = appliedState.clipX = relativeClipRegion.x;
		currentState.clipY = appliedState.clipY = relativeClipRegion.y;
		currentState.clipW = appliedState.clipW = relativeClipRegion.width;
		currentState.clipH = appliedState.clipH = relativeClipRegion.height;

		currentState.alpha = appliedState.alpha = getAlpha();

	}

	@Override
	public void setBackgroundPattern(Pattern pattern) {
		currentState.bgPattern = pattern;
		swtGraphics.setBackgroundPattern(pattern);
	}

	/**
	 * Verifies that the applied state is up to date with the current state and updates
	 * the applied state accordingly.
	 */
	protected void checkState() {

		if( appliedState.font != currentState.font ) {
			appliedState.font = currentState.font;

			setFont(currentState.font);
		}

		if (appliedState.clipX != currentState.clipX
			|| appliedState.clipY != currentState.clipY
			|| appliedState.clipW != currentState.clipW
			|| appliedState.clipH != currentState.clipH) {

			appliedState.clipX = currentState.clipX;
			appliedState.clipY = currentState.clipY;
			appliedState.clipW = currentState.clipW;
			appliedState.clipH = currentState.clipH;

			// Adjust the clip for SVG
			getGraphics2D().setClip(
				currentState.clipX - 1,
				currentState.clipY - 1,
				currentState.clipW + 2,
				currentState.clipH + 2);
		}

		if( appliedState.alpha != currentState.alpha ) {
			appliedState.alpha = currentState.alpha;

			setAlpha(currentState.alpha);
		}

		appliedState.graphicHints = currentState.graphicHints;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#clipRect(org.eclipse.draw2d.geometry.Rectangle)
	 */
	public void clipRect(Rectangle rect) {
		relativeClipRegion.intersect(rect);
		setClipAbsolute(
			relativeClipRegion.x + transX,
			relativeClipRegion.y + transY,
			relativeClipRegion.width,
			relativeClipRegion.height);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#dispose()
	 */
	public void dispose() {
		swtGraphics.dispose();

		if (image != null) {
			image.dispose();
		}

		states.clear();
	}

	/**
	 * This method is used to convert an SWT Color to an AWT Color.
	 *
	 * @param toConvert
	 *            SWT Color to convert
	 * @return AWT Color
	 */
	protected java.awt.Color getColor(Color toConvert) {

		return new java.awt.Color(
			toConvert.getRed(),
			toConvert.getGreen(),
			toConvert.getBlue());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#drawArc(int, int, int, int, int, int)
	 */
	public void drawArc(
		int x,
		int y,
		int width,
		int height,
		int startAngle,
		int endAngle) {

		Arc2D arc =
			new Arc2D.Float(
				x + transX,
				y + transY,
				width - 1,
				height,
				startAngle,
				endAngle,
				Arc2D.OPEN);

		checkState();
		getGraphics2D().setPaint(getColor(swtGraphics.getForegroundColor()));
		getGraphics2D().draw(arc);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#fillArc(int, int, int, int, int, int)
	 */
	public void fillArc(int x, int y, int w, int h, int offset, int length) {

		Arc2D arc =
			new Arc2D.Float(
				x + transX,
				y + transY,
				w,
				h,
				offset,
				length,
				Arc2D.OPEN);

		checkState();
		getGraphics2D().setPaint(getColor(swtGraphics.getBackgroundColor()));
		getGraphics2D().fill(arc);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#drawFocus(int, int, int, int)
	 */
	public void drawFocus(int x, int y, int w, int h) {
		drawRectangle(x, y, w, h);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#drawImage(org.eclipse.swt.graphics.Image,
	 *      int, int)
	 */
	public void drawImage(Image srcImage, int xpos, int ypos) {

		// Translate the Coordinates
		xpos += transX;
		ypos += transY;

		// Convert the SWT Image into an AWT BufferedImage
		BufferedImage toDraw = ImageConverter.convert(srcImage);

		checkState();
		getGraphics2D().drawImage(
			toDraw,
			new AffineTransform(1f, 0f, 0f, 1f, xpos, ypos),
			null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#drawImage(org.eclipse.swt.graphics.Image,
	 *      int, int, int, int, int, int, int, int)
	 */
	public void drawImage(
		Image srcImage,
		int x1,
		int y1,
		int w1,
		int h1,
		int x2,
		int y2,
		int w2,
		int h2) {

		x2 += transX;
		y2 += transY;

		BufferedImage toDraw = ImageConverter.convert(srcImage);
		checkState();
		getGraphics2D().drawImage(toDraw, x2, y2, w2, h2, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#drawLine(int, int, int, int)
	 */
	public void drawLine(int x1, int y1, int x2, int y2) {

		Line2D line =
			new Line2D.Float(
				x1 + transX,
				y1 + transY,
				x2 + transX,
				y2 + transY);

		checkState();
		getGraphics2D().setPaint(getColor(swtGraphics.getForegroundColor()));
		getGraphics2D().draw(line);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#drawOval(int, int, int, int)
	 */
	public void drawOval(int x, int y, int w, int h) {

		Ellipse2D ellipse =
			new Ellipse2D.Float(x + transX, y + transY, w, h);

		checkState();
		getGraphics2D().setPaint(getColor(swtGraphics.getForegroundColor()));
		getGraphics2D().draw(ellipse);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#fillOval(int, int, int, int)
	 */
	public void fillOval(int x, int y, int w, int h) {

		Ellipse2D ellipse =
			new Ellipse2D.Float(x + transX, y + transY, w-1, h-1);

		checkState();
		getGraphics2D().setPaint(getColor(swtGraphics.getBackgroundColor()));
		getGraphics2D().fill(ellipse);
	}

	private Polygon createPolygon(PointList pointList) {

		Polygon toCreate = new Polygon();

		for (int i = 0; i < pointList.size(); i++) {
			Point pt = pointList.getPoint(i);

			toCreate.addPoint(pt.x + transX, pt.y + transY);
		}

		return toCreate;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#drawPolygon(org.eclipse.draw2d.geometry.PointList)
	 */
	public void drawPolygon(PointList pointList) {
		checkState();
		getGraphics2D().setPaint(getColor(swtGraphics.getForegroundColor()));
		getGraphics2D().draw(createPolygon(pointList));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#fillPolygon(org.eclipse.draw2d.geometry.PointList)
	 */
	public void fillPolygon(PointList pointList) {

		checkState();
		if (currentState.bgPattern==null)
			getGraphics2D().setPaint(getColor(swtGraphics.getBackgroundColor()));
		else {
			if (currentState.bgPattern instanceof SWTPattern) {
				SWTPattern pattern = (SWTPattern) currentState.bgPattern;

				java.awt.Color c1 = getColor(pattern.getColor1());
				java.awt.Color c2 = getColor(pattern.getColor2());

				GradientPaint paint = new GradientPaint(pattern.getX1(), pattern.getY1(), c1,
					pattern.getX2(), pattern.getY2(), c2);

				getGraphics2D().setPaint(paint);
			}
		}
		getGraphics2D().fill(createPolygon(pointList));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#drawPolyline(org.eclipse.draw2d.geometry.PointList)
	 */
	public void drawPolyline(PointList pointList) {

		// Draw polylines as a series of lines
		for (int x = 1; x < pointList.size(); x++) {

			Point p1 = pointList.getPoint(x - 1);
			Point p2 = pointList.getPoint(x);

			drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#drawRectangle(int, int, int, int)
	 */
	public void drawRectangle(int x, int y, int w, int h) {

		Rectangle2D rect =
			new Rectangle2D.Float(x + transX, y + transY, w, h);

		checkState();
		getGraphics2D().setPaint(getColor(swtGraphics.getForegroundColor()));
		getGraphics2D().draw(rect);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#fillRectangle(int, int, int, int)
	 */
	public void fillRectangle(int x, int y, int width, int height) {

		Rectangle2D rect =
			new Rectangle2D.Float(
				x + transX,
				y + transY,
				width,
				height);

		checkState();
		if (currentState.bgPattern==null)
			getGraphics2D().setPaint(getColor(swtGraphics.getBackgroundColor()));
		else {
			if (currentState.bgPattern instanceof SWTPattern) {
				SWTPattern pattern = (SWTPattern) currentState.bgPattern;

				java.awt.Color c1 = getColor(pattern.getColor1());
				java.awt.Color c2 = getColor(pattern.getColor2());

				GradientPaint paint = new GradientPaint(pattern.getX1(), pattern.getY1(), c1,
					pattern.getX2(), pattern.getY2(), c2);

				getGraphics2D().setPaint(paint);
			}
		}
		getGraphics2D().fill(relativeClipRegion2D.createIntersection(rect));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#drawRoundRectangle(org.eclipse.draw2d.geometry.Rectangle,
	 *      int, int)
	 */
	public void drawRoundRectangle(
		Rectangle rect,
		int arcWidth,
		int arcHeight) {

		RoundRectangle2D roundRect =
			new RoundRectangle2D.Float(
				rect.x + transX,
				rect.y + transY,
				rect.width,
				rect.height,
				arcWidth,
				arcHeight);

		checkState();
		getGraphics2D().setPaint(getColor(swtGraphics.getForegroundColor()));
		getGraphics2D().draw(roundRect);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#fillRoundRectangle(org.eclipse.draw2d.geometry.Rectangle,
	 *      int, int)
	 */
	public void fillRoundRectangle(
		Rectangle rect,
		int arcWidth,
		int arcHeight) {

		RoundRectangle2D roundRect =
			new RoundRectangle2D.Float(
				rect.x + transX,
				rect.y + transY,
				rect.width,
				rect.height,
				arcWidth,
				arcHeight);

		checkState();
		getGraphics2D().setPaint(getColor(swtGraphics.getBackgroundColor()));
		getGraphics2D().fill(roundRect);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#drawText(java.lang.String, int, int)
	 */
	public void drawText(String s, int x, int y) {
		drawString(s, x, y);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#drawString(java.lang.String, int, int)
	 */
	public void drawString(String s, int x, int y) {

		if( s == null )
			return;

		java.awt.FontMetrics metrics = getGraphics2D().getFontMetrics();
		int stringLength = metrics.stringWidth(s);

		float xpos = x + transX;
		float ypos = y + transY;

		ypos += metrics.getAscent();

		checkState();
		getGraphics2D().setPaint(getColor(swtGraphics.getForegroundColor()));
		getGraphics2D().drawString(s, xpos, ypos);

		int lineWidth;

		if( isFontUnderlined(getFont()) ) {
			int baseline = y + metrics.getAscent();
			lineWidth = getLineWidth();

			setLineWidth( 1 );
			drawLine( x, baseline,	x + stringLength, baseline );
			setLineWidth( lineWidth );
		}

		if( isFontStrikeout(getFont()) ) {
			int strikeline = y + (metrics.getHeight() / 2);
			lineWidth = getLineWidth();

			setLineWidth( 1 );
			drawLine( x, strikeline, x + stringLength, strikeline );
			setLineWidth( lineWidth );
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#fillString(java.lang.String, int, int)
	 */
	public void fillString(String s, int x, int y) {
		// Not implemented
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#fillText(java.lang.String, int, int)
	 */
	public void fillText(String s, int x, int y) {
		// Not implemented
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#getBackgroundColor()
	 */
	public Color getBackgroundColor() {
		return swtGraphics.getBackgroundColor();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#getClip(org.eclipse.draw2d.geometry.Rectangle)
	 */
	public Rectangle getClip(Rectangle rect) {
		rect.setBounds(relativeClipRegion);
		return rect;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#getFont()
	 */
	public Font getFont() {
		return swtGraphics.getFont();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#getFontMetrics()
	 */
	public FontMetrics getFontMetrics() {
		return swtGraphics.getFontMetrics();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#getForegroundColor()
	 */
	public Color getForegroundColor() {
		return swtGraphics.getForegroundColor();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#getLineStyle()
	 */
	public int getLineStyle() {
		return swtGraphics.getLineStyle();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#getLineWidth()
	 */
	public int getLineWidth() {
		return swtGraphics.getLineWidth();
	}

	public float getLineWidthFloat() {
		return swtGraphics.getLineWidth();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#getXORMode()
	 */
	public boolean getXORMode() {
		return swtGraphics.getXORMode();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#popState()
	 */
	public void popState() {
		swtGraphics.popState();

		restoreState(states.pop());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#pushState()
	 */
	public void pushState() {
		swtGraphics.pushState();

		// Make a copy of the current state and push it onto the stack
		State toPush = new State();
		toPush.copyFrom(currentState);
		states.push(toPush);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#restoreState()
	 */
	public void restoreState() {
		swtGraphics.restoreState();

		restoreState(states.peek());
	}

	private void restoreState(State state) {

		setBackgroundColor(state.bgColor);
		setForegroundColor(state.fgColor);
		setLineStyle(state.lineStyle);
		setLineWidthFloat(state.lineWidth);
		setXORMode(state.XorMode);

		setClipAbsolute(state.clipX, state.clipY, state.clipW, state.clipH);

		transX = currentState.translateX = state.translateX;
		transY = currentState.translateY = state.translateY;

		relativeClipRegion.x = state.clipX - transX;
		relativeClipRegion.y = state.clipY - transY;
		relativeClipRegion.width = state.clipW;
		relativeClipRegion.height = state.clipH;

		currentState.font = state.font;
		currentState.alpha = state.alpha;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#scale(double)
	 */
	public void scale(double amount) {
		swtGraphics.scale(amount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#setBackgroundColor(org.eclipse.swt.graphics.Color)
	 */
	public void setBackgroundColor(Color rgb) {
		currentState.bgColor = rgb;
		swtGraphics.setBackgroundColor(rgb);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#setClip(org.eclipse.draw2d.geometry.Rectangle)
	 */
	public void setClip(Rectangle rect) {

		relativeClipRegion.x = rect.x;
		relativeClipRegion.y = rect.y;
		relativeClipRegion.width = rect.width;
		relativeClipRegion.height = rect.height;

		setClipAbsolute(
			rect.x + transX,
			rect.y + transY,
			rect.width,
			rect.height);
	}

	/**
	 * Sets the current clip values
	 *
	 * @param x
	 *            the x value
	 * @param y
	 *            the y value
	 * @param width
	 *            the width value
	 * @param height
	 *            the height value
	 */
	private void setClipAbsolute(int x, int y, int width, int height) {
		currentState.clipX = x;
		currentState.clipY = y;
		currentState.clipW = width;
		currentState.clipH = height;
	}

	private boolean isFontUnderlined(Font f) {
		return false;
	}

	private boolean isFontStrikeout(Font f) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#setFont(org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void setFont(Font f) {

		swtGraphics.setFont(f);
		currentState.font = f;

		FontData[] fontInfo = f.getFontData();

		if (fontInfo[0] != null) {

			int height = fontInfo[0].getHeight();

			float fsize = (float) height
				* (float) Display.getCurrent().getDPI().x
				/ 72.0f;
			height = Math.round(fsize);

			int style = fontInfo[0].getStyle();
			boolean bItalic = (style & SWT.ITALIC) == SWT.ITALIC;
			boolean bBold = (style & SWT.BOLD) == SWT.BOLD;
			String faceName = fontInfo[0].getName();
			int escapement = 0;

			boolean bUnderline = isFontUnderlined(f);
			boolean bStrikeout = isFontStrikeout(f);

			GdiFont font =
				new GdiFont(
					height,
					bItalic,
					bUnderline,
					bStrikeout,
					bBold,
					faceName,
					escapement);

			getGraphics2D().setFont(font.getFont());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#setForegroundColor(org.eclipse.swt.graphics.Color)
	 */
	@Override
	public void setForegroundColor(Color rgb) {
		currentState.fgColor = rgb;
		swtGraphics.setForegroundColor(rgb);
	}

	/**
	 * Sets the dash pattern when the custom line style is in use. Because this
	 * feature is rarely used, the dash pattern may not be preserved when calling
	 * {@link #pushState()} and {@link #popState()}.
	 * @param dash the pixel pattern
	 *
	 */
	@Override
	public void setLineDash(int[] dash) {
		float dashFlt[] = new float[dash.length];
		for (int i=0; i<dash.length; i++) {
			dashFlt[i] = dash[i];
		}
		setLineDash(dashFlt);
	}

	public void setLineDash(float[] dash) {
		currentState.lineDash = dash;
		stroke =
			new BasicStroke(
				stroke.getLineWidth(),
				stroke.getEndCap(),
				stroke.getLineJoin(),
				stroke.getMiterLimit(),
				dash,
				0);
		getGraphics2D().setStroke(stroke);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#setLineStyle(int)
	 */
	@Override
	public void setLineStyle(int style) {
		currentState.lineStyle = style;

		swtGraphics.setLineStyle(style);

		float dash[] = { 18, 9 };
		float dash_dot[] = { 9, 3, 3, 3 };
		float dash_dot_dot[] = { 9, 3, 3, 3, 3, 3 };
		float dot[] = { 3, 3 };

		float dashPattern[];
		switch (style) {
			case SWTGraphics.LINE_DASH :
				dashPattern = dash;
				break;
			case SWTGraphics.LINE_DASHDOT :
				dashPattern = dash_dot;
				break;
			case SWTGraphics.LINE_DASHDOTDOT :
				dashPattern = dash_dot_dot;
				break;
			case SWTGraphics.LINE_DOT :
				dashPattern = dot;
				break;
			case SWTGraphics.LINE_CUSTOM :
				dashPattern = currentState.lineDash;
				break;
			default :
				dashPattern = null;
		}

		stroke =
			new BasicStroke(
				stroke.getLineWidth(),
				stroke.getEndCap(),
				stroke.getLineJoin(),
				stroke.getMiterLimit(),
				dashPattern,
				0);

		getGraphics2D().setStroke(stroke);
	}

	/**
	 * ignored
	 */
	public void setLineMiterLimit(float miterLimit) {
		// do nothing
	}

	/**
	 * ignored
	 */
	@Override
	public void setLineCap(int cap) {
		// do nothing
	}

	/**
	 * ignored
	 */
	@Override
	public void setLineJoin(int join) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#setLineWidth(int)
	 */
	@Override
	public void setLineWidth(int width) {
		setLineWidthFloat(width);
	}

	public void setLineWidthFloat(float width) {
		currentState.lineWidth = width;
		swtGraphics.setLineWidth((int) width);

		stroke =
			new BasicStroke(
				width,
				stroke.getEndCap(),
				stroke.getLineJoin(),
				stroke.getMiterLimit(),
				stroke.getDashArray(),
				0);

		getGraphics2D().setStroke(stroke);
	}

	public void setLineAttributes(LineAttributes lineAttributes) {
//		swtGraphics.setLineAttributes(lineAttributes);
		setLineWidthFloat(lineAttributes.width);
		setLineStyle(lineAttributes.style);
		setLineDash(lineAttributes.dash);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.draw2d.Graphics#setXORMode(boolean)
	 */
	@Override
	public void setXORMode(boolean xorMode) {
		currentState.XorMode = xorMode;
		swtGraphics.setXORMode(xorMode);
	}

	/**
	 * Sets the current translation values
	 *
	 * @param x
	 *            the x translation value
	 * @param y
	 *            the y translation value
	 */
	private void setTranslation(int x, int y) {
		transX = currentState.translateX = x;
		transY = currentState.translateY = y;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.draw2d.Graphics#translate(int, int)
	 */
	@Override
	public void translate(int dx, int dy) {
		swtGraphics.translate(dx, dy);

		setTranslation(transX + dx, transY + dy);
		relativeClipRegion.x -= dx;
		relativeClipRegion.y -= dy;
	}

	/**
	 * @return the <code>Graphics2D</code> that this is delegating to.
	 */
	protected Graphics2D getGraphics2D() {
		return graphics2D;
	}

	/**
	 * @return Returns the swtGraphics.
	 */
	private SWTGraphics getSWTGraphics() {
		return swtGraphics;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.draw2d.Graphics#fillGradient(int, int, int, int, boolean)
	 */
	@Override
	public void fillGradient(int x, int y, int w, int h, boolean vertical) {
		GradientPaint gradient;

		checkState();

		// Gradients in SWT start with Foreground Color and end at Background
		java.awt.Color start = getColor( getSWTGraphics().getForegroundColor() );
		java.awt.Color stop = getColor( getSWTGraphics().getBackgroundColor() );

		// Create the Gradient based on horizontal or vertical
		if( vertical ) {
			gradient = new GradientPaint(x+transX,y+transY, start, x+transX, y+h+transY, stop);
		} else {
			gradient = new GradientPaint(x+transX,y+transY, start, x+w+transX, y+transY, stop);
		}

		Paint oldPaint = getGraphics2D().getPaint();
		getGraphics2D().setPaint(gradient);
		getGraphics2D().fill(new Rectangle2D.Double(x+transX, y+transY, w, h));
		getGraphics2D().setPaint(oldPaint);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.Graphics#drawPath(org.eclipse.swt.graphics.Path)
	 */
	public void drawPath(Path path) {
		checkState();
		getGraphics2D().setPaint(getColor(swtGraphics.getForegroundColor()));
		getGraphics2D().draw(createPathAWT(path));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.Graphics#fillPath(org.eclipse.swt.graphics.Path)
	 */
	public void fillPath(Path path) {
		checkState();
		getGraphics2D().setPaint(getColor(swtGraphics.getBackgroundColor()));
		getGraphics2D().fill(createPathAWT(path));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.Graphics#setClip(org.eclipse.swt.graphics.Path)
	 */
	public void setClip(Path path) {
		if (((appliedState.graphicHints ^ currentState.graphicHints) & FILL_RULE_MASK) != 0) {
			//If there is a pending change to the fill rule, apply it first.
			//As long as the FILL_RULE is stored in a single bit, just toggling it works.
			appliedState.graphicHints ^= FILL_RULE_MASK;
		}
		getGraphics2D().setClip(createPathAWT(path));
		appliedState.clipX = currentState.clipX = 0;
		appliedState.clipY = currentState.clipY = 0;
		appliedState.clipW = currentState.clipW = 0;
		appliedState.clipH = currentState.clipH = 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.Graphics#getFillRule()
	 */
	public int getFillRule() {
		return ((currentState.graphicHints & FILL_RULE_MASK) >> FILL_RULE_SHIFT) - FILL_RULE_WHOLE_NUMBER;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.Graphics#setFillRule(int)
	 */
	public void setFillRule(int rule) {
		currentState.graphicHints &= ~FILL_RULE_MASK;
		currentState.graphicHints |= (rule + FILL_RULE_WHOLE_NUMBER) << FILL_RULE_SHIFT;
	}

	private GeneralPath createPathAWT(Path path) {
		GeneralPath pathAWT = new GeneralPath();
		PathData pathData = path.getPathData();
		int idx = 0;
		for (int i = 0; i < pathData.types.length; i++) {
			switch (pathData.types[i]) {
				case SWT.PATH_MOVE_TO:
					pathAWT.moveTo(pathData.points[idx++] + transX,
						pathData.points[idx++] + transY);
					break;
				case SWT.PATH_LINE_TO:
					pathAWT.lineTo(pathData.points[idx++] + transX,
						pathData.points[idx++] + transY);
					break;
				case SWT.PATH_CUBIC_TO:
					pathAWT.curveTo(pathData.points[idx++] + transX,
						pathData.points[idx++] + transY, pathData.points[idx++]
						+ transX, pathData.points[idx++] + transY,
						pathData.points[idx++] + transX, pathData.points[idx++]
						+ transY);
					break;
				case SWT.PATH_QUAD_TO:
					pathAWT.quadTo(pathData.points[idx++] + transX,
						pathData.points[idx++] + transY, pathData.points[idx++]
						+ transX, pathData.points[idx++] + transY);
					break;
				case SWT.PATH_CLOSE:
					pathAWT.closePath();
					break;
				default:
					dispose();
					SWT.error(SWT.ERROR_INVALID_ARGUMENT);
			}
		}
		int swtWindingRule = ((appliedState.graphicHints & FILL_RULE_MASK) >> FILL_RULE_SHIFT)
			- FILL_RULE_WHOLE_NUMBER;
		if (swtWindingRule == SWT.FILL_WINDING) {
			pathAWT.setWindingRule(GeneralPath.WIND_NON_ZERO);
		} else if (swtWindingRule == SWT.FILL_EVEN_ODD) {
			pathAWT.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		} else {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		return pathAWT;
	}
/*
	public RenderedImage drawRenderedImage(RenderedImage srcImage, Rectangle rect, RenderingListener listener) {
		RenderInfo info = srcImage.getRenderInfo();
		info.setValues(rect.width, rect.height,
						info.shouldMaintainAspectRatio(), info.shouldAntiAlias(),
						info.getBackgroundColor(), info.getForegroundColor());

		RenderedImage img = srcImage.getNewRenderedImage(info);

		BufferedImage bufImg = (BufferedImage)img.getAdapter(BufferedImage.class);
		if (bufImg == null) {
			bufImg = ImageConverter.convert(img.getSWTImage());
		}

		// Translate the Coordinates
		int x = rect.x + transX;
		int y = rect.y + transY + rect.height - bufImg.getHeight();

		checkState();
		getGraphics2D().drawImage(
			bufImg,
			new AffineTransform(1f, 0f, 0f, 1f, x, y),
			null);

		return img;
	}
*/
	/**
	 * Flag whether to allow delayed rendering.
	 */
	public boolean shouldAllowDelayRender() {
		return false;
	}

	/**
	 *
	 * @return always <code>null</code>
	 */
	public Dimension getMaximumRenderSize() {
		return null;
	}

	/**
	 * Accessor method to return the translation offset for the graphics object
	 *
	 * @return <code>Point</code> x coordinate for graphics translation
	 */
	protected Point getTranslationOffset() {
		return new Point(transX, transY);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.draw2d.Graphics#getAntialias()
	 */
	@Override
	public int getAntialias() {
		Object antiAlias = getGraphics2D().getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		if (antiAlias != null) {
			if (antiAlias.equals(RenderingHints.VALUE_ANTIALIAS_ON))
				return SWT.ON;
			else if (antiAlias.equals(RenderingHints.VALUE_ANTIALIAS_OFF))
				return SWT.OFF;
			else if (antiAlias.equals(RenderingHints.VALUE_ANTIALIAS_DEFAULT))
				return SWT.DEFAULT;
		}

		return SWT.DEFAULT;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.draw2d.Graphics#setAntialias(int)
	 */
	@Override
	public void setAntialias(int value) {
		if (value == SWT.ON) {
			getGraphics2D().setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		else if (value == SWT.OFF) {
			getGraphics2D().setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}

	@Override
	public int getAlpha() {
		return swtGraphics.getAlpha();
	}

	@Override
	public void setAlpha(int alpha) {
		swtGraphics.setAlpha(alpha);
		currentState.alpha = alpha;

		Composite composite = getGraphics2D().getComposite();
		if (composite instanceof AlphaComposite) {
			AlphaComposite newComposite = AlphaComposite.getInstance(
				((AlphaComposite) composite).getRule(), (float) alpha / (float) 255);
			getGraphics2D().setComposite(newComposite);
		}
	}

	protected BasicStroke getStroke(){
		return stroke;
	}

	protected void setStroke(BasicStroke stroke){
		this.stroke = stroke;
		getGraphics2D().setStroke(stroke);
	}
}
