package org.gvt.gui;

import java.lang.reflect.Field;
import java.util.*;

import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.xref;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.gvt.ChisioMain;
import org.gvt.util.AbstractOptionsPack;
import org.gvt.util.EntityHolder;

/**
 * This class is abstract class for Local Query Dialogs
 *
 * @author Ozgun Babur
 * @author Merve Cakir
 * @author Shatlyk Ashyralyev
 * 
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public abstract class AbstractQueryParamDialog extends Dialog 
{
	/**
	 * buttons for adding and removing entities
	 */
	protected Button addButton;
	protected Button removeButton;
	protected List entityList;
	
	/**
	 * show result
	 */
	protected Group resultViewGroup;
	protected Button currentViewButton;
	protected Button newViewButton;
	
	/**
	 * buttons to quit the dialog
	 */
	protected Group exeCancelDefaultGroup;
	protected Button executeButton;
	protected Button cancelButton;
	
	/**
	 * Button to restore default valus of dialog
	 */
	protected Button defaultButton;
	
	/**
	 * length limit (depth)
	 */
	protected Label lengthLimitLabel;
	protected Text lengthLimit;
	
	/**
	 * Shell used for query dialogs
	 */
	protected Shell shell;

	/**
	 * Main ChisioMain
	 */
	protected ChisioMain main;

	/**
	 * Key adapter to filter nonDigit characters from Length Limit Text
	 */
	protected KeyAdapter keyAdapter = new KeyAdapter()
	{
		public void keyPressed(KeyEvent arg0)
		{
			arg0.doit = isDigit(arg0.keyCode);
		}

		public boolean isDigit(int keyCode)
		{
			if (Character.isDigit(keyCode)
				|| keyCode == SWT.DEL
				|| keyCode == 8	//ascii of back space
				|| keyCode == SWT.ARROW_LEFT
				|| keyCode == SWT.ARROW_RIGHT)
			{
				return true;
			}
			return false;
		}
	};
	

	/**
	 * Create the dialog
	 */
	public AbstractQueryParamDialog(ChisioMain main)
	{
		super(main.getShell(), SWT.NONE);
		this.main = main;
	}

	/**
	 * Create shell for query dialogs
	 * @param opt
	 */
	protected void createContents(AbstractOptionsPack opt)
	{
		shell = new Shell(getParent(), 
			SWT.RESIZE | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}
	
	/**
	 * Method for creating a list
	 */
	protected void createList(int horizontalSpan,
		int verticalSpan,
		int widthHint,
		int numberOfItems)
	{
		entityList = new List(shell, 
			SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.HORIZONTAL);
		GridData gridData =
			new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.verticalSpan = verticalSpan;
		gridData.horizontalSpan = horizontalSpan;
		gridData.heightHint = entityList.getItemHeight() * numberOfItems;
		gridData.widthHint = widthHint;
		entityList.setLayoutData(gridData);
	}
	
	/**
	 * Method for creating Result View Group
	 */
	protected void createResultViewGroup(int horizontalSpan,
		int verticalSpan)
	{
		resultViewGroup = new Group(shell, SWT.NONE);
		resultViewGroup.setText("Show result in");
		GridData gridData = 
			new GridData(GridData.FILL, GridData.BEGINNING, false, false);
		gridData.horizontalSpan = horizontalSpan;
		gridData.verticalSpan = verticalSpan;
		resultViewGroup.setLayoutData(gridData);
		resultViewGroup.setLayout(new GridLayout());

		//Current View Radio Button
		
		currentViewButton = new Button(resultViewGroup, SWT.RADIO);
		currentViewButton.setText("Current view");
		gridData = 
			new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
		currentViewButton.setLayoutData(gridData);
		
		//New View Radio Button
		
		newViewButton = new Button(resultViewGroup, SWT.RADIO);
		newViewButton.setText("New view");
		gridData = 
			new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
		newViewButton.setLayoutData(gridData);
	}
	
	/**
	 * Method for creating Length Limit Label and Text
	 */
	protected void createLengthLimit(int horizontalSpanLabel,
		int verticalSpanLabel,
		int horizontalSpanText,
		int verticalSpanText,
		int minTextWidth)
	{
		//Length Limit Label
		
		lengthLimitLabel = new Label(shell, SWT.NONE);
		lengthLimitLabel.setText("Length limit");
		GridData gridData = new GridData(GridData.END, GridData.CENTER, false, false);
		gridData.horizontalSpan = horizontalSpanLabel;
		gridData.verticalSpan = verticalSpanLabel;
		lengthLimitLabel.setLayoutData(gridData);

		//Length Limit Text
		
		lengthLimit = new Text(shell, SWT.BORDER);
		lengthLimit.addKeyListener(keyAdapter);
		gridData = 
			new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
		gridData.horizontalSpan = horizontalSpanText;
		gridData.verticalSpan = verticalSpanText;
		gridData.widthHint = minTextWidth;
		lengthLimit.setLayoutData(gridData);
	}
	
	/**
	 * Method to create cancel button
	 */
	protected void createCancelButton(GridData gridData)
	{
		cancelButton.setText("Cancel");
		cancelButton.setLayoutData(gridData);
		cancelButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				shell.close();
			}
		});
	}

	/**
	 * Method to set default values of abstract dialog
	 */
	public void setDefaultQueryDialogOptions()
	{
		// To prevent selecting current view as it is the default value
		if(!currentViewButton.isEnabled())
		{
			currentViewButton.setSelection(!CURRENT_VIEW);
			newViewButton.setSelection(CURRENT_VIEW);
		}
		else
		{
			currentViewButton.setSelection( CURRENT_VIEW );
			newViewButton.setSelection( !CURRENT_VIEW );
		}

		lengthLimit.setText(String.valueOf(DEFAULT_LENGTH_LIMIT));
	}

	/**
	 * After creating the dialog box, initial values are assigned to the  
	 * fields with data in opt OptionsPack
	 */
	public void setInitialValues(AbstractOptionsPack opt)
	{
		if (main.getPathwayGraph() == null)
		{
			newViewButton.setSelection(true);
			currentViewButton.setSelection(false);
			currentViewButton.setEnabled(false);
			opt.setCurrentView(false);
		}

		if (opt.isCurrentView())
		{
			currentViewButton.setSelection(true);
		}
		else
		{
			newViewButton.setSelection(true);
		}

		lengthLimit.setText(String.valueOf(opt.getLengthLimit()));
	}

	class EntityListGroup extends Composite
	{
		protected List entityList;
		protected Button addButton;
		protected Button removeButton;

		/**
		  * All entities of graph
		  */
		java.util.List<EntityHolder> allEntities;

		java.util.List<EntityHolder> addedEntities;

		public EntityListGroup(Composite composite, int i, java.util.List<EntityHolder> allEntities)
		{
			super(composite, i);
			this.allEntities = allEntities;
		}

		public void init()
		{
			GridLayout lay = new GridLayout();
			lay.numColumns = 2;
			lay.makeColumnsEqualWidth = true;

			this.setLayout(lay);

			addedEntities = new ArrayList<EntityHolder>();

			entityList = new List(this,
				SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.HORIZONTAL);
			GridData gridData =
				new GridData(GridData.FILL, GridData.FILL, true, true);
			gridData.verticalSpan = 1;
			gridData.horizontalSpan = 2;
			entityList.setLayoutData(gridData);

		//Add Entity Button

		addButton = new Button(this, SWT.NONE);
		addButton.setText("Add...");
		gridData = new GridData(GridData.END, GridData.BEGINNING, true, false);
		gridData.minimumWidth = 50;
		gridData.horizontalIndent = 5;
		addButton.setLayoutData(gridData);
		addButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				//new addEntityDialog
				AddEntityDialog addEntityDialog = new AddEntityDialog(new Shell(), allEntities);

				//open dialog
				boolean addPressed = addEntityDialog.open();

				//if add button is pressed
				if (addPressed)
				{
					//for each selected entity
					for (EntityHolder entity : addEntityDialog.getSelectedEntities())
					{
						//check if entity has been added before
						if (!addedEntities.contains(entity))
						{
							//add entity keyName to List
							entityList.add(entity.getName());

							//add entity to addedEntities ArrayList
							addedEntities.add(entity);
						}
					}
				}
			}
		});

		//Remove Entity Button

		removeButton = new Button(this, SWT.NONE);
		removeButton.setText("Remove");
		gridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false);
		gridData.horizontalIndent = 5;
		gridData.minimumWidth = 50;
		removeButton.setLayoutData(gridData);
		removeButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				String[] selectionResult = entityList.getSelection();

				//for each selected string
				for (String selected : selectionResult)
				{
					//search among all addedEntities
					for (int j = 0 ; j < addedEntities.size() ; j++)
					{
						EntityHolder entity = addedEntities.get(j);

						//if corresponding entity is found
						if (selected != null && selected.equals(entity.getName()))
						{
							//remove entity from addedEntities ArrayList
							addedEntities.remove(j);

							//remove entity keyName from from List
							entityList.remove(selected);
						}
					}
				}
			}
		});

		}
	}

	class SymbolText extends Composite
	{
		protected Label label;
		protected Text symbolText;

		SymbolText(Composite composite, int i)
		{
			super(composite, i);
		}

		public void init(String labelText)
		{
			GridLayout lay = new GridLayout();
			lay.numColumns = 1;

			this.setLayout(lay);

			label = new Label(this, SWT.NONE);
			label.setText(labelText == null ? "Gene symbols:" : labelText);
			GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, false);
			gridData.verticalSpan = 1;
			gridData.horizontalSpan = 1;
			label.setLayoutData(gridData);

			symbolText = new Text(this, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
			gridData.verticalSpan = 1;
			gridData.horizontalSpan = 1;
			symbolText.setLayoutData(gridData);
		}

		public java.util.List<String> getSymbols()
		{
			java.util.List<String> list = new ArrayList<String>();
			String text = symbolText.getText();
			for (String s : text.split("\n ,\t\"'|"))
			{
				if (s != null && s.length() > 1) list.add(s);
			}
			return list;
		}
	}

	/**
	 * Values for default options
	 */
	public static final boolean DOWNSTREAM = true;
	public static final boolean UPSTREAM = true;
	
	public static final boolean CURRENT_VIEW = true;

	public static final int DEFAULT_LENGTH_LIMIT = 1;
    public static final int DEFAULT_SHORTEST_PLUS_K = 0;

    public static final boolean LIMIT_TYPE = true;

    public static final boolean STRICT = false;   

}
