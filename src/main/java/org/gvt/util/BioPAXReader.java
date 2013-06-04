package org.gvt.util;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.BioPAXLevel;
import org.gvt.model.CompoundModel;
import org.gvt.model.biopaxl2.BioPAXL2Graph;
import org.gvt.model.biopaxl3.BioPAXL3Graph;
import org.gvt.ChisioMain;
import org.patika.mada.util.XRef;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author Ozgun Babur
 */
public class BioPAXReader
{
	Model model;

	public BioPAXReader()
	{
	}

	public BioPAXReader(Model model)
	{
		this.model = model;
	}

	public CompoundModel readXMLFile(File xmlFile)
	{
		CompoundModel root = null;

		XRef.clearDBSet();

		try
		{
			if (model == null)
			{
				BioPAXIOHandler reader = new SimpleIOHandler();
				model = reader.convertFromOWL(new FileInputStream(xmlFile));
			}

			if (model != null)
			{
				BioPAXLevel level = model.getLevel();
				if (level == BioPAXLevel.L2)
				{
					root = new BioPAXL2Graph(model);
					BioPAXL2Reader reader = new BioPAXL2Reader(model);
					reader.createGraph((BioPAXL2Graph) root);
				}
				else if (level == BioPAXLevel.L3)
				{
					root = new BioPAXL3Graph(model);
					BioPAXL3Reader reader = new BioPAXL3Reader(model);
					reader.createGraph((BioPAXL3Graph) root);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			MessageBox messageBox = new MessageBox(
				new Shell(),
				SWT.ERROR_UNSUPPORTED_FORMAT);
			messageBox.setMessage("File cannot be loaded!");
			messageBox.setText(ChisioMain.TOOL_NAME);
			messageBox.open();

			return null;
		}

		return root;
	}


}
