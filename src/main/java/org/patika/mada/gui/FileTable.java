package org.patika.mada.gui;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.Vector;

/**
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class FileTable extends JTable
{
	/**
	 *	Cell editor for combo box manipulation
	 */
	DefaultCellEditor ce;

// ---------------------------------------------------------------------
// Section: Constructor(s)
// ---------------------------------------------------------------------

	/**
	 *	Constructor
	 */
	FileTable (File file)
	{
		this.setModel(new FileTableModel(file));
        this.setAutoscrolls(true);
		this.setDragEnabled(false);
		//this.setBackground(MicroarrayApplet.buttoncolor);

		//set number column's size
		TableColumn noColumn =  this.getColumnModel().getColumn(0);
		noColumn.setPreferredWidth(45);
		noColumn.setMaxWidth(45);
		noColumn.setMinWidth(45);

		//set remove column's size
		TableColumn removeColumn =  this.getColumnModel().getColumn(2);
		removeColumn.setPreferredWidth(60);
		removeColumn.setMaxWidth(60);
		removeColumn.setMinWidth(60);



		//set the patika reference column as the combo box
		TableColumn patikaColumn =  this.getColumnModel().getColumn(1);
		JCheckBox cb = new JCheckBox();
		//cb.setBackground(MicroarrayApplet.buttoncolor);
		patikaColumn.setCellEditor(new DefaultCellEditor(cb));

		  //Set up tool tips for the sport cells.
        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();
        renderer.setToolTipText("Click to select");
        patikaColumn.setCellRenderer(renderer);
	}

// ---------------------------------------------------------------------
// Section: Accesssors
// ---------------------------------------------------------------------

	public boolean canNext()
	{
		if(this.getModel().getRowCount()>=1)
			return true;
		else
			return false;
	}



	public void addRow(File newFile)
	{
		((FileTableModel)this.getModel()).addRow(newFile);
		this.validate();
	}

	public void removeRow(int rowNum)
	{
		((FileTableModel)this.getModel()).deleteRow(rowNum);
		this.validate();
	}


// ---------------------------------------------------------------------
// Section: Table model for mapping table
// ---------------------------------------------------------------------

	public class FileTableModel extends AbstractTableModel
 	{

		// data vectors
		Vector files;
		Vector checkBoxes;
		Vector fileNo;

		/**
		 *	Constructor
		 */
		FileTableModel(File file)
		{
			files = new Vector();
			checkBoxes = new Vector();
			fileNo = new Vector();

			fileNo.add(new Integer(1));
			files.add(file);
			checkBoxes.add(new Boolean("false"));

		}


		// ---------------------------------------------------------------------
		// Section: Accessors
		// ---------------------------------------------------------------------


		public int getColumnCount()
		{
			return 3;
		}

		public int getRowCount()
		{
			return fileNo.size();
		}

		public String getColumnName(int col)
		{

			if(col==0)
			{
				return "No";
			}
			else if(col ==1)
			{
				return "File Name";
			}
			else if(col == 2)
			{
				return "Remove";
			}

			return "";

		}

		public Object getValueAt(int row, int col)
		{
			if(col ==0)
			{
				return fileNo.get(row);
			}
			else if(col == 1)
			{
				return ((File)files.get(row)).getName();
			}
			else if(col ==2)
			{
				return checkBoxes.get(row);
			}

			return null;

		}

		public boolean isCellEditable(int row, int col)
		{
			if(col <=1)
			{
				return false;
			}
			else
			{
				return true;
			}
		}

		public void setValueAt(Object value, int row, int col)
		{
			if(col == 0)
			{
        		fileNo.set(row, value);
			}
			else if(col == 1)
			{
				files.set(row, value);
			}
			else if(col ==2)
			{
				checkBoxes.set(row, value);
			}

        	fireTableCellUpdated(row, col);
    	}

        public Class getColumnClass(int c)
		{
            return getValueAt(0, c).getClass();
        }

		public void addRow(File newFile)
		{
			fileNo.add(new Integer(fileNo.size()+1));
			files.add(newFile);
			checkBoxes.add(new Boolean("false"));
			this.fireTableDataChanged();
		}

		public void deleteRow(int rowNum)
		{
			fileNo.remove(rowNum);
			files.remove(rowNum);
			checkBoxes.remove(rowNum);
		}
	}
}
