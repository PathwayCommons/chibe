package org.gvt.action;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.eclipse.draw2d.*;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.editpart.ChsScalableRootEditPart;
import org.gvt.model.CompoundModel;
import org.gvt.util.onotoa.GraphicsToGraphics2DAdaptor;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

/**
 * Action for saving the graph or view as an image.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class CopyToClipboardAction extends SaveAsImageAction
{
	public CopyToClipboardAction(ChisioMain chisio, boolean saveGraph)
	{
		super(chisio, saveGraph);
		setText("Copy " + (saveGraph ? "Pathway" : "View") + " To Clipboard");
	}

	public void run()
	{
		final Shell shell = main.getShell();
		Figure rootFigure;
		Rectangle bounds;
		ScalableLayeredPane layer = null;

		if (main.getViewer() == null) return;

		rootFigure = (Figure) ((ChsScalableRootEditPart) main.getViewer().
			getRootEditPart()).getFigure();

		if (!saveWholeGraph)
		{
			bounds = getBounds(rootFigure);
		}
		else
		{
			rootFigure = (Figure) rootFigure.getChildren().get(0);
			layer = (ScalableLayeredPane)rootFigure.getChildren().get(0);
			double scale = layer.getScale();
			bounds = getBounds(main.getViewer(), rootFigure, scale);
		}

		final Image image = new Image(shell.getDisplay(), bounds);

		GC gc = new GC(image);
		gc.setAntialias(SWT.ON);
		gc.setTextAntialias(SWT.ON);

		Graphics graphics = new SWTGraphics(gc);

		rootFigure.paint(graphics);

		graphics.dispose();

		Clipboard clipboard = new Clipboard(shell.getDisplay());

		clipboard.setContents(new Object[]{image.getImageData()},
			new Transfer[]{new PngTransfer()});
	}

	class PngTransfer extends ByteArrayTransfer
	{
		String IMAGE_PNG = "image/png";
		int ID = registerType(IMAGE_PNG);

		@Override
		protected String[] getTypeNames() {
			return new String[]{IMAGE_PNG};
		}

		@Override
		protected int[] getTypeIds() {
			return new int[]{ID};
		}

		@Override
		protected void javaToNative(Object object, TransferData transferData) {
			if (object == null || !(object instanceof ImageData)) {
				return;
			}

			if (isSupportedType(transferData)) {
				ImageData image = (ImageData) object;
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try {
					// write data to a byte array and then ask super to convert to pMedium

					ImageLoader imgLoader = new ImageLoader();
					imgLoader.data = new ImageData[] { image };
					imgLoader.save(out, SWT.IMAGE_PNG);

					byte[] buffer = out.toByteArray();
					out.close();

					super.javaToNative(buffer, transferData);
				}
				catch (IOException e) {throw new RuntimeException(e);
				}
			}
		}

		@Override
		protected Object nativeToJava(TransferData transferData) {
			if (isSupportedType(transferData)) {

				byte[] buffer = (byte[])super.nativeToJava(transferData);
				if (buffer == null) {
					return null;
				}

				ByteArrayInputStream in = new ByteArrayInputStream(buffer);
				return new ImageData(in);
			}

			return null;
		}

	}
}
