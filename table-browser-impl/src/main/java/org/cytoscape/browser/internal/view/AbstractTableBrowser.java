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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.browser.internal.io.TableColumnStatFileIO;
import org.cytoscape.browser.internal.util.TableColumnStat;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTable;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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
										   implements CytoPanelComponent2, ActionListener, SessionLoadedListener,
										   			  SessionAboutToBeSavedListener{

	private final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	static final int SELECTOR_WIDTH = 400;
	private static final Dimension PANEL_SIZE = new Dimension(550, 400);
	
	private TableBrowserToolBar toolBar;
	private JPanel dropPanel;
//	private final JScrollPane scrollPane = new JScrollPane();
	
	private final JPanel mainPane = new JPanel();
	
	private final JLabel dropIconLabel = new JLabel();
	private final JLabel dropLabel = new JLabel("Drag table files here");

	private final String tabTitle;
	protected CyTable currentTable;
	
	private final Map<CyTable,TableRenderer> tableRenderers;
	
	protected final String appFileName;
	protected Class<? extends CyIdentifiable> currentTableType;

	protected TextIcon icon;
	
	protected final CyServiceRegistrar serviceRegistrar;
	private final Object lock = new Object();

	AbstractTableBrowser(
			final String tabTitle,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.serviceRegistrar = serviceRegistrar;
		this.tabTitle = tabTitle;
		
		appFileName  = tabTitle.replaceAll(" ", "").concat(".props");
		tableRenderers = new HashMap<>();
		
		setLayout(new BorderLayout());
		setOpaque(!isAquaLAF());
		setPreferredSize(PANEL_SIZE);
		setSize(PANEL_SIZE);
		
		mainPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
		mainPane.setLayout(new BorderLayout());
		
		add(mainPane, BorderLayout.CENTER);
		showDropPanel();
		
		BrowserDropListener dropListener = new BrowserDropListener();
		setTransferHandler(dropListener);
		new DropTarget(this, dropListener);
		
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
		if (icon == null)
			icon = new TextIcon(IconManager.ICON_TABLE,
					serviceRegistrar.getService(IconManager.class).getIconFont(14.0f), 16, 16);
		
		return icon;
	}
	
	public CyTable getCurrentTable() {
		return currentTable;
	}
	
	/**
	 * Delete the given table from the JTable
	 */
	public void removeTable(final CyTable cyTable) {
		TableRenderer renderer = null;
		
		synchronized (lock) {
			renderer = tableRenderers.remove(cyTable);
		}
		
		if (renderer == null)
			return;
		
		// MKTODO make sure this still works
//		table.setVisible(false);
//		
//		TableModel model = table.getModel();
//		serviceRegistrar.unregisterAllServices(table);
//		serviceRegistrar.unregisterAllServices(model);
//		
		renderer.dispose();
		
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
			return tableRenderers.isEmpty();
		}
	}
	
	protected void update() {
		updateToolBar();
	}

	private void updateToolBar() {
		if (toolBar != null)
			toolBar.setVisible(currentTable != null);
	}
	
	private void showDropPanel() {
		mainPane.removeAll();
		mainPane.add(getDropPanel(), BorderLayout.CENTER);
	}
	
	private JPanel getDropPanel() {
		if (dropPanel == null) {
			dropPanel = new JPanel();
			dropPanel.setBackground(UIManager.getColor("Table.background"));
			
			Color fg = UIManager.getColor("Label.disabledForeground");
			fg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 120);
			
			dropPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(3, 3, 3, 3),
					BorderFactory.createDashedBorder(fg, 2, 2, 2, true)
			));
			
			dropIconLabel.setIcon(new ImageIcon(getClass().getClassLoader().getResource("/images/drop-table-file-56.png")));
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
		final TableRenderer tableRenderer = getCurrentRenderer();
		
		if (tableRenderer != null) {
			mainPane.removeAll();
			mainPane.add(tableRenderer.getComponent(), BorderLayout.CENTER);
		} else {
			showDropPanel();
			repaint();
		}

		update();
		getToolBar().setTableRenderer(tableRenderer);
	}

	protected TableRenderer getCurrentRenderer() {
		TableRenderer renderer = null;
		
		synchronized (lock) {
			renderer = tableRenderers.get(currentTable);
		}
		
		if (renderer == null && currentTable != null)
			renderer = createTableRenderer();
		
		return renderer;
	}

	private TableRenderer createTableRenderer() {
		var applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		
		JComponent container = new JPanel();
		
		// MKTODO add ability to choose renderer
		var tableViewRenderer = applicationManager.getDefaultTableViewRenderer();
		
		var tableViewFactory = tableViewRenderer.getTableViewFactory();
		var renderingEngineFactory = tableViewRenderer.getRenderingEngineFactory(TableViewRenderer.DEFAULT_CONTEXT);
		var tableView = tableViewFactory.createTableView(currentTable);
		var renderingEngine = renderingEngineFactory.createRenderingEngine(container, tableView);
		
		var tableRenderer = new TableRenderer(renderingEngine, container);
		
//		
//		browserTable = new BrowserTable(compiler, popupMenuHelper, serviceRegistrar);
//		BrowserTableModel model = new BrowserTableModel(currentTable, currentTableType, compiler);
//		browserTable.setModel(model);
		
		synchronized (lock) {
			tableRenderers.put(currentTable, tableRenderer);
		}
		
//		serviceRegistrar.registerAllServices(browserTable, new Properties());
//		serviceRegistrar.registerAllServices(model, new Properties());
//		browserTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		browserTable.setModel(model);
		
//		//move and hide SUID and selected by default
//		final List<String> attrList = model.getAllAttributeNames();
//
//		BrowserTableColumnModel columnModel = (BrowserTableColumnModel) browserTable.getColumnModel();
//		
//		if (attrList.contains(CyNetwork.SUID))
//			columnModel.moveColumn(browserTable.convertColumnIndexToView(
//					model.mapColumnNameToColumnIndex(CyNetwork.SUID)), 0);
//		
//		if (attrList.contains(CyNetwork.SELECTED))
//			columnModel.moveColumn(browserTable.convertColumnIndexToView(
//					model.mapColumnNameToColumnIndex(CyNetwork.SELECTED)), 1);
//		
//		attrList.remove(CyNetwork.SUID);
//		attrList.remove(CyNetwork.SELECTED);
//		browserTable.setVisibleAttributeNames(attrList);
//		
//		// So the drop event can go straight through the table to the drop target associated with this panel
//		if (browserTable.getDropTarget() != null)
//			browserTable.getDropTarget().setActive(false);
//		
//		ColumnResizer.adjustColumnPreferredWidths(browserTable, false);
		update();
		
		return tableRenderer;
	}
	
	public TableRenderer getTableRenderer(final CyTable table) {
		synchronized (lock) {
			return tableRenderers.get(table);
		}
	}
	
	protected Map<CyTable,TableRenderer> getTableRenderersMap() {
		return new HashMap<>(tableRenderers);
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
		
		Map<CyTable,TableRenderer> browserTablesMap = getTableRenderersMap();
		
		for (CyTable table : browserTablesMap.keySet()){
			if (!tscMap.containsKey(table.getTitle()))
				continue;
			
//			final TableColumnStat tcs = tscMap.get(table.getTitle());
//			
//			final BrowserTable browserTable = getBrowserTable(table);
//			BrowserTableModel model = (BrowserTableModel) browserTable.getModel();
//			final BrowserTableColumnModel colM = (BrowserTableColumnModel)browserTable.getColumnModel();
//			colM.setAllColumnsVisible();
//			final List<String> orderedCols = tcs.getOrderedCol();
//			
//			for (int i = 0; i < orderedCols.size(); i++) {
//				final String colName = orderedCols.get(i);
//				colM.moveColumn(browserTable.convertColumnIndexToView(model.mapColumnNameToColumnIndex(colName)), i);
//			}
//			
//			browserTable.setVisibleAttributeNames(tcs.getVisibleCols());
		}
	}

	// MKTODO We should use the vizmap style of saving VisualProperties to the session
	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
//		Map<CyTable, BrowserTable>  browserTables = getAllBrowserTablesMap();
//		List<TableColumnStat> tableColumnStatList = new ArrayList<>();
//
//		for (CyTable table : browserTables.keySet()){
//			TableColumnStat tcs = new TableColumnStat(table.getTitle());
//
//			BrowserTable browserTable = browserTables.get(table);
//			BrowserTableModel model = (BrowserTableModel) browserTable.getModel();
//			BrowserTableColumnModel colM = (BrowserTableColumnModel) browserTable.getColumnModel();
//			List<String> visAttrs = browserTable.getVisibleAttributeNames();
//			colM.setAllColumnsVisible();
//			Collection<String> attrs =  model.getAllAttributeNames();
//
//			for (String name: attrs){
//				int viewIndex = browserTable.convertColumnIndexToView(model.mapColumnNameToColumnIndex(name));
//				tcs.addColumnStat(name, viewIndex,  visAttrs.contains(name));			
//			}
//
//			browserTable.setVisibleAttributeNames(visAttrs);
//			tableColumnStatList.add(tcs);
//		}
//		
//		TableColumnStatFileIO.write(tableColumnStatList, e, appFileName );	
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
	
	private class BrowserDropListener extends TransferHandler implements DropTargetListener {

		private Border originalBorder;
		
		@Override
		public void dragEnter(DropTargetDragEvent evt) {
			originalBorder = getDropTarget().getBorder();
			getDropTarget().setBorder(BorderFactory.createLineBorder(UIManager.getColor("Focus.color"), 2));
		}

		@Override
		public void dragExit(DropTargetEvent evt) {
			getDropTarget().setBorder(originalBorder);
		}

		@Override
		public void dragOver(DropTargetDragEvent evt) {
		}
		
		@Override
		public void dropActionChanged(DropTargetDragEvent evt) {
		}

		@Override
		@SuppressWarnings("unchecked")
		public void drop(DropTargetDropEvent evt) {
			getDropTarget().setBorder(originalBorder);
	        
			if (!isAcceptable(evt)) {
				evt.rejectDrop();
	        	return;
			}
			
			evt.acceptDrop(evt.getDropAction());
			final Transferable t = evt.getTransferable();
			
			if (evt.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {       
	            // Get the fileList that is being dropped.
		        List<File> data;
		        
		        try {
		            data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
		        } catch (Exception e) { 
		        	logger.error("Cannot load table files by Drag-and-Drop.", e);
		        	return; 
		        }
		        
		        new Thread(() -> {
		        	loadFiles(data);
		        }).start();
	        }
		}
		
		@Override
        public boolean canImport(TransferHandler.TransferSupport info) {
        	return isAcceptable(info);
        }
		
        @Override
        public boolean importData(TransferHandler.TransferSupport info) {
            return info.isDrop() && !isAcceptable(info);
        }
		
		private void loadFiles(final List<File> data) {
			final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
			final LoadTableFileTaskFactory factory = serviceRegistrar.getService(LoadTableFileTaskFactory.class);

			if (factory != null)
				loadFiles(data.iterator(), taskManager, factory);
		}
		
		private void loadFiles(final Iterator<File> iterator, final DialogTaskManager taskManager,
				final LoadTableFileTaskFactory factory) {
			while (iterator.hasNext()) {
				final File file = iterator.next();
				
				if (!file.isDirectory()) {
					try {
						taskManager.execute(factory.createTaskIterator(file), new TaskObserver() {
							@Override
							public void taskFinished(ObservableTask task) {
							}
							@Override
							public void allFinished(FinishStatus finishStatus) {
								// Load the other files recursively
								loadFiles(iterator, taskManager, factory);
							}
						});
					} catch (Exception e) {
						logger.error("Cannot load table file by Drag-and-Drop.", e);
					}
					
					return;
				}
			}
		}

		private boolean isAcceptable(DropTargetDropEvent evt) {
			return evt.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}

		private boolean isAcceptable(TransferHandler.TransferSupport info) {
			return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}
		
		private JComponent getDropTarget() {
			return currentTable == null ? mainPane : AbstractTableBrowser.this;
		}
	}
}
