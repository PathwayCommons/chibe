package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.patika.mada.gui.ExperimentDataManagementDialog;

import java.util.Set;
import java.util.ArrayList;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class DataManagementAction extends Action
{
	private ChisioMain main;

	private String type;

	public DataManagementAction(ChisioMain main)
	{
		super("Data Selection ...");
		setImageDescriptor(ImageDescriptor.createFromFile(
                ChisioMain.class, "icon/data-selection.png"));
		setToolTipText(getText());
		this.main = main;
	}

	public void run()
	{
		if (type == null)
		{
			Set<String> types = main.getLoadedExperimentTypes();

			if (types.isEmpty())
			{
				MessageDialog.openError(main.getShell(),
					"Error!", "Load experiment data first.");

				return;
			}

			if (types.size() == 1)
			{
				type = types.iterator().next();
			}
			else
			{
				ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(),
					200,
					"Experiment Type Selection Dialog",
					"Select experiment type",
					new ArrayList<String>(types),
					new ArrayList<String>(),
					false,
					true,
					null);

				dialog.setMinValidSelect(1);
				Object selected = dialog.open();
				if (selected != null) type = selected.toString();
				

				if (dialog.isCancelled()) type = null;
			}
		}

		if (type != null)
		{
			ExperimentDataManagementDialog dialog = new ExperimentDataManagementDialog(
				main, main.getExperimentDataManager(type));

			dialog.open();

			// We do not want to remember this the next time
			type = null;
		}
	}
}
