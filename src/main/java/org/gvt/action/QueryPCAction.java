package org.gvt.action;

import cpath.client.CPath2Client;
import cpath.client.util.CPathException;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.gvt.ChisioMain;
import org.gvt.gui.AbstractQueryParamDialog;
import org.gvt.model.EntityAssociated;
import org.gvt.model.NodeModel;
import org.gvt.util.Conf;
import org.gvt.util.QueryOptionsPack;
import org.patika.mada.util.XRef;

import java.util.*;

/**
 * @author Ozgun Babur
 *
 */
public abstract class QueryPCAction extends Action
{
	protected ChisioMain main;

	/**
	 * Use selected nodes as source or not.
	 */
	protected boolean useSelected;

	/**
	 * Dialog options are stored, in order to use next time dialog is opened.
	 */
	protected QueryOptionsPack options;

	public QueryPCAction(ChisioMain main, String text, boolean useSelected)
	{
		super(text);
		setImageDescriptor(ImageDescriptor.createFromFile(
			ChisioMain.class, "icon/query-neighbors.png"));
		setToolTipText(getText());
		this.main = main;
		options = new QueryOptionsPack();
		this.useSelected = useSelected;
	}

	public void execute()
	{
        if(main.getOwlModel() == null || main.getOwlModel().getLevel().equals(BioPAXLevel.L3))
        {
            try
            {
                if (!(useSelected && setSelectedPEIDsAsSource()))
                    if (!showParameterDialog()) return;

                if (!canQuery()) return;

                main.lockWithMessage("Querying Pathway Commons ...");
                Model model = doQuery();
                main.unlock();

                if (model != null)
                {
                    if (!model.getObjects().isEmpty())
                    {
                        if (main.getOwlModel() != null)
                        {
                            MergeAction merge = new MergeAction(main, model);
                            merge.setOpenPathways(true);
							boolean hasNonEmptyPathway = modelHasNonEmptyPathway(model);
                            merge.setCreateNewPathway(!hasNonEmptyPathway);
                            if (!hasNonEmptyPathway) merge.setNewPathwayName(getText());
							merge.updatePathways = false;
                            merge.run();
                        }
                        else
                        {
                            LoadBioPaxModelAction load = new LoadBioPaxModelAction(main, model);
                            load.setOpenPathways(true);

                            if (!modelHasNonEmptyPathway(model)) load.setPathwayName(getText());
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
            }
        }
        else
        {
            MessageDialog.openError(main.getShell(), "Incompatible Levels","This query is only applicable to Level 3 models.");
        }
	}

	protected void alertNoResults()
	{
		MessageDialog.openInformation(main.getShell(), "Empty result set", "No results found!");
	}

	/**
	 * Queries pathway commons, gets the model.
	 * @return
	 */
	protected abstract Model doQuery() throws CPathException;

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
	
	protected Set<NodeModel> getSelectedNodes()
	{
		Set<NodeModel> selected = new HashSet<NodeModel>();

		ScrollingGraphicalViewer viewer = main.getViewer();
		Iterator selectedObjects = ((IStructuredSelection) viewer.getSelection()).iterator();

		while (selectedObjects.hasNext())
		{
			Object o = ((EditPart)selectedObjects.next()).getModel();

			if (o instanceof NodeModel)
			{
				selected.add((NodeModel) o);
			}
		}
		return selected;
	}
	
	protected boolean setSelectedPEIDsAsSource()
	{
		Set<String> set = new HashSet<String>();
		for (NodeModel nm : getSelectedNodes())
		{
			if (nm instanceof EntityAssociated)
			{
				set.add(((EntityAssociated) nm).getEntity().getID());
			}
		}
		options.setSourceList(new ArrayList<String>(set));
		options.setUseID(true);
		return !options.getSourceList().isEmpty();
	}

	protected CPath2Client getPCClient()
	{
		System.setProperty("cPath2Url", Conf.get(Conf.PATHWAY_COMMONS_URL));
		CPath2Client pc2 = CPath2Client.newInstance();

		// Setting endpoint url disabled temporarily. New PC client uses the latest url
		// automatically.
//		pc2.setEndPointURL(Conf.get(Conf.PATHWAY_COMMONS_URL));

		pc2.setMergeEquivalentInteractions(true);
		return pc2;
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
}
