package org.gvt.model.basicsif;

import org.biopax.paxtools.model.level3.Level3Element;
import org.eclipse.swt.graphics.Color;
import org.gvt.command.AddCommand;
import org.gvt.command.DeleteConnectionCommand;
import org.gvt.command.OrphanChildCommand;
import org.gvt.model.CompoundModel;
import org.gvt.model.EntityAssociated;
import org.gvt.model.GraphObject;
import org.gvt.model.NodeModel;
import org.gvt.model.biopaxl3.BioPAXCompoundNode;
import org.gvt.util.EntityHolder;
import org.patika.mada.graph.Edge;

import java.util.*;

/**
 * For grouping SIF nodes into Pathways.
 *
 * @author Ozgun Babur
 */
public class BasicSIFPathway extends BioPAXCompoundNode implements EntityAssociated
{
	private Set<String> mediators;


	/**
	 * Constructor for excising.
	 * @param toexcise
	 * @param root
	 */
	public BasicSIFPathway(BasicSIFPathway toexcise, CompoundModel root)
	{
		super(toexcise, root);
	}

	public BasicSIFPathway(CompoundModel root)
	{
		super(root);
		setText("");
	}

	public Collection<? extends Level3Element> getRelatedModelElements()
	{
		return Collections.emptySet();
	}

	public BasicSIFPathway(CompoundModel root, Collection<BasicSIFNode> members, String name, String tooltip)
	{
		this(root);
		setText(name);
		setTooltipText(tooltip);

		// Add members as child node

		for (BasicSIFNode member : members)
		{
			OrphanChildCommand occ = new OrphanChildCommand();
			occ.setChild(member);
			occ.setParent(member.getParentModel());
			occ.execute();

			AddCommand add = new AddCommand();
			add.setParent(this);
			add.setChild(member);
			add.execute();
		}


		this.mediators = new HashSet<String>();

		setBorderColor(BORDER_COLOR);
		setColor(BG_COLOR);
		setTextColor(TEXT_COLOR);
	}


	public Set<String> getMediators()
	{
		return mediators;
	}

	/**
	 * Complexes are breadth nodes.
	 * @return true
	 */
	public boolean isBreadthNode()
	{
		return true;
	}

	public Set<org.patika.mada.graph.GraphObject> getRequisites()
	{
		Set<org.patika.mada.graph.GraphObject> reqs = super.getRequisites();
		reqs.addAll(this.getChildren());
		return reqs;
	}

	public List<String[]> getInspectable()
	{
		return Collections.emptyList();
	}

	public String getIDHash()
	{
		String hash = "";

		for (Object o : children)
		{
			hash += ((BasicSIFNode) o).getIDHash();
		}
		return hash;
	}

	@Override
	public EntityHolder getEntity()
	{
		return null;
	}

	private static final Color BG_COLOR = new Color(null, 255, 255, 255);
	private static final Color BORDER_COLOR = new Color(null, 150, 150, 150);
	private static final Color TEXT_COLOR = new Color(null, 0, 0, 0);

}
