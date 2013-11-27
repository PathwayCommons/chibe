package org.gvt.action;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.gvt.ChisioMain;
import org.gvt.gui.ConnectToDAVIDDialog;
import org.gvt.util.SystemBrowserDisplay;
import org.patika.mada.graph.Node;
import org.patika.mada.util.XRef;

import java.util.*;

/**
 * Performs the required actions for connecting to DAVID. User selects nodes on the view and then based on the type
 * of analysis tool, a URL following DAVID API syntax is prepared. 
 *
 * @author Merve Cakir
 */
public class ConnectToDAVIDAction extends Action
{
    private ChisioMain main;

    /**
     * Name of the selected analysis tool
     */
    private String toolName;

    /**
     * Type of ID that selected genes share, such as Entrez Gene ID or Uniprot ID
     */
    private String idType;

    /**
     * URL following DAVID API syntax
     */
    private String resultURL;

    private Map<String,List<String>> mappingResult;

    private static Map<String,String> refMatching;

    public ConnectToDAVIDAction(ChisioMain main)
    {
        super("Connect To DAVID ...");
        this.main = main;

        this.toolName = null;
        this.idType = null;
        this.resultURL = null;
    }

    public void run()
    {
        if(main.getBioPAXModel() == null)
        {
             MessageDialog.openError(main.getShell(), "Error!", "Load or query a BioPAX model first!");
             return;
        }

        // Find the nodes selected by the user
        Set<Node> selected = getSelectedNodes();

        if (selected.isEmpty())
        {
            MessageDialog.openError(main.getShell(), "No Selection!", "Select node(s) first!");
            return;
        }

        ConnectToDAVIDDialog dialog = new ConnectToDAVIDDialog(main);

        if(dialog.open())
        {
            //Get the selected tool of analysis
            toolName = dialog.getToolName();

            Map<String,String> knownReferences = getReferenceMatching();

            // For DAVID reference names that has a common counterpart in ChiBE references, an entry in map is created.
            mappingResult = new HashMap<String,List<String>>();
            mappingResult.put("ENTREZ_GENE_ID", new ArrayList<String>());
            mappingResult.put("REFSEQ_PROTEIN", new ArrayList<String>());
            mappingResult.put("GENE_SYMBOL", new ArrayList<String>());
            mappingResult.put("UNIPROT_ACCESSION", new ArrayList<String>());


            for (Node node : selected)
            {
                // Map to hold unique reference types and their associated values
                Map<String,String> references = new HashMap<String,String>();

                // Extract all reference types along with the IDs associated with the node
                for (XRef ref : node.getReferences())
                {
                    references.put(ref.getDb(), ref.getRef());
                }
                for (XRef ref2 : node.getSecondaryReferences())
                {
                    references.put(ref2.getDb(), ref2.getRef());
                }

                // If the node has reference types that are same as references specified by DAVID, put the
                // reference value into the bucket specific for that reference type
                for (String reference : references.keySet())
                {
                    if(knownReferences.containsKey(reference))
                    {
                        mappingResult.get(knownReferences.get(reference)).add(references.get(reference));
                    }
                }
            }

            /**
             * Identify the reference type that has the most coverage within the selected nodes.
             * Stop at the first type that has reference values for each selected nodes, if no such type exists
             * obtain the one with the highest number of nodes.
             */

            int maxResult = 0;
            String maxEntry = null;
            for (String key : mappingResult.keySet())
            {
                if(mappingResult.get(key).size() > maxResult)
                {
                    maxResult = mappingResult.get(key).size();
                    maxEntry = key;
                    if(maxResult == selected.size())
                    {
                        break;
                    }
                }
            }

            // Continue only if there is a matching between reference types between DAVID and selected nodes
            if (maxEntry != null)
            {
                // If only a subset of nodes' references can be recovered, ask the user's decision for use of this subset.
                if (maxResult < selected.size())
                {
                    boolean isOk = MessageDialog.openQuestion(main.getShell(), "Incomplete Set!",
                            "Only " + maxResult + " of the selected " + selected.size() + " nodes share a common reference type."
                                    + "\nDo you wish to continue with this subset?");

                    if (!isOk)
                    {
                        return;
                    }
                }

                idType = maxEntry;

                resultURL = "http://david.abcc.ncifcrf.gov/api.jsp?type=" + idType + "&ids=";

                for (String id : mappingResult.get(idType))
                {
                    resultURL += id + ",";
                }

                resultURL = resultURL.substring(0, resultURL.length() - 1) + "&tool=" + toolName;

                System.out.println(resultURL);
                SystemBrowserDisplay.openURL(resultURL);
            }
            else
            {
                MessageDialog.openWarning(main.getShell(), "No Type Matching!",
                        "The selected nodes do not have references in types recognized by DAVID.");
            }
        }
    }

    /**
     * Find and return the nodes that have been selected on the view.
     */
    private Set<Node> getSelectedNodes()
    {
        Set<Node> selected = new HashSet<Node>();

        ScrollingGraphicalViewer viewer = main.getViewer();
        Iterator selectedObjects = ((IStructuredSelection) viewer.getSelection()).iterator();

        while (selectedObjects.hasNext())
        {
            Object o = ((EditPart)selectedObjects.next()).getModel();

            if (o instanceof Node)
            {
                selected.add((Node) o);
            }
        }
        return selected;
    }

    /**
     * Maps the reference ID types required by DAVID to their counterparts commonly encountered in ChiBE
     * (can be extended) 
     */
    private static Map<String,String> getReferenceMatching()
    {
        if(refMatching != null)
        {
            return refMatching;
        }

        refMatching = new HashMap<String,String>();

        refMatching.put("Entrez Gene", "ENTREZ_GENE_ID");
        refMatching.put("ENTREZ_GENE", "ENTREZ_GENE_ID");
        refMatching.put("Refseq", "REFSEQ_PROTEIN");
        refMatching.put("RefSeq", "REFSEQ_PROTEIN");
        refMatching.put("REF_SEQ", "REFSEQ_PROTEIN");
        refMatching.put("GENE_SYMBOL", "GENE_SYMBOL");
        refMatching.put("uniprot", "UNIPROT_ACCESSION");
        refMatching.put("UNIPROT", "UNIPROT_ACCESSION");
        refMatching.put("UniProt", "UNIPROT_ACCESSION");

        return refMatching;
    }
}
