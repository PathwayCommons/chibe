package org.gvt.gui;


import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class maintains the AddCompartmentDialog which is used for
 * adding compartments during local compartment query.
 *
 * @author Merve Cakir
 */
public class AddCompartmentDialog extends Dialog
{
	/**
	 * Selected compartments
	 */
	private ArrayList<String> selectedCompartments;

	/**
	 * All compartments
	 */
	private java.util.List<String> allCompartments;

	private String[] allCompartmentNames;

	/**
	 * Boolean to check whether add button or cancel button has been pressed
	 */
	private boolean addPressed;

	/**
	 * Compartment List
	 */
	private List compartmentList;

	/**
	 * Buttons to add compartments and cancel
	 */
	private Button addButton;
	private Button cancelButton;

	/**
	 * Shell for dialog
	 */
	private Shell shell;

	/**
	 * Constructor
	 */
	public AddCompartmentDialog(Shell shell,
		java.util.List<String> allCompartments)
	{
		super(shell);
		this.shell = shell;

		this.allCompartments = allCompartments;

		// Store all compartments' names in a sorted fashion
		this.allCompartmentNames = new String[allCompartments.size()];
		for (int i = 0; i < allCompartments.size(); i ++)
		{
			allCompartmentNames[i] = allCompartments.get(i);
		}
		Arrays.sort(allCompartmentNames);
		
		this.selectedCompartments = new ArrayList<String>();

		addPressed = false;
	}

	/**
	 * Open the dialog
	 */
	public boolean open()
	{
		createContents();

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

		return addPressed;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents()
	{
		//new shell
		shell = new Shell(getParent(),
			SWT.RESIZE | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Add Compartment");

		//Set Image
		ImageDescriptor id = ImageDescriptor.createFromFile(
			ChisioMain.class, "icon/cbe-icon.png");
		shell.setImage(id.createImage());

		//layout of shell contains 2 columns

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		shell.setLayout(gridLayout);

		// All compartments list

		compartmentList = new List(shell,
			SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		// setItems of List in a sorted form
		compartmentList.setItems(allCompartmentNames);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL,
			true, true);
		gridData.horizontalSpan = 2;
		gridData.heightHint = compartmentList.getItemHeight() * 5;
		gridData.widthHint= 200;
		compartmentList.setLayoutData(gridData);

		//Add Button for adding compartments

		addButton = new Button(shell, SWT.NONE);
		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				saveSelectedCompartments();
				addPressed = true;
				shell.dispose();
			}
		});
		gridData = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
		gridData.minimumWidth = 100;
		addButton.setLayoutData(gridData);

		// Cancel button

		cancelButton = new Button(shell, SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				addPressed = false;
				shell.dispose();
			}
		});
		gridData = new GridData(GridData.END, GridData.CENTER, true, false);
		gridData.minimumWidth = 100;
		cancelButton.setLayoutData(gridData);

		shell.pack();
	}

	/**
	 * Stores selected compartments in selectedCompartments List
	 */
	private void saveSelectedCompartments()
	{
		this.selectedCompartments.clear();
		String[] selectionResult = compartmentList.getSelection();

		//find selected compartment and add it to selectedCompartments ArrayList

		for (String selected : selectionResult)
		{
			for (String compartment : allCompartments)
			{
				if (selected != null && selected.equals(compartment))
				{
					this.selectedCompartments.add(compartment);
				}
			}
		}
	}

	/**
	 * Getter
	 */
	public java.util.List<String> getSelectedCompartments()
	{
		return selectedCompartments;
	}
}



