package org.gvt.action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.model.CompoundModel;
import org.gvt.util.GraphMLWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Action for saving the graph as an GraphML.
 *
 * @author Ozgun Babur
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class SaveAsGraphMLAction extends ChiBEAction
{
	public SaveAsGraphMLAction(ChisioMain chisio)
	{
		super("Save Pathway As GraphML ...", null, chisio);
		addFilterExtension(FILE_KEY, new String[]{"*.xml", "*.graphml"});
		addFilterName(FILE_KEY, new String[]{"XML (*.xml)", "GRAPHML (*.graphml)"});
	}

	public void run()
	{
		String fileName = null;

		// Get the user to choose a file name and type to save.
		fileName = new FileChooser(this, true).choose(FILE_KEY);

		if (fileName == null) return;

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