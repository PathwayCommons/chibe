package org.gvt.action;

import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.CTabItem;
import org.gvt.ChisioMain;
import org.gvt.gui.StringInputDialog;

/**
 * Action for closing the currently open tab.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class RenamePathwayAction extends Action
{
	private ChisioMain main;

	/**
	 * Constructor
	 */
	public RenamePathwayAction(ChisioMain main)
	{
		super("Rename Pathway ...");
		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class,
			"icon/pathway-rename.png"));
		this.main = main;
	}

	public void run()
	{
		CTabItem tab = main.getSelectedTab();
		ScrollingGraphicalViewer viewer = main.getViewer();

		if (viewer != null)
		{
			String newname;

			boolean again = false;

			do
			{
				if (again)
					MessageDialog.openError(main.getShell(), "Error",
						"Name already exists. Choose another name.");
				
				again  = true;

				StringInputDialog dialog = new StringInputDialog(main.getShell(), "Rename Pathway",
					"Enter new name", tab.getText());

				dialog.setSelectText(true);

				newname = dialog.open();
			}
			while(newname != null && !newname.equals(tab.getText()) &&
				(main.getAllPathwayNames().contains(newname) ||
				main.getOpenTabNames().contains(newname)));

			if (newname != null && !newname.equals(tab.getText()))
			{
				main.renamePathway(tab, newname);
			}
		}
	}
}