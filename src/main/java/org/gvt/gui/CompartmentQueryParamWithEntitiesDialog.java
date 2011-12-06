package org.gvt.gui;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.model.biopaxl2.Compartment;
import org.gvt.model.GraphObject;
import org.gvt.ChisioMain;
import org.gvt.util.PoIOptionsPack;
import java.util.ArrayList;
import java.util.Set;

/**
 * This class maintains Compartment Query Dialog for TopMenuBar. As it possesses
 * exactly same parameters with PoI query, PoI Options Pack will be used.
 *
 * @author Merve Cakir
 */
public class CompartmentQueryParamWithEntitiesDialog extends AbstractQueryParamDialog
{
	/**
	* To handle addition and removal of source compartments. Buttons and list
	* from the super class is used for source list.
	*/
	private Label sourceLabel;

	/**
	* To handle addition and removal of target compartments.
	*/
	private Label targetLabel;
	private List targetCompartmentList;
	private Button targetAddButton;
	private Button targetRemoveButton;

	/**
	* Type and value of stop distance.
	*/
	private Group limitTypeGroup;
	private Button lengthLimitButton;
	private Button shortestPlusKButton;
	private Text shortestPlusK;

	/**
	* Type of PoI; strict or not.
	*/
	private Button strictButton;

	/**
	 * All compartments of graph
	 */
	ArrayList<Compartment> allCompartments;

	/**
	 * Compartments which are added to source list and to target list.
	 */
	ArrayList<Compartment> sourceAddedCompartments;
	ArrayList<Compartment> targetAddedCompartments;

	/**
	 * Getters
	 */

	public ArrayList<Compartment> getSourceAddedCompartments()
	{
		return this.sourceAddedCompartments;
	}

	public ArrayList<Compartment> getTargetAddedCompartments()
	{
		return this.targetAddedCompartments;
	}

	/**
	 * Constructor
	 */
	public CompartmentQueryParamWithEntitiesDialog(ChisioMain main)
	{
		super(main);

		this.allCompartments = new ArrayList<Compartment>();
		// Selecting compartments from whole node set
		
		Set<GraphObject> candidate = main.getRootGraph().getNodes();
		for (GraphObject go : candidate)
		{
			if (go instanceof Compartment)
			{
				allCompartments.add((Compartment)go);
			}
		}

		this.sourceAddedCompartments = new ArrayList<Compartment>();
		this.targetAddedCompartments = new ArrayList<Compartment>();
	}

	/**
	 * Open the dialog
	 */
	public PoIOptionsPack open(PoIOptionsPack opt)
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
	 * Create contents of the dialog.
	 * Buttons, List, Text Field, Radio Buttons, etc.
	 */
	protected void createContents(final PoIOptionsPack opt)
	{
		super.createContents(opt);
		shell.setText("Compartment Query Properties");

		//Set Image
		ImageDescriptor id = ImageDescriptor.createFromFile(
			CompartmentQueryParamWithEntitiesDialog.class,
			"/src/main/resources/org/gvt/icon/cbe-icon.png");
		shell.setImage(id.createImage());

		//layout of shell will contain 8 columns.

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 8;
		shell.setLayout(gridLayout);

		//source compartment list's label

		sourceLabel = new Label(shell, SWT.NONE);
		sourceLabel.setText("Source");
		GridData gridData = new GridData(GridData.CENTER, GridData.END,
			false, false);
		gridData.horizontalSpan = 2;
		sourceLabel.setLayoutData(gridData);

		//target compartment list's label

		targetLabel = new Label(shell, SWT.NONE);
		targetLabel.setText("Target");
		gridData = new GridData(GridData.CENTER, GridData.END, false, false);
		gridData.horizontalSpan = 2;
		targetLabel.setLayoutData(gridData);

		//Group for currentViewButton and newViewButton
		createResultViewGroup(2, 2);

		//Group for lengthLimitButton and shortestPlusKButton

		limitTypeGroup = new Group(shell, SWT.NONE);
		limitTypeGroup.setText("Stop distance");
		gridData = new GridData(GridData.FILL, GridData.BEGINNING, false, false);
		gridData.horizontalSpan = 2;
		gridData.verticalSpan = 2;
		limitTypeGroup.setLayoutData(gridData);
		limitTypeGroup.setLayout(new GridLayout(2, true));

		//Length limit radio button

		lengthLimitButton = new Button(limitTypeGroup, SWT.RADIO);
		lengthLimitButton.setText("Length limit");
		gridData = new GridData(GridData.BEGINNING, GridData.CENTER,
			false, false);
		lengthLimitButton.setLayoutData(gridData);

		//Length limit text

		lengthLimit = new Text(limitTypeGroup, SWT.BORDER);
		lengthLimit.addKeyListener(keyAdapter);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
		lengthLimit.setLayoutData(gridData);

		//Shortest+k radio button

		shortestPlusKButton = new Button(limitTypeGroup, SWT.RADIO);
		shortestPlusKButton.setText("Shortest+k");
		gridData = new GridData(GridData.BEGINNING, GridData.CENTER,
			false, false);
		shortestPlusKButton.setLayoutData(gridData);

		//Shortest+k text

		shortestPlusK = new Text(limitTypeGroup, SWT.BORDER);
		shortestPlusK.addKeyListener(keyAdapter);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
		shortestPlusK.setLayoutData(gridData);

		//Source compartment list
		createList(2, 2, 150, 5);

		//Target compartment list

		targetCompartmentList = new List(shell,
			SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.HORIZONTAL);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.verticalSpan = 2;
		gridData.horizontalSpan = 2;
		gridData.heightHint = targetCompartmentList.getItemHeight() * 5;
		gridData.widthHint = 150;
		targetCompartmentList.setLayoutData(gridData);

		//Strict check box

		strictButton = new Button(shell, SWT.CHECK | SWT.WRAP);
		strictButton.setText("Ignore source-source/target-target paths");
		gridData = new GridData(GridData.CENTER, GridData.CENTER, false, false);
		gridData.verticalSpan = 2;
		gridData.horizontalSpan = 4;
		strictButton.setLayoutData(gridData);

		//Source add button

		addButton = new Button(shell, SWT.NONE);
		addButton.setText("Add...");
		gridData = new GridData(GridData.END, GridData.BEGINNING, true, false);
		gridData.minimumWidth = 100;
		gridData.horizontalIndent = 5;
		addButton.setLayoutData(gridData);
		addButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				//new addCompartmentDialog
				AddCompartmentDialog addCompartment =
					new AddCompartmentDialog(new Shell(), allCompartments);

				//open dialog
				boolean addPressed = addCompartment.open();

				//if add button is pressed
				if (addPressed)
				{
					//for each selected compartment
					for (Compartment compartment :
						addCompartment.getSelectedCompartments())
					{
						//check if compartment has been added before
						if (!previouslyAdded(compartment, sourceAddedCompartments))
						{
							//add compartment name to source compartment list
							entityList.add(compartment.getName());

							//add compartment to sourceAddedCompartments ArrayList
							sourceAddedCompartments.add(compartment);
						}
					}
				}
			}
		});

		//Source remove button

		removeButton = new Button(shell, SWT.NONE);
		removeButton.setText("Remove");
		gridData = new GridData(GridData.BEGINNING, GridData.BEGINNING,
			true, false);
		gridData.horizontalIndent = 5;
		gridData.minimumWidth = 100;
		removeButton.setLayoutData(gridData);
		removeButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				String[] selectionResult = entityList.getSelection();

				//for each selected string
				for (String selected : selectionResult)
				{
					//search among all sourceAddedCompartments
					for (int j = 0 ; j < sourceAddedCompartments.size() ; j++)
					{
						Compartment compartment = sourceAddedCompartments.get(j);

						//if corresponding compartment is found
						if (selected != null &&
							selected.equals(compartment.getName()))
						{
							//remove compartment from sourceAddedCompartments ArrayList
							sourceAddedCompartments.remove(j);

							//remove compartment name from source compartment list
							entityList.remove(selected);
						}
					}
			   }
			}
		});

		//Target add button

		targetAddButton = new Button(shell, SWT.NONE);
		targetAddButton.setText("Add...");
		gridData = new GridData(GridData.END, GridData.BEGINNING, true, false);
		gridData.minimumWidth = 100;
		gridData.horizontalIndent = 5;
		targetAddButton.setLayoutData(gridData);
		targetAddButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				//new addCompartmentDialog
				AddCompartmentDialog addCompartment =
					new AddCompartmentDialog(new Shell(), allCompartments);

				//open dialog
				boolean addPressed = addCompartment.open();

				//if add button is pressed
				if (addPressed)
				{
					//for each selected compartment
					for (Compartment compartment :
						addCompartment.getSelectedCompartments())
					{
						//check if compartment has been added before
						if (!previouslyAdded(compartment, targetAddedCompartments))
						{
							//add compartment name to target compartment list
							targetCompartmentList.add(compartment.getName());

							//add compartment to targetAddedCompartments ArrayList
							targetAddedCompartments.add(compartment);
						}
					}
				}
			}
		});

		//Target remove button

		targetRemoveButton = new Button(shell, SWT.NONE);
		targetRemoveButton.setText("Remove");
		gridData = new GridData(GridData.BEGINNING, GridData.BEGINNING,
			true, false);
		gridData.horizontalIndent = 5;
		gridData.minimumWidth = 100;
		targetRemoveButton.setLayoutData(gridData);
		targetRemoveButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				String[] selectionResult = targetCompartmentList.getSelection();

				//for each selected string
				for (String selected : selectionResult)
				{
					//search among all targetAddedCompartments
					for (int j = 0 ; j < targetAddedCompartments.size() ; j++)
					{
						Compartment compartment = targetAddedCompartments.get(j);

						//if corresponding compartment is found
						if (selected != null &&
							selected.equals(compartment.getName()))
						{
							//remove compartment from targetAddedCompartments ArrayList
							targetAddedCompartments.remove(j);

							//remove compartment name from target compartment list
							targetCompartmentList.remove(selected);
						}
					}
			   }
			}
		});

		// Group for execute, cancel and default buttons

		exeCancelDefaultGroup = new Group(shell, SWT.NONE);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
		gridData.horizontalSpan = 8;
		exeCancelDefaultGroup.setLayoutData(gridData);
		exeCancelDefaultGroup.setLayout(new GridLayout(3, true));

		//Execute button

		executeButton = new Button(exeCancelDefaultGroup, SWT.NONE);
		executeButton.setText("Execute");
		gridData = new GridData(GridData.END, GridData.CENTER, true, false);
		executeButton.setLayoutData(gridData);
		executeButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				//if no source compartment is added, show error
				if (getSourceAddedCompartments().isEmpty())
				{
					MessageDialog.openError(main.getShell(), "Error!",
					"Add Source Compartment!");

					return;
				}

				//if no target compartment is added, show error
				if (getTargetAddedCompartments().isEmpty())
				{
					MessageDialog.openError(main.getShell(), "Error!",
					"Add Target Compartment!");

					return;
				}

				//store values in dialog to PoIOptionsPack
				storeValuesToOptionsPack(opt);

				//execute is selected
				opt.setCancel(false);

				shell.close();
			}
		});

		//Cancel button

		cancelButton = new Button(exeCancelDefaultGroup, SWT.NONE);
		gridData = new GridData(GridData.CENTER, GridData.CENTER, true, false);
		createCancelButton(gridData);

		//Default Button

		defaultButton = new Button(exeCancelDefaultGroup, SWT.NONE);
		defaultButton.setText("Default");
		gridData =
			new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
		defaultButton.setLayoutData(gridData);
		defaultButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				//set default values of dialog
				setDefaultQueryDialogOptions();
			}
		});

		//pack dialog
		shell.pack();

		//set initial values from opt OptionsPack
		setInitialValues(opt);
	}

	/**
	 * Saves all data in dialog to PoIOptionsPack
	 */
	public void storeValuesToOptionsPack(PoIOptionsPack opt)
	{
		//store stop distance according to user's selection
        if (lengthLimitButton.getSelection())
        {
            opt.setLengthLimit(Integer.parseInt(lengthLimit.getText()));
            opt.setLimitType(true);
        }
        else if (shortestPlusKButton.getSelection())
        {
            opt.setShortestPlusKLimit(Integer.parseInt(shortestPlusK.getText()));
            opt.setLimitType(false);
        }

		//if currentView is selected
		if (currentViewButton.getSelection())
		{
			opt.setCurrentView(true);
		}
		//if newView is selected
		else
		{
			opt.setCurrentView(false);
		}

        //if strict is selected.
        if (strictButton.getSelection())
        {
            opt.setStrict(true);
        }
        else
        {
            opt.setStrict(false);
        }
    }

	/**
	 * After creating the dialog box, fields are completed with data in
     * opt OptionsPack.
	 */
	public void setInitialValues(PoIOptionsPack opt)
	{
		super.setInitialValues(opt);

		// Strict
        if (opt.isStrict())
        {
            strictButton.setSelection(true);
        }

        // Set shortestPlusKText's value
        shortestPlusK.setText(String.valueOf(opt.getShortestPlusKLimit()));

        //Length limit or shortest+k

        if (opt.getLimitType())
        {
            lengthLimitButton.setSelection(true);
        }
        else
        {
            shortestPlusKButton.setSelection(true);
        }
	}

	/**
	 * Set default values into dialog
	 */
	public void setDefaultQueryDialogOptions()
	{
		super.setDefaultQueryDialogOptions();

        shortestPlusK.setText(String.valueOf(DEFAULT_SHORTEST_PLUS_K));
        lengthLimitButton.setSelection(LIMIT_TYPE);
        shortestPlusKButton.setSelection(!LIMIT_TYPE);

        strictButton.setSelection(STRICT);
	}

	/**
	 * This method checks whether compartment is added before to the compartment
	 * array list.
	 */
	private boolean previouslyAdded(Compartment co,
        ArrayList<Compartment> addedCompartments)
	{
		for (Compartment addedBefore : addedCompartments)
		{
			//if compartment has been added before, return true
			if (co == addedBefore)
			{
				return true;
			}
		}
		//if compartment is not found in added List, then return false
		return false;
	}
}
