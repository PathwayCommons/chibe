package org.gvt.inspector;

import java.util.*;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.model.*;
import org.gvt.action.ChangeMarginAction;
import org.gvt.ChisioMain;
import org.gvt.util.SystemBrowserDisplay;

/**
 * This class maintains the base inspector window. For edges, nodes, compound
 * nodes and graphs, this class is dervied and used with necessary attributes.
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public abstract class Inspector extends Dialog
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	// Font of Table
//	protected Font tableFont;
	// The table which is shown on the inspector window to choose attributes
	protected Table table;
	// This inspector windows owner model. Properties are belong to this object.
	protected GraphObject model;

	protected ChisioMain main;

	protected static List<Inspector> instances = new ArrayList();

	protected Shell shell;

	protected Font newFont;

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
				|| keyCode == 8
				|| keyCode == SWT.ARROW_LEFT
				|| keyCode == SWT.ARROW_RIGHT)
			{
				return true;
			}
			return false;
		}
	};

// -----------------------------------------------------------------------------
// Section: Class methods.
// -----------------------------------------------------------------------------
	protected Inspector(GraphObject model, String title, ChisioMain main)
	{
		super(main.getShell(), SWT.NONE);
		this.main = main;
		
		this.shell = new Shell(main.getShell(), SWT.DIALOG_TRIM | SWT.RESIZE);
		this.shell.setText(title + " Properties");

		ImageDescriptor id = ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbe-icon.png");
		shell.setImage(id.createImage());

		GridLayout layout = new GridLayout();
		this.shell.setLayout(layout);

		final ScrolledComposite sc = new ScrolledComposite(shell,
			SWT.V_SCROLL | SWT.H_SCROLL | SWT.RESIZE);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.heightHint = 400;
		sc.setLayoutData(gridData);

		this.model = model;
		this.table = createTable(sc);
		this.newFont = model.getTextFont();
	}

	/**
	 * This method creates the table and initializes it.
	 */
	public Table createTable(final ScrolledComposite sc)
	{
		final Table table = new Table(sc, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL |
			SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.RESIZE);
		table.setLinesVisible(true);

		sc.setContent(table);
		
		final TableColumn attributes = new TableColumn(table, SWT.LEFT, 0);
		attributes.pack();
		final TableColumn values = new TableColumn(table, SWT.LEFT, 1);
		values.pack();

		return table;
	}

	/**
	 * This method creates a TableItem and adds into table.
	 */
	public TableItem addRow(Table table, String label)
	{
		// Create the row
		final TableItem item = new TableItem(table, SWT.NONE);

		item.setText(0, label);
		table.getColumn(0).pack();

		// It is done to set height of row. There is no way to set height.
//		item.setImage(new Image(null, 1, rowHeight));
//		table.getColumn(0).setWidth(col0Width);
//		table.getColumn(1).setWidth(col1Width);
//
//		// Create the editor and text
//		TableEditor itemEditor = new TableEditor(table);
//		Label itemText = new Label(table, SWT.PUSH);
//		// Set attributes of the text
//		item.setText(label);
//		itemText.setText(label);
//		itemText.setBackground(ColorConstants.white);
//
//		// Set attributes of the editor
//		itemEditor.grabHorizontal = true;
//		itemEditor.minimumHeight = itemText.getSize().y;
//		itemEditor.minimumWidth = itemText.getSize().x;
//
//		// Set the editor for the first column in the row
//		itemEditor.setEditor(itemText, item, 0);
//
		if (item.getText(0).equals("Reference"))
		{
			item.setForeground(1, new Color(shell.getDisplay(), hyperlinkRGB));
		}

		return item;
	}

	/**
	 * This method creates the interactions in second column. Adds a mouse
	 * listener to table and detects the selected row. according to row, a
	 * combo box, or a color dialog or only an editable text field is shown.
	 *
	 * @param shell
	 */
	public void createContents(final Shell shell)
	{
		// Create an editor object to use for text editing
		final TableEditor editor = new TableEditor(table);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.grabVertical = true;

		// Use a selection listener to get seleceted row
		table.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				// Dispose any existing editor
				Control old = editor.getEditor();

				if (old != null)
				{
					old.dispose();
				}

				// Determine which row was selected
				final TableItem item = (TableItem) event.item;

				if (item != null)
				{
					// COMBO
					if (item.getText().equals("Style")
						|| item.getText().equals("Arrow")
						|| item.getText().equals("Shape"))
					{
						// Create the dropdown and add data to it
						final CCombo combo = new CCombo(table, SWT.READ_ONLY);

						if (item.getText().equals("Style"))
						{
							String[] styleOfEdge = {"Solid", "Dashed"};
							combo.setItems(styleOfEdge);
						}
						else if (item.getText().equals("Arrow"))
						{
							String[] arrowOfEdge = {"None",
								"Source",
								"Target",
								"Both",
                                "Modulation",
                                "Stimulation",
                                "Catalysis",
                                "Inhibition"};
							combo.setItems(arrowOfEdge);
						}
						else if (item.getText().equals("Shape"))
						{
							 combo.setItems(NodeModel.shapes);
						}

						// Select the previously selected item from the cell
						combo.select(combo.indexOf(item.getText(1)));
//						combo.setFont(tableFont);
						editor.setEditor(combo, item, 1);

						// Add a listener to set the selected item back into the
						// cell
						combo.addSelectionListener(new SelectionAdapter()
						{
							public void widgetSelected(SelectionEvent event)
							{
								item.setText(1, combo.getText());
                                // They selected an item; end the editing
								// session
								combo.dispose();
							}
						});
					}

					// TEXT
					else if (item.getText().equals("Text"))
					{
						// Create the Text object for our editor
						final Text text = new Text(table, SWT.LEFT);
						// text.setForeground(item.getForeground());

						// Transfer any text from the cell to the Text control,
						// set the color to match this row, select the text,
						// and set focus to the control
						text.setText(item.getText(1));
//						text.setFont(tableFont);

						// text.setForeground(item.getForeground());
						text.selectAll();
						text.setFocus();
						editor.setEditor(text, item, 1);

						// Add a handler to transfer the text back to the cell
						// any time it's modified
						text.addModifyListener(new ModifyListener()
						{
							public void modifyText(ModifyEvent event)
							{
								// Set the text of the editor's control back
								// into the cell
								item.setText(1, text.getText());
							}
						});
					}

					// NUMBER
					else if (item.getText().equals("Margin")
						|| item.getText().equals("Cluster ID")
						|| item.getText().equals("Width"))
					{
						// Create the Text object for our editor
						final Text text = new Text(table, SWT.LEFT);
						// text.setForeground(item.getForeground());

						// Transfer any text from the cell to the Text control,
						// set the color to match this row, select the text,
						// and set focus to the control
						text.setText(item.getText(1));
//						text.setFont(tableFont);

						// text.setForeground(item.getForeground());
						text.selectAll();
						text.setFocus();
						editor.setEditor(text, item, 1);

						// Add a handler to transfer the text back to the cell
						// any time it's modified
						text.addModifyListener(new ModifyListener()
						{
							public void modifyText(ModifyEvent event)
							{
								// Set the text of the editor's control back
								// into the cell
								item.setText(1, text.getText());
							}
						});

						text.addKeyListener(keyAdapter);
					}

					// COLOR
					else if (item.getText().equals("Border Color")
						|| item.getText().equals("Color")
						|| item.getText().equals("Highlight Color"))
					{
						ColorDialog dialog = new ColorDialog(shell);
						dialog.setRGB(item.getBackground(1).getRGB());
						RGB rgb = dialog.open();

						if (rgb != null)
						{
							item.setBackground(1,
								new Color(shell.getDisplay(), rgb));
						}
					}

					// FONT
					else if (item.getText().equals("Text Font"))
					{
						FontDialog dlg = new FontDialog(shell);

						// Pre-fill the dialog with any previous selection
						dlg.setFontList(newFont.getFontData());
						dlg.setRGB(item.getForeground(1).getRGB());

						if (dlg.open() != null)
						{
						  // Create the new font and set it into the label
							newFont =
								new Font(shell.getDisplay(), dlg.getFontList());

							String name = newFont.getFontData()[0].getName();
							int size = newFont.getFontData()[0].getHeight();
							int style = newFont.getFontData()[0].getStyle();

							if (size > 14)
							{
								size = 14;
							}

							item.setText(1, name);
							item.setFont(1,	new Font(null, name, size, style));
							item.setForeground(1,
								new Color(shell.getDisplay(), dlg.getRGB()));
						}
					}
					else if (item.getText().equals("Reference"))
					{
						String[] s = item.getText(1).split(":");

						String hpl = null;

						if (s[0].equalsIgnoreCase("GO") || s[0].equalsIgnoreCase("GENE_ONTOLOGY") ||
							s[0].equalsIgnoreCase("GENE ONTOLOGY"))
						{
							hpl = "http://amigo.geneontology.org/cgi-bin/amigo/term-details.cgi?term=GO:" + s[1];
						}
						else if (s[0].equalsIgnoreCase("Reactome"))
						{
							if (s[1].startsWith("R"))
							{
								hpl = "http://www.reactome.org/cgi-bin/eventbrowser_st_id?ST_ID=" + s[1];
							}
							else
							{
								hpl = "http://www.reactome.org/cgi-bin/eventbrowser?DB=gk_current&ID=" + s[1];
							}
						}
						else if (s[0].equalsIgnoreCase("UniProt"))
						{
							hpl = "http://www.uniprot.org/uniprot/" + s[1];
						}
						else if (s[0].equalsIgnoreCase("REF_SEQ") ||
							s[0].equalsIgnoreCase("REFSEQ"))
						{
							hpl = "http://www.ncbi.nlm.nih.gov/sites/gquery?term=" + s[1];
						}
						else if (s[0].equalsIgnoreCase("CPATH"))
						{
							hpl = "http://www.pathwaycommons.org/pc/record2.do?id=" + s[1];
						}
						else if (s[0].equalsIgnoreCase("ENTREZ_GENE") ||
							s[0].equalsIgnoreCase("ENTREZGENE"))
						{
							hpl = "http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene&term=" + s[1];
						}
						else if (s[0].equalsIgnoreCase("chebi"))
						{
							hpl = "http://www.ebi.ac.uk/chebi/advancedSearchFT.do?searchString=" + s[1];
						}
						else if (s[0].equalsIgnoreCase("GENE_SYMBOL"))
						{
							hpl = "http://www.genenames.org/data/hgnc_data.php?gd_app_sym=" + s[1];
						}
						else if (s[0].equalsIgnoreCase("PUBMED"))
						{
							hpl = "http://www.ncbi.nlm.nih.gov/pubmed/" + s[1];
						}
						else if (s[0].equalsIgnoreCase("NCBI_TAXONOMY") ||
							s[0].equalsIgnoreCase("NCBI TAXONOMY"))
						{
							hpl = "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=" + s[1];
						}
						else if (s[0].equalsIgnoreCase("HumanCyc"))
						{
							hpl = "http://biocyc.org/HUMAN/NEW-IMAGE?object=" + s[1];
						}
						else if (s[0].equalsIgnoreCase("LIGAND") || s[0].equalsIgnoreCase("COMPOUND"))
						{
							hpl = "http://www.genome.jp/dbget-bin/www_bget?compound+" + s[1];
						}
						else if (s[0].equalsIgnoreCase("INTERPRO"))
						{
							hpl = "http://www.ebi.ac.uk/interpro/ISearch?query=" + s[1];
						}
						else if (s[0].equalsIgnoreCase("ENSEMBL"))
						{
							hpl = "http://www.ensembl.org/Homo_sapiens/Search/Summary?species=all;idx=;q=" + s[1];
						}

						if (hpl != null) SystemBrowserDisplay.openURL(hpl);
					}

					table.setSelection(-1);
				}
			}
		});

		Composite buttons = new Composite(shell, SWT.NONE);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.makeColumnsEqualWidth = true;

		buttons.setLayout(gridLayout);

		final Button okButton = new Button(buttons, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(final SelectionEvent e)
			{
				TableItem[] items = table.getItems();

				for (TableItem item : items)
				{
					if (item.getText().equals("Text"))
					{
						model.setText(item.getText(1));
					}
					else if (item.getText().equals("Text Font"))
					{
						model.setTextFont(newFont);
						model.setTextColor(item.getForeground(1));
					}
					else if (item.getText().equals("Color"))
					{
						model.setColor(item.getBackground(1));
					}
					else if (item.getText().equals("Border Color"))
					{
						((NodeModel) model).
							setBorderColor(item.getBackground(1));
					}
					else if (item.getText().equals("Highlight Color"))
					{
						ChisioMain.higlightColor = item.getBackground(1);
					}
					else if (item.getText().equals("Shape"))
					{
						((NodeModel) model).setShape(item.getText(1));
					}
					else if (item.getText().equals("Style"))
					{
						((EdgeModel) model).setStyle(item.getText(1));
					}
					else if (item.getText().equals("Arrow"))
					{
						((EdgeModel) model).setArrow(item.getText(1));
					}
					else if (item.getText().equals("Width"))
					{
						((EdgeModel) model).
							setWidth(Integer.parseInt(item.getText(1)));
					}
					else if (item.getText().equals("Margin"))
					{
						new ChangeMarginAction((CompoundModel)model,
							Integer.parseInt(item.getText(1))).run();
					}
				}

				shell.close();
			}
		});

		okButton.setText("OK");
		final Button cancelButton = new Button(buttons, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(final SelectionEvent e)
			{
				shell.close();
			}
		});

		cancelButton.setText("Cancel");

		GridData data = new GridData();
		data.horizontalAlignment = SWT.CENTER;
		buttons.setLayoutData(data);
		
		int numOfRow = table.getItemCount();
		int heightOfRow = numOfRow > 0 ? table.getItem(0).getBounds(0).height + 1 : 0;

		shell.setSize(INSPECTOR_WIDTH, Math.min(115 + (numOfRow * heightOfRow), MAX_HEIGHT));
	}

	protected static boolean isSingle(GraphObject model)
	{
		if (instances.size() == 0)
		{
			return true;
		}
		else
		{
			for (Inspector inspector : instances)
			{
				if (inspector.model.equals(model))
				{
					if (inspector.shell.isDisposed())
					{
						instances.remove(inspector);
						return true;
					}

					inspector.shell.forceActive();

					return false;
				}
			}

			return true;
		}
	}

	public Point calculateInspectorLocation(int clickLocX, int clickLocY)
	{
		Point loc = shell.getParent().getShell().getLocation();

		Point inspectorLoc = new Point(clickLocX + 10, clickLocY + 80);

		int height = shell.getParent().getSize().y;
		int width = shell.getParent().getSize().x;
		int diffY = inspectorLoc.y + shell.getSize().y - height;
	  	int diffX = inspectorLoc.x + shell.getSize().x - width;

		if (diffY < 0)
		{
			diffY = 0;
		}

		if (diffX < 0)
		{
			diffX = 0;
		}

		return new Point (loc.x + inspectorLoc.x - diffX,
			loc.y + inspectorLoc.y - diffY);
	}

// ---------------------------------------------------------------------------
// Section: Class Variables
// ---------------------------------------------------------------------------
	protected static int col0Width = 75;

	protected static int col1Width = 250;

	protected static int rowHeight = 20;

	protected static int MAX_HEIGHT = 500;
	protected static int INSPECTOR_WIDTH = 350;

	protected static final RGB hyperlinkRGB = new RGB(0, 20, 120);
}