package org.gvt.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.util.NeighborhoodOptionsPack;

/**
 * This class maintains Neighborhood Query Dialog for PopupMenu
 * 
 * @author Ozgun Babur
 * @author Merve Cakir
 * @author Shatlyk Ashyralyev
 * 
 *         Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class NeighborhoodQueryParamDialog extends AbstractQueryParamWithStreamDialog
{
	/**
	 * Create the dialog
	 */
	public NeighborhoodQueryParamDialog(ChisioMain main)
	{
		super(main);
	}

	/**
	 * Open the dialog
	 */
	public NeighborhoodOptionsPack open(NeighborhoodOptionsPack opt)
	{
		createContents(opt);

		shell.setLocation(getParent().getLocation().x
			+ (getParent().getSize().x / 2) - (shell.getSize().x / 2),
			getParent().getLocation().y + (getParent().getSize().y / 2)
				- (shell.getSize().y / 2));

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
	 * Create contents of the dialog Buttons, Text Field, Radio Buttons, etc
	 */
	protected void createContents(final NeighborhoodOptionsPack opt)
	{
		super.createContents(opt);
		shell.setText("Neighborhood Query Properties");

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

		// Group for downstreamButton, upstreamButton and bothBotton
		createStreamDirectionGroup(2, 3, true);

		//Length Limit Label and Text
		createLengthLimit(1, 1, 1, 1, 50);

		// Group for execute, cancel and default buttons

		exeCancelDefaultGroup = new Group(shell, SWT.NONE);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
		gridData.horizontalSpan = 4;
		exeCancelDefaultGroup.setLayoutData(gridData);
		exeCancelDefaultGroup.setLayout(new GridLayout(3, true));

		// Execute Button

		executeButton = new Button(exeCancelDefaultGroup, SWT.NONE);
		executeButton.setText("Execute");
		gridData = new GridData(GridData.END, GridData.CENTER, true, false);
		executeButton.setLayoutData(gridData);
		executeButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				// store values in dialog to optionsPack
				storeValuesToOptionsPack(opt);

				// ok is selected
				opt.setCancel(false);

				shell.close();
			}
		});

		// Cancel Button

		cancelButton = new Button(exeCancelDefaultGroup, SWT.NONE);
		gridData = new GridData(GridData.CENTER, GridData.CENTER, true, false);
		createCancelButton(gridData);

		// Default Button

		defaultButton = new Button(exeCancelDefaultGroup, SWT.NONE);
		defaultButton.setText("Default");
		gridData =
			new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
		defaultButton.setLayoutData(gridData);
		defaultButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				// set default values of dialog
				setDefaultQueryDialogOptions();
			}
		});

		// pack dialog
		shell.pack();

		// set initial values from opt OptionsPack
		setInitialValues(opt);
	}

	/**
	 * After clicking OK button, all data in dialog is saved to
	 * NeighborhoodOptionsPack
	 */
	public void storeValuesToOptionsPack(NeighborhoodOptionsPack opt)
	{
		// store Length Limit
		opt.setLengthLimit(Integer.parseInt(lengthLimit.getText()));

		// if downstream is selected
		if (downstreamButton.getSelection())
		{
			opt.setDownstream(true);
			opt.setUpstream(false);
		}
		// if upstream is selected
		else if (upstreamButton.getSelection())
		{
			opt.setDownstream(false);
			opt.setUpstream(true);
		}
		// if both is selected
		else
		{
			opt.setDownstream(true);
			opt.setUpstream(true);
		}

		// if currentView is selected
		if (currentViewButton.getSelection())
		{
			opt.setCurrentView(true);
		}
		// if newView is selected
		else
		{
			opt.setCurrentView(false);
		}
	}

	/**
	 * After creating the dialog box, fields are completed with data in opt
	 * OptionsPack
	 */
	public void setInitialValues(NeighborhoodOptionsPack opt)
	{
		super.setInitialValues(opt);

		// Downstream, Upstream or Both

		if (opt.isDownstream() && opt.isUpstream())
		{
			bothBotton.setSelection(true);
		}
		else if (opt.isDownstream())
		{
			downstreamButton.setSelection(true);
		}
		else if (opt.isUpstream())
		{
			upstreamButton.setSelection(true);
		}
	}

	/**
	 * Set default values into dialog
	 */
	public void setDefaultQueryDialogOptions()
	{
		super.setDefaultQueryDialogOptions();

		bothBotton.setSelection(DOWNSTREAM && UPSTREAM);
		downstreamButton.setSelection(DOWNSTREAM && !UPSTREAM);
		upstreamButton.setSelection(!DOWNSTREAM && UPSTREAM);

	}
}