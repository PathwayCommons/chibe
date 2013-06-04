package org.gvt;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.AbstractTransferDragSourceListener;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.gvt.model.NodeModel;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsFileDragSourceListener
	extends AbstractTransferDragSourceListener {

	public ChsFileDragSourceListener(EditPartViewer viewer) {
		super(viewer, FileTransfer.getInstance());
	}

	public void dragStart(DragSourceEvent event) {
		if (!(getViewer().getEditDomain().getActiveTool()
			instanceof FileMoveTool))
			event.doit = false;
	}

	public void dragSetData(DragSourceEvent event) {
		NodeModel model =
			(NodeModel) ((EditPart) getViewer().getSelectedEditParts().get(0))
				.getModel();
		event.data = new String[] { model.getText()};
	}
}
