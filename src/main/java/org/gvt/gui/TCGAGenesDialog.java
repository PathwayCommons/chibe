package org.gvt.gui;

import org.cbio.causality.data.portal.BroadAccessor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.layout.CellLayout;
import org.gvt.ChisioMain;

import java.util.*;
import java.util.List;

/**
 * This dialog retrieves MutSig and Gistic genes of a selected Study.
 * @author Ozgun Babur
 */
public class TCGAGenesDialog extends Dialog
{
	private static double lastMutsigThr = 0.05;
	private static double lastGisticThr = 0.05;
	private static int lastStudyIndex = 0;

	private Set<String> genes;
	private List<String> studyCodes;
	/**
	 * Parent shell.
	 */
	private Shell shell;

	private Combo studyCombo;
	private Button okButton;
	private Button cancelButton;
	private Button mutsigButton;
	private Button gisticButton;
	private Text mutsigThrField;
	private Text gisticThrField;

	/**
	 * Constructor.
	 *
	 * @param shell parent
	 */
	public TCGAGenesDialog(Shell shell)
	{
		super(shell);
	}

	/**
	 * Open the dialog. Also returns the selected item if only one is selected.
	 */
	public Set<String> open()
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

		shell.setText("Retrieve significantly altered genes in a TCGA study");

		Group studyGroup = new Group(shell, SWT.NONE);
		studyGroup.setLayout(new RowLayout());
		Label studyLabel = new Label(studyGroup, SWT.NONE);
		studyLabel.setText("TCGA Study:");
		studyCombo = new Combo(studyGroup, SWT.READ_ONLY);
		studyCodes = BroadAccessor.getStudyCodes();
		studyCombo.setItems(studyCodes.toArray(new String[studyCodes.size()]));
		studyCombo.select(lastStudyIndex);

		ButtonAdapter adapter = new ButtonAdapter();

		Group middleGroup = new Group(shell, SWT.NONE);
		middleGroup.setLayout(new GridLayout(2 /*columns*/, false /*not equal width*/));
		mutsigButton = new Button(middleGroup, SWT.CHECK);
		mutsigThrField = new Text(middleGroup, SWT.SINGLE);
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.grabExcessVerticalSpace = false;
		data.horizontalAlignment = SWT.LEFT;
		data.verticalAlignment = SWT.BOTTOM;
		mutsigThrField.setLayoutData(data);
		gisticButton = new Button(middleGroup, SWT.CHECK);
		gisticThrField = new Text(middleGroup, SWT.SINGLE);
		data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.grabExcessVerticalSpace = false;
		data.horizontalAlignment = SWT.LEFT;
		data.verticalAlignment = SWT.BOTTOM;
		gisticThrField.setLayoutData(data);

		mutsigButton.setSelection(true);
		mutsigButton.setText("Get mutated genes\nwith a MutSig q-value threshold");
		gisticButton.setText("Get copy number altered genes\nwith a Gistic q-value threshold");
		mutsigThrField.setText("" + lastMutsigThr);
		gisticThrField.setText("" + lastGisticThr);
		gisticThrField.setEnabled(false);
		mutsigButton.addSelectionListener(adapter);
		gisticButton.addSelectionListener(adapter);

		Composite buttonsGroup = new Composite(shell, SWT.NONE);
		buttonsGroup.setLayout(new RowLayout());

		okButton = new Button(buttonsGroup, SWT.NONE);
		okButton.setText("OK");
		okButton.addSelectionListener(adapter);

		cancelButton = new Button(buttonsGroup, SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(adapter);
	}

	class ButtonAdapter extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent arg)
		{
			Button button = (Button) arg.widget;

			okButton.setEnabled(mutsigButton.getSelection() || gisticButton.getSelection());

			if (button == okButton)
			{
				okPressed();
			}
			else if (button == cancelButton)
			{
				cancelPressed();
			}
			else if (button == mutsigButton)
			{
				mutsigThrField.setEnabled(mutsigButton.getSelection());
			}
			else if (button == gisticButton)
			{
				gisticThrField.setEnabled(gisticButton.getSelection());
			}
		}
	}

	private void cancelPressed()
	{
		shell.dispose();
	}

	private void okPressed()
	{
		genes = new HashSet<String>();

		String study = studyCodes.get(studyCombo.getSelectionIndex());
		if (mutsigButton.getSelection())
		{
			try
			{
				lastMutsigThr = Double.parseDouble(mutsigThrField.getText());
			}
			catch (NumberFormatException e)
			{
				MessageDialog.openError(shell, "Error", "Cannot read MutSig q-value threshold.");
				return;
			}
			genes.addAll(BroadAccessor.getMutsigGenes(study, lastMutsigThr));
		}
		if (gisticButton.getSelection())
		{
			try
			{
				lastGisticThr = Double.parseDouble(gisticThrField.getText());
			}
			catch (NumberFormatException e)
			{
				MessageDialog.openError(shell, "Error", "Cannot read Gistic q-value threshold.");
				return;
			}
			genes.addAll(BroadAccessor.getGisticGenes(study, lastGisticThr));
		}

		lastStudyIndex = studyCombo.getSelectionIndex();
		shell.dispose();
	}
}