package org.patika.mada.gui;

import javax.swing.border.TitledBorder;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;

/**
 * @author Recep Colak
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class KeyValueSpecificationPage extends PatikaWizardPage implements ActionListener
{
	/**
	 * the bioentity creation wizard, that will own this page
	 */
	private ExperimentDataConvertionWizard mdcw;

	/**
	 *
	 */
	private Vector columns;

	/**
	 * GUI components of the page
	 */
	private TitledBorder mainTitledBorder;

	/**
	*  The panel to hold the files tabele and its components
	*/
	private JPanel keyMappingPanel;
	private JLabel keyLabel;
	JComboBox keyComboBox;

	/**
	 *	The buttons panel and its components
	 */
	private JPanel valueColumnPanel;
	private JLabel valueStartLabel;
	private JLabel valueEndLabel;
	JComboBox columnsStartComboBox;
    private JComboBox columnsEndComboBox;
// ---------------------------------------------------------------------
// Section: Constructors and initialization.
// ---------------------------------------------------------------------
	/**
	 * This constructor initializes the load page
	 *
	 * @param mdcw the wizard, that owns this page
	 */
	public KeyValueSpecificationPage(ExperimentDataConvertionWizard mdcw)
	{
		super(mdcw);
		this.mdcw = mdcw;
		columns = new Vector();

		//main panel settings
		this.setSize(new Dimension(489, 260));
		this.setLayout(new FlowLayout());

		mainTitledBorder = new TitledBorder(BorderFactory.createEtchedBorder(
			new Color(52, 52, 52), new Color(25, 25, 25)), " Key - value association ");

		this.setBorder(mainTitledBorder);

	}

	/**
	 * This method configures the GUI components
	 * according to the data taken from previous wizard
	 * pages.
	 */
	public void init()
	{
		this.removeAll();
		// initialize components
		valueColumnPanel = new JPanel();
		valueColumnPanel.setBorder(
				new TitledBorder(BorderFactory.createEtchedBorder
			 (new Color(52, 52, 52),
			 new Color(25, 25, 25))," Experiment value column(s) specification "));

		// configure the labels
		if(this.mdcw.isDataInSingleFile())
		{
			valueStartLabel = new JLabel("Experiment values start at column");
			valueEndLabel   = new JLabel("Experiment values  end  at column ");

			//get the columns of datafiles
			columns= ((SingleDataFileLoadPage)this.mdcw.pages[2]).getColumns();
			if(this.mdcw.hasPlatformFile())
			{
				 //get the columns of datafiles
				columns= ((SingleDataFileLoadPage)this.mdcw.pages[2]).getColumns();
			}
			else
			{
				 //get the columns of datafiles
				columns= ((SingleDataFileLoadPage)this.mdcw.pages[1]).getColumns();
			}
		}
		else
		{
			valueColumnPanel.setBorder(
				new TitledBorder(BorderFactory.createEtchedBorder
			 (new Color(52, 52, 52),
			 new Color(25, 25, 25))," Value column specification "));
			valueStartLabel = new JLabel("Column to use as value ");
			if(this.mdcw.hasPlatformFile())
			{
				//get the columns of datafiles
				columns= ((MultipleDataFileLoadPage)this.mdcw.pages[2]).
					getColumns((File)((MultipleDataFileLoadPage)
						this.mdcw.pages[2]).getLoadedFiles().get(0));

			}
			else
			{
				//get the columns of datafiles
				columns= ((MultipleDataFileLoadPage)this.mdcw.pages[2]).
					getColumns((File)((MultipleDataFileLoadPage)
						this.mdcw.pages[2]).getLoadedFiles().get(0));
			}

		}


		columnsStartComboBox = new JComboBox(columns);
		columnsStartComboBox.addActionListener(this);
		columnsEndComboBox = new JComboBox(columns);
		columnsEndComboBox.addActionListener(this);
		columnsStartComboBox.addActionListener(this);
		recognizeValueColumn();

		if(!this.mdcw.isDataInSingleFile())
		{
			valueColumnPanel.setPreferredSize(new Dimension(470,55));
			valueColumnPanel.setLayout(new GridLayout(1,2,15,15));
			valueColumnPanel.add(valueStartLabel);
			valueColumnPanel.add(columnsStartComboBox);
		}
		else
		{
			valueColumnPanel.setPreferredSize(new Dimension(470,100));
			valueColumnPanel.setLayout(new GridLayout(2,2,15,15));
//			valueColumnPanel.add(new JLabel("	"));
//			valueColumnPanel.add(new JLabel("	"));
			valueColumnPanel.add(valueStartLabel);
			valueColumnPanel.add(columnsStartComboBox);
//			valueColumnPanel.add(new JLabel("	"));
//			valueColumnPanel.add(new JLabel("	"));
			valueColumnPanel.add(valueEndLabel);
			valueColumnPanel.add(columnsEndComboBox);
//			valueColumnPanel.add(new JLabel("	"));
//			valueColumnPanel.add(new JLabel("	"));
		}

		keyMappingPanel = new JPanel();
		keyMappingPanel.setPreferredSize(new Dimension(470,55));
		keyMappingPanel.setBorder(
				new TitledBorder(BorderFactory.createEtchedBorder
			 (new Color(52, 52, 52),
			 new Color(25, 25, 25))," Key Specification "));
		keyLabel = new JLabel("Key in annotation file" +
			" corresponds to");
		keyComboBox = new JComboBox(columns);
		keyComboBox.addActionListener(this);
		keyMappingPanel.setLayout(new GridLayout(1,2));
		keyMappingPanel.add(keyLabel);
		keyMappingPanel.add(keyComboBox);


		//add all to the panel
		//this.setLayout(new GridBagLayout());
		if(!this.mdcw.isDataInSingleFile())
		{
			this.add(new JLabel("                                           "));
			this.add(new JLabel("                                           "));
		}

		this.add(keyMappingPanel);
		this.add(new JLabel("                                           "));
		this.add(new JLabel("                                           "));
		this.add(valueColumnPanel);
	}

	private void recognizeValueColumn()
	{
		for(int i=0;i<columns.size();i++)
		{
			String column = (String) columns.get(i);
			Pattern pattern = Pattern.compile(".*VALUE.*");
		    Matcher matcher = pattern.matcher(column);
		    if(matcher.matches())
		    {
				if(columnsEndComboBox!=null)
				{
					columnsEndComboBox.setSelectedIndex(i);
				}
				columnsStartComboBox.setSelectedIndex(i);
				break;
			}
		}
	}



// ---------------------------------------------------------------------
// Section: Accessors and mutators.
// ---------------------------------------------------------------------
	void reset()
	{
		/**
		*  Set to default options
		*/

	}

	/**
	 * when the "Finish" button of the wizard is clicked,
	 * this method will set update the experiment specification
	 */
	void update()
	{

	}

	public boolean canNext()
	{
		return false;
	}

	public boolean canFinish()
	{
		if(columnsStartComboBox==null ||
			keyComboBox==null)
		{
			return false;
		}
		else if( this.mdcw.isDataInSingleFile() && columnsStartComboBox.getSelectedIndex()
			    > columnsEndComboBox.getSelectedIndex())
		{
			return false;
		}
		else
		{
			if(((String)columnsStartComboBox.getSelectedItem() != null) &&
			   ((String)keyComboBox.getSelectedItem() !=null) &&
			   ((String)columnsStartComboBox.getSelectedItem()).
				equals((String)keyComboBox.getSelectedItem()))
			{
				return false;
			}
			else
			{
				return true;
			}
		}
	}

	public String getKeyInDataFile()
	{
		return (String)this.keyComboBox.getSelectedItem();
	}

	public String getExperimentStartColumn()
	{
		return (String)this.columnsStartComboBox.getSelectedItem();
	}

	public int getIndexOfExperimentStartColumn()
	{
		return this.columns.indexOf(columnsStartComboBox.getSelectedItem());
	}

	public int getIndexOfExperimentEndColumn()
	{
		return this.columns.indexOf(
			this.columnsEndComboBox.getSelectedItem());
	}

	public String getExperimentValueColumn()
	{
		return (String)this.columnsStartComboBox.getSelectedItem();
	}

	public int getIndexOfExperimentValueColumn()
	{
		return this.columns.indexOf(columnsStartComboBox.getSelectedItem());
	}

	public int getIndexOfKeyInDataFile()
	{
		return this.columns.indexOf(keyComboBox.getSelectedItem());
	}

	public Vector getColumns()
	{
		return columns;
	}

	public String[] getExperimentNames()
	{
		String[] experimentNames=null;

		if(this.mdcw.isDataInSingleFile())
		{
			experimentNames = new String[columns.size() -
										 getIndexOfExperimentStartColumn()];
			for(int i=0;i<experimentNames.length;i++)
			{
				experimentNames[i]=(String)columns.
									get(getIndexOfExperimentStartColumn()+i);
			}
		}
		return experimentNames;
	}



// ---------------------------------------------------------------------
// Section: Action Handlers
// ---------------------------------------------------------------------


	public void actionPerformed(ActionEvent e)
	{
		if(this.mdcw.isDataInSingleFile() &&
			(e.getSource() == columnsStartComboBox))
		{
			if(columnsStartComboBox.getSelectedIndex()
				> columnsEndComboBox.getSelectedIndex())
			{
				columnsEndComboBox.setSelectedIndex(
					columnsStartComboBox.getSelectedIndex());
			}
		}
		else if(this.mdcw.isDataInSingleFile() &&
			(e.getSource() == columnsEndComboBox))
		{
			if(columnsStartComboBox.getSelectedIndex()
				> columnsEndComboBox.getSelectedIndex())
			{
				columnsStartComboBox.setSelectedIndex(
					columnsEndComboBox.getSelectedIndex());
			}
		}
		this.mdcw.checkButtons();
	}
}
