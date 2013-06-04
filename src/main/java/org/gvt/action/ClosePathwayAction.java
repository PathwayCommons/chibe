package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.custom.CTabItem;
import org.gvt.ChisioMain;

/**
 * Action for closing the currently open tab.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ClosePathwayAction extends Action
{
	private ChisioMain main;
	private boolean allOpenPathways;

	/**
	 * Constructor
	 */
	public ClosePathwayAction(ChisioMain main, boolean allOpenPathways)
	{
		super(allOpenPathways ? "Close All Open Pathways" : "Close Pathway");

		if (allOpenPathways)
		{
			setImageDescriptor(ImageDescriptor.createFromFile(
                    ChisioMain.class, "icon/pathway-close-all.png"));
		}
		else
		{
			setImageDescriptor(ImageDescriptor.createFromFile(
                    ChisioMain.class, "icon/pathway-close.png"));
		}

		this.setToolTipText(this.getText());
		this.main = main;
		this.allOpenPathways = allOpenPathways;
	}

	public ClosePathwayAction(ChisioMain main)
	{
		this(main, false);
	}

	public void run()
	{
		if (allOpenPathways)
		{
			main.closeAllTabs(true);
		}
		else
		{
			CTabItem tab = main.getSelectedTab();

			if (tab != null)
			{
				main.closeTab(tab, true);
			}
		}
	}
}