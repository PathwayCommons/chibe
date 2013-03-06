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
    public static final String CBIOPORTAL_USE_CACHE = "CBIOPORTAL_USE_CACHE";

	public static final String EXPERIMENT_UP_COLOR = "EXPERIMENT_UP_COLOR";
	public static final String EXPERIMENT_DOWN_COLOR = "EXPERIMENT_DOWN_COLOR";
	public static final String EXPERIMENT_MIDDLE_COLOR = "EXPERIMENT_MIDDLE_COLOR";

	public static final String EXPERIMENT_MAX_UPREGULATION = "EXPERIMENT_MAX_UPREGULATION";
	public static final String EXPERIMENT_NO_CHANGE_UPPER_BOUND = "EXPERIMENT_NO_CHANGE_UPPER_BOUND";
	public static final String EXPERIMENT_NO_CHANGE_LOWER_BOUND = "EXPERIMENT_NO_CHANGE_LOWER_BOUND";
	public static final String EXPERIMENT_MAX_DOWNREGULATION = "EXPERIMENT_MAX_DOWNREGULATION";

	public static final String DISPLAY_FRAGMENT_FEATURE = "DISPLAY_FRAGMENT_FEATURE";

	public static final String CONF_FILENAME = "chibe-conf.txt";

	private static String[] pathCandidate;

	private static Map<String, String> conf;
	
	private static Map<String, String> parse(BufferedReader reader)
	{
		Map<String, String> map = new HashMap<String, String>();

		try
		{
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

	private static String getDefaultConfString()
	{
		String s = "";
		s += PATHWAY_COMMONS_URL + " = http://www.pathwaycommons.org/pc2/\n";
//		s += PATHWAY_COMMONS_URL + " = http://awabi.cbio.mskcc.org/cpath2/\n";

		s += CBIOPORTAL_URL + " = http://www.cbioportal.org/public-portal/webservice.do?\n";
		s += CBIOPORTAL_USE_CACHE + " = true\n";

		s += EXPERIMENT_UP_COLOR + " = 230 0 0\n";
		s += EXPERIMENT_DOWN_COLOR + " = 0 0 230\n";
		s += EXPERIMENT_MIDDLE_COLOR + " = 230 230 230\n";

		s += EXPERIMENT_MAX_UPREGULATION + " = 2\n";
		s += EXPERIMENT_NO_CHANGE_UPPER_BOUND + " = 1\n";
		s += EXPERIMENT_NO_CHANGE_LOWER_BOUND + " = -1\n";
		s += EXPERIMENT_MAX_DOWNREGULATION + " = -2\n";

		s += DISPLAY_FRAGMENT_FEATURE + " = false\n";
		return s.trim();
	}

	private static boolean writeDefaultConfFile(String file)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));

			writer.write(getDefaultConfString());

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

    public static boolean getBoolean(String key) {
        String val = get(key).toLowerCase();
        return val.equals("true");
    }

	private static File searchForConfFile()
	{
		for (String path : pathCandidate)
		{
			String confPath = path + CONF_FILENAME;
			File confFile = new File(confPath);

			if (confFile.exists()) return confFile;
		}

		return null;
	}

	static
	{
		pathCandidate = new String[]{Conf.class.getResource("").getFile() + File.separator,
			"", System.getProperty("user.dir") + File.separator,
			System.getProperty("user.home") + File.separator};

		File confFile = searchForConfFile();

		if (confFile == null)
		{
			int i = 0;
			String file;
			do {
				file = pathCandidate[i++] + CONF_FILENAME;
			} while (!writeDefaultConfFile(file));

			confFile = searchForConfFile();
		}

		if (confFile != null)
		{
			try
			{
				conf = parse(new BufferedReader(new FileReader(confFile)));
			} catch (FileNotFoundException e){e.printStackTrace();}
		}
		else
		{
			conf = parse(new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(getDefaultConfString().getBytes()))));
		}
	}
}
