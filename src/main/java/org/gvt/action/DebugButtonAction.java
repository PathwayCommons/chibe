package org.gvt.action;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level3.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.gvt.ChisioMain;
import org.gvt.model.EdgeModel;
import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl3.Actor;
import org.gvt.model.biopaxl3.ChbComplex;
import org.gvt.model.biopaxl3.ChbConversion;
import org.gvt.model.biopaxl3.NonModulatedEffector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

/**
 *
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class DebugButtonAction extends Action
{
	ChisioMain main;

	/**
	 * Constructor
	 */
	public DebugButtonAction(ChisioMain main)
	{
		super("Debug button");
		this.setToolTipText(
			"Debug Button - You can run any\n" +
			"code after pressing this button.\n" +
			"Insert your code in the class\n" +
			"DebugButtonAction");
		setImageDescriptor(ImageDescriptor.createFromFile(ChisioMain.class, "icon/bug.png"));
		this.main = main;
	}

	public void run()
	{
		try
		{
			workOnSelected();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	private void workOnSelected()
	{
		ScrollingGraphicalViewer viewer = main.getViewer();
		Iterator selectedObjects = ((IStructuredSelection) viewer.getSelection()).iterator();

		while (selectedObjects.hasNext())
		{
			Object o = ((EditPart)selectedObjects.next()).getModel();

			if (o instanceof NodeModel)
			{
				NodeModel model = (NodeModel) o;

				if (model instanceof Actor)
				{
					Actor actor = (Actor) model;
					if (actor.getEntity().l3pe != null)
					{
						System.out.println(actor.getEntity().l3pe.getDisplayName());
						System.out.println("pe id = " + actor.getEntity().l3pe.getRDFId());
					}
					if (actor.getEntity().l3er != null)
						System.out.println("er id = " + actor.getEntity().l3er.getRDFId());

//					for (Xref xref : ((SimplePhysicalEntity)actor.getEntity().l3pe).getEntityReference().getXref())
//					{
//						if (xref instanceof UnificationXref)
//							System.out.println(xref);
//					}
				}
				else if (o instanceof ChbComplex)
				{
					ChbComplex cmp = (ChbComplex) o;
					System.out.println(cmp.getEntity().l3pe.getDisplayName());
					System.out.println(cmp.getComplex().getRDFId());
				}
				else if (o instanceof ChbConversion)
				{
					ChbConversion cnv = (ChbConversion) o;
					System.out.println("conversion id = " + cnv.getConversion().getRDFId());
				}
			}
			else if (o instanceof NonModulatedEffector)
			{
				System.out.println("cont id = " + ((NonModulatedEffector) o).getControl().getRDFId());
			}
		}
	}

	static class Waiter
	{
		public synchronized static void pause(long time)
		{
			Waiter w = new Waiter();
			w.bekle(time);
		}

		private synchronized void bekle(long time)
		{
			try
			{
				this.wait(time);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void clearLayoutInFiles() throws FileNotFoundException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		File dir = new File("/home/ozgun/Projects/chibe/samples/level 2/");

		for (File file : dir.listFiles())
		{
			if (file.getName().endsWith(".owl"));
			{
				Model model = h.convertFromOWL(new FileInputStream(file));
				for (BioPAXElement ele : model.getObjects())
				{
					Set<String> remove = new HashSet<String>();
					if (ele instanceof Level2Element)
					{
						for (String com : ((Level2Element) ele).getCOMMENT())
						{
							if (com.contains("@Layout@")) remove.add(com);
						}
						for (String rem : remove)
						{
							((Level2Element) ele).removeCOMMENT(rem);
						}
					}
					else if (ele instanceof Level3Element)
					{
						for (String com : ((Level3Element) ele).getComment())
						{
							if (com.contains("@Layout@")) remove.add(com);
						}
						for (String rem : remove)
						{
							((Level3Element) ele).removeComment(rem);
						}
					}
				}
				h.convertToOWL(model, new FileOutputStream(file));
			}
		}
	}
}