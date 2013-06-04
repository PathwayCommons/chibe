package org.patika.mada.gui;

import org.patika.mada.util.ExperimentData;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * @author Recep Colak
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ExperimentInfoCollectionPage extends PatikaWizardPage
{
	/**
	 * the bioentity creation wizard, that will own this page
	 */
	ExperimentDataConvertionWizard mdcw;

	private JComboBox dataTypeBox;

	private JRadioButton inSingle;

	private JCheckBox hasPlatform;

// ---------------------------------------------------------------------
// Section: Constructors and initialization.
// ---------------------------------------------------------------------
	/**
	 * This constructor initializes the first page of the
	 * microarray tableData convertion wizard
	 *
	 * @param mdcw the wizard, that owns this page
	 */
	public ExperimentInfoCollectionPage(ExperimentDataConvertionWizard mdcw)
	{
		super(mdcw);
		this.mdcw = mdcw;

		//main panel settings
		this.setSize(new Dimension(489, 260));
		this.setLayout(new FlowLayout());

		TitledBorder mainTitledBorder = new TitledBorder(BorderFactory.createEtchedBorder(
			new Color(52, 52, 52), new Color(25, 25, 25)),
			" Experiment information specification ");

		this.setBorder(mainTitledBorder);

		//settings for the components of the main panels
		Border innerBorder = new EtchedBorder(EtchedBorder.RAISED,
			Color.white, new Color(148, 145, 140));

		//matching panel settings
		JPanel matchingPanel = new JPanel();
		matchingPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		matchingPanel.setBorder(innerBorder);
		JLabel matchingLabel1 = new JLabel("Select type of data to load");
		dataTypeBox = new JComboBox(ExperimentData.getDataTypes().toArray());
		matchingPanel.add(matchingLabel1);
		matchingPanel.add(dataTypeBox);

 		//tableData file panel settings
		JPanel dataFilePanel = new JPanel();
		dataFilePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		dataFilePanel.setBorder(innerBorder);
		JLabel dataFileLabel1 = new JLabel("Experiments are in ");
		JLabel dataFileLabel2 = new JLabel("data file(s) ");
		inSingle = new JRadioButton("single");
		JRadioButton inMultiple = new JRadioButton("multiple");
		ButtonGroup dataFileGroup = new ButtonGroup();
		dataFileGroup.add(inSingle);
		dataFileGroup.add(inMultiple);
		inSingle.setSelected(true);
		dataFilePanel.add(dataFileLabel1);
		dataFilePanel.add(inSingle);
		dataFilePanel.add(inMultiple);
		dataFilePanel.add(dataFileLabel2);

		//platform panel settings
		JPanel platformPanel = new JPanel();
		platformPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		platformPanel.setBorder(innerBorder);
		JLabel platformLabel = new JLabel("Experiment set has a reference information file");
		hasPlatform = new JCheckBox();
		hasPlatform.setSelected(false);
		platformPanel.add(platformLabel);
		platformPanel.add(hasPlatform);

		//put all the components to the main page
		JPanel holderPanel = new JPanel();
		holderPanel.setLayout(new BorderLayout(10,40));
		holderPanel.add(matchingPanel, BorderLayout.NORTH);
		holderPanel.add(dataFilePanel, BorderLayout.CENTER);
		holderPanel.add(platformPanel, BorderLayout.SOUTH);

		this.add(holderPanel);
	}

// ---------------------------------------------------------------------
// Section: Accessors and mutators.
// ---------------------------------------------------------------------
	void reset()
	{
		/**
		*  Set to default options
		*/
		this.hasPlatform.setSelected(true);
		mdcw.setHasPlatformFile(true);
		this.dataTypeBox.setSelectedIndex(0);
		this.hasPlatform.setSelected(false);
		mdcw.setDataInSingleFile(false);
	}

	/**
	 * when the "Finish" button of the wizard is clicked,
	 * this method will set update the experiment specification
	 */
	void update()
	{
		mdcw.setHasPlatformFile(this.hasPlatform.isSelected());
		mdcw.setDataInSingleFile(this.inSingle.isSelected());
		mdcw.setDataType(this.dataTypeBox.getSelectedItem().toString());
	}

	public boolean canNext()
	{
		return true;
	}

	public boolean canFinish()
	{
		return true;
	}
}
