package org.patika.mada.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Recep Colak
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class NonePlatformMappingPage extends PatikaWizardPage implements ActionListener
{
	/**
	 * the bioentity creation wizard, that will own this page
	 */
	ExperimentDataConvertionWizard mdcw;

	/**
	 * Columns read from the user's platforms file
	*/
	java.util.List<String> columns;

	/**
	*  The  value choosing columns  and its components
	*/
	private JPanel valueChoosingPanel;
	private JLabel valuePanelEndLabel;
	private JComboBox columnsStartComboBox;
	private JComboBox columnsEndComboBox;

	/**
	*  The mapping file panel and its components
	*/
	private JPanel mappingPanel;



	/**
	 * Mapping table
	 */
	private ReferenceTable mapTable;

	/**
	 *	This will hold the table
	 */
	JScrollPane scrollPane;

// ---------------------------------------------------------------------
// Section: Constructors and initialization.
// ---------------------------------------------------------------------
	/**
	 * This constructor initializes the load page
	 *
	 * @param mdcw the wizard, that owns this page
	 */
	public NonePlatformMappingPage(ExperimentDataConvertionWizard mdcw)
	{
		super(mdcw);
		this.mdcw = mdcw;
		columns =  new ArrayList<String>();

		//main panel settings
		this.setSize(new Dimension(489, 260));
		this.setLayout(new FlowLayout());

		TitledBorder mainTitledBorder = new TitledBorder(BorderFactory.createEtchedBorder(
			new Color(52, 52, 52), new Color(25, 25, 25)), " Identifier mapping ");

		this.setBorder(mainTitledBorder);

		//channel panel settings
		valueChoosingPanel = new JPanel();
		valueChoosingPanel.setLayout(new FlowLayout());
		valueChoosingPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(
			new Color(52, 52, 52), new Color(25, 25, 25))," Value column specification "));


		// create the mapping panel
		mappingPanel = new JPanel();
		mappingPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(
			new Color(52, 52, 52), new Color(25, 25, 25)), " Mapping table "));
		mappingPanel.setLayout(new BorderLayout());

		//put all the components to the main page
		this.setLayout(new BorderLayout(10,10));
		this.add(valueChoosingPanel, BorderLayout.NORTH);
		this.add(mappingPanel, BorderLayout.CENTER);
	}

	/**
	 * This method configures the GUI components
	 * according to the data taken from previous wizard
	 * pages.
	 */
	public void init()
	{
		JLabel valuePanelStartLabel;
		if(this.mdcw.isDataInSingleFile())
		{
			columns = new ArrayList<String>(((SingleDataFileLoadPage)this.mdcw.pages[1]).getColumns());
			valuePanelStartLabel = new JLabel("Experiment values start at column ");
			valuePanelEndLabel =   new JLabel("Experiment values  end  at column ");
			columnsEndComboBox = new JComboBox(new Vector<String>(columns));
			columnsEndComboBox.addActionListener(this);
		}
		else
		{
			columns = ((MultipleDataFileLoadPage)this.mdcw.pages[1]).getColumnList();
			valuePanelStartLabel = new JLabel("Column to use as experiment value ");
		}

		String[] referenceInCombo = getReferenceArray();

		mapTable = new ReferenceTable(mdcw, columns, referenceInCombo,
			mdcw.getPredictedMatches(columns));

		mapTable.getModel().addTableModelListener(this.mdcw);
		scrollPane = new JScrollPane(mapTable);
		mappingPanel.removeAll();
		mappingPanel.add(scrollPane);
		columnsStartComboBox = new JComboBox(new Vector<String>(columns));
		columnsStartComboBox.addActionListener(this);
		recognizeValueColumn();
		valueChoosingPanel.removeAll();
		if(!this.mdcw.isDataInSingleFile())
		{
			valueChoosingPanel.setPreferredSize(new Dimension(470,55));
			valueChoosingPanel.setLayout(new BorderLayout(10,30));
			valueChoosingPanel.add(valuePanelStartLabel,BorderLayout.WEST);
			valueChoosingPanel.add(columnsStartComboBox,BorderLayout.EAST);
		}

		if(this.mdcw.isDataInSingleFile())
		{
			valueChoosingPanel.setPreferredSize(new Dimension(470,85));
			valueChoosingPanel.setLayout(new GridLayout(2,2,5,5));
			valueChoosingPanel.add(valuePanelStartLabel);
			valueChoosingPanel.add(columnsStartComboBox);
			valueChoosingPanel.add(valuePanelEndLabel);
			valueChoosingPanel.add(columnsEndComboBox);
		}
		this.validate();
	}

	private void recognizeValueColumn()
	{
		for(int i=0;i<columns.size();i++)
		{
			String column = columns.get(i);
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
		if(this.mapTable == null)
		{
			return false;
		}
		else if(this.mdcw.isDataInSingleFile() &&
			columnsStartComboBox.getSelectedIndex() > columnsEndComboBox.getSelectedIndex())
		{
			return false;
		}
		else
		{
			return this.mapTable.canNext(this.mdcw.hasPlatformFile());
		}
	}

	public ReferenceTable getMapTable()
	{
		return mapTable;
	}

	public HashMap<String, String> getMappedReferences()
	{
		return mapTable.getMappedReferences();
	}

	public void setMapTable(ReferenceTable mapTable)
	{
		this.mapTable = mapTable;
	}

	public File getFileToParse()
	{
		if(this.mdcw.isDataInSingleFile())
		{
			return (((SingleDataFileLoadPage)this.mdcw.pages[1]).getLoadedFile());
		}
		else
		{
			 return (File)(((MultipleDataFileLoadPage)this.mdcw.pages[1]).getLoadedFiles().get(0));
		}
	}

	public String getExperimentValueColumn()
	{
		return (String)this.columnsStartComboBox.getSelectedItem();
	}

	public int getIndexOfExperimentValueStartColumn()
	{
		return this.columns.indexOf(this.columnsStartComboBox.getSelectedItem().toString());
	}

	public int getIndexOfExperimentEndColumn()
	{
		return this.columns.indexOf(this.columnsEndComboBox.getSelectedItem().toString());
	}

	public java.util.List<String> getColumns()
	{
		return columns;
	}

	public String getNameOfKeyColumn()
	{
		return this.mapTable.getNameOfKeyColumn();
	}

	public int getIndexOfKey()
	{
		return this.columns.indexOf(getNameOfKeyColumn());
	}

	public String[] getExperimentNames()
	{
		String[] experimentNames=null;

		if(this.mdcw.isDataInSingleFile())
		{
			experimentNames = new String[columns.size() - getIndexOfExperimentValueStartColumn()];
			for(int i=0;i<experimentNames.length;i++)
			{
				experimentNames[i]= columns.get(getIndexOfExperimentValueStartColumn()+i);
			}
		}
		return experimentNames;
	}

// ---------------------------------------------------------------------
// Section: Action Handlers
// ---------------------------------------------------------------------


	public void actionPerformed(ActionEvent e)
	{
	    if(this.mdcw.isDataInSingleFile())
		{
			if(e.getSource() == columnsStartComboBox)
			{
				if(columnsStartComboBox.getSelectedIndex()
					> columnsEndComboBox.getSelectedIndex())
				{
					columnsEndComboBox.setSelectedIndex(
						columnsStartComboBox.getSelectedIndex());

				}
			}
			else if(e.getSource() == columnsEndComboBox)
			{
				if(columnsStartComboBox.getSelectedIndex()
					> columnsEndComboBox.getSelectedIndex())
				{
					columnsStartComboBox.setSelectedIndex(
						columnsEndComboBox.getSelectedIndex());
				}
			}
		}
	}

	/**
	 *
	 * @return supported reference in array
	 */
	private String[] getReferenceArray()
	{
        Collection<String> reftypes = ExperimentDataConvertionWizard.getKnownReferenceSetsMap().values();
		String[] ref = new String[reftypes.size() + 2];

		ref[0] = "None";
		ref[1] = "Key";

		int i = 2;
		for (String s : reftypes)
		{
			ref[i++] = s;
		}

		return ref;
	}
}
