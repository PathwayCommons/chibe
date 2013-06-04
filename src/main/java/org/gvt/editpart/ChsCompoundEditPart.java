package org.gvt.editpart;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.*;
import org.gvt.editpolicy.*;
import org.gvt.figure.*;
import org.gvt.model.*;

/**
 * This class maintains the editpart for Compound Nodes.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsCompoundEditPart extends ChsNodeEditPart
{
	protected IFigure createFigure()
	{
		NodeModel model = getNodeModel();
		CompoundFigure cFigure = new CompoundFigure(model.getLocationAbs(),
			model.getSize(),
			model.getText(),
			model.getTooltipText(),
			model.getTextFont(),
			model.getTextColor(),
			model.getColor(),
			model.getBorderColor(),
			model.getHighlightColor(),
			model.isHighlight());

		cFigure.updateHighlight(
			(HighlightLayer) getLayer(HighlightLayer.HIGHLIGHT_LAYER),
			getNodeModel().isHighlight());
		
		return cFigure;
	}

	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
			new ChsComponentEditPolicy());
		installEditPolicy(EditPolicy.CONTAINER_ROLE,
			new ChsContainerEditPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
			new ChsGraphicalNodeEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE,
			new ChsXYLayoutEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE,
			new ChsDirectEditPolicy());
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,
			new CompoundHighlightEditPolicy());
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals(CompoundModel.P_CHILDREN))
		{
			if (evt.getOldValue() instanceof Integer)
			{	// new child
				addChild(createChild(
					evt.getNewValue()),
					((Integer) evt.getOldValue()).intValue());
			}
			else
			{	// remove child
				removeChild((EditPart) getViewer().getEditPartRegistry().
					get(evt.getOldValue()));
			}
		}
		super.propertyChange(evt);
	}

	protected void refreshVisuals()
	{
		Rectangle constraint = getCompoundModel().getConstraint();
		((GraphicalEditPart) getParent()).setLayoutConstraint(this,
			getFigure(),
			constraint);
	}
	
	public CompoundModel getCompoundModel()
	{
		return (CompoundModel) getModel();
	}

	protected List getModelChildren()
	{
		return getCompoundModel().getChildren();
	}
}