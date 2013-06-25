package org.gvt.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Performs HGNC related conversions. Database updated at Jan 25, 2012.
 *
 * @author Ozgun Babur
 */
public class HGNCUtil
{
	private static Map<String, String> sym2id;
    private static Map<String, String> id2sym;
	private static Map<String, String> old2new;

	/**
	 * Gets the latest approved official symbol related to the given ID or symbol. If the parameter
	 * is ID, then it should start with "HGNC:".
	 * @param symbolOrID HGNC ID, symbol, or a previous symbol
	 * @return latest symbol
	 */
	public static String getSymbol(String symbolOrID)
	{
		if (id2sym.containsKey(symbolOrID)) return id2sym.get(symbolOrID);
		else if (sym2id.containsKey(symbolOrID)) return symbolOrID;
		else if (old2new.containsKey(symbolOrID)) return old2new.get(symbolOrID);
		else return null;
	}

	static
	{
		try
		{
			sym2id = new HashMap<String, String>();
            id2sym = new HashMap<String, String>();
            old2new = new HashMap<String, String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				HGNCUtil.class.getResourceAsStream("hgnc.txt")));

			reader.readLine(); //skip header
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");
				String sym = token[1];
				String id = token[0];
				sym2id.put(sym, id);
                id2sym.put(id, sym);

				if (token.length > 2)
				{
					String olds = token[2];
					for (String old : olds.split(","))
					{
						old = old.trim();
						old2new.put(old, sym);
					}
				}
			}
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
