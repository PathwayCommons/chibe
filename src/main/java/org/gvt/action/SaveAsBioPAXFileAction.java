package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.gvt.ChisioMain;

import java.io.File;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SaveAsBioPAXFileAction extends Action
{
	private ChisioMain main;

	private boolean saved;

	/**
	 * Constructor
	 *
	 * @param chisio
	 */
	public SaveAsBioPAXFileAction(ChisioMain chisio)
	{
		super("Save As ...");
		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/save-as.png"));
		this.main = chisio;
	}

	public void run()
	{
		this.saved = false;

		// Only possible when there is an owl model associated.
		if (main.getOwlModel() == null)
		{
			return;
		}

		String fileName = null;
		boolean done = false;

		while (!done)
		{
			// Get the user to choose a file name and type to save.
			FileDialog fileChooser = new FileDialog(main.getShell(), SWT.SAVE);

			// Do not let user to overwrite a non-graphml file by default

			String currentFilename = main.getOwlFileName();

			if (currentFilename != null)
			{
				if (!currentFilename.endsWith(".owl"))
				{
					if (currentFilename.indexOf(".") > 0)
					{
						currentFilename = currentFilename.substring(
							0, currentFilename.lastIndexOf("."));
					}
					currentFilename += ".owl";
				}

				fileChooser.setFileName(currentFilename);
			}

			String[] filterExtensions = new String[]{"*.owl"};
			String[] filterNames = new String[]{"BioPAX (*.owl)"};

			fileChooser.setFilterExtensions(filterExtensions);
			fileChooser.setFilterNames(filterNames);
			fileName = fileChooser.open();

			if (fileName == null)
			{
				// User has cancelled, so quit and return
				done = true;
			}
			else
			{
				// User has selected a file; see if it already exists
				File file = new File(fileName);

				if (file.exists())
				{
					// The file already exists; asks for confirmation
					MessageBox mb = new MessageBox(
						fileChooser.getParent(),
						SWT.ICON_WARNING | SWT.YES | SWT.NO);

					// We really should read this string from a
					// resource bundle
					mb.setMessage(fileName +
						" already exists. Do you want to overwrite?");
					mb.setText("Confirm Replace File");
					// If they click Yes, we're done and we drop out. If
					// they click No, we redisplay the File Dialog
					done = mb.open() == SWT.YES;
				}
				else
				{
					// File does not exist, so drop out
					done = true;
				}
			}
		}

		if (fileName == null)
		{
			return;
		}

		SaveBioPAXFileAction action = new SaveBioPAXFileAction(main, fileName);
		action.run();
		this.saved = action.isSaved();
	}

	public boolean isSaved()
	{
		return saved;
	}
}