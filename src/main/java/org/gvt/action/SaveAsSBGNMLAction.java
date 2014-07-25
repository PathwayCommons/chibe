package org.gvt.action;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.biopax.paxtools.controller.Cloner;
import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.io.sbgn.L3ToSBGNPDConverter;
import org.biopax.paxtools.io.sbgn.ListUbiqueDetector;
import org.biopax.paxtools.io.sbgn.UbiqueDetector;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.eclipse.draw2d.*;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.gvt.ChisioMain;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.editpart.ChsScalableRootEditPart;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.CompoundModel;
import org.gvt.model.biopaxl3.Actor;
import org.gvt.util.PathwayHolder;
import org.gvt.util.onotoa.GraphicsToGraphics2DAdaptor;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Action for saving the graph or view as an image.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class SaveAsSBGNMLAction extends Action
{
	ChisioMain main;
	boolean currentPathwayOnly;

	public SaveAsSBGNMLAction(ChisioMain chisio, boolean currentPathwayOnly)
	{
		this.main = chisio;
		this.currentPathwayOnly = currentPathwayOnly;
		setText(this.currentPathwayOnly ?
			"Save Pathway As SBGM-ML ..." : "Save Model As SBGN-ML ...");
		setToolTipText(getText());
	}

	public void run()
	{
		final Shell shell = main.getShell();

		if (currentPathwayOnly && main.getViewer() == null) return;

		// Get the user to choose a file name and type to save.
		FileDialog fileChooser = new FileDialog(main.getShell(), SWT.SAVE);

		String tmpfilename = main.getPathwayGraph() != null ?
			main.getPathwayGraph().getName() :
			main.getSelectedTab().getText();

		fileChooser.setFileName(tmpfilename + ".sbgn");
		fileChooser.setFilterExtensions(new String[]{"*.sbgn"});
		fileChooser.setFilterNames(new String[]{"SBGN-ML (*.sbgn)"});
		String filename = fileChooser.open();

		if (filename == null)
		{
			return;
		}
		else if (!filename.endsWith(".sbgn"))
		{
			filename += ".sbgn";
		}

		File file = new File(filename);
		if (file.exists())
		{
			// The file already exists; asks for confirmation
			MessageBox mb = new MessageBox(fileChooser.getParent(),
				SWT.ICON_WARNING | SWT.YES | SWT.NO);

			// We really should read this string from a
			// resource bundle
			mb.setMessage(filename + " already exists.\nDo you want to overwrite?");
			mb.setText("Confirm Replace File");
			// If they click Yes, we're done and we drop out. If
			// they click No, we quit the operation.
			if (mb.open() != SWT.YES)
			{
				return;
			}
		}

		Model model = main.getBioPAXModel();

		if (currentPathwayOnly)
		{
			PathwayHolder ph = main.getPathwayGraph().getPathway();
			Pathway pathway = ph.l3p;
			Set<BioPAXElement> eles = new HashSet<BioPAXElement>(Collections.singleton(pathway));
			EditorMap editorMap = (new SimpleIOHandler(BioPAXLevel.L3)).getEditorMap();
			eles = (new Completer(editorMap)).complete(eles, model);
			model = (new Cloner(editorMap, BioPAXLevel.L3.getDefaultFactory())).clone(model, eles);
		}

		UbiqueDetector ubDet = new ListUbiqueDetector(getUbiqueIDs());
		L3ToSBGNPDConverter converter = new L3ToSBGNPDConverter(ubDet, null, false);
		converter.writeSBGN(model, filename);
	}

	private Set<String> getUbiqueIDs()
	{
		Set<String> set = new HashSet<String>();

		BioPAXGraph graph = main.getPathwayGraph();
		for (Object o : graph.getNodes())
		{
			if (o instanceof Actor) processActor(set, (Actor) o);
			else if (o instanceof CompoundModel) collectInsideCompound((CompoundModel) o, set);
		}
		return set;
	}

	private void collectInsideCompound(CompoundModel node, Set<String> set)
	{
		for (Object o : node.getChildren())
		{
			if (o instanceof CompoundModel) collectInsideCompound((CompoundModel) o, set);
			else if (o instanceof Actor) processActor(set, (Actor) o);
		}
	}

	private void processActor(Set<String> set, Actor actor)
	{
		if (actor.isUbique())
		{
			Collection<? extends Level3Element> elements = actor.getRelatedModelElements();
			for (Level3Element element : elements)
			{
				if (element instanceof PhysicalEntity) set.add(element.getRDFId());
			}
		}
	}
}
