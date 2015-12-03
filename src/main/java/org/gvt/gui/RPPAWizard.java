package org.gvt.gui;

import org.cbio.causality.rppa.*;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class RPPAWizard extends Wizard
{
	FormatType formatType;
	ValueAmount valueAmount;
	public ValueMetric valueMetric;
	public ComparisonType comparisonType;
	String platFile;
	String valuesFile;
	String idColName;
	String siteColName;
	String symbolColName;
	String effectColName;
	String valueColName;
	List<String> vals0;
	List<String> vals1;
	public NetworkType networkType;
	public Centricity centricity;
	public double threshold = -1;
	public Set<RPPAData> activities;
	public Set<String> filterToGenes;

	FormatSelectionPage formatSelectionPage;
	PlatLoadPage platLoadPage;
	SingleFileSingleValueLoadPage singleFileSingleValueLoadPage;
	TwoGroupsPage twoGroupsPage;
	SingleValuePage singleValuePage;
	NetworkPage networkPage;
	ActivityAndFilterPage activityAndFilterPage;

	Text activityText;
	Text filterText;

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
		addPage(twoGroupsPage = new TwoGroupsPage());
		addPage(singleValuePage = new SingleValuePage());
		addPage(networkPage = new NetworkPage());
		addPage(activityAndFilterPage = new ActivityAndFilterPage());
	}

	@Override
	public boolean performFinish()
	{
		readActivityAndFilter();
		return true;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page)
	{
		if (page == formatSelectionPage)
		{
			if (formatType == FormatType.SINGLE_FILE && valueAmount == ValueAmount.SINGLE)
			{
				return singleFileSingleValueLoadPage;
			}
			return platLoadPage;
		}
		else if (page == platLoadPage && valueAmount == ValueAmount.TWO_GROUPS)
		{
			twoGroupsPage.initList();
			getShell().pack();
			return twoGroupsPage;
		}
		else if (page == platLoadPage && formatType == FormatType.TWO_FILES)
		{
			return singleValuePage;
		}
		else if (page == singleFileSingleValueLoadPage || page == twoGroupsPage) return networkPage;
		else if (page == networkPage) return activityAndFilterPage;
		return super.getNextPage(page);
	}

	@Override
	public boolean canFinish()
	{
		return networkType != null;
	}

	protected void readActivityAndFilter()
	{
		activities = new HashSet<RPPAData>();
		for (String s : activityText.getText().split("\\n"))
		{
			s = s.trim();
			String first = s;
			if (s.contains(" "))
			{
				first = s.substring(0, s.indexOf(" "));
			}

			Boolean b = first.endsWith("+") ? Boolean.TRUE : first.endsWith("-") ? Boolean.FALSE : null;
			if (b != null)
			{
				String sym = first.substring(0, first.length() - 1);
				if (!sym.isEmpty())
				{
					RPPAData data = new RPPAData(s, null, Arrays.asList(sym), null);
					data.makeActivityNode(b);
					activities.add(data);
				}
			}
		}

		filterToGenes = new HashSet<String>();

		for (String s : filterText.getText().split("\\s+"))
		{
			if (!s.isEmpty()) filterToGenes.add(s);
		}
	}

	public List<RPPAData> readData() throws FileNotFoundException
	{
		List<RPPAData> datas = RPPAFileReader.readAnnotation(platFile, idColName, symbolColName,
			siteColName, effectColName);

		if (formatType == FormatType.SINGLE_FILE)
		{
			RPPAFileReader.addValues(datas, platFile, idColName, vals0, vals1, 0D);
		}
		else if (formatType == FormatType.TWO_FILES)
		{
			RPPAFileReader.addValues(datas, valuesFile, idColName, vals0, vals1, 0D);
		}

		datas.addAll(activities);

		// todo add other options

		RPPAData.ChangeAdapter chDet = null;

		if (valueAmount != ValueAmount.TWO_GROUPS && valueMetric == ValueMetric.VALS_AROUND_ZERO)
		{
			chDet = ChangeDet.VALS_WITH_CENTER_0.det;
		}
		else if (valueAmount == ValueAmount.TWO_GROUPS)
		{
			if (comparisonType == ComparisonType.TTEST) chDet = ChangeDet.TTEST.det;
			else if (comparisonType == ComparisonType.LOG2_RATIO) chDet = ChangeDet.LOG_2_OF_RATIOS.det;
			else if (comparisonType == ComparisonType.DIFF) chDet = ChangeDet.DIFF.det;
		}
		else if (valueAmount == ValueAmount.SINGLE)
		{
			if (valueMetric == ValueMetric.PVAL) chDet = ChangeDet.SIGNED_PVAL.det;
			else if (valueMetric == ValueMetric.RATIO) chDet = ChangeDet.TAKE_LOG_2_OF_VAL.det;
		}

		if (chDet == null)
		{
			System.err.println("Not implemented yet.");
			return null;
		}

		chDet.setThreshold(threshold);
		for (RPPAData data : datas) if (!data.isActivity()) data.setChDet(chDet);

		return datas;
	}

	public String getSIFFilename()
	{
		String s = null;

		if (valuesFile != null) s = valuesFile;
		else if (platFile != null) s = platFile;

		if (s == null) return null;

		int sepIndex = s.lastIndexOf(File.separator);
		int dotInd = s.lastIndexOf(".");
		if (dotInd > 0 && dotInd > sepIndex) s = s.substring(0, dotInd);
		s += ".sif";
		return s;
	}

	class FormatSelectionPage extends WizardPage
	{
		List<Button> comparisonTypeButtons;
		List<Button> valueMetricButtons;

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
			GridData data = new GridData();
			data.grabExcessHorizontalSpace = true;
			group.setLayoutData(data);
			layout = new GridLayout();
			layout.numColumns = 1;
			group.setLayout(layout);
			group.setText("Select number of files");
			SelectionListener listener = new ButtonListener();
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
			group.setText("Select amount of value columns");
			for (ValueAmount type : ValueAmount.values())
			{
				Button b = new Button(group, SWT.RADIO);
				b.setText(type.text);
				b.addSelectionListener(listener);
			}

			valueMetricButtons = new ArrayList<Button>();

			group = new Group(composite, SWT.NONE);
			group.setToolTipText("What we will find in the given data files?");
			layout = new GridLayout();
			layout.numColumns = 1;
			group.setLayout(layout);
			group.setText("Select the type of values");
			for (ValueMetric type : ValueMetric.values())
			{
				Button b = new Button(group, SWT.RADIO);
				b.setText(type.text);
				b.addSelectionListener(listener);
				valueMetricButtons.add(b);
			}

			comparisonTypeButtons = new ArrayList<Button>();

			group = new Group(composite, SWT.NONE);
			group.setToolTipText("How should we compare the two groups?");
			layout = new GridLayout();
			layout.numColumns = 1;
			group.setLayout(layout);
			group.setText("Select comparison method");
			for (ComparisonType type : ComparisonType.values())
			{
				Button b = new Button(group, SWT.RADIO);
				b.setText(type.text);
				b.addSelectionListener(listener);
				comparisonTypeButtons.add(b);
				b.setEnabled(false);
			}

			setControl(composite);
		}

		@Override
		public boolean isPageComplete()
		{
			return formatType != null && valueAmount != null && valueMetric != null &&
				(valueAmount != ValueAmount.TWO_GROUPS || comparisonType != null);
		}

		class ButtonListener extends SelectionAdapter
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Button b = (Button) e.getSource();
				FormatType ft = FormatType.typeOf(b.getText());
				if (ft != null)
				{
					formatType = ft;

					if (formatType == FormatType.MULTIPLE_FILES)
					{
						for (Button but : valueMetricButtons)
						{
							if (ValueMetric.typeOf(but.getText()) == ValueMetric.MEASUREMENT)
							{
								if (but.getSelection())
								{
									but.setSelection(false);
									valueMetric = null;
								}
								but.setEnabled(false);
							}
						}
					}
					else
					{
						for (Button but : valueMetricButtons)
						{
							but.setEnabled(true);
						}
					}
				}

				ValueAmount va = ValueAmount.typeOf(b.getText());
				if (va != null)
				{
					valueAmount = va;
					for (Button but : comparisonTypeButtons)
					{
						but.setEnabled(valueAmount == ValueAmount.TWO_GROUPS);
					}
				}

				ValueMetric vm = ValueMetric.typeOf(b.getText());
				if (vm != null) valueMetric = vm;
				ComparisonType ct = ComparisonType.typeOf(b.getText());
				if (ct != null) comparisonType = ct;

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

			ComboListener listener = new ComboListener();

			group = new Group(composite, SWT.NONE);
			group.setText("Select \"ID\" column");
			group.setLayout(new RowLayout());
			idCombo = new Combo(group, SWT.NONE);
			idCombo.addSelectionListener(listener);

			group = new Group(composite, SWT.NONE);
			group.setText("Select \"symbol\" column");
			group.setLayout(new RowLayout());
			symbolCombo = new Combo(group, SWT.NONE);
			symbolCombo.addSelectionListener(listener);

			group = new Group(composite, SWT.NONE);
			group.setText("Select \"site\" column");
			group.setLayout(new RowLayout());
			siteCombo = new Combo(group, SWT.NONE);
			siteCombo.addSelectionListener(listener);

			group = new Group(composite, SWT.NONE);
			group.setText("Select \"effect\" column");
			group.setLayout(new RowLayout());
			effectCombo = new Combo(group, SWT.NONE);
			effectCombo.addSelectionListener(listener);

			setControl(composite);
		}

		protected String getSelected(Combo combo)
		{
			int selectionIndex = combo.getSelectionIndex();
			if (selectionIndex >= 0) return header[selectionIndex];
			return null;
		}

		protected void readFields()
		{
			idColName = getSelected(idCombo);
			symbolColName = getSelected(symbolCombo);
			siteColName = getSelected(siteCombo);
			effectColName = getSelected(effectCombo);
			setPageComplete(isPageComplete());
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

				int potentialID = RPPAFileReader.getPotentialIDColIndex(header);
				if (potentialID >= 0) idCombo.select(potentialID);
				int potentialSym = RPPAFileReader.getPotentialSymbolColIndex(header);
				if (potentialSym >= 0) symbolCombo.select(potentialSym);
				int potSite = RPPAFileReader.getPotentialSiteColIndex(header);
				if (potSite >= 0) siteCombo.select(potSite);
				int potEffect = RPPAFileReader.getPotentialEffectColIndex(header);
				if (potEffect >= 0) effectCombo.select(potEffect);
			}
			readFields();
			getShell().pack();
		}

		@Override
		public boolean isPageComplete()
		{
			return platFile != null && idColName != null && symbolColName != null
				&& siteColName != null;
		}

		class ComboListener extends SelectionAdapter
		{
			@Override
			public void widgetSelected(SelectionEvent selectionEvent)
			{
				readFields();
			}
		}
	}

	class SingleFileSingleValueLoadPage extends PlatLoadPage
	{
		Combo valueCombo;
		Text thresholdText;

		@Override
		public void createControl(Composite parent)
		{
			super.createControl(parent);
			Composite composite = (Composite) getControl();

			ComboListener listener = new ComboListener();

			Group group = new Group(composite, SWT.NONE);
			group.setText("Select \"value\" column");
			group.setLayout(new RowLayout());
			valueCombo = new Combo(group, SWT.NONE);
			valueCombo.addSelectionListener(listener);

			group = new Group(composite, SWT.NONE);
			group.setText("Enter a threshold value");
			group.setLayout(new RowLayout());
			thresholdText = new Text(group, SWT.SINGLE);
			thresholdText.setText(threshold + "");
			thresholdText.addModifyListener(new ModifyListener()
			{
				@Override
				public void modifyText(ModifyEvent modifyEvent)
				{
					readThreshold();
					setPageComplete(isPageComplete());
				}
			});
		}

		@Override
		public boolean isPageComplete()
		{
			return super.isPageComplete() && vals0 != null && !vals0.isEmpty() && threshold > 0;
		}

		@Override
		protected void parseFile()
		{
			super.parseFile();
			if (header != null)
			{
				valueCombo.setItems(header);
				List<String> columns = RPPAFileReader.getNamesOfNumberColumns(platFile);
				if (columns != null && !columns.isEmpty())
				{
					valueCombo.select(Arrays.binarySearch(header, columns.get(0)));
				}
			}
			readFields();
			getShell().pack();
		}

		@Override
		protected void readFields()
		{
			String colName = getSelected(valueCombo);
			if (colName != null)
			{
				vals0 = new ArrayList<String>(1);
				vals0.add(colName);
			}

			readThreshold();
			super.readFields();
		}

		private void readThreshold()
		{
			try
			{
				String text = thresholdText.getText();
				threshold = Double.parseDouble(text);
			}
			catch (NumberFormatException e)
			{
			}
		}
	}

	class TwoGroupsPage extends WizardPage
	{
		org.eclipse.swt.widgets.List list1;
		org.eclipse.swt.widgets.List list2;
		List<String> allItems;
		Button add0;
		Button add1;
		Button remove0;
		Button remove1;
		Button toRight;
		Button toLeft;
		Text thresholdText;
		Group thrGroup;

		TwoGroupsPage()
		{
			super("two-groups");
			setTitle("Groups to compare");
			setDescription("Select two sets of groups to compare.");
		}

		@Override
		public void createControl(Composite parent)
		{
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			composite.setLayout(layout);

			thrGroup = new Group(composite, SWT.BORDER);
			thrGroup.setText("Enter t-test p-value threshold");
			RowLayout rowL = new RowLayout();
			thrGroup.setLayout(rowL);
			thresholdText = new Text(thrGroup, SWT.SINGLE);
//			thresholdText.setText("0.05");
			thresholdText.addModifyListener(new ModifyListener()
			{
				@Override
				public void modifyText(ModifyEvent modifyEvent)
				{
					readThreshold();
					setPageComplete(isPageComplete());
				}
			});

			Composite listsGroup = new Composite(composite, SWT.NONE);

			layout = new GridLayout();
			layout.numColumns = 3;
			listsGroup.setLayout(layout);

			Group leftPanel = new Group(listsGroup, SWT.NONE);
			leftPanel.setText("Control group");
			Group middlePanel = new Group(listsGroup, SWT.NONE);
			Group rightPanel = new Group(listsGroup, SWT.NONE);
			rightPanel.setText("Test group");

			layout = new GridLayout();
			layout.numColumns = 1;
			leftPanel.setLayout(layout);
			layout = new GridLayout();
			layout.numColumns = 1;
			middlePanel.setLayout(layout);
			layout = new GridLayout();
			layout.numColumns = 1;
			rightPanel.setLayout(layout);

			ButtonListener listener = new ButtonListener();

			layout = new GridLayout();
			layout.numColumns = 2;
			Composite comp = new Composite(leftPanel, SWT.NONE);
			comp.setLayout(layout);
			add0 = new Button(comp, SWT.PUSH);
			add0.setText("Add");
			add0.addSelectionListener(listener);
			remove0 = new Button(comp, SWT.PUSH);
			remove0.setText("Remove");
			remove0.addSelectionListener(listener);

			layout = new GridLayout();
			layout.numColumns = 2;
			comp = new Composite(rightPanel, SWT.NONE);
			comp.setLayout(layout);
			add1 = new Button(comp, SWT.PUSH);
			add1.setText("Add");
			add1.addSelectionListener(listener);
			remove1 = new Button(comp, SWT.PUSH);
			remove1.setText("Remove");
			remove1.addSelectionListener(listener);

			list1 = new org.eclipse.swt.widgets.List (leftPanel, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
			GridData data = new GridData();
			data.grabExcessHorizontalSpace = true;
			list1.setLayoutData(data);
			list2 = new org.eclipse.swt.widgets.List (rightPanel, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
			data = new GridData();
			data.grabExcessHorizontalSpace = true;
			list2.setLayoutData(data);

			if (platFile != null)
			{
				initList();
			}
			else
			{
				vals0 = Collections.emptyList();
				vals1 = Collections.emptyList();
				initListWithDecoy();
			}

			toRight = new Button(middlePanel, SWT.PUSH);
			toRight.setText("-->");
			toRight.addSelectionListener(listener);
			toLeft = new Button(middlePanel, SWT.PUSH);
			toLeft.setText("<--");
			toLeft.addSelectionListener(listener);

			setControl(composite);
		}

		private void readThreshold()
		{
			try
			{
				String text = thresholdText.getText();
				threshold = Double.parseDouble(text);
			}
			catch (NumberFormatException e)
			{
			}
		}

		private void updateTexts()
		{
			if (comparisonType == ComparisonType.TTEST)
				thrGroup.setText("Enter t-test p-value threshold");
			else if (comparisonType == ComparisonType.LOG2_RATIO)
				thrGroup.setText("Enter log-2-ratio threshold");

		}

		protected void initList()
		{
			allItems = RPPAFileReader.getNamesOfNumberColumns(platFile);
			int midIndex = allItems.size() / 2;
			vals0 = new ArrayList<String>(allItems.subList(0, midIndex));
			vals1 = new ArrayList<String>(allItems.subList(midIndex, allItems.size()));

			updateListContents();
			updateTexts();
		}

		protected void initListWithDecoy()
		{
			for (int i = 0; i < 5; i++)
			{
				list1.add("              ");
				list2.add("              ");
			}
		}

		protected void add(List<String> current)
		{
			List<String> addable = getAddable();
			List<String> selected = new ArrayList<String>();

			ItemSelectionDialog dialog = new ItemSelectionDialog(getShell(), 300, "Select items",
				"Add one or more items to the list", addable, selected, true, true, null);
			dialog.open();

			if (!dialog.isCancelled())
			{
				current.addAll(selected);
				sort(current);
			}
		}

		protected void remove(List<String> current, org.eclipse.swt.widgets.List list)
		{
			String[] selection = list.getSelection();
			for (String s : selection)
			{
				current.remove(s);
			}
		}

		protected void sort(List<String> toSort)
		{
			Collections.sort(toSort, new Comparator<String>()
			{
				@Override
				public int compare(String o1, String o2)
				{
					Integer i1 = allItems.indexOf(o1);
					Integer i2 = allItems.indexOf(o2);
					return i1.compareTo(i2);
				}
			});
		}

		protected List<String> getAddable()
		{
			List<String> list = new ArrayList<String>(allItems);
			list.removeAll(vals0);
			list.removeAll(vals1);
			return list;
		}

		protected void transfer(List<String> fromList, List<String> toList,
			org.eclipse.swt.widgets.List from)
		{
			String[] selection = from.getSelection();
			for (String s : selection)
			{
				fromList.remove(s);
				toList.add(s);
			}
			sort(toList);
		}

		protected void updateListContents()
		{
			list1.removeAll();
			for (String s : vals0) list1.add(s);
			list2.removeAll();
			for (String s : vals1) list2.add(s);
		}

		@Override
		public boolean isPageComplete()
		{
			return !vals0.isEmpty() && !vals1.isEmpty() && threshold > 0;
		}

		class ButtonListener extends SelectionAdapter
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Button b = (Button) e.getSource();

				if (b == add0) add(vals0);
				else if (b == add1) add(vals1);
				else if (b == remove0) remove(vals0, list1);
				else if (b == remove1) remove(vals1, list2);
				else if (b == toRight) transfer(vals0, vals1, list1);
				else if (b == toLeft) transfer(vals1, vals0, list2);

				updateListContents();
				setPageComplete(isPageComplete());
			}
		}
	}

	class SingleValuePage extends PlatLoadPage
	{
		Combo valueCombo;
		Text thresholdText;
		Text valuesFileText;

		SingleValuePage()
		{
			setTitle("Values");
			setDescription("Identify the columns in values file.");
		}

		@Override
		public void createControl(Composite parent)
		{
			Composite composite = new Composite(parent, SWT.NONE);

			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			composite.setLayout(layout);

			Group group = new Group(composite, SWT.NONE);
			group.setText("Select RPPA values file");
			group.setLayout(new RowLayout());
			valuesFileText = new Text(group, SWT.SINGLE | SWT.BORDER);
			valuesFileText.setTextLimit(50);
			valuesFileText.setEditable(false);
			if (valuesFile != null) valuesFileText.setText(valuesFile);
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
						valuesFileText.setText(file);
						valuesFile = file;
						parseFile();
					}
				}
			});

			ComboListener listener = new ComboListener();

			group = new Group(composite, SWT.NONE);
			group.setText("Select \"ID\" column");
			group.setLayout(new RowLayout());
			idCombo = new Combo(group, SWT.NONE);
			idCombo.addSelectionListener(listener);

			group = new Group(composite, SWT.NONE);
			group.setText("Select \"value\" column");
			group.setLayout(new RowLayout());
			valueCombo = new Combo(group, SWT.NONE);
			valueCombo.addSelectionListener(listener);

			group = new Group(composite, SWT.NONE);
			group.setText("Enter a threshold value");
			group.setLayout(new RowLayout());
			thresholdText = new Text(group, SWT.SINGLE);
			thresholdText.setText(threshold + "");
			thresholdText.addModifyListener(new ModifyListener()
			{
				@Override
				public void modifyText(ModifyEvent modifyEvent)
				{
					readThreshold();
					setPageComplete(isPageComplete());
				}
			});

			setControl(composite);
		}

		protected void parseFile()
		{
			idColName = null;
			valueColName = null;

			header = RPPAFileReader.getHeader(valuesFile);

			if (header != null)
			{
				idCombo.setItems(header);
				valueCombo.setItems(header);

				int potentialID = RPPAFileReader.getPotentialIDColIndex(header);
				if (potentialID >= 0) idCombo.select(potentialID);
				valueCombo.select(header.length - 1);
			}
			readFields();
			getShell().pack();
		}


		private void readThreshold()
		{
			try
			{
				String text = thresholdText.getText();
				threshold = Double.parseDouble(text);
			}
			catch (NumberFormatException e)
			{
			}
		}

		@Override
		public boolean isPageComplete()
		{
			return valuesFile != null && vals0 != null && !vals0.isEmpty() && threshold > 0;
		}

		protected void readFields()
		{
			String colName = getSelected(valueCombo);
			if (colName != null)
			{
				vals0 = new ArrayList<String>(1);
				vals0.add(colName);
			}
			idColName = getSelected(idCombo);

			readThreshold();

			setPageComplete(isPageComplete());
		}

//		class ComboListener extends SelectionAdapter
//		{
//			@Override
//			public void widgetSelected(SelectionEvent selectionEvent)
//			{
//				readFields();
//			}
//		}
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
				if (type == networkType) b.setSelection(true);
			}

			group = new Group(composite, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 1;
			group.setLayout(layout);
			group.setText("Select desired network centricity");
			listener = new CentricityListener();
			for (Centricity type : Centricity.values())
			{
				Button b = new Button(group, SWT.RADIO);
				b.setText(type.text);
				b.addSelectionListener(listener);
				if (type == centricity) b.setSelection(true);
			}

			setControl(composite);
		}

		class NetworkTypeListener extends SelectionAdapter
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Button b = (Button) e.getSource();
				networkType = NetworkType.typeOf(b.getText());
				setPageComplete(isPageComplete());
			}
		}

		class CentricityListener extends SelectionAdapter
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Button b = (Button) e.getSource();
				centricity = Centricity.typeOf(b.getText());
				setPageComplete(isPageComplete());
			}
		}

		@Override
		public boolean isPageComplete()
		{
			return networkType != null && centricity != null;
		}
	}

	class ActivityAndFilterPage extends WizardPage
	{
		ActivityAndFilterPage()
		{
			super("filter");
			setTitle("Activities and gene filter");
			setDescription("Enter other activity changes that should be included to the analysis." +
				" The network can also be cropped to gene symbols of focus using this page.");
		}

		@Override
		public void createControl(Composite parent)
		{
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;
			composite.setLayout(layout);

			Group group = new Group(composite, SWT.BORDER);
			layout = new GridLayout();
			layout.numColumns = 1;
			group.setLayout(layout);
			group.setText("Enter activites as symbol and a plus (+) or a minus (-)");
			group.setToolTipText("Examples: AKT1+ EGFR-");
			activityText = new Text(group, SWT.MULTI);
			GridData data = new GridData();
			data.minimumWidth = 300;
			data.minimumHeight = 80;
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			activityText.setLayoutData(data);

			group = new Group(composite, SWT.BORDER);
			layout = new GridLayout();
			layout.numColumns = 1;
			group.setLayout(layout);
			group.setText("Enter genes to crop the network");
			group.setToolTipText("Only the relation(s) that are related with these genes will be " +
				"shown.");
			filterText = new Text(group, SWT.MULTI);
			data = new GridData();
			data.minimumWidth = 300;
			data.minimumHeight = 80;
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			filterText.setLayoutData(data);

			setControl(composite);
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

	enum ValueAmount
	{
		SINGLE("Single value"),
		GROUP("A group of values"),
		TWO_GROUPS("Two groups of values");

		String text;

		ValueAmount(String text)
		{
			this.text = text;
		}

		static ValueAmount typeOf(String text)
		{
			for (ValueAmount type : values())
			{
				if (type.text.equals(text)) return type;
			}
			return null;
		}
	}

	public enum ValueMetric
	{
		MEASUREMENT("Measurement (non-negative)"),
		VALS_AROUND_ZERO("Values around zero"),
		RATIO("Ratio (take log-2)"),
		LOG2_RAT("Log-2-ratio"),
		PVAL("P-values (use sign to indicate decrease)");

		String text;

		ValueMetric(String text)
		{
			this.text = text;
		}

		static ValueMetric typeOf(String text)
		{
			for (ValueMetric type : values())
			{
				if (type.text.equals(text)) return type;
			}
			return null;
		}
	}

	public enum ComparisonType
	{
		LOG2_RATIO("Log-2-ratio of values"),
		TTEST("P-value of a t-test"),
		DIFF("Difference of values");

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

	public enum NetworkType
	{
//		USE_EXISTING("Use current SIF network", RPPANetworkMapper.GraphType.EXISTING_NETWORK),
//		ALL_INCLUSIVE("Show all relations between all molecules", RPPANetworkMapper.GraphType.ALL_INCLUSIVE),
//		SELECT_NODES("Show all relations between changed molecules", RPPANetworkMapper.GraphType.CHANGED_ONLY),
//		NON_CONFLICT("Show all non-conflicting relations between all molecules", RPPANetworkMapper.GraphType.NON_CONFLICTING),
		COMPATIBLE_NETWORK("Show compatible relations between changed molecules", RPPANetworkMapper.GraphType.COMPATIBLE),
		COMPATIBLE_WITH_SITE_MATCH("Show compatible relations with matching sites", RPPANetworkMapper.GraphType.COMPATIBLE_WITH_SITE_MATCH),
		CONFLICTING_NETWORK("Show conflicting relations between changed molecules", RPPANetworkMapper.GraphType.CONFLICTING),
		CONFLICTING_WITH_SITE_MATCH("Show conflicting relations with matching sites", RPPANetworkMapper.GraphType.CONFLICTING_WITH_SITE_MATCH),
		;

		String text;
		public RPPANetworkMapper.GraphType type;

		private NetworkType(String text, RPPANetworkMapper.GraphType type)
		{
			this.text = text;
			this.type = type;
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

	public enum Centricity
	{
		GENE_CENTRIC("Gene Centric Network"),
		ANTIBODY_CENTRIC("Antibody Centric Network"),
		;

		String text;

		Centricity(String text)
		{
			this.text = text;
		}
		static Centricity typeOf(String text)
		{
			for (Centricity type : values())
			{
				if (type.text.equals(text)) return type;
			}
			return null;
		}
	}

	enum ChangeDet
	{
		VALS_WITH_CENTER_0(new RPPAData.ChangeAdapter(){}),

		POSITIVE_VALS_WITH_CENTER_1(new RPPAData.ChangeAdapter()
		{
			@Override
			public int getChangeSign(RPPAData data)
			{
				double val = getChangeValue(data);
				if (val >= threshold) return 1;
				if (val <= 1/threshold) return -1;
				return 0;
			}
		}),

		TAKE_LOG_2_OF_VAL(new RPPAData.ChangeAdapter()
		{
			@Override
			public double getChangeValue(RPPAData data)
			{
				return data.getLog2MeanVal();
			}
		}),

		LOG_2_OF_RATIOS(new RPPAData.ChangeAdapter()
		{
			@Override
			public double getChangeValue(RPPAData data)
			{
				return data.getLog2Ratio();
			}
		}),

		TTEST(new RPPAData.ChangeAdapter()
		{
			@Override
			public int getChangeSign(RPPAData data)
			{
				double pval = data.getTTestPval();
				if (pval > threshold) return 0;
				if (getChangeValue(data) > 0) return 1;
				return -1;
			}

			@Override
			public double getChangeValue(RPPAData data)
			{
				return data.getSignificanceBasedVal();
			}
		}),

		SIGNED_PVAL(new RPPAData.ChangeAdapter()
		{
			@Override
			public int getChangeSign(RPPAData data)
			{
				double pval = data.getMeanVal();
				if (Math.abs(pval) > threshold) return 0;
				return pval > 0 ? 1 : -1;
			}

			@Override
			public double getChangeValue(RPPAData data)
			{
				double val = -Math.log(Math.abs(data.getMeanVal())) / Math.log(2);
				if (data.getMeanVal() < 0) val *= -1;
				return val;
			}
		}),

		DIFF(new RPPAData.ChangeAdapter()
		{
			@Override
			public double getChangeValue(RPPAData data)
			{
				return data.getDifOfMeans();
			}
		})

		;

		RPPAData.ChangeAdapter det;

		ChangeDet(RPPAData.ChangeAdapter det)
		{
			this.det = det;
		}
	}

}