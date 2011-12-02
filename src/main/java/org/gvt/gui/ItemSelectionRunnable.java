package org.gvt.gui;

import java.util.Collection;

/**
 * A runnable interface to use with ItemSelectionDialog. When the selection is changed, this
 * runnable is executed.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public interface ItemSelectionRunnable
{
	public void run(Collection<String> selectedTerms);
}
