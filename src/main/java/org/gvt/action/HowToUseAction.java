package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;
import org.gvt.util.SystemBrowserDisplay;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class HowToUseAction extends Action
{
	ChisioMain main;

	public HowToUseAction(ChisioMain main)
	{
		super("How To Use");

		this.main = main;
	}

	public void run()
	{
		SystemBrowserDisplay.openURL(
			"http://www.cs.bilkent.edu.tr/~ivis/chibe/ChiBE-2.1.UG.pdf");
	}
}