package org.gvt;

import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.Xref;
import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class ReactionToGeneFilePreparer
{
	@Test
	public void prepare() throws IOException
	{
		SimpleIOHandler io = new SimpleIOHandler();
		Model model = io.convertFromOWL(new FileInputStream(
			"/home/ozgun/Projects/biopax-pattern/All-Data.owl"));

		Completer cpt = new Completer(SimpleEditorMap.L3);

		BufferedWriter writer = new BufferedWriter(new FileWriter("reaction2gene.txt"));

		boolean first = true;

		for (Conversion conv : model.getObjects(Conversion.class))
		{
			Set<BioPAXElement> set = new HashSet<>();
			set.add(conv);

			set.addAll(conv.getControlledOf());

			set = cpt.complete(set, model);

			Set<String> symbols = new HashSet<>();
			Set<Control> controls = new HashSet<>();
			for (BioPAXElement ele : set)
			{
				if (ele instanceof ProteinReference)
				{
					String symbol = getSymbol((ProteinReference) ele);
					if (symbol != null) symbols.add(symbol);
				}
				else if (ele instanceof Control)
				{
					controls.add((Control) ele);
				}
			}

			if (!symbols.isEmpty())
			{
				if (first) first = false;
				else writer.write("\n");

				writer.write(conv.getRDFId());

				for (Control control : controls)
				{
					writer.write(" " + control.getRDFId());
				}

				for (String symbol : symbols)
				{
					writer.write("\t" + symbol);
				}
			}

		}
		writer.close();
	}

	private String getSymbol(ProteinReference prot)
	{
		for (Xref xref : prot.getXref())
		{
			if (xref.getDb() != null && xref.getDb().equals("HGNC Symbol")) return xref.getId();
		}
		return null;
	}
}
