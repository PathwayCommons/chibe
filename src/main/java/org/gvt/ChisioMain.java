package org.gvt;

import cpath.client.PathwayCommonsIOHandler;
import org.biopax.paxtools.controller.Cloner;
import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.cbio.causality.data.portal.CBioPortalAccessor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.*;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.gvt.action.*;
import org.gvt.editpart.ChsEditPartFactory;
import org.gvt.editpart.ChsNodeEditPart;
import org.gvt.editpart.ChsRootEditPart;
import org.gvt.editpart.ChsScalableRootEditPart;
import org.gvt.figure.HighlightLayer;
import org.gvt.model.*;
import org.gvt.model.basicsif.BasicSIFGraph;
import org.gvt.model.biopaxl2.Actor;
import org.gvt.model.biopaxl2.BioPAXL2Graph;
import org.gvt.model.biopaxl2.Complex;
import org.gvt.model.biopaxl2.Conversion;
import org.gvt.model.biopaxl3.BioPAXL3Graph;
import org.gvt.model.sifl2.SIFGraph;
import org.gvt.util.*;
import org.patika.mada.dataXML.ChisioExperimentData;
import org.patika.mada.util.ExperimentDataManager;

import javax.swing.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * This class maintains the main function for this application. Chisio is a
 * compound graph editor with support for various types of layout algorithms.
 *
 * @author Cihan Kucukkececi
 * @author Ozgun Babur
 *
 * Copyright: Bilkent Center for Bioinformatics, 2007 - present
 */
public class ChisioMain extends ApplicationWindow
	implements MouseListener, KeyListener, MouseTrackListener
{
	private CTabFolder tabFolder;
	private PopupManager popupManager;
	private KeyHandler keyHandler;

	private Set<String> openTabNames;

	private Map<CTabItem, ScrollingGraphicalViewer> tabToViewerMap;
	private Map<String, CTabItem> nameToTabMap;

	public static boolean transferNode = false;

	public static Color higlightColor;

	// true if polygon is should be shown for each cluster
	public boolean isClusterBoundShown;

	public static Combo zoomCombo;

	private String owlFileName;

	public Point clickLocation;

	private Map<String, ExperimentDataManager> dataManagerMap;

	private Shell lockShell;

	/**
	 * This is the BioPAX model to visualize its pathways.
	 */
	private Model rootBioPAXModel;

	/**
	 * Used to undeerstand if there is a modification to the biopax file which is not captured by
	 * views' command stakes.
	 */
	private boolean dirty;

	private org.eclipse.jface.action.Action firstAction, secondAction;

    // Used for fetching data from cBio Portal
    public static CBioPortalAccessor cBioPortalAccessor;

    public ChisioMain()
	{
		super(null);
//		createChangeModeAction();
		this.dataManagerMap = new HashMap<String, ExperimentDataManager>();
		this.isClusterBoundShown = false;
	}

	protected void handleShellCloseEvent()
	{
		if (LoadBioPaxModelAction.saveChangesBeforeDiscard(this))
		{
			super.handleShellCloseEvent();
			Shell[] inspectors = Display.getDefault().getShells();
			int size = inspectors.length;

			for(int i = 0; i < size ; i++)
			{
				Shell current = inspectors[i];

				if(current.getText().indexOf("Properties") > 0)
				{
					current.close();
				}
			}
		}
	}
	
	/*	UK: A side effect of the added functionality is the added risk for exceptions. I am not sure if there's a specific way
	 * 	these should be handled by try/catch blocks, from my perspective if an IO or URISyntax Exception occurs, disturbing the 
	 * 	proper loading of a pathway, the execution could just as well be quit, to report and hopefully handle the problem. 
	 */
	public static void main(String[] args) throws InterruptedException, URISyntaxException, IOException	{
		ChisioMain window = new ChisioMain();
		window.setBlockOnOpen(true);
		window.addMenuBar();
		window.addToolBar(SWT.FLAT  | SWT.RIGHT);

		if (args.length > 0)
		{
			String filename = args[0];

			if (filename.endsWith(".owl"))
			{
				LoadBioPaxModelAction action = new LoadBioPaxModelAction(window, filename);
				window.firstAction = action;
			}
			/*	UK: If a CPATH_ID is given instead of a OWL file, look up PathwayCommons Database and acquire pathway
			 * 	This is particularly useful when giving the users a long list of pathways and letting them choose which pathway to 
			 * 	visualize on the spot, which can be very useful in JWS deployment.	*/
			else if (filename.matches("\\d*")){
				try {
					BioPAXIOHandler reader = new SimpleIOHandler();
					PathwayCommonsIOHandler pcIOHandler	= new PathwayCommonsIOHandler(reader);
					Model model = pcIOHandler.retrieveByID(filename);
					LoadBioPaxModelAction action = new LoadBioPaxModelAction(window, model);
					window.firstAction = action;
					
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Cannot load the specified pathway; CPATH_ID:"+filename);
				}
			}
		}
		
		/* 	UK: if a second argument is supplied (path to a .ced file) take the file and do a LoadExperimentDataAction
		 * 	This is a nice feature which saves user the trouble to create and load a CED file. As it's optional it doesn't change any 
		 * 	existing functionality.	*/
		if(args.length > 1){
			String cedFilename;
			cedFilename = args[1];
			LoadExperimentDataAction ledAction;
			if (cedFilename.startsWith("http")){
				URL cedUrl = new URL(cedFilename);
				ledAction = new LoadExperimentDataAction(window, cedUrl);
				
			}
			else{
				ledAction = new LoadExperimentDataAction(window,cedFilename);
			}
			window.secondAction = ledAction;
		}

		window.open();

		Display.getCurrent().dispose();
	}

	public Dimension getCurrentSize()
	{
		int w = getShell().getSize().x;
		int h = getShell().getSize().y;
		return new Dimension(w, h);
	}

	protected Control createContents(Composite parent)
	{
		getShell().setText(TOOL_NAME);
		getShell().setSize(800, 600);
		this.getShell().setImage(
			ImageDescriptor.createFromFile(getClass(), "icon/cbe-icon.png").createImage());

		Composite compositeRoot = new Composite(parent, SWT.BORDER);
		compositeRoot.setLayout(new FillLayout());

		tabFolder = new CTabFolder(compositeRoot, SWT.NONE);
		tabFolder.setSimple(false);

		tabFolder.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				if (getViewer() != null)
				{
					updateCombo(((ChsScalableRootEditPart) getViewer().getRootEditPart()).
						getZoomManager().getZoomAsText());
				}
			}
		});

		this.openTabNames = new HashSet<String>();
		this.nameToTabMap = new HashMap<String, CTabItem>();

		this.tabToViewerMap = new HashMap<CTabItem, ScrollingGraphicalViewer>();

		popupManager = new PopupManager(this);
		popupManager.setRemoveAllWhenShown(true);
		popupManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				popupManager.createActions(manager);
			}
		});

		keyHandler = new KeyHandler();
		ActionRegistry a = new ActionRegistry();
		keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
			new DeleteAction(this));

		keyHandler.put(KeyStroke.getPressed('+',SWT.KEYPAD_ADD, 0),
			new ZoomAction(this,1,null));

		keyHandler.put(KeyStroke.getPressed('-',SWT.KEYPAD_SUBTRACT, 0),
			new ZoomAction(this,-1,null));

		/*keyHandler.put(KeyStroke.getPressed(SWT.CTRL, 0),
			new ZoomAction(this,-1,null));*/

		keyHandler.put(KeyStroke.getPressed(SWT.F2, 0), a
			.getAction(GEFActionConstants.DIRECT_EDIT));

		this.higlightColor = ColorConstants.yellow;

		createCombos();

		if (firstAction != null) firstAction.run();
		if (secondAction != null) secondAction.run();

		return compositeRoot;
	}

	public CTabItem createNewTab(CompoundModel root)
	{
		assert root != null;

		root.setAsRoot();

		String name;

		if (root instanceof BioPAXGraph)
		{
			BioPAXGraph graph = (BioPAXGraph) root;

			graph.setName(graph.getPathway() != null ? graph.getPathway().getName() :
				adviceTabName(graph.getName()));

			name = graph.getName();
		}
		else
		{
			String nameCand = root.getText();
			name = adviceTabName(nameCand.trim().length() > 0 ? nameCand : null);
		}

		if (openTabNames.contains(name))
		{
			return null;
		}

		CTabItem tab = new CTabItem(tabFolder, SWT.NONE);

		if (root instanceof SIFGraph)
		{
			ImageDescriptor id = ImageDescriptor.createFromFile(
				getClass(), "icon/sif.png");

			tab.setImage(id.createImage());
		}
				
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new FillLayout());

		tab.setControl(composite);

		ScrollingGraphicalViewer viewer = new ScrollingGraphicalViewer();
		viewer.setEditDomain(new EditDomain());
		viewer.createControl(composite);
		viewer.getControl().setBackground(ColorConstants.white);
		RootEditPart rootEditPart = new ChsScalableRootEditPart();
		viewer.setRootEditPart(rootEditPart);
		viewer.setEditPartFactory(new ChsEditPartFactory());

		((FigureCanvas)viewer.getControl()).setScrollBarVisibility(FigureCanvas.ALWAYS);
		// DropTargetListener
		viewer.addDropTargetListener(new ChsFileDropTargetListener(viewer, this));
		// DragSourceListener
		viewer.addDragSourceListener(new ChsFileDragSourceListener(viewer));

		tab.setText(name);

		viewer.setContents(root);
		viewer.getControl().addMouseListener(this);
		viewer.setKeyHandler(keyHandler);

		this.tabToViewerMap.put(tab, viewer);
		this.setSelectedTab(tab);

		this.openTabNames.add(name);
		this.nameToTabMap.put(name, tab);

		return tab;
	}
	
	/**
	 * 	Advices a tab name that is derived from the parameter name and not exists.
	 */
	public String adviceTabName(String candidate)
	{
		String name = candidate != null ? candidate :
			"Tab - " + Long.toString(System.currentTimeMillis()).substring(8);

		int i = 2;

		Set<String> existing = getAllPathwayNames();
		while (existing.contains(name))
		{
			name = candidate + " (" + (i++) + ")";
		}
		return name;
	}

	public CTabItem getSelectedTab()
	{
		if (this.tabFolder != null && this.tabFolder.getItemCount() > 0)
		{
			return this.tabFolder.getSelection();
		}
		return null;
	}

	public void setSelectedTab(CTabItem tab)
	{
		this.tabFolder.setSelection(tab);
	}

	public void closeTab(String tabName, boolean remmeberLayout)
	{
		assert openTabNames.contains(tabName) : "Tab close request with an unknown name:" + tabName;
		assert nameToTabMap.containsKey(tabName) : "Tab name not known: " + tabName;

		CTabItem tab = nameToTabMap.get(tabName);
		closeTab(tab, remmeberLayout);
	}

	public void closeTabIfNotBasicSIF(String tabName, boolean remmeberLayout)
	{
		assert openTabNames.contains(tabName) : "Tab close request with an unknown name:" + tabName;
		assert nameToTabMap.containsKey(tabName) : "Tab name not known: " + tabName;

		CTabItem tab = nameToTabMap.get(tabName);

		ScrollingGraphicalViewer viewer = tabToViewerMap.get(tab);
		CompoundModel root = (CompoundModel) viewer.getContents().getModel();
		if (root instanceof BasicSIFGraph) return;
		
		closeTab(tab, remmeberLayout);
	}

	public void closeTab(CTabItem tab, boolean rememberLayout)
	{
		ScrollingGraphicalViewer viewer = tabToViewerMap.get(tab);
		CompoundModel root = (CompoundModel) viewer.getContents().getModel();

		if (root instanceof BioPAXGraph)
		{
			BioPAXGraph graph = (BioPAXGraph) root;

			assert graph.getName().equals(tab.getText()) :
				"graph name: " + graph.getName() + " tab name: " + tab.getText();

			if (rememberLayout && graph.isMechanistic())
			{
				graph.recordLayout();
			}
		}

		boolean removed = openTabNames.remove(tab.getText());

		assert removed : "tab name: " + tab.getText();

		ScrollingGraphicalViewer v = tabToViewerMap.remove(tab);

		assert v != null;

		CTabItem t = nameToTabMap.remove(tab.getText());
		
		assert t != null;

		tab.dispose();
	}

	public void closeAllTabs(boolean rememberLayout)
	{
		for (CTabItem tab : tabFolder.getItems())
		{
			closeTab(tab, rememberLayout);
		}
		assert tabFolder.getItemCount() == 0;
		assert tabToViewerMap.size() == 0;
		assert nameToTabMap.size() == 0;
		assert openTabNames.size() == 0;
	}

	public Map<CTabItem, ScrollingGraphicalViewer> getTabToViewerMap()
	{
		return tabToViewerMap;
	}

	public ScrollingGraphicalViewer getViewer()
	{
		CTabItem tab = getSelectedTab();
		if (tab != null)
		{
			return this.tabToViewerMap.get(tab);
		}
		return null;
	}

	public Set<String> getOpenTabNames()
	{
		return openTabNames;
	}

	public Set<String> getAllPathwayNames()
	{
		Set<String> names = new HashSet<String>();
		for (CTabItem cTabItem : tabToViewerMap.keySet())
		{
			names.add(cTabItem.getText());
		}
		names.addAll(BioPAXUtil.getPathwayNames(rootBioPAXModel));
		return names;
	}

	public void renamePathway(CTabItem tab, String newName)
	{
		nameToTabMap.remove(tab.getText());
		openTabNames.remove(tab.getText());

		nameToTabMap.put(newName, tab);
		openTabNames.add(newName);

		Object compmod = tabToViewerMap.get(tab).getContents().getModel();
		if (compmod instanceof BioPAXGraph)
		{
			BioPAXGraph graph = (BioPAXGraph) compmod;
			graph.setName(newName);
		}
		tab.setText(newName);

		assert nameToTabMap.size() == openTabNames.size();
	}


	/**
	 * Checks if any view is dirty, i.e. needs to saved to be persistent.
	 * @return true if at least one view is dirty
	 */
	public boolean isDirty()
	{
		if (this.dirty)
		{
			return true;
		}

		for (ScrollingGraphicalViewer viewer : tabToViewerMap.values())
		{
			if (viewer.getEditDomain().getCommandStack().isDirty())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Marks the command stacks of edit domains of views as saved at the current location. This save
	 * location is used for understanding if the view is dirty.
	 */
	public void markSaved()
	{
		this.dirty = false;

		for (ScrollingGraphicalViewer viewer : tabToViewerMap.values())
		{
			viewer.getEditDomain().getCommandStack().markSaveLocation();
		}
	}

	public void makeDirty()
	{
		this.dirty = true;
	}

	/**
	 * Locks the application window, showing a message in the middle. This is useful while doing
	 * time consuming operations. So the user has an idea why they wait.
	 * @param msg message about the task being performed
	 */
	public void lockWithMessage(String msg)
	{
		System.out.println(msg);
		unlock();
		lockShell = new Shell(this.getShell(), SWT.APPLICATION_MODAL);
		lockShell.setLocation(getShell().getLocation());
		lockShell.setLayout(new GridLayout());
		StyledText txt = new StyledText(lockShell, SWT.SINGLE);
		txt.setEditable(false);
		FontData fd = txt.getFont().getFontData()[0];
		fd.setHeight(fd.getHeight() + 2);
		fd.setStyle(SWT.BOLD);
		Font font = new Font(null, new FontData[]{fd});
		txt.setFont(font);
		txt.setText(msg);

		Dimension extents = TextUtilities.INSTANCE.getStringExtents(msg, font);

		int buffer = 15;

		GridData data = new GridData();
		data.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
		data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_CENTER;
		data.widthHint = extents.width + buffer;
		data.heightHint = extents.height + buffer;
		data.horizontalIndent = buffer;
		data.verticalIndent = buffer;
		txt.setLayoutData(data);

		Color prgrssBgColor = new Color(null, 255, 247, 240);
		Color fgcolor = new Color(null, 90, 100, 90);
		lockShell.setBackground(prgrssBgColor);
		txt.setBackground(prgrssBgColor);
		txt.setForeground(fgcolor);
		txt.getCaret().setVisible(false);
		lockShell.pack();

		// Icrease height a little bit to get rid of the scrollbar in mac
//		lockShell.setSize(lockShell.getSize().x, (int) (lockShell.getSize().y * 2));

		lockShell.setLocation(
			lockShell.getLocation().x +
				(getShell().getBounds().width / 2) -
				(msg.length() * fd.getHeight() / 2),
			lockShell.getLocation().y + (getShell().getBounds().height / 2) - 50);

		lockShell.open();

		// Below code is needed to make the contents of the lock message visible in Linux

		Display display = getShell().getDisplay();
		long time = System.currentTimeMillis();

		while (System.currentTimeMillis() - time < 50)
		{
			if (!display.readAndDispatch()) display.sleep();
		}
	}

	/**
	 * Removes any message and unlocks the application.
	 */
	public void unlock()
	{
		if (lockShell != null)
		{
			lockShell.dispose();
			lockShell = null;
		}
	}

	public boolean isLevel2()
	{
		return getBioPAXModel() != null && getBioPAXModel().getLevel() == BioPAXLevel.L2;
	}

	public boolean isLevel3()
	{
		return getBioPAXModel() != null && getBioPAXModel().getLevel() == BioPAXLevel.L3;
	}

	protected MenuManager createMenuManager()
	{
		return TopMenuBar.createBarMenu(this);
	}

	protected ToolBarManager createToolBarManager(int style)
	{
		return new ToolbarManager(style, this);
	}

	public void mouseDoubleClick(MouseEvent e)
	{
		InspectorAction inspectorAction = new InspectorAction(this, false);
		inspectorAction.run();
	}

	public void mouseDown(MouseEvent e)
	{
		clickLocation = new Point(e.x,e.y);
		popupManager.setClickLocation(clickLocation);
		getViewer().getControl().setMenu(
			popupManager.createContextMenu(getViewer().getControl()));
	}

	public void mouseUp(MouseEvent e)
	{
		clickLocation = new Point(e.x,e.y);
	}

	public void keyPressed(KeyEvent e)
	{
		transferNode = !transferNode;
		JOptionPane.showMessageDialog(null, "Transfer Mode: " + transferNode);
	}

	public void keyReleased(KeyEvent e)
	{

	}

	public void mouseEnter(MouseEvent e)
	{
	}

	public void mouseExit(MouseEvent e)
	{
	}

	public void mouseHover(MouseEvent e)
	{
	}

	public EditDomain getEditDomain()
	{
		ScrollingGraphicalViewer viewer = getViewer();
		if (viewer != null)
		{
			return viewer.getEditDomain();
		}
		return null;
	}

	public void createCombos()
	{
		ToolBar toolbar =  this.getToolBarManager().getControl();

		ToolItem item = new ToolItem(toolbar, SWT.SEPARATOR, 15);

		zoomCombo = new Combo(toolbar, SWT.NONE);
		zoomCombo.add("2000%");
		zoomCombo.add("1000%");
		zoomCombo.add("500%");
		zoomCombo.add("150%");
		zoomCombo.add("100%");
		zoomCombo.add("75%");
		zoomCombo.add("50%");
		zoomCombo.add("25%");
		zoomCombo.pack();
		item.setWidth(zoomCombo.getSize().x);
		item.setControl(zoomCombo);

		zoomCombo.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent keyEvent)
			{
				// When ENTER is pressed, zoom to given level
				if(keyEvent.keyCode == 13)
				{
					if (getViewer() != null)
					{
						((ChsScalableRootEditPart)getViewer().getRootEditPart()).
							getZoomManager().setZoomAsText(zoomCombo.getText());
					}
				}
			}

			public void keyReleased(KeyEvent keyEvent)
			{
				// nothing
			}
		});

		zoomCombo.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				if (getViewer() != null)
				{
					((ChsScalableRootEditPart) getViewer().getRootEditPart()).
						getZoomManager().setZoomAsText(zoomCombo.getText());
				}
			}
		});

		if (getViewer() != null)
		{
			updateCombo(((ChsScalableRootEditPart) getViewer().getRootEditPart()).
				getZoomManager().getZoomAsText());
		}
	}

	public static void updateCombo(String newValue)
	{
		zoomCombo.setText(newValue);
		zoomCombo.update();
	}

	public  void setOwlFileName(String filename)
	{
		this.owlFileName = filename;

		String suffix = "";
		Model model = getBioPAXModel();
		if (model != null)
		{
			suffix = " (Level " + (model.getLevel() == BioPAXLevel.L3 ? "3" : "2") + ")";
		}
		suffix += " - " + TOOL_NAME;

		if (filename != null)
		{
			this.getShell().setText(filename + suffix);
		}
		else
		{
			this.getShell().setText(suffix);
		}
	}

	public String getOwlFileName()
	{
		return owlFileName;
	}

	public HighlightLayer getHighlightLayer()
	{
		HighlightLayer highlight = (HighlightLayer)
			((ChsScalableRootEditPart) getViewer().getRootEditPart()).
			getLayer(HighlightLayer.HIGHLIGHT_LAYER);

		return highlight;
	}

	public Layer getHandleLayer()
	{
		Layer handle = (Layer) ((ChsScalableRootEditPart) getViewer().getRootEditPart()).
			getLayer(LayerConstants.HANDLE_LAYER);

		return handle;
	}

	public java.util.List<GraphObject> getSelectedModel()
	{
		if (getViewer() == null) return null;

		java.util.List<GraphObject> models = new ArrayList<GraphObject>();
		java.util.List parts = ((IStructuredSelection) getViewer().getSelection()).toList();

		for (Object partObj : parts)
		{
			EditPart ep = (EditPart) partObj;
			models.add((GraphObject) ep.getModel());
		}

		return models;
	}

	public Set<BioPAXElement> getSelectedBioPAXElements()
	{
		Set<BioPAXElement> set = new HashSet<BioPAXElement>();
		for (GraphObject go : getSelectedModel())
		{
			if (go instanceof EntityAssociated)
			{
				set.add (((EntityAssociated) go).getEntity().getEntity());
			}
		}
		return set;
	}

	public BioPAXGraph createAModelForView(PathwayHolder p)
	{
		if (p.l3p != null)
		{
			return new BioPAXL3Graph(rootBioPAXModel, p.l3p);
		}
		else if (p.l2p != null)
		{
			return new BioPAXL2Graph(rootBioPAXModel, p.l2p);
		}
		return null;
	}


	//----------------------------------------------------------------------------------------------
	// Section: BioPAX related methods
	//----------------------------------------------------------------------------------------------

	public Model getBioPAXModel()
	{
		return this.rootBioPAXModel;
	}

	public void setBioPAXModel(Model model)
	{
		this.rootBioPAXModel = model;
	}

	public BioPAXGraph getPathwayGraph()
	{
		ScrollingGraphicalViewer viewer = getViewer();

		if (viewer != null)
		{
			Object o = viewer.getContents().getModel();

			if (o instanceof BioPAXGraph)
			{
				return (BioPAXGraph) o;
			}
		}

		return null;
	}

	public List<BioPAXGraph> getAllPathwayGraphs()
	{
		List<BioPAXGraph> list = new ArrayList<BioPAXGraph>();

		for (CTabItem tab : tabFolder.getItems())
		{
			ScrollingGraphicalViewer viewer = tabToViewerMap.get(tab);

			if (viewer != null)
			{
				Object o = viewer.getContents().getModel();

				if (o instanceof BioPAXGraph)
				{
					list.add((BioPAXGraph) o);
				}
			}
		}

		return list;
	}


    /**
     * A method to highlight graph objects in the current model
     * according to a collection of Rdf Ids.
     * @param ids rdfs of the objects to be highlighted as a collecton of string
     */
    public void highlightIDs(Collection<String> ids)
    {
		BioPAXGraph graph = getPathwayGraph();
		if (graph == null) return;

		for (Object o : graph.getGraphObjects())
		{
			if (o instanceof EntityAssociated)
			{
				EntityAssociated ea = (EntityAssociated) o;
				if (ea.getEntity() != null && ids.contains(ea.getEntity().getID()))
				{
					((GraphObject) o).setHighlightColor(higlightColor);
					((GraphObject) o).setHighlight(true);
				}
			}
		}
    } // End of method

	/**
	 * Collects IDs of the ubique molecules in the current model.
	 */
	public Blacklist collectUbiqueIDs()
	{
		return org.gvt.model.biopaxl3.Actor.getBlackList();
	}

    //----------------------------------------------------------------------------------------------
	// Section: Experiment data related methods
	//----------------------------------------------------------------------------------------------

	public boolean hasExperimentData(String type)
	{
		return dataManagerMap.containsKey(type) && dataManagerMap.get(type).isDataAvailable();
	}


	public void setExperimentData(ChisioExperimentData data, String fileLocation)
	{
		String type = data.getExperimentType();

		ExperimentDataManager man = new ExperimentDataManager(data, fileLocation);
		dataManagerMap.put(type, man);

		for (ScrollingGraphicalViewer viewer : tabToViewerMap.values())
		{
			Object model = viewer.getContents().getModel();
			if (model instanceof BioPAXGraph)
			{
				BioPAXGraph graph = (BioPAXGraph) model;
				man.clearExperimentData(graph);
				man.associateExperimentData(graph);
			}
		}
	}

	public Set<String> getLoadedExperimentTypes()
	{
		return dataManagerMap.keySet();
	}

	public ExperimentDataManager getExperimentDataManager(String type)
	{
		return dataManagerMap.get(type);
	}

	public ChisioExperimentData getExperimentData(String type)
	{
		return dataManagerMap.get(type).getCed();
	}

	public void associateGraphWithExperimentData()
	{
		associateGraphWithExperimentData(this.getPathwayGraph());
	}

	public void associateGraphWithExperimentData(BioPAXGraph graph)
	{
		if (graph == null || dataManagerMap.keySet().isEmpty())
		{
			return;
		}

		for (String type : dataManagerMap.keySet())
		{
			ExperimentDataManager man = dataManagerMap.get(type);
			man.associateExperimentData(graph);
		}

		String type = graph.getLastAppliedColoring();
		if (type != null)
		{
			graph.setLastAppliedColoring(null);
			new ColorWithExperimentAction(this, graph, type).run();
		}
	}

	// -------------------------------------------------------------------------
	// Class Constants
	// -------------------------------------------------------------------------

	public final static String TOOL_NAME = "Chisio BioPAX Editor";

	/**
	 * Used for preventing antialiasing and transparent colors in non-windows 
	 * systems. During packaging this variable should be manually set.
	 */
//	public static boolean runningOnWindows = true;

	// Following expression can be used for automatic detection of operating
	// system. However we don't use it for safety.

	public static boolean runningOnWindows = System.getProperty("os.name").startsWith("Windows");
}