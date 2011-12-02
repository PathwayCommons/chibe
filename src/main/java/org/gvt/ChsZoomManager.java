package org.gvt;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.editparts.ZoomListener;

/**
 * ZoomManager of GEF is extended to redesign it in necessary points. Zoom
 * levels are extended. Also MarqueeZoom operation was not implemented in GEF.
 * It is implemented. This implemention is also used in FIT IN WINDOW operation.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsZoomManager extends ZoomManager implements ZoomListener
{
	ScalableLayeredPane pane;

	ChsViewport viewport;

	Rectangle newZoomArea;

	public ChsZoomManager(ScalableLayeredPane pane, ChsViewport viewport)
	{
		super(pane, viewport);
		this.pane = pane;
		this.viewport = viewport;

		double[] zoomLevels = {.01, .05, .1, .5, .75, 1.0, 1.5, 2.0, 2.5, 3.0,
			4.0, 6.0, 8.0, 10.0, 15.0, 20.0, 40.0};
		setZoomLevels(zoomLevels);
		addZoomListener(this);
	}

	public void zoomTo(Rectangle rect)
	{
		Point p = primSetZoom(rect);
		// move to middle of the screen
		p.x -= (viewport.getSize().width - newZoomArea.getSize().width) / 2;
		p.y -= (viewport.getSize().height - newZoomArea.getSize().height) / 2;
		viewport.setViewLocation(p.x, p.y);
	}

	protected Point primSetZoom(Rectangle rect)
	{
		// new zoom level is calculated.
		double zoom = getFitXZoomLevel(rect);
		double prevZoom = getZoom();
		//before applying new zoom, view location is kept
		Point l = viewport.getViewLocation();
		// new zoom is applied.
		setZoom(zoom);

		// previous zoom location is used for transformation
		newZoomArea = rect.getCopy().translate(l);
		newZoomArea.scale(zoom / prevZoom);

		// point for setting viewport.
		return newZoomArea.getLocation();
	}

	/**
	 * Calculates the new zoom level according to marquee zoom rectangle and
	 * viewport size.
	 */
	private double getFitXZoomLevel(Rectangle bound)
	{
		IFigure fig = getScalableFigure();

		Dimension available = getViewport().getClientArea().getSize();
		Dimension desired;

		if (fig instanceof FreeformFigure)
		{
			desired = ((FreeformFigure) fig).getFreeformExtent().getCopy().
				union(0, 0).getSize();
		}
		else
		{
			desired = bound.getSize().getCopy();
		}

		desired.width -= fig.getInsets().getWidth();
		desired.height -= fig.getInsets().getHeight();

		while (fig != getViewport())
		{
			available.width -= fig.getInsets().getWidth();
			available.height -= fig.getInsets().getHeight();
			fig = fig.getParent();
		}

		double scaleX = available.width * getZoom() / desired.width;
		double scaleY = available.height * getZoom() / desired.height;

		return Math.min(scaleX, scaleY);
	}

	public void zoomChanged(double zoom)
	{
		// updates the zoom combo
		ChisioMain.updateCombo(getZoomAsText());
	}
}
