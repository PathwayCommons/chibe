package org.patika.mada.gui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.gvt.ChisioMain;
import org.gvt.action.ColorWithExperimentAction;
import org.gvt.model.BioPAXGraph;
import org.patika.mada.util.ExperimentDataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Recep Colak
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ExperimentDataManagementDialog extends Dialog implements SelectionListener
{
	protected Shell shell;

	/**
	 * Main applicaiton to access.
	 */
	ChisioMain main;

	/**
	 * Data to be managed.
	 */
	ExperimentDataManager dataManager;

	/**
	 * Used to mark experiments in the first group and the second group.
	 */
	Boolean[] checks1;
	Boolean[] checks2;

	/**
	 * Table to choose experiments.
	 */
//	JTable experimentTable;

	Table table;
	TableViewer viewer;

	Group experimentGroup;

	/**
	 * To display the whole experiment set info.
	 */
	Text datasetInfoArea;

	/**
	 * To display the selected experiment's info.
	 */
	Text infoArea;

	Button singleButton;
	Button compareButton;

	Button valuesButton;
	Button saveButton;

	Button updateButton;

	List<Integer> selection1;
	List<Integer> selection2;

	RowItem[] items;

	/**
	 * Will be in SINGLE or COMPARE mode.
	 */
	boolean mode;

	public ExperimentDataManagementDialog(ChisioMain main, ExperimentDataManager dataManager)
	{
		super(main.getShell(), SWT.NONE);
		this.dataManager = dataManager;
		this.main = main;

		createContents();
	}

	/**
	 * Open the dialog
	 */
	public void open()
	{
		createContents();
		shell.pack();
		shell.layout();
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents()
	{
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText(dataManager.getType());

		ImageDescriptor id = ImageDescriptor.createFromFile(
			ChisioMain.class, "icon/cbe-icon.png");
		shell.setImage(id.createImage());

		shell.setLayout(new org.eclipse.swt.layout.GridLayout(2, false));

		// Dataset info group

		Group datasetInfoPanel = new Group(shell, SWT.NONE);
		datasetInfoPanel.setText("Experiment Dataset Info");
		datasetInfoPanel.setLayout(new FillLayout());
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.widthHint = 400;
		data.heightHint = 100;
		datasetInfoPanel.setLayoutData(data);

		datasetInfoArea = new Text(datasetInfoPanel, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		if (this.dataManager.isDatasetInfoAvailable())
		{
			datasetInfoArea.setText(dataManager.getDatasetInfo());
		}
		else
		{
			datasetInfoArea.setText("Dataset info not available.");
			datasetInfoArea.setEnabled(false);
		}

		datasetInfoArea.setEditable(false);


		// Experiments table

		experimentGroup = new Group(shell, SWT.NONE);
		experimentGroup.setLayout(new FillLayout());
		experimentGroup.setText("Loaded Experiments");
		data = new GridData(GridData.FILL_BOTH);
		//data.grabExcessVerticalSpace = true;
        data.heightHint = 400;
		experimentGroup.setLayoutData(data);

		// Selected experiment info group

		Group infoPanel = new Group(shell, SWT.NONE);
		infoPanel.setText("Experiment Info");
		infoPanel.setLayout(new FillLayout());
		data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		data.widthHint = 100;
		data.heightHint = 100;
		infoPanel.setLayoutData(data);

		infoArea = new Text(infoPanel, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		if (this.dataManager.isExpInfoAvailable())
		{
			infoArea.setText(NO_SELECTED_EXP_DATA_MSSG);
		}
		else
		{
			infoArea.setText("Experiment Info not available");
		}
		infoArea.setEditable(false);

		// Mode group

		Composite valueTypePanel = new Composite(shell, SWT.NONE);
		valueTypePanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		valueTypePanel.setLayout(new FillLayout(SWT.VERTICAL));

		singleButton = new Button(valueTypePanel, SWT.RADIO);
		singleButton.setText("Visualize one");
		compareButton = new Button(valueTypePanel, SWT.RADIO);
		compareButton.setText("Compare two");
		singleButton.addSelectionListener(this);
		compareButton.addSelectionListener(this);
		compareButton.setEnabled(this.dataManager.getExperimentSize() > 1);

		// Buttons group

		Composite buttonsGroup = new Composite(shell, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.justify = true;
		buttonsGroup.setLayout(rowLayout);
		valuesButton = new Button(buttonsGroup, SWT.PUSH);
		valuesButton.setText("Values...");
		saveButton = new Button(buttonsGroup, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.setToolTipText("Save Grouping");
		updateButton = new Button(buttonsGroup, SWT.PUSH);
		updateButton.setText("Update");
		updateButton.addSelectionListener(this);
		valuesButton.addSelectionListener(this);
		saveButton.addSelectionListener(this);
		saveButton.setEnabled(false);

		// Other data related structures

		this.mode = dataManager.getSecondExpIndices().isEmpty() ? SINGLE : COMPARE;

		checks1 = new Boolean[dataManager.getExperimentSize()];
		checks2 = new Boolean[dataManager.getExperimentSize()];

		for (int i=0; i<checks1.length; i++)
		{
			checks1[i] = false;
			checks2[i] = false;
		}

		this.selection1 = new ArrayList<Integer>();
		this.selection2 = new ArrayList<Integer>();

		if (!dataManager.getFirstExpIndices().isEmpty())
		{
			for (int i : dataManager.getFirstExpIndices())
			{
				checks1[i] = true;
				selection1.add(i);
			}
			singleButton.setSelection(true);
		}
		if (!dataManager.getSecondExpIndices().isEmpty())
		{
			singleButton.setSelection(false);
			compareButton.setSelection(true);

			for (int i : dataManager.getSecondExpIndices())
			{
				checks2[i] = true;
				selection2.add(i);
			}
		}

		// Experiment table

		createTable(experimentGroup);

		if (dataManager.getExperimentSize() == 0)
		{
			this.valuesButton.setEnabled(false);
		}
	}

	public void widgetDefaultSelected(SelectionEvent event)
	{
		widgetSelected(event);
	}

	public void widgetSelected(SelectionEvent e)
	{
		Object source = e.getSource();

		if (source == this.updateButton)
		{
			if (updateGrouping())
			{
				List<BioPAXGraph> graphs = main.getAllPathwayGraphs();
				graphs.add(main.getRootGraph());

				for (BioPAXGraph graph : graphs)
				{
					this.dataManager.clearExperimentData(graph);
					this.dataManager.associateExperimentData(graph);
					if (graph.getLastAppliedColoring() != null)
					{
						graph.setLastAppliedColoring(null);
						new ColorWithExperimentAction(main, graph, dataManager.getType()).run();
					}
				}

				BioPAXGraph currentGraph = main.getPathwayGraph();
				if (currentGraph != null && currentGraph.getLastAppliedColoring() == null)
				{
					new ColorWithExperimentAction(main, currentGraph, dataManager.getType()).run();
				}

				shell.dispose();
			}
		}
		else if (source == this.valuesButton)
		{
			ExperimentDataVisualizationDialog valuesDialog =
				new ExperimentDataVisualizationDialog(main, this.dataManager);

			valuesDialog.open();
		}
		else if (source == this.saveButton)
		{
			if (updateGrouping())
			{
				this.dataManager.saveData();
				saveButton.setEnabled(false);
			}
		}
		else if (source == this.singleButton)
		{
			if (singleButton.getSelection())
			{
				mode = SINGLE;
				saveButton.setEnabled(true);
				recreateTable();
			}
		}
		else if (source == this.compareButton)
		{
			if (compareButton.getSelection())
			{
				mode = COMPARE;
				saveButton.setEnabled(true);
				recreateTable();
			}
		}
		else if (source == table)
		{
			TableItem[] selectedItems = table.getSelection();
			if (selectedItems.length > 0)
			{
				int expNo = ((RowItem) selectedItems[0].getData()).expNum;
				this.infoArea.setText(dataManager.getExperimentInfo(expNo));
			}
			else
			{
				this.infoArea.setText(NO_SELECTED_EXP_DATA_MSSG);
			}
		}
	}

	private boolean updateGrouping()
	{
		if (singleButton.getSelection())
		{
			selection2.clear();
		}
		if (selectionsOK())
		{
			this.dataManager.setDataToBeUsed(selection1, selection2);
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean selectionsOK()
	{
		if (selection1.isEmpty())
		{
			MessageDialog.openError(shell, "Missing Selection!",
				"You should select at least one experiment " +
					((mode == SINGLE) ? "to use!" : "in group 1 (Gr1)!"));

			return false;
		}
		else if (mode == COMPARE && selection2.isEmpty())
		{
			MessageDialog.openError(shell, "Missing Selection!",
				"You should select at least one experiment in group 2 (Gr2)!");

			return false;
		}
		return true;
	}

	private void createTable(Composite c)
	{
		// Table

		table = new Table(c, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL |
			SWT.FULL_SELECTION | SWT.HIDE_SELECTION);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] columnNames = createColumns();

		// Table viewer
		createViewer(columnNames);

		table.addSelectionListener(this);
	}

	private void createViewer(String[] columnNames)
	{
		viewer = new TableViewer(table);
		viewer.setUseHashlookup(true);

		viewer.setColumnProperties(columnNames);

		// Create the cell editors
		CellEditor[] editors = new CellEditor[columnNames.length];

		// Column 0 : Experiment name (Free text)
		TextCellEditor textEditor = new TextCellEditor(table);
		((Text) textEditor.getControl()).setTextLimit(60);
		editors[0] = textEditor;

		// Column 1 : Group 1 (Checkbox)
		editors[1] = new CheckboxCellEditor(table);

		// Column 2 : Group 1 (Checkbox)
		if (mode == COMPARE) editors[2] = new CheckboxCellEditor(table);

		// Assign the cell editors to the viewer
		viewer.setCellEditors(editors);
		// Set the cell modifier for the viewer
		viewer.setCellModifier(new CellModifier());

		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new LabelProvider());

		items = new RowItem[dataManager.getExperimentSize()];

		for (int i = 0; i < items.length; i++)
		{
			items[i] = new RowItem(i);
		}

		// The input for the table viewer is the instance of ExampleTaskList
		viewer.setInput(items);
	}

	private String[] createColumns()
	{
		boolean compare = mode == COMPARE;

		String[] columnNames = getColumnNames();

		TableColumn column = new TableColumn(table, SWT.LEFT, 0);
		column.setText(columnNames[0]);
		column.setWidth(100);

		column = new TableColumn(table, SWT.CENTER, 1);
		column.setText(columnNames[1]);
		column.setWidth(30);

		if (compare)
		{
			column = new TableColumn(table, SWT.CENTER, 2);
			column.setText(columnNames[2]);
			column.setWidth(30);
		}

		return columnNames;
	}

	private void recreateTable()
	{
		table.dispose();
		createTable(experimentGroup);
		if (false) colorColumns();
		shell.setSize(shell.getSize().x, shell.getSize().y + 1);
		shell.setSize(shell.getSize().x, shell.getSize().y - 1);
		shell.pack();
	}

	private void colorColumns()
	{
		for (TableItem item : table.getItems())
		{
			if (item.hashCode() % 2 == 0) item.setForeground(new Color(null, 255, 0, 0));
		}
	}

	private String[] getColumnNames()
	{
		String[] columnNames = new String[compareButton.getSelection() ? 3 : 2];

		columnNames[0] = "Experiment";
		columnNames[1] = mode == COMPARE ? "Gr1" : "Use";
		if (mode == COMPARE) columnNames[2] =  "Gr2";

		return columnNames;
	}

	private class CellModifier implements ICellModifier
	{
		public boolean canModify(Object o, String s)
		{
			return getColumnNo(s) > 0;
		}

		public Object getValue(Object o, String s)
		{
			RowItem item = (RowItem) o;
			return item.getValue(s);
		}

		public void modify(Object o, String s, Object o1)
		{
			TableItem item = (TableItem) o;
			RowItem row = (RowItem) item.getData();
			row.setValue(s, o1);
			viewer.update(item, null);
		}
	}

	private class ContentProvider implements IStructuredContentProvider
	{
		public void dispose(){}
		public void inputChanged(Viewer viewer, Object o, Object o1){}

		public Object[] getElements(Object o)
		{
			return items;
		}
	}

	private class LabelProvider implements ITableLabelProvider
	{
		public void addListener(ILabelProviderListener iLabelProviderListener){}

		public void dispose(){}

		public boolean isLabelProperty(Object o, String s)
		{
			return false;
		}

		public void removeListener(ILabelProviderListener iLabelProviderListener){}

		public Image getColumnImage(Object o, int i)
		{
			return null;
		}

		public String getColumnText(Object o, int i)
		{
			RowItem item = (RowItem) o;

			if (i == 0)
			{
				return item.getValue(i).toString();
			}
			else
			{
				boolean checked = (Boolean) item.getValue(i);
				return checked ? "X" : "";
			}
		}
	}

	private class RowItem
	{
		private int expNum;

		private RowItem(int expNum)
		{
			this.expNum = expNum;
		}

		String getExperimentName()
		{
			return dataManager.getExperimentName(expNum);
		}

		boolean inGroup1()
		{
			return selection1.contains(expNum);
		}

		boolean inGroup2()
		{
			return selection2.contains(expNum);
		}

		void updateGroup1Inclusion(boolean include)
		{
			if (include)
			{
				if (!selection1.contains(expNum))
				{
					selection1.add(expNum);
					selection2.remove(new Integer(expNum));
				}
			}
			else
			{
				selection1.remove(new Integer(expNum));
			}
		}

		void updateGroup2Inclusion(boolean include)
		{
			if (include)
			{
				if (!selection2.contains(expNum))
				{
					selection2.add(expNum);
					selection1.remove(new Integer(expNum));
				}
			}
			else
			{
				selection2.remove(new Integer(expNum));
			}
		}

		Object getValue(int columnNo)
		{
			switch (columnNo)
			{
				case 0 : return getExperimentName();
				case 1 : return inGroup1();
				case 2 : return inGroup2();
				default : throw new RuntimeException("Invalid column no: " + columnNo);
			}
		}

		Object getValue(String columnName)
		{
			int no = getColumnNo(columnName);
			return getValue(no);
		}

		void setValue(int columnNo, Object newValue)
		{
			boolean v = (Boolean) newValue;
			switch (columnNo)
			{
				case 1 : updateGroup1Inclusion(v); break;
				case 2 : updateGroup2Inclusion(v); break;
				default : throw new RuntimeException("Invalid column no: " + columnNo);
			}
			saveButton.setEnabled(true);
			viewer.update(this, null);
		}

		void setValue(String columnName, Object newValue)
		{
			int no = getColumnNo(columnName);
			setValue(no, newValue);
		}
	}

	int getColumnNo(String s)
	{
		if (s.equals("Experiment"))
		{
			return 0;
		}
		else if (s.equals("Use") || s.equals("Gr1"))
		{
			return 1;
		}
		else if (s.equals("Gr2"))
		{
			return 2;
		}
		else
		{
			throw new RuntimeException("Invalid column name: " + s);
		}
	}

	public static final boolean SINGLE = false;
	public static final boolean COMPARE = true;

	private static final String NO_SELECTED_EXP_DATA_MSSG = "Select experiment to view info.";
}
