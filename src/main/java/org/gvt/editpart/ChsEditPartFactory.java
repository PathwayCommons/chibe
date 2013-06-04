package org.gvt.editpart;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.gvt.model.*;

/**
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class ChsEditPartFactory implements EditPartFactory
{
	/* ( Javadoc)
	 * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
	 */
	public EditPart createEditPart(EditPart context, Object model)
	{
		EditPart part = null;

		if (model instanceof CompoundModel)
		{
			if (((CompoundModel) model).isRoot())
			{
				part = new ChsRootEditPart();
			}
			else
			{
				part = new ChsCompoundEditPart();
			}
		}			
		else if (model instanceof NodeModel)
			part = new ChsNodeEditPart();
		else if (model instanceof EdgeModel)
			part = new ChsEdgeEditPart();

		part.setModel(model);

		return part;
	}
}