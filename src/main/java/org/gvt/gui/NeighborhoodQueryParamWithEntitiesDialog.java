package org.gvt.gui;

import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.util.QueryOptionsPack;
import org.gvt.util.EntityHolder;

/**
 * This class maintains Neighborhood Query Dialog for TopMenuBar
 *
 * @author Ozgun Babur
 * @author Merve Cakir
 * @author Shatlyk Ashyralyev
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class NeighborhoodQueryParamWithEntitiesDialog extends AbstractQueryParamDialog
{
	/**
	 * All entities of graph
	 */
	Collection<EntityHolder> allEntities;

	/**
	 * Create the dialog
	 */
	public NeighborhoodQueryParamWithEntitiesDialog(ChisioMain main,
		Collection<EntityHolder> allEntities)
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
		shell.setText("Neighborhood Query Properties");
        infoLabel.setText(info);

		//Set Image
		ImageDescriptor id = ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbe-icon.png");
		shell.setImage(id.createImage());

		//layout of shell contains 6 columns

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		shell.setLayout(gridLayout);

		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.verticalSpan = 5;
		gridData.horizontalSpan = 1;
		gridData.widthHint = 300;

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
		createStreamDirectionGroup(2, 3, true);

		//Length Limit Label and Text
		createLengthLimit(1, 1, 1, 1, 50);

		createExeCancDefGroup(opt, 6);

		// Disable features that are not currently supported
		if (allEntities == null)
		{
			currentViewButton.setEnabled(false);
		}

		//pack dialog
		shell.pack();

		//set initial values from opt OptionsPack
		setInitialValues(opt);
	}

    public static String info = "Find the neighbors of an entity within a certain distance";
}