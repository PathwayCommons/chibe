package org.gvt.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;

import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.gvt.util.EntityHolder;

import java.util.Arrays;
import java.util.*;

/**
 * This class maintains the Add Entity Dialog which is used for
 * adding entities during local querying
 *
 * @author Ozgun Babur
 * @author Merve Cakir
 * @author Shatlyk Ashyralyev
 * 
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class AddEntityDialog extends Dialog
{
	/**
	 * Result of filtering physical Entities with searchKey
	 */
	private ArrayList<EntityHolder> possibleEntities;
	
	/**
	 * Selected physical Entities
	 */
	private ArrayList<EntityHolder> selectedEntities;
	
	/**
	 * All physical Entites
	 */
	private ArrayList<EntityHolder> allEntities;
	
	/**
	 * Provides a mapping from KeyName of the entity to the entity type.
	 */
	private Map<String, EntityHolder> entityKeyNameMap;
	
	/**
	 * Boolean to check whether add button or cancel button has been pressed
	 */
	private boolean addPressed;

	/**
	 * SearchKey Text and Filter Button, to filter entities
	 */
	private Text searchKey;
	private Button filterButton;
	
	/**
	 * Entity List
	 */
	private List entityList;
	private Set<String> possibleEntityNames;
	private String[] allEntityNames;
	
	/**
	 * Buttons to add entities, showAll entities, cancel
	 */
	private Button addButton;
	private Button cancelButton;
	private Button showAllButton;

	/**
	 * Shell for dialog
	 */
	private Shell shell;

	/**
	 * Create the dialog
	 */
	public AddEntityDialog(Shell shell,	final ArrayList<EntityHolder> allEntities)
	{
		super(shell);
		
		this.entityKeyNameMap = new HashMap<String, EntityHolder>();
		this.selectedEntities = new ArrayList<EntityHolder>();
		this.possibleEntities = new ArrayList<EntityHolder>();
		this.possibleEntityNames = new HashSet<String>();
		
		/* make map between entities and their keyNames, 
		and add all entities to possibleEntities and EntityList*/
		for (EntityHolder entity : allEntities)
		{
			String keyName = entity.getName();

			possibleEntityNames.add(keyName);
			entityKeyNameMap.put(keyName, entity);
			possibleEntities.add(entity);
		}
		
		//keyNames of all entities in sorted form
		this.allEntityNames = toSortedArray(possibleEntityNames);
		this.allEntities = allEntities;
		this.shell = shell;
		addPressed = false;
	}

	/**
	 * keyAdapter for TextFiled
	 */
	protected KeyAdapter keyAdapter = new KeyAdapter(){};

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
		shell.setText("Add Entity");
		
		ButtonAdapter adapter = new ButtonAdapter();
		
		//Set Image
		ImageDescriptor id = ImageDescriptor.createFromFile(
			NeighborhoodQueryParamWithEntitiesDialog.class,
			"/src/main/resources/org/gvt/icon/cbe-icon.png");
		shell.setImage(id.createImage());
		
		//layout of shell contains 6 columns
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		shell.setLayout(gridLayout);
		
		//Length Limit Text
		
		searchKey = new Text(shell, SWT.BORDER);
		searchKey.addKeyListener(keyAdapter);
		GridData gridData = 
			new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan= 2;
		gridData.minimumWidth = 200;
		searchKey.setLayoutData(gridData);
		
		//Filter Button
		
		filterButton = new Button(shell, SWT.NONE);
		filterButton.setText("Filter");
		filterButton.addSelectionListener(adapter);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
		filterButton.setLayoutData(gridData);

		//EntityList
		
		entityList = new List(shell,
			SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		//setItems of List in sorted form
		entityList.setItems(allEntityNames);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.verticalSpan = 20;
		gridData.horizontalSpan = 3;
		gridData.heightHint = entityList.getItemHeight() * 10;
		gridData.widthHint= 500;
		entityList.setLayoutData(gridData);
		
		//ShowAll Button
		
		showAllButton = new Button(shell, SWT.NONE);
		showAllButton.setText("Show All");
		showAllButton.addSelectionListener(adapter);
		gridData = new GridData(GridData.CENTER, GridData.CENTER, false, false);
		showAllButton.setLayoutData(gridData);

		//Add Entity Button
		
		addButton = new Button(shell, SWT.NONE);
		addButton.setText("Add");
		addButton.addSelectionListener(adapter);
		gridData = new GridData(GridData.CENTER, GridData.CENTER, true, false);
		gridData.minimumWidth = 100;
		gridData.horizontalIndent = 5;
		addButton.setLayoutData(gridData);
				
		//Cancel Button
		
		cancelButton = new Button(shell, SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(adapter);
		gridData = new GridData(GridData.CENTER, GridData.CENTER, false, false);
		gridData.horizontalIndent = 5;
		cancelButton.setLayoutData(gridData);
		
		//pack dialog
		shell.pack();
	}
	
	/**
	 * Filters entities in List according to searchKey
	 */
	public void filterEntities()
	{
		//get search keyword
		String searchKeyWord = this.searchKey.getText();
		
		//clear all previously selected entities
		this.selectedEntities.clear();
		
		//if searchKey is empty then show all entities
		if ( searchKeyWord == null)
		{
			this.possibleEntities = new ArrayList<EntityHolder>(this.allEntities);
			return;
		}
		
		//clear all previous possible keys
		this.possibleEntities.clear();
		this.possibleEntityNames.clear();
		
		//for each entity
		for (EntityHolder entity : allEntities)
		{
			String name = entity.getName();

			if (entity.containsWord(searchKeyWord))
			{
				this.possibleEntities.add(entity);
				this.possibleEntityNames.add(name);
			}
		}
	}
	
	/**
	 * This method stores selected entities in selectedEntities List
	 */
	private void saveSelectedEntities()
	{
		this.selectedEntities.clear();
		String[] selectionResult = entityList.getSelection();
		
		//find selected entity and add it to selectedEntities ArrayList
		for (String selected : selectionResult)
		{
			for (EntityHolder entity : possibleEntities)
			{
				if (selected != null &&
					selected.equals(entity.getName()))
				{
					this.selectedEntities.add(entity);
				}
			}
		}
	}
	
	/**
	 * Getter
	 */
	public ArrayList<EntityHolder> getSelectedEntities()
	{
		return selectedEntities;
	}

	/**
	 * Method which sorts given set of Strings and converts them to Array
	 */
	private String[] toSortedArray(Set<String> entityNames)
	{
		//New string array of size ArrayList
		String[] entityNameArray = new String[entityNames.size()];
		//Convert ArrayList to array
		entityNameArray = entityNames.toArray(entityNameArray);
		//Sort entities according to keyNames
		Arrays.sort(entityNameArray);
	
		return entityNameArray;
	}
	
	/**
	 * Shows all entities in entity list
	 */
	private void showAllEntities()
	{
		//possible entities = all entities
		this.possibleEntities = new ArrayList<EntityHolder>(this.allEntities);
		
		//no entity is selected
		this.selectedEntities.clear();
		
		//update List
		this.entityList.removeAll();
		this.entityList.setItems(allEntityNames);

		searchKey.setText("");
	}
	
	/**
	 * This method updates the entity list. 
	 * It is called just after the filter
	 */
	private void updateList()
	{
		this.entityList.removeAll();
		this.entityList.setItems(toSortedArray(this.possibleEntityNames));
	}
	
	/**
	 * Class for handling button selections of buttons
	 */
	class ButtonAdapter extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent arg)
		{
			Button button = (Button) arg.widget;
			//if add button is pushed, save selected entities, set addPressed true
			if (button == addButton)
			{
				saveSelectedEntities();
				addPressed = true;
				shell.dispose();
			}
			//if cancel button is pushed, set addPressed false, dispose the shel
			else if (button == cancelButton)
			{
				addPressed = false;
				shell.dispose();
			}
			//if filter button is pushed, filter the list, then update it
			else if (button == filterButton)
			{
				filterEntities();
				updateList();
			}
			//if showAll button is pushed, show all entities
			else if (button == showAllButton)
			{
				showAllEntities();
			}
		}
	}
}
