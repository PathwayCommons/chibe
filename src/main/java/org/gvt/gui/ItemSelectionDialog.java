package org.gvt.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.draw2d.TextUtilities;

import java.util.*;
import java.util.List;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ItemSelectionDialog extends Dialog
{
	/**
	 * Supported rules.
	 */
	private List possibleItems;

	/**
	 * Rule types that user selected.
	 */
	private List selectedItems;

	/**
	 * Multiple selection eanbled or disabled.
	 */
	private boolean multipleSelect;

	/**
	 * Title of the dialog.
	 */
	private String title;

	/**
	 * Short message to user about what to do.
	 */
	private String message;

	/**
	 * The modalitiy of the dialog.
	 */
	boolean modal;

	/**
	 * Minimum number of selection for OK button to be enabled.
	 */
	int minValidSelect;

	/**
	 * A runnable to use when the selection is changed.
	 */
	private ItemSelectionRunnable selectionRun;

	/**
	 * If this is true then the button "Update" will automatically will be pressed upon selection.
	 */
	boolean updateUponSelection;

	/**
	 * True if user pressed cancel.
	 */
	private boolean pressedCancel;

	/**
	 * Width of the dialog.
	 */
	private int width;

	/**
	 * Whether the list will be shown sorted or not
	 */
	private boolean doSort;

	/**
	 * Flag to use 3 dots if the item does not fit into the the dimensions.
	 */
	private boolean use3dots;

	/**
	 * Parent shell.
	 */
	private Shell shell;

	private Button okButton;
	private Button cancelButton;
	private Button allButton;
	private Button noneButton;

	private Button[] itemButtonArray;

	/**
	 * Texts of the buttons are sometimes truncated. So we use this map for remembering original
	 * texts of buttons.
	 */
	Map<Button, Object> but2orig;

	/**
	 * Constructor.
	 *
	 * @param shell parent
	 * @param possibleItems items that user will select
	 * @param selectedItems list of checked items, may be empty when passed, will be filled by user
	 */
	public ItemSelectionDialog(Shell shell,
		int width,
		String title,
		String message,
		List possibleItems,
		List selectedItems,
		boolean multipleSelect,
		boolean modal,
		ItemSelectionRunnable selectionRun)
	{
		super(shell, modal ? SWT.APPLICATION_MODAL : SWT.MODELESS);
		this.width = width;
		this.title = title;
		this.message = message;
		this.possibleItems = possibleItems;

		this.selectedItems = selectedItems != null ? selectedItems : new ArrayList<String>();
		this.multipleSelect = multipleSelect;
		this.modal = modal;
		this.selectionRun = selectionRun;
		this.updateUponSelection = false;
		this.doSort = true;
		this.minValidSelect = 0;
		this.use3dots = modal;
		but2orig = new HashMap<Button, Object>();
	}

	public boolean isUpdateUponSelection()
	{
		return updateUponSelection;
	}

	public void setUpdateUponSelection(boolean updateUponSelection)
	{
		this.updateUponSelection = updateUponSelection;
	}

	public void setDoSort(boolean doSort)
	{
		this.doSort = doSort;
	}

	public int getMinValidSelect()
	{
		return minValidSelect;
	}

	public void setMinValidSelect(int minValidSelect)
	{
		this.minValidSelect = minValidSelect;
	}

	public boolean isUse3dots()
	{
		return use3dots;
	}

	public void setUse3dots(boolean use3dots)
	{
		this.use3dots = use3dots;
	}

	/**
	 * Open the dialog. Also returns the selected item if only one is selected.
	 */
	public Object open()
	{
		pressedCancel = true;
		createContents();

		shell.setText(title);
		shell.pack();
		if (shell.getSize().y > 500) shell.setSize(new Point(shell.getSize().x, 500));

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

		if (selectedItems.size() == 1 && !pressedCancel)
		{
			return selectedItems.get(0);
		}
		else
		{
			return null;
		}
	}

	private void createContents()
	{
		if (doSort)
		{
			Collections.sort(possibleItems);
		}
		if (!modal && !multipleSelect)
		{
			this.possibleItems.add(0, NONE);
		}

		shell = new Shell(getParent(),
			SWT.DIALOG_TRIM | (modal ? SWT.APPLICATION_MODAL : SWT.MODELESS) | SWT.RESIZE);

		GridLayout grLy = new GridLayout();
		grLy.numColumns = 2;
		shell.setLayout(grLy);
		shell.setText("Selection Dialog");

		final Group outerGroup = new Group(shell, SWT.NONE);
		outerGroup.setText(message); // + " - " + possibleItems.size());
		outerGroup.setLayout(new GridLayout());

		outerGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		final ScrolledComposite scroll = new ScrolledComposite(outerGroup,
			SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		scroll.setLayout(new GridLayout());
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);

		scroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite itemsGroup = new Composite(scroll, SWT.NONE);
		scroll.setContent(itemsGroup);
		grLy = new GridLayout();
		grLy.numColumns = 1;
		itemsGroup.setLayout(grLy);

		itemsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		ButtonAdapter adapter = new ButtonAdapter();

		itemButtonArray = new Button[possibleItems.size()];
		int i = 0;

		for (Object pItem : possibleItems)
		{
			Button itemBox = new Button(itemsGroup, multipleSelect ? SWT.CHECK : SWT.RADIO);
			itemButtonArray[i++] = itemBox;
			itemBox.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, false));

			but2orig.put(itemBox, pItem);

			if (use3dots)
			{
				String txt = truncate(pItem.toString(), itemBox.getFont(), width - 87);
				if (!txt.equals(pItem))
				{
					itemBox.setToolTipText(pItem.toString());
				}
				itemBox.setText(txt);
			}
			else itemBox.setText(pItem.toString());

			itemBox.addSelectionListener(adapter);
			itemBox.setSelection(selectedItems.contains(but2orig.get(itemBox)));
		}

		itemsGroup.pack();
		scroll.setMinSize(itemsGroup.getSize());

		Composite leftButtonsGroup = new Composite(shell, SWT.NONE);
		leftButtonsGroup.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		leftButtonsGroup.setLayout(new RowLayout());

		if (modal && multipleSelect && !updateUponSelection)
		{
			allButton = new Button(leftButtonsGroup, SWT.NONE);
			allButton.setText("All");
			allButton.addSelectionListener(adapter);

			noneButton = new Button(leftButtonsGroup, SWT.NONE);
			noneButton.setText("None");
			noneButton.addSelectionListener(adapter);
		}

		Composite rightButtonGroup = new Composite(shell, SWT.NONE);
		rightButtonGroup.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false));
		rightButtonGroup.setLayout(new RowLayout());

		okButton = new Button(rightButtonGroup, SWT.NONE);
		okButton.setText(modal ? "OK" : "Update");
		okButton.addSelectionListener(adapter);
		okButton.setEnabled(!isUpdateUponSelection() && selectedItems.size() >= minValidSelect &&
			(selectedItems.isEmpty() || !selectedItems.get(0).equals(NONE)));

		cancelButton = new Button(rightButtonGroup, SWT.NONE);
		cancelButton.setText(modal ? "Cancel" : "Close");
		cancelButton.addSelectionListener(adapter);
	}

	private String truncate(String s, Font f, int limit)
	{
		int w = TextUtilities.INSTANCE.getStringExtents(s, f).width;

		boolean truncated = false;

		while (w > limit)
		{
			s = s.substring(0, s.length() - 1);
			w = TextUtilities.INSTANCE.getStringExtents(s + "...", f).width;
			truncated = true;
		}

		return truncated ? s + "..." : s;
	}

	/**
	 * Since ok button only disposes the dialog, we need a mechanism to understand if cancel is
	 * pressed.
	 * @return
	 */
	public boolean isCancelled()
	{
		return pressedCancel;
	}

	public List getSelectedItems()
	{
		return selectedItems;
	}

	/**
	 * Makes the selections possible programmatically. Trows an exception if item not found.
	 * @param item to select
	 */
	public void selectItem(Object item)
	{
		int index = possibleItems.indexOf(item);
		itemButtonArray[index].setSelection(true);
	}

	public void runAsIfSelected(List list)
	{
		this.selectionRun.run(list);
	}

	public void runAsIfSelected(Object item)
	{
		List list = new ArrayList();
		list.add(item);
		this.selectionRun.run(list);
	}

	class ButtonAdapter extends SelectionAdapter
	{
		public void widgetSelected(SelectionEvent arg)
		{
			Button button = (Button) arg.widget;

			if ((button.getStyle() & SWT.CHECK) > 0  ||
				(button.getStyle() & SWT.RADIO) > 0)
			{
				Object item = but2orig.get(button);

				if (button.getSelection())
				{
					// this check is for radio buttons.
					if (!selectedItems.contains(item))
					{
						selectedItems.add(item);
					}
				}
				else
				{
					selectedItems.remove(item);
				}

				if (isUpdateUponSelection() && !(!multipleSelect && selectedItems.isEmpty()))
				{
					Event e = new Event();
					e.item = okButton;
					e.data = okButton;
					e.widget = okButton;
					SelectionEvent event = new SelectionEvent(e);

					widgetSelected(event);
				}
				else
				{
					okButton.setEnabled(minValidSelect <= selectedItems.size());
				}
			}
			else if (button == okButton)
			{
				pressedCancel = false;

				if (modal)
				{
					shell.dispose();
				}
				else
				{
					if (selectionRun != null)
					{
						selectionRun.run(selectedItems);
						okButton.setEnabled(false);
					}
				}
			}
			else if (button == cancelButton)
			{
				pressedCancel = true;
				shell.dispose();
			}
			else if (button == allButton || button == noneButton)
			{
				boolean selected = button == allButton;

				for (Button item : itemButtonArray)
				{
					item.setSelection(selected);
				}

				selectedItems.clear();

				if(selected)
				{
					selectedItems.addAll(possibleItems);
				}
				okButton.setEnabled(selectedItems.size() >= minValidSelect);
			}
		}
	}

	public static final String NONE = "None";
	public static final int MAX_HEIGHT = 500;
}