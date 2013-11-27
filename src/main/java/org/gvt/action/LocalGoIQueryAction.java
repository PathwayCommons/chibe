package org.gvt.action;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.query.QueryExecuter;
import org.gvt.ChisioMain;
import org.gvt.gui.GoIQueryParamDialog;
import org.gvt.gui.GoIQueryParamWithEntitiesDialog;
import org.gvt.util.BioPAXUtil;
import org.gvt.util.EntityHolder;
import org.eclipse.jface.dialogs.MessageDialog;
import org.biopax.paxtools.model.Model;
import org.gvt.util.QueryOptionsPack;
import org.patika.mada.graph.Node;
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
        if (main.getBioPAXModel() == null)
        {
            MessageDialog.openError(main.getShell(), "Error!",
                "Load or query a BioPAX model first!");

            return;
        }

		Set<BioPAXElement> source;

        //if action is called from PopupMenu
        if (useSelection)
        {
			if (main.getPathwayGraph() == null)
			{
				MessageDialog.openError(main.getShell(), "Error!",
					"This feature works only for BioPAX graphs");
			}

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
			source = main.getSelectedBioPAXElements();
		}
        //if action is called from TopMenuBar
        else
        {
            //open dialog
            GoIQueryParamWithEntitiesDialog dialog = new GoIQueryParamWithEntitiesDialog(
				main, BioPAXUtil.getEntities(main.getBioPAXModel()));

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
			source = BioPAXUtil.getContent(addedEntities);
        }

		Set<BioPAXElement> result = QueryExecuter.runPathsBetween(
			source, main.getBioPAXModel(), options.getLengthLimit());

        viewAndHighlightResult(result, options.isCurrentView(), "Query Result");
    }
}
