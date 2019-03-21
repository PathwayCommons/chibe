package org.gvt.util;

import org.cbio.causality.util.BaseDir;
import org.eclipse.swt.graphics.Color;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Ozgun Babur
 */
public enum Conf
{
	DRAWPPI;

	public static final String DRAW_PPI_EDGES = "DRAW_PPI_EDGES";

	public static final String PATHWAY_COMMONS_URL = "PATHWAY_COMMONS_URL";
	public static final String PC_SIF_FILE = "PC_SIF_FILE";
	public static final String PC_SIF_FILE_URL = "PC_SIF_FILE_URL";
	public static final String REACTION_2_GENE_FILE = "REACTION_2_GENE_FILE";
	public static final String REACTION_2_GENE_FILE_URL = "REACTION_2_GENE_FILE_URL";
	public static final String PATHWAY_2_GENE_FILE = "PATHWAY_2_GENE_FILE";
	public static final String PATHWAY_2_GENE_FILE_URL = "PATHWAY_2_GENE_FILE_URL";

    public static final String CBIOPORTAL_URL = "CBIOPORTAL_URL";
    public static final String CBIOPORTAL_USE_CACHE = "CBIOPORTAL_USE_CACHE";
    public static final String CBIOPORTAL_CACHE_DIR = "CBIOPORTAL_CACHE_DIR";

	public static final String EXPERIMENT_UP_COLOR = "EXPERIMENT_UP_COLOR";
	public static final String EXPERIMENT_DOWN_COLOR = "EXPERIMENT_DOWN_COLOR";
	public static final String EXPERIMENT_MIDDLE_COLOR = "EXPERIMENT_MIDDLE_COLOR";

	public static final String EXPERIMENT_MAX_UPREGULATION = "EXPERIMENT_MAX_UPREGULATION";
	public static final String EXPERIMENT_NO_CHANGE_UPPER_BOUND = "EXPERIMENT_NO_CHANGE_UPPER_BOUND";
	public static final String EXPERIMENT_NO_CHANGE_LOWER_BOUND = "EXPERIMENT_NO_CHANGE_LOWER_BOUND";
	public static final String EXPERIMENT_MAX_DOWNREGULATION = "EXPERIMENT_MAX_DOWNREGULATION";

	public static final String DISPLAY_FRAGMENT_FEATURE = "DISPLAY_FRAGMENT_FEATURE";
	public static final String HIDE_COMPARTMENT_EDGE_THRESHOLD = "HIDE_COMPARTMENT_EDGE_THRESHOLD";
	public static final String USE_SIF_GROUPING = "USE_SIF_GROUPING";
	public static final String SPREAD_DOUBLE_EDGES = "SPREAD_DOUBLE_EDGES";
	public static final String DEFAULT = "DEFAULT";

	public static final String CONVERT_INTERACTS_WITH_TO_IN_COMPLEX_WITH = "CONVERT_INTERACTS_WITH_TO_IN_COMPLEX_WITH";

	public static final String CONF_FILENAME = "chibe-conf.txt";

	private static String[] pathCandidate;

	private static Map<String, String> conf;

	private static String baseDir;
	
	private static Map<String, String> parse(BufferedReader reader)
	{
		Map<String, String> map = new HashMap<String, String>();

		try
		{
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				if (line.startsWith("#") || !line.contains("=")) continue;

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
		s += DRAW_PPI_EDGES + " = true\n";
		s += PATHWAY_COMMONS_URL + " = http://www.pathwaycommons.org/pc2/\n";
//		s += PATHWAY_COMMONS_URL + " = http://purl.org/chibe/pc2/\n";
//		s += PATHWAY_COMMONS_URL + " = http://webservice.baderlab.org:48080/\n";

		s += PC_SIF_FILE + " = " + DEFAULT + "\n";
		s += PC_SIF_FILE_URL + " = http://cbio.mskcc.org/~ozgun/PC.sif.gz\n";
		s += REACTION_2_GENE_FILE + " = " + DEFAULT + "\n";
		s += REACTION_2_GENE_FILE_URL + " = https://raw.githubusercontent.com/PathwayAndDataAnalysis/repo/master/resource-files/reaction2gene.txt\n";
		s += PATHWAY_2_GENE_FILE + " = " + DEFAULT + "\n";
		s += PATHWAY_2_GENE_FILE_URL + " = https://raw.githubusercontent.com/PathwayAndDataAnalysis/repo/master/resource-files/pathway2gene.txt\n";

		s += CBIOPORTAL_URL + " = http://www.cbioportal.org/public-portal/webservice.do?\n";
		s += CBIOPORTAL_USE_CACHE + " = true\n";
		s += CBIOPORTAL_CACHE_DIR + " = " + DEFAULT + "\n";

		s += EXPERIMENT_UP_COLOR + " = 230 0 0\n";
		s += EXPERIMENT_DOWN_COLOR + " = 0 0 230\n";
		s += EXPERIMENT_MIDDLE_COLOR + " = 230 230 230\n";

		s += EXPERIMENT_MAX_UPREGULATION + " = 2\n";
		s += EXPERIMENT_NO_CHANGE_UPPER_BOUND + " = 1\n";
		s += EXPERIMENT_NO_CHANGE_LOWER_BOUND + " = -1\n";
		s += EXPERIMENT_MAX_DOWNREGULATION + " = -2\n";

		s += DISPLAY_FRAGMENT_FEATURE + " = false\n";
		s += HIDE_COMPARTMENT_EDGE_THRESHOLD + " = 20\n";
		s += USE_SIF_GROUPING + " = true\n";
		s += SPREAD_DOUBLE_EDGES + " = true\n";

		s += CONVERT_INTERACTS_WITH_TO_IN_COMPLEX_WITH + " = true\n";
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

	/**
	 * Decides the base directory for ChiBE file operations, and loads the configuration from the
	 * resource file.
	 */
	static
	{
		decideBaseDir();

		pathCandidate = new String[]{
			System.getProperty("user.dir") + File.separator,
			Conf.class.getResource("").getFile() + File.separator,
			System.getProperty("user.home") + File.separator + ".chibe" + File.separator,
			""};

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

	/**
	 * Decides the base directory for file operations based on writiblity.
	 */
	private static void decideBaseDir()
	{
		String dir = System.getProperty("user.dir");
		if (isWritable(dir))
		{
			baseDir = dir + File.separator;
		}
		else
		{
			dir = System.getProperty("user.home") + File.separator + ".chibe";
			File f = new File(dir);
			f.mkdir();

			if (isWritable(dir))
			{
				baseDir = dir + File.separator;
			}
			else
			{
				throw new RuntimeException("Neither \"user.dir\" nor \"user.home\" are writable.");
			}
		}

		BaseDir.setDir(baseDir);
	}

	/**
	 * Checks if the directory is writable by creating and a temp file.
	 * @param loc location to check
	 * @return true if writable
	 */
	public static boolean isWritable(String loc)
	{
		Random r = new Random();
		String x = loc + File.separator + "tempchibefile" + r.nextInt(10000) + ".txt";
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(x));
			writer.write("test");
			writer.close();

			File f = new File(x);
			f.delete();
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
		catch (SecurityException e)
		{
			return false;
		}
	}

	/**
	 * Gets the directory to store downloaded experiments.
	 * @return
	 */
	public static String getExperimentsDir()
	{
		return baseDir + "experiments" + File.separator;
	}

	/**
	 * Directory used for caching cbioportal data.
	 * @return
	 */
	public static String getPortalCacheDir()
	{
		String dir;

		if (!conf.containsKey(CBIOPORTAL_CACHE_DIR) ||
			conf.get(CBIOPORTAL_CACHE_DIR).equals(DEFAULT))
		{
			dir =  baseDir + "portal-cache" + File.separator;
		}
		else
		{
			dir = conf.get(CBIOPORTAL_CACHE_DIR);
		}

		File f = new File(dir);
		if (!f.exists()) f.mkdirs();
		return dir;
	}

	public static boolean drawPPI()
	{
		return getBoolean(DRAW_PPI_EDGES);
	}

	/**
	 * This is a temporary code. Change this when the blacklist is provided by Pathway Commons.
	 * @return
	 */
	public static String getBlacklistURL()
	{
		return "http://www.pathwaycommons.org/archives/PC2/v9/blacklist.txt";
	}

	public static String getBaseDir()
	{
		return baseDir;
	}
}
