package org.patika.mada.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.LinkedList;

/**
 * @author Emek Demir
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public abstract class PatikaWizard extends JDialog
{
	/**
	 * Total number of pages of the wizard.
	 */
	protected int pageCount;

	/**
	 * Page array.
	 */
	PatikaWizardPage[] pages;

	/**
	 * Index to use in page array possibly the page being displayed.
	 */
	protected int currentPage = 0;

	/**
	 * Finish button of the wizard.
	 */
	JButton finishButton = new JButton();

	/**
	 * Cancel button of the wizard.
	 */
	JButton cancelButton = new JButton();

	/**
	 * Next button of the wizard.
	 */
	JButton nextButton = new JButton();

	/**
	 * Back button of the wizard.
	 */
	JButton backButton = new JButton();

	/**
	 * The panel that will host the controlling buttons.
	 */
	JCheckBox propagate = new JCheckBox();

	JPanel buttonsPanel = new JPanel();

	JPanel controllerPanel = new JPanel();

	BorderLayout mainLayout = new BorderLayout();

	/**
	 * This variable stores whether the wizard is finished associated
	 * process or not.
	 */
	protected boolean completed;

	/**
	 * The listener for the controlling buttons.
	 */
	private ActionListener patikaWizardAction;

	/**
	 * This list holds predefined members, i.e. selected members in
	 * the graph in the time of creation.
	 */
	LinkedList predefinedMembers;

// ---------------------------------------------------------------------
// Section: Constructors and initialization.
// ---------------------------------------------------------------------

	public PatikaWizard()
	{
		this(null, null, null);
	}


	public PatikaWizard(String title, Object target)
	{
		this(title, target, null);
	}

	public PatikaWizard(String title, Object target, LinkedList predefinedMembers)
	{
		setTitle(title);

		// If not running on Mac, make the dialog modal. Mac cannot handle this code and application
		// freezes.
		if (!(System.getProperty("on.name") != null &&
			System.getProperty("on.name").toLowerCase().contains("os")))
			setModal(true);

		this.predefinedMembers = predefinedMembers;

	    this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		patikaWizardAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				PatikaWizard.this.checkButtons();
				Object object = event.getSource();
				if (object == finishButton)
					try
					{
						finishButton_actionPerformed(event);
					} catch (Throwable e)
					{
						e.printStackTrace();
					}

				else if (object == nextButton)
					try
					{
						nextButton_actionPerformed(event);
					} catch (Throwable e)
					{
						e.printStackTrace();
					}

				else if (object == backButton)
					try
					{
						backButton_actionPerformed(event);
					} catch (Throwable e)
					{
					}

				else if (object == cancelButton)
					try
					{
						cancelButton_actionPerformed(event);
					} catch (Throwable e)
					{
					}
			}
		};

		/**
		 * Register listeners.
		 */
		this.cancelButton.addActionListener(this.patikaWizardAction);
		this.backButton.addActionListener(this.patikaWizardAction);
		this.nextButton.addActionListener(this.patikaWizardAction);
		this.finishButton.addActionListener(this.patikaWizardAction);

		this.initializeGUI();

		this.initPages(target);
	}

// ---------------------------------------------------------------------
// Section: Accessors.
// ---------------------------------------------------------------------
	/**
	 * This methods returns true if the user selected to propagate the
	 * created node to other views as well.
	 *
	 * @return true if the user selected to propagate the created
	 * node to other viewa as well
	 */
	public boolean isPropagateSelected()
	{
		return this.propagate.isSelected();
	}

	/**
	 * This method sets the location and attributes of the GUI
	 * components.
	 */
	protected void initializeGUI()
	{
		this.setSize(500, 330);
		this.getContentPane().setLayout(mainLayout);
		controllerPanel.setAlignmentY((float) 0.5);
		controllerPanel.setBorder(BorderFactory.createEtchedBorder());
		backButton.setText("< Back");
		nextButton.setText("Next >");
		cancelButton.setText("Cancel");
		finishButton.setText("Finish");
		this.propagate.setText("Propagate to all views");
		this.controllerPanel.add(this.propagate, null);
		controllerPanel.add(cancelButton, null);
		controllerPanel.add(backButton, null);
		controllerPanel.add(nextButton, null);
		controllerPanel.add(finishButton, null);
		this.getContentPane().add(controllerPanel, BorderLayout.SOUTH);
		this.setLocationRelativeTo(null);

	}

	/**
	 * This method resets the pages of the wizard.
	 */
	protected void resetPages()
	{
		for (int i = 0; i < pageCount; i++)
		{
			pages[i].reset();
		}
	}

	/**
	 * This method switches the wizard to the page specified.
	 *
	 * @param index of the target page
	 */
	protected void switchPage(int index, boolean reset)
	{
		assert (index > 0 || index <= this.pageCount):"Page index out of bounds";

		pages[currentPage].setVisible(false);

		if (reset)
			pages[currentPage].reset();

		currentPage = index;
		getContentPane().add(pages[currentPage], BorderLayout.CENTER);

		pages[currentPage].setVisible(true);

		this.checkButtons();

		getContentPane().repaint();
	}

	/**
	 * This method updates the interactibility status of the controller
	 * buttons.
	 */
	protected void checkButtons()
	{
		//Check if the user can do next
		if ((currentPage == pages.length - 1)
		    || !pages[currentPage].canNext())
		{
			nextButton.setEnabled(false);
		}
		else
		{
			nextButton.setEnabled(true);
		}

		//Check if the user can do back
		if (currentPage == 0)
		{
			backButton.setEnabled(false);
		}
		else
		{
			backButton.setEnabled(true);
		}

		//Check if the user can finish
		if (this.canFinish())
		{
			finishButton.setEnabled(true);
		}
		else
		{
			finishButton.setEnabled(false);
		}
	}

	/**
	 * This method checks whether the wizard can finish or not.
	 *
	 * @return true if the wizard can finish
	 */
	protected boolean canFinish()
	{
		boolean completed = true;

		for (int i = 0; i < pageCount; i++)
		{
			if (!pages[i].canFinish())
			{
				completed = false;
				break;
			}
		}
		return completed;
	}

	/**
	 * This method accesses to the listener of the wizard.
	 *
	 * @return the listener.
	 */
	protected ActionListener getActionListener()
	{
		return this.patikaWizardAction;
	}

	public boolean isCompleted()
	{
		return completed;
	}

	public void setCompleted(boolean completed)
	{
		this.completed = completed;
	}

	/**
	 * This method should initialize the pages.
	 */
	protected abstract void initPages(Object target);

	private void cancelButton_actionPerformed(ActionEvent event)
	{
		this.dispose();
	}

	private void backButton_actionPerformed(ActionEvent event)
	{
		this.switchPage(this.currentPage - 1, true);
	}

	protected void nextButton_actionPerformed(ActionEvent event)
	{
		this.switchPage(this.currentPage + 1, false);
	}

	/**
	 * This method calls finishButton method.
	 * @param event
	 */
	public void finishButton_keyTyped(java.awt.event.KeyEvent event)
	{
		if (event.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER)
		{
			this.finish();
		}
	}

	void finishButton_actionPerformed(ActionEvent event)
	{
		finish();
	}

	protected void finish()
	{
		for (int i = 0; i < this.pages.length; i++)
		{
			pages[i].update();
		}

		this.completed = true;

		this.dispose();
	}
}
