package org.gvt.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.gvt.ChisioMain;
import org.gvt.model.CompoundModel;
import org.gvt.model.GraphObject;
import org.gvt.util.QueryOptionsPack;

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

	private List targetCompartmentList;

	/**
	 * All compartments of graph
	 */
	ArrayList<CompoundModel> allCompartments;

	/**
	 * Compartments which are added to source list and to target list.
	 */
	ArrayList<CompoundModel> sourceAddedCompartments;
	ArrayList<CompoundModel> targetAddedCompartments;

	/**
	 * Getters
	 */

	public ArrayList<CompoundModel> getSourceAddedCompartments()
	{
		return this.sourceAddedCompartments;
	}

	public ArrayList<CompoundModel> getTargetAddedCompartments()
	{
		return this.targetAddedCompartments;
	}

	/**
	 * Constructor
	 */
	public CompartmentQueryParamWithEntitiesDialog(ChisioMain main)
	{
		super(main);

		this.allCompartments = new ArrayList<CompoundModel>();
		// Selecting compartments from whole node set
		
		Set<GraphObject> candidate = main.getRootGraph().getNodes();
		for (GraphObject go : candidate)
		{
			if (go instanceof org.gvt.model.biopaxl2.Compartment ||
				go instanceof org.gvt.model.biopaxl3.Compartment)
			{
				allCompartments.add((CompoundModel)go);
			}
		}

		this.sourceAddedCompartments = new ArrayList<CompoundModel>();
		this.targetAddedCompartments = new ArrayList<CompoundModel>();
	}

	/**
	 * Create contents of the dialog.
	 * Buttons, List, Text Field, Radio Buttons, etc.
	 */
	protected void createContents(final QueryOptionsPack opt)
	{
		super.createContents(opt);
		shell.setText("Compartment Query Properties");
        infoLabel.setText("Find paths that originate in one compartment and end in another compartment");

		//Set Image
		ImageDescriptor id = ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbe-icon.png");
		shell.setImage(id.createImage());

		//layout of shell will contain 8 columns.

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 8;
		shell.setLayout(gridLayout);

		//source compartment list's label

		Label sourceLabel = new Label(shell, SWT.NONE);
		sourceLabel.setText("Source");
		GridData gridData = new GridData(GridData.CENTER, GridData.END,
			false, false);
		gridData.horizontalSpan = 2;
		sourceLabel.setLayoutData(gridData);

		//target compartment list's label

		Label targetLabel = new Label(shell, SWT.NONE);
		targetLabel.setText("Target");
		gridData = new GridData(GridData.CENTER, GridData.END, false, false);
		gridData.horizontalSpan = 2;
		targetLabel.setLayoutData(gridData);

		//Group for currentViewButton and newViewButton
		createResultViewGroup(2, 2);

		//Group for lengthLimitButton and shortestPlusKButton

		Group limitTypeGroup = new Group(shell, SWT.NONE);
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
					for (CompoundModel compartment :
						addCompartment.getSelectedCompartments())
					{
						//check if compartment has been added before
						if (!sourceAddedCompartments.contains(compartment))
						{
							//add compartment name to source compartment list
							entityList.add(compartment.getText());

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
						CompoundModel compartment = sourceAddedCompartments.get(j);

						//if corresponding compartment is found
						if (selected != null &&
							selected.equals(compartment.getText()))
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

		Button targetAddButton = new Button(shell, SWT.NONE);
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
					for (CompoundModel compartment :
						addCompartment.getSelectedCompartments())
					{
						//check if compartment has been added before
						if (!targetAddedCompartments.contains(compartment))
						{
							//add compartment name to target compartment list
							targetCompartmentList.add(compartment.getText());

							//add compartment to targetAddedCompartments ArrayList
							targetAddedCompartments.add(compartment);
						}
					}
				}
			}
		});

		//Target remove button

		Button targetRemoveButton = new Button(shell, SWT.NONE);
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
					for (int j = 0; j < targetAddedCompartments.size(); j++)
					{
						CompoundModel compartment = targetAddedCompartments.get(j);

						//if corresponding compartment is found
						if (selected != null &&
							selected.equals(compartment.getText()))
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
		createExeCancDefGroup(opt, 8);

		//pack dialog
		shell.pack();

		//set initial values from opt OptionsPack
		setInitialValues(opt);
	}
}
