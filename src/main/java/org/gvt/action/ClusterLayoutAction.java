package org.gvt.action;

import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.command.LayoutCommand;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.model.CompoundModel;
import org.ivis.layout.cluster.ClusterLayout;

/** Action for Cluster layout operation.
 *
 * @author Cihan Kucukkececi
 * @author Selcuk Onur Sumer (modified by)
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ClusterLayoutAction extends Action
{
	ChisioMain main = null;

	/**
	 * Constructor
	 */
	public ClusterLayoutAction(ChisioMain main)
	{
		super("Cluster Layout");
		setToolTipText("Cluster Layout");
		setImageDescriptor(ImageDescriptor.createFromFile(
			ChisioMain.class,
			"icon/layout-cluster.gif"));
		this.main = main;
	}

	public void run()
	{
		CompoundModel root = (CompoundModel)((ChsRootEditPart) main.getViewer().
				getRootEditPart().getChildren().get(0)).getModel();

		LayoutCommand command = new LayoutCommand(main,
			root,
			new ClusterLayout());
		
		command.execute();
	}
}