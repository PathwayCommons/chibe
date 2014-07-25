package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.RPPAWizard;

/**
 * @author Ozgun Babur
 */
public class LoadRPPAAction extends Action
{
	ChisioMain main;

	public LoadRPPAAction(ChisioMain main)
	{
		super("Load RPPA Data ...");
		this.main = main;
	}

	public void run()
	{
		// Load RPPA data

		WizardDialog dialog = new WizardDialog(main.getShell(), new RPPAWizard());
		dialog.open();

		// Prepare network using the data


	}
}