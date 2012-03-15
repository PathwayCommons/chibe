package org.patika.mada.gui;

import javax.swing.border.TitledBorder;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.HashMap;

/**
 * @author Recep Colak
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class PlatformMappingPage extends PatikaWizardPage implements ActionListener
{
	/**
	 * the bioentity creation wizard, that will own this page
	 */
	ExperimentDataConvertionWizard mdcw;

	/**
	* The platform file that user specifies
	*/
	private File platformFile;

	/**
	 * Columns read from the user's platforms file
	*/
	private java.util.List<String> columns;

	private JTextField fileNameArea;

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
	public PlatformMappingPage(ExperimentDataConvertionWizard mdcw)
	{
		super(mdcw);
		this.mdcw = mdcw;
		columns =  new ArrayList<String>();

		//main panel settings
		this.setSize(new Dimension(489, 260));
		this.setLayout(new FlowLayout());

		TitledBorder mainTitledBorder = new TitledBorder(BorderFactory.createEtchedBorder
			(new Color(52, 52, 52),
				new Color(25, 25, 25)),
			" Identifier mapping ");
		this.setBorder(mainTitledBorder);

		//channel panel settings
		JPanel platformFilePanel = new JPanel();
		platformFilePanel.setLayout(new FlowLayout());
		platformFilePanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder
			 (new Color(52, 52, 52),
			 new Color(25, 25, 25))," Select the tab-limited reference information file "));
		JLabel platformLabel1 = new JLabel("Reference information file: ");
		fileNameArea = new JTextField("             ");
		fileNameArea.setEditable(false);
		JButton browseButton = new JButton(" Browse ");
		browseButton.addActionListener(this);
		platformFilePanel.setLayout(new GridLayout(1,3,10,10));
		//platformFilePanel.add(new JLabel("              "));
		platformFilePanel.add(platformLabel1);
		//platformFilePanel.add(new JLabel("    "));
		platformFilePanel.add(fileNameArea);
		//platformFilePanel.add(new JLabel("    "));
		platformFilePanel.add(browseButton);
		//platformFilePanel.add(new JLabel("              "));

		// create the mapping panel
		mappingPanel = new JPanel();
		mappingPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder
			 (new Color(52, 52, 52),
			 new Color(25, 25, 25))," Map a column as key to "
			+ "data file(s) and at least one external reference"));
		mappingPanel.setLayout(new BorderLayout());


		//put all the components to the main page
		this.setLayout(new BorderLayout(10,10));
		this.add(platformFilePanel, BorderLayout.NORTH);
		this.add(mappingPanel, BorderLayout.CENTER);

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
           //extractColumn("Key to Data File(s)");
	}

	public boolean canNext()
	{
		return this.mapTable != null && this.mapTable.canNext(this.mdcw.hasPlatformFile());
	}

	public boolean canFinish()
	{
		return true;
	}

// ---------------------------------------------------------------------
// Section: Action Handlers
// ---------------------------------------------------------------------


	public void actionPerformed(ActionEvent e)
	{
		FileReader fr;
		BufferedReader br;
		String line="";
		StringTokenizer tokenizer;
		JFileChooser chooser = new JFileChooser();
        int returnVal;
		String experimentSetInfo="";
		if(ExperimentDataConvertionWizard.recentDir != null)
		{
			chooser.setCurrentDirectory(
				ExperimentDataConvertionWizard.recentDir);
		}
		chooser.setDialogTitle("Open the Reference File");
		returnVal = chooser.showOpenDialog(mdcw);

		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			platformFile = chooser.getSelectedFile();
			ExperimentDataConvertionWizard.recentDir = platformFile.getParentFile();
			columns.clear();

			if(!platformFile.exists())
			{
				JOptionPane.showMessageDialog(this.mdcw,
					platformFile.getName()+" does not exists! ",
					null,
					JOptionPane.ERROR_MESSAGE);
			}
			else if(!isFileValid(platformFile))
			{

				JOptionPane.showMessageDialog(this.mdcw,
					platformFile.getName() + " is not recognized by CBE because \n " +
						"it contains non-ASCII characters." ,
					"Illegal Reference File",
					JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				this.fileNameArea.setText(platformFile.getName());
				try
				{
					fr = new FileReader(platformFile);
					br = new BufferedReader(fr);
					do
					{
						// This is platform file info, not dataset info, so we discard
//						experimentSetInfo += line + "\n";
						line = br.readLine();
					}
					while(line != null &&
						ExperimentDataConvertionWizard.startsWithSpecialChar(line));

					//the ignored lines correspond to the experiment set info
					this.mdcw.setExperimentSetInfo(experimentSetInfo);


					// extract the tableData userColumns names
					if(line != null)
					{
						tokenizer = new StringTokenizer(line,"\t");
						while(tokenizer.hasMoreTokens())
						{
							columns.add(tokenizer.nextToken());
						}
					}

					if(columns.size() < 2)
					{
						JOptionPane.showMessageDialog(this.mdcw,
										    "Could not recognize file format, "
											+ "reference file must contain at "
											+ "least 1 identifier and 1 ex"
											+ "ternal reference columns! " ,
											null,
											JOptionPane.ERROR_MESSAGE);
					}
					else
					{
						//update the mapping panel
						if(mapTable != null)
						{
							mappingPanel.removeAll();
						}

						String[] referenceInCombo = getReferenceArray();

						mapTable = new ReferenceTable(this.mdcw, columns, referenceInCombo,
							mdcw.getPredictedMatches(referenceInCombo, columns));

						mapTable.getModel().addTableModelListener(this.mdcw);
						scrollPane = new JScrollPane(mapTable);
						scrollPane.setPreferredSize(mapTable.getSize());
						mappingPanel.add(scrollPane);
						mappingPanel.validate();
						this.validate();
					}
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}



// ---------------------------------------------------------------------
// Section: Getters&Setters
// ---------------------------------------------------------------------
/**
 * Array for displaying references in graph.
 * @return references array
 */
	private String[] getReferenceArray()
	{
		java.util.List<String> reftypes = mdcw.getSupportedReferencesTypes();
		String[] ref = new String[reftypes.size() + 2];

		ref[0] = "None";
		ref[1]="Key to data file(s)";

		int i=2;
		for (String s : reftypes)
		{
			ref[i++] = s;
		}

		return ref;
	}

	public ReferenceTable getMapTable()
	{
		return mapTable;
	}

	public java.util.List<String> getColumns()
	{
		return columns;
	}

	public File getPlatformFile()
	{
		return platformFile;
	}

	public HashMap<String, String> getMappedReferences()
	{
		return this.mapTable.getMappedReferences();
	}

	public String getNameOfKeyColumn()
	{
		return this.mapTable.getNameOfKeyColumn();
	}

	private boolean isFileValid(File f)
	{
		BufferedReader br;
		try
		{
			br = new BufferedReader(new FileReader(f));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}

		String line;
		do
		{
			try
			{
				line = br.readLine();
				for(int i=0;line!=null && i<line.length();i++)
				{
					if(line.charAt(i)>127)
					{
						throw new IOException();
					}
				}
			}
			catch (IOException e)
			{
				System.out.println("reference file not valid");
				return false;
			}
		}
		while (line != null);
		return true;
	}
}
