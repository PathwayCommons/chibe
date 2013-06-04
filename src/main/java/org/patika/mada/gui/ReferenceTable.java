package org.patika.mada.gui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.util.*;

/**
 * @author Recep Colak
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ReferenceTable extends JTable
{
	/**
	 * Cell editor for combo box manipulation
	 */
	DefaultCellEditor ce;

	/**
	 *
	 */
	private ExperimentDataConvertionWizard mdcw;

	/**
	 * A predicted mapping from the column names to the references in graph.
	 */
	private Map<String, String> predictedMatching;

	/**
	 * to distinguish where the call comes from, wizard or automatic
	 */
	private boolean fromWizard;
// ---------------------------------------------------------------------
// Section: Constructor(s)
// ---------------------------------------------------------------------

	/**
	 * Constructor
	 *
	 * @param mdcw the wizard
	 * @param columns	   , the columns got from user's data/platform file
	 *                      to be mapped with supported reference types.
	 * @param referenceList , the supported reference types
	 * @param predictedMatching predicted mapping from column names to ref in graph
	 */
	ReferenceTable(ExperimentDataConvertionWizard mdcw,
		List<String> columns,
		String[] referenceList,
		Map<String, String> predictedMatching)
	{
		this.mdcw = mdcw;
		this.predictedMatching = predictedMatching;
		this.fromWizard = true;
		//this.setBackground(MicroarrayApplet.buttoncolor);
		this.setModel(new ReferenceTableModel(mdcw, columns));
		//set the patika reference column as the combo box
		TableColumn patikaColumn = this.getColumnModel().getColumn(1);
		JComboBox cb = new JComboBox(referenceList);
		//	cb.setBackground(MicroarrayApplet.buttoncolor);
		patikaColumn.setCellEditor(new DefaultCellEditor(cb));

		//Set up tool tips for the sport cells.
		DefaultTableCellRenderer renderer =
			new DefaultTableCellRenderer();
		//	renderer.setBackground(MicroarrayApplet.buttoncolor);
		renderer.setToolTipText("Click for combo box");
		patikaColumn.setCellRenderer(renderer);
	}

	/**
	 * Constructor for automatic conversion version
	 *
	 * @param columns
	 * @param predictedMatching
	 */
	ReferenceTable(List<String> columns,Map<String, String> predictedMatching)
	{
		this.predictedMatching = predictedMatching;
		this.fromWizard = false;

		this.setModel(new ReferenceTableModel(mdcw, columns));
	}

// ---------------------------------------------------------------------
// Section: Accesssors
// ---------------------------------------------------------------------

	/**
	 * this method checks whether user has performed the
	 * the required mapping
	 * @param hasPlatformFile has any?
	 * @return true if user can press next
	 */
	public boolean canNext(boolean hasPlatformFile)
	{
		boolean isKeyToDataFileSet = false;
		boolean isAnyReferenceSet = false;

		for (int i = 0; i < getRowCount(); i++)
		{
			String columnValue = (String) this.getModel().getValueAt(i, 1);

			if (columnValue.equals("Key to data file(s)"))
			{
				isKeyToDataFileSet = true;
			}
			else if (!(columnValue.equals("None")))
			{
				isAnyReferenceSet = true;
			}
		}

		if (hasPlatformFile)
		{
			return isKeyToDataFileSet & isAnyReferenceSet;
		}
		else
		{
			return isAnyReferenceSet;
		}
	}


	public void updateTable(List<String> columns)
	{
		this.setModel(new ReferenceTableModel(this.mdcw, columns));
		this.repaint();
	}

// ---------------------------------------------------------------------
// Section: Getters & Setters
// ---------------------------------------------------------------------

	/**
	 * This method returns a hashtable of mapped references, whose
	 * keys are of from supported reference types and
	 * values are of from column types of user's platform file.
	 * @return map (supported --> column name)
	 */
	public HashMap<String, String> getMappedReferences()
	{
		HashMap<String, String> hm = new HashMap<String, String>();

		for (int i = 0; i < getRowCount(); i++)
		{
			if (getValueAt(i, 1).equals("None")
				|| getValueAt(i, 1).equals("Key to data file(s)"))
			{
				// ignore
			}
			else
			{
				hm.put(getValueAt(i, 1).toString(), getValueAt(i, 0).toString());
			}
		}

		//TODO document below changes
		// now put the key column. note that key column
		// is set by the user if the experiment has a platform file
		// otherwise a key column is set within the referenced ones.
		if (getNameOfKeyColumn().equals(""))
		{
			hm.put(ExperimentDataConvertionWizard.KEY_COLUMN, hm.values().toArray()[0].toString());
		}
		else
		{
			hm.put(ExperimentDataConvertionWizard.KEY_COLUMN, getNameOfKeyColumn());
		}
		return hm;
	}


	/**
	 * This method return the name of yhe key column. The key column
	 * is the one selected as "Key to Data File(s)" by the user providing that
	 * the experiment has a platform file. Otherwise, one of the mapped
	 * reference type is selected as the key column, namely the
	 * first one.
	 * @return name of the key column
	 */
	public String getNameOfKeyColumn()
	{
		String key = "";
		for (int i = 0; i < getRowCount() - 1; i++)
		{
			if ((getValueAt(i, 1).equals("Key to data file(s)")))
			{
				key = (((String) getValueAt(i, 0)));
			}
		}

		// no platform file, just assing the first mapped column as key column
		// !!modification!!: above is not a good idea since the first selected
		// reference may not be present on some entries. So I modified this one
		// to return the first column as the key. Is this a good idea? Well, all
		// the microarray data files that I view contained a key at the first
		// column.
		if (key.equals(""))
		{
			key = (String) getValueAt(0, 0);
		}
		return key;
	}

// ---------------------------------------------------------------------
// Section: Table model for mapping table
// ---------------------------------------------------------------------

	public class ReferenceTableModel extends AbstractTableModel
	{
		/**
		 *
		 */
		private ExperimentDataConvertionWizard mdcw;
		/**
		 * Table parameters
		 */
		String[] columnNames = {"Column", "Maps to"};

		String[][] data = {};

		/**
		 * Constructor
		 * @param mdcw the wizard
		 * @param userColumns columns in data file
		 */
		ReferenceTableModel(ExperimentDataConvertionWizard mdcw, List<String> userColumns)
		{
			super();
			this.mdcw = mdcw;
			data = new String[userColumns.size()][2];
			Iterator<String> iter = userColumns.iterator();
			for (int i = 0; iter.hasNext(); i++)
			{
				data[i][0] = iter.next();
				data[i][1] = "None";
			}

			mapSupportedReferences();
		}

		public void setValueAt(Object value, int row, int col)
		{
			boolean isLegal = true;

			for (int i = 0; i < getRowCount(); i++)
			{
				if (value.equals("None") || i == row)
				{
					// do nothing
				}
				else
				{
					if (data[i][1].equals(value))
					{
						JOptionPane.showMessageDialog(null,
							value + " is already mapped! ",
							"Ilegal Mapping",
							JOptionPane.WARNING_MESSAGE);
						isLegal = false;
						break;
					}
				}
			}

			if (isLegal)
			{
				data[row][col] = value.toString();
				fireTableCellUpdated(row, col);
			}
		}

		// ---------------------------------------------------------------------
		// Section: Accessors
		// ---------------------------------------------------------------------
		public int getColumnCount()
		{
			return columnNames.length;
		}

		public int getRowCount()
		{
			return data.length;
		}

		public String getColumnName(int col)
		{
			return columnNames[col];
		}

		public Object getValueAt(int row, int col)
		{
			return data[row][col];
		}


		public boolean isCellEditable(int row, int col)
		{
			return col != 0;
		}


		public Class getColumnClass(int c)
		{
			return getValueAt(0, c).getClass();
		}

		// ---------------------------------------------------------------------
		// Section:
		// ---------------------------------------------------------------------

		/**
		 * This method automatically recognizes columns if they are within the
		 * supported reference type lists. Hence, saves user from time.
		 */
		private void mapSupportedReferences()
		{
			String massage = "ChiBE performed the below reference mapping:\n\n";
			int numberOfMappedColumns = 0;
			for (int i = 0; i < data.length; i++)
			{
				String columnInFile = data[i][0];

				String ref = predictedMatching.get(columnInFile);

				if (ref != null)
				{
					setValueAt(ref, i, 1);
					massage += columnInFile + "   ->   " + ref + "\n";
					numberOfMappedColumns++;
				}
			}

			massage += "\nIf you wish you may change this mapping! ";
			if (numberOfMappedColumns > 0 && fromWizard)
			{
				JOptionPane.showMessageDialog(this.mdcw,
					massage,
					null,
					JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
}
