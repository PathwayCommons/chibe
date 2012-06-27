package org.gvt.command;

import org.eclipse.gef.commands.Command;
import org.gvt.ChisioMain;
import org.gvt.LayoutManager;
import org.gvt.model.CompoundModel;
import org.ivis.layout.*;

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
	private LayoutManager manager;

	public LayoutCommand(ChisioMain main, CompoundModel root, Layout layout)
	{
		super("Layout Command");
		this.manager = LayoutManager.getInstance();
		this.manager.setLayout(layout);
		this.manager.setRoot(root);
		this.manager.setMain(main);
	}

	public void execute()
	{
		// perform required operations before the layout
		this.manager.preRun();

		// create topology for chiLay
		this.manager.createTopology();

		// run the layout
		this.manager.runLayout();

		// finalize the layout by performing updates
		this.manager.postRun();
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