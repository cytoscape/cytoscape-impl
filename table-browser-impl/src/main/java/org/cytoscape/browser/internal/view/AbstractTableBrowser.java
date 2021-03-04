package org.cytoscape.browser.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
import org.cytoscape.browser.internal.io.TableColumnStat;
import org.cytoscape.browser.internal.io.TableColumnStatFileIO;
import org.cytoscape.browser.internal.util.ViewUtil;
import org.cytoscape.browser.internal.view.tools.AbstractToolBarControl;
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
import org.cytoscape.view.model.events.TableViewAddedEvent;
import org.cytoscape.view.model.events.TableViewAddedListener;
import org.cytoscape.view.model.table.CyTableViewFactory;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
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
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
		implements CytoPanelComponent2, TableViewAddedListener, SessionLoadedListener, SessionAboutToBeSavedListener {

	private final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	static final int SELECTOR_WIDTH = 400;
	private static final Dimension PANEL_SIZE = new Dimension(550, 400);
	
	protected TableBrowserToolBar toolBar;
	protected OptionsBar optionsBar;
	private JPanel dropPanel;
	
	private final JPanel mainPane = new JPanel();
	
	private final JLabel dropIconLabel = new JLabel();
	private final JLabel dropLabel = new JLabel("Drag table files here");

	private final String tabTitle;
	protected CyTable currentTable;
	
	private final Map<CyTable,TableRenderer> tableRenderers;
	
	protected final String appFileName;
	protected Class<? extends CyIdentifiable> currentTableType;

	protected TextIcon icon;
	
	protected final Class<? extends CyIdentifiable> objType;
	
	protected final CyServiceRegistrar serviceRegistrar;
	private final Object lock = new Object();

	AbstractTableBrowser(
			String tabTitle,
			Class<? extends CyIdentifiable> objType,
			CyServiceRegistrar serviceRegistrar
	) {
		this.tabTitle = tabTitle;
		this.objType = objType;
		this.serviceRegistrar = serviceRegistrar;
		
		appFileName  = tabTitle.replaceAll(" ", "").concat(".props");
		tableRenderers = new HashMap<>();
		
		setLayout(new BorderLayout());
		setPreferredSize(PANEL_SIZE);
		setSize(PANEL_SIZE);
		
		mainPane.setLayout(new BorderLayout());
		
		getToolBar().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
		getOptionsBar().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getToolBar(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getOptionsBar(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(mainPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getToolBar(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getOptionsBar(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(mainPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		getToolBar().getFormatButton().addActionListener(evt -> {
			getOptionsBar().setVisible(getToolBar().getFormatButton().isSelected());
		});
		
		showDropPanel();
		
		var dropListener = new BrowserDropListener();
		setTransferHandler(dropListener);
		new DropTarget(this, dropListener);
		
		update();
	}
	
	protected abstract boolean containsTable(CyTable table);
	
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
	
	public void setCurrentTable(CyTable currentTable) {
		this.currentTable = currentTable;
		update();
	}
	
	/**
	 * Delete the given table from the JTable
	 */
	public void removeTable(CyTable cyTable) {
		TableRenderer renderer = null;
		
		synchronized (lock) {
			renderer = tableRenderers.remove(cyTable);
		}
		
		if (renderer == null)
			return;
		
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
		getOptionsBar().update();
	}

	private void updateToolBar() {
		getToolBar().setVisible(currentTable != null);
		getOptionsBar().setVisible(currentTable != null && getToolBar().getFormatButton().isSelected());
	}
	
	private void showDropPanel() {
		mainPane.removeAll();
		mainPane.add(getDropPanel(), BorderLayout.CENTER);
	}
	
	private JPanel getDropPanel() {
		if (dropPanel == null) {
			dropPanel = new JPanel();
			dropPanel.setBackground(UIManager.getColor("Table.background"));
			
			var fg = UIManager.getColor("Label.disabledForeground");
			fg = new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 120);
			
			dropPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(3, 3, 3, 3),
					BorderFactory.createDashedBorder(fg, 2, 2, 2, true)
			));
			
			dropIconLabel.setIcon(new ImageIcon(getClass().getClassLoader().getResource("/images/drop-table-file-56.png")));
			dropIconLabel.setForeground(fg);
			
			dropLabel.setFont(dropLabel.getFont().deriveFont(18.0f).deriveFont(Font.BOLD));
			dropLabel.setForeground(fg);
			
			var layout = new GroupLayout(dropPanel);
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
		var tableRenderer = getCurrentRenderer();
		
		if (tableRenderer != null) {
			mainPane.removeAll();
			mainPane.add(tableRenderer.getComponent(), BorderLayout.CENTER);
			mainPane.revalidate();
			mainPane.validate();
			mainPane.repaint();
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
			 createDefaultTableView();
		
		synchronized (lock) {
			renderer = tableRenderers.get(currentTable);
		}
		
		update();
		return renderer;
	}

	private void createDefaultTableView() {
		var tableViewManager = serviceRegistrar.getService(CyTableViewManager.class);
		
		// If no table view exists yet then automatically create one using the default renderer.
		var tableView = tableViewManager.getTableView(currentTable);
		
		if (tableView == null) {
			var tableViewFactory = serviceRegistrar.getService(CyTableViewFactory.class);
			tableView = tableViewFactory.createTableView(currentTable);
			
			// this will fire the event that runs the below handler
			tableViewManager.setTableView(tableView);
		}
	}
	
	@Override
	public void handleEvent(TableViewAddedEvent event) {
		var tableView = event.getTableView();
		
		if (!containsTable(tableView.getModel()))
			return;

		// a renderer may already exist
		TableRenderer exitingRenderer = null;
		
		synchronized (lock) {
			exitingRenderer = tableRenderers.remove(tableView.getModel());
		}
		
		if (exitingRenderer != null)
			exitingRenderer.dispose();
		
		var renderingEngineManager = serviceRegistrar.getService(RenderingEngineManager.class);
		var applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		
		var container = new JPanel();
		
		// create a rendering engine for the table view
		var tableViewRenderer = applicationManager.getTableViewRenderer(tableView.getRendererId());
		var renderingEngineFactory = tableViewRenderer.getRenderingEngineFactory(TableViewRenderer.DEFAULT_CONTEXT);
		var renderingEngine = renderingEngineFactory.createRenderingEngine(container, tableView);
		renderingEngineManager.addRenderingEngine(renderingEngine);
		
		var renderer = new TableRenderer(renderingEngine, container);
		
		synchronized (lock) {
			tableRenderers.put(tableView.getModel(), renderer);
		}
		
		showSelectedTable();
	}
	
	public TableRenderer getTableRenderer(CyTable table) {
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
	
	// We have to keep this for backwards compatibility
	@Override
	public void handleEvent(SessionLoadedEvent e) {
		var tscMap = TableColumnStatFileIO.read(e, appFileName);
		
		if (tscMap == null || tscMap.isEmpty())
			return;
		
		var browserTablesMap = getTableRenderersMap();
		
		for (var table : browserTablesMap.keySet()){
			if (!tscMap.containsKey(table.getTitle()))
				continue;
			
			var tcs = tscMap.get(table.getTitle());
			var renderer = getTableRenderer(table);
			
			var orderedCols = tcs.getOrderedCol();
			var visibleCols = tcs.getVisibleCols(); // MKTODO this should be a Set
			
			for (int i = 0; i < orderedCols.size(); i++) {
				var colName = orderedCols.get(i);
				renderer.setColumnGravity(colName, i);
				renderer.setColumnVisible(colName, visibleCols.contains(colName));
			}
		}
	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		var tableRendererMap = getTableRenderersMap();
		var tableColumnStatList = new ArrayList<TableColumnStat>();

		for (var table : tableRendererMap.keySet()) {
			var tcs = new TableColumnStat(table.getTitle());
			var renderer = getTableRenderer(table);

			var sortedColViews = renderer.getColumnViewsSortedByGravity();
			
			for (int i = 0; i < sortedColViews.size(); i++) {
				var colView = sortedColViews.get(i);
				boolean vis = renderer.getColumnVisible(colView);
				tcs.addColumnStat(colView.getModel().getName(), i, vis);
			}

			tableColumnStatList.add(tcs);
		}
		
		TableColumnStatFileIO.write(tableColumnStatList, e, appFileName);
	}
	
	@SuppressWarnings("unchecked")
	protected boolean showPrivateTables() {
		CyProperty<Properties> cyProp = serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		return cyProp != null && "true".equalsIgnoreCase(cyProp.getProperties().getProperty("showPrivateTables"));
	}
	
	protected abstract TableBrowserToolBar getToolBar();
	
	protected OptionsBar getOptionsBar() {
		if (optionsBar == null) {
			optionsBar = new OptionsBar();
			optionsBar.setVisible(false);
		}
		
		return optionsBar;
	}
	
	class OptionsBar extends JPanel {
		
		protected List<AbstractToolBarControl> controls = new ArrayList<>();
		
		OptionsBar() {
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		}
		
		void setFormatControls(List<AbstractToolBarControl> list) {
			controls.clear();
			removeAll();
			
			int idx = 0;
			
			for (var c : list) {
				c.setCurrentTable(getCurrentTable());
				controls.add(c);
				
				c.setMaximumSize(c.getPreferredSize());
				add(c);
				
				var sep = ViewUtil.createToolBarSeparator();
				sep.setMaximumSize(new Dimension(10, Short.MAX_VALUE));
				add(sep);
				
				if (++idx == list.size())
					add(Box.createHorizontalGlue());
			}
		}
		
		void update() {
			for (var c : controls)
				c.setCurrentTable(getCurrentTable());
		}
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
			var t = evt.getTransferable();
			
			if (evt.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {       
	            // Get the fileList that is being dropped.
		        final List<File> data;
		        
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
		
		private void loadFiles(List<File> data) {
			var taskManager = serviceRegistrar.getService(DialogTaskManager.class);
			var factory = serviceRegistrar.getService(LoadTableFileTaskFactory.class);

			if (factory != null)
				loadFiles(data.iterator(), taskManager, factory);
		}
		
		private void loadFiles(Iterator<File> iterator, DialogTaskManager taskManager,
				LoadTableFileTaskFactory factory) {
			while (iterator.hasNext()) {
				var file = iterator.next();
				
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
