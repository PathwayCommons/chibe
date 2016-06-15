package org.gvt.model.biopaxl2;

import org.biopax.paxtools.model.level2.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.gvt.model.CompoundModel;
import org.gvt.model.NodeModel;
import org.gvt.util.ID;
import org.patika.mada.graph.GraphObject;

import java.util.*;

/**
 * Control in BioPAX file.
 * 
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class Control extends BioPAXNode
{
	/**
	 * BioPAX control node.
	 */
	control con;
	
	public Control(CompoundModel root)
	{
		super(root);
		
		setColor(COLOR_ACTIVATE);
		setText("");
		setSize(new Dimension(9, 9));
		setShape("Diamond");
	}
	
	public Control(CompoundModel root, control con, NodeModel target, Map<String, NodeModel> map)
	{
		this(root);
		
		assert target instanceof Conversion || target instanceof Control || target instanceof Hub;

//		assert (con.isCONTROLLEDOf() != null && !con.isCONTROLLEDOf().isEmpty()) ||
//			con.getCONTROLLER().size() > 1;
		
		// Remember this control to prevent duplication.
		map.put(ID.get(con), this);
		
		// Connect controller and controlled.
		
		Set<physicalEntityParticipant> pars = con.getCONTROLLER();
		
		for (physicalEntityParticipant par : pars)
		{
			NodeModel node = map.get(ID.get(par));
			new EffectorFirstHalf(node, this, par);
		}

		new EffectorSecondHalf(this, target, con);
		
		// Connect modulators.
		
		for (control c : con.isCONTROLLEDOf())
		{
			if (map.containsKey(ID.get(c)))
			{
				Control mod = (Control) map.get(ID.get(c));
				new EffectorSecondHalf(mod, this, mod.getControl());
			}
			else if (c.isCONTROLLEDOf().isEmpty() && c.getCONTROLLER().size() == 1)
			{
				NodeModel source = map.get(ID.get(c.getCONTROLLER().iterator().next()));
				new NonModulatedEffector(source, this, c, c.getCONTROLLER().iterator().next(), con);
			}
			else
			{
				Control ctrl = new Control(root, c, this, map);
				map.put(ID.get(c), ctrl);
			}
		}
		this.con = con;

		configFromModel();
	}

	public void configFromModel()
	{
		super.configFromModel();
		setTooltipText(con.getNAME());
		setColor(isActivation(con) ? COLOR_ACTIVATE : COLOR_INHIBIT);
	}

	public Control(Control excised, CompoundModel root)
	{
		super(excised, root);
		this.con = excised.getControl();
	}

	public control getControl()
	{
		return con;
	}

	public static boolean isActivation(control cont)
	{
		boolean isActivation = true;

		ControlType type = cont.getCONTROL_TYPE();
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

	public Collection<? extends Level2Element> getRelatedModelElements()
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

		for (evidence ev : con.getEVIDENCE())
		{
			list.add(new String[]{"Evidence", ev.toString()});
		}

		if (!con.getINTERACTION_TYPE().isEmpty())
		{
			String s = formatInString(con.getINTERACTION_TYPE());
			list.add(new String[]{"Interaction Type", s});
		}

		if (con.getCONTROL_TYPE() != null)
			list.add(new String[]{"Control Type", con.getCONTROL_TYPE().toString()});

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
