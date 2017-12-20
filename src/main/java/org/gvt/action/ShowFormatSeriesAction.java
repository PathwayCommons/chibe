package org.gvt.action;

import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.gui.ItemSelectionRunnable;
import org.gvt.model.basicsif.BasicSIFGraph;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ShowFormatSeriesAction extends ChiBEAction
{
	private String formatFilename;
	private BasicSIFGraph graph;
	private List<String> initialFormat;
	private List<String> names;
	private List<List<String>> series;

	/**
	 * Constructor
	 * @param main main application
	 */
	public ShowFormatSeriesAction(ChisioMain main)
	{
		super("Load formatting...", null, main);
		addFilterExtension(FILE_KEY, new String[]{"*.format", "*.formatseries"});
		addFilterName(FILE_KEY, new String[]{"Format File (*.format)", "Format Series File (*.formatseries)"});
	}

	public void setFormatFilename(String formatFilename)
	{
		this.formatFilename = formatFilename;
	}

	public void run()
	{
		if (!(main.getPathwayGraph() instanceof BasicSIFGraph)) return;

		graph = (BasicSIFGraph) main.getPathwayGraph();
		initialFormat = graph.getCurrentFormat();

		if (formatFilename == null)
		{
			formatFilename = new FileChooser(this).choose(FILE_KEY);
			if (formatFilename == null) return;
		}

		if (formatFilename.endsWith(".formatseries"))
		{
			series = loadFormattingSeries(formatFilename);
			names = getSeriesNames(series);

			if (!names.isEmpty())
			{
				// Open a dialog so that user selected the specific path to visualize

				ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(),
					250,
					"Timepoint Selection Dialog",
					"Select timepoint of interest",
					new ArrayList<String>(names), new ArrayList<String>(Collections.singleton(names.get(0))),
					false, false, new Runner());

				dialog.setUpdateUponSelection(true);
				dialog.setDoSort(false);
				dialog.runAsIfSelected(names.get(0));
				Object lastItem = dialog.open();

				if (lastItem == null || !lastItem.equals(ItemSelectionDialog.NONE))
				{
					dialog.runAsIfSelected(ItemSelectionDialog.NONE);
				}
			}
		}
		else
		{
			List<String> format = loadFormat(formatFilename);
			graph.format(format);
		}

		graph = null;
		formatFilename = null;
		names = null;
		series = null;
		initialFormat = null;
	}

	private class Runner implements ItemSelectionRunnable
	{
		private String lastName;

		public void run(Collection selectedTerms)
		{
			if (selectedTerms.isEmpty())
			{
				return;
			}

			String id = selectedTerms.iterator().next().toString();

			run(id);
		}

		public void run(String name)
		{
			if (name.equals(lastName))
			{
				return;
			}

			if (name.equals(ItemSelectionDialog.NONE))
			{
				graph.format(initialFormat);
			}
			else
			{
				graph.format(series.get(names.indexOf(name)));
			}

			lastName = name;
		}
	}

	private List<String> loadFormat(String filename)
	{
		try
		{
			List<String> list = new ArrayList<String>();
			Scanner sc = new Scanner(new File(filename));

			while (sc.hasNextLine())
			{
				String line = sc.nextLine();
				list.add(line);
			}

			return list;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}

	}

	private List<List<String>> loadFormattingSeries(String filename)
	{
		try
		{
			List<List<String>> list = new ArrayList<List<String>>();
			Scanner sc = new Scanner(new File(filename));

			List<String> current = null;
			while (sc.hasNextLine())
			{
				String line = sc.nextLine();
				if (line.startsWith("group-name\t"))
				{
					if (current != null) list.add(current);
					current = new ArrayList<String>();
				}

				if (current == null) throw new RuntimeException("Invalid Series Format");

				current.add(line);
			}
			list.add(current);

			return list;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private List<String> getSeriesNames(List<List<String>> series)
	{
		List<String> names = new ArrayList<String>(series.size());

		for (List<String> group : series)
		{
			names.add(group.get(0).substring(group.get(0).indexOf("\t") + 1));
			group.remove(0);
		}
		return names;
	}
}