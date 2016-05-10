package org.gvt.action;

import cpath.client.CPathClient;
import cpath.client.util.CPathException;
import cpath.query.CPathGetQuery;
import cpath.query.CPathGraphQuery;
import cpath.query.CPathSearchQuery;
import org.biopax.paxtools.controller.Cloner;
import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.paxtools.pattern.miner.SIFType;
import org.cbio.causality.network.PathwayCommons;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.model.EntityAssociated;
import org.gvt.model.GraphObject;
import org.gvt.model.NodeModel;
import org.gvt.model.basicsif.BasicSIFEdge;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.model.basicsif.BasicSIFGroup;
import org.gvt.model.basicsif.BasicSIFNode;
import org.gvt.model.sifl3.SIFEdge;
import org.gvt.model.sifl3.SIFGroup;
import org.gvt.util.Conf;
import org.gvt.util.QueryOptionsPack;
import org.patika.mada.graph.Graph;
import org.patika.mada.graph.Node;
import org.patika.mada.util.XRef;

import java.io.FileInputStream;
import java.util.*;

/**
 * @author Ozgun Babur
 *
 */
public abstract class QueryPCAction extends ChiBEAction
{
	/**
	 * Use selected nodes as source or not.
	 */
	protected boolean useSelected;

	/**
	 * Dialog options are stored, in order to use next time dialog is opened.
	 */
	protected QueryOptionsPack options;

	protected boolean increaseLimitIfNoResult;

	protected String newPathwayName;

	protected QueryLocation queryLoc;

	protected String localFilename;

	public QueryPCAction(ChisioMain main, String text, boolean useSelected, QueryLocation qLoc)
	{
		super(text, "icon/query-neighbors.png", main);
		options = new QueryOptionsPack();
		this.useSelected = useSelected;
		this.queryLoc = qLoc;
		increaseLimitIfNoResult = false;
		addFilterExtension(FILE_KEY, new String[]{queryLoc == QueryLocation.FILE_MECH ? "*.owl" : "*.sif"});
		addFilterName(FILE_KEY, new String[]{queryLoc == QueryLocation.FILE_MECH ?
			"BioPAX File (*.owl)" : "Simple Interaction File (*.sif)"});
	}

	public boolean isIncreaseLimitIfNoResult()
	{
		return increaseLimitIfNoResult;
	}

	public void setNewPathwayName(String newPathwayName)
	{
		this.newPathwayName = newPathwayName;
	}

	public void setIncreaseLimitIfNoResult(boolean increaseLimitIfNoResult)
	{
		this.increaseLimitIfNoResult = increaseLimitIfNoResult;
	}

	public void execute()
	{
		if (queryLoc.isFile() && localFilename == null)
		{
			localFilename = new FileChooser(this).choose(FILE_KEY);
			if (localFilename == null) return;
		}

        if(main.getBioPAXModel() == null || main.getBioPAXModel().getLevel().equals(BioPAXLevel.L3))
        {
            try
            {
                if (!(useSelected && setSelectedPEIDsAsSource()))
                    if (!showParameterDialog()) return;

                if (!canQuery()) return;

                main.lockWithMessage("Querying Pathway Commons ...");

				if (queryLoc.isSIF())
				{
					doSIFQuery();
				}
				else doMechanisticQuery();
            }
            catch (Exception e)
            {
                if (e instanceof CPathException ||
                    e.getCause() instanceof CPathException)
                {
                    alertNoResults();
                }
                else
                {
                    e.printStackTrace();
                    MessageDialog.openError(main.getShell(), "Error",
                        "An error occured during querying:\n" + e.getMessage());
                }
            }
            finally
            {
                main.unlock();
				localFilename = null;
            }
        }
        else
        {
            MessageDialog.openError(main.getShell(), "Incompatible Levels",
				"This query is only applicable to Level 3 models.");
        }
	}

	private void doSIFQuery() throws CPathException
	{
		org.cbio.causality.analysis.Graph g = null;
		if (queryLoc.isFile())
		{
			g = new org.cbio.causality.analysis.Graph("Local sif file", "mixed-edge-types");

		}
		BasicSIFGraph graph = queryLoc.isFile() ?  new BasicSIFGraph(g) : getPCGraph(options.getSifTypes());

		Collection<org.patika.mada.graph.GraphObject> gos = doSIFQuery(graph);

		if (gos.isEmpty())
		{
			alertNoResults();
			return;
		}

		BasicSIFGraph goi = (BasicSIFGraph) graph.excise(gos, true);
		goi.setName(getNewPathwayName());
		goi.setAsRoot();

		main.createNewTab(goi);

		new CoSELayoutAction(main).run();

		Set<String> seed = new HashSet<String>(options.getConvertedSourceList());
		seed.addAll(options.getConvertedTargetList());

		if (highlightSeed())
		{
			for (Object o : goi.getNodes())
			{
				NodeModel node = (NodeModel) o;
				if (seed.contains(node.getText()))
				{
					node.setHighlight(true);
				}
			}
		}
	}

	protected boolean highlightSeed()
	{
		return options.getLengthLimit() > 1;
	}

	private void doMechanisticQuery() throws CPathException
	{
		Model model;
		if (queryLoc == QueryLocation.FILE_MECH)
		{
			model = doFileQuery(localFilename);
		}
		else
		{
			model = doQuery();
		}

		if (model == null && increaseLimitIfNoResult)
		{
			options.setLengthLimit(options.getLengthLimit() + 1);
			model = doQuery();
		}

		main.unlock();

		if (model != null)
		{
			if (containsOnlyAnEmptyPathway(model))
			{
				alertEmptyPathway();
			}
			else if (!model.getObjects().isEmpty())
			{
				if (main.getBioPAXModel() != null)
				{
					MergeAction merge = new MergeAction(main, model);
					merge.setOpenPathways(true);
					boolean hasNonEmptyPathway = modelHasNonEmptyPathway(model);
					merge.setCreateNewPathway(!hasNonEmptyPathway);
					if (!hasNonEmptyPathway) merge.setNewPathwayName(getNewPathwayName());
					merge.updatePathways = false;
					merge.run();
				}
				else
				{
					LoadBioPaxModelAction load = new LoadBioPaxModelAction(main, model);
					load.setOpenPathways(true);

					if (!modelHasNonEmptyPathway(model)) load.setPathwayName(getNewPathwayName());
					load.run();
				}

				// Highlight source and target

				if (main.getPathwayGraph() != null)
				{
					Set<String> st = new HashSet<String>();
					if (options.getSourceList() != null)
						st.addAll(options.getSourceList());
					if (options.getTargetList() != null)
						st.addAll(options.getTargetList());

					if (options.isUseID())
					{
						HighlightWithEntityIDAction hac = new HighlightWithEntityIDAction(
							main, main.getPathwayGraph(), st);

						hac.run();
					}
					else
					{
						Set<XRef> refSet = new HashSet<XRef>();
						for (String name : st)
						{
							refSet.add(new XRef("Name", name));
						}

						HighlightWithRefAction hac = new HighlightWithRefAction(
							main, main.getPathwayGraph(), refSet);

						hac.run();
					}
				}
			}
			else
			{
				alertNoResults();
			}
		}
		else
		{
			alertNoResults();
		}
	}

	protected boolean containsOnlyAnEmptyPathway(Model model)
	{
		return !model.getObjects(Pathway.class).isEmpty() &&
			model.getObjects(Interaction.class).isEmpty();
	}

	protected String getNewPathwayName()
	{
		if (newPathwayName == null)
			return getText();
		else return newPathwayName;
	}

	protected void alertNoResults()
	{
		MessageDialog.openInformation(main.getShell(), "Empty result set", "No results found!");
	}

	protected void alertEmptyPathway()
	{
		MessageDialog.openInformation(main.getShell(), "Empty pathway",
			"The pathway has no content.");
	}

	/**
	 * Queries pathway commons, gets the model.
	 * @return
	 */
	protected abstract Model doQuery() throws CPathException;

	/**
	 * Queries pathway commons, gets the model.
	 * @return
	 */
	protected abstract Collection<org.patika.mada.graph.GraphObject> doSIFQuery(BasicSIFGraph graph)
		throws CPathException;

	/**
	 * Provides the parameter dialog that will display to get input.
	 * @return
	 */
	protected abstract AbstractQueryParamDialog getDialog();

	/**
	 * This method checks if enough input is collected from user.
	 * @return true if can continue to query
	 */
	protected abstract boolean canQuery();

	protected boolean showParameterDialog()
	{
		options.clearUnknownSymbols();
		AbstractQueryParamDialog dialog = getDialog();

		// If the dialog is null, then it is not needed
		if (dialog == null) return true;

		options = dialog.open(options);

		if (!options.isCancel())
		{
			options.setCancel(true);
		}
		else
		{
			return false;
		}
		return true;
	}
	
	protected boolean modelHasNonEmptyPathway(Model model)
	{
		for (Pathway p : model.getObjects(Pathway.class))
		{
			if (!p.getPathwayComponent().isEmpty()) return true;
		}
		return false;
	}
	
	protected Set<GraphObject> getSelectedObjects()
	{
		Set<GraphObject> selected = new HashSet<GraphObject>();

		ScrollingGraphicalViewer viewer = main.getViewer();
		Iterator selectedObjects = ((IStructuredSelection) viewer.getSelection()).iterator();

		while (selectedObjects.hasNext())
		{
			Object o = ((EditPart)selectedObjects.next()).getModel();

			if (o instanceof GraphObject)
			{
				selected.add((GraphObject) o);
			}
		}
		return selected;
	}
	
	protected boolean setSelectedPEIDsAsSource()
	{
		Set<String> set = new HashSet<String>();
		Set<GraphObject> selectedObjects = getSelectedObjects();
		for (GraphObject nm : selectedObjects)
		{
			if (nm instanceof BasicSIFEdge)
			{
				set.addAll(((BasicSIFEdge) nm).getMediators(selectedObjects));
			}
			else if (nm instanceof SIFEdge)
			{
				set.addAll(((SIFEdge) nm).getMediators(selectedObjects));
			}
			else if (nm instanceof BasicSIFGroup)
			{
				set.addAll(((BasicSIFGroup) nm).getMediators(selectedObjects));
			}
			else if (nm instanceof SIFGroup)
			{
				set.addAll(((SIFGroup) nm).getMediators(selectedObjects));
			}
			else if (nm instanceof EntityAssociated)
			{
				set.add(((EntityAssociated) nm).getEntity().getID());
			}
			else if (nm instanceof BasicSIFNode)
			{
				set.add(nm.getText());
			}
		}

		options.setSourceList(new ArrayList<String>(set));
		options.setUseID(true);
		return !options.getSourceList().isEmpty();
	}

	protected CPathClient getPCClient()
	{
		System.setProperty("cPath2Url", Conf.get(Conf.PATHWAY_COMMONS_URL));
		CPathClient pc2 = CPathClient.newInstance();

		// Setting endpoint url disabled temporarily. New PC client uses the latest url
		// automatically.
//		pc2.setEndPointURL(Conf.get(Conf.PATHWAY_COMMONS_URL));

		return pc2;
	}
	
	protected CPathGraphQuery getPCGraphQuery()
	{
		CPathClient client = getPCClient();
		CPathGraphQuery query = client.createGraphQuery();
//		query.mergeEquivalentInteractions(true);
		return query;
	}

	protected CPathSearchQuery getPCSearchQuery()
	{
		CPathClient client = getPCClient();
		CPathSearchQuery query = client.createSearchQuery();
		return query;
	}

	protected CPathGetQuery getPCGetQuery()
	{
		CPathClient client = getPCClient();
		CPathGetQuery query = client.createGetQuery();
//		query.mergeEquivalentInteractions(true);
		return query;
	}

	protected void warnForUnknownSymbols(List<String> unknown)
	{
		if (unknown != null && unknown.size() > 0)
		{
			String s = "Unknown symbol";
			
			if (unknown.size() > 1) s += "s";
			s += ":";

			for (String un : unknown)
			{
				s += "  \"" + un + "\"";
			}

			MessageDialog.openWarning(main.getShell(), "Some symbols are unfamiliar", s);
		}
	}

	protected void warnForLowInput(int required, int found)
	{
		MessageDialog.openWarning(main.getShell(), "Need more input", "Query needs at least " +
			required + " entities. Currently only " + found + " is recognized.");
	}

	protected Set<BioPAXElement> findRelatedReferences(Model model, Collection<String> symbols)
	{
		Set<BioPAXElement> set = new HashSet<BioPAXElement>();

		for (RelationshipXref xref : model.getObjects(RelationshipXref.class))
		{
			if (symbols.contains(xref.getId())) set.add(xref);
		}

		return set;
	}

	protected Model excise(Model model, Set<BioPAXElement> result)
	{
		Completer c = new Completer(SimpleEditorMap.L3);

		result = c.complete(result, model);

		Cloner cln = new Cloner(SimpleEditorMap.L3, BioPAXLevel.L3.getDefaultFactory());

		return cln.clone(model, result);
	}

	protected Model doFileQuery(String filename)
	{
		try
		{
			SimpleIOHandler handler = new SimpleIOHandler();
			Model model = handler.convertFromOWL(new FileInputStream(filename));
			Set<BioPAXElement> results = doFileQuery(model);
			return excise(model, results);
		}
		catch (Exception e)
		{
			//todo better handle empty results
			e.printStackTrace();
			return null;
		}
	}

	protected abstract Set<BioPAXElement> doFileQuery(Model model);

	protected Set<BioPAXElement> findInFile(Model model, Set<String> ids)
	{
		Set<BioPAXElement> result = new HashSet<BioPAXElement>();

		for (String id : ids)
		{
			BioPAXElement element = model.getByID(id);
			if (element != null) result.add(element);
		}
		return result;
	}

	//--------------------- Getting PC SIF graph --------------------------------------------------|

	public static BasicSIFGraph getPCGraph(List<? extends SIFType> types)
	{
		return new BasicSIFGraph(PathwayCommons.getGraph(types.toArray(new SIFType[types.size()])));
	}

	public static Set<Node> getSeed(Graph graph, Collection<String> symbols)
	{
		Set<Node> seed = new HashSet<Node>();
		for (Node node : graph.getNodes())
		{
			if (symbols.contains(node.getName())) seed.add(node);
		}
		return seed;
	}

	public enum QueryLocation
	{
		PC_MECH,
		PC_SIF,
		FILE_MECH,
		FILE_SIF;

		boolean isFile()
		{
			return this == FILE_MECH || this == FILE_SIF;
		}

		boolean isSIF()
		{
			return this == FILE_SIF || this == PC_SIF;
		}
	}
}
