package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.AboutDialog;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class AboutAction extends Action
{
	ChisioMain main;

	public AboutAction(ChisioMain main)
	{
		setText("About Chisio BioPAX Editor");
		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(
			getClass(), "../icon/cbe-icon.png"));
		this.main = main;
	}

	public void run()
	{
		new AboutDialog(main.getShell()).open();
	}
}