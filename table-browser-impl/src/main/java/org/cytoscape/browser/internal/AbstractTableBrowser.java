package org.cytoscape.browser.internal;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.browser.internal.util.ColumnResizer;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.swing.DialogTaskManager;
import javax.swing.table.DefaultTableModel;


/**
 * Base class for all Table Browsers.
 *
 */
public abstract class AbstractTableBrowser extends JPanel implements CytoPanelComponent, ActionListener {

	private static final long serialVersionUID = 1968196123280466989L;
	
	static final Dimension SELECTOR_SIZE = new Dimension(400, 32);
	
	// Color theme for table browser.
	static final Color NETWORK_COLOR = new Color(0xA5, 0x2A, 0x2A);
	static final Color SELECTED_ITEM_BACKGROUND_COLOR = new Color(0xA0, 0xA0, 0xA0, 80);
	
	private static final Dimension PANEL_SIZE = new Dimension(550, 400);
	
	protected final CyTableManager tableManager;
	protected final CyServiceRegistrar serviceRegistrar;
	private final EquationCompiler compiler;
	
	protected AttributeBrowserToolBar attributeBrowserToolBar;
		
	protected CyTable currentTable;
	protected final CyApplicationManager applicationManager;
	protected final CyNetworkManager networkManager;
	private final OpenBrowser openBrowser;
	private final PopupMenuHelper popupMenuHelper; 
	private final CyEventHelper eventHelper;
	

	// Tab title for the CytoPanel
	private final String tabTitle;
	private final Map<BrowserTableModel,JScrollPane> scrollPanes;
	private final Map<CyTable,BrowserTableModel> browserTableModels;
	private JScrollPane currentScrollPane;
	
	AbstractTableBrowser(final String tabTitle,
			final CyTableManager tableManager, final CyNetworkTableManager networkTableManager,
			final CyServiceRegistrar serviceRegistrar, final EquationCompiler compiler, final OpenBrowser openBrowser,
			final CyNetworkManager networkManager, final TableTaskFactory deleteTableTaskFactoryService,
			final DialogTaskManager guiTaskManagerServiceRef, final PopupMenuHelper popupMenuHelper,
			final CyApplicationManager applicationManager, final CyEventHelper eventHelper) {
		this.tableManager = tableManager;
		this.serviceRegistrar = serviceRegistrar;
		this.compiler = compiler;
		this.tabTitle = tabTitle;
		this.networkManager = networkManager;
		this.applicationManager = applicationManager;
		this.openBrowser = openBrowser;
		this.popupMenuHelper = popupMenuHelper;
		this.eventHelper = eventHelper;

		this.scrollPanes = new HashMap<BrowserTableModel,JScrollPane>();
		this.browserTableModels = new HashMap<CyTable,BrowserTableModel>();
		this.currentScrollPane = null;
		
		this.setLayout(new BorderLayout());
		this.setPreferredSize(PANEL_SIZE);
		this.setSize(PANEL_SIZE);
	}

	/**
	 * Returns the Component to be added to the CytoPanel.
	 * @return The Component to be added to the CytoPanel.
	 */
	@Override
	public Component getComponent() { return this; }

	/**
	 * Returns the name of the CytoPanel that this component should be added to.
	 * @return the name of the CytoPanel that this component should be added to.
	 */
	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.SOUTH;
	}

	/**
	 * Returns the title of the tab within the CytoPanel for this component.
	 * @return the title of the tab within the CytoPanel for this component.
	 */
	@Override
	public String getTitle() { return tabTitle; }

	/**
	 * @return null
	 */
	@Override
	public Icon getIcon() { return null; }
	
	// Delete the given table from the JTable
	public void DeleteTable(CyTable cyTable){
		
		if (this.currentTable != cyTable){
			return;
		}

		browserTableModels.get(cyTable).getBrowserTable().setModel(new DefaultTableModel());				
		this.browserTableModels.remove(cyTable);
	}
	
	
	synchronized void showSelectedTable() {
		final BrowserTableModel currentBrowserTableModel = getCurrentBrowserTableModel();
		final JScrollPane newScrollPane = getScrollPane(currentBrowserTableModel);

		if (currentScrollPane != null)
			remove(currentScrollPane);

		add(newScrollPane, BorderLayout.CENTER);

		currentScrollPane = newScrollPane;
		applicationManager.setCurrentTable(currentTable);
		attributeBrowserToolBar.setBrowserTableModel(currentBrowserTableModel);
		
		// Resize column
		ColumnResizer.adjustColumnPreferredWidths(currentBrowserTableModel.getTable());
	}

	private JScrollPane getScrollPane(final BrowserTableModel browserTableModel) {
		JScrollPane scrollPane = scrollPanes.get(browserTableModel);
		
		if (scrollPane == null) {
			final BrowserTable browserTable = browserTableModel.getBrowserTable(); 
			serviceRegistrar.registerAllServices(browserTableModel, new Properties());
			browserTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			browserTable.getTableHeader().setBackground(Color.LIGHT_GRAY);
			browserTable.setUpdateComparators(false);
			browserTable.setModel(browserTableModel);
			
			final TableRowSorter<BrowserTableModel> rowSorter = new TableRowSorter<BrowserTableModel>(browserTableModel);
			browserTable.setRowSorter(rowSorter);
			updateColumnComparators(rowSorter, browserTableModel);
			browserTable.setUpdateComparators(true);
			
			scrollPane = new JScrollPane(browserTable);
			scrollPanes.put(browserTableModel,scrollPane);
		}

		return scrollPane;
	}

	protected BrowserTableModel getCurrentBrowserTableModel() {
		BrowserTableModel btm = browserTableModels.get(currentTable);
		
		if (btm == null) {
			final BrowserTable browserTable = new BrowserTable(openBrowser, compiler, popupMenuHelper,
					applicationManager, eventHelper, tableManager);
			
			btm = new BrowserTableModel(browserTable, currentTable, compiler, tableManager);
			browserTableModels.put(currentTable, btm);
		}
		
		return btm;
	}

	void updateColumnComparators(final TableRowSorter<BrowserTableModel> rowSorter,
			final BrowserTableModel browserTableModel) {
		for (int column = 0; column < browserTableModel.getColumnCount(); ++column)
			rowSorter.setComparator(
				column,
				new ValidatedObjectAndEditStringComparator(
					browserTableModel.getColumn(column).getType()));
	}

	@Override
	public String toString() {
		return "AbstractTableBrowser [tabTitle=" + tabTitle + ", currentTable=" + currentTable + "]";
	}
}
