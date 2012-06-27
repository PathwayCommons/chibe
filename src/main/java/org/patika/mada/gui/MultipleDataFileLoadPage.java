package org.patika.mada.gui;

import javax.swing.border.TitledBorder;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.io.*;

/**
 * @author Recep Colak
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class MultipleDataFileLoadPage extends PatikaWizardPage implements ActionListener
{
	/**
	 * the bioentity creation wizard, that will own this page
	 */
	ExperimentDataConvertionWizard mdcw;


	/**
	 *	Loaded files
	 */
	private ArrayList loadedFiles;

	/**
	 * GUI components of the page
	 */
	private TitledBorder mainTitledBorder;

	/**
	*  The panel to hold the files tabele and its components
	*/
	private JPanel filePanel;
	private FileTable fileTable;
	JScrollPane scrollPane;

	/**
	 *	The buttons panel and its components
	 */
	private JPanel buttonsPanel;


	private JButton addButton;
	private JButton removeButton;

// ---------------------------------------------------------------------
// Section: Constructors and initialization.
// ---------------------------------------------------------------------
	/**
	 * This constructor initializes the load page
	 *
	 * @param mdcw the wizard, that owns this page
	 */
	public MultipleDataFileLoadPage(
					ExperimentDataConvertionWizard mdcw)
	{
		super(mdcw);
		this.mdcw = mdcw;
		loadedFiles = new ArrayList();


		//main panel settings
		this.setSize(new Dimension(489, 260));
		this.setLayout(new BorderLayout(10,10));

		mainTitledBorder
			= new TitledBorder(BorderFactory.createEtchedBorder
			 (new Color(52, 52, 52),
			 new Color(25, 25, 25)),
			 " Multiple data file loading ");
		this.setBorder(mainTitledBorder);

		// initialize the file panel
		filePanel = new JPanel();
		filePanel.setLayout(new BorderLayout());
		filePanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder
			 (new Color(52, 52, 52),
			 new Color(25, 25, 25))," Loaded files "));

		// initialize the buttons panel
		buttonsPanel = new JPanel();
		addButton = new JButton("Add");
		addButton.addActionListener(this);
		removeButton = new JButton("Remove Selected(s)");
		removeButton.setEnabled(false);
		removeButton.addActionListener(this);
		buttonsPanel.add(addButton);
		buttonsPanel.add(removeButton);

		//add all to the panel
		this.setLayout(new BorderLayout(10,10));
		this.add(buttonsPanel,BorderLayout.NORTH);
		this.add(filePanel,BorderLayout.CENTER);


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
		if(this.fileTable==null)
		{
			return false;
		}
		else if(this.fileTable.getRowCount()>=1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean canFinish()
	{
		return true;
	}

	public ArrayList getLoadedFiles()
	{
			return loadedFiles;
	}

	public FileTable getFileTable()
	{
			return fileTable;
	}


	/**
	 *	This methods parses a data file and
	 *  extracts the column headers
	 */
	public Vector<String> getColumns(File dataFile)
	{
		Vector<String> columns = new Vector<String>();

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(dataFile));

			String line;

			do
			{
				line = br.readLine();
			}
			while (line != null && (ExperimentDataConvertionWizard.startsWithSpecialChar(line)));

			// extract the tableData userColumns names
			StringTokenizer tokenizer = new StringTokenizer(line, "\t");
			while (tokenizer.hasMoreTokens())
			{
				columns.add(tokenizer.nextToken());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return columns;
	}

	/**
	 *	This methods parses a data file and
	 *  extracts the column headers
	 */
	public ArrayList<String> getColumnList()
	{
		return  new ArrayList<String>(getColumns((File)loadedFiles.get(0)));
	}

	private boolean validateFiles()
	{
		boolean areValid = true;

		ArrayList columnOfFiles = new ArrayList();
		for(int i=0;i<loadedFiles.size();i++)
		{
			columnOfFiles.add(getColumns((File)loadedFiles.get(i)));
		}

		// validate each column list
		for(int i=0;(i<getLoadedFiles().size()-1) && areValid;i++)
		{
			areValid &= ((Vector)columnOfFiles.get(i)).size() ==
					  ((Vector)columnOfFiles.get(i+1)).size();
		}

		return areValid;
	}

	private boolean isFileAdded(File newFile)
	{
		boolean result = false;
		File f;
		Iterator addedFileIter = loadedFiles.iterator();
		while (addedFileIter.hasNext())
		{
			f = ((File)addedFileIter.next());
			if(f.getAbsolutePath().equals(newFile.getAbsolutePath()))
			{
				result=true;
				break;
			}

		}
      	return result;
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
				return false;
			}
		}
		while (line != null);
		return true;
	}


// ---------------------------------------------------------------------
// Section: Action Handlers
// ---------------------------------------------------------------------


	public void actionPerformed(ActionEvent e)
	{
		File[] files;
		int returnVal=JFileChooser.CANCEL_OPTION;
		ArrayList alreadyAddedFiles = new ArrayList();
		ArrayList invalidFiles = new ArrayList();
		ArrayList filesTobeAdded = new ArrayList();

		if(e.getSource().equals(addButton))
		{
			JFileChooser chooser = new JFileChooser();

			if(ExperimentDataConvertionWizard.recentDir != null)
			{
				chooser.setCurrentDirectory(
					ExperimentDataConvertionWizard.recentDir);
			}
			chooser.setMultiSelectionEnabled(true);
			returnVal = chooser.showOpenDialog(mdcw);
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				files = chooser.getSelectedFiles();
				ExperimentDataConvertionWizard.recentDir = files[0].getParentFile();

				for(int i=0;i<files.length;i++)
				{
					if(!isFileAdded(files[i]))
					{
						if(isFileValid(files[i]))
						{
							this.loadedFiles.add(files[i]);
							filesTobeAdded.add(files[i]);
						}
						else
						{
							invalidFiles.add(files[i]);
						}
					}
					else
					{
						alreadyAddedFiles.add(files[i]);
					}
				}

				if(!validateFiles())
				{
					JOptionPane.showMessageDialog(this.mdcw,
												"Loaded files are invalid, they must"
												+ " contain the same number of colum"
												+ "n(s)! " ,
												"Invalid Selection",
												JOptionPane.ERROR_MESSAGE);
					for(int i=0;i<files.length;i++)
					{
						this.loadedFiles.remove(files[i]);
					}
				}
				else
				{

					//inform the user about the invalid files
					String massage = "The below file(s) contain non-ASCII "
									+ "characters and are not loaded: \n";
					Iterator invalidFileIter = invalidFiles.iterator();
					while (invalidFileIter.hasNext())
					{
						massage+=((File)invalidFileIter.next()).getName()+"\n";

					}

					if(invalidFiles.size() !=0)
					{
					 	JOptionPane.showMessageDialog(this.mdcw,
												massage,
												null,
												JOptionPane.ERROR_MESSAGE);
					}

					//inform the user about the already added files
					massage = "The below file(s) are already loaded: \n";
					Iterator alreadyAddedIter = alreadyAddedFiles.iterator();
					while (alreadyAddedIter.hasNext())
					{
						massage+=((File)alreadyAddedIter.next()).getName()+"\n";

					}

					if(alreadyAddedFiles.size() !=0)
					{
					 	JOptionPane.showMessageDialog(this.mdcw,
												massage,
												null,
												JOptionPane.ERROR_MESSAGE);
					}



					// update the files table
					if(filesTobeAdded.size()>0)
					{
						if(fileTable == null)
						{
							fileTable = new FileTable((File) filesTobeAdded.get(0));
							scrollPane = new JScrollPane(fileTable);

							//index starts from 1 because we already
							// added the first file
							for(int i=1;i<filesTobeAdded.size();i++)
							{
								fileTable.addRow((File) filesTobeAdded.get(i));
							}

							this.filePanel.add(scrollPane);
							this.filePanel.validate();
							this.validate();
						}
						else
						{
							for(int i=0;i<filesTobeAdded.size();i++)
							{
								fileTable.addRow((File) filesTobeAdded.get(i));
							}

							this.filePanel.validate();
							this.validate();
						}
					}
					this.removeButton.setEnabled(true);
				}
			}
		}
		else
		{
               	int i=0;
			    while(i< this.fileTable.getRowCount())
				{
					if(((Boolean)this.fileTable.getValueAt(i,2)).booleanValue() == true)
					{
						this.fileTable.removeRow(i);
						loadedFiles.remove(i);
						i=0;
					}
					else
					{
						i++;
					}
				}

			    this.fileTable.revalidate();
				this.revalidate();

				for(i=0;i< this.fileTable.getRowCount();i++)
				{
					this.fileTable.setValueAt(new Integer(i+1),i,0);
				}

				if(this.fileTable.getRowCount()==0)
				{
					this.removeButton.setEnabled(false);
					this.repaint();
				}
		}


		if(this.fileTable != null)
		{
			if((returnVal == JFileChooser.APPROVE_OPTION) &&
				this.fileTable.getRowCount() >= 1)
			{
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

		this.mdcw.checkButtons();
	}
}
