package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class CloseBioPAXFileAction extends Action
{
	ChisioMain main;

	public CloseBioPAXFileAction(ChisioMain chisio)
	{
		super("Close");
		this.setToolTipText(this.getText());
		this.main = chisio;
	}

	public void run()
	{
		if (LoadBioPaxModelAction.saveChangesBeforeDiscard(main))
		{
			main.closeAllTabs(false);
			main.setOwlFileName(null);
			main.setBioPAXModel(null);
			ChisioMain.updateCombo("100%");
		}
	}
}