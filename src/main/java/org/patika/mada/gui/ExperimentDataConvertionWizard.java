package org.patika.mada.gui;

import org.patika.mada.dataXML.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * @author Recep Colak
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ExperimentDataConvertionWizard extends PatikaWizard implements TableModelListener
{
	/**
	 * Is data in one or multiple file(s)
	 */
	private boolean isDataInSingleFile;

	/**
	 * Does experiment set have a platform file
	 */
	private boolean hasPlatformFile;

	/**
	 * Type of the data to load.
	 */
	protected String dataType;

	/**
	 * The microarray data object that will be configured
	 * during the load process.
	 */
	protected ChisioExperimentData rmd;

	/**
	 * Factory to create XML objects
	 */
	ObjectFactory objFac;

	/**
	 * Information about the set of experiments, provided that,
	 * they have a platform(annotation) file
	 */
	String experimentSetInfo;

	/**
	 * Information about each sample in the dataset.
	 */
	String[] sampleInfo;

	/**
	 * Must given to constructor as parameter. The reference types in the graph to be used.
	 */
	List<String> supportedReferenceTypes;

	/**
	 * Last used directory for remembering
	 */
	public static File recentDir = new File("experiments");

	public static int LINE_COUNTER = 0;

	/**
	 * This will hold the name of the file that user successfully writes the
	 * microarray data. This is stored in case PatikaActions will ask this name
	 * and load the data.
	 */
	protected String resultFileName;

	private Map<String, Row> index2Row;

	/**
	 * Constuctor to initialize without GUI 
	 * @param supportedReferenceTypes
	 */
	public ExperimentDataConvertionWizard(List<String> supportedReferenceTypes)
	{
		this.supportedReferenceTypes = supportedReferenceTypes;

		objFac = new ObjectFactory();
	}

	/**
	 * This constructor initializes the bioentity creation wizard.
	 * @param supportedReferenceTypes reference types found in the graph
	 */
	public ExperimentDataConvertionWizard(List<String> supportedReferenceTypes, Point p)
	{
		super("Experiment Data Converter Wizard", null);
		this.supportedReferenceTypes = supportedReferenceTypes;
		this.setLocation(p);
		this.setResizable(false);
		objFac = new ObjectFactory();
	}

	protected void initPages(Object o)
	{
		this.pageCount = 2;

		this.pages = new PatikaWizardPage[this.pageCount];

		pages[0] = new ExperimentInfoCollectionPage(this);
		pages[1] = new SingleDataFileLoadPage(this);
		currentPage = 0;

		this.switchPage(0, false);
	}

	protected void goToStart()
	{
		this.switchPage(0, false);
		this.setEnabled(true);
	}

	/**
	 * This method sets the location and attributes of the GUI
	 * components.
	 */
	protected void initializeGUI()
	{
		this.setSize(500, 330);
		this.setLayout(mainLayout);
		controllerPanel.setAlignmentY((float) 0.5);
		controllerPanel.setBorder(BorderFactory.createEtchedBorder());
		backButton.setText("< Back");
		nextButton.setText("Next >");
		cancelButton.setText("Close");
		finishButton.setText("Finish");
		controllerPanel.add(cancelButton, BorderLayout.SOUTH);
		controllerPanel.add(backButton, null);
		controllerPanel.add(nextButton, null);
		controllerPanel.add(finishButton, null);
		this.add(controllerPanel, BorderLayout.SOUTH);
	}

	/**
	 * This method sets the pages according to the
	 * experiment specification
	 */
	protected void setPages()
	{
		//experiment has a platform info file
		if (this.hasPlatformFile())
		{
			// tableData files are in single file
			if (this.isDataInSingleFile())
			{
				this.pageCount = 4;
				PatikaWizardPage[] temp = new PatikaWizardPage[pageCount];
				temp[0] = this.pages[0];
				this.pages = temp;
				this.pages[1] = new PlatformMappingPage(this);
				this.pages[2] = new SingleDataFileLoadPage(this);
				this.pages[3] = new KeyValueSpecificationPage(this);
			} else
			{
				this.pageCount = 4;
				PatikaWizardPage[] temp = new PatikaWizardPage[pageCount];
				temp[0] = this.pages[0];
				this.pages = temp;
				this.pages[1] = new PlatformMappingPage(this);
				this.pages[2] = new MultipleDataFileLoadPage(this);
				this.pages[3] = new KeyValueSpecificationPage(this);
			}
		} else
		{
			// tableData files are in single file
			if (this.isDataInSingleFile())
			{
				this.pageCount = 3;
				PatikaWizardPage[] temp = new PatikaWizardPage[pageCount];
				temp[0] = this.pages[0];
				this.pages = temp;
				this.pages[1] = new SingleDataFileLoadPage(this);
				this.pages[2] = new NonePlatformMappingPage(this);

			} else
			{
				this.pageCount = 3;
				PatikaWizardPage[] temp = new PatikaWizardPage[pageCount];
				temp[0] = this.pages[0];
				this.pages = temp;
				this.pages[1] = new MultipleDataFileLoadPage(this);

				//this is a dummy page, just for next button to be enabled
				this.pages[2] = new NonePlatformMappingPage(this);
			}
		}
		this.repaint();
	}

// ---------------------------------------------------------------------
// Section: Accessors and Mutators.
// ---------------------------------------------------------------------

	public boolean isDataInSingleFile()
	{
		return isDataInSingleFile;
	}

	public void setDataInSingleFile(boolean dataInSingleFile)
	{
		isDataInSingleFile = dataInSingleFile;
	}

	public boolean hasPlatformFile()
	{
		return hasPlatformFile;
	}

	public void setHasPlatformFile(boolean hasPlatformFile)
	{
		this.hasPlatformFile = hasPlatformFile;
	}

	public String getDataType()
	{
		return dataType;
	}

	public void setDataType(String dataType)
	{
		this.dataType = dataType;
	}

	public String getExperimentSetInfo()
	{
		return experimentSetInfo;
	}

	public void setExperimentSetInfo(String experimentSetInfo)
	{
		this.experimentSetInfo = experimentSetInfo;
	}

	public String[] getSampleInfo()
	{
		return sampleInfo;
	}

	public void setSampleInfo(String[] sampleInfo)
	{
		this.sampleInfo = sampleInfo;
	}

	public List<String> getSupportedReferencesTypes()
	{
		return this.supportedReferenceTypes;
	}

	/**
	 * Gets the name of the result file, i.e. the .pmad file written after
	 * conversion. This is null if no file is written.
	 * @return path of .ced file that is the result of conversion
	 */
	public String getResultFileName()
	{
		return resultFileName;
	}

// ---------------------------------------------------------------------
// Section: Event handlers
// ---------------------------------------------------------------------

	protected void nextButton_actionPerformed(ActionEvent event)
	{
		if (this.currentPage == 0)
		{
			this.pages[0].update();
			this.setPages();
		}
		this.switchPage(this.currentPage + 1, false);
	}

	public void tableChanged(TableModelEvent e)
	{
		if (e.getSource() instanceof ReferenceTable.ReferenceTableModel)
		{
			ReferenceTable rt;

			if (this.hasPlatformFile())
			{
				rt = ((PlatformMappingPage) this.pages[1]).getMapTable();

				if (rt.canNext(true))
				{
					this.nextButton.setEnabled(true);
				}
				else
				{
					this.nextButton.setEnabled(false);
				}
			}
			else
			{
				if (this.pages[2].canFinish())
				{
					this.finishButton.setEnabled(true);
				}
				else
				{
					this.finishButton.setEnabled(false);
				}
			}
		}
	}

	/**
	 *
	 */
	public void finish()
	{
		this.setEnabled(false);

		// create the reference xml document so as to send to the server
		try
		{
			rmd = objFac.createRootExperimentData();

			// fill in the references
			if (this.hasPlatformFile())
			{
				PlatformMappingPage pmp = (PlatformMappingPage) pages[1];

				// STEP_1 : create the reference tuples
				fillInReferences(pmp.getPlatformFile(),
					pmp.getMappedReferences(), pmp.getColumns().size());
			}
			else
			{
				NonePlatformMappingPage npmp = (NonePlatformMappingPage) pages[2];

				// STEP_1 : create the reference tuples
				fillInReferences(npmp.getFileToParse(),
					npmp.getMappedReferences(), npmp.getColumns().size());
			}

			//STEP_2 : send them to the server so as to be mapped
			fillInDataValues();

			clearUselessRows();

			// STEP_6: write Experiment data to XML
			writeExperimentData(rmd);

			this.dispose();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null,
				"There occured an error during file "
					+ "parsing, \n please check your files and parameters!  ",
				"Parsing Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			goToStart();
		}

	}

// ---------------------------------------------------------------------
// Section:  Utility functions
// ---------------------------------------------------------------------

	/**
	 *
	 */
	public void fillInDataValues()
	{
		try
		{
			rmd.setExperimentType(this.getDataType());
			rmd.setExperimentSetInfo(this.experimentSetInfo);

			// fill in the references
			if (this.hasPlatformFile())
			{
				KeyValueSpecificationPage kvsp = (KeyValueSpecificationPage) pages[3];

				if (this.isDataInSingleFile())
				{
					SingleDataFileLoadPage sdflp = (SingleDataFileLoadPage) pages[2];
					createExperimentsFromSingleDataFile(sdflp.getLoadedFile(),
						kvsp.getIndexOfKeyInDataFile(),
						kvsp.getIndexOfExperimentStartColumn(),
						kvsp.getIndexOfExperimentEndColumn());
				}
				else
				{
					MultipleDataFileLoadPage mdflp = (MultipleDataFileLoadPage) pages[2];

					createExperimentsFromMultipleFiles(
						mdflp.getLoadedFiles(),
						kvsp.getIndexOfKeyInDataFile(),
						kvsp.getIndexOfExperimentValueColumn());
				}
			}
			else
			{
				NonePlatformMappingPage npmp = (NonePlatformMappingPage) pages[2];

				if (this.isDataInSingleFile())
				{
					SingleDataFileLoadPage sdflp = (SingleDataFileLoadPage) pages[1];

					createExperimentsFromSingleDataFile(sdflp.getLoadedFile(),
						npmp.getIndexOfKey(),
						npmp.getIndexOfExperimentValueStartColumn(),
						npmp.getIndexOfExperimentEndColumn());
				}
				else
				{
					MultipleDataFileLoadPage mdflp = (MultipleDataFileLoadPage) pages[1];

					createExperimentsFromMultipleFiles(
						mdflp.getLoadedFiles(),
						npmp.getIndexOfKey(),
						npmp.getIndexOfExperimentValueStartColumn());
				}
			}

			// STEP_5: average the duplicates
			// deleted.. it was not averaging but removing duplicates

		}
		catch (Exception e)
		{
			System.out.println("Error during patika micro file creation");
			goToStart();
		}

		this.completed = true;
	}

// ---------------------------------------------------------------------
// Section:    Identifier mapping functions
// ---------------------------------------------------------------------

	/**
	 * This method fills in a hashmap whose key values are
	 * identifier column of platform file and whose
	 * values are actual experiment values. This method
	 * is designed for experiment sets that include all experiment
	 * values in single data file.
	 */
	public void createExperimentsFromSingleDataFile(File dataFile,
		int indexOfKeyColumn,
		int indexOfExperimentStartColumn,
		int indexOfExperimentEndColumn)
	{
		try
		{
			int experimetnCount = indexOfExperimentEndColumn - indexOfExperimentStartColumn + 1;

			// STEP-1 create an experiment for each experiment column
			for (int i = 0; i < experimetnCount; i++)
			{
				rmd.getExperiment().add(objFac.createExperiment());
			}

			// STEP-2: parse file
			BufferedReader br = new BufferedReader(new FileReader(dataFile));

			String line = "";
			do
			{
				line = br.readLine();
			}
			while (line != null && startsWithSpecialChar(line));

			// get the column index and ignore the column header line
			if (line != null)
			{
				// get columns
				String[] tokens = getTokenArray(line);

				// set the experiment names from the column headers
				for (int i = 0; i < experimetnCount; i++)
				{
					Experiment exp = ((Experiment) (rmd.getExperiment().get(i)));
					exp.setExperimentName(tokens[i + indexOfExperimentStartColumn]);

					if (sampleInfo == null || sampleInfo[i]==null || sampleInfo[i].isEmpty())
						exp.setExperimentInfo("No experiment info available. " +
							"Please check dataset info.");
					else
						exp.setExperimentInfo(sampleInfo[i]);

					exp.setNo(i);
				}

				// ignore any other special lines between
				// the column header line and data values
				do
				{
					line = br.readLine();
				}
				while (line != null && startsWithSpecialChar(line));

				// now we are at the start of lines of data values,get values
				if (line != null)
				{
					do
					{
						if (!startsWithSpecialChar(line))
						{
							tokens = getTokenArray(line);
							try
							{
								String keyValue = tokens[indexOfKeyColumn];

								// to get rid of " " surrounding the keyValue
								// may occur in GSE files if keyValue is a string
								if(keyValue.startsWith("\""))
								{
									keyValue = keyValue.substring(1,keyValue.length()-1);
								}

								if (keyValue != null && index2Row.containsKey(keyValue))
								{
									Row row = index2Row.get(keyValue);

									assert row != null;

									for (int i = 0; i < experimetnCount; i++)
									{
										String experimentValue =
											tokens[i + indexOfExperimentStartColumn];

										if (experimentValue != null &&
											!experimentValue.equals("null"))
										{
											try
											{
												double expValue = Double.parseDouble(experimentValue);
												ValueTuple tuple = objFac.createValueTuple();
												tuple.setNo(i);
												tuple.setValue(expValue);
												row.getValue().add(tuple);
											}
											catch (NumberFormatException e)
											{
												continue;
											}
										}
									}
								}
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						line = br.readLine();
					}
					while (line != null);
				}
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * This method fills in a hashmap whose key values are
	 * identifier column of platform file and whose
	 * values are actual experiment values. This method
	 * is designed for experiment sets that include only one experiment
	 * values in a data file.
	 */
	public void createExperimentsFromMultipleFiles(
		ArrayList dataFiles,
		int indexOfKeyColumn,
		int indexOfValueColum)
	{
		int fileno = 0;
		for (Object dataFile : dataFiles)
		{
			int not_mapped_key_in_data = 0;
			int non_numerical_vallue = 0;
			int null_key = 0;
			int null_experiment_value = 0;
			int added = 0;
			int lineCounter = 0;

			try
			{
				File currentFile = (File) dataFile;
				BufferedReader br = new BufferedReader(new FileReader(currentFile));

				//create the experiment corresponding to this file
				Experiment exp = objFac.createExperiment();
				rmd.getExperiment().add(exp);
				exp.setExperimentName(currentFile.getName());
				exp.setNo(fileno);

				String line = "";
				String expInfo = "";
				do
				{
					expInfo += line + "\n";
					line = br.readLine();
					lineCounter++;
				}
				while (line != null && startsWithSpecialChar(line));

				exp.setExperimentInfo(expInfo);

				// get the column fileIndex and ignore the column header line
				if (line != null)
				{
					// ignore any other special lines between
					// the column header line and data values
					do
					{
						lineCounter++;
						line = br.readLine();
					} while (line != null && startsWithSpecialChar(line));

					// now we are at the start of lines of data values,get values
					if (line != null)
					{
						do
						{
							if (!startsWithSpecialChar(line))
							{
								try
								{
									String[] tokens = getTokenArray(line);
									String keyValue = tokens[indexOfKeyColumn];
									String experimentValue = tokens[indexOfValueColum];

									if (experimentValue != null && keyValue != null
										&& index2Row.containsKey(keyValue))
									{
										ValueTuple tuple = objFac.createValueTuple();

										double expValue = Double.parseDouble(experimentValue);
										tuple.setNo(fileno);
										tuple.setValue(expValue);

										// If a number format exception occurs, then
										// the row won't be created.

										Row row = index2Row.get(keyValue);

										row.getValue().add(tuple);

										added++;
									} else
									{
										if (experimentValue == null)
										{
											null_experiment_value++;
										} else if (keyValue == null)
										{
											null_key++;
										} else
										{
											not_mapped_key_in_data++;
										}
									}
								}
								catch (ArrayIndexOutOfBoundsException e)
								{
									// do not add this tuple
								}
								catch (NumberFormatException e)
								{
									non_numerical_vallue++;
								}
							}
							line = br.readLine();
							lineCounter++;
						}
						while (line != null);
					}
					br.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

//			System.out.println("Total read lines = " + lineCounter);
//			System.out.println("mapped and added =" + added);
//			System.out.println("null_key=" + null_key);
//			System.out.println("null_experiment_valeu=" + null_experiment_value);
//			System.out.println("not_included_in_key_pid=" + not_mapped_key_in_data);
//			System.out.println("non_numerical=" + non_numerical_vallue);

			fileno++;
		}
	}


	/**
	 * This method fills in a <code>MicroarrayReference</code> object
	 * with reference tuples extracted from the platform file.
	 *
	 * @param file		 , file to be parsed
	 * @param referenceMap , user's reference mapping
	 * @param numberOfColumns number of columns in the data file
	 */
	public void fillInReferences(File file,
		HashMap<String, String> referenceMap,
		int numberOfColumns)
	{
		String line = "";
		int index = 0;
		LINE_COUNTER = 0;

		this.index2Row = new HashMap<String, Row>();

		//structure: patikaereferencetype-mappedReference-indexInfile
		Object[][] referenceIndexes = new Object[referenceMap.size()][3];

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			do
			{
				line = br.readLine();
				LINE_COUNTER++;
			}
			while (line != null && startsWithSpecialChar(line));

			// get the column index and ignore the column header line
			if (line != null)
			{
				// find out the indexes of the mapped references in the
				// platform file
				for (String patikaReference : referenceMap.keySet())
				{
					referenceIndexes[index][0] = patikaReference;
					referenceIndexes[index][1] = referenceMap.get(patikaReference);
					referenceIndexes[index][2] =
						getColumnIndex(line, (String) referenceIndexes[index][1]);
					index++;
				}

				// ignore any other special lines between
				// the column header line and data values
				do
				{
					line = br.readLine();
					LINE_COUNTER++;
				}
				while (line != null && startsWithSpecialChar(line));

				// now we are at the start of lines of data values,get values
				if (line != null)
				{
					do
					{
						if (!startsWithSpecialChar(line))
						{
							Row row = getRow(referenceIndexes, line, numberOfColumns);
							if (row != null)
							{
								rmd.getRow().add(row);
							}
						}

						line = br.readLine();
						LINE_COUNTER++;
					}
					while (line != null);
				}
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * This methods creates a Row which contains references of the row in the data.
	 *
	 * @param referenceIndexes , patika_reference - mapped_reference - index_of_mapped_reference
	 */
	private Row getRow(Object[][] referenceIndexes, String dataLine, int numberOfExpectedColumns)
	{
		Row row = null;
		String[] tokens = getTokenArray(dataLine);

		if (numberOfExpectedColumns != tokens.length)
		{
			return row;
		}

		try
		{
			row = objFac.createRow();

			for (Object[] referenceIndex : referenceIndexes)
			{
				String referenceValue = tokens[(Integer) referenceIndex[2]];

				if (referenceValue != null)
				{
					StringTokenizer tokenizer = new StringTokenizer(referenceValue, "/");

					while (tokenizer.hasMoreTokens())
					{
						String token = tokenizer.nextToken().trim();

						if (token.length() > 0)
						{
							if (referenceIndex[0].equals(KEY_COLUMN))
							{
								this.index2Row.put(token, row);
							}
							else
							{
								Reference ref = objFac.createReference();
								ref.setDb((String) referenceIndex[0]);
								ref.setValue(token);
								row.getRef().add(ref);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return row;
	}

	/**
	 * Clears rows that have no xref or have no experiment data associated.
	 */
	protected void clearUselessRows()
	{
		for (Row row : new ArrayList<Row>(rmd.getRow()))
		{
			if (row.getRef().isEmpty() || row.getValue().isEmpty())
			{
				rmd.getRow().remove(row);
			}
		}
	}

	/**
	 * This method marshalls a mapped Experiment data object
	 * into an XML file.
	 *
	 * @param pmd , tuples of ref->value
	 */
	public void writeExperimentData(ChisioExperimentData pmd)
	{
		try
		{
			File fileToWrite;
			JFileChooser chooser = new JFileChooser();
			int returnValue;
			int returnValue2;
			boolean isOverWrite = false;
			boolean willWrite = true;

			// In the provious implementation, empty experiments were being
			// checked and user was being warned. Now it is a bit more complex
			// to check this.

			chooser.setFileFilter(new javax.swing.filechooser.FileFilter()
			{
				public boolean accept(File pathname)
				{
					return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(".ced");
				}

				public String getDescription()
				{
					return "CBE Experiment Data (*.ced)";
				}
			});

			chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

			returnValue = chooser.showSaveDialog(this);

			if (returnValue == JFileChooser.APPROVE_OPTION)
			{
				fileToWrite = chooser.getSelectedFile();

				if (fileToWrite.exists())
				{
					returnValue2 = JOptionPane.showConfirmDialog(this,
						"Confirm overwrite?");

					if ((returnValue2 == JOptionPane.NO_OPTION)
						|| (returnValue2 == JOptionPane.CANCEL_OPTION))
					{
						willWrite = false;
					} else
					{
						isOverWrite = true;
					}
				}


				if (willWrite)
				{
					JAXBContext jc = JAXBContext.newInstance(
						"org.patika.mada.dataXML");

					Marshaller m = jc.createMarshaller();

					if (!isOverWrite)
					{
						if (fileToWrite.getAbsolutePath().toLowerCase().endsWith(".ced"))
						{
							fileToWrite = new File(fileToWrite.getAbsolutePath());
						}
						else
						{
							fileToWrite = new File(fileToWrite.getAbsolutePath() + ".ced");
						}
					}

					this.resultFileName = fileToWrite.getPath();

					BufferedWriter writer =
						new BufferedWriter(new FileWriter(fileToWrite));

					m.setProperty("jaxb.formatted.output", Boolean.TRUE);
					m.marshal(pmd, writer);

					writer.close();
				}
			}
		}
		catch (MarshalException ue)
		{
			ue.printStackTrace();
		}
		catch (JAXBException je)
		{
			je.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

	}

// ---------------------------------------------------------------------
// Section: File parser functions
// ---------------------------------------------------------------------

	/**
	 * This is a utility function to be used for several
	 * classes during paring data/platform files into
	 * columns.
	 * @param str string to check
	 * @return true if starts with special character
	 */
	public static boolean startsWithSpecialChar(String str)
	{
		boolean result = false;

		if (str != null && str.length() > 0)
		{
			char first = str.charAt(0);
			if ((first >= 48 && first <= 57) || // numerical
				(first >= 65 && first <= 90) || // uppercase alphebetical
				(first >= 97 && first <= 122) ||  // lowercase alphebetical
				(first == 9) || // tab character
				(first == 34)) // " 
			{
				// do nothing
			} else
			{
				result = true;
			}
		}
		else if (str.equals("")) //found in GSE files between series data and sample data
		{
			result = true;
		}
		return result;
	}


	/**
	 * This method returns the index of a spexific column of a file
	 * @param line line to search
	 * @param columnName column name
	 * @return index of column whose name matches to the parameter
	 */
	public int getColumnIndex(String line, String columnName)
	{
		int index = 0;
		String[] tokens = getTokenArray(line);
		for (String token : tokens)
		{
			if (token.equals(columnName))
			{
				break;
			}
			index++;
		}
		return index;
	}


	/**
	 * Returns tokens in an array. If there is nothing between two consecutive tabs, then the array
	 * contains null for it.
	 * @param line line to parse
	 * @return array of tab-delimited tokens
	 */
	private String[] getTokenArray(String line)
	{
		List<String> tokens = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(line, "\t", true);

		boolean justSawTab = false;

		while(tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();

			if (token.equals("\t"))
			{
				if (justSawTab)
				{
					tokens.add(null);
				}
				else
				{
					justSawTab = true;
				}
			}
			else
			{
				justSawTab = false;
				tokens.add(token);
			}
		}

		return tokens.toArray(new String[tokens.size()]);
	}

	/**
	 * Infers a matching from column names in the data file to the found reference names in graph
	 * file.
	 * @param graphRefs references found in the graph
	 * @param dataCols column names in data file
	 * @return inferred matching (column name --> ref in graph)
	 */
	protected Map<String, String> getPredictedMatches(String[] graphRefs, List<String> dataCols)
	{
		Map<String, String> match = new HashMap<String, String>();

		// Get known reference sets
		Map<String, List<String>> knownRefsSetsMap = getKnownReferenceSetsMap();

		// Prepare a reverse map from refs in graph to synonyms sets

		Map<List<String>, String> list2Ref = new HashMap<List<String>, String>();

		for (String ref : graphRefs)
		{
			List<String> refSet = knownRefsSetsMap.get(ref);

			if (refSet != null && !list2Ref.containsKey(refSet))
			{
				list2Ref.put(refSet, ref);
			}
		}

		// Find a matching for column names

		for (String col : dataCols)
		{
			List<String> list = knownRefsSetsMap.get(col);

			if (list != null && list2Ref.containsKey(list))
			{
				match.put(col, list2Ref.get(list));
			}
		}

		return match;
	}

	/**
	 * Prepares a map from known reference names to the sets of those names. These sets contain
	 * other known names of the same reference, including the reference itself (used as key).
	 * @return map from known reference names to the sets of those names
	 */
	private Map<String, List<String>> getKnownReferenceSetsMap()
	{
		Map<String, List<String>> map = new HashMap<String, List<String>>();

		putInRefListMap(map, COMMON_ENSEMBLE_COLUMN_NAMES);
		putInRefListMap(map, COMMON_GB_ACCESSION_COLUMN_NAMES);
		putInRefListMap(map, COMMON_GENE_ID_COLUMN_NAMES);
		putInRefListMap(map, COMMON_GENE_SYMBOL_COLUMN_NAMES);
		putInRefListMap(map, COMMON_OMIM_COLUMN_NAMES);
		putInRefListMap(map, COMMON_REF_SEQ_PROT_ID_COLUMN_NAMES);
		putInRefListMap(map, COMMON_REF_SEQ_TRANSCRIPT_ID_COLUMN_NAMES);
		putInRefListMap(map, COMMON_SWISSPROT_COLUMN_NAMES);
		putInRefListMap(map, COMMON_UNIGENE_COLUMN_NAMES);
		putInRefListMap(map, COMMON_CPATH_COLUMN_NAMES);
		putInRefListMap(map, COMMON_KEY_COLUMN_NAMES);

		return map;
	}

	/**
	 * Adds the elements of the array as a new set to the map of known names.
	 * @param map to fill
	 * @param list to add
	 */
	private void putInRefListMap(Map<String, List<String>> map, List<String> list)
	{
		for (String name : list)
		{
			map.put(name, list);
		}
	}

	// ---------------------------------------------------------------------
	// Section: Static Members
	// ---------------------------------------------------------------------

	public static String KEY_COLUMN = "Key Column";


	/**
	 * This is the type of org.patika.pro.micro.converter.ExperimentDataConvertionWizard so as to
	 * differentiate between different components that will be put
	 * in the org.patika.pro.micro.converter.MicroarrayApplet	 *
	 */
	public static int TYPE_ID = 1;

	// ---------------------------------------------------------------------
	// Automatically recognized reference types
	// ---------------------------------------------------------------------

	// 1- Genebank
	public static final List<String> COMMON_GB_ACCESSION_COLUMN_NAMES = Arrays.asList(
		"GenBank Accession",
		"GB_ACC",
		"GenBank",
		"GB_LIST");

	// 2- Genesymbol
	public static final List<String> COMMON_GENE_SYMBOL_COLUMN_NAMES = Arrays.asList(
		"Gene Symbol",
		"Gene symbol",	
		"GENE_SYMBOL",
		"Symbol",
		"SYMBOL",
		"symbol",	
		"GeneSymbol");

	// 3-  GeneID
	public static final List<String> COMMON_GENE_ID_COLUMN_NAMES = Arrays.asList(
		"Locuslink",
		"Entrez Gene",
		"Locuslink ID",
		"Locuslink_ID",
		"EntrezGeneID",
		"ENTREZ_GENE_ID");

	// 4- Unigene
	public static final List<String> COMMON_UNIGENE_COLUMN_NAMES = Arrays.asList(
		"UNIGENE",	
		"Unigene",
		"Unigene_ID",
		"Database DB:unigene");

	// 5- OMIM
	public static final List<String> COMMON_OMIM_COLUMN_NAMES = Arrays.asList(
		"OMIM",
		"MIM");

	// 6- Ref_seq_prot_id
	public static final List<String> COMMON_REF_SEQ_PROT_ID_COLUMN_NAMES = Arrays.asList(
		"Refseq prot",
		"REF_SEQ");

	// 7- Ref_seq_trans_id
	public static final List<String> COMMON_REF_SEQ_TRANSCRIPT_ID_COLUMN_NAMES = Arrays.asList(
		"Refseq trans",
		"RefSeq Transcript ID");

	// 8- Ensemble
	public static final List<String> COMMON_ENSEMBLE_COLUMN_NAMES = Arrays.asList(
		"Ensembl Gene ID",
		"Ensemble",
		"Database DB:ensembl");

	// 8- Swissprot
	public static final List<String> COMMON_SWISSPROT_COLUMN_NAMES = Arrays.asList(
		"Swissprot",
		"SP_LIST",
		"UniProt",
		"UNIPROT");

	// 8- Swissprot
	public static final List<String> COMMON_CPATH_COLUMN_NAMES = Arrays.asList(
		"CPATH");

	// 9- Key in file
	public static final List<String> COMMON_KEY_COLUMN_NAMES = Arrays.asList(
		"ID",
		"Key",
		"Key to data file(s)");
}
