package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.command.LayoutCommand;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.layout.SpringLayout;
import org.gvt.model.CompoundModel;

/**
 * Action for Spring Layout operation.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class SpringLayoutAction extends Action
{
	ChisioMain main = null;

	/**
	 * Constructor
	 */
	public SpringLayoutAction(ChisioMain main)
	{
		setText("Spring Layout");
		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(
			getClass(), "../icon/layout-spring.gif"));
		this.main = main;

	}

	public void run()
	{
		try
		{
			main.lockWithMessage("Performing layout ...");
			CompoundModel root = (CompoundModel)((ChsRootEditPart) main.getViewer().
					getRootEditPart().getChildren().get(0)).getModel();

			LayoutCommand command = new LayoutCommand(main, new SpringLayout(root));
			command.execute();
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