package org.gvt.gui;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;

/**
 * @author Cihan Kucukkececi
 * @author Ugur Dogrusoz
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class AboutDialog extends Dialog
{
	protected Object result;
	protected Shell shell;

	/**
	 * Create the dialog
	 */
	public AboutDialog(Shell parent)
	{
		super(parent, SWT.NONE);
	}

	/**
	 * Open the dialog
	 */
	public Object open()
	{
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();

		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}

		return result;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents()
	{
		shell = new Shell(getParent(), SWT.CLOSE);
		shell.setSize(355, 370);
		shell.setLayout(new GridLayout());

		ImageDescriptor id = ImageDescriptor.createFromFile(
			ChisioMain.class, "icon/cbe-icon.png");
		shell.setImage(id.createImage());

		shell.setBackground(ColorConstants.white);
		shell.setText("About " + ChisioMain.TOOL_NAME);

		// Display it in the middle
		Point loc = getParent().getShell().getLocation();
		Point size = getParent().getShell().getSize();
		Point s = shell.getSize();
		shell.setLocation(size.x/2 + loc.x - s.x/2, size.y/2 + loc.y -s.y/2);

		ImageDescriptor bcbiLogo =
			ImageDescriptor.createFromFile(ChisioMain.class,
                    "icon/ivis-logo.png");

		ImageDescriptor cbioLogo =
			ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbio.png");

		Label bcbiLabel = new Label(shell, SWT.CENTER);
		GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		bcbiLabel.setLayoutData(gridData);
		bcbiLabel.setBackground(ColorConstants.white);
		bcbiLabel.setImage(bcbiLogo.createImage());

		Label cbioLabel = new Label(shell, SWT.CENTER);
		gridData = new GridData(SWT.CENTER, SWT.BEGINNING, false, false, 1, 10);
		cbioLabel.setLayoutData(gridData);
		cbioLabel.setBackground(ColorConstants.white);
		cbioLabel.setImage(cbioLogo.createImage());

		Label chisioLabel = new Label(shell, SWT.CENTER);
		chisioLabel.setLayoutData(
			new GridData(SWT.CENTER, SWT.CENTER, false, false));
		chisioLabel.setBackground(ColorConstants.white);
		chisioLabel.setFont(
			new Font(null, "Verdana", 16, SWT.BOLD | SWT.ITALIC));
		chisioLabel.setText(ChisioMain.TOOL_NAME + "\nversion 2.0");

		Label compoundOrHierarchicalLabel = new Label(shell, SWT.NONE);
		compoundOrHierarchicalLabel.setLayoutData(
			new GridData(SWT.CENTER, SWT.CENTER, false, false));
		compoundOrHierarchicalLabel.setFont(
			new Font(null, "Verdana", 8, SWT.NONE));
		compoundOrHierarchicalLabel.setBackground(ColorConstants.white);
		compoundOrHierarchicalLabel.setAlignment(SWT.CENTER);
		compoundOrHierarchicalLabel.setText(
			"based on Chisio, version 2.0,\n" +
			"using Eclipse Graph Editing Framework, version 3.1,\n" +
			"Paxtools, version 1.0, and\n" +
			"PATIKAmad, version 2.1\n" +
			"\n" +
			" Information Visualization Research Group, 2007 - present\n" +
			"\n" +
			"Bilkent University,\n" +
			"Ankara 06800, TURKEY\n" +
			"\n");

		Label mail = new Label(shell, SWT.NONE);
		mail.setLayoutData(
			new GridData(SWT.CENTER, SWT.CENTER, false, false));
		mail.setFont(
			new Font(null, "Verdana", 8, SWT.BOLD));
		mail.setBackground(ColorConstants.white);
		mail.setAlignment(SWT.CENTER);
		mail.setText("ivis@cs.bilkent.edu.tr\n" +
			"http://www.bilkent.edu.tr/~bcbi/chibe.html");
	}
}