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
public class SaveSIFLayoutAction extends ChiBEAction
{
	/**
	 * Constructor
	 *
	 * @param chisio
	 */
	public SaveSIFLayoutAction(ChisioMain chisio)
	{
		super("Save SIF Layout ...", "icon/save-as.png", chisio);
		addFilterExtension(FILE_KEY, new String[]{"*.layout"});
		addFilterName(FILE_KEY, new String[]{"SIF Layout (*.layout)"});
	}

	@Override
	public String getCurrentFilename()
	{
		String currentFilename = main.getOwlFileName();

		if (currentFilename != null)
		{
			if (!currentFilename.endsWith(".layout"))
			{
				if (currentFilename.indexOf(".") > 0)
				{
					currentFilename = currentFilename.substring(0, currentFilename.lastIndexOf("."));
				}
				currentFilename += ".layout";
			}
		}
		return currentFilename;
	}

	public void run()
	{
		if (main.getPathwayGraph() == null) return;

		org.gvt.model.sifl3.SIFGraph sifL3Graph = null;
		BasicSIFGraph basicSifGraph = null;

		if (main.getPathwayGraph().getGraphType().equals(BioPAXGraph.SIF_LEVEL3))
		{
			sifL3Graph = (org.gvt.model.sifl3.SIFGraph) main.getPathwayGraph();
		}
		else if (main.getPathwayGraph().getGraphType().equals(BioPAXGraph.BASIC_SIF))
		{
			basicSifGraph = (BasicSIFGraph) main.getPathwayGraph();
		}

		if (sifL3Graph == null && basicSifGraph == null)
		{
			MessageDialog.openError(main.getShell(), "Not A Valid View!",
				"Only the layouts of SIF graphs can be saved.");
			return;
		}

		String fileName = new FileChooser(this, true).choose(FILE_KEY);

		if (fileName == null)
		{
			return;
		}

		try
		{
			OutputStream os = new FileOutputStream(fileName);

			if (sifL3Graph != null)
			{
				sifL3Graph.writeLayout(os);
			}
			else if (basicSifGraph != null)
			{
				basicSifGraph.writeLayout(os);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			MessageBox messageBox = new MessageBox(main.getShell(), SWT.ERROR_UNSUPPORTED_FORMAT);
			messageBox.setMessage("Layout cannot be saved!");
			messageBox.setText(ChisioMain.TOOL_NAME);
			messageBox.open();
		}
	}
}