package org.gvt.action;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;

import java.io.FileOutputStream;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SaveBioPAXFileAction extends Action
{
	private ChisioMain main;
	private String filename;

	private boolean saved;

	/**
	 * Constructor
	 *
	 * @param chisio
	 */
	public SaveBioPAXFileAction(ChisioMain chisio)
	{
		super("Save");
		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "../icon/save.png"));
		this.main = chisio;
	}

	public SaveBioPAXFileAction(ChisioMain main, String filename)
	{
		this(main);
		this.filename = filename;
	}

	public void run()
	{
		this.saved = false;

		// Only possible when there is an owl model associated.
		if (main.getOwlModel() == null)
		{
			return;
		}

		// If no filename is specified, then check for it in main application
		if (filename == null)
		{
			filename = main.getOwlFileName();
		}

		// If this was not saved before, i.e. no filename, then run save as action
		if (filename == null)
		{
			SaveAsBioPAXFileAction action = new SaveAsBioPAXFileAction(main);
			action.run();
			this.saved = action.isSaved();
			return;
		}

		// filename not null
		try
		{
			main.lockWithMessage("Saving ...");

			// Record layout of all views
			for (ScrollingGraphicalViewer viewer : main.getTabToViewerMap().values())
			{
				Object o = viewer.getContents().getModel();

				if (o instanceof BioPAXGraph)
				{
					BioPAXGraph graph = (BioPAXGraph) o;
					if (graph.isMechanistic())
					{
						graph.recordLayout();
					}
				}
			}

			BioPAXIOHandler exporter = new SimpleIOHandler(main.getOwlModel().getLevel());

			FileOutputStream stream = new FileOutputStream(filename);
			exporter.convertToOWL(main.getOwlModel(), stream);
			stream.close();

			main.setOwlFileName(filename);
			main.markSaved();
			this.saved = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			MessageDialog.openError(main.getShell(), ChisioMain.TOOL_NAME,
				"File cannot be saved!\n" + e.getMessage());
		}
		finally
		{
			main.unlock();
			filename = null;
		}
	}

	public boolean isSaved()
	{
		return saved;
	}
}