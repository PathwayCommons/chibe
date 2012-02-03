package org.gvt.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class Conf
{
	public static final String PC_URL_KEY = "Pathway Commons URL";
	public static final String CONF_FILENAME = "conf.txt";
	private static String confPath;

	private static Map<String, String> conf;
	
	private static Map<String, String> parse()
	{
		Map<String, String> map = new HashMap<String, String>();

		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				Conf.class.getResourceAsStream(CONF_FILENAME)));
			
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				if (!line.contains("=")) continue;

				String[] tuple = line.split("=");
				tuple[0] = tuple[0].trim();
				tuple[1] = tuple[1].trim();
				map.put(tuple[0], tuple[1]);
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return map;
	}

	private static void writeDefaultConfFile()
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(confPath));

			writer.write(PC_URL_KEY + " = http://awabi.cbio.mskcc.org/pc2/");

			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static String get(String property)
	{
		return conf.get(property);
	}
	
	static
	{
		confPath = Conf.class.getResource("").getFile() + File.separator + CONF_FILENAME;
		File confFile = new File(confPath);
		
		if (!confFile.exists())
		{
			writeDefaultConfFile();
		}

		conf = parse();
	}
}
