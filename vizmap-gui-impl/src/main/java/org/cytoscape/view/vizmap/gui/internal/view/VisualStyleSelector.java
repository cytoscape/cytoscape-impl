package org.cytoscape.view.vizmap.gui.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.IconManager.ICON_CHECK_SQUARE_O;
import static org.cytoscape.util.swing.IconManager.ICON_EDIT;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT_SQUARE;
import static org.cytoscape.util.swing.IconManager.ICON_SQUARE_O;
import static org.cytoscape.util.swing.IconManager.ICON_TRASH_O;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;
import static org.cytoscape.view.vizmap.gui.internal.view.util.ViewUtil.invokeOnEDT;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.event.DebounceTimer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.task.RemoveVisualStylesTask;
import org.cytoscape.view.vizmap.gui.internal.task.RenameVisualStyleTask;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.util.SimpleToolBarToggleButton;
import org.cytoscape.view.vizmap.gui.internal.view.util.ViewUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

@SuppressWarnings("serial")
public class VisualStyleSelector extends JPanel {

	private static final int IMAGE_WIDTH = 120;
	private static final int IMAGE_HEIGHT = 68;
	private static final int ITEM_BORDER_WIDTH = 1;
	private static final int ITEM_MARGIN = 2;
	private static final int ITEM_PAD = 2;
	
	final Color BG_COLOR;
	final Color FG_COLOR;
	final Color SEL_BG_COLOR;
	final Color SEL_FG_COLOR;
	final Color FOCUS_BORDER_COLOR;
	final Color FOCUS_OVERLAY_COLOR;
	final Color BORDER_COLOR;
	
	private JTextField searchTxtFld;
	private StyleGrid styleGrid;
	private JScrollPane gridScrollPane;
	
	private JToggleButton editBtn;
	private JPanel toolBarPanel;
	private JButton selectAllBtn;
	private JButton selectNoneBtn;
	private JToggleButton filterAppliedBtn;
	private JButton removeStylesBtn;
	
	private int cols;
	
	/** Should store all styles in the current session */
	private LinkedList<VisualStyle> allStyles;
	/** Store filtered in styles; can be null */
	private LinkedList<VisualStyle> filteredStyles;
	
	private String titleFilter = "";
	/** If true, only styles that are applied to at least one view are filtered in */
	private boolean appliedFilter;
	
	private final int minColumns;
	private final int maxColumns;
	
	private boolean editMode;
	private boolean editingTitle;
	private boolean selectionIsAdjusting;
	
	private int selectionHead;
	private int selectionTail;
	
	private final Map<String, RenderingEngine<CyNetwork>> engineMap;
	
	private VisualStyle selectedStyle;
	
	private final CyNetworkView previewNetView;
	private final Map<String/*visual style name*/, JPanel> defViewPanelsMap;
	private final ServicesUtil servicesUtil;
	
	/**
	 * @param minColumns 1 is the minimum value.
	 * @param maxColumns 0 means any number of columns.
	 * @param servicesUtil
	 */
	public VisualStyleSelector(int minColumns, int maxColumns, ServicesUtil servicesUtil) {
		this(minColumns, maxColumns, false, servicesUtil);
	}
	
	public VisualStyleSelector(int minColumns, int maxColumns, boolean editMode, ServicesUtil servicesUtil) {
		super(true);

		this.minColumns = Math.max(1, minColumns); // at least 1 column
		this.maxColumns = Math.max(0, maxColumns); // avoid negatives!
		this.editMode = editMode;
		this.servicesUtil = servicesUtil;
		
		allStyles = new LinkedList<>();
		engineMap = new HashMap<>();
		defViewPanelsMap = new HashMap<>();
		
		BG_COLOR = UIManager.getColor("Table.background");
		FG_COLOR = UIManager.getColor("Table.foreground");
		SEL_BG_COLOR = UIManager.getColor("Table.focusCellBackground");
		SEL_FG_COLOR = UIManager.getColor("Table.focusCellForeground");
		FOCUS_BORDER_COLOR = UIManager.getColor("Table.selectionBackground");
		FOCUS_OVERLAY_COLOR = new Color(FOCUS_BORDER_COLOR.getRed(), FOCUS_BORDER_COLOR.getGreen(), FOCUS_BORDER_COLOR.getBlue(), 100);
		BORDER_COLOR = UIManager.getColor("Separator.foreground");
		
		previewNetView = createPreviewNetworkView();
		
		init();
		update();
	}
	
	public boolean isEditMode() {
		return editMode;
	}
	
	public void setEditMode(boolean editMode) {
		if (editMode != this.editMode) {
			selectionIsAdjusting = true;
			this.editMode = editMode;
			
			try {
				update();
				
				if (!editMode)
					getStyleGrid().setSelectedValue(selectedStyle, getStyleGrid().isShowing());
			} finally {
				selectionIsAdjusting = false;
			}
			
			firePropertyChange("editMode", !editMode, editMode);
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		getSearchTxtFld().setEnabled(enabled);
		getEditBtn().setEnabled(enabled);
		getSelectAllBtn().setEnabled(enabled);
		getSelectNoneBtn().setEnabled(enabled);
		getRemoveStylesBtn().setEnabled(enabled);
		getStyleGrid().setEnabled(enabled);
	}
	
	@Override
    public void addNotify() {
    	super.addNotify();
    	getSearchTxtFld().requestFocusInWindow();
    }
	
	public void update(SortedSet<VisualStyle> styles, VisualStyle currentStyle) {
		allStyles.clear();
		
		if (styles != null)
			allStyles.addAll(styles);
		
		createPreviewRenderingEngines();
		getStyleGrid().update(allStyles);
		
		getStyleGrid().setSelectedValue(currentStyle, getStyleGrid().isShowing());
		update(true);
	}
	
	public void setSelectedStyle(VisualStyle style) {
		if (!Objects.equals(selectedStyle, style)) {
			var oldValue = selectedStyle;
			selectedStyle = style;
			getStyleGrid().setSelectedValue(style, getStyleGrid().isShowing());
			
			if (!selectionIsAdjusting)
				firePropertyChange("selectedStyle", oldValue, selectedStyle);
		}
	}
	
	/**
	 * It this is on edit mode (see {@link #isEditMode()}),
	 * you probably want to use {@link #getSelectedStyleList()} instead.
	 */
	public VisualStyle getSelectedStyle() {
		return selectedStyle;
	}
	
	/**
	 * Returns the selected styles. Use this one instead of {@link #getSelectedStyle()} when on edit mode.
	 */
	public List<VisualStyle> getSelectedStyleList() {
		return getStyleGrid().getSelectedValuesList();
	}
	
	public int getSelectionCount() {
		return getStyleGrid().getSelectionModel().getSelectedItemsCount();
	}
	
	public boolean isSelected(VisualStyle style) {
		var index = getStyleGrid().indexOf(style);
		return getStyleGrid().getSelectionModel().isSelectedIndex(index);
	}
	
	public JPanel getDefaultView(VisualStyle vs) {
		return defViewPanelsMap.get(vs.getTitle());
	}
	
	protected RenderingEngine<CyNetwork> getRenderingEngine(VisualStyle vs) {
		return vs != null ? engineMap.get(vs.getTitle()) : null;
	}
	
	public boolean isEmpty() {
		return allStyles == null || allStyles.isEmpty();
	}
	
	public void resetFilter() {
		getSearchTxtFld().setText("");
		
		if (getFilterAppliedBtn().isSelected())
			getFilterAppliedBtn().doClick();
	}
	
	public void dispose() {
		try {
			allStyles.clear();
			filteredStyles = null;
			engineMap.clear();
			defViewPanelsMap.clear();
			
			previewNetView.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void init() {
		var layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(false);
		layout.setAutoCreateContainerGaps(false);
		
		int btnSize = getEditBtn().getPreferredSize().height;
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getSearchTxtFld(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(getFilterAppliedBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(getToolBarPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getEditBtn(), btnSize, btnSize, btnSize)
				)
				.addComponent(getGridScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER)
						.addComponent(getSearchTxtFld(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getFilterAppliedBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getToolBarPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(getEditBtn(), btnSize, btnSize, btnSize)
				)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(getGridScrollPane(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
	}
	
	JTextField getSearchTxtFld() {
		if (searchTxtFld == null) {
			searchTxtFld = new JTextField();
			searchTxtFld.putClientProperty("JTextField.variant", "search"); // Aqua LAF only
			searchTxtFld.setToolTipText("Search by style name...");
			
			var debouncer = new DebounceTimer(250);
			
			searchTxtFld.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent evt) {
					updateTitleFilter();
				}
				@Override
				public void removeUpdate(DocumentEvent evt) {
					updateTitleFilter();
				}
				@Override
				public void changedUpdate(DocumentEvent evt) {
					// Ignore...
				}
				private void updateTitleFilter() {
					debouncer.debounce(() -> setTitleFilter(searchTxtFld.getText()));
				}
			});
		}
		
		return searchTxtFld;
	}
	
	JToggleButton getEditBtn() {
		if (editBtn == null) {
			editBtn = createToolBarToggleButton(ICON_EDIT, "Edit...", 16.0f);
			editBtn.addActionListener(evt -> setEditMode(editBtn.isSelected()));
		}
		
		return editBtn;
	}
	
	JPanel getToolBarPanel() {
		if (toolBarPanel == null) {
			toolBarPanel = new JPanel();
			
			var layout = new GroupLayout(toolBarPanel);
			toolBarPanel.setLayout(layout);
			layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
			layout.setAutoCreateContainerGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getSelectAllBtn())
					.addComponent(getSelectNoneBtn())
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getRemoveStylesBtn())
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER)
					.addComponent(getSelectAllBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getSelectNoneBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getRemoveStylesBtn(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return toolBarPanel;
	}
	
	JButton getSelectAllBtn() {
		if (selectAllBtn == null) {
			selectAllBtn = createToolBarButton(ICON_CHECK_SQUARE_O + ICON_CHECK_SQUARE_O, "Select All", 14.0f);
			selectAllBtn.addActionListener(evt -> getStyleGrid().selectAll());
		}
		
		return selectAllBtn;
	}
	
	JButton getSelectNoneBtn() {
		if (selectNoneBtn == null) {
			selectNoneBtn = createToolBarButton(ICON_SQUARE_O + ICON_SQUARE_O, "Select None", 14.0f);
			selectNoneBtn.addActionListener(evt -> getStyleGrid().deselectAll());
		}
		
		return selectNoneBtn;
	}
	
	JToggleButton getFilterAppliedBtn() {
		if (filterAppliedBtn == null) {
			filterAppliedBtn = createToolBarToggleButton(ICON_SHARE_ALT_SQUARE, "Show only Applied Styles", 16.0f);
			filterAppliedBtn.addActionListener(evt -> setAppliedFilter(filterAppliedBtn.isSelected()));
		}
		
		return filterAppliedBtn;
	}
	
	JButton getRemoveStylesBtn() {
		if (removeStylesBtn == null) {
			removeStylesBtn = createToolBarButton(ICON_TRASH_O, "Remove Selected Styles", 18.0f);
			removeStylesBtn.addActionListener(evt -> {
				var styles = new LinkedHashSet<>(getStyleGrid().getSelectedValuesList());
				var task = new RemoveVisualStylesTask(styles, servicesUtil);
				
				new Thread(() -> {
					// TODO Move to a mediator (?)
					servicesUtil.get(DialogTaskManager.class).execute(new TaskIterator(task), new TaskObserver() {
						@Override
						public void taskFinished(ObservableTask task) {
							// Ignore...
						}
						@Override
						public void allFinished(FinishStatus finishStatus) {
							var vmProxy = getProxy();
							update(vmProxy.getVisualStyles(), vmProxy.getCurrentVisualStyle());
						}
					});
				}).start();
			});
		}
		
		return removeStylesBtn;
	}

	StyleGrid getStyleGrid() {
		if (styleGrid == null) {
			styleGrid = new StyleGrid(allStyles);
			
			styleGrid.getSelectionModel().addListSelectionListener(evt -> {
				if (evt.getValueIsAdjusting())
					return;
				
				updateToolBarButtons();
				repaint();
				
				if (!isEditMode() && !selectionIsAdjusting)
					setSelectedStyle(styleGrid.getSelectedValue());
			});
		}
		
		return styleGrid;
	}
	
	JScrollPane getGridScrollPane() {
		if (gridScrollPane == null) {
			gridScrollPane = new JScrollPane(getStyleGrid());
			gridScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			gridScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			gridScrollPane.setBackground(BG_COLOR);
			gridScrollPane.getViewport().setBackground(BG_COLOR);
			
			var debouncer = new DebounceTimer();
			
			gridScrollPane.getViewport().addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent evt) {
					debouncer.debounce(() -> {
						invokeOnEDT(() -> getStyleGrid().update());
					});
				}
			});
			gridScrollPane.getViewport().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent evt) {
					if (isEditMode() && !evt.isShiftDown()) // Deselect all items
						getStyleGrid().deselectAll();
				}
			});
		}
		
		return gridScrollPane;
	}
	
	private void setTitleFilter(String s) {
		if (s == null)
			s = "";
		
		var newValue = s.trim().toLowerCase();
		
		invokeOnEDT(() -> {
			if (!newValue.equals(titleFilter)) {
				titleFilter = newValue;
				filterStyles();
			}
		});
	}
	
	public void setAppliedFilter(boolean b) {
		invokeOnEDT(() -> {
			if (b != appliedFilter) {
				appliedFilter = b;
				filterStyles();
			}
		});
	}
	
	private void createPreviewRenderingEngines() {
		defViewPanelsMap.clear();
		engineMap.clear();
		
		if (allStyles != null && previewNetView != null) {
			var vmProxy = (VizMapperProxy) servicesUtil.getProxy(VizMapperProxy.NAME);
			var engineFactory = vmProxy.getRenderingEngineFactory(previewNetView);
			
			for (var vs : allStyles) {
				var p = new JPanel();
				defViewPanelsMap.put(vs.getTitle(), p);
				
				var engine = engineFactory.createRenderingEngine(p, previewNetView);
				engineMap.put(vs.getTitle(), engine);
			}
		}
	}
	
	private void filterStyles() {
		filteredStyles = titleFilter == null || titleFilter.isBlank() ? null : new LinkedList<>();
		
		if (filteredStyles == null)
			filteredStyles = !appliedFilter ? null : new LinkedList<>();
		
		if (filteredStyles != null) {
			for (var vs : allStyles) {
				if (vs.getTitle().toLowerCase().contains(titleFilter)) {
					if (!appliedFilter || getProxy().countNetworkViewsWithStyle(vs) > 0)
						filteredStyles.add(vs);
				}
			}
		}
		
		// Save current selection
		var selectedStyles = isEditMode() ? getSelectedStyleList() : new ArrayList<VisualStyle>();
		
		if (selectedStyle != null && !isEditMode())
			selectedStyles.add(selectedStyle);
		
		selectionIsAdjusting = true;
		
		try {
			getStyleGrid().setModel(new StyleGridModel(filteredStyles != null ? filteredStyles : allStyles));
			// Restore previous selection
			getStyleGrid().setSelectedList(selectedStyles);
		} finally {
			selectionIsAdjusting = false;
		}
		
		firePropertyChange("filterChanged", false, true);
	}
	
	private boolean isFilteredOut(VisualStyle vs) {
		return false;
	}
	
	private int calculateColumns(int itemWidth, int gridWidth) {
		var cols = itemWidth > 0 ? Math.floorDiv(gridWidth, itemWidth) : 1;
		cols = Math.max(cols, 1);
		
		return Math.max(cols, maxColumns);
	}
	
	private int calculateRows(int total, int cols) {
		return (int) Math.round(Math.ceil((float)total / (float)cols));
	}
	
	private JButton createToolBarButton(String iconText, String tooltipText, float size) {
		var btn = new JButton(iconText);
		btn.setToolTipText(tooltipText);
		ViewUtil.styleToolBarButton(btn, servicesUtil.get(IconManager.class).getIconFont(size), 2, 0);
		
		return btn;
	}
	
	private JToggleButton createToolBarToggleButton(String iconText, String tooltipText, float size) {
		var btn = new SimpleToolBarToggleButton(iconText);
		btn.setToolTipText(tooltipText);
		ViewUtil.styleToolBarButton(btn, servicesUtil.get(IconManager.class).getIconFont(size), 2, 0);
		
		return btn;
	}
	
	public void setDirty(VisualStyle style) {
		getStyleGrid().setDirty(style);
	}
	
	void update() {
		update(false);
	}
	
	void update(boolean updateThumbnails) {
		setEnabled(!isEmpty());
		getEditBtn().setSelected(isEditMode());
		getToolBarPanel().setVisible(isEditMode());
		
		if (updateThumbnails)
			getStyleGrid().updateStylePanels();
		
		getStyleGrid().getSelectionModel().setSelectionMode(
				isEditMode() ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
		
		updateToolBarButtons();
	}

	private void updateToolBarButtons() {
		if (isEnabled() && isEditMode()) {
			var selValues = getStyleGrid().getSelectedValuesList();
			int selCount = selValues.size();
			int total =  getStyleGrid().getModel().getSize();
			var defStyle = getProxy().getDefaultVisualStyle();
			
			getSelectAllBtn().setEnabled(selCount < total);
			getSelectNoneBtn().setEnabled(selCount > 0);
			getRemoveStylesBtn().setEnabled(selCount > 1
					|| (selCount == 1 && !defStyle.equals(selValues.iterator().next())));
		}
	}
	
	private CyNetworkView createPreviewNetworkView() {
		// Create dummy view first
		var net = servicesUtil.get(CyNetworkFactory.class).createNetworkWithPrivateTables(SavePolicy.DO_NOT_SAVE);
		var source = net.addNode();
		var target = net.addNode();

		net.getRow(source).set(CyNetwork.NAME, "Source");
		net.getRow(target).set(CyNetwork.NAME, "Target");

		var edge = net.addEdge(source, target, true);
		net.getRow(edge).set(CyNetwork.NAME, "Source (interaction) Target");

		net.getRow(net).set(CyNetwork.NAME, "Default Appearance");
		var view = servicesUtil.get(CyNetworkViewFactory.class).createNetworkView(net);

		// Set node locations
		view.getNodeView(source).setVisualProperty(NODE_X_LOCATION, 0d);
		view.getNodeView(source).setVisualProperty(NODE_Y_LOCATION, 0d);
		view.getNodeView(target).setVisualProperty(NODE_X_LOCATION, 150d);
		view.getNodeView(target).setVisualProperty(NODE_Y_LOCATION, 20d);
		
		return view;
	}
	
	private VizMapperProxy getProxy() {
		return (VizMapperProxy) servicesUtil.getProxy(VizMapperProxy.NAME);
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	/**
	 * Some methods were copied from the JList implementation.
	 */
	class StyleGrid extends JComponent implements Scrollable {
		
		private final LinkedHashMap<VisualStyle, StylePanel> vsPanelMap = new LinkedHashMap<>();
		
		private ListModel<VisualStyle> dataModel;
		private ListSelectionModel selectionModel;
		private ListSelectionListener selectionListener;
		
		StyleGrid(List<VisualStyle> data) {
			this.dataModel = new StyleGridModel(data);
			this.selectionModel = createSelectionModel();
			this.setOpaque(true);
			this.setBackground(BG_COLOR);
			
			setKeyBindings();
			update();
		}
		
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getModel().getSize() == 0 ? getMinimumSize() : getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 10;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return cols > 0;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return getModel().getSize() == 0;
		}
		
		@Override
		public void repaint() {
			for (var item : vsPanelMap.values())
				item.repaint();
			
			super.repaint();
		}
		
		StylePanel getItem(VisualStyle style) {
			return vsPanelMap.get(style);
		}
		
		int indexOf(VisualStyle style) {
			int i, c;
			var dm = getModel();
			
			for (i = 0, c = dm.getSize(); i < c; i++) {
				if (style.equals(dm.getElementAt(i)))
					return i;
			}
			
			return -1;
		}
		
		void setFocus(int index) {
			var dm = getModel();
			
			if (index > -1 && index < dm.getSize()) {
				var vs = dm.getElementAt(index);
				var item = getItem(vs);
				
				if (item != null)
					item.requestFocusInWindow();
			}
		}
		
		/**
	     * Returns the value for the smallest selected cell index;
	     * <i>the selected value</i> when only a single item is selected in the
	     * list. When multiple items are selected, it is simply the value for the
	     * smallest selected index. Returns {@code null} if there is no selection.
	     * <p>
	     * This is a convenience method that simply returns the model value for
	     * {@code getMinSelectionIndex}.
	     *
	     * @return the first selected value
	     */
	    VisualStyle getSelectedValue() {
			int i = getMinSelectionIndex();
			
			return (i == -1) || (i >= getModel().getSize()) ? null : getModel().getElementAt(i);
	    }
		
		void setSelectedValue(VisualStyle style, boolean shouldScroll) {
			if (style == null) {
				deselectAll();
			} else if (!style.equals(getSelectedValue())) {
				int i = indexOf(style);
				setSelectedIndex(i);

				if (i >= 0 && shouldScroll)
					ensureIndexIsVisible(i);
			}
		}
		
		List<VisualStyle> getSelectedValuesList() {
			var dm = getModel();
			var sm = getSelectionModel();
			int[] selectedIndices = sm.getSelectedIndices();
			var selectedItems = new ArrayList<VisualStyle>();

			if (selectedIndices.length > 0) {
				int size = dm.getSize();
				
				if (selectedIndices[0] < size) {
					for (int i : selectedIndices) {
						if (i >= size)
							break;
						
						selectedItems.add(dm.getElementAt(i));
					}
				}
			}
			
			return selectedItems;
		}

		void selectAll() {
			if (getModel().getSize() >= 0) {
				getSelectionModel().setSelectionInterval(0, getModel().getSize() - 1);
				selectionHead = -1;
				selectionTail = -1;
			}
		}
		
		void deselectAll() {
			getSelectionModel().clearSelection();
			selectionHead = -1;
			selectionTail = -1;
		}
		
		/**
	     * Selects a single cell. Does nothing if the given index is greater
	     * than or equal to the model size. This is a convenience method that uses
	     * {@code setSelectionInterval} on the selection model. Refer to the
	     * documentation for the selection model class being used for details on
	     * how values less than {@code 0} are handled.
	     *
	     * @param index the index of the cell to select
	     */
		void setSelectedIndex(int index) {
			if (index >= getModel().getSize())
				return;
			
			getSelectionModel().setSelectionInterval(index, index);
		}
		
		int getMinSelectionIndex() {
	        return getSelectionModel().getMinSelectionIndex();
	    }
		
		int getMaxSelectionIndex() {
	        return getSelectionModel().getMaxSelectionIndex();
	    }
		
		boolean isSelectedIndex(int index) {
	        return getSelectionModel().isSelectedIndex(index);
	    }
		
		boolean isSelectionEmpty() {
	        return getSelectionModel().isSelectionEmpty();
	    }
		
		ListSelectionModel getSelectionModel() {
	        return selectionModel;
	    }
		
		void setSelectionInterval(int anchor, int lead) {
			getSelectionModel().setSelectionInterval(anchor, lead);
		}

		void addSelectionInterval(int anchor, int lead) {
			getSelectionModel().addSelectionInterval(anchor, lead);
		}

		void removeSelectionInterval(int index0, int index1) {
			getSelectionModel().removeSelectionInterval(index0, index1);
		}
		
		void setSelectedList(List<VisualStyle> styles) {
			var sm = getSelectionModel();
			sm.clearSelection();
			
			for (var vs : styles) {
				int idx = indexOf(vs);
				
				if (idx >= 0)
					sm.addSelectionInterval(idx, idx);
			}
			
			selectionHead = -1;
			selectionTail = -1;
		}
		
		void setSelectionModel(ListSelectionModel selectionModel) {
	        if (selectionModel == null)
	            throw new IllegalArgumentException("selectionModel must be non null");

	        // Remove the forwarding ListSelectionListener from the old
	        // selectionModel, and add it to the new one, if necessary.
	        if (selectionListener != null) {
	            this.selectionModel.removeListSelectionListener(selectionListener);
	            selectionModel.addListSelectionListener(selectionListener);
	        }

	        var oldValue = this.selectionModel;
	        this.selectionModel = selectionModel;
	        firePropertyChange("selectionModel", oldValue, selectionModel);
	    }
		
		ListModel<VisualStyle> getModel() {
			return dataModel;
		}

		void setModel(ListModel<VisualStyle> model) {
			if (model == null)
				throw new IllegalArgumentException("model must be non null");

			var oldValue = dataModel;
			dataModel = model;
			firePropertyChange("model", oldValue, dataModel);
			
			selectionIsAdjusting = true;
			
			try {
				deselectAll();
				update();
			} finally {
				selectionIsAdjusting = false;
			}
		}
		
		/**
	     * Scrolls the list within an enclosing viewport to make the specified
	     * cell completely visible. This calls {@code scrollRectToVisible} with
	     * the bounds of the specified cell. For this method to work, the
	     * {@code JList} must be within a <code>JViewport</code>.
	     * <p>
	     * If the given index is outside the list's range of cells, this method
	     * results in nothing.
	     *
	     * @param index  the index of the cell to make visible
	     */
	    void ensureIndexIsVisible(int index) {
	        var cellBounds = getCellBounds(index, index);
	        
	        if (cellBounds != null)
	            scrollRectToVisible(cellBounds);
	    }
	    
	    /**
	     * Returns the bounding rectangle, in the list's coordinate system,
	     * for the range of cells specified by the two indices.
	     * These indices can be supplied in any order.
	     * <p>
	     * If the smaller index is outside the list's range of cells, this method
	     * returns {@code null}. If the smaller index is valid, but the larger
	     * index is outside the list's range, the bounds of just the first index
	     * is returned. Otherwise, the bounds of the valid range is returned.
	     * <p>
	     * This is a cover method that delegates to the method of the same name
	     * in the list's {@code ListUI}. It returns {@code null} if the list has
	     * no {@code ListUI}.
	     *
	     * @param index0 the first index in the range
	     * @param index1 the second index in the range
	     * @return the bounding rectangle for the range of cells, or {@code null}
	     */
	    Rectangle getCellBounds(int index0, int index1) {
	        // TODO
	        return null;
	    }
		
		void addListSelectionListener(ListSelectionListener listener) {
			if (selectionListener == null) {
				selectionListener = new ListSelectionHandler();
				getSelectionModel().addListSelectionListener(selectionListener);
			}

			listenerList.add(ListSelectionListener.class, listener);
		}

		void removeListSelectionListener(ListSelectionListener listener) {
			listenerList.remove(ListSelectionListener.class, listener);
		}

		ListSelectionListener[] getListSelectionListeners() {
			return listenerList.getListeners(ListSelectionListener.class);
		}
		
		private ListSelectionModel createSelectionModel() {
	        return new DefaultListSelectionModel();
	    }
		
		protected void fireSelectionValueChanged(int firstIndex, int lastIndex, boolean isAdjusting) {
			Object[] listeners = listenerList.getListenerList();
			ListSelectionEvent e = null;

			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == ListSelectionListener.class) {
					if (e == null)
						e = new ListSelectionEvent(this, firstIndex, lastIndex, isAdjusting);
					
					((ListSelectionListener) listeners[i + 1]).valueChanged(e);
				}
			}
		}
		
		void setDirty(VisualStyle style) {
			if (style != null) {
				var item = getItem(style);
				
				if (item != null)
					item.setDirty();
			}
		}
		
		void updateStylePanels() {
			for (var item : vsPanelMap.values()) {
				if (item.isDirty())
					item.update();
			}
		}
		
		void update() {
			removeAll();
			
			var dm = getModel();
			
			if (dm.getSize() > 0) {
				var size = this.getParent() != null ? this.getParent().getSize() : null;
				
				if (size != null) {
					var itemWidth = IMAGE_WIDTH + 2 * ITEM_BORDER_WIDTH + 2 * ITEM_MARGIN + 2 * ITEM_PAD;
					var width = size != null ? size.width : itemWidth;
					cols = width <= 0 ? minColumns : calculateColumns(itemWidth, width);
				} else {
					cols = minColumns;
				}
				
				var rows = calculateRows(dm.getSize(), cols);
				setLayout(new GridLayout(rows, cols));
				
				for (int i = 0; i < dm.getSize(); i++) {
					var vs = dm.getElementAt(i);
					var itemPnl = getItem(vs);
					
					if (itemPnl == null)
						itemPnl = createItem(vs);
					
					if (!isFilteredOut(vs))
						add(itemPnl);
				}
				
				var diff = (cols * rows) - dm.getSize();
					
				for (int i = 0; i < diff; i++) {
					var fillPnl = new JPanel();
					fillPnl.setBackground(BG_COLOR);
					fillPnl.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent evt) {
							if (isEditMode() && !evt.isShiftDown()) // Deselect all items
								deselectAll();
						}
					});
					add(fillPnl);
				}
			}
			
			revalidate();
			repaint();
		}
		
		void update(List<VisualStyle> data) {
			vsPanelMap.clear();
			filterStyles();
		}
		
		private StylePanel createItem(VisualStyle style) {
			var item = new StylePanel(style);
			vsPanelMap.put(style, item);
			
			// Events
			item.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent evt) {
					if (isEnabled() && !editingTitle)
						item.requestFocusInWindow();
				}
				@Override
				public void mousePressed(MouseEvent evt) {
					if (isEnabled())
						onMousePressedItem(evt, item);
				}
			});
			item.getTitleTextField().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent evt) {
					if (isEnabled())
						onMousePressedItem(evt, item);
				}
				@Override
				public void mouseClicked(MouseEvent evt) {
					if (isEnabled()) {
						if (isEditMode() && evt.getClickCount() == 2)
							editTitleStart(item);
					}
				}
			});
			
			return item;
		}
		
		private void onMousePressedItem(MouseEvent evt, StylePanel item) {
			item.requestFocusInWindow();
			
			if (isEditMode()) {
				int index = indexOf(item.style);
				boolean selected = isSelected(item.style);
				var sm = getSelectionModel();
				
				if (evt.isPopupTrigger()) {
					// RIGHT-CLICK...
					selectionHead = index;
				} else {
					// LEFT-CLICK...
					var isMac = LookAndFeelUtil.isMac();
					
					if ((isMac && evt.isMetaDown()) || (!isMac && evt.isControlDown())) {
						// CMD or CTRL key pressed...
						toggleSelection(item);
						// Find new selection range head
						selectionHead = selected ? index : findNextSelectionHead(selectionHead);
					} else if (evt.isShiftDown()) {
						// SHIFT key pressed...
						if (selectionHead >= 0 && selectionHead != index && sm.isSelectedIndex(selectionHead)) {
							// First deselect previous range, if there is a tail
							if (selectionTail >= 0)
								changeRangeSelection(selectionHead, selectionTail, false);
							// Now select the new range
							changeRangeSelection(selectionHead, (selectionTail = index), true);
						} else if (!selected) {
							addSelectionInterval(index, index);
						}
					} else {
						setSelectedValue(item.getStyle(), false);
					}
					
					if (sm.getSelectedItemsCount() == 1)
						selectionHead = index;
				}
			} else {
				setSelectedValue(item.getStyle(), false);
			}
			
			item.repaint();
		}
		
		private void toggleSelection(StylePanel item) {
			var index = indexOf(item.style);
			
			if (isSelectedIndex(index))
				removeSelectionInterval(index, index);
			else
				addSelectionInterval(index, index);
		}
		
		private void changeRangeSelection(int index0, int index1, boolean select) {
			if (select)
				addSelectionInterval(index0, index1);
			else
				removeSelectionInterval(index0, index1);
		}
		
		private int findNextSelectionHead(int fromIndex) {
			int head = -1;
			
			if (fromIndex >= 0) {
				var dm = getModel();
				int total = dm.getSize();
				
				// Try with the tail subset first (go down)...
				for (int i = fromIndex; i < total; i++) {
					var nextItem = dm.getElementAt(i);
					
					if (isSelected(nextItem)) {
						head = i;
						break;
					}
				}
				
				if (head == -1) {
					// Try with the head subset  (go up)...
					for (int i = fromIndex; i <= 0; i--) {
						var nextItem = dm.getElementAt(i);
						
						if (isSelected(nextItem)) {
							head = i;
							break;
						}
					}
				}
			}
			
			return head;
		}

		void editTitleStart(StylePanel item) {
			if (getProxy().isDefaultStyle(item.getStyle()))
				return;
			
			editingTitle = true;
			var oldValue = item.getTitleTextField().getText().trim();
			
			item.getTitleTextField().setFocusable(true);
			item.getTitleTextField().setEditable(true);
			item.getTitleTextField().requestFocusInWindow();
			item.getTitleTextField().selectAll();
			
			item.getTitleTextField().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					editTitleCommit(item, oldValue);
					item.getTitleTextField().removeActionListener(this);
				}
			});
			item.getTitleTextField().addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent evt) {
					editTitleCommit(item, oldValue);
					item.getTitleTextField().removeFocusListener(this);
				}
			});
			item.getTitleTextField().addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
						editTitleCancel(item, oldValue);
						item.getTitleTextField().removeKeyListener(this);
						evt.consume(); // Prevent any dialog that contains this component from closing
					}
				}
			});
		}
		
		void editTitleCommit(StylePanel item, String oldValue) {
			var newValue = item.getTitleTextField().getText().trim();
			
			if (newValue.isEmpty() || newValue.equals(oldValue)) {
				editTitleCancel(item, oldValue);
				return;
			}
			
			editTitleEnd(item);
			
			var task = new RenameVisualStyleTask(item.getStyle(), servicesUtil);
			var map = new HashMap<String, Object>();
			map.put("vsName", newValue);
			var taskIterator = servicesUtil.get(TunableSetter.class).createTaskIterator(new TaskIterator(task), map);
			
			new Thread(() -> {
				servicesUtil.get(DialogTaskManager.class).execute(taskIterator);
			}).start();
		}
		
		void editTitleCancel(StylePanel item, String oldValue) {
			item.getTitleTextField().setText(oldValue);
			editTitleEnd(item);
		}
		
		void editTitleEnd(StylePanel item) {
			item.getTitleTextField().setEditable(false);
			item.requestFocusInWindow();
			item.getTitleTextField().setFocusable(false);
			editingTitle = false;
		}

		private class ListSelectionHandler implements ListSelectionListener, Serializable {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				fireSelectionValueChanged(e.getFirstIndex(), e.getLastIndex(), e.getValueIsAdjusting());
			}
		}
		
		private void setKeyBindings() {
			var actionMap = this.getActionMap();
			var inputMap = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			int ctrl = LookAndFeelUtil.isMac() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;

			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), KeyAction.VK_LEFT);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), KeyAction.VK_RIGHT);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KeyAction.VK_UP);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KeyAction.VK_DOWN);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), KeyAction.VK_ENTER);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), KeyAction.VK_SPACE);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ctrl), KeyAction.VK_CTRL_A);
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ctrl + InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_CTRL_SHIFT_A);
			
			actionMap.put(KeyAction.VK_LEFT, new KeyAction(KeyAction.VK_LEFT));
			actionMap.put(KeyAction.VK_RIGHT, new KeyAction(KeyAction.VK_RIGHT));
			actionMap.put(KeyAction.VK_UP, new KeyAction(KeyAction.VK_UP));
			actionMap.put(KeyAction.VK_DOWN, new KeyAction(KeyAction.VK_DOWN));
			actionMap.put(KeyAction.VK_ENTER, new KeyAction(KeyAction.VK_ENTER));
			actionMap.put(KeyAction.VK_SPACE, new KeyAction(KeyAction.VK_SPACE));
			actionMap.put(KeyAction.VK_CTRL_A, new KeyAction(KeyAction.VK_CTRL_A));
			actionMap.put(KeyAction.VK_CTRL_SHIFT_A, new KeyAction(KeyAction.VK_CTRL_SHIFT_A));
		}
		
		private class KeyAction extends AbstractAction {

			final static String VK_LEFT = "VK_LEFT";
			final static String VK_RIGHT = "VK_RIGHT";
			final static String VK_UP = "VK_UP";
			final static String VK_DOWN = "VK_DOWN";
			final static String VK_ENTER = "VK_ENTER";
			final static String VK_SPACE = "VK_SPACE";
			final static String VK_CTRL_A = "VK_CTRL_A";
			final static String VK_CTRL_SHIFT_A = "VK_CTRL_SHIFT_A";
			
			KeyAction(final String actionCommand) {
				putValue(ACTION_COMMAND_KEY, actionCommand);
			}

			@Override
			public void actionPerformed(ActionEvent evt) {
				var cmd = evt.getActionCommand();
				var focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
				var focusedItem = focusOwner instanceof StylePanel ? (StylePanel) focusOwner : null;
				var dm = StyleGrid.this.getModel();
				
				if (cmd.equals(VK_ENTER) || cmd.equals(VK_SPACE)) {
					if (focusedItem != null)
						setSelectedValue(focusedItem.getStyle(), false);
				} else if (dm.getSize() > 0) {
					var vs = focusedItem != null ? focusedItem.getStyle() : dm.getElementAt(0);
					int size = dm.getSize();
					int idx = indexOf(vs);
					int newIdx = idx;
					
					if (cmd.equals(VK_RIGHT)) {
						newIdx = idx + 1;
					} else if (cmd.equals(VK_LEFT)) {
						newIdx = idx - 1;
					} else if (cmd.equals(VK_UP)) {
						newIdx = idx - cols < 0 ? idx : idx - cols;
					} else if (cmd.equals(VK_DOWN)) {
						final boolean sameRow = Math.ceil(size / (double) cols) == Math.ceil((idx + 1) / (double) cols);
						newIdx = sameRow ? idx : Math.min(size - 1, idx + cols);
					} else if (cmd.equals(VK_CTRL_A)) {
						if (isEditMode())
							selectAll();
					} else if (cmd.equals(VK_CTRL_SHIFT_A)) {
						if (isEditMode())
							deselectAll();
					}
					
					if (newIdx != idx)
						setFocus(newIdx);
				}
			}
		}
	}
	
	class StyleGridModel extends AbstractListModel<VisualStyle>  {

		private final List<VisualStyle> data = new ArrayList<>();

		public StyleGridModel(List<VisualStyle> data) {
			this.data.addAll(data);
		}
		
		@Override
		public int getSize() {
			return data.size();
		}

		@Override
		public VisualStyle getElementAt(int index) {
			return data.get(index);
		}
	}
	
	class StylePanel extends JComponent {
		
		private JLabel imageLabel;
		private JTextField titleTextField;
		
		private final VisualStyle style;
		private boolean dirty = true;

		StylePanel(VisualStyle style) {
			this.style = style;
			init();
			
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent evt) {
					StylePanel.this.requestFocusInWindow();
				}
			});
			this.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					StylePanel.this.repaint();
				}
				@Override
				public void focusLost(FocusEvent e) {
					StylePanel.this.repaint();
				}
			});
		}
		
		VisualStyle getStyle() {
			return style;
		}
		
		private void init() {
			setBackground(BG_COLOR);
			setFocusable(true);
			
			var gap = ITEM_MARGIN + ITEM_PAD;
			setBorder(BorderFactory.createEmptyBorder(gap,  gap,  gap,  gap));
			
			var layout = new GroupLayout(this);
			setLayout(layout);
			layout.setAutoCreateGaps(false);
			layout.setAutoCreateContainerGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
					.addComponent(getImageLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getTitleTextField(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getImageLabel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getTitleTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		JLabel getImageLabel() {
			if (imageLabel == null) {
				imageLabel = new JLabel();
				imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
				imageLabel.setOpaque(true);
				
				int bw = 1;
				imageLabel.setBorder(BorderFactory.createMatteBorder(0, 0, bw, 0, BORDER_COLOR));
				imageLabel.setMinimumSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT + bw));
			}
			
			return imageLabel;
		}
		
		JTextField getTitleTextField() {
			if (titleTextField == null) {
				titleTextField = new JTextField(style.getTitle()) {
					@Override
					public String getToolTipText(MouseEvent evt) {
						boolean isDefault = getProxy().isDefaultStyle(getStyle());
						
					    return "<html><p style='text-align: center;'>" +
								"<b>" + style.getTitle() + "</b>" +
								(isEditMode() && !isDefault ? "<br>(double-click to rename...)" : "") +
								"</p></html>";
					}
				};
				titleTextField.setHorizontalAlignment(SwingConstants.CENTER);
				titleTextField.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
				titleTextField.setBackground(BG_COLOR);
				titleTextField.setEditable(false);
				titleTextField.setFocusable(false);
				LookAndFeelUtil.makeSmall(titleTextField);
			}
			
			return titleTextField;
		}
		
		boolean isDirty() {
			return dirty;
		}
		
		void setDirty() {
			this.dirty = true;
		}
		
		void update() {
			// Image
			var bgPaint = style.getDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
			var bgColor = bgPaint instanceof Color ? (Color) bgPaint : BG_COLOR;
			getImageLabel().setBackground(bgColor);
			
			if (previewNetView != null) {
				var engine = getRenderingEngine(style);
				
				if (engine != null) {
					style.apply(previewNetView);
					previewNetView.updateView();
					previewNetView.fitContent();
					
					var img = engine.createImage(IMAGE_WIDTH, IMAGE_HEIGHT);
					var icon = new ImageIcon(img); 
					getImageLabel().setIcon(icon);
				}
			}
			
			// Title
			getTitleTextField().setText(style.getTitle());
			getTitleTextField().setToolTipText(style.getTitle()); // Otherwise our getToolTipText() won't be called!
			
			dirty = false;
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			
			var g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			int w = this.getWidth();
			int h = this.getHeight();
			int arc = 10;

			boolean selected = isSelected(style);
			boolean focusOwner = this.isFocusOwner();
			
			// Add a colored border if it is the current style
			if (selected) {
				g2d.setColor(SEL_BG_COLOR);
				g2d.setStroke(new BasicStroke(2 * ITEM_BORDER_WIDTH));
				g2d.drawRoundRect(ITEM_MARGIN, ITEM_MARGIN, w - 2 * ITEM_MARGIN, h - 2 * ITEM_MARGIN, arc, arc);
			} else {
				g2d.setColor(BORDER_COLOR);
				g2d.setStroke(new BasicStroke(ITEM_BORDER_WIDTH));
				g2d.drawRoundRect(ITEM_MARGIN, ITEM_MARGIN, w - 2 * ITEM_MARGIN, h - 2 * ITEM_MARGIN, arc, arc);
			}
			
			// Add a colored border and transparent overlay on top if it currently has focus
			if (focusOwner) {
				g2d.setColor(FOCUS_OVERLAY_COLOR);
				g2d.fillRect(ITEM_MARGIN, ITEM_MARGIN, w - 2 * ITEM_MARGIN, h - 2 * ITEM_MARGIN);
				
				g2d.setColor(FOCUS_BORDER_COLOR);
				g2d.setStroke(new BasicStroke(ITEM_BORDER_WIDTH));
				g2d.drawRoundRect(ITEM_MARGIN, ITEM_MARGIN, w - 2 * ITEM_MARGIN, h - 2 * ITEM_MARGIN, arc, arc);
			}

			g2d.dispose();
		}
		
		@Override
		public String toString() {
			return style.getTitle();
		}
	}

// TODO
//	private class KeyAction extends AbstractAction {
//
//		final static String VK_CTRL_A = "VK_CTRL_A";
//		final static String VK_CTRL_SHIFT_A = "VK_CTRL_SHIFT_A";
//		
//		KeyAction(final String actionCommand) {
//			putValue(ACTION_COMMAND_KEY, actionCommand);
//		}
//
//		@Override
//		public void actionPerformed(final ActionEvent e) {
//			final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
//			
//			if (focusOwner instanceof JTextComponent || focusOwner instanceof JTable ||
//					!NetworkViewGrid.this.isVisible() || isEmpty())
//				return; // We don't want to steal the key event from these components
//			
//			final String cmd = e.getActionCommand();
//			
//			if (cmd.equals(VK_CTRL_A))
//				selectAll();
//			else if (cmd.equals(VK_CTRL_SHIFT_A))
//				deselectAll();
//		}
//	}
}