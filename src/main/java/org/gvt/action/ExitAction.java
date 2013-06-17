package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;

/**
 * This class maintains the exit action to exit the program.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ExitAction extends Action
{
	ChisioMain main;

	public ExitAction(String text, ChisioMain chisio)
	{
		super(text);
		this.main = chisio;
	}

	public void run()
	{
		if (LoadBioPaxModelAction.saveChangesBeforeDiscard(main))
		{
			main.close();
		}
	}
}