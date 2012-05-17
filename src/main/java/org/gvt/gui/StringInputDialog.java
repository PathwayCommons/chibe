package org.gvt.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
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

    /**
     * Information about functionality of the dialog
     */
    private String info;
    private boolean infoPresent = false;

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

    /**
     * Constructor used for cases where an informative message is found in the dialog.
     *
     */
    public StringInputDialog(Shell shell, String title, String message, String input, String info)
    {
        super(shell);
        this.title = title;
        this.message = message;
        this.input = input;
        this.info = info;
        this.infoPresent = true;
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

        shell.setImage(ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbe-icon.png").createImage());

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

        // If there is an information to display, it is added in the first place.
        if(infoPresent)
        {
            Label infoLabel = new Label(shell, SWT.NONE);
            infoLabel.setText(this.info);
            GridData gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
            gridData.verticalSpan = 6;
            infoLabel.setLayoutData(gridData);
        }

		GridLayout grLy = new GridLayout();
		grLy.numColumns = 1;
		shell.setLayout(grLy);

		shell.setText(title);

		final Group itemsGroup = new Group(shell, SWT.NONE);
		itemsGroup.setLayout(new FillLayout());
		itemsGroup.setText(message);

		GridData gd = new GridData(GridData.FILL, GridData.FILL, false, true);
		gd.widthHint = 200;
		gd.heightHint = 20;
		itemsGroup.setLayoutData(gd);

		ButtonAdapter adapter = new ButtonAdapter();

		text = new Text(itemsGroup, SWT.SINGLE);
		text.addKeyListener(new KeyListener()
		{
			@Override
			public void keyPressed(KeyEvent keyEvent){}

			@Override
			public void keyReleased(KeyEvent keyEvent)
			{
//				System.out.println(keyEvent.keyCode);
				if (keyEvent.keyCode == 13)
				{
					okPressed();
				}
			}
		});

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
				okPressed();
			}
			else if (button == cancelButton)
			{
				cancelPressed();
			}
		}
	}

	private void cancelPressed()
	{
		input = null;
		shell.dispose();
	}

	private void okPressed()
	{
		input = text.getText();
		shell.dispose();
	}
}