package org.gvt.util;

import org.cbio.causality.model.RPPAData;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class RPPAFileReader
{
	public static String[] getHeader(String filename)
	{
		try
		{
			Scanner sc = new Scanner(new File(filename));
			String line = sc.nextLine();
			return line.split("\t");
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static List<String> getIndexesOfNumberColumns(String filename)
	{
		try
		{
			Scanner sc = new Scanner(new File(filename));
			String[] header = sc.nextLine().split("\t");

			String[] row = sc.nextLine().split("\t");

			List<String> names = new ArrayList<String>();

			for (int i = 0; i < row.length; i++)
			{
				try
				{
					Double.parseDouble(row[i]);
					names.add(header[i]);
				}
				catch (NumberFormatException e){}
			}

			return names;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static Map<String, Set<String>> readAnnotation(String filename, String idname,
		String symbolname, String psitename, String effectName,
		Map<String, Set<String>> pSites, Map<String, RPPAData.SiteEffect> effectMap)
	{
		try
		{
			Scanner sc = new Scanner(new File(filename));
			String[] header = sc.nextLine().split("\t");

			int idInd = Arrays.binarySearch(header, idname);
			int symInd = Arrays.binarySearch(header, symbolname);
			int siteInd = Arrays.binarySearch(header, psitename);
			int effInd = effectName != null ? Arrays.binarySearch(header, effectName) : -1;

			Map<String, Set<String>> symbolMap = new HashMap<String, Set<String>>();

			while (sc.hasNextLine())
			{
				String[] row = sc.nextLine().split("\t");
				if (row.length < 2) break;

				String id = row[idInd];

				symbolMap.put(id, readVals(row[symInd]));
				pSites.put(id, readVals(row[siteInd]));

				if (effectName != null)
				{
					RPPAData.SiteEffect eff = row[effInd].trim().toLowerCase().startsWith("a") ?
						RPPAData.SiteEffect.ACTIVATING :
						row[effInd].trim().toLowerCase().startsWith("i") ?
						RPPAData.SiteEffect.INHIBITING :
						row[effInd].trim().toLowerCase().startsWith("c") ?
						RPPAData.SiteEffect.COMPLEX : null;

					effectMap.put(id, eff);
				}
			}

			return symbolMap;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	protected static Set<String> readVals(String s)
	{
		s = s.replace("|", " ");
		s = s.replace(",", " ");
		return new HashSet<String>(Arrays.asList(s.split("\\s+")));
	}

	public static Map<String, Double>[] readVals(String filename, String idName, String... colname)
	{
		try
		{
			Map<String, Double>[] valMaps = new Map[colname.length];
			for (int i = 0; i < valMaps.length; i++)
			{
				valMaps[i] = new HashMap<String, Double>();
			}

			Scanner sc = new Scanner(new File(filename));
			String[] header = sc.nextLine().split("\t");

			int idInd = Arrays.binarySearch(header, idName);
			int[] valInd = new int[colname.length];
			for (int i = 0; i < colname.length; i++)
			{
				valInd[i] = Arrays.binarySearch(header, colname[i]);
			}

			while (sc.hasNextLine())
			{
				String[] row = sc.nextLine().split("\t");

				for (int i = 0; i < colname.length; i++)
				{
					valMaps[i].put(row[idInd], Double.parseDouble(row[valInd[i]]));
				}
			}

			return valMaps;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static List<RPPAData> prepareData(Map<String, Set<String>> symbolMap,
		Map<String, Set<String>> siteMap, Map<String, RPPAData.SiteEffect> effectMap,
		Map<String, Double>[] valMaps0, Map<String, Double>[] valMaps1)
	{
		List<RPPAData> list = new ArrayList<RPPAData>();

		for (String id : symbolMap.keySet())
		{
			double[] v0 = getVals(valMaps0, id);
			double[] v1 = valMaps1 == null ? null : getVals(valMaps1, id);

			RPPAData data = new RPPAData(id, v0, v1, symbolMap.get(id), siteMap.get(id));

			if (effectMap != null) data.effect = effectMap.get(id);

			list.add(data);
		}

		return list;
	}

	private static double[] getVals(Map<String, Double>[] valMaps, String key)
	{
		double[] v = new double[valMaps.length];
		for (int i = 0; i < valMaps.length; i++)
		{
			v[i] = valMaps[i].get(key);
		}
		return v;
	}

	public static String getPotentialIDColname(String[] header)
	{
		return getPotentialColname(header, "id");
	}

	public static String getPotentialSymbolColname(String[] header)
	{
		return getPotentialColname(header, "symbol");
	}

	public static String getPotentialSiteColname(String[] header)
	{
		return getPotentialColname(header, "site");
	}

	public static String getPotentialEffectColname(String[] header)
	{
		return getPotentialColname(header, "effect");
	}

	private static String getPotentialColname(String[] header, String find)
	{
		for (String s : header)
		{
			if (s.toLowerCase().contains(find)) return s;
		}
		return null;
	}
}
