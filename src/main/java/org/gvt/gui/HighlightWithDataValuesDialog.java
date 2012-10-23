package org.gvt.gui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.gvt.ChisioMain;

import java.text.DecimalFormat;


/**
 * This class implements the dialog used to obtain range of interest for HighlightWithDataValues feature.
 *
 * @author Merve Cakir
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class HighlightWithDataValuesDialog extends Dialog
{
    private Shell shell;

    private Label infoLabel;

    private Button buttonMin;
    private Button buttonMax;

    private Text textMin;
    private Text textMax;

    private Scale scaleMin;
    private Scale scaleMax;

    private Button withinButton;
    private Button outsideButton;

    private Button okButton;
    private Button cancelButton;

    // The lowest value that Scale can display
    private double minBound;
    // The highest value that Scale can display
    private double maxBound;

    // Lower bound of the range specified by user
    private double minResult;
    // Upper bound of the range specified by user
    private double maxResult;

    // First element stores minResult and second element stores maxResult
    private double[] resultArray;

    // true for within, false for outside
    private boolean rangeType;

    private boolean okPressed;

    private boolean newData;

    // Arrays to remember selections of the user
    static boolean[] currentBoolean = new boolean[3];       //button selections
    static int[] currentInt = new int[2];       //values of scales
    static String[] currentString = new String[2];      //values in text fields

    public HighlightWithDataValuesDialog(Shell parent, double maxBound, double minBound, boolean newData)
    {
        super(parent, SWT.NONE);

        this.minBound = minBound;
        this.maxBound = maxBound;

        this.newData = newData;

        // Initialized to lowest value possible
        minResult = -Double.MAX_VALUE;
        // Initialized to highest value possible
        maxResult = Double.MAX_VALUE;

        resultArray = new double[] {minResult, maxResult};

        rangeType = true;

        okPressed = false;
    }

    /**
     * Open the dialog
     * @return whether ok or cancel pressed
     */
    public boolean open()
    {
        createContents();

        shell.pack();
        shell.setLocation(getParent().getLocation().x + (getParent().getSize().x / 2) - (shell.getSize().x / 2),
            getParent().getLocation().y + (getParent().getSize().y / 2) - (shell.getSize().y / 2));
        shell.open();

        Display display = getParent().getDisplay();

        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }

        return okPressed;
    }

    /**
     * Create contents of the dialog
     */
    public void createContents()
    {
        shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText("Highlight With Data Values");

        ImageDescriptor id = ImageDescriptor.createFromFile(
            ChisioMain.class, "icon/cbe-icon.png");
        shell.setImage(id.createImage());

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        shell.setLayout(gridLayout);

        infoLabel = new Label(shell,SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
        gridData.horizontalSpan = 3;
        gridData.verticalSpan = 5;
        infoLabel.setLayoutData(gridData);
        infoLabel.setText("Choose range of interest for data values");

        Group scaleGroup = new Group(shell, SWT.NONE);
        gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        gridData.horizontalSpan = 3;
        scaleGroup.setLayoutData(gridData);
        scaleGroup.setLayout(new GridLayout(3, false));

        buttonMin = new Button(scaleGroup, SWT.CHECK | SWT.WRAP);
        gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        buttonMin.setLayoutData(gridData);
        buttonMin.setText("Min");
        buttonMin.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent event)
            {
                // Enable text and scale only if button is selected
                textMin.setEnabled(buttonMin.getSelection());
                scaleMin.setEnabled(buttonMin.getSelection());

                // Ensures minimum value is smaller than maximum value after minimum button's selection is toggled true.
                if (buttonMin.getSelection() && scaleMin.getSelection() > scaleMax.getSelection())
                {
                    scaleMin.setSelection(scaleMax.getSelection());
                    textMin.setText(textMax.getText());
                }

                // Range selection is only offered when both min and max are selected and for other cases rangeType is
                // always true to ensure that only min and only max selections are working properly.

                if (!buttonMax.getSelection() || !buttonMin.getSelection())
                {
                    outsideButton.setEnabled(false);
                    withinButton.setEnabled(false);
                    rangeType = true;
                }
                else
                {
                    outsideButton.setEnabled(true);
                    withinButton.setEnabled(true);
                    rangeType = withinButton.getSelection();
                }
            }
        });

        textMin = new Text(scaleGroup, SWT.BORDER | SWT.SINGLE);
        gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        gridData.widthHint = 60;
        textMin.setLayoutData(gridData);
        textMin.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent event)
            {
                try
                {
                    int code = event.keyCode;

                    // enter pressed
                    if (code == SWT.CR)
                    {
                        String text = ((Text)event.widget).getText();
                        if (!text.isEmpty())
                        {
                            Double textDouble = Double.parseDouble(text);
                            // Convert double value obtained from text to integer value that is compatible with Scale
                            int value = double2Scale(textDouble);

                            /**
                             * If a value greater/smaller than maximum/minimum bound is given, the text is set to
                             * maximum/minimum bound. Scale is set to maximum/minimum value by default.
                             */
                            if (textDouble < minBound)
                            {
                                textMin.setText(minBound + "");
                            }
                            if (textDouble > maxBound)
                            {
                                textMin.setText(maxBound + "");
                            }

                            // If value given is greater than upper bound value, it is set to the upper bound value rather
                            // than the specified value.
                            if (value > scaleMax.getSelection() && buttonMax.getSelection())
                            {
                                scaleMin.setSelection(scaleMax.getSelection());
                                textMin.setText(textMax.getText());
                            }
                            else
                            {
                                scaleMin.setSelection(value);
                            }
                        }
                    }
                }
                catch(NumberFormatException e)
                {
                    MessageDialog.openError(shell, "Invalid input!", "Please enter a valid number.");
                }
            }
        });

        scaleMin = new Scale(scaleGroup, SWT.HORIZONTAL);
        gridData = new GridData(SWT.CENTER,SWT.CENTER,false,false);
        gridData.widthHint = 200;
        scaleMin.setLayoutData(gridData);
        scaleMin.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                // Convert integer Scale value to double and update text with this double value
                textMin.setText(scale2Double(scaleMin.getSelection()) + "");

                // Prevent lower bound value from becoming greater than the upper bound value
                if(scaleMax.getSelection() < scaleMin.getSelection() && buttonMax.getSelection())
                {
                    scaleMax.setSelection(scaleMin.getSelection());
                    textMax.setText(textMin.getText());
                }
            }
        });
        scaleMin.setMaximum(1000);
        scaleMin.setMinimum(0);
        scaleMin.setIncrement(1);
        scaleMin.setPageIncrement(100);

        buttonMax = new Button(scaleGroup, SWT.CHECK | SWT.WRAP);
        gridData = new GridData(SWT.CENTER,SWT.CENTER,false,false);
        buttonMax.setLayoutData(gridData);
        buttonMax.setText("Max");
        buttonMax.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent event)
            {
                // Enable text and scale only if button is selected
                textMax.setEnabled(buttonMax.getSelection());
                scaleMax.setEnabled(buttonMax.getSelection());

                // Ensures maximum value is greater than minimum value after maximum button's selection is toggled true.
                if (buttonMax.getSelection() && scaleMax.getSelection() < scaleMin.getSelection())
                {
                    scaleMax.setSelection(scaleMin.getSelection());
                    textMax.setText(textMin.getText());
                }

                // Range selection is only offered when both min and max are selected and for other cases rangeType is
                // always true to ensure that only min and only max selections are working properly.

                if (!buttonMax.getSelection() || !buttonMin.getSelection())
                {
                    outsideButton.setEnabled(false);
                    withinButton.setEnabled(false);
                    rangeType = true;
                }
                else
                {
                    outsideButton.setEnabled(true);
                    withinButton.setEnabled(true);
                    rangeType = withinButton.getSelection();
                }
            }
        });

        textMax = new Text(scaleGroup, SWT.BORDER | SWT.SINGLE);
        gridData = new GridData(SWT.BEGINNING,SWT.CENTER,false,false);
        gridData.widthHint = 60;
        textMax.setLayoutData(gridData);
        textMax.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent event)
            {
                try
                {
                    int code = event.keyCode;

                    // enter pressed
                    if (code == SWT.CR)
                    {
                        String text = ((Text)event.widget).getText();
                        if (!text.isEmpty())
                        {
                            Double textDouble = Double.parseDouble(text);
                            // Convert double value obtained from text to integer value that is compatible with Scale
                            int value = double2Scale(textDouble);

                            /**
                             * If a value greater/smaller than maximum/minimum bound is given, the text is set to
                             * maximum/minimum bound. Scale is set to maximum/minimum value by default.
                             */
                            if (textDouble < minBound)
                            {
                                textMax.setText(minBound+"");
                            }
                            if (textDouble > maxBound)
                            {
                                textMax.setText(maxBound+"");
                            }

                            // If value given is smaller than lower bound value, it is set to the lower bound value rather
                            // than the specified value.
                            if (value < scaleMin.getSelection() && buttonMin.getSelection())
                            {
                                scaleMax.setSelection(scaleMin.getSelection());
                                textMax.setText(textMin.getText());
                            }
                            else
                            {
                                scaleMax.setSelection(value);
                            }
                        }
                    }
                }
                catch (NumberFormatException e)
                {
                    MessageDialog.openError(shell, "Invalid input!", "Please enter a valid number.");
                }
            }
        });

        scaleMax = new Scale(scaleGroup, SWT.HORIZONTAL);
        gridData = new GridData(SWT.CENTER,SWT.CENTER,false,false);
        gridData.widthHint = 200;
        scaleMax.setLayoutData(gridData);
        scaleMax.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                // Convert integer Scale value to double and update text with this double value
                textMax.setText(scale2Double(scaleMax.getSelection()) + "");

                // Prevent upper bound value from becoming smaller than the lower bound value
                if (scaleMax.getSelection() < scaleMin.getSelection() && buttonMin.getSelection())
                {
                    scaleMin.setSelection(scaleMax.getSelection());
                    textMin.setText(textMax.getText());
                }
            }
        });
        scaleMax.setMaximum(1000);
        scaleMax.setMinimum(0);
        scaleMax.setIncrement(1);
        scaleMax.setPageIncrement(100);

        Group rangeGroup = new Group(shell, SWT.NONE);
        gridData = new GridData(SWT.END, SWT.BEGINNING, false, false);
        gridData.horizontalSpan = 1;
        rangeGroup.setLayoutData(gridData);
        rangeGroup.setLayout(new GridLayout(1,true));

        withinButton = new Button(rangeGroup, SWT.RADIO);
        gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        withinButton.setLayoutData(gridData);
        withinButton.setText("Within specified range");
        withinButton.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent event)
            {
                rangeType = withinButton.getSelection();
            }
        });

        outsideButton = new Button(rangeGroup, SWT.RADIO);
        gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        outsideButton.setLayoutData(gridData);
        outsideButton.setText("Outside specified range");

        Group okCancelGroup = new Group(shell,SWT.NONE);
        gridData = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        gridData.horizontalSpan = 4;
        okCancelGroup.setLayoutData(gridData);
        okCancelGroup.setLayout(new GridLayout(2,true));

        okButton = new Button(okCancelGroup, SWT.NONE);
        okButton.setText("OK");
        gridData = new GridData(SWT.END, SWT.CENTER, true, false);
        okButton.setLayoutData(gridData);
        okButton.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent event)
            {
                if ((!buttonMax.getSelection() && !buttonMin.getSelection())
                    || (textMax.getText().isEmpty() && textMin.getText().isEmpty()))
                {
                    MessageDialog.openWarning(shell, "No Data", "Choose at least one boundary.");
                    return;
                }

                if (buttonMax.getSelection() && !textMax.getText().isEmpty())
                {
                    maxResult = Double.parseDouble(textMax.getText());
                }
                if (buttonMin.getSelection() && !textMin.getText().isEmpty())
                {
                    minResult = Double.parseDouble(textMin.getText());
                }

                resultArray[0] = minResult;
                resultArray[1] = maxResult;

                okPressed = true;

                storeSelections();
                
                shell.close();
            }
        });


        cancelButton = new Button(okCancelGroup, SWT.NONE);
        cancelButton.setText("Cancel");
        gridData = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
        cancelButton.setLayoutData(gridData);
        cancelButton.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent event)
            {
                shell.close();
            }
        });

        // If new data is loaded, start from scratch. Otherwise, use previous selections for initialization.
        if(newData)
        {
            setInitialValues();
        }
        else
        {
            useCurrentSelections();
        }
    }

    // Initial values for the new dialog
    private void setInitialValues()
    {
        buttonMin.setSelection(true);
        buttonMax.setSelection(true);
        withinButton.setSelection(true);

        scaleMin.setSelection(scaleMin.getMinimum());
        scaleMax.setSelection(scaleMax.getMaximum());

        textMin.setText(minBound + "");
        textMax.setText(maxBound + "");
    }

    // Store user's particular selections in order to be used later
    private void storeSelections()
    {
        currentBoolean[0] = buttonMin.getSelection();
        currentBoolean[1] = buttonMax.getSelection();
        currentBoolean[2] = withinButton.getSelection();

        currentInt[0] = scaleMin.getSelection();
        currentInt[1] = scaleMax.getSelection();

        currentString[0] = textMin.getText();
        currentString[1] = textMax.getText();
    }

    // Initialize the dialog exactly same as the previous one
    private void useCurrentSelections()
    {
        buttonMin.setSelection(currentBoolean[0]);
        buttonMax.setSelection(currentBoolean[1]);

        withinButton.setSelection(currentBoolean[2]);
        outsideButton.setSelection(!currentBoolean[2]);
        // Enable if only both min and max are selected        
        withinButton.setEnabled(currentBoolean[0] && currentBoolean[1]);
        outsideButton.setEnabled(currentBoolean[0] && currentBoolean[1]);

        // Ensure rangeType is properly set according to enabled state of min/max buttons and withinButton selection
        rangeType = (!currentBoolean[0] || !currentBoolean[1]) ? true : currentBoolean[2];

        scaleMin.setSelection(currentInt[0]);
        scaleMin.setEnabled(currentBoolean[0]);
        scaleMax.setSelection(currentInt[1]);
        scaleMax.setEnabled(currentBoolean[1]);

        textMin.setText(currentString[0]);
        textMin.setEnabled(currentBoolean[0]);
        textMax.setText(currentString[1]);
        textMax.setEnabled(currentBoolean[1]);
    }

    /**
     * Convert double values of texts to integer values to make them compatible with Scale
     *
     */
    private int double2Scale(double actual)
    {
        double factor = 1000 / (this.maxBound - this.minBound);

        double scale = (actual - this.minBound) * factor;

        return (int)scale;
    }

    /**
     * Convert integer values of Scale to double values that will be shown in texts
     *
     */
    private double scale2Double(int scale)
    {
        double factor = (this.maxBound - this.minBound) / 1000;

        double actual = this.minBound + scale * factor;

        // Show only two decimal points
        double formatted = Double.parseDouble(new DecimalFormat("#.##").format(actual));

        return formatted;
    }
    
    public double[] getResultArray()
    {
        return resultArray;
    }

    public boolean getRangeType()
    {
        return rangeType;
    }
}
