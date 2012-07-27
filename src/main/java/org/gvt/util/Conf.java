package org.gvt.util;

import org.eclipse.swt.graphics.Color;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class Conf
{
	public static final String PATHWAY_COMMONS_URL = "PATHWAY_COMMONS_URL";

    public static final String CBIOPORTAL_URL = "CBIOPORTAL_URL";

	public static final String EXPERIMENT_UP_COLOR = "EXPERIMENT_UP_COLOR";
	public static final String EXPERIMENT_DOWN_COLOR = "EXPERIMENT_DOWN_COLOR";
	public static final String EXPERIMENT_MIDDLE_COLOR = "EXPERIMENT_MIDDLE_COLOR";

	public static final String EXPERIMENT_MAX_UPREGULATION = "EXPERIMENT_MAX_UPREGULATION";
	public static final String EXPERIMENT_NO_CHANGE_UPPER_BOUND = "EXPERIMENT_NO_CHANGE_UPPER_BOUND";
	public static final String EXPERIMENT_NO_CHANGE_LOWER_BOUND = "EXPERIMENT_NO_CHANGE_LOWER_BOUND";
	public static final String EXPERIMENT_MAX_DOWNREGULATION = "EXPERIMENT_MAX_DOWNREGULATION";

	public static final String DISPLAY_FRAGMENT_FEATURE = "DISPLAY_FRAGMENT_FEATURE";

	public static final String CONF_FILENAME = "`";

	private static String confPath;

	private static Map<String, String> conf;
	
	private static Map<String, String> parse()
	{
		Map<String, String> map = new HashMap<String, String>();

		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(confPath));
			
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

	private static boolean writeDefaultConfFile()
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(confPath));

//			writer.write(PATHWAY_COMMONS_URL + " = http://www.pathwaycommons.org/pc2/\n");
			writer.write(PATHWAY_COMMONS_URL + " = http://awabi.cbio.mskcc.org/cpath2/\n");

            writer.write(CBIOPORTAL_URL + " = http://www.cbioportal.org/public-portal/webservice.do?\n");

            writer.write(EXPERIMENT_UP_COLOR + " = 230 0 0\n");
			writer.write(EXPERIMENT_DOWN_COLOR + " = 0 0 230\n");
			writer.write(EXPERIMENT_MIDDLE_COLOR + " = 230 230 230\n");

			writer.write(EXPERIMENT_MAX_UPREGULATION + " = 2\n");
			writer.write(EXPERIMENT_NO_CHANGE_UPPER_BOUND + " = 1\n");
			writer.write(EXPERIMENT_NO_CHANGE_LOWER_BOUND + " = -1\n");
			writer.write(EXPERIMENT_MAX_DOWNREGULATION + " = -2\n");

			writer.write(DISPLAY_FRAGMENT_FEATURE + " = false\n");

			writer.close();
			return true;
		}
		catch (FileNotFoundException e)
		{
			return false;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static String get(String property)
	{
		if (conf.containsKey(property)) return conf.get(property);
		else return "";
	}
	
	public static Color getColor(String key)
	{
		String val = get(key);
		if (val != null)
		{
			String[] v = val.split(" ");
			if (v.length == 3)
			{
				try
				{
					return new Color(null,
						Integer.parseInt(v[0]), Integer.parseInt(v[1]), Integer.parseInt(v[2]));
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public static double getNumber(String key)
	{
		String val = get(key);
		if (val != null)
		{
			try
			{
				return Double.parseDouble(val);
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	static
	{
		confPath = Conf.class.getResource("").getFile() + File.separator + CONF_FILENAME;
		File confFile = new File(confPath);
		
		if (!confFile.exists())
		{
			if (!writeDefaultConfFile())
			{
				confPath = CONF_FILENAME;
				writeDefaultConfFile();
			}
		}

		conf = parse();
	}
}
