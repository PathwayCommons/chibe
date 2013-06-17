package org.gvt.action;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.Tool;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.MarqueeZoomTool;

/**
 * Tool to select and manipulate figures. A selection tool is in one of three
 * states, e.g., background selection, figure selection, handle manipulation.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class MarqueeZoomToolAction extends AbstractGEFToolAction
{
	public MarqueeZoomToolAction(String text, ChisioMain main)
	{
		super(text, main);
		setImageDescriptor(ImageDescriptor.createFromFile(
			ChisioMain.class, "icon/zoom.png"));
	}

	protected Tool createTool()
	{
		MarqueeZoomTool tool = new MarqueeZoomTool()
		{
			public void activate()
			{
				setChecked(true);
				super.activate();
			}

			public void deactivate()
			{
				setChecked(false);
				super.deactivate();
			}
		};

		return tool;
	}
}