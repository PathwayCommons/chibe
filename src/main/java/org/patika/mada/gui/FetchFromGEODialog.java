package org.patika.mada.gui;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.List;
import org.gvt.ChisioMain;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.gvt.gui.ItemSelectionDialog;
import org.gvt.util.Conf;
import sun.net.www.protocol.ftp.FtpURLConnection;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.zip.GZIPInputStream;


/**
 * Creates GUI features for data download from GEO database
 *
 * @author Merve Cakir
 */
public class FetchFromGEODialog extends Dialog
{
	ChisioMain main;
	Shell shell;

	private Label seriesIDLabel;
	private Text seriesIDText;

	private Label savedSeriesLabel;
	private List savedSeriesList;

	private Button fetchButton;
	private Button cancelButton;

	/**
	 * whether fetch(true) or cancel(false) is pressed
	 */
	private boolean fetchPressed;

	/**
	 * downloaded series matrix and platform files will be saved here
	 */
	public static File directory = new File(Conf.getExperimentsDir());

	/**
	 * subdirectory of "experiments". GSE and .ced files will be saved in here.
	 */
	private File seriesDirectory;

	/**
	 * accession number of file to be downloaded
	 */
	private String selectedSeries;

	/**
	 * file generated as a result of conversions
	 */
	private File cedFile;

	/**
	 * to control presence of .ced file in seriesDirectory
	 */
	private boolean cedPresent;

	/**
	 * File to hold series matrix file (GSExxx)
	 */
	private File seriesMatrixFile;

	/**
	 * File to hold platform file (GPLxxx)
	 */
	private File platformFile;

	/**
	 * holds files saved in directory
	 */
	private String[] savedSeriesNames = directory.list();

	/**
	 * Constructor
	 * @param main
	 */
	public FetchFromGEODialog (ChisioMain main)
	{
		super(main.getShell(), SWT.NONE);
		this.main = main;

		this.fetchPressed = false;
	}

	/**
	 * Open the dialog
	 */
	public void open()
	{
		createContents();

		shell.pack();
		shell.setLocation(
			getParent().getLocation().x + (getParent().getSize().x / 2) -
				(shell.getSize().x / 2),
			getParent().getLocation().y + (getParent().getSize().y / 2) -
				(shell.getSize().y / 2));

		shell.open();

		Display display = getParent().getDisplay();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
	}

	/**
	 * Create contents of the dialog
	 */	
	public void createContents()
	{
		shell = new Shell(getParent(),
			SWT.RESIZE | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		
		shell.setText("Fetch from GEO");

		ImageDescriptor id = ImageDescriptor.createFromFile(
                ChisioMain.class, "icon/cbe-icon.png");
		shell.setImage(id.createImage());

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		shell.setLayout(gridLayout);

		seriesIDLabel = new Label(shell, SWT.NONE);
		seriesIDLabel.setText("Enter GEO Series ID (GSExxx)");
		GridData gridData = new GridData(GridData.CENTER, GridData.CENTER,
			false, false);
		gridData.horizontalSpan = 2;
		seriesIDLabel.setLayoutData(gridData);

		seriesIDText = new Text(shell, SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.CENTER,
			true, false);
		gridData.horizontalSpan = 2;
		seriesIDText.setLayoutData(gridData);

		savedSeriesLabel = new Label(shell, SWT.NONE);
		savedSeriesLabel.setText("Or select from previous");
		gridData = new GridData(GridData.CENTER, GridData.CENTER,
			false, false);
		gridData.horizontalSpan = 2;
		savedSeriesLabel.setLayoutData(gridData);

		savedSeriesList = new List(shell,SWT.BORDER | SWT.V_SCROLL);
		gridData = new GridData(GridData.FILL, GridData.FILL,
			true, true);
		gridData.horizontalSpan = 2;
		gridData.heightHint = 75;
		savedSeriesList.setLayoutData(gridData);

		// want to display GSE directories only
		ArrayList<String> savedNames = new ArrayList<String>();
		for (String name : savedSeriesNames)
		{
			if(name.startsWith("GSE"))
			{
				savedNames.add(name);
			}
		}
		savedSeriesList.setItems(savedNames.toArray(new String[savedNames.size()]));

		savedSeriesList.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
                if(savedSeriesList.getSelection().length > 0)
                {
				    seriesIDText.setText(savedSeriesList.getSelection()[0]);
                }
			}
		});

		fetchButton = new Button(shell, SWT.NONE);
		fetchButton.setText("Fetch");
		gridData = new GridData(GridData.END, GridData.CENTER, true, false);
		fetchButton.setLayoutData(gridData);
		fetchButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				if(main.getRootGraph() == null)
				{
					MessageDialog.openError(main.getShell(), "Error!",
						"No BioPAX model loaded.");
					return;
				}

				// query string must be GSExxx. will prevent database search with invalid input
				if(seriesIDText.getText().startsWith("GSE"))
				{
					selectedSeries = seriesIDText.getText();
				}
				else
				{
					MessageDialog.openWarning(main.getShell(),
						"Invalid input!",
						"Enter a valid series ID!");
					return;
				}

				if (fetchAction())
				{
					shell.close();
				}
			}
		});

		cancelButton = new Button(shell, SWT.NONE);
		cancelButton.setText("Cancel");
		gridData = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
		cancelButton.setLayoutData(gridData);
		cancelButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				shell.close();
			}
		});
	}

	/**
	 * organizes the action for fetchButton, according to different situations.
	 */
	private boolean fetchAction()
	{
		assert selectedSeries != null;

		if (!createDataDirectory(selectedSeries)) return false;
		
		File[] fileList = seriesDirectory.listFiles();

		for (File file : fileList)
		{
			// returns .ced file if it is present
			if(file.getName().endsWith(".ced"))
			{
				cedFile = file;
				this.cedPresent = true;
				this.fetchPressed = true;
				return true;
			}
		}

		// assigns series matrix file if it is present in the directory,
		// otherwise creates an empty file to be filled.
		seriesMatrixFile = new File(seriesDirectory, selectedSeries);

		// series matrix file is downloaded if it is not found in seriesDirectory
		if(!seriesMatrixFile.exists())
		{
			main.lockWithMessage("Downloading series matrix file ...");
			if (!findSeriesMatrixFile(selectedSeries)) return false;
			main.unlock();
		}

		// permission to go further is given only after valid series matrix
		// and platform files are generated.
		if(seriesMatrixFile.exists())
		{
			main.lockWithMessage("Downloading platform file ...");
			platformFile = findPlatformFile();

			if(platformFile.exists())
			{
				fetchPressed = true;
			}
			main.unlock();
		}
		return true;
	}

	public boolean isFetchPressed()
	{
		return this.fetchPressed;
	}

	public boolean isCedPresent()
	{
		return cedPresent;
	}

	public String getSelectedSeries()
	{
		return selectedSeries;
	}

	public File getSeriesMatrixFile()
	{
		return seriesMatrixFile;
	}

	public File getPlatformFile()
	{
		return platformFile;
	}

	public File getCedFile()
	{
		return cedFile;
	}

	private boolean createDataDirectory(String selectedSeries)
	{
		// Check if data is already there
		
		File seriesDirectory = new File(directory, selectedSeries);
		File dataFile = new File(seriesDirectory, selectedSeries);

		if (dataFile.exists()) 
		{
			this.seriesDirectory = seriesDirectory;
			this.selectedSeries = selectedSeries;
			return true;
		}

		String givenPlat = null;

		if (selectedSeries.contains("-"))
		{
			// Check if data is there as a single platform file
			
			String series = selectedSeries.substring(0, selectedSeries.indexOf("-"));
			givenPlat = selectedSeries.substring(selectedSeries.indexOf("-") + 1);

			seriesDirectory = new File(directory, series);
			dataFile = new File(seriesDirectory, series);

			if (dataFile.exists()) 
			{
				this.seriesDirectory = seriesDirectory;
				this.selectedSeries = series;
				return true;
			}
		}

		java.util.List<String> plat = getMultiplePlatforms(selectedSeries);
		
		if (plat.size() < 2)
		{
			String series = selectedSeries.contains("-") ? 
				selectedSeries.substring(0, selectedSeries.indexOf("-")) : selectedSeries;

			this.seriesDirectory = new File(directory, series);
			this.seriesDirectory.mkdir();

			this.selectedSeries = series;
			return true;
			
		}

		String platform = selectPlatform(plat, (givenPlat != null &&
			!plat.contains(givenPlat) ? givenPlat : null));

		if (platform == null) return false;

		String series = selectedSeries.contains("-") ?
			selectedSeries.substring(0, selectedSeries.indexOf("-")) : selectedSeries;

		series += "-" + platform;

		this.seriesDirectory = new File(directory, series);
		this.seriesDirectory.mkdir();

		this.selectedSeries = series;
		return true;
	}
	
	/**
	 * downloads series matrix file from GEO database
	 */
	private boolean findSeriesMatrixFile(String selectedSeries)
	{
		String ftpPrefix = "ftp://ftp.ncbi.nih.gov/pub/geo/DATA/SeriesMatrix/";

		String platName = null;
		if (selectedSeries.contains("-"))
		{
			platName = selectedSeries.substring(selectedSeries.indexOf("-") + 1);
			selectedSeries = selectedSeries.substring(0, selectedSeries.indexOf("-"));
		}

		if (platName != null) platformFile = new File(directory, platName);

		try
		{
			String url;
			if (platName == null)
			{
				url = ftpPrefix + selectedSeries + "/" + selectedSeries + "_series_matrix.txt.gz";
			}
			else
			{
				url = ftpPrefix + selectedSeries + "/" + selectedSeries + "-" + platName +
					"_series_matrix.txt.gz";
			}

			if (!downloadCompressedFile(url, seriesMatrixFile.getPath()))
			{
				url = url.substring(0, url.lastIndexOf("ix") + 2);
				
				int i = 0;

				String u;
				do
				{
					i++;
					u = url + "-"  + i + ".txt.gz";
				}
				while(downloadCompressedFile(u, seriesMatrixFile.getPath() + "-" + i));

				uniteSeriesFiles(i-1);
			}
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageDialog.openWarning(main.getShell(), "Download failed!",
				"ChiBE could not download the file. Check your parameters.");

			return false;
		}
	}

	private void uniteSeriesFiles(int count)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(seriesMatrixFile.getPath()));

			BufferedReader rdr = new BufferedReader(
				new FileReader(seriesMatrixFile.getPath() + "-1"));

			for (String line = rdr.readLine(); line != null && line.startsWith("!Series"); 
				 line = rdr.readLine())
			{
				writer.write(line + "\n");
			}

			rdr.close();

			BufferedReader[] reader = new BufferedReader[count];

			for (int i = 0; i < count; i++)
			{
				reader[i] = new BufferedReader(new FileReader(
					seriesMatrixFile.getPath() + "-" + (i+1)));
			}

			for (String line = reader[0].readLine(); line != null; line = reader[0].readLine())
			{
				if (!line.contains("\t"))
				{
					writer.write(line + "\n");
					for (int j = 1; j < reader.length; j++) reader[j].readLine();
				}
				else
				{
					String id = line.substring(0, line.indexOf("\t"));
					
					writer.write(line);

					for (int j = 1; j < reader.length; j++)
					{
						line = reader[j].readLine();

						assert id.equals(line.substring(0, line.indexOf("\t")));

						writer.write(line.substring(line.indexOf("\t")));
					}
					writer.write("\n");
				}
			}

			for (BufferedReader r : reader) r.close();
			writer.close();

			// Delete temporary files
			for (int i = 0; i < count; i++)
			{
				new File(seriesMatrixFile.getPath() + "-" + (i+1)).delete();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private boolean downloadCompressedFile(String address, String saveFile) throws IOException
	{
		if (address.startsWith("ftp://"))
			return downloadCompressedFileThroughFTP(address, saveFile);

		try
		{
			URL url = new URL(address);
			URLConnection con = url.openConnection();

//			if (con instanceof )

			GZIPInputStream in = new GZIPInputStream(con.getInputStream());
	
			// Open the output file
			OutputStream out = new FileOutputStream(saveFile);
			// Transfer bytes from the compressed file to the output file
			byte[] buf = new byte[1024];

			int lines = 0;
			int len;
			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
				lines++;
			}
	
			// Close the file and stream
			in.close();
			out.close();

			return lines > 0;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	private boolean downloadCompressedFileThroughFTP(String address, String saveFile) throws IOException
	{
		try
		{
			FTPClient fc = new FTPClient();

			String host = address.substring(6, address.indexOf("/", 7));
			String fileloc = address.substring(address.indexOf("/", 7));

			fc.connect(host);
			fc.enterLocalPassiveMode();
			fc.setRemoteVerificationEnabled(false);
			fc.login("anonymous", "");
			fc.setFileType(FTP.BINARY_FILE_TYPE);

			InputStream is = new GZIPInputStream(fc.retrieveFileStream(fileloc));
			OutputStream out = new FileOutputStream(saveFile);

			int length = 0;

			byte[] buffer = new byte[65536];
			int noRead;

			while ((noRead = is.read(buffer)) > 0) {
				out.write(buffer, 0, noRead);
				length += noRead;
			}

			is.close();
			fc.disconnect();
			out.close();

			return length > 0;
		}
		catch (IOException e)
		{
//			e.printStackTrace();
			return false;
		}
	}

	private java.util.List<String> getMultiplePlatforms(String series)
	{
		if (series.contains("-")) series = series.substring(0, series.indexOf("-"));
		
		java.util.List<String> list = new ArrayList<String>();

		try
		{
			String[] files = listFtpFiles("ftp://ftp.ncbi.nih.gov/pub/geo/DATA/SeriesMatrix/" +
				series + "/");

			if (files.length > 1)
			{
				for (String file : files)
				{
					String id = file.substring(0, file.indexOf("_s"));
					if (file.contains("-"))
					{
						String pl = id.substring(id.indexOf("-") + 1);
						if (!list.contains(pl)) list.add(pl);
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return list;
	}
	
	private String selectPlatform(java.util.List<String> platforms)
	{
		return selectPlatform(platforms, null);
	}
	
	private String selectPlatform(java.util.List<String> platforms, String wrongPlat)
	{
		String message = "Series contain more than one platform." + 
			(wrongPlat == null ? "" : "\nAnd " + wrongPlat + " is not an option.") + 
			"\nPlease select one below.";
		
		ItemSelectionDialog d = new ItemSelectionDialog(this.shell, 200, "Select platform",
			message, platforms, null, false, true, null);

		Object selected = d.open();
		if (selected != null)
		{
			return selected.toString();
		}
		return null;
	}
	
	private String[] listFtpFiles(String ftpDir) throws IOException
	{
		java.util.List<String> list = new ArrayList<String>();

		FTPClient fc = new FTPClient();

		String host = ftpDir.substring(6, ftpDir.indexOf("/", 7));

		String dir = ftpDir.substring(ftpDir.indexOf("/", 7));

		fc.connect(host);
		fc.enterLocalPassiveMode();
		fc.setRemoteVerificationEnabled(false);
		fc.login("anonymous", "");

//		URL url = new URL(ftpDir);
//		URLConnection con = url.openConnection();
//		BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
//		for (String line = reader.readLine(); line != null; line = reader.readLine())
//		{
//			list.add(line);
//		}
//		reader.close();

		FTPFile[] ftpFiles = fc.listFiles(dir);
		for (FTPFile file : ftpFiles) {
			list.add(file.getName());
		}

		String[] files = list.toArray(new String[list.size()]);
		for (int i = 0; i < files.length; i++)
		{
			if (files[i].contains(" ")) files[i] = files[i].substring(files[i].indexOf(" ") + 1);
		}
		return files;
	}
	
	/**
	 * will load platform file if it is in the directory, otherwise
	 * downloads it from GEO database
	 */
	public File findPlatformFile()
	{
		try
		{
			if (platformFile == null)
			{
				BufferedReader br = new BufferedReader(new FileReader(seriesMatrixFile));

				String currentLine;
				String platformName = "";

				// finding accession number of platform file

				while((currentLine = br.readLine()) != null)
				{
					if(currentLine.contains("!Series_platform_id"))
					{
						platformName = currentLine.substring(currentLine.indexOf("G"),
							currentLine.lastIndexOf("\""));
						break;
					}
				}

				// assigns platform file if it is present in the directory,
				// otherwise creates an empty file to be filled.
				platformFile = new File(directory, platformName);
			}

			// if not found, download from GEO database

			if (!platformFile.exists())
			{
				String URLname = "http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?targ=self&" +
					"form=text&view=data&acc=" + platformFile.getName();

				URL url = new URL(URLname);
				URLConnection con = url.openConnection();

				BufferedReader reader = new BufferedReader(
					new InputStreamReader(con.getInputStream()));

				BufferedWriter writer = new BufferedWriter(new FileWriter(platformFile));

				String currentRead;

				while((currentRead = reader.readLine()) != null)
				{
					writer.write(currentRead + "\n");
				}

				reader.close();
				writer.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			MessageDialog.openWarning(main.getShell(),
				"Download failed!",
				"ChiBE could not download the file. Check your parameters.");

		}
		return platformFile;
	}

	static
	{
		if (!directory.exists())
		{
			directory.mkdir();
		}
	}
}




		
	