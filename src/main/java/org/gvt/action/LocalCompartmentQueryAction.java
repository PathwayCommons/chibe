package org.gvt.action;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.LimitType;
import org.gvt.ChisioMain;
import org.gvt.gui.CompartmentQueryParamWithEntitiesDialog;
import org.gvt.model.CompoundModel;
import org.gvt.model.EntityAssociated;
import org.eclipse.jface.dialogs.MessageDialog;
import org.biopax.paxtools.model.Model;
import org.gvt.util.BioPAXUtil;
import org.gvt.util.QueryOptionsPack;
import org.patika.mada.graph.Node;
import org.patika.mada.graph.GraphObject;
import org.patika.mada.algorithm.LocalPoIQuery;

import java.util.List;
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
		super(main, "Compartment ...");
		setToolTipText(getText());
		options = new QueryOptionsPack();
		this.main = main;
    }

    public void run()
    {
        if (main.getBioPAXModel() == null)
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

		Set<String> source = new HashSet<String>(dialog.getSourceAddedCompartments());
        Set<String> target = new HashSet<String>(dialog.getTargetAddedCompartments());

		Set<BioPAXElement> result = QueryExecuter.runPathsFromTo(
			BioPAXUtil.getElementsAtLocations(main.getBioPAXModel(), source),
			BioPAXUtil.getElementsAtLocations(main.getBioPAXModel(), target),
			main.getBioPAXModel(),
			options.getLimitType() ? LimitType.NORMAL : LimitType.SHORTEST_PLUS_K,
			options.getLengthLimit());

		viewAndHighlightResult(result, options.isCurrentView(), "Query Result");
    }
}









