package org.gvt.gui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.layout.CellData;
import org.eclipse.ui.internal.layout.CellLayout;
import org.gvt.ChisioMain;

import java.util.*;
import java.util.List;

/**
 * This dialog retrieves MutSig and Gistic genes of a selected Study.
 * @author Ozgun Babur
 */
public class EnrichedReactionsDialog extends Dialog
{
	private List<String> genes;
	private List<String> studyCodes;

	/**
	 * Parent shell.
	 */
	private Shell shell;

	private Button okButton;
	private Button cancelButton;
	private Button tcgaButton;
	private Text genesText;

	/**
	 * Constructor.
	 *
	 * @param shell parent
	 */
	public EnrichedReactionsDialog(Shell shell)
	{
		super(shell);
	}

	/**
	 * Open the dialog. Also returns the selected item if only one is selected.
	 */
	public List<String> open()
	{
		createContents();
		shell.pack();
		shell.setLocation(
			getParent().getLocation().x + (getParent().getSize().x / 2) -
				(shell.getSize().x / 2),
			getParent().getLocation().y + (getParent().getSize().y / 2) -
				(shell.getSize().y / 2));

        shell.setImage(ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbe-icon.png").
			createImage());

		shell.open();

		Display display = getParent().getDisplay();

		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}

		return genes;
	}

	private void createContents()
	{
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setLayout(new CellLayout(1));
		shell.setText("Find enriched reactions");

		shell.setMinimumSize(200, 200);

		ButtonAdapter adapter = new ButtonAdapter();

		Group middleGroup = new Group(shell, SWT.NONE);
		middleGroup.setLayout(new CellLayout(1));
		middleGroup.setText("Enter gene symbols to be enriched with");

		tcgaButton = new Button(middleGroup, SWT.PUSH);
		tcgaButton.setText("Use TCGA genes");
		CellData data = new CellData();
		data.align(SWT.RIGHT, SWT.CENTER);
		tcgaButton.setLayoutData(data);
		tcgaButton.addSelectionListener(adapter);

		genesText = new Text(middleGroup, SWT.MULTI | SWT.WRAP);
//		genesText.setSize(200, 200);
		data = new CellData();
		data.widthHint = 400;
		data.heightHint = 100;
		genesText.setLayoutData(data);

		Composite buttonsGroup = new Composite(shell, SWT.NONE);
		buttonsGroup.setLayout(new CellLayout(2));

		okButton = new Button(buttonsGroup, SWT.NONE);
		okButton.setText("OK");
		okButton.addSelectionListener(adapter);
		data = new CellData();
		data.align(SWT.RIGHT, SWT.CENTER);
		okButton.setLayoutData(data);

		cancelButton = new Button(buttonsGroup, SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(adapter);
		data = new CellData();
		data.align(SWT.LEFT, SWT.CENTER);
		cancelButton.setLayoutData(data);
	}

	class ButtonAdapter extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent arg)
		{
			Button button = (Button) arg.widget;

			if (button == okButton)
			{
				okPressed();
			}
			else if (button == cancelButton)
			{
				cancelPressed();
			}
			else if (button == tcgaButton)
			{
				tcgaPressed();
			}
		}
	}

	private void cancelPressed()
	{
		shell.dispose();
	}

	private void okPressed()
	{
		readGenesInBox();
		shell.dispose();
	}

	private void readGenesInBox()
	{
		genes = new ArrayList<String>();
		for (String s : genesText.getText().split("\\s+"))
		{
			if (!s.isEmpty()) genes.add(s);
		}
	}

	private void tcgaPressed()
	{
		readGenesInBox();

		TCGAGenesDialog d = new TCGAGenesDialog(shell);
		Set<String> selected = d.open();

		if (selected == null) return;

		if (selected.isEmpty())
		{
			MessageDialog.openInformation(shell, "", "No genes found.");
			return;
		}

		genes.addAll(selected);
		Collections.sort(genes);

		String s = "";
		for (String gene : genes)
		{
			s += gene + " ";
		}

		genesText.setText(s.trim());
	}
}