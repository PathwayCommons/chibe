package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.gvt.ChisioMain;
import org.patika.mada.gui.FetchFromGEODialog;
import org.patika.mada.gui.ExperimentDataAutomaticConvertionWizard;
import org.patika.mada.util.XRef;

import java.util.ArrayList;

/**
 * This class creates the action for downloading microarray data from GEO database
 * and converting into .ced files automatically .
 *
 * @author Merve Cakir
 */
public class FetchFromGEOAction extends Action
{
	ChisioMain main;

	/**
	 * Constructor
	 * @param main
	 */
	public FetchFromGEOAction (ChisioMain main)
	{
		super("Fetch from GEO ...");
		this.main = main;
	}

	public void run()
	{
		FetchFromGEODialog dialog = new FetchFromGEODialog(main);
		dialog.open();

		String filename = null;

		if(dialog.isFetchPressed())
		{
			// if .ced file is not present, generate it using series matrix
			// and platform files

			if(!dialog.isCedPresent())
			{
				main.lockWithMessage("Generating experiment data ...");
				ExperimentDataAutomaticConvertionWizard edacw = new ExperimentDataAutomaticConvertionWizard(
						new ArrayList<String>(XRef.getDBSet()),
						dialog.getSeriesMatrixFile(),
						dialog.getPlatformFile(),
						dialog.getSelectedSeries());
				edacw.run();

				filename = edacw.getResultFileName();
				main.unlock();
			}
			// if .ced file is already present, directly load it.
			else if(dialog.isCedPresent())
			{
				filename = dialog.getCedFile().getPath();
			}
		}

		if (filename != null)
		{
			new LoadExperimentDataAction(main, filename).run();
		}
	}
}
