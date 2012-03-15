package org.gvt.editpart;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.*;
import org.eclipse.gef.*;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.gvt.*;
import org.gvt.figure.HighlightLayer;

/**
 * ScalableRootEditPart of GEF is extended to redesign it in necessary points.
 * ChsViewport, ChsZoomManager and ChsMarqueeDragTracker are created instead of
 * GEF classes.
 * 
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsScalableRootEditPart extends ScalableRootEditPart
{
	ChsZoomManager zoomManager;

	private PropertyChangeListener gridListener = new PropertyChangeListener()
	{
		public void propertyChange(PropertyChangeEvent evt)
		{
			String property = evt.getPropertyName();
			if (property.equals(SnapToGrid.PROPERTY_GRID_ORIGIN)
				|| property.equals(SnapToGrid.PROPERTY_GRID_SPACING)
				|| property.equals(SnapToGrid.PROPERTY_GRID_VISIBLE))
				refreshGridLayer();
		}
	};

	/**
	 * Constructor for ScalableFreeformRootEditPart
	 */
	public ChsScalableRootEditPart()
	{
		zoomManager = new ChsZoomManager((ScalableLayeredPane) getScaledLayers(),
				((ChsViewport) getFigure()));
	}

	/**
	 * Returns the zoomManager.
	 *
	 * @return ZoomManager
	 */
	public ZoomManager getZoomManager()
	{
		return zoomManager;
	}

	/**
	 * Constructs the viewport that will be used to contain all of the layers.
	 * @return a new Viewport
	 */
	protected Viewport createViewport()
	{
		return new ChsViewport(true);
	}

	/**
	 * Should not be called, but returns a MarqeeDragTracker for good measure.
	 *
	 * @see org.eclipse.gef.EditPart#getDragTracker(org.eclipse.gef.Request)
	 */
	public DragTracker getDragTracker(Request req)
	{
		/* 
		 * The root will only be asked for a drag tracker if for some reason the contents
		 * editpart says it is neither selector nor opaque.
		 */
		return new ChsMarqueeDragTracker();
	}

	protected void register()
	{
		super.register();
		getViewer().setProperty(ZoomManager.class.toString(), getZoomManager());
		if (getLayer(GRID_LAYER) != null)
		{
			getViewer().addPropertyChangeListener(gridListener);
			refreshGridLayer();
		}
	}

	protected void unregister()
	{
		getViewer().removePropertyChangeListener(gridListener);
		super.unregister();
		getViewer().setProperty(ZoomManager.class.toString(), null);
	}

	protected ScalableLayeredPane createScaledLayers()
	 {
		ScalableLayeredPane slp = super.createScaledLayers();
		slp.add(new HighlightLayer(), HighlightLayer.HIGHLIGHT_LAYER, 0);

		return slp;
	}
}