package org.gvt.action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

import java.io.File;

/**
 * Created by babur on 3/10/16.
 */
public class FileChooser
{
	FileAccessorAction action;
	boolean forSave;

	public FileChooser(FileAccessorAction action, boolean forSave)
	{
		this.action = action;
		this.forSave = forSave;
	}

	public FileChooser(FileAccessorAction action)
	{
		this(action, false);
	}

	public String choose(String key)
	{
		// choose an input file.
		FileDialog fileChooser = new FileDialog(action.getShell(), forSave ? SWT.SAVE : SWT.OPEN);
		fileChooser.setFilterExtensions(action.getFilterExtensions(key));
		fileChooser.setFilterNames(action.getFilterNames(key));

		if (action.getLastPath(key) != null) fileChooser.setFilterPath(action.getLastPath(key));
		if (action.getCurrentFilename() != null) fileChooser.setFileName(action.getCurrentFilename());

		String f = fileChooser.open();

		String x = null;
		if (f != null)
		{
			if (f.contains("/"))
			{
				x = f.substring(0, f.lastIndexOf("/"));
			}
			else if (f.contains("\\"))
			{
				x = f.substring(0, f.lastIndexOf("\\"));
			}
		}

		if (x != null) action.setLastPath(x, key);

		if (f != null && forSave)
		{
			// User has selected a file; see if it already exists
			File file = new File(f);

			if (file.exists())
			{
				// The file already exists; asks for confirmation
				MessageBox mb = new MessageBox(action.getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);

				// We really should read this string from a
				// resource bundle
				mb.setMessage(f + " already exists. Do you want to replace it?");
				mb.setText("Confirm Replace File");

				// If they click Yes, we're done and we drop out. If
				// they click No, we redisplay the File Dialog
				if (mb.open() != SWT.YES) f = new FileChooser(action, forSave).choose(key);
			}
		}

		return f;
	}
}
