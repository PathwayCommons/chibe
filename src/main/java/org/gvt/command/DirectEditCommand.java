package org.gvt.command;

import org.eclipse.gef.commands.Command;
import org.gvt.model.GraphObject;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class DirectEditCommand extends Command
{
	private String oldText, newText;

	private GraphObject model;

	/* (”ñ Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute()
	{
		oldText = model.getText();
		model.setText(newText);
	}

	public void setModel(Object model)
	{
		this.model = (GraphObject) model;
	}

	public void setText(String text)
	{
		newText = text;
	}

	/* (”ñ Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo()
	{
		model.setText(oldText);
	}
}