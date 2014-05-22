package org.gvt.action;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.gvt.ChisioMain;
import org.gvt.gui.StringInputDialog;
import org.gvt.util.BioPAXUtil;
import org.gvt.util.PathwayHolder;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
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

	protected String location;

	private String pathwayName;

	private boolean fromURL;

	/**
	 * BioPAX paxtools model object.
	 */
	protected Model model;

	/**
	 * What to do after load operation.
	 */
	private boolean openPathways;

	private static String lastLocation = "samples/";

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
		this.fromURL = false;
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

	public void setFromURL(boolean fromURL)
	{
		this.fromURL = fromURL;
	}

	/**
	 * Constructor with filename. opens the xml with file with the given
	 * filename.
	 *
	 * @param chisio
	 * @param location
	 */
	public LoadBioPaxModelAction(ChisioMain chisio, String location)
	{
		this(chisio);
		this.location = location;
	}

	public LoadBioPaxModelAction(ChisioMain chisio, Model model)
	{
		this(chisio);
		this.model = model;
	}

	/**
	 * Hands unsaved changes before the text is discarded.
	 *
	 * @param main main application
	 * @return whether further action should be carried on.
	 */
	public static boolean saveChangesBeforeDiscard(ChisioMain main)
	{
		if (main.isDirty() && main.getBioPAXModel() != null)
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

	public String openURLGetter()
	{
		StringInputDialog d = new StringInputDialog(
			main.getShell(), "URL", "Please enter URL of the owl file", null);

		return d.open();
	}

	public void run()
	{
		if (saveChangesBeforeDiscard(main))
		{
			if (location == null && model == null)
			{
				location = fromURL ? openURLGetter() : openFileChooser();

				if (location == null)
				{
					return;
				}
			}

			try
			{
				main.lockWithMessage("Loading BioPAX model ...");

				if (model == null)
				{
					BioPAXIOHandler reader = new SimpleIOHandler();
					if (fromURL)
					{
						model = reader.convertFromOWL(new URL(location).openStream());
					}
					else model = reader.convertFromOWL(new FileInputStream(location));
				}

				if (model != null)
				{
					if (BioPAXUtil.numberOfUnemptyPathways(model) == 0 || pathwayName != null)
					{
						String name = pathwayName == null ? location : pathwayName;

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
						PathwayHolder ph = BioPAXUtil.createGlobalPathway(model, name);
						pathwayName = ph.getName();
					}

					if (main.getBioPAXModel() != null) main.closeAllTabs(false);
					main.setBioPAXModel(model);
					if (!fromURL) main.setOwlFileName(location);

					if (openPathways)
					{
						main.unlock();

						// If there is only one pathway, open it automatically

						List<String> names = BioPAXUtil.namesOfUnemptyPathways(model);
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
				location = null;
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