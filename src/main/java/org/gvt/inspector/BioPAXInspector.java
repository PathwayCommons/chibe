/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.gvt.inspector;

/*
 * Composite example snippet: create and dispose children of a composite
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 */

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;


public class BioPAXInspector {

    static Composite pageComposite;

    public static void main(String args[]) {
        Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setLayout(new GridLayout());
        Button button = new Button(shell, SWT.PUSH);
        button.setText("Push");
        pageComposite = new Composite(shell, SWT.NONE);
        pageComposite.setLayout(new GridLayout());
        pageComposite.setLayoutData(new GridData());

        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if ((pageComposite != null) && (!pageComposite.isDisposed())) {
                    pageComposite.dispose();
                }
                pageComposite = new Composite(shell, SWT.NONE);
                pageComposite.setLayout(new GridLayout());
                GridData gridData = new GridData();
                gridData.horizontalAlignment = GridData.FILL;
                gridData.grabExcessHorizontalSpace = true;
                pageComposite.setLayoutData(gridData);

                Canvas canvas = new Canvas(pageComposite, SWT.BORDER);
                canvas.setVisible(false);


                Table table = new Table(pageComposite, SWT.BORDER);
                table.setLayoutData(new GridData());
                for (int i = 0; i < 5; i++) {
                    new TableItem(table, SWT.NONE).setText("table item " + i);
                }

                shell.layout(true);
            }
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}
