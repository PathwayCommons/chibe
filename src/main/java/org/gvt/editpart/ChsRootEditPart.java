package org.gvt.editpart;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.*;
import org.eclipse.gef.*;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.swt.SWT;
import org.gvt.ChisioMain;
import org.gvt.ChsDragEditPartsTracker;
import org.gvt.ChsXYLayout;
import org.gvt.editpolicy.ChsContainerEditPolicy;
import org.gvt.editpolicy.ChsXYLayoutEditPolicy;
import org.gvt.model.CompoundModel;

/**
 * This class maintains the edit part for root model. Root is the main graph in
 * Chisio.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsRootEditPart extends EditPartWithListener
{
	/**
	 * Creates the figure of root model. In fact there is not any UI. An empty
	 * are for drawing the child graph objects is created.
	 */
	protected IFigure createFigure()
	{
		IFigure figure = new LayeredPane();
		figure.setLayoutManager(new ChsXYLayout());
		return figure;
	}

	public DragTracker getDragTracker(Request request)
	{
		return new ChsDragEditPartsTracker(this);
	}

	/**
	 * installs the edit policies of this edit part
	 */
	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
			new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.CONTAINER_ROLE,
			new ChsContainerEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ChsXYLayoutEditPolicy());
	}

// -----------------------------------------------------------------------------
// Section: Getters
// -----------------------------------------------------------------------------

	private CompoundModel getRootModel()
	{
		return (CompoundModel) getModel();
	}

	protected List getModelChildren()
	{
		return getRootModel().getChildren();
	}

// -----------------------------------------------------------------------------
// Section: Overriden Methods
// -----------------------------------------------------------------------------
	/**
	 * Updates the UI of root model.
	 */
	protected void refreshVisuals()
	{
		ConnectionLayer cLayer = (ConnectionLayer) getLayer("Connection Layer");
		
//		if (ChisioMain.runningOnWindows)
		{
			cLayer.setAntialias(SWT.ON);
		}
//		else
//		{
//			cLayer.setAntialias(SWT.OFF);
//		}
		
		//AutomaticRouter router = new FanRouter();
		//router.setNextRouter(new BendpointConnectionRouter());
		//cLayer.setConnectionRouter(router);
	}

	/**
	 * if a child is added or deleted handles it.
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals(CompoundModel.P_CHILDREN))
			refreshChildren();
	}
}