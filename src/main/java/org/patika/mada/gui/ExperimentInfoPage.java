package org.patika.mada.gui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.SWT;

/**
 * @author Ozgun Babur
 */
public class ExperimentInfoPage extends WizardPage
{
	protected ExperimentInfoPage()
	{
		super("Information About Experiment");
		setTitle(getName());
		setDescription("Please fill in some initial information " +
			"about the experiment data you want to load.");
	}

	public void createControl(Composite parent)
	{
		Composite cmpst = new Composite(parent, SWT.NONE);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.makeColumnsEqualWidth = true;
		cmpst.setLayout(gridLayout);

		//------

		Group typeGrp = new Group(cmpst, SWT.NONE);
		typeGrp.setLayout(new FillLayout());

		Text typeTxt = new Text(typeGrp, SWT.NONE);
		typeTxt.setText("Select type of data to load .. ");

		Combo typeCmb = new Combo(typeGrp, SWT.NONE);
		typeCmb.add("Expression Data");
		typeCmb.add("Mass Spectrometry Data");
		typeCmb.add("Copy Number Variation");
		typeCmb.add("Mutation Data");

		//------

		Group fileNumGrp = new Group(cmpst, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fileNumGrp.setLayout(fillLayout);

		Text filetxt1 = new Text(fileNumGrp, SWT.NONE);
		filetxt1.setText("Experiments are in ");

		Button singleButton = new Button(fileNumGrp, SWT.RADIO);
		singleButton.setText("single");
		singleButton.pack();

		Button multipleButton = new Button(fileNumGrp, SWT.RADIO);
		multipleButton.setText("multiple");
		multipleButton.pack();

		Text filetxt2 = new Text(fileNumGrp, SWT.NONE);
		filetxt2.setText(" data file(s)");

		//------

		Group platformGrp = new Group(cmpst, SWT.NONE);
		platformGrp.setLayout(new FillLayout());

		Button platformButton = new Button(platformGrp, SWT.CHECK);
		platformButton.setText("Experiment set has a reference information file");

		//------

		setControl(parent);
	}

	/* (non-Javadoc)
				* Method declared on IWizardPage.
				* The default behavior is to ask the wizard for the next page.
				*/
	public IWizardPage getNextPage()
	{
		ExperimentInfoPage page = new ExperimentInfoPage();
		page.setWizard(this.getWizard());
		return page;
	}
}
