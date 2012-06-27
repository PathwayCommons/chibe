package org.gvt.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gvt.ChisioMain;
import org.gvt.action.*;
import org.gvt.layout.*;

/**
 * This class maintains the Layout Properties dialog to set the parameters of
 * each layout
 *
 * @author Cihan Kucukkececi
 *
 * Copyright: I-Vis Research Group, Bilkent University, 2007
 */
public class LayoutInspector extends Dialog
{
	//General
	private Button incremental;
	private Button proofButton;
	private Button defaultButton;
	private Button draftButton;
	private Group tuningGroup;
	private Scale animationPeriod;
	protected Button animateDuringLayoutButton;
	protected Button animateOnLayoutButton;
	private Button createBendsAsButton;

	//CoSE
	private Scale gravityStrength,
		springStrength,
		repulsionStrength,
		compoundGravityStrength;

	private Text desiredEdgeLengthCoSE;

	//Spring
	private Text disconnectedNodeDistanceSpringRestLength;

	private Text nodeDistanceRestLength;

	protected Object result;

	protected Shell shell;

	private ChisioMain main;

	public static int lastTab = 0;

	private KeyAdapter keyAdapter = new KeyAdapter()
	{
		public void keyPressed(KeyEvent arg0)
		{
			arg0.doit = isDigit(arg0.keyCode);
		}

		public boolean isDigit(int keyCode)
		{
			if (Character.isDigit(keyCode)
				|| keyCode == SWT.DEL
				|| keyCode == 8
				|| keyCode == SWT.ARROW_LEFT
				|| keyCode == SWT.ARROW_RIGHT)
			{
				return true;
			}
			return false;
		}
	};

	public static void main(String args[])
	{
		//	new LayoutInspector(new Shell(), SWT.TITLE).open();
	}

	/**
	 * Create the dialog
	 */
	public LayoutInspector(ChisioMain main)
	{
		super(main.getShell(), SWT.NONE);
		this.main = main;
	}

	/**
	 * Open the dialog
	 */
	public Object open()
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
			if (!display.readAndDispatch())
				display.sleep();
		}
		return result;
	}

	/**
	 * Create contents of the dialog
	 */
	protected void createContents()
	{
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Layout Properties");

		ImageDescriptor id = ImageDescriptor.createFromFile(ChisioMain.class, "icon/cbe-icon.png");
		shell.setImage(id.createImage());

		GridLayout gridLy = new GridLayout();
		gridLy.numColumns = 1;
		shell.setLayout(gridLy);

		final TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		GridData gridDt = new GridData();
		gridDt.grabExcessVerticalSpace = true;
		tabFolder.setLayoutData(gridDt);

		final TabItem generalTabItem = new TabItem(tabFolder, SWT.NONE);
		generalTabItem.setText("General");

		final Composite compositeGeneral = new Composite(tabFolder, SWT.NONE);
		generalTabItem.setControl(compositeGeneral);

		gridLy = new GridLayout();
		gridLy.numColumns = 2;
		compositeGeneral.setLayout(gridLy);

		final Group animationGroup = new Group(compositeGeneral, SWT.NONE);
		animationGroup.setText("Animation");
		animationGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
		gridLy = new GridLayout();
		gridLy.numColumns = 1;
		animationGroup.setLayout(gridLy);

		animateOnLayoutButton = new Button(animationGroup, SWT.CHECK);
		animateOnLayoutButton.setText("Animate on Layout");

		animateDuringLayoutButton = new Button(animationGroup, SWT.CHECK);
		animateDuringLayoutButton.setAlignment(SWT.UP);
		animateDuringLayoutButton.setText("Animate during Layout");

		Composite periodComposite = new Composite(animationGroup, SWT.NONE);
		gridLy = new GridLayout();
		gridLy.numColumns = 2;
		periodComposite.setLayout(gridLy);

		final Label animationPeriodLabel = new Label(periodComposite, SWT.NONE);
		animationPeriodLabel.setText("Animation Period");
		gridDt = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		animationPeriodLabel.setLayoutData(gridDt);

		animationPeriod = new Scale(periodComposite, SWT.NONE);
		animationPeriod.setSelection(50);
		animationPeriod.setIncrement(5);
		gridDt = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		animationPeriod.setLayoutData(gridDt);

		final Group layoutQualityGroup = new Group(compositeGeneral, SWT.NONE);
		layoutQualityGroup.setText("Layout Quality");
		gridLy = new GridLayout();
		gridLy.numColumns = 1;
		layoutQualityGroup.setLayout(gridLy);

		draftButton = new Button(layoutQualityGroup, SWT.RADIO);
		draftButton.setText("Draft");

		defaultButton = new Button(layoutQualityGroup, SWT.RADIO);
		defaultButton.setText("Default");

		proofButton = new Button(layoutQualityGroup, SWT.RADIO);
		proofButton.setText("Proof");

		Composite extra = new Composite(compositeGeneral, SWT.NONE);
		gridDt = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridDt.horizontalSpan = 2;
		extra.setLayoutData(gridDt);
		gridLy = new GridLayout();
		gridLy.numColumns = 1;
		extra.setLayout(gridLy);

		incremental = new Button(extra, SWT.CHECK);
		incremental.setText("Incremental");

		createBendsAsButton = new Button(extra, SWT.CHECK);
		createBendsAsButton.setText("Create Bends as Needed");

		final TabItem coseTabItem = new TabItem(tabFolder, SWT.NONE);
		coseTabItem.setText("CoSE");

		final TabItem springTabItem = new TabItem(tabFolder, SWT.NONE);
		springTabItem.setText("Spring");

		final Composite compositeCoSE = new Composite(tabFolder, SWT.NONE);
		coseTabItem.setControl(compositeCoSE);
		gridLy = new GridLayout();
		gridLy.numColumns = 1;
		compositeCoSE.setLayout(gridLy);

		tuningGroup = new Group(compositeCoSE, SWT.NONE);
		tuningGroup.setText("Tuning");
		gridLy = new GridLayout();
		gridLy.numColumns = 2;
		tuningGroup.setLayout(gridLy);

		final Label springStrengthLabel = new Label(tuningGroup, SWT.NONE);
		springStrengthLabel.setText("Spring Strength");

		springStrength = new Scale(tuningGroup, SWT.NONE);
		springStrength.setIncrement(5);

		final Label repulsionStrengthLabel = new Label(tuningGroup, SWT.NONE);
		repulsionStrengthLabel.setText("Repulsion Strength");

		repulsionStrength = new Scale(tuningGroup, SWT.NONE);
		repulsionStrength.setIncrement(5);

		final Label gravityLevelLabel = new Label(tuningGroup, SWT.NONE);
		gravityLevelLabel.setText("Gravity Strength");

		gravityStrength = new Scale(tuningGroup, SWT.NONE);
		gravityStrength.setIncrement(5);

		final Label compoundGravityStrengthLabel = new Label(tuningGroup, SWT.NONE);
		compoundGravityStrengthLabel.setText("Compound Gravity Strength");

		compoundGravityStrength = new Scale(tuningGroup, SWT.NONE);
		compoundGravityStrength.setIncrement(5);
		compositeCoSE.setTabList(new Control[] {tuningGroup});

		Composite desire = new Composite(compositeCoSE, SWT.NONE);
		gridLy = new GridLayout();
		gridLy.numColumns = 2;
		desire.setLayout(gridLy);

		final Label desiredEdgeLengthLabel = new Label(desire, SWT.NONE);
		desiredEdgeLengthLabel.setText("Desired Edge Length    ");
		gridDt = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		desiredEdgeLengthLabel.setLayoutData(gridDt);

		desiredEdgeLengthCoSE = new Text(desire, SWT.BORDER);
		desiredEdgeLengthCoSE.addKeyListener(keyAdapter);
		gridDt = new GridData();
		gridDt.widthHint = 30;
		desiredEdgeLengthCoSE.setLayoutData(gridDt);

		final Composite compositeSpring = new Composite(tabFolder, SWT.NONE);
		springTabItem.setControl(compositeSpring);
		gridLy = new GridLayout();
		gridLy.numColumns = 2;
		compositeSpring.setLayout(gridLy);

		final Label nodedistancerestlengthLabel = new Label(compositeSpring, SWT.NONE);
		nodedistancerestlengthLabel.setText("Desired Edge Length");

		nodeDistanceRestLength = new Text(compositeSpring, SWT.BORDER);
		nodeDistanceRestLength.addKeyListener(keyAdapter);
		gridDt = new GridData();
		gridDt.widthHint = 30;
		nodeDistanceRestLength.setLayoutData(gridDt);

		final Label disconnectednodedistancespringrestlengthLabel =
			new Label(compositeSpring, SWT.NONE);
		disconnectednodedistancespringrestlengthLabel.
			setText("Disconnected Component Separation");

		disconnectedNodeDistanceSpringRestLength = new Text(compositeSpring, SWT.BORDER);
		disconnectedNodeDistanceSpringRestLength.addKeyListener(keyAdapter);
		gridDt = new GridData();
		gridDt.widthHint = 30;
		disconnectedNodeDistanceSpringRestLength.setLayoutData(gridDt);

		Composite buttons = new Composite(shell, SWT.NONE);
		buttons.setLayout(new RowLayout());
		gridDt = new GridData();
		gridDt.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;

		final Button okButton = new Button(buttons, SWT.NONE);
		okButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				storeValuesToOptionsPack();
				lastTab = tabFolder.getSelectionIndex();
				shell.close();
			}
		});
		okButton.setText("OK");

		final Button layoutButton = new Button(buttons, SWT.NONE);
		layoutButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int i = tabFolder.getSelectionIndex();
				storeValuesToOptionsPack();

				switch (i)
				{
					case 1:
						new CoSELayoutAction(main).run();
						break;
					case 2:
						new SpringLayoutAction(main).run();
						break;
				}
			}
		});

		layoutButton.setText("Layout");

		final Button cancelButton = new Button(buttons, SWT.NONE);
		cancelButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				lastTab = tabFolder.getSelectionIndex();
				shell.close();
			}
		});

		cancelButton.setText("Cancel");

		final Button defaultButton2 = new Button(buttons, SWT.NONE);
		defaultButton2.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int i = tabFolder.getSelectionIndex();
				setDefaultLayoutProperties(i);
			}
		});

		defaultButton2.setText("Default");

		if (lastTab == 0)
		{
			layoutButton.setEnabled(false);
		}

		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0)
			{
				if (tabFolder.getSelectionIndex() == 0)
				{
					layoutButton.setEnabled(false);
				}
				else
				{
					layoutButton.setEnabled(true);
				}
			}
		});

		setInitialValues();

		tabFolder.setSelection(lastTab);
	}

	public void storeValuesToOptionsPack()
	{
		LayoutOptionsPack lop = LayoutOptionsPack.getInstance();

		//General
		lop.getGeneral().setAnimationPeriod(animationPeriod.getSelection());
		lop.getGeneral().setAnimationOnLayout(
			animateOnLayoutButton.getSelection());
		lop.getGeneral().setAnimationDuringLayout(
			animateDuringLayoutButton.getSelection());

		if (proofButton.getSelection())
		{
			lop.getGeneral().setLayoutQuality(AbstractLayout.PROOF_QUALITY);
		}
		else if (draftButton.getSelection())
		{
			lop.getGeneral().setLayoutQuality(AbstractLayout.DRAFT_QUALITY);
		}
		else
		{
			lop.getGeneral().setLayoutQuality(AbstractLayout.DEFAULT_QUALITY);
		}

		lop.getGeneral().setIncremental(incremental.getSelection());
		lop.getGeneral().setCreateBendsAsNeeded(
			createBendsAsButton.getSelection());

		//CoSE
		lop.getCoSE().setIdealEdgeLength(
			Integer.parseInt(desiredEdgeLengthCoSE.getText()));
		lop.getCoSE().setCompoundGravityStrength(
			compoundGravityStrength.getSelection());
		lop.getCoSE().setGravityStrength(gravityStrength.getSelection());
		lop.getCoSE().setRepulsionStrength(repulsionStrength.getSelection());
		lop.getCoSE().setSpringStrength(springStrength.getSelection());

		//Spring
		lop.getSpring().setNodeDistanceRestLength(
			Integer.parseInt(nodeDistanceRestLength.getText()));
		lop.getSpring().setDisconnectedNodeDistanceSpringRestLength(
			Integer.parseInt(
				disconnectedNodeDistanceSpringRestLength.getText()));
	}

	public void setInitialValues()
	{
		LayoutOptionsPack lop = LayoutOptionsPack.getInstance();

		//General
		animationPeriod.setSelection(lop.getGeneral().getAnimationPeriod());
		animateDuringLayoutButton.setSelection(
			lop.getGeneral().isAnimationDuringLayout());
		animateOnLayoutButton.setSelection(
			lop.getGeneral().isAnimationOnLayout());

		if (lop.getGeneral().getLayoutQuality() == AbstractLayout.PROOF_QUALITY)
		{
			proofButton.setSelection(true);
		}
		else if (lop.getGeneral().getLayoutQuality() ==
			AbstractLayout.DRAFT_QUALITY)
		{
			draftButton.setSelection(true);
		}
		else
		{
			defaultButton.setSelection(true);
		}

		incremental.setSelection(lop.getGeneral().isIncremental());
		createBendsAsButton.setSelection(
			lop.getGeneral().isCreateBendsAsNeeded());

		//CoSE
		desiredEdgeLengthCoSE.setText(
			String.valueOf(lop.getCoSE().getIdealEdgeLength()));
		gravityStrength.setSelection(lop.getCoSE().getGravityStrength());
		compoundGravityStrength.setSelection(
			lop.getCoSE().getCompoundGravityStrength());
		repulsionStrength.setSelection(lop.getCoSE().getRepulsionStrength());
		springStrength.setSelection(lop.getCoSE().getSpringStrength());

		//Spring
		nodeDistanceRestLength.setText(
			String.valueOf(lop.getSpring().getNodeDistanceRestLength()));
		disconnectedNodeDistanceSpringRestLength.setText(String.valueOf(
			lop.getSpring().getDisconnectedNodeDistanceSpringRestLength()));
	}

	public void setDefaultLayoutProperties(int select)
	{
		if (select == 0)
		{
			//General
			animateDuringLayoutButton.setSelection(
				AbstractLayout.DEFAULT_ANIMATION_DURING_LAYOUT);
			animationPeriod.setSelection(50);
			animateOnLayoutButton.setSelection(
				AbstractLayout.DEFAULT_ANIMATION_ON_LAYOUT);
			defaultButton.setSelection(true);
			proofButton.setSelection(false);
			draftButton.setSelection(false);
			incremental.setSelection(AbstractLayout.DEFAULT_INCREMENTAL);
			createBendsAsButton.setSelection(
				AbstractLayout.DEFAULT_CREATE_BENDS_AS_NEEDED);
		}
		else if (select == 1)
		 {
			//CoSE
			desiredEdgeLengthCoSE.setText(
				String.valueOf(CoSELayout.DEFAULT_EDGE_LENGTH));
			gravityStrength.setSelection(50);
			compoundGravityStrength.setSelection(50);
			repulsionStrength.setSelection(50);
			springStrength.setSelection(50);
		}
		else if (select == 2)
		{
			//Spring
			nodeDistanceRestLength.setText(String.valueOf((int)
				SpringLayout.DEFAULT_NODE_DISTANCE_REST_LENGTH_CONSTANT));
			disconnectedNodeDistanceSpringRestLength.setText(
				String.valueOf((int) SpringLayout.
					DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_REST_LENGTH));
		}
	}
}