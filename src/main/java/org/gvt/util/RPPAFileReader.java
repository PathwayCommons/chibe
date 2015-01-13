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

	public static List<String> getNamesOfNumberColumns(String filename)
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

	public static List<RPPAData> readAnnotation(String filename, String idname,
		String symbolname, String psitename, String effectName)
	{
		try
		{
			List<RPPAData> datas = new ArrayList<RPPAData>();

			Scanner sc = new Scanner(new File(filename));
			String s = sc.nextLine();
			List<String> cols = Arrays.asList(s.split("\t"));

			int colInd = cols.indexOf(idname);
			int symbolInd = cols.indexOf(symbolname);
			int siteInd = cols.indexOf(psitename);
			int effectInd = effectName == null ? -1 : cols.indexOf(effectName);

			while (sc.hasNextLine())
			{
				String[] row = sc.nextLine().split("\t");
				String id = row[colInd];
				String syms = row[symbolInd];
				String sites = row.length > siteInd ? row[siteInd] : "";
				String effect = effectInd >= 0 && row.length > effectInd ? row[effectInd] : null;

				List<String> genes = Arrays.asList(syms.split("\\s+"));
				Map<String, List<String>> siteMap = sites.isEmpty() ? null : new HashMap<String, List<String>>();
				if (!sites.isEmpty())
				{
					String[] perGene = sites.split("\\s+");
					for (int i = 0; i < perGene.length; i++)
					{
						siteMap.put(genes.get(i), Arrays.asList(perGene[i].split("\\|")));
					}
					if (siteMap.size() < genes.size())
					{
						for (int i = siteMap.size(); i < genes.size(); i++)
						{
							siteMap.put(genes.get(i), siteMap.get(genes.get(0)));
						}
					}
				}

				RPPAData data = new RPPAData(id, null, genes, siteMap);

				if (effect != null)
				{
					data.effect = effect.equals("c") ? RPPAData.SiteEffect.COMPLEX :
						effect.equals("a") ? RPPAData.SiteEffect.ACTIVATING : effect.equals("i") ?
							RPPAData.SiteEffect.INHIBITING : null;
				}

				datas.add(data);
			}
			return datas;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
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
			List<String> header = Arrays.asList(sc.nextLine().split("\t"));

			int idInd = header.indexOf(idName);
			int[] valInd = new int[colname.length];
			for (int i = 0; i < colname.length; i++)
			{
				valInd[i] = header.indexOf(colname[i]);
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

	public static void addValues(List<RPPAData> datas, String filename, String idColName,
		List<String> vals0, List<String> vals1)
	{
		Map<String, Double>[] v0 = readVals(
			filename, idColName, vals0.toArray(new String[vals0.size()]));

		Map<String, Double>[] v1 = vals1 == null || vals1.isEmpty() ? null :
			readVals(filename, idColName, vals1.toArray(new String[vals1.size()]));

		List<RPPAData> remove = new ArrayList<RPPAData>();

		for (RPPAData data : datas)
		{
//			data.vals = new double[v1 == null ? 1 : 2][];
			data.vals = new double[2][];
			data.vals[0] = new double[v0.length];
			for (int i = 0; i < v0.length; i++)
			{
				Double doubVal = v0[i].get(data.id);
				if (doubVal != null) data.vals[0][i] = doubVal;
				else remove.add(data);
			}
		}
		datas.removeAll(remove);

		if (v1 != null)
		{
			for (RPPAData data : datas)
			{
				data.vals[1] = new double[v1.length];
				for (int i = 0; i < v1.length; i++)
				{
					data.vals[1][i] = v1[i].get(data.id);
				}
			}
		}
	}

	public static int getPotentialIDColIndex(String[] header)
	{
		return getPotentialColIndex(header, "id");
	}

	public static int getPotentialSymbolColIndex(String[] header)
	{
		return getPotentialColIndex(header, "symbol");
	}

	public static int getPotentialSiteColIndex(String[] header)
	{
		return getPotentialColIndex(header, "site");
	}

	public static int getPotentialEffectColIndex(String[] header)
	{
		return getPotentialColIndex(header, "effect");
	}

	private static int getPotentialColIndex(String[] header, String find)
	{
		for (int i = 0; i < header.length; i++)
		{
			if (header[i].toLowerCase().contains(find)) return i;
		}
		return -1;
	}
}
