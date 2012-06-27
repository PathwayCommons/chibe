package org.gvt.action;

import cpath.client.PathwayCommonsIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
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
public class QueryPathwaysAction extends Action
{
	private ChisioMain main;

	/**
	 * Reference set to search. Only the first one that matches the db will be searched.
	 */
	private Set<XRef> refs;

	/**
	 * Flag for not asking the user reference to query but to use xref of the selected nodes.
	 */
	private boolean useSelectedNodes;

	public QueryPathwaysAction(ChisioMain main, boolean useSelectedNodes)
	{
		super("Pathways ...");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/query-pathways.png"));
		setToolTipText(getText());
		this.main = main;
		this.useSelectedNodes = useSelectedNodes;
	}

	public QueryPathwaysAction(ChisioMain main, Collection<XRef> refs)
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
        if(main.getOwlModel() == null || main.getOwlModel().getLevel().equals(BioPAXLevel.L2))
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
                StringInputDialog dialog = new StringInputDialog(main.getShell(), "Query Pathways",
                    "Enter UniProt or Entrez Gene ID", null,
                    "Find pathways related to the specified molecule");

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
                    Map<String, String> pathToID = new HashMap<String, String>();
                    List<String> resultPathways = new ArrayList<String>();

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
                        if (ref.getDb().equalsIgnoreCase(XRef.CPATH))
                        {
                            ioHandler.setInputIdType(PathwayCommonsIOHandler.ID_TYPE.CPATH_ID);
                        }
                        else if (ref.getDb().equalsIgnoreCase(XRef.UNIPROT))
                        {
                            ioHandler.setInputIdType(PathwayCommonsIOHandler.ID_TYPE.UNIPROT);
                        }
                        else if (ref.getDb().equalsIgnoreCase(XRef.ENTREZ_GENE))
                        {
                            ioHandler.setInputIdType(PathwayCommonsIOHandler.ID_TYPE.ENTREZ_GENE);
                        }
                        else
                        {
                            continue;
                        }

                        System.out.println("Querying pathways for " + ref);

                        List<List<String>> resultList = ioHandler.getPathways(ref.getRef());
                        main.unlock();

                        if (resultList.get(0).get(0).contains("xml"))
                        {
                            MessageDialog.openError(main.getShell(), "Error!", "Unexpected error!");
                            resultList.clear();
                        }
                        else if (resultList.get(1).size() == 2)
                        {
    //						MessageDialog.openInformation(main.getShell(), "No results",
    //							"No results found.");

                            resultList.clear();
                        }
                        else
                        {
                            resultList.remove(0);
                        }

                        // Prepare the result as listable pathway items

                        for (List<String> columns : resultList)
                        {
                            assert columns.size() == 4;

                            String cpathid = columns.get(3);

                            if (!pathToID.containsValue(cpathid))
                            {
                                String pathwayName = columns.get(1);
                                String db = columns.get(2);

                                String line = "[" + db + "] " + pathwayName;

                                resultPathways.add(line);
                                pathToID.put(line, cpathid);
                            }
                        }
                    }

                    if (!resultPathways.isEmpty())
                    {
                        ArrayList<String> selectedItems = new ArrayList<String>();

                        ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(),
                            500,
                            "Pathway Selection Dialog",
                            "Select pathways to retrieve",
                            resultPathways, selectedItems,
                            true, true, null);

                        dialog.setMinValidSelect(1);
                        dialog.open();

                        List<String> idList = new ArrayList<String>();

                        if (!dialog.isCancelled())
                        {
                            for (String item : selectedItems)
                            {
                                idList.add(pathToID.get(item));
                            }
                        }

                        if (!idList.isEmpty())
                        {
                            try
                            {
                                main.lockWithMessage("Querying Pathway Commons Database ...");
                                queryIDs(ioHandler, idList);
                            }
                            catch (Exception e){e.printStackTrace();}
                            finally { main.unlock(); }


                            new UpdatePathwayAction(main, true).run();
                            OpenPathwaysAction opa = new OpenPathwaysAction(main);
                            opa.setRefsToHighlight(refs);
                            opa.run();
                        }
                    }
                    else if (!refs.isEmpty())
                    {
                        MessageDialog.openInformation(main.getShell(), "Not found!",
                            "No pathway found.");
                    }
                }
                catch (Exception e)
                {
                    refs = null;
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
        }
        else
        {
            MessageDialog.openError(main.getShell(), "Incompatible Levels","This query is only applicable to Level 2 models.");
        }

	}

	private void queryIDs(PathwayCommonsIOHandler ioHandler, List<String> idList) throws IOException
	{
		for (String id : idList)
		{
			ioHandler.setInputIdType(PathwayCommonsIOHandler.ID_TYPE.CPATH_ID);
			Model resultModel = ioHandler.retrieveByID(id);

			if (resultModel != null && !resultModel.getObjects().isEmpty())
			{
				if (main.getOwlModel() != null)
				{
					MergeAction merge = new MergeAction(main, resultModel);
					merge.setOpenPathways(false);
					merge.setUpdatePathways(false);
					merge.run();
				}
				else
				{
					LoadBioPaxModelAction load = new LoadBioPaxModelAction(main, resultModel);
					load.setOpenPathways(false);
					load.run();
				}
			}
		}
	}
}