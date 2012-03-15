package org.gvt.action;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.ScalableLayeredPane;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Shell;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.editpart.ChsScalableRootEditPart;
import org.gvt.model.CompoundModel;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

/**
 * Action for printing the graph from default printer.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class PrintAction extends Action
{
	ChisioMain main;

	public PrintAction(ChisioMain chisio)
	{
		super("Print Pathway ...");
		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(
			ChisioMain.class,
			"icon/print.png"));
		this.main = chisio;		
	}

	public void run()
	{
		final Shell shell = main.getShell();
		Figure rootFigure;
		Rectangle bounds;

		if (main.getViewer() == null) return;

		rootFigure = (Figure) ((ChsScalableRootEditPart) main.getViewer().
			getRootEditPart()).getFigure().getChildren().get(0);
		ScalableLayeredPane layer =
			(ScalableLayeredPane) rootFigure.getChildren().get(0);
		double scale = layer.getScale();
		layer.setScale(1.0);
		bounds = getBounds(main.getViewer(), rootFigure);

		final Image image = new Image(shell.getDisplay(), bounds);

		GC gc = new GC(image);
		
		if (ChisioMain.runningOnWindows)
		{
			gc.setAntialias(SWT.ON);
			gc.setTextAntialias(SWT.ON);
		}
		else
		{
			gc.setAntialias(SWT.OFF);
			gc.setTextAntialias(SWT.OFF);
		}
		
		Graphics graphics = new SWTGraphics(gc);
		rootFigure.paint(graphics);
		graphics.dispose();

		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[]{image.getImageData()};

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		loader.save(os, SWT.IMAGE_BMP);

		layer.setScale(scale);

		try
		{
			PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
			pras.add(new Copies(1));
			pras.add(new JobName(main.getOwlFileName(), Locale.getDefault()));

			final PrintService[] services =
				PrintServiceLookup.lookupPrintServices(DocFlavor.INPUT_STREAM.JPEG, pras);

			if (services != null && services.length > 0)
			{
				PrintService ps = ServiceUI.printDialog(null, 100, 100,
					services,
					PrintServiceLookup.lookupDefaultPrintService(),
					DocFlavor.INPUT_STREAM.JPEG,
					pras);

				if (ps != null)
				{
					DocPrintJob job = ps.createPrintJob();
					ByteArrayInputStream bis =
						new ByteArrayInputStream(os.toByteArray());
					Doc doc = new SimpleDoc(bis, DocFlavor.INPUT_STREAM.JPEG, null);
					job.print(doc, pras);
				}
			}
			else
			{
				MessageDialog.openError(main.getShell(), "No Printer", "No printer installed.");
			}
		}
		catch (PrintException pe)
		{
			pe.printStackTrace();
			MessageDialog.openError(main.getShell(), "Print Error!", pe.getMessage());
		}
	}

	public Rectangle getBounds(ScrollingGraphicalViewer viewer, Figure f)
	{
		CompoundModel rootModel = (CompoundModel) ((ChsRootEditPart) viewer.
			getRootEditPart().getChildren().get(0)).getModel();

		org.eclipse.draw2d.geometry.Rectangle bounds
			= rootModel.calculateBounds();
		org.eclipse.draw2d.geometry.Rectangle boundsRoot = f.getBounds();

		boundsRoot.setSize(bounds.x + bounds.width + CompoundModel.MARGIN_SIZE,
			bounds.y + bounds.height + CompoundModel.MARGIN_SIZE);

		return new Rectangle(boundsRoot.x,
			boundsRoot.y,
			boundsRoot.width,
			boundsRoot.height);
	}
}