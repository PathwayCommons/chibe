package org.gvt;

import java.util.Iterator;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.*;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.gvt.command.ChsCompoundCommand;

/**
 * This class handles the dragging.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsDragEditPartsTracker extends DragEditPartsTracker
{
	public ChsDragEditPartsTracker(EditPart sourceEditPart)
	{
		super(sourceEditPart);
	}

	protected Command getCommand()
	{
		ChsCompoundCommand command = new ChsCompoundCommand();
		command.setDebugLabel("Drag Object Tracker");

		Iterator iter = getOperationSet().iterator();

		Request request = getTargetRequest();

		if (isCloneActive())
		{
			request.setType(REQ_CLONE);
		}
		else if (isMove())
		{
			request.setType(REQ_MOVE);
		}
		else
		{
			request.setType(REQ_ORPHAN);
		}

		if (!isCloneActive())
		{
			while (iter.hasNext())
			{
				EditPart editPart = (EditPart)iter.next();
				command.add(editPart.getCommand(request));
			}
		}

		/*OK*/
		if (ChisioMain.transferNode)
		{
			if (!isMove() || isCloneActive())
			{
				if (!isCloneActive())
				{
					request.setType(REQ_ADD);
				}

				if (getTargetEditPart() == null)
				{
					command.add(UnexecutableCommand.INSTANCE);
				}
				else
				{
					command.add(
						getTargetEditPart().getCommand(getTargetRequest()));
				}
			}
		}

		return command;
	}
}
