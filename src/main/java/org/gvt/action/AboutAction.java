package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
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
        ImageDescriptor id = ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbe-icon.png");
		setImageDescriptor(ImageDescriptor.createFromImageData(id.getImageData().scaledTo(16, 16)));
		this.main = main;
	}

	public void run()
	{
		new AboutDialog(main.getShell()).open();
	}
}