package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.patika.mada.gui.ExperimentDataConvertionWizard;
import org.patika.mada.gui.RawDataWizard;
import org.patika.mada.util.XRef;

import java.awt.*;
import java.util.ArrayList;

/**
 * Displays experiment data conversion wizard, and loads the .ced file which is the result of
 * converison.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class LoadRawExpDataAction extends Action
{
	ChisioMain main;

	public LoadRawExpDataAction(ChisioMain main)
	{
		super("Load Raw Data ...");
		setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "../icon/open.png"));
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		if (false)
		{
			RawDataWizard.open(main.getShell());
			return;
		}

		ExperimentDataConvertionWizard edcw = new ExperimentDataConvertionWizard(
			new ArrayList<String>(XRef.getDBSet()), new Point(200, 200));

		edcw.setVisible(true);

		String filename = edcw.getResultFileName();

		if (filename != null)
		{
			new LoadExperimentDataAction(main, filename).run();
		}
	}
}