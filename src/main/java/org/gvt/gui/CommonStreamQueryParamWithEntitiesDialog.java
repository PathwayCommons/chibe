package org.gvt.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.gvt.ChisioMain;
import org.gvt.util.EntityHolder;
import org.gvt.util.QueryOptionsPack;

import java.util.ArrayList;

/**
 * This class maintains Common Stream Query Dialog for TopMenuBar
 *
 * @author Shatlyk Ashyralyev
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class CommonStreamQueryParamWithEntitiesDialog extends AbstractQueryParamDialog
{
	/**
	 * All entities of graph
	 */
	ArrayList<EntityHolder> allEntities;

	/**
	 * Create the dialog
	 */
	public CommonStreamQueryParamWithEntitiesDialog(ChisioMain main,
		ArrayList<EntityHolder> allEntities)
	{
		super(main);
		this.allEntities = allEntities;
	}

	/**
	 * Create contents of the dialog.
	 * Buttons, List, Text Field, Radio Buttons, etc
	 */
	protected void createContents(final QueryOptionsPack opt)
	{
		super.createContents(opt);
		shell.setText("Common Stream Query Properties");

		//Set Image
		ImageDescriptor id = ImageDescriptor.createFromFile(
			CommonStreamQueryParamWithEntitiesDialog.class,
			"/src/main/resources/org/gvt/icon/cbe-icon.png");
		shell.setImage(id.createImage());

		//layout of shell contains 6 columns

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		shell.setLayout(gridLayout);

		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.verticalSpan = 5;
		gridData.horizontalSpan = 1;
		gridData.widthHint = 150;

		//Entity list
		if (allEntities != null)
		{
			sourceElg = new EntityListGroup(shell, SWT.NONE, allEntities);
			sourceElg.init();
			sourceElg.setLayoutData(gridData);
		}
		else
		{
			sourceST = new SymbolText(shell, SWT.NONE);
			sourceST.init(null);
			sourceST.setLayoutData(gridData);
		}

		//Group for currentViewButton and newViewButton
		createResultViewGroup(2, 2);

		//Group for downstreamButton, upstreamButton and bothButton
		createStreamDirectionGroup(2, 2, false);

		//Length Limit Label and Text
		createLengthLimit(1, 1, 1, 1, 50);

		// Group for execute, cancel and default buttons
		createExeCancDefGroup(opt, 6);

		//pack dialog
		shell.pack();

		//set initial values from opt OptionsPack
		setInitialValues(opt);
	}
}