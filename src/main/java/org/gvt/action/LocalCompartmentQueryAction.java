package org.gvt.action;

import org.gvt.ChisioMain;
import org.gvt.gui.CompartmentQueryParamWithEntitiesDialog;
import org.gvt.model.biopaxl2.Compartment;
import org.gvt.model.EntityAssociated;
import org.eclipse.jface.dialogs.MessageDialog;
import org.biopax.paxtools.model.Model;
import org.gvt.util.QueryOptionsPack;
import org.patika.mada.graph.Node;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.algorithm.LocalPoIQuery;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * This class creates the action for opening compartment query properties
 * window.
 *
 * @author Merve Cakir
 */
public class LocalCompartmentQueryAction extends AbstractLocalQueryAction
{
    ChisioMain main;

  	/**
	 * Dialog options are stored, in order to use next time dialog is opened.
	 */
	QueryOptionsPack options;

	/**
	 * Constructor
	 */
	public LocalCompartmentQueryAction(ChisioMain main)
	{
		super(main, "Compartment Query");
		setToolTipText(getText());
		options = new QueryOptionsPack();
		this.main = main;
    }

    public void run()
    {
        Model owlModel = this.main.getOwlModel();

        if (owlModel == null)
        {
            MessageDialog.openError(main.getShell(), "Error!",
                "Load or query a BioPAX model first!");

            return;
        }

        //open dialog
        CompartmentQueryParamWithEntitiesDialog dialog =
			new CompartmentQueryParamWithEntitiesDialog(main);
        options = dialog.open(options);

        if ( !options.isCancel() )
		{
			options.setCancel(true);
		}
		else
		{
			return;
		}

        //Source and target node sets

        Set<Node> sourceNodes = new HashSet<Node>();
        Set<Node> targetNodes = new HashSet<Node>();

		//Get added source compartments.
        ArrayList<Compartment> sourceAddedCompartments =
			dialog.getSourceAddedCompartments();

		Set<GraphObject> sourceSet = new HashSet<GraphObject>();
		//Get nodes and edges from source added compartments.
		for (Compartment compartment : sourceAddedCompartments)
		{
			sourceSet.addAll(compartment.getChildren());
		}

		//Select the nodes that are entity associated
		for (GraphObject go : sourceSet)
        {
            if (go instanceof EntityAssociated)
            {
                sourceNodes.add((Node)go);
            }
        }

		//Get added target compartments.
        ArrayList<Compartment> targetAddedCompartments =
			dialog.getTargetAddedCompartments();

        Set<GraphObject> targetSet = new HashSet<GraphObject>();
		//Get nodes and edges from target added compartments.
		for (Compartment compartment : targetAddedCompartments)
		{
			targetSet.addAll(compartment.getChildren());
		}

		//Select the nodes that are entity associated
        for (GraphObject go : targetSet)
        {
            if (go instanceof EntityAssociated)
            {
                targetNodes.add((Node) go);
            }
        }

		/**
		 * To find the paths between compartments, PoI will be run from
		 * the nodes in the source compartment to the nodes in the target
		 * compartment
		 */
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

        //Run PoI and add result of PoI to the result set
        result.addAll(poi.run());

        //if no result can be found, open dialog to warn.
        if (result.size() == 0)
        {
            MessageDialog.openWarning(main.getShell(),
                "No result!",
                "No path can be found with specified parameters!");
        }
        else
        {
            viewAndHighlightResult(result,
                options.isCurrentView(),
                "Compartment");
        }
    }
}









