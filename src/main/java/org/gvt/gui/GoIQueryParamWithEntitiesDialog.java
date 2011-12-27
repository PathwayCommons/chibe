package org.gvt.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gvt.ChisioMain;
import org.gvt.util.EntityHolder;
import org.gvt.util.QueryOptionsPack;

import java.util.ArrayList;

/**
 * This class maintains GoI Query Dialog for TopMenuBar.
 *
 * @author Merve Cakir
 */
public class GoIQueryParamWithEntitiesDialog extends AbstractQueryParamDialog
{
    /**
     * All entities of graph
     */
    ArrayList<EntityHolder> allEntities;

    /**
     * Create the dialog
     */
    public GoIQueryParamWithEntitiesDialog(ChisioMain main, ArrayList<EntityHolder> allEntities)
    {
        super(main);
        this.allEntities = allEntities;
    }

    /**
     * Open the dialog
     */
    public QueryOptionsPack open(QueryOptionsPack opt)
    {
        createContents(opt);

        shell.setLocation(
            getParent().getLocation().x + (getParent().getSize().x / 2) -
            (shell.getSize().x / 2),
            getParent().getLocation().y + (getParent().getSize().y / 2) -
            (shell.getSize().y / 2));

        shell.open();

        shell.layout();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return opt;
    }

    /**
     * Create contents of the dialog.
     * Buttons, List, Text Field, Radio Buttons, etc
     */
    protected void createContents(final QueryOptionsPack opt)
    {
        super.createContents(opt);
        shell.setText("GoI Query Properties");

        //Set Image

        ImageDescriptor id = ImageDescriptor.createFromFile(
            GoIQueryParamWithEntitiesDialog.class,
			"org/gvt/icon/cbe-icon.png");
        shell.setImage(id.createImage());

        //layout of shell contains 4 columns

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        shell.setLayout(gridLayout);

        //Entity list

		Composite cmp;
		if (allEntities != null)
		{
			sourceElg = new EntityListGroup(shell, SWT.NONE, allEntities);
			sourceElg.init();
			cmp = sourceElg;
		}
		else
		{
			sourceST = new SymbolText(shell, SWT.NONE);
			sourceST.init(null);
			cmp = sourceST;
		}
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.verticalSpan = 5;
		gridData.horizontalSpan = 1;
		gridData.widthHint = 150;
		cmp.setLayoutData(gridData);

        //Group for currentViewButton and newViewButton
        createResultViewGroup(2, 2);

        //Length Limit Label and Text
        createLengthLimit(1, 1, 1, 1, 50);

		createExeCancDefGroup(opt, 4);

        //pack dialog
        shell.pack();

        //set initial values from opt OptionsPack
        setInitialValues(opt);
    }

}
