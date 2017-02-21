package org.gvt.action;

import org.gvt.ChisioMain;
import org.gvt.model.CompoundModel;
import org.gvt.util.CustomReader;
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
public class LoadSIFFileAction extends ChiBEAction
{
	String filename;
	boolean runLayout;

	/**
	 * Constructor without filename. opens an FileChooser for filename
	 *
	 * @param chisio
	 */
	public LoadSIFFileAction(ChisioMain chisio)
	{
		super("Load SIF File ...", "icon/open.png", chisio);
		this.runLayout = true;
		addFilterExtension(FILE_KEY, FILTER_EXTENSIONS);
		addFilterName(FILE_KEY, new String[]{"Simple Interaction Format (*.sif)", "Custom Graph (*.cus)"});
		addLastLocation(FILE_KEY, "samples/");
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

	public void run()
	{
		if (filename == null)
		{
			filename = new FileChooser(this).choose(FILE_KEY);

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

		File file = new File(filename);

		CompoundModel root;

		if (file.getName().endsWith(".cus"))
		{
			CustomReader reader = new CustomReader();
			root = reader.readFile(file);
		} else
		{
			SIFReader reader = new SIFReader();
			root = reader.readXMLFile(file);
		}

		if (root != null)
		{
			main.createNewTab(root);

			String layoutFile = null;
			if (filename.endsWith(".sif"))
			{
				layoutFile = filename.substring(0, filename.lastIndexOf(".")) + ".layout";
				if (!new File(layoutFile).exists()) layoutFile = null;
			}

			if (layoutFile == null)
			{
				if (runLayout)
				{
					new CoSELayoutAction(main).run();
				} else
				{
					new ZoomAction(main, 0, null).run();
				}
			}

			main.setOwlFileName(filename);

			if (filename.endsWith(".sif"))
			{
				String series = filename.substring(0, filename.lastIndexOf(".")) + ".formatseries";

				if (new File(series).exists())
				{
					ShowFormatSeriesAction action = new ShowFormatSeriesAction(main);
					action.setFormatFilename(series);
					action.run();
				}
			}

			if (layoutFile != null)
			{
				LoadSIFLayoutAction action = new LoadSIFLayoutAction(main);
				action.setLayoutFilename(layoutFile);
				action.run();

				new ZoomAction(main, 100, null).run();
			}
		}

		// reset filename for future loadings.
		// otherwise always opens the same file
		filename = null;
	}

	/**
	 * Checks if the file has a valid extension for loading into chisio.
	 *
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

	public static final String[] FILTER_EXTENSIONS = new String[]{"*.sif", "*.cus"};
}