package org.gvt.command;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.gvt.layout.*;
import org.gvt.ChisioMain;

/**
 * This command performs a layout on the compound graph passed to its
 * constructor.
 * 
 * @author Cihan Kucukkececi
 * 
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class LayoutCommand extends Command
{
	private AbstractLayout layout;

	public LayoutCommand(ChisioMain main, AbstractLayout layout)
	{
		super("Layout Command");
		this.layout = layout;
		this.layout.setViewer(main.getViewer());
	}

	public LayoutCommand(ScrollingGraphicalViewer viewer, AbstractLayout layout)
	{
		super("Layout Command");
		this.layout = layout;
		this.layout.setViewer(viewer);
	}

	public void execute()
	{
		this.layout.runLayout();
	}

	public void redo()
	{
		//TODO
	}

	public void undo()
	{
		//TODO
	}
}