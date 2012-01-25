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
	private static Map<String, Integer> sym2id;

	public static void main(String[] args)
	{
		System.out.println(getHGNCID("BAX"));
	}

	/**
	 * Provides HGNC ID of the given approved gene symbol.
	 * @param symbol
	 * @return
	 */
	public static Integer getHGNCID(String symbol)
	{
		return sym2id.get(symbol);
	}
	
	static
	{
		try
		{
			sym2id = new HashMap<String, Integer>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				HGNCUtil.class.getResourceAsStream("hgnc.txt")));
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");
				String sym = token[1];
				Integer id = Integer.parseInt(token[0]);
				sym2id.put(sym, id);
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
