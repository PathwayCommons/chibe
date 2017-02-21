package org.gvt.action;

import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.command.LayoutCommand;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.layout.BiPaLayout;
import org.gvt.layout.EdgeFixLayout;
import org.gvt.model.CompoundModel;

/**
 * Action for fixing Edge bendpoint routing.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class EdgeFixLayoutAction extends Action
{
	ChisioMain main = null;
	ScrollingGraphicalViewer viewer;

	/**
	 * Constructor
	 */
	public EdgeFixLayoutAction(ChisioMain main)
	{
		this(main, null);
	}

	public EdgeFixLayoutAction(ChisioMain main, ScrollingGraphicalViewer viewer)
	{
		super("Edge Fix Layout");
		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(
                ChisioMain.class, "icon/layout-spring.gif"));
		this.main = main;
		this.viewer = viewer;
	}

	public void run()
	{
		if (viewer == null) viewer = main.getViewer();
		if (viewer == null) return;

		try
		{
			CompoundModel root = (CompoundModel)((ChsRootEditPart) viewer.
					getRootEditPart().getChildren().get(0)).getModel();

			LayoutCommand command = new LayoutCommand(main, root, new EdgeFixLayout());
			command.execute();

			viewer = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}