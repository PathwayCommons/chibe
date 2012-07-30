package org.patika.mada.gui;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.gvt.ChisioMain;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;
import java.util.ArrayList;


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
	public static File directory = new File("experiments");

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
//                if(savedSeriesList.getSelection().length > 0)
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

				shell.close();				
				fetchAction(selectedSeries);

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
	private void fetchAction(String selectedSeries)
	{
		seriesDirectory = new File(directory, selectedSeries);
		seriesDirectory.mkdir();

		File[] fileList = seriesDirectory.listFiles();

		for (File file : fileList)
		{
			// returns .ced file if it is present
			if(file.getName().endsWith(".ced"))
			{
				cedFile = file;
				this.cedPresent = true;
				this.fetchPressed = true;
				return;
			}
		}

		// assigns series matrix file if it is present in the directory,
		// otherwise creates an empty file to be filled.
		seriesMatrixFile = new File(seriesDirectory, selectedSeries);		

		// series matrix file is downloaded if it is not found in seriesDirectory
		if(!seriesMatrixFile.exists())
		{
			main.lockWithMessage("Downloading series matrix file ...");
			seriesMatrixFile = 	findSeriesMatrixFile(selectedSeries);
			main.unlock();
		}

		// permission to go further is given only after valid series matrix
		// and platform files are generated.
		if(seriesMatrixFile.exists())
		{
			main.lockWithMessage("Downloading platform file ...");
			platformFile = findPlatformFile(seriesMatrixFile);

			if(platformFile.exists())
			{
				fetchPressed = true;
			}
			main.unlock();
		}
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

	/**
	 * downloads series matrix file from GEO database
	 */
	private File findSeriesMatrixFile(String selectedSeries)
	{
		try
		{
			String URLname = "ftp://ftp.ncbi.nih.gov/pub/geo/DATA/SeriesMatrix/"
				+ selectedSeries + "/"+ selectedSeries + "_series_matrix.txt.gz";

			URL url = new URL(URLname);
			URLConnection con = url.openConnection();
			GZIPInputStream in = new GZIPInputStream(con.getInputStream());

			// Open the output file
			String target = seriesMatrixFile.getPath();
			OutputStream out = new FileOutputStream(target);
			// Transfer bytes from the compressed file to the output file
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}

			// Close the file and stream
			in.close();
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageDialog.openWarning(main.getShell(),
				"Download failed!",
				"ChiBE could not download the file. Check your parameters.");
		}
		return seriesMatrixFile;
	}

	/**
	 * will load platform file if it is in the directory, otherwise
	 * downloads it from GEO database
	 */
	public File findPlatformFile(File seriesMatrixFile)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(seriesMatrixFile));
			
			String currentLine = "";
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
			platformFile = new File("experiments/" + platformName);

			// if not found, download from GEO database

			if (platformFile.exists() == false)
			{
				String URLname = "http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?targ=self&form=text&view=data&acc="
					+ platformName;

				URL url = new URL(URLname);
				URLConnection con = url.openConnection();

				BufferedReader reader;
				BufferedWriter writer = new BufferedWriter(new FileWriter(platformFile));

				do
				{
					reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				}
				while (reader == null);

				String currentRead = "";

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




		
	