package org.patika.mada.gui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.gvt.ChisioMain;
import org.gvt.action.GetNeighborhoodOfSelectedEntityAction;
import org.gvt.action.QueryNeighborsAction;
import org.gvt.model.BioPAXGraph;
import org.gvt.util.EntityHolder;
import org.patika.mada.dataXML.Reference;
import org.patika.mada.dataXML.Row;
import org.patika.mada.util.ExperimentDataManager;
import org.patika.mada.util.XRef;

import java.util.*;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ExperimentDataVisualizationDialog extends Dialog implements SelectionListener
{
	//--------------------------------------------------------------------------
	// Section: Instance variables.
	//--------------------------------------------------------------------------

	Shell shell;

	/**
	 * Main application.
	 */
	ChisioMain main;

	/**
	 * Data to visualize
	 */
	ExperimentDataManager man;

	/**
	 * All rows to display
	 */
	List<Row> display;

	/**
	 * Current page of rows to display
	 */
	List<Row> page;

	/**
	 * Concatanated data of rows for querying
	 */
	List<String> data;

	/**
	 * Maps querying data to rows
	 */
	Map<String, Row> dataToRow;

	/**
	 * Size of a page to display
	 */
	int pageSize;

	/**
	 * Current page number
	 */
	int pageNo;

	/**
	 * Table to display values
	 */
	Table table;

	/**
	 * Scrolled composite to hold table
	 */
	ScrolledComposite tableScroll;

	/**
	 * Table model for values table
	 */
	ExperimentValuesTableModel model;

	/**
	 * Text field for querying the data rows
	 */
	Text filterField;

	/**
	 * Table will refresh according to the query upon pressing this button.
	 */
	Button filterButton;

	/**
	 * Navigation buttons
	 */
	Button nextButton;
	Button backButton;

	/**
	 * Shows the page number and total pages.
	 */
	Label pageLabel;

	/**
	 * For getting the selected rows to the graph.
	 */
	Button queryButton;

	/**
	 * Determines the type of the query to be done when user hits the query
	 * button.
	 */
	Combo queryTypeBox;

	/**
	 * Page size combo
	 */
	Combo pageSizeComboBox;

	/**
	 * Sorting option
	 */
	Button sortCheckBox;

	//--------------------------------------------------------------------------
	// Section: Constructors and initializers.
	//--------------------------------------------------------------------------

	/**
	 * Constructor with Experiment data
	 */
	public ExperimentDataVisualizationDialog(ChisioMain main, ExperimentDataManager man)
	{
		super(main.getShell(), SWT.NONE);

		this.main = main;
		this.man = man;
	}

	/**
	 * Open the dialog
	 */
	public void open()
	{
		createContents();
		shell.open();
		shell.pack();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
	}

	private void createContents()
	{
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Values");

		ImageDescriptor id = ImageDescriptor.createFromFile(
                ChisioMain.class, "icon/cbe-icon.png");
		shell.setImage(id.createImage());

		int width = Math.max(620, 100 + (man.getExperimentSize() * 60));
		width = Math.min(width, 1200);
		shell.setSize(width, 600);

		shell.setLayout(new GridLayout(1, false));

		// Upper panel

		Composite upperPanel = new Composite(shell, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = false;
		rowLayout.fill = true;
		rowLayout.justify = true;
		upperPanel.setLayout(rowLayout);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		upperPanel.setLayoutData(data);

		// Filter panel

		Group filterPanel = new Group(upperPanel, SWT.NONE);
		filterPanel.setText("Filter rows");
		filterPanel.setLayout(new RowLayout());

		filterField = new Text(filterPanel, SWT.BORDER);
		filterField.setTextLimit(30);
		filterField.setLayoutData(new RowData(80, 17));

		filterButton = new Button(filterPanel, SWT.PUSH);
		filterButton.setText("Filter");

		filterButton.addSelectionListener(this);

		// Page size panel

		Group pageSizePanel = new Group(upperPanel, SWT.NONE);
		pageSizePanel.setText("Page size");
		pageSizePanel.setLayout(new RowLayout());

		pageSizeComboBox = new Combo(pageSizePanel, SWT.NONE);
		pageSizeComboBox.setItems(new String[] {"20", "40", "60", "100", "All"});

		pageSizeComboBox.setText(pageSizeComboBox.getItem(1));
		this.pageSize = 40;

		pageSizeComboBox.addSelectionListener(this);

		// Sort panel

		Group sortPanel = new Group(upperPanel, SWT.NONE);
		sortPanel.setText("Sort");
		sortPanel.setLayout(new GridLayout(1, false));

		sortCheckBox = new Button(sortPanel, SWT.CHECK);
		sortCheckBox.setText("Show values sorted in page");
		sortCheckBox.addSelectionListener(this);

		data = new GridData();
		data.horizontalIndent = 5;
		sortCheckBox.setLayoutData(data);

		// Values panel

		Group valuesPanel = new Group(shell, SWT.NONE);
		valuesPanel.setText("Values");
		data = new GridData(GridData.FILL_BOTH);
		valuesPanel.setLayoutData(data);
		valuesPanel.setLayout(new GridLayout(2, false));

		// Navigation panel

		Composite navigationPanel = new Composite(valuesPanel, SWT.NONE);
		navigationPanel.setLayout(new GridLayout(3, false));
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		navigationPanel.setLayoutData(data);
		
		backButton = new Button(navigationPanel, SWT.NONE);
		backButton.setText("<< prev");
		pageLabel = new Label(navigationPanel, SWT.NONE);
		data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER,
			GridData.VERTICAL_ALIGN_CENTER,
			false, false, 1, 1);
		data.minimumWidth = 95;
		pageLabel.setLayoutData(data);
		nextButton = new Button(navigationPanel, SWT.NONE);
		nextButton.setText("next >>");

		backButton.addSelectionListener(this);
		nextButton.addSelectionListener(this);

		// Query panel

		Composite queryPanel = new Composite(valuesPanel, SWT.NONE);
		queryPanel.setLayout(new RowLayout());

		queryButton = new Button(queryPanel, SWT.NONE);
		queryButton.setText("Retrieve");
		queryButton.setToolTipText("Retrieve selected to graph");
		queryButton.addSelectionListener(this);

		queryTypeBox = new Combo(queryPanel, SWT.NONE);
		queryTypeBox.setItems(new String[]{
			"Neighbors in File",
			"Neighbors in Database"});

		tableScroll = new ScrolledComposite(valuesPanel, SWT.NONE);
		model = new ExperimentValuesTableModel(man);
		tableScroll.setExpandHorizontal(true);
		tableScroll.setExpandVertical(true);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.horizontalSpan = 2;
		tableScroll.setLayoutData(data);
		tableScroll.setLayout(new GridLayout(1, true));

		this.initRowData();
		this.filter();
	}

	private void initRowData()
	{
		this.data = new ArrayList<String>();
		this.dataToRow = new HashMap<String, Row>();

		for (Object o : this.man.getCed().getRow())
		{
			Row row = (Row) o;

			String d = "";

			for (Object ref : row.getRef())
			{
				d += new XRef((Reference) ref).toString() + " ";
			}

			d = d.trim();
			d = d.toLowerCase();

			this.data.add(d);
			this.dataToRow.put(d, row);
		}
	}

	//--------------------------------------------------------------------------
	// Section: Action handlers.
	//--------------------------------------------------------------------------

	public void widgetDefaultSelected(SelectionEvent event)
	{
		widgetSelected(event);
	}

	public void widgetSelected(SelectionEvent e)
	{
		Object source = e.getSource();

		if (source == this.filterButton)
		{
			this.filter();
		}
		else if (source == pageSizeComboBox)
		{
			this.updatePageSize();
		}
		else if (source == sortCheckBox)
		{
			this.updatePage();
		}
		else if (source == this.backButton)
		{
			this.pageNo--;
			this.updatePage();
		}
		else if (source == this.nextButton)
		{
			this.pageNo++;
			this.updatePage();
		}
		else if (source == this.queryButton)
		{
			List<XRef> refs = model.getSelectedReferences(table.getSelectionIndices());

			if (!refs.isEmpty())
			{
				int selection = queryTypeBox.getSelectionIndex();

				switch (selection)
				{
					case 0: // Neighbors in loaded file
						BioPAXGraph root = main.getRootGraph();
						if (root != null)
						{
							Collection<EntityHolder> entities = root.getRelatedEntities(refs);

							if (entities.isEmpty())
							{
								MessageDialog.openInformation(main.getShell(), "Not found",
									"Selected entities are not found in the current model.");
							}
							else
							{
								new GetNeighborhoodOfSelectedEntityAction(main, entities).run();
							}
						}
						else
						{
							MessageDialog.openInformation(main.getShell(), "No BioPAX model",
								"There is no current loaded BioPAX model.");
						}
						break;
					case 1: // Neighbors in Pathway commons
						new QueryNeighborsAction(main, refs).run();
						break;
				}
			}
			else
			{
				MessageDialog.openError(shell, "Error!",
					"You must select one CPATH or UNIPROT reference.");
			}
		}
	}

	//--------------------------------------------------------------------------
	// Section: Filtering and updating page.
	//--------------------------------------------------------------------------

	private void updatePageSize()
	{
		int newSize;

		String pageSizeString = pageSizeComboBox.getText();

		if (pageSizeString.equals("All"))
		{
			newSize = -1;
		}
		else
		{
			newSize = Integer.parseInt(pageSizeString);
		}

		if (newSize > 0 && pageSize > 0)
		{
			this.pageNo = (pageSize * pageNo) / newSize;
		}
		else
		{
			this.pageNo = 0;
		}
		this.pageSize = newSize;
		updatePage();
	}

	private void filter()
	{
		if (display == null)
		{
			display = new ArrayList<Row>();
		}
		else
		{
			this.display.clear();
		}

		List<String> params = this.getFilterParams();

		if (params.isEmpty())
		{
			this.display.addAll(this.man.getCed().getRow());
		}
		else
		{
			for (String d : data)
			{
				for (String p : params)
				{
					if (d.indexOf(p) >= 0)
					{
						Row row = dataToRow.get(d);
						display.add(row);
						break;
					}
				}
			}
		}

		this.pageNo = 0;
		this.updatePage();
	}

	private void updatePage()
	{
		if (page == null)
		{
			page = new ArrayList<Row>();
		}
		else
		{
			this.page.clear();
		}

		int totalPages;

		if (this.pageSize < 0)
		{
			this.page.addAll(this.display);
			totalPages = 1;
			this.pageNo = 0;
		}
		else
		{
			int size = display.size();
			int start = pageSize * pageNo;
			int end = start + pageSize - 1;

			if (size != 0)
			{
				assert start < size;

				int i = 0;
				for (Row row : display)
				{
					if (i >= start && i <= end)
					{
						page.add(row);
					}
					i++;
				}
			}
			totalPages = (int) Math.ceil(size / (double) pageSize);
		}

		this.model.updateRows(page);

		if (this.sortCheckBox.getSelection())
		{
			this.model.sort(this.model.getValueIndex());
		}

		updatePageLabel(pageNo+1, totalPages);

		this.backButton.setEnabled((pageNo > 0));
		this.nextButton.setEnabled((pageNo < totalPages - 1));

		if (table != null && !table.isDisposed())
		{
			table.dispose();
		}

		createTable(tableScroll);
		tableScroll.setContent(table);
		
		shell.pack();
	}

	private void updatePageLabel(int pageNo, int totalPages)
	{
		if (totalPages < 2)
		{
			pageLabel.setText("");
		}
		else
		{
			String lbl = "page " + pageNo + " of " + totalPages;

			if (totalPages >= 1000 && pageNo < 1000) lbl = " " + lbl + " ";
			if (totalPages >= 100 && pageNo < 100) lbl = " " + lbl + " ";
			if (totalPages >= 10 && pageNo < 10) lbl = " " + lbl + " ";

			pageLabel.setText(lbl);
		}
	}

	private List<String> getFilterParams()
	{
		String t = filterField.getText();
		t = t.toLowerCase();

		String[] array = t.split(" or ");

		List<String> params = new ArrayList<String>();

		for (String s : array)
		{
			s = s.trim();
			if (s.length() > 1)
			{
				params.add(s);
			}
		}
		return params;
	}

	public void createTable(Composite c)
	{
		table = new Table(c, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL |
			SWT.FULL_SELECTION);

		GridData data = new GridData(GridData.FILL_BOTH | GridData.CENTER |
			GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		table.setLayoutData(data);
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		table.addSelectionListener(ExperimentDataVisualizationDialog.this);

		createColumns();
		createViewer();
		colorColumns();
	}

	private void createColumns()
	{
		String[] columnNames = model.getColumnNames();

		for (int i = 0; i < model.getColumnCount(); i++)
		{
            TableColumn column = new TableColumn(table, SWT.LEFT, i);
			column.setText(columnNames[i]);
			column.setWidth(i == 0 ? 150 : 55);
		}
	}
	private void createViewer()
	{
		TableViewer viewer = new TableViewer(table);
		viewer.setUseHashlookup(true);

		String[] columnNames = model.getColumnNames();

		viewer.setColumnProperties(columnNames);

		// Create the cell editors
		CellEditor[] editors = new CellEditor[columnNames.length];

		for (int i = 0; i < editors.length; i++)
		{
			TextCellEditor textEditor = new TextCellEditor(table);
			editors[i] = textEditor;
		}

		// Assign the cell editors to the viewer
		viewer.setCellEditors(editors);
		// Set the cell modifier for the viewer
		viewer.setCellModifier(new CellModifier());

		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new LabelProvider());

		// The input for the table viewer is the instance of ExampleTaskList
		viewer.setInput(model.getDisplayArray());
	}

	private void colorColumns()
	{
		for (TableItem item : table.getItems())
		{
			for (int i = 0; i < model.getColumnCount(); i++)
			{
				item.setBackground(i, model.getColumnColor(i));
			}
		}
	}

	private class CellModifier implements ICellModifier
	{
		public boolean canModify(Object o, String s)
		{
			return false;
		}

		public Object getValue(Object o, String s)
		{
			return null;
		}

		public void modify(Object o, String s, Object o1){}
	}

	private class ContentProvider implements IStructuredContentProvider
	{
		public void dispose(){}
		public void inputChanged(Viewer viewer, Object o, Object o1){}

		public Object[] getElements(Object o)
		{
			return model.getDisplayArray();
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
			return model.getValueAt(model.getRowIndex((Row) o), i).toString();
		}
	}
}
