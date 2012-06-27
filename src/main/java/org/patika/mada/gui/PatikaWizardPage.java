package org.patika.mada.gui;

import javax.swing.*;

/**
 * @author Emek Demir
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public abstract class PatikaWizardPage extends JPanel
{
	/**
	 * The owner wizard.
	 */
	protected PatikaWizard wizard;

// ---------------------------------------------------------------------
// Section: Constructors and initialization.
// ---------------------------------------------------------------------
	/**
	 * The constructor of a wizard page.
	 * @param wizard is the owner wizard
	 */
	public PatikaWizardPage( PatikaWizard wizard )
	{
		setLayout( null );
		this.wizard = wizard;
	}

// ---------------------------------------------------------------------
// Section: Accessors.
// ---------------------------------------------------------------------
	/**
	 * All pages should have the ability to reset its fields.
	 */
	abstract void reset();

	/**
	 * All pages make their changes on the subject at once, when this
	 * method is called.
	 */
	abstract void update();

	/**
	 * The owner wizard should maintain the page controller, and this
	 * method provides the required information to enable or disable the
	 * next button of the controller.
	 *
	 * @return true if the page is ready to be skipped
	 */
	public boolean canNext()
	{
		return true;
	}

	/**
	 * The owner wizard should maintain the page controller, and this
	 * method provides the required information to enable or disable the
	 * finish button of the controller.
	 *
	 * @return true if the page is permitting finalization of the
	 * creation process.
	 */
	public boolean canFinish()
	{
		return true;
	}

	/**
	 * This method forward the call to super.
	 *
	 * @param visible
	 */
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
	}
}
