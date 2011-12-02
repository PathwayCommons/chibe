package org.patika.mada.gui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.patika.mada.dataXML.ChisioExperimentData;
import org.patika.mada.dataXML.Experiment;

import javax.swing.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.MarshalException;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * This class is for automatic conversion of microarray data into .ced files.
 * It extends the manual version due to dependence on certain amount of methods
 * of that class.
 *
 * @author Merve Cakir
 */
public class ExperimentDataAutomaticConvertionWizard extends ExperimentDataConvertionWizard
{
	/**
	 * File to hold series matrix file (GSExxx)
	 */
	private File seriesMatrixFile;

	/**
	 * File to hold platform file (GPLxxx)
	 */
	private File platformFile;

	/**
	 * accession number of selected series
	 */
	private String selectedSeries;

	/**
	 * Column headers read from series matrix and platform files
	 */
	private List<String> columnsSeries = new ArrayList<String>();
	private List<String> columnsPlatform = new ArrayList<String>();

	/**
	 * Mapping table
	 */
	private ReferenceTable mapTable;

	/**
	 * Constructor
	 * @param supportedReferenceTypes
	 * @param seriesMatrixFile
	 * @param platformFile
	 */
	public ExperimentDataAutomaticConvertionWizard(List<String> supportedReferenceTypes,
		File seriesMatrixFile,File platformFile, String selectedSeries)
	{
		super(supportedReferenceTypes);

		this.seriesMatrixFile = seriesMatrixFile;
		this.platformFile = platformFile;

		this.selectedSeries = selectedSeries;

		dataType = "Expression Data";
	}

	public void run()
	{
		readPlatformFile();
		readSeriesFile();

		finish();
	}

	/**
	 * reads platform file in order to identify column headers in the file
	 */
	private void readPlatformFile()
	{
		FileReader fr;
		BufferedReader br;
		String line = "";
		StringTokenizer tokenizer;

		columnsPlatform.clear();

		try
		{
			fr = new FileReader(platformFile);
			br = new BufferedReader(fr);
			do
			{
				line = br.readLine();
			}
			while(line != null && startsWithSpecialChar(line));

			// extract the column's names

			if(line != null)
			{
				tokenizer = new StringTokenizer(line,"\t");
				while(tokenizer.hasMoreTokens())
				{
					columnsPlatform.add(tokenizer.nextToken());
				}
			}

			String[] referenceInCombo = getReferenceArray();

			mapTable = new ReferenceTable(columnsPlatform,
				getPredictedMatches(referenceInCombo, columnsPlatform));
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
	}

	/**
	 * reads series matrix file in order to identify column names in the file
	 */
	public void readSeriesFile()
	{
		FileReader fr;
		BufferedReader br;
		String line="";
		StringTokenizer tokenizer;
		String experimentSetInfo="";

		columnsSeries.clear();

		try
		{
			fr = new FileReader(seriesMatrixFile);
			br = new BufferedReader(fr);
			do
			{
				line = br.readLine();
				experimentSetInfo += line + "\n";
			}
			while (line != null && startsWithSpecialChar(line));

			setExperimentSetInfo(experimentSetInfo);

			// extract the column's names

			if(line != null)
			{
				tokenizer = new StringTokenizer(line, "\t");
				while (tokenizer.hasMoreTokens())
				{
					columnsSeries.add(tokenizer.nextToken());
				}

				setSampleInfo(extractSampleInfos(experimentSetInfo, columnsSeries.size() - 1));

			}
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
	}

	public void finish()
	{
		// create the reference xml document so as to send to the server
		try
		{
			rmd = objFac.createRootExperimentData();

			// create the reference tuples
			fillInReferences(platformFile,
				mapTable.getMappedReferences(),columnsPlatform.size());

			// send them to the server so as to be mapped
			fillInDataValues();

			clearUselessRows();

			/**
			 * there must be at least one overlapping external reference type
			 * between the ones in graph and in platform file to create a valid
			 * .cde file.  
			 */
			if(rmd.getRow().isEmpty())
			{
				MessageDialog.openWarning(null,
					"No data!",
				 	"There is no external reference matching between graph and data");
				return;
			}

			//write Experiment data to XML
			writeExperimentData(rmd);
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null,
				"There occured an error during file "
					+ "parsing, \n please check your files and parameters!  ",
				"Parsing Error",
				JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public void fillInDataValues()
	{
		try
		{
			rmd.setExperimentType(this.getDataType());
			rmd.setExperimentSetInfo(this.experimentSetInfo);

			createExperimentsFromSingleDataFile(seriesMatrixFile,
				columnsPlatform.indexOf(mapTable.getNameOfKeyColumn()),
				getIndexOfExperimentStartColumn(columnsSeries),
				(columnsSeries.size() - 1));
		}
		catch (Exception e)
		{
			System.out.println("Error during patika micro file creation");
		}
	}

	/**
	 * Reads the dataset info, extracts sample related information and prepares sample info.
	 * @param datasetInfo
	 * @param sampleSize
	 * @return sample infos
	 */
	public String[] extractSampleInfos(String datasetInfo, int sampleSize)
	{
		String[] sampleInfo = new String[sampleSize];
		for (int i = 0; i < sampleSize; i++)
		{
			sampleInfo[i] = "";
		}
		String[] line = datasetInfo.split("\n");

		for (int i = 0; i < line.length; i++)
		{
			String[] token = line[i].split("\t");
			if (token.length == sampleSize + 1 && !allTheSame(token))
			{
				for (int j = 0; j < sampleSize; j++)
				{
					sampleInfo[j] += "\n" + token[j+1];
				}
			}
		}
		return sampleInfo;
	}

	protected boolean allTheSame(String[] info)
	{
		for (int i = 2; i < info.length; i++)
		{
			if (!info[1].equals(info[i])) return false;
		}
		return true;
	}

	/**
	 * This method marshalls a mapped Experiment data object
	 * into an XML file and saves .ced file into corresponding series directory.
	 */
	public void writeExperimentData(ChisioExperimentData pmd)
	{
		try
		{
			File fileToWrite = new File("experiments/" + selectedSeries, selectedSeries + ".ced");

			JAXBContext jc = JAXBContext.newInstance("org.patika.mada.dataXML");

			Marshaller m = jc.createMarshaller();

			this.resultFileName = fileToWrite.getPath();

			BufferedWriter writer =
					new BufferedWriter(new FileWriter(fileToWrite));

			m.setProperty("jaxb.formatted.output", Boolean.TRUE);
			m.marshal(pmd, writer);

			writer.close();

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
	
	/**
	 * identifies the start of experiment columns within series matrix columns
	 */
	private int getIndexOfExperimentStartColumn(List<String> columns)
	{
		int index = 0;
		for (int i = 0; i < columns.size(); i++)
		{
			if(columns.get(i).startsWith("\"GSM"))
			{
				index = i;
				break;
			}
		}
		return index;
	}

	/**
 	* Array for displaying references in graph
 	*/
	private String[] getReferenceArray()
	{
		java.util.List<String> reftypes = getSupportedReferencesTypes();
		String[] ref = new String[reftypes.size() + 2];

		ref[0] = "None";
		ref[1] = "Key to data file(s)";

		int i=2;
		for (String s : reftypes)
		{
			ref[i++] = s;
		}
		return ref;
	}
}
