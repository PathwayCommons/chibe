package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.LayoutInspector;
import org.ivis.layout.LayoutOptionsPack;

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
	private static LayoutInspector layoutInspector = null;

	/**
	 * Constructor
	 */
	public LayoutInspectorAction(ChisioMain main)
	{
		super("Layout Properties ...");
		this.setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/layout-inspector.png"));
		setToolTipText(getText());
		this.main = main;
	}

	public LayoutInspector getLayoutInspector()
	{
		if (LayoutInspectorAction.layoutInspector == null)
		{
			LayoutInspectorAction.layoutInspector = new LayoutInspector(this.main);

			// Make any changes to default layout options here
			LayoutOptionsPack layoutOptionsPack = LayoutOptionsPack.getInstance();

			// CoSE
			layoutOptionsPack.getCoSE().idealEdgeLength = 30;
			layoutOptionsPack.getCoSE().defaultIdealEdgeLength = 30;
		}

		return LayoutInspectorAction.layoutInspector;
	}

	public void run()
	{
		this.getLayoutInspector().open();
	}
}
