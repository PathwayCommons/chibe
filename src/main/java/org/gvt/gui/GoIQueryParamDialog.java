package org.gvt.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.layout.GridLayout;
import org.gvt.ChisioMain;
import org.gvt.util.QueryOptionsPack;

/**
 * This class maintains GoI Query Dialog for PopupMenu.
 *
 * @author Merve Cakir
 */
public class GoIQueryParamDialog extends AbstractQueryParamDialog
{
    /**
     * Create the dialog
     */
    public GoIQueryParamDialog(ChisioMain main)
    {
        super(main);
    }

    /**
     * Create contents of the dialog
     * Buttons, Text Field, Radio Buttons, etc
     */
    protected void createContents(final QueryOptionsPack opt)
    {
        super.createContents(opt);
        shell.setText("GoI Query Properties");

        //Set Image
        
        ImageDescriptor id = ImageDescriptor.createFromFile(
            GoIQueryParamWithEntitiesDialog.class, "org/gvt/icon/cbe-icon.png");
        shell.setImage(id.createImage());

        //layout of shell contains 4 columns

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        shell.setLayout(gridLayout);

        //Group for currentViewButton and newViewButton
        createResultViewGroup(2, 2);

        //Length Limit Label
        createLengthLimit(1, 2, 1, 2, 50);

		// Group for execute, cancel and default buttons
		createExeCancDefGroup(opt, 4);

		//pack dialog
		shell.pack();

		//set initial values from opt OptionsPack
		setInitialValues(opt);
    }
}
