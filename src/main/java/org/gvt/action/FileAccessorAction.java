package org.gvt.action;

import org.eclipse.swt.widgets.Shell;

/**
 * Interface for the actions that will get one or more file names from user.
 * Created by babur on 3/10/16.
 */
public interface FileAccessorAction
{
	public String[] getFilterExtensions(String key);
	public String[] getFilterNames(String key);
	public String getLastPath(String key);
	public void setLastPath(String path, String key);
	public Shell getShell();
	public String getCurrentFilename();
}
