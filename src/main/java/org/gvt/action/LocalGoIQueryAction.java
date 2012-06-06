package org.gvt.action;

import org.gvt.ChisioMain;
import org.gvt.gui.GoIQueryParamDialog;
import org.gvt.gui.GoIQueryParamWithEntitiesDialog;
import org.gvt.util.EntityHolder;
import org.eclipse.jface.dialogs.MessageDialog;
import org.biopax.paxtools.model.Model;
import org.gvt.util.QueryOptionsPack;
import org.patika.mada.graph.Node;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.algorithm.LocalPoIQuery;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * This class creates the action for opening query properties window.
 *
 * @author Merve Cakir
 */
public class LocalGoIQueryAction extends AbstractLocalQueryAction
{
    /**
     * Dialog options are stored, in order to use next time dialog is opened.
     */
    QueryOptionsPack options;

    /**
     * To determine whether action is called from
     * topMenuBar(false) or popUpManager(true)
     */
    private boolean useSelection;

    /**
     * Constructor
     */
    public LocalGoIQueryAction(ChisioMain main, boolean useSelection)
    {
        super(main, "Paths Between ...");
        setToolTipText(getText());
        options = new QueryOptionsPack();
        this.main = main;
        this.useSelection = useSelection;
    }

    public void run()
    {
        LocalGoI();
    }

    public void LocalGoI()
    {
        Model owlModel = this.main.getOwlModel();

        if (owlModel == null)
        {
            MessageDialog.openError(main.getShell(), "Error!",
                "Load or query a BioPAX model first!");

            return;
        }

        Set<Node> sourceNodes = new HashSet<Node>();

        //if action is called from PopupMenu
        if (useSelection)
        {
            //open dialog
            GoIQueryParamDialog dialog = new GoIQueryParamDialog(this.main);
            options = dialog.open(options);

            //Return if Cancel was pressed
            if ( !options.isCancel() )
            {
                options.setCancel(true);
            }
            else
            {
                return;
            }

            //Get Selected Nodes in graph
            sourceNodes = getSelectedNodes();
        }
        //if action is called from TopMenuBar
        else
        {
            //open dialog
            GoIQueryParamWithEntitiesDialog dialog =
                new GoIQueryParamWithEntitiesDialog(main, main.getAllEntities());
            options = dialog.open(options);

            if ( !options.isCancel() )
            {
                options.setCancel(true);
            }
            else
            {
                return;
            }

            //Get added entities to the list
            List<EntityHolder> addedEntities = dialog.getAddedSourceEntities();

            //Get the states of added entities.
            Set<Node> sourceSet =
                main.getRootGraph().getRelatedStates(addedEntities);

            //Replace any complex member with its corresponding complex.
            main.getRootGraph().replaceComplexMembersWithComplexes(sourceSet);

            for (GraphObject go : sourceSet)
            {
                if (go instanceof Node)
                {
                    sourceNodes.add((Node) go);
                }
            }
        }

        Set<GraphObject> result = new HashSet<GraphObject>();

        /**
         * GoI finds the paths between a set of nodes, thus running GoI is equal
         * to giving the source nodes to PoI as both source set and target set.
         */
        LocalPoIQuery poi = new LocalPoIQuery(sourceNodes, sourceNodes,
            true, options.getLengthLimit(), false);
        result.addAll(poi.run());

        viewAndHighlightResult(result,
            options.isCurrentView(),
            "Graph of Interest");
    }
}
