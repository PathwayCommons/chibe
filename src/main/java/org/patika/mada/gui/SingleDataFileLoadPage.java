package org.patika.mada.gui;

import javax.swing.border.TitledBorder;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;

/**
 * @author Recep Colak
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class SingleDataFileLoadPage extends PatikaWizardPage implements ActionListener
{
	/**
	 * the bioentity creation wizard, that will own this page
	 */
	ExperimentDataConvertionWizard mdcw;

	/**
	 * Columns read from the user's platforms file
	*/
	File dataFile;

	/**
	 * GUI components of the page
	 */
	private TitledBorder mainTitledBorder;

	/**
	*  The file load panel and its components
	*/
	private JPanel filePanel;
  	private JLabel filePanelLabel;
	private JTextField fileNameArea;
	private JButton browseButton;


// ---------------------------------------------------------------------
// Section: Constructors and initialization.
// ---------------------------------------------------------------------
	/**
	 * This constructor initializes the load page
	 *
	 * @param mdcw the wizard, that owns this page
	 */
	public SingleDataFileLoadPage(
		ExperimentDataConvertionWizard mdcw)
	{
		super(mdcw);
		this.mdcw = mdcw;


		//main panel settings
		this.setSize(new Dimension(489, 260));
		this.setLayout(new FlowLayout());

		mainTitledBorder
			= new TitledBorder(BorderFactory.createEtchedBorder
			 (new Color(52, 52, 52),
			 new Color(25, 25, 25)),
			 " Single data file loading ");
		this.setBorder(mainTitledBorder);

		//channel panel settings
		filePanel = new JPanel();
		filePanel.setPreferredSize(new Dimension(470,55));
		filePanel.setLayout(new GridLayout(1,1));
		filePanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder
			 (new Color(52, 52, 52),
			 new Color(25, 25, 25))," Load a tab-limited data file "));
		filePanelLabel = new JLabel("Loaded data file: ");
		fileNameArea = new JTextField("                         ");
		fileNameArea.setMaximumSize(new Dimension(30,26));
		fileNameArea.setEditable(false);
		browseButton = new JButton(" Browse ");
		browseButton.addActionListener(this);
		filePanel.setLayout(new GridLayout(1,3,5,10));
	//	filePanel.add(new JLabel("              "));
		filePanel.add(filePanelLabel);
		//filePanel.add(new JLabel(" "));
		filePanel.add(fileNameArea);
	//	filePanel.add(new JLabel(" "));
		filePanel.add(browseButton);
	//	filePanel.add(new JLabel("              "));
		this.add(filePanel);

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
		if(dataFile==null)
		{
			return false;
		}
		else if(dataFile.exists())
		{
			if(getColumns().size() == 0)
			{
				JOptionPane.showMessageDialog(this.mdcw,
										    "Could not recognize file format,"
											+ " data must be tab delimitted! " ,
											null,
											JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else
			{
				return true;
			}
		}
		else
		{
			return false;
		}

	}

	public boolean canFinish()
	{
		return this.dataFile != null;
	}


// ---------------------------------------------------------------------
// Section: Action Handlers
// ---------------------------------------------------------------------


	public void actionPerformed(ActionEvent e)
	{

		JFileChooser chooser = new JFileChooser();
		if(ExperimentDataConvertionWizard.recentDir != null)
		{
			chooser.setCurrentDirectory(
				ExperimentDataConvertionWizard.recentDir);
		}
        int returnVal = chooser.showOpenDialog(mdcw);

		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			dataFile = chooser.getSelectedFile();
            ExperimentDataConvertionWizard.recentDir = dataFile.getParentFile();
			if(!dataFile.exists())
			{
				JOptionPane.showMessageDialog(this.mdcw,
										    dataFile.getName()+" does not exists! " ,
											null,
											JOptionPane.ERROR_MESSAGE);
			}
			else if(!isFileValid(dataFile))
			{

				JOptionPane.showMessageDialog(this.mdcw,
										    dataFile.getName()+" is not "
											+ "recognized by ChiBE because "
											+ "\n it contains non-ASCII characters." ,
											"Illegal Data File",
											JOptionPane.ERROR_MESSAGE);
			}
			else
			{
			//	this.fileNameArea.setSize(90,(int)fileNameArea.getSize().getHeight());
				this.fileNameArea.setText(dataFile.getName());

				this.mdcw.checkButtons();

				if(this.mdcw.hasPlatformFile())
				{
					((KeyValueSpecificationPage)this.mdcw.pages[3]).init();
				}
				else
				{
					((NonePlatformMappingPage)this.mdcw.pages[2]).init();
				}
			}
		}
	}

// ---------------------------------------------------------------------
// Section: Accessors
// ---------------------------------------------------------------------

	/**
	 *	This methods parses a data file and
	 *  extracts the column headers
	 */
	public Vector getColumns()
	{
		Vector columns = new Vector();
		FileReader fr;
		BufferedReader br;
		String line="";
		StringTokenizer tokenizer;

		try
		{
			fr = new FileReader(dataFile);
			br = new BufferedReader(fr);
			do
			{
				line = br.readLine();
			} while (line != null &&
				     (ExperimentDataConvertionWizard.
							startsWithSpecialChar(line) == true));

			// extract the tableData userColumns names
			if(line != null)
			{
				tokenizer = new StringTokenizer(line, "\t");
				while (tokenizer.hasMoreTokens())
				{
					columns.add(tokenizer.nextToken());
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return columns;
	}

	public File getLoadedFile()
	{
		return dataFile;
	}

	private boolean isFileValid(File f)
		{
			BufferedReader br;
			FileReader fr;
			String line="";
			try
			{
				fr = new FileReader(f);
				br = new BufferedReader(fr);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
				return false;
			}

			do
			{
				try
				{
					line = br.readLine();

					if (line != null &&
						(line.startsWith("!") || line.startsWith("#") || line.startsWith("^")))
						continue;

					for(int i=0;line!=null && i<line.length();i++)
					{
						if(line.charAt(i)>127)
						{
							System.out.println("line = " + line);
							System.out.println("line.charAt("+i+") = " + ((int) line.charAt(i)) + " -- " + line.charAt(i));
							return false;
						}
					}
				}
				catch (IOException e)
				{
					return false;
				}
			}
			while (line != null);
			return true;
		}
}
