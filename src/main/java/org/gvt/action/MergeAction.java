package org.gvt.action;

import org.biopax.paxtools.controller.Merger;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.interaction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.gvt.util.BioPAXReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This class merges two models via BioPAX Merger interface
 * and shows a confirmation dialog upon request.
 *
 * @author Arman Aksoy
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class MergeAction extends Action
{
	ChisioMain main;

	String filename;

	String newPathwayName;

	/**
	 * BioPAX paxtools model object.
	 */
	Model model;

	boolean updatePathways;
	boolean openPathways;
	boolean createNewPathway;

	// We are going to initilaze it in the constructor for later use
    private Merger merger;

    /**
	 * Constructor without filename. opens an FileChooser for filename
	 *
	 * @param chisio
	 */
	public MergeAction(ChisioMain chisio)
	{
		super("Merge With ...");
		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/merge.png"));

		this.main = chisio;

		this.updatePathways = true;
		this.openPathways = true;
    }

	public void setOpenPathways(boolean openPathways)
	{
		this.openPathways = openPathways;
	}

	public void setUpdatePathways(boolean updatePathways)
	{
		this.updatePathways = updatePathways;
	}

	public void setCreateNewPathway(boolean createNewPathway)
	{
		this.createNewPathway = createNewPathway;
	}

	public String getNewPathwayName()
	{
		return newPathwayName;
	}

	public void setNewPathwayName(String newPathwayName)
	{
		this.newPathwayName = newPathwayName;
	}

	/**
	 * Constructor with filename. opens the xml with file with the given
	 * filename.
	 *
	 * @param chisio
	 * @param filename
	 */
	public MergeAction(ChisioMain chisio, String filename)
	{
		this(chisio);
		this.filename = filename;
	}

	public MergeAction(ChisioMain chisio, Model model)
	{
		this(chisio);
		this.model = model;
	}

	/**
	 * opens a FileChooser for loading an xml file
	 *
	 * @return chosen filename
	 */
	public String openFileChooser()
	{
		// choose an input file.
		FileDialog fileChooser = new FileDialog(main.getShell(), SWT.OPEN);
		fileChooser.setFilterExtensions(FILTER_EXTENSIONS);
		fileChooser.setFilterNames(FILTER_NAMES);

		return fileChooser.open();
	}

	public void run()
	{
		if ( main.getRootGraph() == null )
		{
			MessageDialog.openError(main.getShell(), "Error!",
				"No BioPAX model.");
			return;
		}

		if (filename == null && model == null)
		{
			filename = openFileChooser();

			// If no file is selected
			if( filename == null )
			{
				return;
			}
		}

		// For now we only have one source model
		HashSet<Model> sources = new HashSet<Model>();

		try
		{
			assert model == null || filename == null : "One and only one must be null";
			assert model != null || filename != null : "One and only one must be null";

			if (model == null)
			{
				BioPAXIOHandler reader = new SimpleIOHandler();
				model = reader.convertFromOWL(new FileInputStream(filename));
			}

			// Don't do anything if the source model does not contain anything
			if (model.getObjects().isEmpty())
			{
				MessageDialog.openInformation(main.getShell(), "No Data!",
					"The file is empty.");
				return;
			}

			// Typical merging process
			sources.add(model);
			Model target = main.getOwlModel();

			if (merger == null)
			{
				merger = new Merger(SimpleEditorMap.get(target.getLevel()));
			}
			
			merger.merge(target, sources.toArray(new Model[sources.size()]));

			BioPAXReader reader = new BioPAXReader(target);
			BioPAXGraph graph = (BioPAXGraph) reader.readXMLFile(null);

			if (createNewPathway)
			{
				List<String> intids = getInteractionIDs(model);
				newPathwayName = graph.createPathway(
					newPathwayName == null ? "Neighborhood" : newPathwayName, intids);
				main.getAllPathwayNames().add(newPathwayName);
			}

			main.setRootGraph(graph);
			main.makeDirty();

			if (updatePathways) new UpdatePathwayAction(main, true).run();

			if (openPathways)
			{
				if (newPathwayName != null)
				{
					List<String> pnames = new ArrayList<String>(main.getOpenTabNames());
					pnames.add(newPathwayName);
					new OpenPathwaysAction(main, pnames).run();
				}
				else
				{
					new OpenPathwaysAction(main).run();
				}
			}

//			// Prepare a RDF list for merged elements
//			// we are going to use it for highlighting
//			Iterator mergedEnts = merger.getMergedElements().iterator();
//			ArrayList<String> mergedRdfs = new ArrayList<String>();
//			while (mergedEnts.hasNext())
//			{
//				BioPAXElement be = (BioPAXElement) mergedEnts.next();
//				mergedRdfs.add( be.getRDFId() );
//			}
//
//			// Clean up highlighting
//			new RemoveHighlightsAction(main).run();
//
//			// Highlight merged items
//			main.highlightRDFs(mergedRdfs);
//			// A simple procedure can be followed by starting with
//			// the elements in merger.getAddedElements()
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// Reset the variables for later use
		filename = null;
		model = null;
    }

    /**
	 * Checks if the file has a valid extension for loading into chisio.
	 * @param path the path of the file
	 * @return true if file name is valid
	 */
	public static boolean hasValidExtension(String path)
	{
		for (String extension : FILTER_EXTENSIONS)
		{
			if (path.substring(path.lastIndexOf(".")).equalsIgnoreCase(extension.substring(1)))
			{
				return true;
			}
		}
		return false;
	}

	private static List<String> getInteractionIDs(Model model)
	{
		List<String> ids = new ArrayList<String>();

		for (interaction inter : model.getObjects(interaction.class))
		{
			ids.add(inter.getRDFId());
		}
		return ids;
	}

    public static final String[] FILTER_EXTENSIONS = new String[]{ "*.owl" };

	public static final String[] FILTER_NAMES = new String[]{ "BioPAX (*.owl)" };
}
