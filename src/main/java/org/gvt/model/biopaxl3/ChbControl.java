package org.gvt.model.biopaxl3;

import org.biopax.paxtools.model.level3.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
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
	
	public ChbControl(CompoundModel root, Control con, NodeModel target, Map<String, NodeModel> map)
	{
		this(root);
		
		assert target instanceof ChbConversion || target instanceof ChbControl || target instanceof Hub;

//		assert (con.isCONTROLLEDOf() != null && !con.isCONTROLLEDOf().isEmpty()) ||
//			con.getCONTROLLER().size() > 1;
		
		// Remember this control to prevent duplication.
		map.put(con.getRDFId(), this);
		
		// Connect controller and controlled.
		Set<Controller> controllers = con.getController();

		for (Controller controller : controllers)
		{
			if (controller instanceof PhysicalEntity)
			{
				PhysicalEntity ent = (PhysicalEntity) controller;
				NodeModel node = map.get(ent.getRDFId());
				new EffectorFirstHalf(node, this);
			}
		}
		
		new EffectorSecondHalf(this, target, con);
		
		// Connect modulators.
		
		for (Control c : con.getControlledOf())
		{
			if (map.containsKey(c.getRDFId()))
			{
				ChbControl mod = (ChbControl) map.get(c.getRDFId());
				new EffectorSecondHalf(mod, this, mod.getControl());
			}
			else if (c.getControlledOf().isEmpty() && c.getController().size() == 1)
			{
				NodeModel source = map.get(c.getController().iterator().next().getRDFId());
				new NonModulatedEffector(source, this, c, con);
			}
			else
			{
				ChbControl ctrl = new ChbControl(root, c, this, map);
				map.put(c.getRDFId(), ctrl);
			}
		}
		this.con = con;

		configFromModel();
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
		return con.getRDFId();
	}

	private static final Color COLOR_ACTIVATE = new Color(null, 170, 170, 170);
	private static final Color COLOR_INHIBIT = new Color(null, 170, 170, 170);

	public static final Color EDGE_COLOR_ACTIVATE = new Color(null, 50, 150, 50);
	public static final Color EDGE_COLOR_INHIBIT = new Color(null, 150, 50, 50);
}
