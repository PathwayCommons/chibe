package org.gvt.action;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.draw2d.*;
import org.gvt.ChisioMain;
import org.gvt.model.CompoundModel;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.editpart.ChsScalableRootEditPart;
import org.gvt.util.onotoa.GraphicsToGraphics2DAdaptor;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;

/**
 * Action for saving the graph or view as an image.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class SaveAsImageAction extends Action
{
	ChisioMain main;
	boolean saveWholeGraph;

	public SaveAsImageAction(ChisioMain chisio, boolean saveGraph)
	{
		this.main = chisio;
		this.saveWholeGraph = saveGraph;
		setText(saveWholeGraph ? "Save Pathway As Image ..." : "Save View As Image ...");
		setToolTipText(getText());
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

		// Get the user to choose a file name and type to save.
		FileDialog fileChooser = new FileDialog(main.getShell(), SWT.SAVE);

		/* UK: the default name for the saved image is set to the name of the graph/pathway instead of the name of the BioPAX file.
		 * This makes sense as there might be (and often are) multiple pathways/networks in one owl file.
		 */
		String tmpfilename = main.getPathwayGraph() != null ?
			main.getPathwayGraph().getName() :
			main.getSelectedTab().getText();
//		int ind = tmpfilename.lastIndexOf('.');
//		tmpfilename = tmpfilename.substring(0, ind);


		/* UK: Added support for saving images as PNG, as the libraries support these formats */
		fileChooser.setFileName(tmpfilename + ".svg");
		fileChooser.setFilterExtensions(new String[]{"*.svg", "*.jpg", "*.bmp", "*.png"});
		fileChooser.setFilterNames(
			new String[]{"SVG (*.svg)", "JPEG (*.jpg)", "BMP (*.bmp)", "PNG (*.png)"});
		String filename = fileChooser.open();

		if (filename == null)
		{
			return;
		}
		else if (!filename.endsWith(".jpg") && !filename.endsWith(".bmp") &&
			!filename.endsWith(".png")&& !filename.endsWith(".svg"))
		{
			MessageDialog.openError(main.getShell(), "Invalid filename",
				"ChiBE supports SVG, JPEG, PNG and Bitmap file formats only.\n" +
					"Please provide a filename with an appropriate extension.");
			return;
		}
		else
		{
			File file = new File(filename);
			if (file.exists())
			{
				// The file already exists; asks for confirmation
				MessageBox mb = new MessageBox(fileChooser.getParent(),
					SWT.ICON_WARNING | SWT.YES | SWT.NO);

				// We really should read this string from a
				// resource bundle
				mb.setMessage(filename + " already exists.\nDo you want to overwrite?");
				mb.setText("Confirm Replace File");
				// If they click Yes, we're done and we drop out. If
				// they click No, we quit the operation.
				if (mb.open() != SWT.YES)
				{
					return;
				}
			}
		}

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

		if (filename.endsWith(".svg"))
		{
			exportSvg(image, rootFigure, new File(filename));
		}
		else
		{
			GC gc = new GC(image);
			gc.setAntialias(SWT.ON);
			gc.setTextAntialias(SWT.ON);

			Graphics graphics = new SWTGraphics(gc);

			rootFigure.paint(graphics);
			graphics.drawText(main.getPathwayGraph().getName(), 3, 3);

			graphics.dispose();

			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[]{image.getImageData()};

			if (filename.endsWith(".bmp"))
			{
				loader.save(filename, SWT.IMAGE_BMP);
			}
			else if (filename.endsWith(".jpg"))
			{
				loader.save(filename, SWT.IMAGE_JPEG);
			}
			else if (filename.endsWith(".png"))
			{
				loader.save(filename, SWT.IMAGE_PNG);
			}
		}
	}

	public Rectangle getBounds(ScrollingGraphicalViewer viewer, Figure f, double zoom)
	{
		CompoundModel rootModel = (CompoundModel) ((ChsRootEditPart) viewer.
			getRootEditPart().getChildren().get(0)).getModel();

		org.eclipse.draw2d.geometry.Rectangle bounds
			= rootModel.calculateBounds();
		org.eclipse.draw2d.geometry.Rectangle boundsRoot = f.getBounds();

		boundsRoot.setSize((int) (bounds.x + ((bounds.width + CompoundModel.MARGIN_SIZE) * zoom)),
			(int) (bounds.y + ((bounds.height + CompoundModel.MARGIN_SIZE) * zoom)));

		return new Rectangle(boundsRoot.x,
			boundsRoot.y,
			boundsRoot.width,
			boundsRoot.height);
	}

	public Rectangle getBounds(Figure f)
	{
		org.eclipse.draw2d.geometry.Rectangle bounds = f.getBounds();

		return new Rectangle(bounds.x ,
			bounds.y ,
			bounds.width ,
			bounds.height);
	}

	public void exportSvg(Image image, IFigure fig, File file) {
		Graphics g = null;
		try {
			String svgNS = "http://www.w3.org/2000/svg";

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation().createDocument(
				svgNS, "svg", null);

			SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(doc);
			ctx.setComment("Generated by Onotoa with Batik SVG Generator");
			ctx.setEmbeddedFontsOn(true);
			ctx.setPrecision(3);
			SVGGraphics2D svgGraphics2d = new SVGGraphics2D(ctx, true);
			g = new GraphicsToGraphics2DAdaptor(svgGraphics2d, image.getBounds());

			g.translate(fig.getBounds().getLocation().getCopy().scale(-1.));
			fig.paint(g);

			FileWriter writer = new FileWriter(file);
			svgGraphics2d.stream(writer);
			writer.flush();

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (g != null) {
				g.dispose();
			}
			if (image!=null)
				image.dispose();
		}
	}

}
