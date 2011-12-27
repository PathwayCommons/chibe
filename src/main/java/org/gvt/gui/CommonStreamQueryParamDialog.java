package org.gvt.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
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
	 * Open the dialog
	 */
	public QueryOptionsPack open(QueryOptionsPack opt)
	{
		createContents(opt);

		shell.setLocation(
			getParent().getLocation().x + (getParent().getSize().x / 2) -
			(shell.getSize().x / 2),
			getParent().getLocation().y + (getParent().getSize().y / 2) -
			(shell.getSize().y / 2));

		shell.open();

		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		return opt;
	}

	/**
	 * Create contents of the dialog
	 * Buttons, Text Field, Radio Buttons, etc
	 */
	protected void createContents(final QueryOptionsPack opt)
	{
		super.createContents(opt);
		shell.setText("Common Stream Query Properties");

		//Set Image
		ImageDescriptor id = ImageDescriptor.createFromFile(
			NeighborhoodQueryParamWithEntitiesDialog.class, "/src/main/resources/org/gvt/icon/cbe-icon.png");
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