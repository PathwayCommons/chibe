package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.util.ID;
import org.gvt.util.NodeProvider;
import org.patika.mada.graph.GraphObject;

import java.util.*;

/**
 * Control in BioPAX file.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ChbControl extends BioPAXNode
{
	/**
	 * BioPAX control node.
	 */
	Control con;
	
	public ChbControl(CompoundModel root)
	{
		super(root);
		
		setColor(COLOR_ACTIVATE);
		setText("");
		setSize(new Dimension(9, 9));
		setShape("Diamond");
	}
	
	public ChbControl(CompoundModel root, Control con, NodeProvider provider)
	{
		this(root);
		
		// Remember this control to prevent duplication.
		provider.register(ID.get(con), this);
		
		// Connect controller and control.
		for (Controller controller : con.getController())
		{
			if (provider.needsToBeDisplayed(ID.get(controller)))
			{
				NodeModel node = provider.getNode(ID.get(controller), root);
				new EffectorFirstHalf(node, this);

				if (node instanceof Actor && ((Actor) node).isUbique())
				{
					((Actor) node).setRelated(con);
				}
			}
		}
		
		// Connect control and controlled.
		for (Process process : con.getControlled())
		{
			if (provider.needsToBeDisplayed(ID.get(process)))
			{
				NodeModel node = provider.getNode(ID.get(process), root);

				if (process instanceof Pathway && node != null)
				{
					new EffectorSecondHalf(this, node, con);
				}
				// else the interaction will establish the link
			}
		}

		// Connect modulators.
		createControlOverInteraction(root, con, provider);

		this.con = con;

		configFromModel();
	}

	public static boolean controlNeedsToBeANode(Control ctrl, NodeProvider prov)
	{
		// check if there are multiple controllers

		int controllerCnt = 0;

		for (Controller controller : ctrl.getController())
		{
			if (prov.needsToBeDisplayed(ID.get(controller))) controllerCnt++;
		}

		if (controllerCnt != 1) return true;

		// check if the control has another control over it

		for (Control control : ctrl.getControlledOf())
		{
			if (prov.needsToBeDisplayed(ID.get(control))) return true;
		}

		// check if the control has empty targets

		int tarCnt = 0;
		for (Process process : ctrl.getControlled())
		{
			if (prov.needsToBeDisplayed(ID.get(process))) tarCnt++;
		}
		if (tarCnt == 0) return true;

		// return false if none of the above holds
		return false;
	}



	public void configFromModel()
	{
		super.configFromModel();
		setTooltipText(con.getStandardName());
		setColor(isActivation(con) ? COLOR_ACTIVATE : COLOR_INHIBIT);
	}

	public ChbControl(ChbControl excised, CompoundModel root)
	{
		super(excised, root);
		this.con = excised.getControl();
	}

	public Control getControl()
	{
		return con;
	}

	public static boolean isActivation(Control cont)
	{
		boolean isActivation = true;

		ControlType type = cont.getControlType();
		if (type != null)
		{
			if (type == ControlType.ACTIVATION ||
				type == ControlType.ACTIVATION_ALLOSTERIC ||
				type == ControlType.ACTIVATION_NONALLOSTERIC ||
				type == ControlType.ACTIVATION_UNKMECH)
			{
				isActivation = true;
			}
			else if (type == ControlType.INHIBITION ||
				type == ControlType.INHIBITION_ALLOSTERIC ||
				type == ControlType.INHIBITION_COMPETITIVE ||
				type == ControlType.INHIBITION_IRREVERSIBLE ||
				type == ControlType.INHIBITION_NONCOMPETITIVE ||
				type == ControlType.INHIBITION_OTHER ||
				type == ControlType.INHIBITION_UNCOMPETITIVE ||
				type == ControlType.INHIBITION_UNKMECH)
			{
				isActivation = false;
			}
			else
			{
				assert false : "Unknown control type: " + type;
			}
		}

		return isActivation;
	}

	public Collection<? extends Level3Element> getRelatedModelElements()
	{
		return Arrays.asList(con);
	}

	public boolean isEvent()
	{
		return true;
	}

	public Set<GraphObject> getRequisites()
	{
		Set<GraphObject> reqs = super.getRequisites();
		reqs.addAll(this.getSourceConnections());
		reqs.addAll(this.getTargetConnections());
		return reqs;
	}

	public List<String[]> getInspectable()
	{
		List<String[]> list = super.getInspectable();

		addNamesAndTypeAndID(list, con);

		for (Evidence ev : con.getEvidence())
		{
			list.add(new String[]{"Evidence", ev.toString()});
		}

		if (!con.getInteractionType().isEmpty())
		{
			String s = formatInString(con.getInteractionType());
			list.add(new String[]{"Interaction Type", s});
		}

		if (con.getControlType() != null)
			list.add(new String[]{"Control Type", con.getControlType().toString()});

		addDataSourceAndXrefAndComments(list, con);

		return list;
	}
	
	public String getIDHash()
	{
		return ID.get(con);
	}

	private static final Color COLOR_ACTIVATE = new Color(null, 170, 170, 170);
	private static final Color COLOR_INHIBIT = new Color(null, 170, 170, 170);

	public static final Color EDGE_COLOR_ACTIVATE = new Color(null, 50, 150, 50);
	public static final Color EDGE_COLOR_INHIBIT = new Color(null, 150, 50, 50);
}
