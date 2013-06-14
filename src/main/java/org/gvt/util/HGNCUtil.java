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

	public static void main(String[] args)
	{
		System.out.println(getHGNCID("BAX"));
	}

	/**
	 * Provides HGNC ID of the given approved gene symbol.
	 * @param symbol
	 * @return
	 */
	public static String getHGNCID(String symbol)
	{
		symbol = getOfficial(symbol);
		if (symbol != null) return sym2id.get(symbol);
		return null;
	}

    public static String getSymbolByID(String hgncID)
   	{
   		return id2sym.get(hgncID);
   	}
	
	public static String getOfficial(String symbol)
	{
		if (sym2id.containsKey(symbol)) return symbol;
		else if (old2new.containsKey(symbol)) return old2new.get(symbol);
		else return null;
	}
	
	public static boolean idExists(String id)
	{
		return id2sym.containsKey(id);
	}

	public static boolean isKnown(String symbol)
	{
		return sym2id.containsKey(symbol) || old2new.containsKey(symbol);
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
