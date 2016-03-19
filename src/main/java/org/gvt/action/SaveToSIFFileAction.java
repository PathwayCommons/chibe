package org.gvt.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.basicsif.BasicSIFGraph;

import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SaveToSIFFileAction extends ChiBEAction
{
	public boolean isSaved = true;

	/**
	 * Constructor
	 *
	 * @param chisio
	 */
	public SaveToSIFFileAction(ChisioMain chisio)
	{
		super("Save As SIF File ...", "icon/save-as.png", chisio);
		addFilterExtension(FILE_KEY, new String[]{"*.sif"});
		addFilterName(FILE_KEY, new String[]{"Simple Interaction Format (*.sif)"});
	}

	@Override
	public String getCurrentFilename()
	{
		// Do not let user to overwrite a non-graphml file by default

		String currentFilename = main.getOwlFileName();

		if (currentFilename != null)
		{
			if (!currentFilename.endsWith(".sif"))
			{
				if (currentFilename.indexOf(".") > 0)
				{
					currentFilename = currentFilename.substring(0, currentFilename.lastIndexOf("."));
				}
				currentFilename += ".sif";
			}
		}
		return currentFilename;
	}

	public void run()
	{
		if (main.getPathwayGraph() == null) return;

		org.gvt.model.sifl2.SIFGraph sifL2Graph = null;
		org.gvt.model.sifl3.SIFGraph sifL3Graph = null;
		BasicSIFGraph basicSifGraph = null;

		if (main.getPathwayGraph().getGraphType().equals(BioPAXGraph.SIF_LEVEL2))
		{
			sifL2Graph = (org.gvt.model.sifl2.SIFGraph) main.getPathwayGraph();
		}
		if (main.getPathwayGraph().getGraphType().equals(BioPAXGraph.SIF_LEVEL3))
		{
			sifL3Graph = (org.gvt.model.sifl3.SIFGraph) main.getPathwayGraph();
		}
		else if (main.getPathwayGraph().getGraphType().equals(BioPAXGraph.BASIC_SIF))
		{
			basicSifGraph = (BasicSIFGraph) main.getPathwayGraph();
		}

		if (sifL2Graph == null && sifL3Graph == null && basicSifGraph == null)
		{
			MessageDialog.openError(main.getShell(), "Not A Valid View!",
				"Only Simple Interaction Views can be written in SIF format.");
			return;
		}

		String fileName = new FileChooser(this, true).choose(FILE_KEY);

		if (fileName == null)
		{
			isSaved = false;
			return;
		}

		try
		{
			OutputStream os = new FileOutputStream(fileName);

			if (sifL2Graph != null)
			{
				sifL2Graph.write(os);
			}
			else if (sifL3Graph != null)
			{
				sifL3Graph.write(os);
			}
			else if (basicSifGraph != null)
			{
				basicSifGraph.write(os);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			MessageBox messageBox = new MessageBox(main.getShell(), SWT.ERROR_UNSUPPORTED_FORMAT);
			messageBox.setMessage("File cannot be saved!");
			messageBox.setText(ChisioMain.TOOL_NAME);
			messageBox.open();
		}
	}
}