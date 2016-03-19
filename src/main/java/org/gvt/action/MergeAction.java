package org.gvt.action;

import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.SimpleMerger;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.util.BioPAXUtil;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class merges two models via BioPAX Merger interface
 * and shows a confirmation dialog upon request.
 *
 * @author Arman Aksoy
 * @author Ozgun Babur
 *         <p/>
 *         Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class MergeAction extends ChiBEAction
{
	String filename;

	String newPathwayName;

	/**
	 * BioPAX paxtools model object.
	 */
	Model model;

	boolean updatePathways;
	boolean openPathways;
	boolean createNewPathway;

	/**
	 * Constructor without filename. opens an FileChooser for filename
	 *
	 * @param chisio
	 */
	public MergeAction(ChisioMain chisio)
	{
		super("Merge With ...", "icon/merge.png", chisio);
		this.updatePathways = true;
		this.openPathways = true;
		addFilterExtension(FILE_KEY, new String[]{"*.owl"});
		addFilterName(FILE_KEY, new String[]{"BioPAX (*.owl)"});
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

	public void run()
	{
		if (main.getBioPAXModel() == null)
		{
			MessageDialog.openError(main.getShell(), "Error!",
				"Load or query a BioPAX model first.");
			return;
		}

		if (filename == null && model == null)
		{
			filename = new FileChooser(this).choose(FILE_KEY);

			// If no file is selected
			if (filename == null)
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

			if (main.getBioPAXModel().getLevel().equals(model.getLevel()))
			{
				// Typical merging process
				sources.add(model);

				SimpleMerger merger = new SimpleMerger(
					SimpleEditorMap.get(main.getBioPAXModel().getLevel()));

				merger.merge(main.getBioPAXModel(), model);
//				ModelUtils.mergeEquivalentInteractions(main.getBioPAXModel());

				if (createNewPathway)
				{
					Set<String> intids = BioPAXUtil.getInteractionIDs(model);

					newPathwayName = BioPAXUtil.createPathway(
						main.getBioPAXModel(), newPathwayName, intids).getName();
				}

				main.makeDirty();

				if (updatePathways) new UpdatePathwayAction(main, true).run();

				if (openPathways)
				{
					if (newPathwayName != null)
					{
						List<String> pnames = new ArrayList<String>(main.getOpenTabNames());
						pnames.add(newPathwayName);
						new OpenPathwaysAction(main, pnames).run();
					} else
					{
						new OpenPathwaysAction(main).run();
					}
				}
			} else
			{
				MessageDialog.openError(main.getShell(), "Incompatible Levels", "Models with different levels cannot be merged.");
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
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			// Reset the variables for later use
			filename = null;
			model = null;
		}
	}
}
