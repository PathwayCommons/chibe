package org.gvt.editpart;

import org.biopax.paxtools.model.level3.SmallMolecule;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.*;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.gvt.ChsCellEditorLocator;
import org.gvt.ChsDirectEditManager;
import org.gvt.ChsDragEditPartsTracker;
import org.gvt.editpolicy.ChsComponentEditPolicy;
import org.gvt.editpolicy.ChsDirectEditPolicy;
import org.gvt.editpolicy.ChsGraphicalNodeEditPolicy;
import org.gvt.figure.ChsChopboxAnchor;
import org.gvt.figure.HighlightLayer;
import org.gvt.figure.NodeFigure;
import org.gvt.model.EdgeModel;
import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl3.Actor;
import org.gvt.model.custom.CustomNode;
import org.gvt.util.EntityHolder;

import java.beans.PropertyChangeEvent;
import java.util.List;

/**
 * This class maintains the editpart for Nodes.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsNodeEditPart extends EditPartWithListener
	implements NodeEditPart
{
	ChsDirectEditManager directManager = null;

	protected IFigure createFigure()
	{
		NodeModel model = getNodeModel();
		NodeFigure nFigure = new NodeFigure(model.getLocationAbs(),
			model.getSize(),
			model.getText(),
			model.getTooltipText(),
			model.getTextFont(),
			model.getTextColor(),
			model.getColor(),
			model.getBorderColor(),
			model.getBorderWidth(),
			model.getShape(),
			model.getHighlightColor(),
			model.isHighlight(),
			model instanceof Actor ? ((Actor) model).getMultimerNo() : 1);

		nFigure.updateHighlight(
			(HighlightLayer) getLayer(HighlightLayer.HIGHLIGHT_LAYER),
			getNodeModel().isHighlight());

		if (model instanceof Actor)
		{
			EntityHolder eh = ((Actor) model).getEntity();
			if (eh.l3pe instanceof SmallMolecule)
			{
				nFigure.setSmallMolecule(true);
				
				if (((Actor) model).isUbique())
				{
					nFigure.setDrawCloneMarker(true);
				}
			}
		}
		else if (model instanceof CustomNode)
		{
			if (((CustomNode) model).isDuplicate())
			{
				nFigure.setDrawCloneMarker(true);
			}
		}
		
		return nFigure;
	}

	public DragTracker getDragTracker(Request request)
	{
		return new ChsDragEditPartsTracker(this);
	}

	public void performRequest(Request req)
	{
		if (req.getType().equals(RequestConstants.REQ_DIRECT_EDIT))
		{
			performDirectEdit();

			return;
		}

		super.performRequest(req);
	}

	private void performDirectEdit()
	{
		if (directManager == null)
		{
			directManager =
				new ChsDirectEditManager(
					this,
					TextCellEditor.class,
					new ChsCellEditorLocator(getFigure()));
		}

		directManager.show();
	}

	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
			new ChsComponentEditPolicy());
		installEditPolicy(
			EditPolicy.DIRECT_EDIT_ROLE,
			new ChsDirectEditPolicy());
		installEditPolicy(
			EditPolicy.GRAPHICAL_NODE_ROLE,
			new ChsGraphicalNodeEditPolicy());
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals(NodeModel.P_CONSTRAINT))
		{
			refreshVisuals();
		}
		else if (evt.getPropertyName().equals(NodeModel.P_TEXT))
		{
			((NodeFigure)figure).updateText((String) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(NodeModel.P_TOOLTIP_TEXT))
		{
			((NodeFigure)figure).updateTooltipText((String) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(NodeModel.P_TEXTFONT))
		{
			((NodeFigure)figure).updateTextFont((Font) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(NodeModel.P_TEXTCOLOR))
		{
			((NodeFigure)figure).updateTextColor((Color) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(NodeModel.P_COLOR))
		{
			((NodeFigure)figure).updateColor((Color) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(NodeModel.P_BORDERCOLOR))
		{
			((NodeFigure)figure).updateBorderColor((Color) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(NodeModel.P_BORDERWIDTH))
		{
			((NodeFigure)figure).updateBorderWidth((Integer) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(NodeModel.P_SHAPE))
		{
			((NodeFigure)figure).updateShape((String) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(NodeModel.P_CONNX_SOURCE))
		{
			refreshSourceConnections();
		}
		else if (evt.getPropertyName().equals(NodeModel.P_CONNX_TARGET))
		{
			refreshTargetConnections();
		}
		else if (evt.getPropertyName().equals(EdgeModel.P_HIGHLIGHT))
		{
			((NodeFigure)figure).updateHighlight(
				(Layer) getLayer(HighlightLayer.HIGHLIGHT_LAYER),
				getNodeModel().isHighlight());
		}
		else if (evt.getPropertyName().equals(EdgeModel.P_HIGHLIGHTCOLOR))
		{
			((NodeFigure)figure).
				updateHighlightColor((Color) evt.getNewValue());
		}
	}

	protected void refreshVisuals()
	{
		Rectangle constraint = getNodeModel().getConstraint();

		((GraphicalEditPart) getParent()).setLayoutConstraint(
			this,
			getFigure(),
			constraint);
	}

	protected List getModelSourceConnections()
	{
		return getNodeModel().getSourceConnections();
	}

	protected List getModelTargetConnections()
	{
		return getNodeModel().getTargetConnections();
	}

	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection)
	{
		return new ChsChopboxAnchor(getFigure());
	}

	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection)
	{
		return new ChsChopboxAnchor(getFigure());
	}

	public ConnectionAnchor getSourceConnectionAnchor(Request request)
	{
		return new ChsChopboxAnchor(getFigure());
	}

	public ConnectionAnchor getTargetConnectionAnchor(Request request)
	{
		return new ChsChopboxAnchor(getFigure());
	}
}