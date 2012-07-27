package org.gvt.inspector;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.TableItem;
import org.gvt.ChisioMain;
import org.gvt.model.GraphObject;
import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl3.Actor;

public class CBioPortalInspector extends Inspector {
    protected CBioPortalInspector(GraphObject model, String title, ChisioMain main) {
        super(model, title, main);
    }

    private void prepareForGraphObject() {
        org.patika.mada.graph.GraphObject go = (org.patika.mada.graph.GraphObject) model;
        if(go instanceof Actor) {
            for (String[] property : ((Actor) go).getDataInspectable()) {
                addRow(table, property[0]).setText(1, property[1]);
            }
            table.getColumn(1).pack();
            table.pack();
        }
    }

    private void show()
    {
        createContents(shell);

        shell.setLocation(calculateInspectorLocation(main.clickLocation.x, main.clickLocation.y));
        shell.open();
    }

    public static void getInstance(GraphObject model, String title, ChisioMain main)
    {
        if (isSingle(model)) {
            CBioPortalInspector inspector = new CBioPortalInspector(model, title, main);
            instances.add(inspector);

            if (model instanceof org.patika.mada.graph.GraphObject) {
                inspector.prepareForGraphObject();
            }

            inspector.show();
        }
    }


}
