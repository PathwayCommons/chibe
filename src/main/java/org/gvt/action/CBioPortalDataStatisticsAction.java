package org.gvt.action;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.gvt.ChisioMain;
import org.gvt.inspector.CBioPortalInspector;
import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl3.Actor;

import java.util.*;

public class CBioPortalDataStatisticsAction extends Action {
    protected ChisioMain main;

    public CBioPortalDataStatisticsAction(ChisioMain main) {
        super("Show cBio Portal Data Details");
        setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbio_portal.png"));
        setToolTipText(getText());
        this.main = main;
    }

    public void run() {
        execute();
    }

    public void execute() {
        Set<NodeModel> selectedNodes = getSelectedNodes();
        for (NodeModel node : selectedNodes) {
            if(node instanceof Actor) {
                CBioPortalInspector.getInstance(node, ((Actor) node).getName() + ": Data Details", main);
            }
        }

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

}
