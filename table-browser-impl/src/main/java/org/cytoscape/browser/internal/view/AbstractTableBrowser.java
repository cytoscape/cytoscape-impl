package org.cytoscape.browser.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableModel;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.browser.internal.io.TableColumnStatFileIO;
import org.cytoscape.browser.internal.util.TableColumnStat;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.util.swing.ColumnResizer;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

/**
 * Base class for all Table Browsers.
 */
@SuppressWarnings("serial")
public abstract class AbstractTableBrowser extends JPanel
										   implements CytoPanelComponent, ActionListener, SessionLoadedListener,
										   			  SessionAboutToBeSavedListener{

	static final int SELECTOR_WIDTH = 400;
	private static final Dimension PANEL_SIZE = new Dimension(550, 400);
	
	private TableBrowserToolBar toolBar;
	private JPanel dropPanel;
	private final JLabel dropIconLabel = new JLabel();
	private final JLabel dropLabel = new JLabel("Drag a table file here");
	private JScrollPane currentScrollPane;
	private final PopupMenuHelper popupMenuHelper; 

	private final String tabTitle;
	protected CyTable currentTable;
	private final Map<BrowserTable, JScrollPane> scrollPanes;
	private final Map<CyTable, BrowserTable> browserTables;
	protected final String appFileName;
	protected Class<? extends CyIdentifiable> currentTableType;

	protected final CyServiceRegistrar serviceRegistrar;
	private final Object lock = new Object();
	
	AbstractTableBrowser(
			final String tabTitle,
			final CyServiceRegistrar serviceRegistrar,
			final PopupMenuHelper popupMenuHelper
	) {
		this.serviceRegistrar = serviceRegistrar;
		this.tabTitle = tabTitle;
		this.popupMenuHelper = popupMenuHelper;
		
		appFileName  = tabTitle.replaceAll(" ", "").concat(".props");
		scrollPanes = new HashMap<>();
		browserTables = new HashMap<>();
		
		setLayout(new BorderLayout());
		setOpaque(!isAquaLAF());
		setPreferredSize(PANEL_SIZE);
		setSize(PANEL_SIZE);
		
		addDropPanel();
		new CyDropListener(this, serviceRegistrar);
		update();
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.SOUTH;
	}

	@Override
	public String getTitle() {
		return tabTitle;
	}

	@Override
	public Icon getIcon() {
		return null;
	}
	
	/**
	 * Delete the given table from the JTable
	 */
	public void removeTable(final CyTable cyTable) {
		BrowserTable table = null;
		
		synchronized (lock) {
			table = browserTables.remove(cyTable);
		}
		
		if (table == null)
			return;
		
		table.setVisible(false);
		
		synchronized (lock) {
			scrollPanes.remove(table);
		}
		
		TableModel model = table.getModel();
		serviceRegistrar.unregisterAllServices(table);
		serviceRegistrar.unregisterAllServices(model);
		
		if (currentTable == cyTable) {
			currentTable = null;
			currentTableType = null;
		}
		
		update();
	}
	
	/**
	 * @return true if it contains no tables
	 */
	protected boolean isEmpty() {
		synchronized (lock) {
			return browserTables.isEmpty();
		}
	}
	
	protected void update() {
		updateToolBar();
	}

	private void updateToolBar() {
		if (toolBar != null)
			toolBar.setVisible(!isEmpty());
	}
	
	private void addDropPanel() {
		if (getDropPanel().getParent() == null)
			add(getDropPanel(), BorderLayout.CENTER);
	}
	
	private JPanel getDropPanel() {
		if (dropPanel == null) {
			dropPanel = new JPanel();
			dropPanel.setBackground(UIManager.getColor("Table.background"));
			
			Color fg = UIManager.getColor("Label.disabledForeground");
			fg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 60);
			
			dropPanel.setBorder(BorderFactory.createCompoundBorder(
					UIManager.getBorder("ScrollPane.border"),
					BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(3, 3, 3, 3),
						BorderFactory.createDashedBorder(fg, 2, 2, 2, true)
					)
			));
			
			dropIconLabel.setIcon(
					new ImageIcon(getClass().getClassLoader().getResource("/images/drop-table-file-56.png")));
			dropIconLabel.setForeground(fg);
			
			dropLabel.setFont(dropLabel.getFont().deriveFont(18.0f).deriveFont(Font.BOLD));
			dropLabel.setForeground(fg);
			
			final GroupLayout layout = new GroupLayout(dropPanel);
			dropPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(CENTER, true)
							.addComponent(dropIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(dropLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(dropIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(dropLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
			);
		}
		
		return dropPanel;
	}
	
	void showSelectedTable() {
		synchronized (lock) {
			if (currentScrollPane != null)
				remove(currentScrollPane);
	
			final BrowserTable currentBrowserTable = getCurrentBrowserTable();
			final JScrollPane newScrollPane = getScrollPane(currentBrowserTable);
			
			if (newScrollPane != null) {
				if (getDropPanel().getParent() == this)
					remove(getDropPanel());
					
				add(newScrollPane, BorderLayout.CENTER);
				ColumnResizer.adjustColumnPreferredWidths(currentBrowserTable, false);
			} else {
				addDropPanel();
				repaint();
			}
	
			currentScrollPane = newScrollPane;
			getToolBar().setBrowserTable(currentBrowserTable);
		}
	}

	private JScrollPane getScrollPane(final BrowserTable browserTable) {
		JScrollPane scrollPane = null;
		
		if (browserTable != null) {
			synchronized (lock) {
				scrollPane = scrollPanes.get(browserTable);
			}
			
			if (scrollPane == null) {
				BrowserTableModel browserTableModel = (BrowserTableModel) browserTable.getModel();
				serviceRegistrar.registerAllServices(browserTable, new Properties());
				serviceRegistrar.registerAllServices(browserTableModel, new Properties());
				browserTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
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
				
				synchronized (lock) {
					scrollPanes.put(browserTable, scrollPane);
				}
				
				// So the drop event can go straight through the table to the drop target associated with this panel
				if (browserTable.getDropTarget() != null)
					browserTable.getDropTarget().setActive(false);
			}
		}

		return scrollPane;
	}

	protected BrowserTable getCurrentBrowserTable() {
		BrowserTable table = null;
		
		synchronized (lock) {
			table = browserTables.get(currentTable);
		}
		
		if (table == null && currentTable != null) {
			final EquationCompiler compiler = serviceRegistrar.getService(EquationCompiler.class);
			
			table = new BrowserTable(compiler, popupMenuHelper, serviceRegistrar);
			BrowserTableModel model = new BrowserTableModel(currentTable, currentTableType, compiler);
			table.setModel(model);
			
			synchronized (lock) {
				browserTables.put(currentTable, table);
			}
			
			update();
		}
		
		return table;
	}
	
	public BrowserTable getBrowserTable(final CyTable table) {
		synchronized (lock) {
			return browserTables.get(table);
		}
	}
	
	protected Map<CyTable, BrowserTable> getAllBrowserTablesMap() {
		return new HashMap<>(browserTables);
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
		
		Map<CyTable, BrowserTable> browserTablesMap = getAllBrowserTablesMap();
		
		for (CyTable table : browserTablesMap.keySet()){
			if (! tscMap.containsKey(table.getTitle()))
				continue;
			
			final TableColumnStat tcs = tscMap.get(table.getTitle());
			
			final BrowserTable browserTable = getBrowserTable(table);
			BrowserTableModel model = (BrowserTableModel) browserTable.getModel();
			final BrowserTableColumnModel colM = (BrowserTableColumnModel)browserTable.getColumnModel();
			colM.setAllColumnsVisible();
			final List<String> orderedCols = tcs.getOrderedCol();
			
			for (int i = 0; i < orderedCols.size(); i++) {
				final String colName = orderedCols.get(i);
				colM.moveColumn(browserTable.convertColumnIndexToView(model.mapColumnNameToColumnIndex(colName)), i);
			}
			
			browserTable.setVisibleAttributeNames(tcs.getVisibleCols());
		}
	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		Map<CyTable, BrowserTable>  browserTables = getAllBrowserTablesMap();
		List<TableColumnStat> tableColumnStatList = new ArrayList<>();

		for (CyTable table : browserTables.keySet()){
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
		
		TableColumnStatFileIO.write(tableColumnStatList, e, appFileName );	
	}
	
	@SuppressWarnings("unchecked")
	protected boolean showPrivateTables() {
		final CyProperty<Properties> cyProp =
				serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		
		return cyProp != null && "true".equalsIgnoreCase(cyProp.getProperties().getProperty("showPrivateTables"));
	}
	
	protected TableBrowserToolBar getToolBar() {
		return toolBar;
	}
	
	protected void setToolBar(TableBrowserToolBar toolBar) {
		if (toolBar == null && this.toolBar != null)
			remove(this.toolBar);
		else if (toolBar != null)
			add(toolBar, BorderLayout.NORTH);
			
		this.toolBar = toolBar;
		updateToolBar();
	}
}
