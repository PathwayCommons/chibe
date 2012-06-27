package org.gvt;

import java.io.File;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.tools.SelectionTool;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.gvt.model.NodeModel;
import org.gvt.editpart.ChsNodeEditPart;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class FileMoveTool extends SelectionTool {

	public boolean handleNativeDragStarted(DragSourceEvent event) {

		EditPart part = getTargetEditPart();
		String path = ((NodeModel) part.getModel()).getText();
		if (part instanceof ChsNodeEditPart || path != null) {
			File file = new File(path);
			if (!file.exists())
				event.doit = false; 
			else {
				event.data = new String[] { path };
			}
			return true;
		}
		event.doit = false;
		return super.handleNativeDragStarted(event);
	}
}

