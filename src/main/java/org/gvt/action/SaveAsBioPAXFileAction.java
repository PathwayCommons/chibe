package org.gvt.action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.gvt.ChisioMain;

import java.io.File;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SaveAsBioPAXFileAction extends ChiBEAction
{
	private boolean saved;

	/**
	 * Constructor
	 *
	 * @param chisio
	 */
	public SaveAsBioPAXFileAction(ChisioMain chisio)
	{
		super("Save As ...", "icon/save-as.png", chisio);
		addFilterExtension(FILE_KEY, new String[]{"*.owl"});
		addFilterName(FILE_KEY, new String[]{"BioPAX (*.owl)"});
	}

	public void run()
	{
		this.saved = false;

		// Only possible when there is an owl model associated.
		if (main.getBioPAXModel() == null)
		{
			return;
		}

		String fileName = new FileChooser(this, true).choose(FILE_KEY);

		if (fileName == null)
		{
			return;
		}

		// Ensure that file extension is .owl
		if (!fileName.endsWith(".owl"))
		{
			// Below line was erasing what the user entered after the last dot, so
			// commented out.
//					fileName = fileName.substring(0, fileName.lastIndexOf("."));

			fileName += ".owl";
		}

		SaveBioPAXFileAction action = new SaveBioPAXFileAction(main, fileName);
		action.run();
		this.saved = action.isSaved();
	}

	public boolean isSaved()
	{
		return saved;
	}

	@Override
	public String getCurrentFilename()
	{
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

			return currentFilename;
		}

		return null;
	}
}