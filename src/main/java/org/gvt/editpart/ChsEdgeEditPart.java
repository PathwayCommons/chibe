package org.gvt.editpart;

import org.eclipse.draw2d.*;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.gvt.GraphAnimation;
import org.gvt.editpolicy.ChsBendpointEditPolicy;
import org.gvt.editpolicy.ChsConnectionEditPolicy;
import org.gvt.editpolicy.ChsConnectionEndpointEditPolicy;
import org.gvt.figure.EdgeFigure;
import org.gvt.figure.HighlightLayer;
import org.gvt.model.EdgeBendpoint;
import org.gvt.model.EdgeModel;
import org.ivis.layout.LayoutOptionsPack;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * This class maintains the editpart for Edges. Bendpoint supported is handled.
 *
 * @author Cihan Kucukkececi
 * 
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsEdgeEditPart extends AbstractConnectionEditPart
	implements PropertyChangeListener
{
	/**
	 *  creates the UI of edge model.
	 * @return figure
	 */
	protected IFigure createFigure()
	{
		EdgeModel model = getEdgeModel();
		EdgeFigure eFigure = new EdgeFigure(model.getText(),
			model.getTextFont(),
			model.getTextColor(),
			model.getColor(),
			model.getStyle(),
			model.getArrow(),
			model.getWidth(),
			model.getHighlightColor(),
			model.isHighlight());

		eFigure.updateHighlight(
			(HighlightLayer) getLayer(HighlightLayer.HIGHLIGHT_LAYER),
			getEdgeModel().isHighlight());
		eFigure.setConnectionRouter(new BendpointConnectionRouter()
		{
			public void route(Connection conn)
			{
				if (!LayoutOptionsPack.getInstance().getGeneral().animationDuringLayout
					|| LayoutOptionsPack.getInstance().getGeneral().animationOnLayout)
				{
					GraphAnimation.recordInitialState(conn);

					if (GraphAnimation.playbackState(conn))
					{
						return;
					}
				}
				else
				{
					GraphAnimation.recordInitialState((IFigure)conn);

					if (GraphAnimation.playbackState((IFigure)conn))
					{
						return;
					}
				}

				super.route(conn);
			}
		});

		return eFigure;
	}

	/**
	 * creates edit polies for this editpart
	 */
	protected void createEditPolicies()
	{
		installEditPolicy(
			EditPolicy.CONNECTION_ROLE,
			new ChsConnectionEditPolicy());

		installEditPolicy(
			EditPolicy.CONNECTION_BENDPOINTS_ROLE,
			new ChsBendpointEditPolicy());

		installEditPolicy(
			EditPolicy.CONNECTION_ENDPOINTS_ROLE,
			new ChsConnectionEndpointEditPolicy());
	}

	/**
	 * updates the bendpoints when needed.
	 */
	protected void refreshBendpoints()
	{
		if (getConnectionFigure().getConnectionRouter()
			instanceof ManhattanConnectionRouter)
		{
			return;
		}

		List bendpoints = ((EdgeModel) getModel()).getBendpoints();
		List figureConstraint = new ArrayList();

		for (int i = 0; i < bendpoints.size(); i++)
		{
			EdgeBendpoint bp = (EdgeBendpoint) bendpoints.get(i);
			bp.setConnection(getConnectionFigure());
			bp.setWeight((i + 1) / ((float) bendpoints.size() + 1));
			figureConstraint.add(bp);
		}
		
		getConnectionFigure().setRoutingConstraint(figureConstraint);		
	}

	/**
	 * Handles the changes about edges
	 *
	 * @param evt event fired
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals(EdgeModel.P_TEXT))
		{
			((EdgeFigure)figure).updateText((String) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(EdgeModel.P_TEXTFONT))
		{
			((EdgeFigure)figure).updateTextFont((Font) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(EdgeModel.P_TEXTCOLOR))
		{
			((EdgeFigure)figure).updateTextColor((Color) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(EdgeModel.P_COLOR))
		{
			((EdgeFigure)figure).updateColor((Color) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(EdgeModel.P_STYLE))
		{
			((EdgeFigure)figure).updateStyle((String) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(EdgeModel.P_ARROW))
		{
			((EdgeFigure)figure).updateArrow((String) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(EdgeModel.P_WIDTH))
		{
			((EdgeFigure)figure).updateWidth((Integer) evt.getNewValue());
		}
		else if (evt.getPropertyName().equals(EdgeModel.P_BENDPOINT))
		{
			refreshBendpoints();
		}
		else if (evt.getPropertyName().equals(EdgeModel.P_HIGHLIGHT))
		{
			((EdgeFigure)figure).updateHighlight(
				(Layer) getLayer(HighlightLayer.HIGHLIGHT_LAYER),
				getEdgeModel().isHighlight());
		}
		else if (evt.getPropertyName().equals(EdgeModel.P_HIGHLIGHTCOLOR))
		{
			((EdgeFigure)figure).
				updateHighlightColor((Color) evt.getNewValue());
		}
	}

	/**
	 * activates the listener for this editpart
	 */
	public void activate()
	{
		getEdgeModel().addPropertyChangeListener(this);
		super.activate();
	}

	/**
	 * deactivates the listener for this editpart
	 */
	public void deactivate()
	{
		getEdgeModel().removePropertyChangeListener(this);
		super.deactivate();
	}

	// GETTER method
	public EdgeModel getEdgeModel()
	{
		return (EdgeModel) getModel();
	}
}