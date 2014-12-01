package org.gvt.gui;

import org.biopax.paxtools.pattern.miner.SIFType;
import org.cbio.causality.analysis.Graph;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.gvt.ChisioMain;

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
public class EnrichmentInSIFParameterDialog extends Dialog
{
	/**
	 * Supported rules.
	 */
	private List<SIFType> possibleRules;

	/**
	 * Rule types that user selected.
	 */
	private List<SIFType> selectedRules;

	/**
	 * Provides a mapping from tag of the rule to the rule type.
	 */
	private Map<String, SIFType> ruleTagMap;

	private String backgroundfile;
	private String genefile;
	private Double fdr;

	private boolean okPressed;

	/**
	 * Parent shell.
	 */
	private Shell shell;

	private Button okButton;
	private Button cancelButton;

	private Button bgBrowseButton;
	private Button geneBrowseButton;
	private Text bgText;
	private Text geneText;
	private Text fdrText;

	private Combo neighTypeCombo;
	private int neighTypeSelectionIndex = 2;

	/**
	 * Constructor.
	 *
	 * @param shell parent
	 * @param possibleRules rule types that user might to use
	 * @param selectedRules list of checked rules, may be empty when passed, will be filled by user
	 */
	public EnrichmentInSIFParameterDialog(Shell shell,
		List<SIFType> possibleRules,
		List<SIFType> selectedRules,
		String genefile,
		String backgroundfile,
		Double fdr)
	{
		super(shell);
		this.possibleRules = possibleRules;
		this.selectedRules = selectedRules;
		this.backgroundfile = backgroundfile;
		this.genefile = genefile;
		this.fdr = fdr;

		this.ruleTagMap = new HashMap<String, SIFType>();
		for (SIFType rule : possibleRules)
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
		shell.setText("Paths Between Query on SIF File");
        shell.setImage(ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbe-icon.png").createImage());
		shell.setLayout(new GridLayout());

        Label infoLabel = new Label(shell, SWT.NONE);
        infoLabel.setText("Find the sub-network that contains\nnodes with enriched neighborhood ");
        infoLabel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, 1,8));

		ButtonAdapter adapter = new ButtonAdapter();

		Group geneGroup = new Group(shell, SWT.NONE);
		geneGroup.setText("Query genes file name");
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

		Group bgGroup = new Group(shell, SWT.NONE);
		bgGroup.setText("Background genes file name");
		bgGroup.setLayout(new RowLayout());
		bgText = new Text(bgGroup, SWT.SINGLE | SWT.BORDER);
		bgText.setLayoutData(new RowData(200, 20));
		if (backgroundfile != null)
		{
			bgText.setText(backgroundfile);
		}
		bgBrowseButton = new Button(bgGroup, SWT.PUSH);
		bgBrowseButton.setText("Browse");
		bgBrowseButton.addSelectionListener(adapter);

		Group fdrGroup = new Group(shell, SWT.NONE);
		fdrGroup.setLayout(new RowLayout());
		fdrGroup.setText("False discovery rate");
		fdrText = new Text(fdrGroup, SWT.SINGLE | SWT.BORDER);
		if (fdr != null) fdrText.setText(fdr.toString());
		fdrText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent modifyEvent)
			{
				checkButtons();
			}
		});

		Group neighTypeGroup = new Group(shell, SWT.NONE);
		neighTypeGroup.setLayout(new RowLayout());
		neighTypeGroup.setText("Neighborhood type");
		neighTypeCombo = new Combo(neighTypeGroup, SWT.NONE);
		Graph.NeighborType[] types = Graph.NeighborType.values();
		String[] s = new String[types.length];
		for (int i = 0; i < s.length; i++)
		{
			s[i] = types[i].name().toLowerCase();
		}
		neighTypeCombo.setItems(s);
		neighTypeCombo.select(neighTypeSelectionIndex);
		neighTypeCombo.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent modifyEvent)
			{
				checkButtons();
			}
		});

		Group rulesGroup = new Group(shell, SWT.NONE);
		rulesGroup.setLayout(new GridLayout());
		rulesGroup.setText("Select rules to use");

		for (SIFType rule : possibleRules)
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

	public String getBackgroundfile()
	{
		return backgroundfile;
	}

	public Double getFdr()
	{
		return fdr;
	}

	private void checkButtons()
	{
		okButton.setEnabled(!selectedRules.isEmpty() &&
			(bgText.getText().isEmpty() ||
				(bgText.getText().length() > 0 && new File(bgText.getText()).exists())) &&
			geneText.getText().length() > 0 && new File(geneText.getText()).exists() &&
			readFDR() != null);

		neighTypeSelectionIndex = neighTypeCombo.getSelectionIndex();
	}

	private Double readFDR()
	{
		try
		{
			return new Double(fdrText.getText());
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public int getNeighTypeIndex()
	{
		return neighTypeSelectionIndex;
	}

	class ButtonAdapter extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent arg)
		{
			Button button = (Button) arg.widget;

			if ((button.getStyle() & SWT.CHECK) > 0)
			{
				String ruleTag = button.getText();
				SIFType rule = ruleTagMap.get(ruleTag);

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
					fdr = readFDR();
					okPressed = true;
					shell.dispose();
				}
				else if (button == cancelButton)
				{
					okPressed = false;
					shell.dispose();
				}
				else if (button == bgBrowseButton)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					if (backgroundfile != null) dialog.setFileName(backgroundfile);
					dialog.setFilterNames(new String[]{"Simple Interaction Format (*.sif)"});
					dialog.setFilterExtensions(new String[]{"*.sif"});
					String file = dialog.open();
					if (file != null)
					{
						backgroundfile = file;
						bgText.setText(backgroundfile);
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