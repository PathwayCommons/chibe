package org.gvt.gui;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.util.QueryOptionsPack;

/**
 * This class maintains Common Stream Query Dialog for PopupMenu
 *
 * @author Shatlyk Ashyralyev
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class CommonStreamQueryParamDialog extends AbstractQueryParamDialog
{
	/**
	 * Create the dialog
	 */
	public CommonStreamQueryParamDialog(ChisioMain main)
	{
		super(main);
	}

	/**
	 * Create contents of the dialog
	 * Buttons, Text Field, Radio Buttons, etc
	 */
	protected void createContents(final QueryOptionsPack opt)
	{
		super.createContents(opt);
		shell.setText("Common Stream Query Properties");
        infoLabel.setText(CommonStreamQueryParamWithEntitiesDialog.info);

		//Set Image
		ImageDescriptor id = ImageDescriptor.createFromFile(
			ChisioMain.class, "icon/cbe-icon.png");
		shell.setImage(id.createImage());

		//layout of shell contains 4 columns
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		shell.setLayout(gridLayout);		

		//Group for currentViewButton and newViewButton
		createResultViewGroup(2, 2);

		//Group for downstreamButton, upstreamButton and bothButton
		createStreamDirectionGroup(2, 2, false);

		//Length Limit Label and Text
		createLengthLimit(1, 1, 2, 1, 50);

		// Group for execute, cancel and default buttons
		createExeCancDefGroup(opt, 4);

		//pack dialog
		shell.pack();

		//set initial values from opt OptionsPack
		setInitialValues(opt);
	}
}