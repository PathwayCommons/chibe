package org.gvt.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Owner on 2/21/2017.
 */
public class ChEBI
{
	private static Map<String, String> idToName;

	public static String getName(String id)
	{
		return idToName.get(id);
	}

	static
	{
		try
		{
			load();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void load() throws IOException
	{
		idToName = new HashMap<String, String>();
		InputStream is = ChEBI.class.getResourceAsStream("ChEBI.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		reader.readLine();
		String line = reader.readLine();
		while (line != null)
		{
			String[] t = line.split("\t");
			idToName.put(t[0], t[1]);
			line = reader.readLine();
		}
		reader.close();
	}
}
