package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.gvt.ChisioMain;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by babur on 3/10/16.
 */
public abstract class ChiBEAction extends Action implements FileAccessorAction
{
	protected ChisioMain main;

	private Map<String, String[]> filterExtensionMap;
	private Map<String, String[]> filterNameMap;
	private Map<String, String> lastLocationMap;

	/**
	 * Default input file key. Useful to actions when there is only file for input.
	 */
	public static final String FILE_KEY = "FILE_KEY";

	public ChiBEAction(String text, String imagePath, ChisioMain main)
	{
		super(text);
		this.main = main;
		setToolTipText(getText());
		if (imagePath != null) setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, imagePath));

		filterExtensionMap = new HashMap<String, String[]>();
		filterNameMap = new HashMap<String, String[]>();
		lastLocationMap = new HashMap<String, String>();
	}

	protected void addFilterExtension(String key, String[] extensions)
	{
		filterExtensionMap.put(key, extensions);
	}

	protected void addFilterName(String key, String[] names)
	{
		filterNameMap.put(key, names);
	}

	protected void addLastLocation(String key, String loc)
	{
		lastLocationMap.put(key, loc);
	}

	@Override
	public String[] getFilterExtensions(String key)
	{
		return filterExtensionMap.get(key);
	}

	@Override
	public String[] getFilterNames(String key)
	{
		return filterNameMap.get(key);
	}

	@Override
	public String getLastPath(String key)
	{
		return lastLocationMap.get(key);
	}

	@Override
	public void setLastPath(String path, String key)
	{
		lastLocationMap.put(key, path);
	}

	@Override
	public Shell getShell()
	{
		return main.getShell();
	}

	@Override
	public String getCurrentFilename()
	{
		return null;
	}
}
