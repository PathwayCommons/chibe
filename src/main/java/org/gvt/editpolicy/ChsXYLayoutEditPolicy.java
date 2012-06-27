package org.gvt.editpolicy;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.gvt.ChsXYLayout;
import org.gvt.command.AddCommand;
import org.gvt.command.CreateCommand;
import org.gvt.command.MoveCommand;
import org.gvt.editpart.ChsCompoundEditPart;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;

import java.util.List;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsXYLayoutEditPolicy extends XYLayoutEditPolicy
{
	public ChsXYLayoutEditPolicy()
	{
		super();
		setXyLayout(new ChsXYLayout());
	}

	protected Command createAddCommand(Request request,
		EditPart childEditPart,
		Object constraint)
	{
		NodeModel part = (NodeModel) childEditPart.getModel();
		Rectangle rect = (Rectangle) constraint;

		AddCommand add = new AddCommand();
		add.setParent((CompoundModel) getHost().getModel());
		add.setChild(part);

		MoveCommand move = new MoveCommand(part, rect);
		move.setParent((CompoundModel) getHost().getModel());

		return add.chain(move);
	}

	protected Command createChangeConstraintCommand(EditPart child,
		Object constraint)
	{
		return new MoveCommand(child, (Rectangle) constraint);
	}

	protected EditPolicy createChildEditPolicy(EditPart child)
	{
		if (child instanceof ChsCompoundEditPart)
		{
			return new ChsNonResizableEditPolicy();
		}
		else
		{
			return new ChsResizableEditPolicy();
		}
	}

	protected IFigure createSizeOnDropFeedback(CreateRequest createRequest)
	{
		IFigure figure;

		figure = new RectangleFigure();
		((RectangleFigure) figure).setFill(true);
		figure.setBackgroundColor(NodeModel.DEFAULT_COLOR);
		figure.setForegroundColor(ColorConstants.white);

		addFeedback(figure);

		return figure;
	}

	protected Command getAddCommand(Request generic)
	{
		ChangeBoundsRequest request = (ChangeBoundsRequest) generic;
		List editParts = request.getEditParts();
		CompoundCommand command = new CompoundCommand();
		command.setDebugLabel("Add in ConstrainedLayoutEditPolicy");
		GraphicalEditPart childPart;
		Rectangle r;
		Object constraint;

		for (int i = 0; i < editParts.size(); i++)
		{
			childPart = (GraphicalEditPart) editParts.get(i);
			r = childPart.getFigure().getBounds().getCopy();
			//convert r to absolute from childpart figure
			childPart.getFigure().translateToAbsolute(r);
			r = request.getTransformedRectangle(r);
			//convert this figure to relative
			getLayoutContainer().translateToRelative(r);
			getLayoutContainer().translateFromParent(r);
			r.translate(getLayoutOrigin().getNegated());

			// Because of the scaling and not using double type in size,
			// nodes size was getting bigger. this bug is fixed.
			r.setSize(childPart.getFigure().getSize().getCopy());
			r.setLocation(r.getLocation().translate(1, 1));

			constraint = getConstraintFor(r);
			command.add(createAddCommand(generic,
				childPart,
				translateToModelConstraint(constraint)));
		}

		return command.unwrap();
	}

	protected Command getCreateCommand(CreateRequest request)
	{
		CreateCommand create = new CreateCommand(
			(CompoundModel)getHost().getModel(),
			(NodeModel)request.getNewObject());
		Rectangle constraint = (Rectangle) getConstraintFor(request);
		create.setConstraint(constraint);

		return create;
	}

	protected Insets getCreationFeedbackOffset(CreateRequest request)
	{
		return new Insets();
	}

	protected Command getDeleteDependantCommand(Request request)
	{
		return null;
	}

	/**
	 * Returns the layer used for displaying feedback.
	 *
	 * @return the feedback layer
	 */
	protected IFigure getFeedbackLayer()
	{
		return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
	}

	protected Command createAddCommand(EditPart child, Object constraint)
	{
		return null;
	}
}