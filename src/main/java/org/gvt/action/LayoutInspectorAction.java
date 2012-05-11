package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.LayoutInspector;

/**
 * This class creates the action for opening layout properties window.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class LayoutInspectorAction extends Action
{
	ChisioMain main;

	/**
	 * Constructor
	 */
	public LayoutInspectorAction(ChisioMain main)
	{
		super("Layout Properties ...");
		this.setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "../icon/layout-inspector.png"));
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		new LayoutInspector(main).open();
	}
}
