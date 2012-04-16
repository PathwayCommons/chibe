package org.gvt.action;

import cpath.client.PathwayCommonsIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.StringInputDialog;
import org.gvt.model.biopaxl2.Actor;
import org.gvt.model.biopaxl2.Complex;
import org.patika.mada.util.XRef;

import java.io.IOException;
import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class QueryNeighborsAction extends Action
{
	private ChisioMain main;

	/**
	 * Reference set to search. Only the first one that matches the db will be searched.
	 */
	private Set<XRef> refs;

	/**
	 * Database name.
	 */
	private String db;

	/**
	 * Flag for not asking the user reference to query but to use xref of the selected nodes.
	 */
	private boolean useSelectedNodes;

	/**
	 * Where to display
	 */
	private boolean newView;

	public QueryNeighborsAction(ChisioMain main, boolean useSelectedNodes)
	{
		super("Neighborhood");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/query-neighbors.png"));
		setToolTipText(getText());
		this.main = main;
		this.newView = true;
		this.useSelectedNodes = useSelectedNodes;
	}

	public QueryNeighborsAction(ChisioMain main, Collection<XRef> refs)
	{
		this(main, false);
		this.refs = new HashSet<XRef>(refs);
	}

	public boolean isUseSelectedNodes()
	{
		return useSelectedNodes;
	}

	public void setUseSelectedNodes(boolean useSelectedNodes)
	{
		this.useSelectedNodes = useSelectedNodes;
	}

	public void run()
	{
        if(main.getOwlModel().getLevel().equals(BioPAXLevel.L2))
        {
            if (refs == null)
            {
                refs = new HashSet<XRef>();

                if (useSelectedNodes && main.getViewer() != null)
                {
                    for (Object o : main.getSelectedModel())
                    {
                        if (o instanceof Actor)
                        {
                            refs.addAll(((Actor) o).getReferences());
                        }
                        else if (o instanceof Complex)
                        {
                            refs.addAll(((Complex) o).getReferences());
                        }
                    }
                }
            }

            if (refs.isEmpty() && !useSelectedNodes)
            {
                StringInputDialog dialog = new StringInputDialog(main.getShell(), "Query Neighborhood",
                    "Enter UniProt or Entrez Gene ID", null);

                String ids = dialog.open();

                if (ids != null && ids.trim().length() > 0)
                {
                    for (String id : ids.split(" "))
                    {
                        if (id.length() < 1) continue;

                        String dbtext = Character.isDigit(id.charAt(0)) ?
                            XRef.ENTREZ_GENE : XRef.UNIPROT;

                        refs.add(new XRef(dbtext + ":" + id));
                    }
                }
            }

            if (!refs.isEmpty())
            {
                try
                {
                    main.lockWithMessage("Querying Pathway Commons Database ...");
                    PathwayCommonsIOHandler ioHandler = new PathwayCommonsIOHandler(new SimpleIOHandler());

                    // Names of new pathways for the neighborhood
                    Map<XRef, String> ref2pname = new HashMap<XRef, String>();

                    // Query for only one xref

                    XRef xr = XRef.getFirstRef(refs,
                        new String[]{XRef.CPATH, XRef.ENTREZ_GENE, XRef.UNIPROT});

                    refs.clear();
                    if (xr != null)
                    {
                        refs.add(xr);
                    }
                    else
                    {
                        MessageDialog.openError(main.getShell(), "No Reference ID",
                            "No CPATH, Entrez Gene or UniProt ID found to query.");
                    }

                    for (XRef ref : refs)
                    {
                        if ((db == null || db.equals(XRef.CPATH)) &&
                            ref.getDb().equalsIgnoreCase(XRef.CPATH))
                        {
                            ioHandler.setInputIdType(PathwayCommonsIOHandler.ID_TYPE.CPATH_ID);
                        }
                        else if ((db == null || db.equals(XRef.UNIPROT)) &&
                            ref.getDb().equalsIgnoreCase(XRef.UNIPROT))
                        {
                            ioHandler.setInputIdType(PathwayCommonsIOHandler.ID_TYPE.UNIPROT);
                        }
                        else if ((db == null || db.equals(XRef.ENTREZ_GENE)) &&
                            ref.getDb().equalsIgnoreCase(XRef.ENTREZ_GENE))
                        {
                            ioHandler.setInputIdType(PathwayCommonsIOHandler.ID_TYPE.ENTREZ_GENE);
                        }
                        else
                        {
                            continue;
                        }

                        System.out.println("Querying neighbors for " + ref);
                        Model resultModel = ioHandler.getNeighbors(ref.getRef());

                        main.unlock();

                        if (resultModel != null && !resultModel.getObjects().isEmpty())
                        {
                            String pname = "Neighborhood for " + ref;

                            if (main.getOwlModel() != null)
                            {
                                MergeAction merge = new MergeAction(main, resultModel);
                                merge.setOpenPathways(false);
                                merge.setCreateNewPathway(true);
                                merge.setNewPathwayName(pname);
                                merge.run();
                                ref2pname.put(ref, merge.getNewPathwayName());
                            }
                            else
                            {
                                LoadBioPaxModelAction load = new LoadBioPaxModelAction(main, resultModel);
                                load.setOpenPathways(false);

                                load.setPathwayName(pname);
                                load.run();
                                ref2pname.put(ref, load.getPathwayName());
                            }
                        }
                        else
                        {
                            MessageDialog.openInformation(main.getShell(), "Not found!",
                                "Nothing found!");
                        }
                    }

                    assert main.getAllPathwayNames().containsAll(ref2pname.values()) :
                        "New pathway names are not in allPathwayNames";

                    // Open new neighborhood pathways

                    for (XRef ref : ref2pname.keySet())
                    {
                        ArrayList<String> nms = new ArrayList<String>();
                        nms.add(ref2pname.get(ref));
                        OpenPathwaysAction opa = new OpenPathwaysAction(main, nms);
                        List<XRef> refL = new ArrayList<XRef>();
                        refL.add(ref);
                        opa.setRefsToHighlight(refL);
                        opa.run();
                    }

                    refs = null;
                    return;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    MessageDialog.openError(main.getShell(), "Error",
                        "An error occured during querying:\n" + e.getMessage());
                }
                finally
                {
                    main.unlock();
                }
		    }

		    refs = null;
		    db = null;
	    }
        else
        {
            MessageDialog.openError(main.getShell(), "Incompatible Levels","This query is only applicable to Level 2 models.");
        }
    }

}
