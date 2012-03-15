package org.gvt.gui;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class GOIofSIFParameterDialog extends Dialog
{
	/**
	 * Supported rules.
	 */
	private List<BinaryInteractionType> possibleRules;

	/**
	 * Rule types that user selected.
	 */
	private List<BinaryInteractionType> selectedRules;

	/**
	 * Provides a mapping from tag of the rule to the rule type.
	 */
	private Map<String, BinaryInteractionType> ruleTagMap;

	private String siffile;
	private String genefile;
	private Boolean directed;
	private Integer limit;

	private boolean okPressed;

	/**
	 * Parent shell.
	 */
	private Shell shell;

	private Button okButton;
	private Button cancelButton;

	private Button sifBrowseButton;
	private Button geneBrowseButton;
	private Button directedButton;
	private Text sifText;
	private Text geneText;
	private Combo limitCombo;

	/**
	 * Constructor.
	 *
	 * @param shell parent
	 * @param possibleRules rule types that user might to use
	 * @param selectedRules list of checked rules, may be empty when passed, will be filled by user
	 */
	public GOIofSIFParameterDialog(Shell shell,
		List<BinaryInteractionType> possibleRules,
		List<BinaryInteractionType> selectedRules,
		String siffile,
		String genefile,
		Integer limit,
		Boolean directed)
	{
		super(shell);
		this.possibleRules = possibleRules;
		this.selectedRules = selectedRules;
		this.siffile = siffile;
		this.genefile = genefile;
		this.limit = limit;
		this.directed = directed;

		this.ruleTagMap = new HashMap<String, BinaryInteractionType>();
		for (BinaryInteractionType rule : possibleRules)
		{
			ruleTagMap.put(rule.getTag(), rule);
		}

		okPressed = false;
	}

	/**
	 * Open the dialog
	 */
	public boolean open()
	{
		createContents();
		shell.pack();

		shell.setLocation(
			getParent().getLocation().x + (getParent().getSize().x / 2) -
				(shell.getSize().x / 2),
			getParent().getLocation().y + (getParent().getSize().y / 2) -
				(shell.getSize().y / 2));

		shell.open();
		Display display = getParent().getDisplay();

		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}

		return okPressed;
	}

	private void createContents()
	{
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Find GoI on SIF File");
		shell.setLayout(new GridLayout());

		ButtonAdapter adapter = new ButtonAdapter();

		Group sifGroup = new Group(shell, SWT.NONE);
		sifGroup.setText("SIF graph file name");
		sifGroup.setLayout(new RowLayout());
		sifText = new Text(sifGroup, SWT.SINGLE | SWT.BORDER);
		sifText.setLayoutData(new RowData(200, 20));
		if (siffile != null)
		{
			sifText.setText(siffile);
		}
		sifBrowseButton = new Button(sifGroup, SWT.PUSH);
		sifBrowseButton.setText("Browse");
		sifBrowseButton.addSelectionListener(adapter);

		Group geneGroup = new Group(shell, SWT.NONE);
		geneGroup.setText("Genes-of-interest file name");
		geneGroup.setLayout(new RowLayout());
		geneText = new Text(geneGroup, SWT.SINGLE | SWT.BORDER);
		geneText.setLayoutData(new RowData(200, 20));
		if (genefile != null)
		{
			geneText.setText(genefile);
		}
		geneBrowseButton = new Button(geneGroup, SWT.PUSH);
		geneBrowseButton.setText("Browse");
		geneBrowseButton.addSelectionListener(adapter);

		Group paramsGroup = new Group(shell, SWT.NONE);
		paramsGroup.setLayout(new RowLayout());
		paramsGroup.setText("Parameters");
		new Label(paramsGroup, SWT.NONE).setText("Search distance: ");
		limitCombo = new Combo(paramsGroup, SWT.READ_ONLY);
		limitCombo.setItems(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});
		limitCombo.select(limit != null? limit : 2);
		Label stub = new Label(paramsGroup, SWT.NONE);
		stub.setLayoutData(new RowData(70, 20));
		directedButton = new Button(paramsGroup, SWT.CHECK);
		directedButton.setText("Directed");
		directedButton.setSelection(directed != null ? directed : false);
		directedButton.setEnabled(false);

		Group rulesGroup = new Group(shell, SWT.NONE);
		rulesGroup.setLayout(new GridLayout());
		rulesGroup.setText("Select rules to use");

		for (BinaryInteractionType rule : possibleRules)
		{
			Button ruleBox = new Button(rulesGroup, SWT.CHECK);
			ruleBox.setText(rule.getTag());
			ruleBox.setToolTipText(prepareToolTipText(rule.getDescription()));
			ruleBox.addSelectionListener(adapter);

			if (selectedRules.contains(rule))
			{
				ruleBox.setSelection(true);
			}
		}

		Composite buttonsGroup = new Composite(shell, SWT.NONE);
		buttonsGroup.setLayout(new RowLayout());

		okButton = new Button(buttonsGroup, SWT.NONE);
		okButton.setText("OK");
		okButton.addSelectionListener(adapter);

		cancelButton = new Button(buttonsGroup, SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(adapter);

		checkButtons();
	}

	private String prepareToolTipText(String text)
	{
		String tooltip = "";
		String line = "";

		StringTokenizer tokenizer = new StringTokenizer(text);
		while (tokenizer.hasMoreTokens())
		{
			String token = (line.length() == 0 ? "" : " ") + tokenizer.nextToken();
			tooltip += token;
			line += token;

			if (line.length() > 50)
			{
				tooltip += "\n";
				line = "";
			}
		}
		return tooltip;
	}

	public String getGenefile()
	{
		return genefile;
	}

	public String getSiffile()
	{
		return siffile;
	}

	public Integer getLimit()
	{
		return limit;
	}

	public Boolean getDirected()
	{
		return directed;
	}

	private void checkButtons()
	{
		okButton.setEnabled(!selectedRules.isEmpty() &&
			sifText.getText().length() > 0 && new File(sifText.getText()).exists() &&
			geneText.getText().length() > 0 && new File(geneText.getText()).exists());

		if (directedButton.getSelection() == undirectedRuleSelected())
		{
			limit = limitCombo.getSelectionIndex();

			directedButton.setSelection(!directedButton.getSelection());

			limitCombo.setItems(directedButton.getSelection() ?
				new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"} :
				new String[]{"0", "2", "4", "6", "8"});

			limitCombo.select(directedButton.getSelection() ? limit * 2 : limit / 2);
		}
	}

	private boolean undirectedRuleSelected()
	{
		for (BinaryInteractionType type : selectedRules)
		{
			if (!type.isDirected()) return true;
		}
		return false;
	}

	class ButtonAdapter extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent arg)
		{
			Button button = (Button) arg.widget;

			if ((button.getStyle() & SWT.CHECK) > 0)
			{
				String ruleTag = button.getText();
				BinaryInteractionType rule = ruleTagMap.get(ruleTag);

				if (button.getSelection())
				{
					assert !selectedRules.contains(rule);
					selectedRules.add(rule);
				}
				else
				{
					assert selectedRules.contains(rule);
					selectedRules.remove(rule);
				}
				checkButtons();
			}
			else
			{
				if (button == okButton)
				{
					directed = directedButton.getSelection();
					limit = limitCombo.getSelectionIndex();

					okPressed = true;
					shell.dispose();
				}
				else if (button == cancelButton)
				{
					okPressed = false;
					shell.dispose();
				}
				else if (button == sifBrowseButton)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					if (siffile != null) dialog.setFileName(siffile);
					dialog.setFilterNames(new String[]{"Simple Interaction Format (*.sif)"});
					dialog.setFilterExtensions(new String[]{"*.sif"});
					String file = dialog.open();
					if (file != null)
					{
						siffile = file;
						sifText.setText(siffile);
					}
					checkButtons();
				}
				else if (button == geneBrowseButton)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					if (genefile != null) dialog.setFileName(genefile);
					dialog.setFilterNames(new String[]{"Genes of Interest List (*.txt)"});
					dialog.setFilterExtensions(new String[]{"*.txt"});
					String file = dialog.open();
					if (file != null)
					{
						genefile = file;
						geneText.setText(genefile);
					}
					checkButtons();
				}
			}
		}
	}
}