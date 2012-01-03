package org.gvt.gui;

/**
 *
 */

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Process;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PathwaySelectionDialog
{
	Model model;
	String[] selected;

	public static void main(String[] args)
	{
		PathwaySelectionDialog d = new PathwaySelectionDialog(null);
		d.open();
	}

	public PathwaySelectionDialog(Model model)
	{
		this.model = model;
	}

	public String[] open()
	{
		final Display display = new Display();
		Shell shell = new Shell(display);
		createContents(shell);
		shell.pack();
		if (shell.getSize().y > 500) shell.setSize(new Point(shell.getSize().x, 500));
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
		return selected;
	}

	private void createContents(final Shell shell)
	{
		shell.setLayout(new FillLayout());
		Composite parent = new Composite(shell, SWT.BORDER);
		GridLayout grid = new GridLayout(2, true);
		parent.setLayout(grid);

		final Tree tree = new Tree(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		fillTree(tree);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		tree.setLayoutData(data);

		Button okButton = new Button(parent, SWT.PUSH);
		okButton.setText("OK");
		okButton.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event event)
			{
				TreeItem[] selection = tree.getSelection();
				selected = new String[selection.length];
				for (int i = 0; i < selected.length; i++)
				{
					selected[i] = selection[i].getText();
				}

				shell.dispose();
			}
		});
		data = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		okButton.setLayoutData(data);

		Button cancelButton = new Button(parent, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.addListener(SWT.Selection, new Listener()
		{
			public void handleEvent(Event event)
			{
				shell.dispose();
			}
		});
		data = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		cancelButton.setLayoutData(data);
	}

	private void fillTree(Tree tree)
	{
		for (Pathway p : model.getObjects(Pathway.class))
		{
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setText(p.getDisplayName());
			handleChildren(p, item);
		}
	}

	private void handleChildren(Pathway p, TreeItem parent)
	{
		for (Process prc : p.getPathwayComponent())
		{
			if (prc instanceof Pathway)
			{
				Pathway c = (Pathway) prc;
				TreeItem item = new TreeItem(parent, SWT.NONE);
				item.setText(c.getDisplayName());
				handleChildren(c, item);
			}
		}
	}

	private void setSelection(Tree tree, String[] selected)
	{
		Set<String> set = new HashSet<String>();
		set.addAll(Arrays.asList(selected));
		TreeItem[] items = tree.getItems();
		
	}
	
	private void collectSelected(TreeItem item, Set<String> selected, Set<TreeItem> items)
	{
		if (selected.contains(item.getText())) items.add(item);

		for (TreeItem child : item.getItems())
		{
			collectSelected(child, selected, items);
		}
	}

}
