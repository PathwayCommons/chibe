package org.gvt.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.gvt.ChisioMain;
import org.gvt.util.QueryOptionsPack;

/**
 * This class maintains Neighborhood Query Dialog for PopupMenu
 * 
 * @author Ozgun Babur
 * @author Merve Cakir
 * @author Shatlyk Ashyralyev
 * 
 *         Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class NeighborhoodQueryParamDialog extends AbstractQueryParamDialog
{
	/**
	 * Create the dialog
	 */
	public NeighborhoodQueryParamDialog(ChisioMain main)
	{
		super(main);
	}

	/**
	 * Create contents of the dialog Buttons, Text Field, Radio Buttons, etc
	 */
	protected void createContents(final QueryOptionsPack opt)
	{
		super.createContents(opt);
		shell.setText("Neighborhood Query Properties");
        infoLabel.setText(NeighborhoodQueryParamWithEntitiesDialog.info);

		// Set Image
		ImageDescriptor id = 
			ImageDescriptor.createFromFile(
				NeighborhoodQueryParamWithEntitiesDialog.class,
				"/src/main/resources/org/gvt/icon/cbe-icon.png");
		shell.setImage(id.createImage());

		// layout of shell contains 4 columns

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		shell.setLayout(gridLayout);

		GridData gridData;

		// Group for currentViewButton and newViewButton
		createResultViewGroup(2, 2);

		// Group for downstreamButton, upstreamButton and bothButton
		createStreamDirectionGroup(2, 3, true);

		//Length Limit Label and Text
		createLengthLimit(1, 1, 1, 1, 50);

		createExeCancDefGroup(opt, 4);

		// pack dialog
		shell.pack();

		// set initial values from opt OptionsPack
		setInitialValues(opt);
	}
}