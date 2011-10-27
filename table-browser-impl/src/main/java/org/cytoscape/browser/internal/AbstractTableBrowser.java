package org.cytoscape.browser.internal;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
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


/**
 * Base class for all Table Browsers.
 *
 */
public abstract class AbstractTableBrowser extends JPanel implements CytoPanelComponent, ActionListener {

	private static final long serialVersionUID = 1968196123280466989L;
	
	static final Dimension SELECTOR_SIZE = new Dimension(320, 20);
	
	// Color theme for table browser.
	static final Color NETWORK_COLOR = new Color(0xA5, 0x2A, 0x2A);
	static final Color SELECTED_ITEM_BACKGROUND_COLOR = new Color(0xA0, 0xA0, 0xA0, 80);
	
	private static final Dimension PANEL_SIZE = new Dimension(550, 400);
	
	protected final CyNetworkTableManager networkTableManager;
	protected final CyServiceRegistrar serviceRegistrar;
	private final EquationCompiler compiler;
	
	protected final BrowserTable browserTable;
	protected AttributeBrowserToolBar attributeBrowserToolBar;
		
	protected BrowserTableModel browserTableModel;
	protected CyTable currentTable;
	protected final Map<CyTable, TableMetadata> tableToMetadataMap;
	protected final CyApplicationManager applicationManager;
	protected final CyNetworkManager networkManager;

	// Tab title for the CytoPanel
	private final String tabTitle;
	
	AbstractTableBrowser(final String tabTitle,
			final CyTableManager tableManager, final CyNetworkTableManager networkTableManager,
			final CyServiceRegistrar serviceRegistrar, final EquationCompiler compiler, final OpenBrowser openBrowser,
			final CyNetworkManager networkManager, final TableTaskFactory deleteTableTaskFactoryService,
			final DialogTaskManager guiTaskManagerServiceRef, final PopupMenuHelper popupMenuHelper,
			final CyApplicationManager applicationManager, final CyEventHelper eventHelper) {
		this.networkTableManager = networkTableManager;
		this.serviceRegistrar = serviceRegistrar;
		this.compiler = compiler;
		this.tabTitle = tabTitle;
		this.networkManager = networkManager;

		this.tableToMetadataMap = new HashMap<CyTable, TableMetadata>();
		this.applicationManager = applicationManager;

		this.browserTable = new BrowserTable(openBrowser, compiler, popupMenuHelper, applicationManager, eventHelper);
		
		this.setLayout(new BorderLayout());
		
		browserTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		browserTable.getTableHeader().setBackground(Color.LIGHT_GRAY);
		add(new JScrollPane(browserTable), BorderLayout.CENTER);
		
		this.setPreferredSize(PANEL_SIZE);
		this.setSize(PANEL_SIZE);
	}

	/**
	 * Returns the Component to be added to the CytoPanel.
	 * @return The Component to be added to the CytoPanel.
	 */
	public Component getComponent() { return this; }

	/**
	 * Returns the name of the CytoPanel that this component should be added to.
	 * @return the name of the CytoPanel that this component should be added to.
	 */
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.SOUTH;
	}

	/**
	 * Returns the title of the tab within the CytoPanel for this component.
	 * @return the title of the tab within the CytoPanel for this component.
	 */
	public String getTitle() { return tabTitle; }

	/**
	 * @return null
	 */
	public Icon getIcon() { return null; }
	
	
	void showSelectedTable() {
		browserTableModel = new BrowserTableModel(browserTable, currentTable, compiler);
		
		serviceRegistrar.registerAllServices(browserTableModel, new Properties());
		browserTable.setUpdateComparators(false);
		browserTable.setModel(browserTableModel);
		final TableRowSorter rowSorter = new TableRowSorter(browserTableModel);
		browserTable.setRowSorter(rowSorter);
		updateColumnComparators(rowSorter);
		browserTable.setUpdateComparators(true);
		attributeBrowserToolBar.setBrowserTableModel(browserTableModel);
		final TableMetadata tableMetadata = tableToMetadataMap.get(currentTable);
		if (tableMetadata != null) {
			final JTable jTable = browserTableModel.getTable();
			final TableColumnModel columnModel = jTable.getColumnModel();
			final Iterator<ColumnDescriptor> columnDescIter =
				tableMetadata.getColumnDescriptors();
			while (columnDescIter.hasNext()) {
				final ColumnDescriptor desc = columnDescIter.next();
				final int savedColumnIndex = desc.getColumnIndex();
				final TableColumn tableColumn = columnModel.getColumn(savedColumnIndex);
				tableColumn.setPreferredWidth(desc.getColumnWidth());
				final int currentColumnIndex =
					jTable.convertColumnIndexToView(
						browserTableModel.mapColumnNameToColumnIndex(desc.getColumnName()));
				if (currentColumnIndex != savedColumnIndex)
					jTable.moveColumn(currentColumnIndex, savedColumnIndex);
			}
		}

		applicationManager.setCurrentTable(currentTable);
	}

	void updateColumnComparators(final TableRowSorter rowSorter) {
		for (int column = 0; column < browserTableModel.getColumnCount(); ++column)
			rowSorter.setComparator(
				column,
				new ValidatedObjectAndEditStringComparator(
					browserTableModel.getColumn(column).getType()));
	}
}
