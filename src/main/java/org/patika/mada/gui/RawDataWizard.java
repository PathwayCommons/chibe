package org.patika.mada.gui;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Ozgun Babur
 */
public class RawDataWizard extends Wizard
{

	public RawDataWizard()
	{
		setWindowTitle("Raw Data Loading Wizard");
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
	}

	public boolean performFinish()
	{
		return true;
	}

	public void addPages()
	{
		addPage(new ExperimentInfoPage());
	}

	public static int open(Shell parent)
	{
		WizardDialog dialog = new WizardDialog(parent, new RawDataWizard());
        dialog.setBlockOnOpen(true);
		dialog.updateSize();
        return dialog.open();
	}
}
