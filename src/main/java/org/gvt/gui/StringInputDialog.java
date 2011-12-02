package org.gvt.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;
import org.gvt.ChisioMain;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class StringInputDialog extends Dialog
{
	/**
	 * Short message to user about what to do.
	 */
	private String message;
	private String title;

	private String input;

	private boolean selectText;

	/**
	 * Parent shell.
	 */
	private Shell shell;

	private Text text;

	private Button okButton;
	private Button cancelButton;

	/**
	 * Constructor.
	 *
	 * @param shell parent
	 * @param message to display user
	 */
	public StringInputDialog(Shell shell, String title, String message, String input)
	{
		super(shell);
		this.title = title;
		this.message = message;
		this.input = input;
	}

	public void setSelectText(boolean selectText)
	{
		this.selectText = selectText;
	}

	/**
	 * Open the dialog. Also returns the selected item if only one is selected.
	 */
	public String open()
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

		return input;
	}

	private void createContents()
	{
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

		GridLayout grLy = new GridLayout();
		grLy.numColumns = 1;
		shell.setLayout(grLy);

		shell.setText(title);

		final Group itemsGroup = new Group(shell, SWT.NONE);
		itemsGroup.setLayout(new FillLayout());
		itemsGroup.setText(message);

		GridData gd = new GridData();
		gd.widthHint = 200;
		gd.heightHint = 20;
		itemsGroup.setLayoutData(gd);

		ButtonAdapter adapter = new ButtonAdapter();

		text = new Text(itemsGroup, SWT.SINGLE);

		if (input != null) text.setText(input);
		if (selectText) text.selectAll();

		Composite buttonsGroup = new Composite(shell, SWT.NONE);
		buttonsGroup.setLayout(new RowLayout());

		gd = new GridData();
		gd.horizontalAlignment = GridData.CENTER;
		buttonsGroup.setLayoutData(gd);
		
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

			if (button == okButton)
			{
				input = text.getText();
				shell.dispose();
			}
			else if (button == cancelButton)
			{
				input = null;
				shell.dispose();
			}
		}
	}
}