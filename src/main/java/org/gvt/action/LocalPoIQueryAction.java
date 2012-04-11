package org.gvt.action;

import org.gvt.gui.PoIQueryParamWithEntitiesDialog;
import org.gvt.util.EntityHolder;
import org.gvt.ChisioMain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.util.QueryOptionsPack;
import org.patika.mada.graph.Node;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.algorithm.LocalPoIQuery;
import org.biopax.paxtools.model.Model;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * This class creates the action for opening query properties window.
 *
 * @author Merve Cakir
 */
public class LocalPoIQueryAction extends AbstractLocalQueryAction
{
    ChisioMain main;

  	/**
	 * Dialog options are stored, in order to use next time dialog is opened.
	 */
	QueryOptionsPack options;

	/**
	 * Constructor
	 */
	public LocalPoIQueryAction(ChisioMain main)
	{
		super(main, "Paths Of Interest");
		setToolTipText(getText());
		options = new QueryOptionsPack();
		this.main = main;
    }

    public void run()
    {
        LocalPoI();
    }

    public void LocalPoI()
    {
        Model owlModel = this.main.getOwlModel();

        if (owlModel == null)
        {
            MessageDialog.openError(main.getShell(), "Error!",
                "Load or query a BioPAX model first!");

            return;
        }

        //Source and target node sets

        Set<Node> sourceNodes = new HashSet<Node>();
        Set<Node> targetNodes = new HashSet<Node>();

        //open dialog
        PoIQueryParamWithEntitiesDialog dialog = new PoIQueryParamWithEntitiesDialog(
			main, main.getAllEntities());

        options = dialog.open(options);

        if ( !options.isCancel() )
		{
			options.setCancel(true);
		}
		else
		{
			return;
		}

        //Get added source entities.
        List<EntityHolder> sourceAddedEntities = dialog.getAddedSourceEntities();

        //Get the states of added entities.
        Set<Node> sourceSet = main.getRootGraph().getRelatedStates(sourceAddedEntities);

        //Replace any complex member with its corresponding complex.
        main.getRootGraph().replaceComplexMembersWithComplexes(sourceSet);

        for (GraphObject go : sourceSet)
        {
            if (go instanceof Node)
            {
                sourceNodes.add((Node) go);
            }
        }

        //Get added target entities.
        List<EntityHolder> targetAddedEntities = dialog.getAddedTargetEntities();

        //Get the states of added entities.
        Set<Node> targetSet =
            main.getRootGraph().getRelatedStates(targetAddedEntities);

        //Replace any complex member with its corresponding complex.
        main.getRootGraph().replaceComplexMembersWithComplexes(targetSet);

        for (GraphObject go : targetSet)
        {
            if (go instanceof Node)
            {
                targetNodes.add((Node) go);
            }
        }

        LocalPoIQuery poi;
        Set<GraphObject> result = new HashSet<GraphObject>();

        //if length limit is selected and strict is unchecked.
        if (options.getLimitType() && !options.isStrict())
        {
            poi = new LocalPoIQuery(sourceNodes, targetNodes,
                true, options.getLengthLimit(), false);
        }
        //if length limit is selected and strict is checked.
        else if (options.getLimitType() && options.isStrict())
        {
            poi = new LocalPoIQuery(sourceNodes, targetNodes,
                true, options.getLengthLimit(), true);
        }
        //if shortest+k is selected and strict is unchecked.
        else if (!options.getLimitType() && !options.isStrict())
        {
            poi = new LocalPoIQuery(sourceNodes, targetNodes,
                false, options.getShortestPlusKLimit(), false);
        }
        //if shortest+k is selected and strict is checked.
        else
        {
            poi = new LocalPoIQuery(sourceNodes, targetNodes,
                false, options.getShortestPlusKLimit(), true);
        }

        //Add result of PoI to the result set
        result.addAll(poi.run());

        //if no result can be found, open dialog to warn.
        if (result.size() == 0)
        {
            MessageDialog.openWarning(main.getShell(),
                "No result!",
                "No path can be found with specified parameters");
        }
        else
        {
            viewAndHighlightResult(result,
                options.isCurrentView(),
                "Paths of Interest");
        }
    }
}





	

