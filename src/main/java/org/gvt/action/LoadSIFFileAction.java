package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.gvt.ChisioMain;
import org.gvt.model.CompoundModel;
import org.gvt.util.SIFReader;

import java.io.File;

/**
 * This class loads an XML file which is Graphml based and visualizes the graph.
 * It is called from Menubar/File/Open item.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class LoadSIFFileAction extends Action
{
	ChisioMain main;
	String filename;
	boolean runLayout;
	static String lastLocation = "samples/";

	/**
	 * Constructor without filename. opens an FileChooser for filename
	 *
	 * @param chisio
	 */
	public LoadSIFFileAction(ChisioMain chisio)
	{
		super("Load SIF File ...");
		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(
			ChisioMain.class, "icon/open.png"));

		this.main = chisio;
		this.runLayout = true;
	}

	/**
	 * Constructor with filename. opens the xml with file with the given
	 * filename.
	 *
	 * @param chisio
	 * @param filename
	 */
	public LoadSIFFileAction(ChisioMain chisio, String filename, boolean runLayout)
	{
		this(chisio);
		this.filename = filename;
		this.runLayout = runLayout;
	}

	public LoadSIFFileAction(ChisioMain chisio, String filename)
	{
		this(chisio, filename, true);
	}

	/**
	 * opens a FileChooser for loading an xml file
	 *
	 * @return chosen filename
	 */
	public String openFileChooser()
	{
		// choose an input file.
		FileDialog fileChooser = new FileDialog(main.getShell(), SWT.OPEN);
		fileChooser.setFilterExtensions(FILTER_EXTENSIONS);
		fileChooser.setFilterNames(FILTER_NAMES);

		fileChooser.setFilterPath(lastLocation);

		String f = fileChooser.open();

		String x = null;
		if (f != null)
		{
			if (f.contains("/"))
			{
				x = f.substring(0, f.lastIndexOf("/"));
			}
			else if (f.contains("\\"))
			{
				x = f.substring(0, f.lastIndexOf("\\"));
			}
		}

		if (x != null) lastLocation = x;

		return f;
	}

	public void run()
	{
//		if (saveChangesBeforeDiscard(main))
		if (true)
		{
			if (filename == null)
			{
				filename = openFileChooser();

				if (filename == null)
				{
					return;
				}
			}

			// reset highlight
			if (main.getViewer() != null)
			{
				main.getHighlightLayer().removeAll();
				main.getHighlightLayer().highlighted.clear();
			}

			File xmlfile = new File(filename);

			SIFReader reader = new SIFReader();

			CompoundModel root = reader.readXMLFile(xmlfile);

			if (root != null)
			{
				main.createNewTab(root);

				if (runLayout)
				{
					new CoSELayoutAction(main).run();
				}
				else
				{
					new ZoomAction(main, 0, null).run();
				}

				main.setOwlFileName(filename);
			}

			// reset filename for future loadings.
			// otherwise always opens the same file
			filename = null;
		}
	}

	/**
	 * Checks if the file has a valid extension for loading into chisio.
	 * @param path the path of the file
	 * @return true if file name is valid
	 */
	public static boolean hasValidExtension(String path)
	{
		for (String extension : FILTER_EXTENSIONS)
		{
			if (path.substring(path.lastIndexOf(".")).equalsIgnoreCase(extension.substring(1)))
			{
				return true;
			}
		}
		return false;
	}

	public static final String[] FILTER_EXTENSIONS = new String[]{"*.sif"};
	public static final String[] FILTER_NAMES = new String[]{"Simple Interaction Format (*.sif)"};
}