package org.cytoscape.browser.internal.view;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.browser.internal.io.TableColumnStatFileIO;
import org.cytoscape.browser.internal.util.TableColumnStat;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;


/**
 * Base class for all Table Browsers.
 *
 */
public abstract class AbstractTableBrowser extends JPanel
										   implements CytoPanelComponent, ActionListener, SessionLoadedListener,
										   			  SessionAboutToBeSavedListener{

	private static final long serialVersionUID = 1968196123280466989L;
	
	static final Dimension SELECTOR_SIZE = new Dimension(400, 32);
	
	// Color theme for table browser.
	static final Color NETWORK_COLOR = new Color(0xA5, 0x2A, 0x2A);
	static final Color SELECTED_ITEM_BACKGROUND_COLOR = new Color(0xA0, 0xA0, 0xA0, 80);
	
	private static final Dimension PANEL_SIZE = new Dimension(550, 400);
	
	protected final CyServiceRegistrar serviceRegistrar;
	
	protected AttributeBrowserToolBar attributeBrowserToolBar;
		
	protected CyTable currentTable;
	protected Class<? extends CyIdentifiable> currentTableType;
	private final PopupMenuHelper popupMenuHelper; 
	

	// Tab title for the CytoPanel
	private final String tabTitle;
	private final Map<BrowserTable,JScrollPane> scrollPanes;
	private final Map<CyTable,BrowserTable> browserTables;
	private JScrollPane currentScrollPane;
	protected final String appFileName;


	AbstractTableBrowser(
			final String tabTitle,
			final CyServiceRegistrar serviceRegistrar,
			final PopupMenuHelper popupMenuHelper
	) {
		this.serviceRegistrar = serviceRegistrar;
		this.tabTitle = tabTitle;
		this.popupMenuHelper = popupMenuHelper;
		this.appFileName  = tabTitle.replaceAll(" ", "").concat(".props");

		this.scrollPanes = new HashMap<BrowserTable,JScrollPane>();
		this.browserTables = new HashMap<CyTable,BrowserTable>();
		
		this.setLayout(new BorderLayout());
		this.setOpaque(!isAquaLAF());
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
	
	/**
	 * Delete the given table from the JTable
	 * @param cyTable
	 */
	public void deleteTable(final CyTable cyTable) {
		final BrowserTable table = browserTables.remove(cyTable);
		
		if (table == null)
			return;
		
		table.setVisible(false);
		
		scrollPanes.remove(table);
		TableModel model = table.getModel();
		serviceRegistrar.unregisterAllServices(table);
		serviceRegistrar.unregisterAllServices(model);
		
		if (currentTable == cyTable) {
			currentTable = null;
			currentTableType = null;
		}
	}
	
	synchronized void showSelectedTable() {
		if (currentScrollPane != null)
			remove(currentScrollPane);

		final BrowserTable currentBrowserTable = getCurrentBrowserTable();
		final JScrollPane newScrollPane = getScrollPane(currentBrowserTable);
		
		if (newScrollPane != null)
			add(newScrollPane, BorderLayout.CENTER);
		else
			repaint();

		currentScrollPane = newScrollPane;
		attributeBrowserToolBar.setBrowserTable(currentBrowserTable);
	
		/* 
		// Never resize columns as they would reset the columns each time the view is changed
		if (currentBrowserTableModel != null)
			ColumnResizer.adjustColumnPreferredWidths(currentBrowserTableModel.getTable());
		 */
	}

	private JScrollPane getScrollPane(final BrowserTable browserTable) {
		JScrollPane scrollPane = null;
		
		if (browserTable != null) {
			scrollPane = scrollPanes.get(browserTable);
			
			if (scrollPane == null) {
				BrowserTableModel browserTableModel = (BrowserTableModel) browserTable.getModel();
				serviceRegistrar.registerAllServices(browserTable, new Properties());
				serviceRegistrar.registerAllServices(browserTableModel, new Properties());
				browserTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				browserTable.getTableHeader().setBackground(Color.LIGHT_GRAY);
				browserTable.setModel(browserTableModel);
				
				//move and hide SUID and selected by default
				final List<String> attrList = browserTableModel.getAllAttributeNames();

				BrowserTableColumnModel columnModel = (BrowserTableColumnModel) browserTable.getColumnModel();
				
				if (attrList.contains(CyNetwork.SUID))
					columnModel.moveColumn(browserTable.convertColumnIndexToView(
							browserTableModel.mapColumnNameToColumnIndex(CyNetwork.SUID)), 0);
				
				if (attrList.contains(CyNetwork.SELECTED))
					columnModel.moveColumn(browserTable.convertColumnIndexToView(
							browserTableModel.mapColumnNameToColumnIndex(CyNetwork.SELECTED)), 1);
				
				attrList.remove(CyNetwork.SUID);
				attrList.remove(CyNetwork.SELECTED);
				browserTable.setVisibleAttributeNames(attrList);
				
				scrollPane = new JScrollPane(browserTable);
				scrollPanes.put(browserTable, scrollPane);
			}
		}

		return scrollPane;
	}

	protected BrowserTable getCurrentBrowserTable() {
		BrowserTable table = browserTables.get(currentTable);
		
		if (table == null && currentTable != null) {
			final EquationCompiler compiler = serviceRegistrar.getService(EquationCompiler.class);
			
			table = new BrowserTable(compiler, popupMenuHelper, serviceRegistrar);
			BrowserTableModel model = new BrowserTableModel(currentTable, currentTableType, compiler);
			table.setModel(model);
			browserTables.put(currentTable, table);
			
			return table;
		}
		
		return table;
	}
	
	protected Map<CyTable, BrowserTable> getAllBrowserTablesMap() {
		return browserTables;
	}

	@Override
	public String toString() {
		return "AbstractTableBrowser [tabTitle=" + tabTitle + ", currentTable=" + currentTable + "]";
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent e) {
		Map<String, TableColumnStat> tscMap = TableColumnStatFileIO.read(e, appFileName);
		
		if (tscMap == null || tscMap.isEmpty())
			return;
		
		Map<CyTable, BrowserTable>  browserTableModels = getAllBrowserTablesMap();
		
		for (CyTable table : browserTableModels.keySet()){
			if (! tscMap.containsKey(table.getTitle()))
				continue;
			
			final TableColumnStat tcs = tscMap.get(table.getTitle());
			
			final BrowserTable browserTable = browserTables.get(table);
			BrowserTableModel model = (BrowserTableModel) browserTable.getModel();
			final BrowserTableColumnModel colM = (BrowserTableColumnModel)browserTable.getColumnModel();
			colM.setAllColumnsVisible();
			final List<String> orderedCols = tcs.getOrderedCol();
			
			for (int i = 0; i < orderedCols.size(); i++){
				final String colName = orderedCols.get(i);
				colM.moveColumn(browserTable.convertColumnIndexToView(model.mapColumnNameToColumnIndex(colName)), i);
			}
			
			browserTable.setVisibleAttributeNames(tcs.getVisibleCols());
		}
	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		Map<CyTable, BrowserTable>  browserTables = getAllBrowserTablesMap();
		List< TableColumnStat> tableColumnStatList = new ArrayList<TableColumnStat>();

		for (CyTable table :  browserTables.keySet()){
			TableColumnStat tcs = new TableColumnStat(table.getTitle());

			BrowserTable browserTable = browserTables.get(table);
			BrowserTableModel model = (BrowserTableModel) browserTable.getModel();
			BrowserTableColumnModel colM = (BrowserTableColumnModel) browserTable.getColumnModel();
			List<String> visAttrs = browserTable.getVisibleAttributeNames();
			colM.setAllColumnsVisible();
			Collection<String> attrs =  model.getAllAttributeNames();

			for (String name: attrs){
				int viewIndex = browserTable.convertColumnIndexToView(model.mapColumnNameToColumnIndex(name));
				tcs.addColumnStat(name, viewIndex,  visAttrs.contains(name));			
			}

			browserTable.setVisibleAttributeNames(visAttrs);
			tableColumnStatList.add(tcs);
		}
		
		TableColumnStatFileIO.write(tableColumnStatList, e, this.appFileName );	
	}
}
