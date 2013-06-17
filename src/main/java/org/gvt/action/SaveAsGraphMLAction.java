package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.draw2d.*;
import org.gvt.ChisioMain;
import org.gvt.util.GraphMLWriter;
import org.gvt.model.CompoundModel;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.editpart.ChsScalableRootEditPart;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Action for saving the graph as an GraphML.
 *
 * @author Ozgun Babur
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class SaveAsGraphMLAction extends Action
{
	ChisioMain main;

	public SaveAsGraphMLAction(ChisioMain chisio)
	{
		this.main = chisio;
		setText("Save Pathway As GraphML ...");
		setToolTipText(getText());
	}

	public void run()
	{
		String fileName = null;
		boolean done = false;

		while (!done)
		{
			// Get the user to choose a file name and type to save.
			FileDialog fileChooser = new FileDialog(main.getShell(), SWT.SAVE);
			fileChooser.setFilterExtensions(new String[]{"*.xml", "*.graphml"});
			fileChooser.setFilterNames(
				new String[]{"XML (*.xml)", "GRAPHML (*.graphml)"});
			fileName = fileChooser.open();

			if (fileName == null)
			{
				// User has cancelled, so quit and return
				done = true;
			}
			else
			{
				// User has selected a file; see if it already exists
				File file = new File(fileName);

				if (file.exists())
				{
					// The file already exists; asks for confirmation
					MessageBox mb = new MessageBox(
						fileChooser.getParent(),
						SWT.ICON_WARNING | SWT.YES | SWT.NO);

					// We really should read this string from a
					// resource bundle
					mb.setMessage(fileName +
						" already exists. Do you want to replace it?");
					mb.setText("Confirm Replace File");
					// If they click Yes, we're done and we drop out. If
					// they click No, we redisplay the File Dialog
					done = mb.open() == SWT.YES;
				}
				else
				{
					// File does not exist, so drop out
					done = true;
				}
			}
		}

		if (fileName == null)
		{
			return;
		}

		try
		{
			// get the root of graph
			CompoundModel root = (CompoundModel)
				((ChsRootEditPart) main.getViewer().getRootEditPart().
					getChildren().get(0)).getModel();

			BufferedWriter xmlFile =
				new BufferedWriter(new FileWriter(fileName));
			GraphMLWriter writer = new GraphMLWriter();
			xmlFile.write(writer.writeXMLFile(root).toString());
			xmlFile.close();

			// mark save location in commandstack
			main.getEditDomain().getCommandStack().markSaveLocation();
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			MessageBox messageBox = new MessageBox(
				main.getShell(),
				SWT.ERROR_UNSUPPORTED_FORMAT);
			messageBox.setMessage("File cannot be saved!");
			messageBox.setText("Chisio");
			messageBox.open();
			e.printStackTrace();
		}

	}
}