package org.gvt.action;

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
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.gvt.ChisioMain;
import org.gvt.model.BioPAXGraph;
import org.gvt.model.CompoundModel;
import org.gvt.model.biopaxl3.Actor;
import org.gvt.util.PathwayHolder;

import java.io.File;
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
public class SaveAsSBGNMLAction extends ChiBEAction
{
	boolean currentPathwayOnly;

	public SaveAsSBGNMLAction(ChisioMain chisio, boolean currentPathwayOnly)
	{
		super(currentPathwayOnly ? "Save Pathway As SBGM-ML ..." : "Save Model As SBGN-ML ...", null, chisio);
		this.currentPathwayOnly = currentPathwayOnly;
		addFilterExtension(FILE_KEY, new String[]{"*.sbgn"});
		addFilterName(FILE_KEY, new String[]{"SBGN-ML (*.sbgn)"});
	}

	@Override
	public String getCurrentFilename()
	{
		String tmpfilename = main.getPathwayGraph() != null ?
			main.getPathwayGraph().getName() : main.getSelectedTab().getText();

		return tmpfilename + ".sbgn";
	}

	public void run()
	{
		if (currentPathwayOnly && main.getViewer() == null) return;

		String filename = new FileChooser(this, true).choose(FILE_KEY);

		if (filename == null)
		{
			return;
		}
		else if (!filename.endsWith(".sbgn"))
		{
			filename += ".sbgn";
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
		L3ToSBGNPDConverter converter = new L3ToSBGNPDConverter(ubDet, null, true);
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
