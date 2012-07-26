package org.gvt.gui;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.gvt.ChisioMain;

/**
 * @author Merve Cakir
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */

public class LegendDialog extends Dialog
{
    private Shell shell;

    public LegendDialog(Shell parent)
    {
        super(parent, SWT.NONE);
    }

    /**
     * Open the dialog
     */
    public void open()
    {
        createContents();

        shell.pack();
        shell.setLocation(getParent().getLocation().x + (getParent().getSize().x / 2) - (shell.getSize().x / 2),
            getParent().getLocation().y + (getParent().getSize().y / 2) - (shell.getSize().y / 2));
        shell.open();

        Display display = getParent().getDisplay();

        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }

    /**
     * Create contents of the dialog
     */
    public void createContents()
    {
        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText("Visual Legend");

        ImageDescriptor id = ImageDescriptor.createFromFile(
            ChisioMain.class, "icon/cbe-icon.png");
        shell.setImage(id.createImage());

        shell.setBackground(ColorConstants.white);
        shell.setLayout(new GridLayout());

        ImageDescriptor legend = ImageDescriptor.createFromFile(ChisioMain.class, "icon/legend.png");

        Label legendLabel = new Label(shell, SWT.CENTER);
        legendLabel.setImage(legend.createImage());
    }
}
