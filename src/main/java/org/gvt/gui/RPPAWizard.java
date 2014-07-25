package org.gvt.gui;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.gvt.util.RPPAFileReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class RPPAWizard extends Wizard
{
	FormatType formatType;
	ValueType valueType;
	String platFile;
	String idColName;
	String siteColName;
	String symbolColName;
	String effectColName;
	List<String> vals1;
	List<String> vals2;
	NetworkType networkType;

	FormatSelectionPage formatSelectionPage;
	PlatLoadPage platLoadPage;
	SingleFileSingleValueLoadPage singleFileSingleValueLoadPage;

	/**
	 * Create the dialog
	 */
	public RPPAWizard()
	{
	}

	@Override
	public void addPages()
	{
		addPage(formatSelectionPage = new FormatSelectionPage());
		addPage(platLoadPage = new PlatLoadPage());
		addPage(singleFileSingleValueLoadPage = new SingleFileSingleValueLoadPage());
	}

	@Override
	public boolean performFinish()
	{
		return true;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page)
	{
		if (page == formatSelectionPage)
		{
			if (formatType == FormatType.SINGLE_FILE && valueType == ValueType.SINGLE)
			{
				return singleFileSingleValueLoadPage;
			}
			return platLoadPage;
		}
		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish()
	{
		return networkType != null;
	}



	class FormatSelectionPage extends WizardPage
	{
		protected FormatSelectionPage()
		{
			super("intro");
			setTitle("File format");
			setDescription("Select if the RPPA data is in a single or multiple files. Also state" +
				" how to use the values.");
		}

		@Override
		public void createControl(Composite parent)
		{
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			layout.makeColumnsEqualWidth = true;
			composite.setLayout(layout);

			Group group = new Group(composite, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 1;
			group.setLayout(layout);
			group.setText("Select number of files");
			SelectionListener listener = new FormatTypeListener();
			for (FormatType type : FormatType.values())
			{
				Button b = new Button(group, SWT.RADIO);
				b.setText(type.text);
				b.addSelectionListener(listener);
			}

			group = new Group(composite, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 1;
			group.setLayout(layout);
			group.setText("Select value type");
			listener = new ValueTypeListener();
			for (ValueType type : ValueType.values())
			{
				Button b = new Button(group, SWT.RADIO);
				b.setText(type.text);
				b.addSelectionListener(listener);
			}

			setControl(composite);
		}

		@Override
		public boolean isPageComplete()
		{
			return formatType != null && valueType != null;
		}

		class FormatTypeListener extends SelectionAdapter
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Button b = (Button) e.getSource();
				formatType = FormatType.typeOf(b.getText());
				setPageComplete(isPageComplete());
			}
		}

		class ValueTypeListener extends SelectionAdapter
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Button b = (Button) e.getSource();
				valueType = ValueType.typeOf(b.getText());
				setPageComplete(isPageComplete());
			}
		}
	}

	class PlatLoadPage extends WizardPage
	{
		Text platFileText;
		Combo idCombo;
		Combo symbolCombo;
		Combo siteCombo;
		Combo effectCombo;
		String[] header;

		protected PlatLoadPage()
		{
			super("file-load");
			setTitle("Load file");
			setDescription("Select files and designate columns.");
		}

		@Override
		public void createControl(Composite parent)
		{
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			composite.setLayout(layout);

			Group group = new Group(composite, SWT.NONE);
			group.setText(formatType == FormatType.SINGLE_FILE ?
				"Select RPPA data file" : "Select RPPA annotation file");
			group.setLayout(new RowLayout());
			platFileText = new Text(group, SWT.SINGLE | SWT.BORDER);
			platFileText.setTextLimit(50);
			platFileText.setEditable(false);
			if (platFile != null) platFileText.setText(platFile);
			Button platButton = new Button(group, SWT.PUSH);
			platButton.setText("Browse");
			platButton.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent selectionEvent)
				{
					FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
					String file = dialog.open();
					if (file != null)
					{
						platFileText.setText(file);
						platFile = file;
						parseFile();
					}
				}
			});

			group = new Group(composite, SWT.NONE);
			group.setText("Select \"ID\" column");
			group.setLayout(new RowLayout());
			idCombo = new Combo(group, SWT.NONE);
			idCombo.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent selectionEvent)
				{
					idColName = header[idCombo.getSelectionIndex()];
					setPageComplete(isPageComplete());
				}
			});

			group = new Group(composite, SWT.NONE);
			group.setText("Select \"symbol\" column");
			group.setLayout(new RowLayout());
			symbolCombo = new Combo(group, SWT.NONE);
			symbolCombo.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent selectionEvent)
				{
					symbolColName = header[symbolCombo.getSelectionIndex()];
					setPageComplete(isPageComplete());
				}
			});

			group = new Group(composite, SWT.NONE);
			group.setText("Select \"site\" column");
			group.setLayout(new RowLayout());
			siteCombo = new Combo(group, SWT.NONE);
			siteCombo.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent selectionEvent)
				{
					siteColName = header[siteCombo.getSelectionIndex()];
					setPageComplete(isPageComplete());
				}
			});

			group = new Group(composite, SWT.NONE);
			group.setText("Select \"effect\" column");
			group.setLayout(new RowLayout());
			effectCombo = new Combo(group, SWT.NONE);
			effectCombo.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent selectionEvent)
				{
					effectColName = header[effectCombo.getSelectionIndex()];
					setPageComplete(isPageComplete());
				}
			});

			setControl(composite);
		}

		protected void parseFile()
		{
			idColName = null;
			symbolColName = null;
			siteColName = null;
			effectColName = null;

			header = RPPAFileReader.getHeader(platFile);
			if (header != null)
			{
				idCombo.setItems(header);
				symbolCombo.setItems(header);
				siteCombo.setItems(header);
				effectCombo.setItems(header);

				String potentialID = RPPAFileReader.getPotentialIDColname(header);
				if (potentialID != null) idCombo.select(Arrays.binarySearch(header, potentialID));
				String potentialSym = RPPAFileReader.getPotentialSymbolColname(header);
				if (potentialSym != null) symbolCombo.select(Arrays.binarySearch(header, potentialSym));
				String potSite = RPPAFileReader.getPotentialSiteColname(header);
				if (potSite != null) siteCombo.select(Arrays.binarySearch(header, potSite));
				String potEffect = RPPAFileReader.getPotentialEffectColname(header);
				if (potEffect != null) effectCombo.select(Arrays.binarySearch(header, potEffect));
			}
			setPageComplete(isPageComplete());
			getShell().pack();
		}

		@Override
		public boolean isPageComplete()
		{
			return platFile != null && idColName != null && symbolColName != null
				&& siteColName != null;
		}
	}

	class SingleFileSingleValueLoadPage extends PlatLoadPage
	{
		Combo valueCombo;
		Combo treatmentCombo;
		ValueTreatment treatment;

		@Override
		public void createControl(Composite parent)
		{
			super.createControl(parent);
			Composite composite = (Composite) getControl();

			Group group = new Group(composite, SWT.NONE);
			group.setText("Select \"value\" column");
			group.setLayout(new RowLayout());
			valueCombo = new Combo(group, SWT.NONE);
			valueCombo.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent selectionEvent)
				{
					vals1 = new ArrayList<String>(1);
					vals1.add(header[valueCombo.getSelectionIndex()]);
					setPageComplete(isPageComplete());
				}
			});
			group = new Group(composite, SWT.NONE);
			group.setText("Select value treatment");
			group.setLayout(new RowLayout());
			treatmentCombo = new Combo(group, SWT.NONE);
			treatmentCombo.setItems(ValueTreatment.getStringItems());
			treatmentCombo.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent selectionEvent)
				{
					treatment = ValueTreatment.values()[treatmentCombo.getSelectionIndex()];
					setPageComplete(isPageComplete());
				}
			});
		}

		@Override
		public boolean isPageComplete()
		{
			return super.isPageComplete() && vals1 != null && !vals1.isEmpty() && treatment != null;
		}

		@Override
		protected void parseFile()
		{
			super.parseFile();
			if (header != null)
			{
				valueCombo.setItems(header);
				List<String> columns = RPPAFileReader.getIndexesOfNumberColumns(platFile);
				if (columns != null && !columns.isEmpty())
				{
					valueCombo.select(Arrays.binarySearch(header, columns.get(0)));
				}
			}
		}
	}

	class NetworkPage extends WizardPage
	{

		NetworkPage()
		{
			super("network");
			setTitle("Network type");
			setDescription("Select the desired method to generate the network using RPPA data.");
		}

		@Override
		public void createControl(Composite parent)
		{
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			layout.makeColumnsEqualWidth = true;
			composite.setLayout(layout);

			Group group = new Group(composite, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 1;
			group.setLayout(layout);
			group.setText("Select desired network type");
			SelectionListener listener = new NetworkTypeListener();
			for (NetworkType type : NetworkType.values())
			{
				Button b = new Button(group, SWT.RADIO);
				b.setText(type.text);
				b.addSelectionListener(listener);
			}
		}

		class NetworkTypeListener extends SelectionAdapter
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Button b = (Button) e.getSource();
				formatType = FormatType.typeOf(b.getText());
				setPageComplete(isPageComplete());
			}
		}

	}

	enum FormatType
	{
		SINGLE_FILE("Single file"),
		TWO_FILES("An annotation and one value(s) file"),
		MULTIPLE_FILES("An annotation and several value files");

		String text;

		FormatType(String text)
		{
			this.text = text;
		}

		static FormatType typeOf(String text)
		{
			for (FormatType type : values())
			{
				if (type.text.equals(text)) return type;
			}
			return null;
		}
	}


	enum ValueType
	{
		SINGLE("Single value"),
		AVERAGE("Average of several values"),
		TWO("Compare two values"),
		TWO_GROUPS("Compare two groups of values"),
		TIME_SERIES("Time series data");

		String text;

		ValueType(String text)
		{
			this.text = text;
		}

		static ValueType typeOf(String text)
		{
			for (ValueType type : values())
			{
				if (type.text.equals(text)) return type;
			}
			return null;
		}
	}

	enum ComparisonType
	{
		RATIO("Ratio of values"),
		LOG2_RATIO("Log-2-ratio of values"),
		TTEST("P-value of a t-test");

		String text;

		ComparisonType(String text)
		{
			this.text = text;
		}

		static ComparisonType typeOf(String text)
		{
			for (ComparisonType type : values())
			{
				if (type.text.equals(text)) return type;
			}
			return null;
		}
	}

	enum ValueTreatment
	{
		AS_IS("Use as is"),
		LOG2("Take log2"),
		P_VAL("Treat as p-values, sign indicating the change direction");

		String text;

		ValueTreatment(String text)
		{
			this.text = text;
		}

		static String[] getStringItems()
		{
			ValueTreatment[] values = values();
			String[] s = new String[values.length];
			for (int i = 0; i < s.length; i++)
			{
				s[i] = values[i].text;
			}
			return s;
		}
	}

	enum NetworkType
	{
		USE_EXISTING("Use current network"),
		ALL_INCLUSIVE("Show all relations between all molecules"),
		SELECT_NODES("Show all relations between changed molecules"),
		COMPATIBLE_NETWORK("Show compatible relations between changed molecules");

		String text;

		NetworkType(String text)
		{
			this.text = text;
		}

		static NetworkType typeOf(String text)
		{
			for (NetworkType type : values())
			{
				if (type.text.equals(text)) return type;
			}
			return null;
		}
	}
}