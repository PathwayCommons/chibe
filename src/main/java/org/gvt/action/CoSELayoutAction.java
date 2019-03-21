package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.gvt.ChisioMain;
import org.gvt.command.LayoutCommand;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.layout.BiPaLayout;
import org.gvt.layout.EdgeFixLayout;
import org.gvt.model.CompoundModel;
import org.ivis.layout.cose.CoSELayout;

/**
 * Action for CoSE layout operation.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class CoSELayoutAction extends Action
{
	ChisioMain main = null;
	ScrollingGraphicalViewer viewer;

	/**
	 * Constructor
	 */
	public CoSELayoutAction(ChisioMain main)
	{
		this(main, null);
	}

	public CoSELayoutAction(ChisioMain main, ScrollingGraphicalViewer viewer)
	{
		super("CoSE Layout");
		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(
                ChisioMain.class, "icon/layout-cose.gif"));
		this.main = main;
		this.viewer = viewer;
	}

	public void run()
	{
		if (viewer == null) viewer = main.getViewer();
		if (viewer == null) return;

		try
		{
			main.lockWithMessage("Performing layout ...");

			CompoundModel root = (CompoundModel)((ChsRootEditPart) viewer.
					getRootEditPart().getChildren().get(0)).getModel();

//			main.lockWithMessage("Performing layout (" + root.getNodes().size() + " nodes, " +
//				root.getEdges().size() + " edges) ...");

//			LayoutCommand command = new LayoutCommand(main, root, new CoSELayout());
			LayoutCommand command = new LayoutCommand(main, root, new BiPaLayout());
			command.execute();
			command = new LayoutCommand(main, root, new EdgeFixLayout());
			command.execute();

			main.makeDirty();
			viewer = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			main.unlock();
		}
	}
}