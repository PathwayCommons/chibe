package org.gvt.gui;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;
import org.gvt.ChisioMain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ExportToSIFDialog extends Dialog
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

	private boolean okPressed;

	/**
	 * Parent shell.
	 */
	private Shell shell;

	private Button okButton;
	private Button cancelButton;

	/**
	 * Constructor.
	 *
	 * @param shell parent
	 * @param possibleRules rule types that user might to use
	 * @param selectedRules list of checked rules, may be empty when passed, will be filled by user
	 */
	public ExportToSIFDialog(Shell shell,
		List<BinaryInteractionType> possibleRules,
		List<BinaryInteractionType> selectedRules)
	{
		super(shell);
		this.possibleRules = possibleRules;
		this.selectedRules = selectedRules;

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
		shell.setText("SIF Rules Settings");
		GridLayout gridLy = new GridLayout();
		gridLy.numColumns = 1;
		shell.setLayout(gridLy);

		Group rulesGroup = new Group(shell, SWT.NONE);
		rulesGroup.setText("Select rules to use");
		gridLy = new GridLayout();
		gridLy.numColumns = 1;
		rulesGroup.setLayout(gridLy);
		GridData gridDt = new GridData();
		gridDt.grabExcessVerticalSpace = true;
		rulesGroup.setLayoutData(gridDt);

		ButtonAdapter adapter = new ButtonAdapter();

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

		Composite buttons = new Composite(shell, SWT.NONE);
		buttons.setLayout(new RowLayout());
		gridDt = new GridData();
		gridDt.horizontalAlignment = GridData.HORIZONTAL_ALIGN_CENTER;
		buttons.setLayoutData(gridDt);

		okButton = new Button(buttons, SWT.NONE);
		okButton.setText("OK");
		okButton.addSelectionListener(adapter);
		okButton.setEnabled(!selectedRules.isEmpty());

		cancelButton = new Button(buttons, SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(adapter);
	}

	private String prepareToolTipText(String text)
	{
		text = text.trim();
		if (text.endsWith("\n"))
		{
			text = text.substring(0, text.length() - 1);
		}

		String tooltip = "";
		String line = "";

		StringTokenizer tokenizer = new StringTokenizer(text);
		while (tokenizer.hasMoreTokens())
		{
			String token = (line.length() == 0 ? "" : " ") + tokenizer.nextToken();
			tooltip += token;
			line += token;

			if (line.length() > 50 && tokenizer.hasMoreTokens())
			{
				tooltip += "\n";
				line = "";
			}
		}
		return tooltip;
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
				okButton.setEnabled(!selectedRules.isEmpty());
			}
			else
			{
				if (button == okButton)
				{
					okPressed = true;
					shell.dispose();
				}
				else if (button == cancelButton)
				{
					okPressed = false;
					shell.dispose();
				}
			}
		}
	}
}
