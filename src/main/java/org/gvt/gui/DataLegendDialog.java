package org.gvt.gui;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.layout.CellLayout;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.patika.mada.util.AlterationData;
import org.patika.mada.util.ExperimentData;
import org.patika.mada.util.Representable;

/**
 * @author Ozgun Babur
 */

public class DataLegendDialog extends Dialog
{
	private Shell shell;
	private ExperimentData data;

	public DataLegendDialog(ChisioMain main, ExperimentData data)
	{
		super(main.getShell(), SWT.NONE);
		this.data = data;
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
		shell = new Shell(getParent(), SWT.DIALOG_TRIM);
		shell.setText("Legend");

		ImageDescriptor id = ImageDescriptor.createFromFile(
			ChisioMain.class, "icon/cbe-icon.png");

		shell.setImage(id.createImage());

		shell.setBackground(ColorConstants.white);
		shell.setLayout(new FillLayout());

		Group legendGroup = new Group(shell, SWT.NONE);
		legendGroup.setBackground(ColorConstants.white);
		legendGroup.setLayout(new CellLayout(1));

		if (data instanceof AlterationData)
		{
			for (int i = 0; i <= 10; i++)
			{
				Label lab = new Label(legendGroup, SWT.NONE);
				double v = i / 10D;
				v *= v * v;

				v = Math.round(v * 1000) / 1000D;

				lab.setText("       " + v + "        ");
				Color color = AlterationData.getNodeColor(v);
				lab.setBackground(color);
//				lab.setForeground(ColorConstants.black);
			}
		}
		else
		{
			for (double i = ExperimentData.low; i <= ExperimentData.mid_l; i += 0.2)
			{
				i = addExperimentColor(legendGroup, i);
			}
			for (double i = ExperimentData.mid_h; i <= ExperimentData.high; i += 0.2)
			{
				i = addExperimentColor(legendGroup, i);
			}
		}
	}

	private double addExperimentColor(Group legendGroup, double i)
	{
		i = Math.round(i * 10) / 10D;
		Label lab = new Label(legendGroup, SWT.NONE);
		lab.setText("       " + i + "        ");
//		lab.setForeground(ColorConstants.black);
		Color color = ExperimentData.getNodeColor(i);
		lab.setBackground(color);
		return i;
	}
}
