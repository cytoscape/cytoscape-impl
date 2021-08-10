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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.TableToolBarComponent;
import org.cytoscape.browser.internal.io.TableColumnStat;
import org.cytoscape.browser.internal.io.TableColumnStatFileIO;
import org.cytoscape.browser.internal.util.CyToolBar;
import org.cytoscape.browser.internal.util.ViewUtil;
import org.cytoscape.browser.internal.view.tools.AbstractToolBarControl;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
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
public abstract class AbstractTableBrowser extends JPanel implements CytoPanelComponent2, SessionLoadedListener,
		SessionAboutToBeSavedListener, TableViewAddedListener {

	private final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	public static final CytoPanelName CYTO_PANEL_NAME = CytoPanelName.SOUTH;
	
	public static final int ICON_WIDTH = 32;
	public static final int ICON_HEIGHT = 31;
	public static final float ICON_FONT_SIZE = 22.0f;
	
	private static final Dimension PANEL_SIZE = new Dimension(550, 400);
	
	protected JPanel header;
	protected TableToolBar toolBar;
	protected OptionsBar optionsBar;
	
	private JPanel dropPanel;
	
	private final JPanel mainPane = new JPanel();
	
	private final JLabel dropIconLabel = new JLabel();
	private final JLabel dropLabel = new JLabel("Drag table files here");

	protected final String tabTitle;
	protected CyTable currentTable;
	
	private final Map<CyTable, TableRenderer> tableRenderers;
	
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
		
		init();
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CYTO_PANEL_NAME;
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
	
	/**
	 * @return {@link CyNode.class}, {@link CyEdge.class}, {@link CyNetwork.class} or <code>null</code> ("Global" table).
	 */
	public Class<? extends CyIdentifiable> getObjectType() {
		return objType;
	}
	
	public CyTable getCurrentTable() {
		return currentTable;
	}
	
	public void setCurrentTable(CyTable currentTable) {
		this.currentTable = currentTable;
		update();
	}
	
	public boolean addTable(CyTable table) {
		if (!containsTable(table)) {
			((DefaultComboBoxModel<CyTable>) getTableChooser().getModel()).addElement(table);
			return true;
		}
		
		return false;
	}
	
	public void selectTable(CyTable table) {
		getTableChooser().setSelectedItem(table);
	}
	
	public int getTableCount() {
		return getTableChooser().getItemCount();
	}
	
	public boolean containsTable(CyTable table) {
		return ((DefaultComboBoxModel<CyTable>) getTableChooser().getModel()).getIndexOf(table) >= 0;
	}
	
	/**
	 * @return true if it contains no tables
	 */
	public boolean isEmpty() {
		synchronized (lock) {
			return tableRenderers.isEmpty();
		}
	}
	
	public TableRenderer getTableRenderer(CyTable table) {
		if (table == null)
			return null;
		
		synchronized (lock) {
			var renderer = tableRenderers.get(table);
			
			if (renderer == null)
				 createDefaultTableView(table);
			
			return tableRenderers.get(table);
		}
	}
	
	public TableRenderer getCurrentRenderer() {
		return getTableRenderer(currentTable);
	}
	
	/**
	 * Delete the given table from the JTable
	 */
	public void removeTable(CyTable cyTable) {
		var chooserModel = (DefaultComboBoxModel<CyTable>) getTableChooser().getModel();
		chooserModel.removeElement(cyTable);
		
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
		
		if (isEmpty())
			showSelectedTable();
		else
			update();
	}
	
	private void init() {
		setPreferredSize(PANEL_SIZE);
		setSize(PANEL_SIZE);
		
		// Layouts
		mainPane.setLayout(new BorderLayout());
		
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getHeader(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(mainPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getHeader(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(mainPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		// Drag and Drop
		showDropPanel();
		
		var dropListener = new BrowserDropListener();
		setTransferHandler(dropListener);
		new DropTarget(this, dropListener);
		
		// Toolbar
		getToolBar().addSpacer(Integer.MAX_VALUE - 10);
		
		if (getTableChooser() != null) {
			var toolbarComp = new TableToolBarComponent() {
				@Override
				public Component getComponent() {
					return getTableChooser();
				}
				@Override
				public float getToolBarGravity() {
					return Integer.MAX_VALUE;
				}
				@Override
				public Class<? extends CyIdentifiable> getTableType() {
					return objType;
				}
				@Override
				public boolean isApplicable(CyTable table) {
					return true;
				}
			};
			
			getToolBar().addToolBarComponent(toolbarComp, Collections.emptyMap());
		}
		
		getTableChooser().getModel().addListDataListener(new ListDataListener() {
			@Override
			public void intervalRemoved(ListDataEvent e) {
				updateTableChooser();
			}
			@Override
			public void intervalAdded(ListDataEvent e) {
				updateTableChooser();
			}
			@Override
			public void contentsChanged(ListDataEvent e) {
				// Ignore...
			}
		});
		
		update();
	}
	
	protected void update() {
		updateToolBar();
		getOptionsBar().update();
	}

	private void updateToolBar() {
		updateTableChooser();
		getHeader().setVisible(currentTable != null);
	}
	
	private void updateTableChooser() {
		var minToShow = getObjectType() == null ? 1 : 2;
		getTableChooser().setVisible(getTableChooser().getItemCount() >= minToShow);
		
		if (currentTable != null && !currentTable.equals(getTableChooser().getSelectedItem()))
			selectTable(currentTable);
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
	
	protected void showSelectedTable() {
		var renderer = getCurrentRenderer();
		
		if (renderer != null) {
			mainPane.removeAll();
			mainPane.add(renderer.getComponent(), BorderLayout.CENTER);
			mainPane.revalidate();
			mainPane.validate();
			mainPane.repaint();
		} else {
			showDropPanel();
			repaint();
		}

		update();
	}

	private void createDefaultTableView(CyTable table) {
		var tableViewManager = serviceRegistrar.getService(CyTableViewManager.class);
		
		// If no table view exists yet then automatically create one using the default renderer.
		var tableView = tableViewManager.getTableView(table);
		
		if (tableView == null) {
			var tableViewFactory = serviceRegistrar.getService(CyTableViewFactory.class);
			tableView = tableViewFactory.createTableView(table);
			
			// This will fire the event that runs the below handleEvent(...) method.
			tableViewManager.setTableView(tableView);
		}
	}
	
	
	private boolean correctType(CyTable table) {
		// what is the objType for unassigned tables?
		var networkTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
		var type = networkTableManager.getTableType(table);
		return Objects.equals(objType, type);
	}
	
	@Override
	public void handleEvent(TableViewAddedEvent e) {
		var tableView = e.getTableView();
		
		if(!correctType(tableView.getModel()))
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
				boolean vis = renderer.isColumnVisible(colView);
				tcs.addColumnStat(colView.getModel().getName(), i, vis);
			}

			tableColumnStatList.add(tcs);
		}
		
		TableColumnStatFileIO.write(tableColumnStatList, e, appFileName);
	}
	
	protected Map<CyTable,TableRenderer> getTableRenderersMap() {
		return new HashMap<>(tableRenderers);
	}

	@Override
	public String toString() {
		return "AbstractTableBrowser [tabTitle=" + tabTitle + ", currentTable=" + currentTable + "]";
	}
	
	protected JPanel getHeader() {
		if (header == null) {
			header = new JPanel(new BorderLayout());
			header.add(getToolBar(), BorderLayout.NORTH);
			header.add(getOptionsBar(), BorderLayout.SOUTH);
		}
		
		return header;
	}
	
	public TableToolBar getToolBar() {
		if (toolBar == null) {
			toolBar = new TableToolBar(objType, serviceRegistrar);
			toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
		}
		
		return toolBar;
	}
	
	protected OptionsBar getOptionsBar() {
		if (optionsBar == null) {
			optionsBar = new OptionsBar();
			optionsBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
			optionsBar.setVisible(false);
		}
		
		return optionsBar;
	}
	
	protected abstract JComboBox<CyTable> getTableChooser();
	
	public class TableToolBar extends CyToolBar {

		private final Class<? extends CyIdentifiable> objType;
		
		public TableToolBar(Class<? extends CyIdentifiable> objType, CyServiceRegistrar serviceRegistrar) {
			super(tabTitle + " Tools", JToolBar.HORIZONTAL, ICON_WIDTH, ICON_HEIGHT, serviceRegistrar);
			this.objType = objType;
		}
		
		public Class<? extends CyIdentifiable> getObjectType() {
			return objType;
		}
		
		public CyTable getCurrentTable() {
			return currentTable;
		}
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
