package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.HighlightWithDataValuesDialog;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.IBioPAXNode;
import org.patika.mada.util.Representable;

import java.util.*;

/**
 * This class implements the action for highlighting nodes in the graph with respect to their expression and
 * alteration data values.
 *
 * @author Merve Cakir
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class HighlightWithDataValuesAction extends Action
{
    ChisioMain main;

    private BioPAXGraph graph;

    private double preMin = -Double.MAX_VALUE;
    private double preMax = Double.MAX_VALUE;

    public HighlightWithDataValuesAction(ChisioMain main)
    {
        super("Highlight With Data Values ...");

        this.main = main;
    }


    public void run()
    {
        graph = main.getPathwayGraph();

        if (graph == null)
        {
            return;
        }

        // Type of data currently loaded, ie. expression or alteration
        String type = graph.getLastAppliedColoring();

        // Warn user if there is no data loaded.
        if (type == null)
        {
            MessageDialog.openError(main.getShell(),
                "Error!", "Load data first.");

            return;
        }

        // Remove previous highlights before starting a new selection
        RemoveHighlightsAction rha = new RemoveHighlightsAction(main);
        rha.run();

        // Mapping between a node and its data value stored in tooltip text
        Map<IBioPAXNode, Double> valueMap = new HashMap<IBioPAXNode, Double>();

        for (Object obj : graph.getNodes())
        {
            if (obj instanceof IBioPAXNode)
            {
                IBioPAXNode node = (IBioPAXNode) obj;

                Representable data = node.getRepresentableData(type);

                if (data != null )
                {
                    String toolTip = data.getToolTipText();

                    if (toolTip != null && !toolTip.equals(BioPAXGraph.noDataText))
                    {
                        Double value = Double.parseDouble(toolTip);
                        valueMap.put(node, value);
                    }
                }
            }
        }

        if (valueMap.isEmpty())
        {
            MessageDialog.openWarning(main.getShell(), "No Data!",
                "There is not any node associated with experiment data. \nPlease load another data.");
            return;
        }

        // Boolean to control whether the loaded data is different from the previous one
        boolean newData = true;

        // Find minimum and maximum of data values to determine the boundaries in the dialog
        double maxValue = Collections.max(valueMap.values());
        double minValue = Collections.min(valueMap.values());

        if (maxValue == preMax && minValue == preMin)
        {
            newData = false;
        }

        // Open dialog
        HighlightWithDataValuesDialog dialog = new HighlightWithDataValuesDialog(main.getShell(), maxValue, minValue, newData);
        boolean okPressed = dialog.open();

        if (!okPressed)
        {
            return;
        }

        // Obtain user defined range of interest from the dialog (first element minimum, second element maximum)
        double[] results = dialog.getResultArray();

        // Highlight the nodes falling within the specified range
        for (IBioPAXNode node : valueMap.keySet())
        {
            // Within selected
            if(dialog.getRangeType() && (valueMap.get(node) >= results[0] && valueMap.get(node) <= results[1]))
            {
                node.setHighlight(true);
            }
            // Outside selected
            else if(!dialog.getRangeType() && (valueMap.get(node) < results[0] || valueMap.get(node) > results[1]))
            {
                node.setHighlight(true);
            }
        }

        // Store current minimum and maximum values to be compared with the upcoming ones.
        preMin = minValue;
        preMax = maxValue;
    }
}
