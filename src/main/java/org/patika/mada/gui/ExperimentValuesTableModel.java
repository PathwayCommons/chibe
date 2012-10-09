package org.patika.mada.gui;

import org.eclipse.swt.graphics.Color;
import org.patika.mada.dataXML.Experiment;
import org.patika.mada.dataXML.Reference;
import org.patika.mada.dataXML.Row;
import org.patika.mada.dataXML.ValueTuple;
import org.patika.mada.util.ExperimentDataManager;
import org.patika.mada.util.ExpressionData;
import org.patika.mada.util.XRef;

import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ExperimentValuesTableModel extends AbstractTableModel
{
	//--------------------------------------------------------------------------
	// Section: Instance variables.
	//--------------------------------------------------------------------------

	/**
	 * Number of experiments
	 */
	private Integer[] expNos;

	/**
	 * Names of the experiments
	 */
	private String[] expNames;

	/**
	 * Index of the first experiment in use
	 */
	protected List<Integer> firstExpIndices;

	/**
	 * Index of the second experiment in use. This is -1 if only one experiment
	 * is in use.
	 */
	protected List<Integer> secondExpIndices;

	/**
	 * This is either 2 (secondExpIndex == -1) or 3. This is the number of
	 * columns before experiment values start.
	 */
	protected int expIndexShift;

	/**
	 * Array of rows for fast access
	 */
	private Row[] displayArray;

	private Map<Row, Integer> rowToIndex;

	/**
	 * Mapping from row and column indexes to values for avoiding
	 * re-calculation.
	 */
	private Map<String, String> cache;

	/**
	 * Used for sorting rows according to value of a column.
	 */
	private int compareIndex;

	private Color[] columnColors;

	//--------------------------------------------------------------------------
	// Section: Constructor
	//--------------------------------------------------------------------------

	public ExperimentValuesTableModel(ExperimentDataManager man)
	{
		this.displayArray = new Row[0];
		this.cache = new HashMap<String, String>();

		this.firstExpIndices = new ArrayList<Integer>();
		this.secondExpIndices = new ArrayList<Integer>();

		this.expIndexShift = 1;

		this.configure(man);
	}

	public void configure(ExperimentDataManager man)
	{
		List<Integer> expNos = new ArrayList<Integer>();

		for (Object o : man.getCed().getExperiment())
		{
			Experiment e = (Experiment) o;
			int no = e.getNo();
			assert !expNos.contains(no);
			expNos.add(no);
		}

		this.expNos = expNos.toArray(new Integer[0]);

		this.expNames = new String[this.expNos.length];

		for (int i=0; i < this.expNos.length; i++)
		{
			this.expNames[i] = man.getExperimentName(i);
		}

		this.firstExpIndices = man.getFirstExpIndices();
		this.secondExpIndices = man.getSecondExpIndices();

		this.expIndexShift = (secondExpIndices.isEmpty() &&
			firstExpIndices.size() == 1) ? 1 : 2;
	}

	//--------------------------------------------------------------------------
	// Section: Accessors
	//--------------------------------------------------------------------------

	public int getRowCount()
	{
		return this.displayArray.length;
	}

	public int getColumnCount()
	{
		return expNos.length + expIndexShift;
	}

	public Object getValueAt(int rowIndex, int columnIndex)
	{
		String key = rowIndex + ":" + columnIndex;
		String value = cache.get(key);

		if (value == null)
		{
			Row row = displayArray[rowIndex];

			switch (columnIndex)
			{
				case 0:
					value = this.getRefs(row);
					break;
				case 1:
					if (expIndexShift == 2)
					{
						if (secondExpIndices.isEmpty())
						{
							Double v = getMean(rowIndex, firstExpIndices);
							value = v == null ? "" : "" + v;
						}
						else
						{
							Double v = getRatio(rowIndex);
							value = v == null ? "" : "" + v;
						}
						break;
					}
				default:
					value = this.getValue(row, columnIndex - expIndexShift);
			}

			cache.put(key, value);
		}

		return value;
	}

	public String getColumnName(int column)
	{
		if (column == 0)
		{
			return "References";
		}
		if (column == 1 && expIndexShift == 2)
		{
			return secondExpIndices.isEmpty() ? "Avg value" : "Log Ratio";
		}

		return this.expNames[column - this.expIndexShift];
	}

	public String[] getColumnNames()
	{
		String[] names = new String[getColumnCount()];

		for (int i = 0; i < getColumnCount(); i++)
		{
			names[i] = getColumnName(i);
		}
		return names;
	}

	public Row[] getDisplayArray()
	{
		return displayArray;
	}

	/**
	 * Gets external references out of the row.
	 */
	private String getRefs(Row row)
	{
		String refs = "";

		for (Object o : row.getRef())
		{
			Reference ref = (Reference) o;
			refs += ref.getDb() + ":" + ref.getValue() + " ";
		}
		return refs;
	}

	/**
	 * Gets the value of the specified experiment in the row.
	 */
	private String getValue(Row row, int expIndex)
	{
		assert expIndex < expNos.length :
			"Experiment index out of bounds: " + expIndex;

		for (Object o : row.getValue())
		{
			ValueTuple val = (ValueTuple) o;

			if (val.getNo() == expNos[expIndex])
			{
                return new DecimalFormat("#.####").format(val.getValue());
			}
		}

		return "";
	}

	private Double getMean(int rowIndex, List<Integer> exps)
	{
		List<Double> v = new ArrayList<Double>();

		for (int i : exps)
		{
			String s = (String) getValueAt(rowIndex, i + expIndexShift);
			if (s.length() > 0) v.add(new Double(s));
		}

		if (v.isEmpty())
		{
			return null;
		}
		else
		{
            return Double.parseDouble(new DecimalFormat("#.####").format(mean(v)));
		}
	}

	private Double getRatio(int rowIndex)
	{
		assert !firstExpIndices.isEmpty();
		assert !secondExpIndices.isEmpty();

		List<Double> v1 = new ArrayList<Double>();
		List<Double> v2 = new ArrayList<Double>();

		for (int i : firstExpIndices)
		{
			String s = (String) getValueAt(rowIndex, i + expIndexShift);
			if (s.length() > 0) v1.add(new Double(s));
		}
		for (int i : secondExpIndices)
		{
			String s = (String) getValueAt(rowIndex, i + expIndexShift);
			if (s.length() > 0) v2.add(new Double(s));
		}

		if (!v1.isEmpty() && !v2.isEmpty())
		{
			ExpressionData info = new ExpressionData(mean(v1), mean(v2));
            return Double.parseDouble(new DecimalFormat("#.####").format(info.getValue()));
		}
		else
		{
			return null;
		}
	}

	private double mean(Collection<Double> col)
	{
		double sum = 0;
		for (double d : col)
		{
			sum += d;
		}
		return sum / col.size();
	}

	/**
	 * Used for sending the PIDs of the selected rows.
	 */
	public List<XRef> getSelectedReferences(int[] rowIndex)
	{
		List<XRef> refs = new ArrayList<XRef>();

		for (int r : rowIndex)
		{
			String s = (String) this.getValueAt(r, 0);

			StringTokenizer st = new StringTokenizer(s);
			while (st.hasMoreTokens())
			{
				refs.add(new XRef(st.nextToken()));
			}
		}
		return refs;
	}

	//--------------------------------------------------------------------------
	// Section: Refresh methods
	//--------------------------------------------------------------------------

	/**
	 * Updates the rows to display.
	 */
	public void updateRows(List<Row> displayList)
	{
		this.displayArray = displayList.toArray(new Row[displayList.size()]);
		this.cache.clear();

		if (rowToIndex == null)
		{
			rowToIndex = new HashMap<Row, Integer>();
		}
		rowToIndex.clear();

		int i = 0;
		for (Row row : displayArray)
		{
			rowToIndex.put(row, i++);
		}
	}

	public int getRowIndex(Row row)
	{
		return rowToIndex.get(row);
	}

	public Color getColumnColor(int col)
	{
		if (columnColors == null)
		{
			columnColors = new Color[getColumnCount()];

			for (int i = 0; i < getColumnCount(); i++)
			{
				if (i >= expIndexShift)
				{
					if (firstExpIndices.contains(i - expIndexShift))
					{
						columnColors[i] = gr1Color;
					}
					else if (secondExpIndices.contains(i - expIndexShift))
					{
						columnColors[i] = gr2Color;
					}
					else
					{
						columnColors[i] = defaultColor;
					}
				}
				else if (i == 1 && expIndexShift == 2)
				{
					columnColors[i] = valueColor;
				}
				else
				{
					columnColors[i] = defaultColor;
				}
			}
		}

		return columnColors[col];
	}


	//--------------------------------------------------------------------------
	// Section: Sorting related.
	//--------------------------------------------------------------------------

	public void sort(int index)
	{
		this.compareIndex = index;

		TreeSet<ComparableRow> set = new TreeSet<ComparableRow>();

		for (int i = 0; i < displayArray.length; i++)
		{
			set.add(new ComparableRow(displayArray[i], i));
		}

		List<Row> sorted = new ArrayList<Row>();

		for (ComparableRow cr : set)
		{
			sorted.add(cr.row);
		}

		updateRows(sorted);
	}

	public int getValueIndex()
	{
		if (this.expIndexShift == 2)
		{
			return 1;
		}
		else
		{
			return firstExpIndices.get(0) + expIndexShift;
		}
	}

	class ComparableRow implements Comparable
	{
		Row row;
		int rowIndex;

		public ComparableRow(Row row, int rowIndex)
		{
			this.row = row;
			this.rowIndex = rowIndex;
		}

		public int compareTo(Object o)
		{
			assert o instanceof ComparableRow :
				"Cannot compare with another object!";

			ComparableRow cr = (ComparableRow) o;

			String s1 = (String) getValueAt(rowIndex, compareIndex);
			String s2 = (String) getValueAt(cr.rowIndex, compareIndex);

			int c;

			if (compareIndex == 0)
			{
				c = s1.compareTo(s2);
			}
			else
			{
				Double d1 = s1.length() == 0 ? 0.0 : new Double(s1);
				Double d2 = s2.length() == 0 ? 0.0 : new Double(s2);

				c = d1.compareTo(d2);
			}

			if (c == 0)
			{
				c = (new Integer(rowIndex)).compareTo(cr.rowIndex);
			}

			return c;
		}
	}

	public static final Color gr1Color= new Color(null, 255, 255, 225); // yellow
	public static final Color gr2Color= new Color(null, 225, 225, 255); // blue
	public static final Color valueColor= new Color(null, 255, 235, 235); // reddish
	public static final Color defaultColor= new Color(null, 255, 255, 255); // reddish
}
