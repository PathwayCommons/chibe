package org.gvt.action;

import org.biopax.paxtools.model.Model;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.gvt.util.BioPAXReader;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * This class loads an XML file which is Graphml based and visualizes the graph.
 * It is called from Menubar/File/Open item.
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class LoadBioPaxModelAction extends Action
{
	protected ChisioMain main;

	protected String filename;

	private String pathwayName;

	/**
	 * BioPAX paxtools model object.
	 */
	protected Model model;

	/**
	 * The graph to load (if supplied)
	 */
	protected BioPAXGraph graph;

	/**
	 * What to do after load operation.
	 */
	private boolean openPathways;

	private static String lastLocation = "samples/biopax-files/";

	/**
	 * Constructor without filename. opens an FileChooser for filename
	 *
	 * @param chisio
	 */
	public LoadBioPaxModelAction(ChisioMain chisio)
	{
		super("Load ...");
		setToolTipText(getText());
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/open.png"));

		this.openPathways = true;
		this.main = chisio;
	}

	public void setOpenPathways(boolean openPathways)
	{
		this.openPathways = openPathways;
	}

	public void setPathwayName(String pathwayName)
	{
		this.pathwayName = pathwayName;
	}

	public String getPathwayName()
	{
		return pathwayName;
	}

	/**
	 * Constructor with filename. opens the xml with file with the given
	 * filename.
	 *
	 * @param chisio
	 * @param filename
	 */
	public LoadBioPaxModelAction(ChisioMain chisio, String filename)
	{
		this(chisio);
		this.filename = filename;
	}

	public LoadBioPaxModelAction(ChisioMain chisio, Model model)
	{
		this(chisio);
		this.model = model;
	}

	public LoadBioPaxModelAction(ChisioMain chisio, BioPAXGraph graph)
	{
		this(chisio);
		this.graph = graph;
	}

	/**
	 * Hands unsaved changes before the text is discarded.
	 *
	 * @param main main application
	 * @return whether further action should be carried on.
	 */
	public static boolean saveChangesBeforeDiscard(ChisioMain main)
	{
		if (main.isDirty() && main.getOwlModel() != null)
		{
			MessageBox messageBox = new MessageBox(
				main.getShell(),
				SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.CANCEL);

			messageBox.setMessage("Would you like to save the changes?");
			messageBox.setText(ChisioMain.TOOL_NAME);
			int answer = messageBox.open();

			if (answer == SWT.YES)
			{
				SaveBioPAXFileAction save = new SaveBioPAXFileAction(main);
				save.run();

				return save.isSaved();
			}
			else if (answer == SWT.NO)
			{
				return true;
			}

			return false;
		}

		return true;
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
		if (saveChangesBeforeDiscard(main))
		{
			if (filename == null && model == null && graph == null)
			{
				filename = openFileChooser();

				if (filename == null)
				{
					return;
				}
			}

			try
			{
				main.lockWithMessage("Loading BioPAX model ...");

				BioPAXGraph root = this.graph;

				if (root == null)
				{
					File xmlfile = filename == null ? null : new File(filename);
					BioPAXReader reader = model == null ?
						new BioPAXReader(): new BioPAXReader(model);
					root = (BioPAXGraph) reader.readXMLFile(xmlfile);
				}

				if (root != null)
				{
					if (root.numberOfUnemptyPathways() == 0 || pathwayName != null)
					{
						String name = pathwayName == null ? filename : pathwayName;

						if (name != null)
						{
							if (name.contains("\\"))
							{
								name = name.substring(name.lastIndexOf("\\") + 1);
							}
							else if (name.contains("/"))
							{
								name = name.substring(name.lastIndexOf("/") + 1);
							}

							if (name.contains("."))
							{
								name = name.substring(0, name.lastIndexOf("."));
							}
						}
						else
						{
							name = "Auto-created Pathway";
						}
						root.createGlobalPathway(name);
						pathwayName = name;
						main.getAllPathwayNames().add(pathwayName);
					}

					if (main.getOwlModel() != null) main.closeAllTabs(false);
					main.setRootGraph(root);
					main.setOwlFileName(filename);

					if (openPathways)
					{
						main.unlock();

						// If there is only one pathway, open it automatically

						List<String> names = root.namesOfUnemptyPathways();
						List<String> autoOpen = null;

						if (names.size() == 1)
						{
							autoOpen = Arrays.asList(names.get(0));
						}

						new OpenPathwaysAction(main, autoOpen).run();
					}
				}
				else
				{
					MessageDialog.openError(main.getShell(), "Error", "Cannot load file.");
				}

				// reset filename for future loadings.
				// otherwise always opens the same file
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				filename = null;
				model = null;
				pathwayName = null;
				main.unlock();
			}
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

	public static final String[] FILTER_EXTENSIONS = new String[]{"*.owl"};
	public static final String[] FILTER_NAMES = new String[]{"BioPAX (*.owl)"};
}