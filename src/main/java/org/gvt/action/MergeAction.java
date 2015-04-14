package org.gvt.action;

import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.SimpleMerger;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level3.Interaction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.gvt.util.BioPAXUtil;
import org.gvt.util.PathwayHolder;

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
		if (main.getBioPAXModel() == null)
		{
			MessageDialog.openError(main.getShell(), "Error!",
				"Load or query a BioPAX model first.");
			return;
		}

		if (filename == null && model == null)
		{
			filename = openFileChooser();

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

	public static final String[] FILTER_EXTENSIONS = new String[]{"*.owl"};

	public static final String[] FILTER_NAMES = new String[]{"BioPAX (*.owl)"};
}
