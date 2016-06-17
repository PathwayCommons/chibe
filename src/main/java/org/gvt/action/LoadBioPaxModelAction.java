package org.gvt.action;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.gvt.ChisioMain;
import org.gvt.gui.StringInputDialog;
import org.gvt.util.BioPAXUtil;
import org.gvt.util.PathwayHolder;

import java.io.FileInputStream;
import java.io.IOException;
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
public class LoadBioPaxModelAction extends ChiBEAction
{
	protected String filename;

	private String newPathwayName;
	private String openPathwayName;

	/**
	 * To remember the pathway name from last execution.
	 */
	private String lastPathwayName;

	private boolean fromURL;

	/**
	 * BioPAX paxtools model object.
	 */
	protected Model model;

	/**
	 * What to do after load operation.
	 */
	private boolean openPathways;

	/**
	 * Constructor without filename. opens an FileChooser for filename
	 *
	 * @param chisio
	 */
	public LoadBioPaxModelAction(ChisioMain chisio)
	{
		super("Load ...", "icon/open.png", chisio);
		addLastLocation(FILE_KEY, "samples/");
		addFilterName(FILE_KEY, new String[]{"BioPAX (*.owl)"});
		addFilterExtension(FILE_KEY, FILTER_EXTENSIONS);
		this.openPathways = true;
		this.fromURL = false;
	}

	public LoadBioPaxModelAction(ChisioMain main, boolean fromURL)
	{
		this(main);
		this.fromURL = fromURL;
		if (fromURL)
		{
			setText("Load from URL ...");
			setToolTipText(getText());
		}
	}

	public void setOpenPathways(boolean openPathways)
	{
		this.openPathways = openPathways;
	}

	public void setOpenPathwayName(String openPathwayName)
	{
		this.openPathwayName = openPathwayName;
	}

	public void setNewPathwayName(String newPathwayName)
	{
		this.newPathwayName = newPathwayName;
	}

	public String getNewPathwayName()
	{
		return newPathwayName;
	}

	public String getLastPathwayName()
	{
		return lastPathwayName;
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
			if (filename == null && model == null)
			{
				filename = fromURL ? openURLGetter() : new FileChooser(this).choose(FILE_KEY);

				if (filename == null || filename.isEmpty())
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
						try
						{
							model = reader.convertFromOWL(new URL(filename).openStream());
						}
						catch (IOException e)
						{
							MessageDialog.openError(main.getShell(), "URL error",
								"Cannot get data from the provided URL:\n" + filename);
							e.printStackTrace();
						}
					}
					else model = reader.convertFromOWL(new FileInputStream(filename));
				}

				if (model != null)
				{
					if (BioPAXUtil.numberOfUnemptyPathways(model) == 0 || newPathwayName != null)
					{
						String name = newPathwayName == null ? filename : newPathwayName;

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
						newPathwayName = ph.getName();
					}

					if (main.getBioPAXModel() != null) main.closeAllTabs(false);
					main.setBioPAXModel(model);
					if (!fromURL) main.setOwlFileName(filename);

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
						else if (newPathwayName != null)
						{
							autoOpen = Arrays.asList(newPathwayName);
						}
						else if (openPathwayName != null)
						{
							autoOpen = Arrays.asList(openPathwayName);
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
				lastPathwayName = newPathwayName;
				newPathwayName = null;
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
}