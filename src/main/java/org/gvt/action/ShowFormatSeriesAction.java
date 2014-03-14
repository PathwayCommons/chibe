package org.gvt.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.gvt.ChisioMain;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.gui.ItemSelectionRunnable;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.patika.mada.util.CausativePath;

import java.io.*;
import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ShowFormatSeriesAction extends Action
{
	private ChisioMain main;
	private String formatFilename;
	private String lastLocation;
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
		super("Load formatting series...");
		this.setToolTipText(this.getText());
		this.main = main;
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

		String filename = getFilename();
		if (filename == null) return;

		series = loadFormattingSeries(filename);
		names = getSeriesNames(series);

		if (!names.isEmpty())
		{
			// Open a dialog so that user selected the specific path to visualize

			ItemSelectionDialog dialog = new ItemSelectionDialog(main.getShell(),
				250,
				"Timepoint Selection Dialog",
				"Select timepoint of interest",
				new ArrayList<String>(names), new ArrayList<String>(), false, false, new Runner());

			dialog.setUpdateUponSelection(true);
			dialog.setDoSort(false);
			Object lastItem = dialog.open();

			if (lastItem == null || !lastItem.equals(ItemSelectionDialog.NONE))
			{
				dialog.runAsIfSelected(ItemSelectionDialog.NONE);
			}
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

	private String getFilename()
	{
		if (formatFilename == null)
		{
			// choose an input file.
			FileDialog fileChooser = new FileDialog(main.getShell(), SWT.OPEN);
			fileChooser.setFilterExtensions(new String[]{"*.formatseries"});
			fileChooser.setFilterNames(new String[]{"Format Series File (*.formatseries)"});

			if (lastLocation != null) fileChooser.setFilterPath(lastLocation);

			String f = fileChooser.open();

			if (f != null) lastLocation = new File(f).getParent();

			return f;

		}
		return formatFilename;
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