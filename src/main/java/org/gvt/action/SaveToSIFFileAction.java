package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.model.sif.SIFGraph;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SaveToSIFFileAction extends Action
{
	ChisioMain main;

	public boolean isSaved = true;

	/**
	 * Constructor
	 *
	 * @param chisio
	 */
	public SaveToSIFFileAction(ChisioMain chisio)
	{
		super("Save As SIF File ...");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/save-as.png"));
		setToolTipText(getText());
		this.main = chisio;
	}

	public void run()
	{
		String fileName = null;
		boolean done = false;

		SIFGraph sifGraph = null;
		BasicSIFGraph basicSifGraph = null;

		if (main.getPathwayGraph().getGraphType().equals(BioPAXGraph.SIF))
		{
			sifGraph = (SIFGraph) main.getPathwayGraph();
		}
		else if (main.getPathwayGraph().getGraphType().equals(BioPAXGraph.BASIC_SIF))
		{
			basicSifGraph = (BasicSIFGraph) main.getPathwayGraph();
		}

		if (sifGraph == null && basicSifGraph == null)
		{
			MessageDialog.openError(main.getShell(), "Not A Valid View!",
				"Only Simple Interaction Views can be written in SIF format.");
			return;
		}

		while (!done)
		{
			// Get the user to choose a file name and type to save.
			FileDialog fileChooser = new FileDialog(main.getShell(), SWT.SAVE);

			// Do not let user to overwrite a non-graphml file by default

			String currentFilename = main.getOwlFileName();
			if (!currentFilename.endsWith(".sif"))
			{
				if (currentFilename.indexOf(".") > 0)
				{
					currentFilename = currentFilename.substring(
						0, currentFilename.lastIndexOf("."));
				}
				currentFilename += ".sif";
			}

			fileChooser.setFileName(currentFilename);

			String[] filterExtensions = new String[]{"*.sif"};
			String[] filterNames = new String[]{"Simple Interaction Format (*.sif)"};

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
					mb.setMessage(fileName + " already exists. Do you want to replace it?");
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
			isSaved = false;
			return;
		}

		try
		{
			OutputStream os = new FileOutputStream(fileName);

			if (sifGraph != null)
			{
				sifGraph.write(os);
			}
			else if (basicSifGraph != null)
			{
				basicSifGraph.write(os);
			}
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			MessageBox messageBox = new MessageBox(main.getShell(), SWT.ERROR_UNSUPPORTED_FORMAT);
			messageBox.setMessage("File cannot be saved!");
			messageBox.setText(ChisioMain.TOOL_NAME);
			messageBox.open();
		}
	}
}