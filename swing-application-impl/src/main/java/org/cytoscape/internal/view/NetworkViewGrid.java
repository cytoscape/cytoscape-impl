package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static org.cytoscape.internal.view.util.ViewUtil.styleToolBarButton;
import static org.cytoscape.util.swing.IconManager.ICON_EXTERNAL_LINK_SQUARE;
import static org.cytoscape.util.swing.IconManager.ICON_TRASH_O;
import static org.cytoscape.util.swing.LookAndFeelUtil.equalizeSize;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_TITLE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import org.cytoscape.internal.util.IconUtil;
import org.cytoscape.internal.util.Util;
import org.cytoscape.internal.view.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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
public class NetworkViewGrid extends JPanel {
	
	public static final String NAME = "__NETWORK_VIEW_GRID__";
	
	public static int MIN_THUMBNAIL_SIZE = 100;
	public static int MAX_THUMBNAIL_SIZE = 500;
	
	private GridPanel gridPanel;
	private JScrollPane gridScrollPane;
	private JPanel toolBar;
	private JLabel infoLabel;
	private JLabel viewSelectionLabel;
	private JButton detachSelectedViewsButton;
	private JButton reattachAllViewsButton;
	private JButton destroySelectedViewsButton;
	private JSlider thumbnailSlider;
	private final GridViewTogglePanel gridViewTogglePanel;
	
	private Map<CyNetworkView, RenderingEngines> enginesMap;
	
	private final TreeMap<CyNetworkView, ThumbnailPanel> thumbnailPanels;
	private CyNetworkView currentNetworkView;
	/** Selected views are saved here because they can be set before this component has any {@link ThumbnailPanel} */
	private final Set<CyNetworkView> selectedNetworkViews;
	private final Set<CyNetworkView> detachedViews;
	private int thumbnailSize;
	private int maxThumbnailSize;
	private boolean dirty = true;
	private boolean fireSelectedNetworkViewsEvent = true;
	
	private DefaultListSelectionModel selectionModel;
	
	private ComponentAdapter componentAdapter;
	
	private int cols;
	
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewGrid(
			GridViewToggleModel gridViewToggleModel,
			Comparator<CyNetworkView> viewComparator,
			CyServiceRegistrar serviceRegistrar
	) {
		this.serviceRegistrar = serviceRegistrar;
		
		enginesMap = new HashMap<>();
		thumbnailPanels = new TreeMap<>(viewComparator);
		selectedNetworkViews = new HashSet<>();
		detachedViews = new HashSet<>();
		
		gridViewTogglePanel = new GridViewTogglePanel(gridViewToggleModel, serviceRegistrar);
		
		selectionModel = new DefaultListSelectionModel();
		// Here is where we listen to the changed indexes in order to:
		// a) select/deselect the the actual items
		// b) call firePropertyChange for the "selectedNetworkViews" property when necessary
		selectionModel.addListSelectionListener(evt -> {
			if (!evt.getValueIsAdjusting()) {
				var oldValue = getSelectedNetworkViews();
				
				var allItems = getAllItems();
				boolean changed = false;
				int first = evt.getFirstIndex();
				int last = evt.getLastIndex();
				
				for (int i = first; i <= last; i++) {
					if (i >= allItems.size())
						break;
					
					var p = allItems.get(i);
					boolean selected = selectionModel.isSelectedIndex(i);
					
					if (p.isSelected() != selected) {
						p.setSelected(selected);
						changed = true;
						
						if (selected)
							selectedNetworkViews.add(p.getNetworkView());
						else
							selectedNetworkViews.remove(p.getNetworkView());
					}
				}
				
				if (changed && fireSelectedNetworkViewsEvent)
					firePropertyChange("selectedNetworkViews", oldValue, getSelectedNetworkViews());
			}
		});
		
		init();
	}
	
	public ThumbnailPanel getItem(CyNetworkView view) {
		return thumbnailPanels.get(view);
	}
	
	public Collection<ThumbnailPanel> getItems() {
		return new ArrayList<>(thumbnailPanels.values());
	}
	
	protected ThumbnailPanel getCurrentItem() {
		return currentNetworkView != null ? thumbnailPanels.get(currentNetworkView) : null;
	}
	
	public ThumbnailPanel firstItem() {
		return thumbnailPanels.firstEntry().getValue();
	}
	
	public ThumbnailPanel lastItem() {
		return thumbnailPanels.lastEntry().getValue();
	}
	
	public int indexOf(final ThumbnailPanel tp) {
		return new ArrayList<CyNetworkView>(thumbnailPanels.keySet()).indexOf(tp.getNetworkView());
	}
	
	public boolean isEmpty() {
		return thumbnailPanels.isEmpty();
	}
	
	public void addItem(RenderingEngine<CyNetwork> re, RenderingEngineFactory<CyNetwork> thumbnailFactory) {
		if (!contains(re)) {
			var oldViews = getAllNetworkViews();
			enginesMap.put((CyNetworkView)re.getViewModel(), new RenderingEngines(re, thumbnailFactory));
			dirty = true;
			firePropertyChange("networkViews", oldViews, getAllNetworkViews());
		}
	}
	
	public void removeItems(Collection<RenderingEngine<CyNetwork>> enginesToRemove) {
		if (enginesToRemove != null && !enginesToRemove.isEmpty()) {
			var oldViews = getAllNetworkViews();
			boolean removed = false;
			
			for (var re : enginesToRemove) {
				if (re != null && enginesMap.remove(re.getViewModel()) != null) {
					removed = true;
					dirty = true;
				}
			}
			
			if (removed) {
				updateToolBar();
				firePropertyChange("networkViews", oldViews, getAllNetworkViews());
			}
		}
	}
	
	public List<CyNetworkView> getAllNetworkViews() {
		return new ArrayList<>(enginesMap.keySet());
	}
	
	public void scrollToCurrentItem() {
		var tp = getCurrentItem();
		
		if (tp != null && tp.getParent() instanceof JComponent) {
			if (!isValid()) // If invalid, the thumbnail panel may not be ready yet, usually with 0 width/height
				validate();
			
			((JComponent) tp.getParent()).scrollRectToVisible(tp.getBounds());
		}
	}
	
	public void dispose() {
		if (componentAdapter != null) {
			removeComponentListener(componentAdapter);
			componentAdapter = null;
		}
		
		removeAll();
	}
	
	private boolean contains(RenderingEngine<CyNetwork> re) {
		return enginesMap.containsKey(re.getViewModel());
	}
	
	void selectAndSetCurrent(ThumbnailPanel item) {
		if (item == null)
			return;
		
		// First select the clicked item
		var allItems = getAllItems();
		setSelectedIndex(allItems.indexOf(item));
		setCurrentNetworkView(item.getNetworkView());
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
		if (index >= thumbnailPanels.size())
			return;
		
		selectionModel.setSelectionInterval(index, index);
	}
	
	protected CyNetworkView getCurrentNetworkView() {
		return currentNetworkView;
	}
	
	protected boolean setCurrentNetworkView(CyNetworkView newView) {
		if ((currentNetworkView == null && newView == null) || 
				(currentNetworkView != null && currentNetworkView.equals(newView)))
			return false;
		
		var oldView = currentNetworkView;
		currentNetworkView = newView;
		
		for (var tp : thumbnailPanels.values())
			tp.update(false);
		
		firePropertyChange("currentNetworkView", oldView, newView);
		
		return true;
	}
	
	protected boolean isDetached(final CyNetworkView view) {
		return detachedViews.contains(view);
	}
	
	protected void setDetached(final CyNetworkView view, final boolean newValue) {
		final boolean oldValue = isDetached(view);
		
		if (newValue != oldValue) {
			if (newValue)
				detachedViews.add(view);
			else
				detachedViews.remove(view);
			
			updateDetachReattachButtons();
		}
	}
	
	/** Updates the whole grid and recreate the thumbnails **/
	protected void update(final int thumbnailSize) {
		dirty = dirty || thumbnailSize < this.thumbnailSize || thumbnailSize > this.maxThumbnailSize;
		this.thumbnailSize = thumbnailSize;
		
		final Dimension size = getSize();
		
		if (!dirty && size != null && size.width > 0) {
			cols = calculateColumns(thumbnailSize, size.width);
			
			if (getGridPanel().getLayout() instanceof GridLayout)
				dirty = cols != ((GridLayout) getGridPanel().getLayout()).getColumns();
			else
				dirty = true;
		}
		
		if (!dirty)
			return;
		
		recreateThumbnails();
		updateToolBar();
	}
	
	protected void updateToolBar() {
		final Collection<ThumbnailPanel> items = getItems();
		final List<ThumbnailPanel> selectedItems = getSelectedItems();
		
		gridViewTogglePanel.update();
		getDestroySelectedViewsButton().setEnabled(!selectedItems.isEmpty());
		
		if (items.isEmpty())
			getViewSelectionLabel().setText(null);
		else
			getViewSelectionLabel().setText(
					selectedItems.size() + " of " + 
							items.size() + " View" + (items.size() == 1 ? "" : "s") +
							" selected");
		
		updateDetachReattachButtons();
		getToolBar().updateUI();
	}
	
	private void updateDetachReattachButtons() {
		boolean hasAttached = false;
		boolean hasDetached = false;
		
		for (ThumbnailPanel tp : getSelectedItems()) {
			if (!isDetached(tp.getNetworkView())) {
				hasAttached = true;
				break;
			}
		}
		for (ThumbnailPanel tp : getItems()) {
			if (isDetached(tp.getNetworkView())) {
				hasDetached = true;
				break;
			}
		}
		
		getDetachSelectedViewsButton().setEnabled(hasAttached);
		getReattachAllViewsButton().setEnabled(hasDetached);
	}
	

	protected int getThumbnailSize() {
		return thumbnailSize;
	}
	
	protected void selectAll() {
		if (!thumbnailPanels.isEmpty())
			selectionModel.setSelectionInterval(0, thumbnailPanels.size() - 1);
	}
	
	protected void deselectAll() {
		setCurrentNetworkView(null);
		setSelectedItems(Collections.emptyList());
	}
	
	void onMousePressedItem(MouseEvent e, ThumbnailPanel item) {
		item.requestFocusInWindow();
		
		if (!e.isPopupTrigger() && SwingUtilities.isLeftMouseButton(e)) {
			// LEFT-CLICK...
			boolean isMac = LookAndFeelUtil.isMac();
			boolean isControlDown = (isMac && e.isMetaDown()) || (!isMac && e.isControlDown());
			
			if (isControlDown) {
				// COMMAND button down on MacOS or CONTROL button down on another OS.
				// Toggle this item's selection state
				toggleSelection(item);
			} else if (e.isShiftDown()) {
				// SHIFT key pressed...
				var allItems = getAllItems();
				int index = allItems.indexOf(item);
				shiftSelectTo(index);
			} else {
				// No SHIFT/CTRL pressed
				selectAndSetCurrent(item);
			}
		}
	}
	
	private void shiftSelectTo(int index) {
		int size = thumbnailPanels.size();
		
		if (index < 0 || index >= size)
			return;
		
		int anchor = selectionModel.getAnchorSelectionIndex();
		int lead = selectionModel.getLeadSelectionIndex();
		
		selectionModel.setValueIsAdjusting(true);
		
		// 1. remove everything between anchor and focus (lead)
		if (anchor != lead && (anchor >= 0 || lead >= 0)) {
			fireSelectedNetworkViewsEvent = false;
			
			try {
				selectionModel.removeIndexInterval(Math.max(0, anchor), Math.max(0, lead));
			} finally {
				fireSelectedNetworkViewsEvent = true;
			}
		}
		
		// 2. add everything between anchor and the new index, which  should also be made the new lead
		selectionModel.addSelectionInterval(Math.max(0, anchor), index);
		
		selectionModel.setValueIsAdjusting(false);
		
		// 3. Make sure the lead component is focused
		var allItems = getAllItems();
		allItems.get(index).requestFocusInWindow();
	}
	
	private void toggleSelection(ThumbnailPanel item) {
		var allItems = getAllItems();
		int index = allItems.indexOf(item);
		
		if (selectionModel.isSelectedIndex(index))
			selectionModel.removeSelectionInterval(index, index);
		else
			selectionModel.addSelectionInterval(index, index);
		
		selectionModel.setValueIsAdjusting(true);
		
		if (selectionModel.isSelectedIndex(index)) {
			selectionModel.setAnchorSelectionIndex(index);
			selectionModel.moveLeadSelectionIndex(index);
		} else {
			index = selectionModel.getMaxSelectionIndex();
			selectionModel.setAnchorSelectionIndex(index);
			selectionModel.moveLeadSelectionIndex(index);
		}
		
		selectionModel.setValueIsAdjusting(false);
	}
	
	protected List<CyNetworkView> getSelectedNetworkViews() {
		return new ArrayList<>(selectedNetworkViews);
	}
	
	protected void setSelectedNetworkViews(Collection<CyNetworkView> views) {
		if (Util.equalSets(views, selectedNetworkViews))
			return;
		
		selectedNetworkViews.clear();
		selectedNetworkViews.addAll(views);
		
		if (thumbnailPanels.isEmpty() || enginesMap.isEmpty())
			return;
		
		var selectedItems = new LinkedHashSet<ThumbnailPanel>();
		
		for (var entry : thumbnailPanels.entrySet()) {
			if (views.contains(entry.getKey()) && enginesMap.containsKey(entry.getKey()))
				selectedItems.add(entry.getValue());
		}
		
		fireSelectedNetworkViewsEvent = false;
		
		try {
			setSelectedItems(selectedItems);
		} finally {
			fireSelectedNetworkViewsEvent = true;
		}
		
		updateToolBar();
	}
	
	private void setSelectedItems(Collection<ThumbnailPanel> items) {
		selectionModel.setValueIsAdjusting(true);
		
		try {
			selectionModel.clearSelection();
			
			var allKeys = new ArrayList<>(thumbnailPanels.keySet());
			int maxIdx = -1;
			
			for (var p : items) {
				int idx = allKeys.indexOf(p.getNetworkView());
				
				if (idx >= 0) {
					selectionModel.addSelectionInterval(idx, idx);
					maxIdx = Math.max(idx, maxIdx);
				}
			}
			
			selectionModel.setAnchorSelectionIndex(maxIdx);
			selectionModel.moveLeadSelectionIndex(maxIdx);
		} finally {
			selectionModel.setValueIsAdjusting(false);
		}
	}
	
	protected List<ThumbnailPanel> getSelectedItems() {
		 var list = new ArrayList<ThumbnailPanel>();
		 
		 for (var entry : thumbnailPanels.entrySet()) {
			 var tp = entry.getValue();
			 
			 if (tp.isSelected())
				 list.add(tp);
		 }
		 
		 return list;
	}
	
	private List<ThumbnailPanel> getAllItems() {
		return new ArrayList<>(thumbnailPanels.values());
	}
	
	@SuppressWarnings("unchecked")
	private void init() {
		setName(NAME);
		setFocusable(true);
		setRequestFocusEnabled(true);
		
		setLayout(new BorderLayout());
		add(getGridScrollPane(), BorderLayout.CENTER);
		add(getToolBar(), BorderLayout.SOUTH);
		
		addComponentListener(componentAdapter = new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				update(thumbnailSize);
				updateToolBar();
			}
			@Override
			public void componentResized(ComponentEvent e) {
				update(thumbnailSize);
			}
		});
		
		setSelectionKeyBindings(this);
		setSelectionKeyBindings(getGridScrollPane().getViewport());
		
		update(thumbnailSize);
	}
	
	private void recreateThumbnails() {
		var size = getSize();
		
		if (size == null || size.width <= 0)
			return;
		
		getGridPanel().removeAll();
		
		for (var tp : thumbnailPanels.values())
			tp.getThumbnailRenderingEngine().ifPresent(RenderingEngine::dispose);
		
		thumbnailPanels.clear();
		
		if (enginesMap == null || enginesMap.isEmpty()) {
			// Just show an info label
			var layout = new GroupLayout(getGridPanel());
			getGridPanel().setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(getInfoLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(getInfoLabel())
					.addGap(0, 0, Short.MAX_VALUE)
			);
			
			// Clear cache
			detachedViews.clear();
			deselectAll();
		} else {
			maxThumbnailSize = maxThumbnailSize(thumbnailSize, size.width);
			
			int cols = calculateColumns(maxThumbnailSize, size.width);
			int rows = calculateRows(enginesMap.size(), cols);
			getGridPanel().setLayout(new GridLayout(rows, cols));
			
			for (var entry : enginesMap.entrySet()) {
				var view = entry.getKey();
				var engines = entry.getValue();
				boolean selected = selectedNetworkViews.contains(view);
				
				var tp = new ThumbnailPanel(engines, maxThumbnailSize, selected);
				thumbnailPanels.put(tp.getNetworkView(), tp);
				
				setSelectionKeyBindings(tp);
			}
			
			for (var tp : thumbnailPanels.values())
				getGridPanel().add(tp);
			
			if (thumbnailPanels.size() < cols) {
				int diff = cols - thumbnailPanels.size();
				
				for (int i = 0; i < diff; i++) {
					var filler = new JPanel();
					filler.setOpaque(false);
					getGridPanel().add(filler);
				}
			}
			
			// Clear cache
			for (var iter = detachedViews.iterator(); iter.hasNext();) {
				if (!thumbnailPanels.containsKey(iter.next()))
					iter.remove();
			}
			
			// Restore the selection
			for (var iter = selectedNetworkViews.iterator(); iter.hasNext();) {
				if (!thumbnailPanels.containsKey(iter.next()))
					iter.remove();
			}
			
			fireSelectedNetworkViewsEvent = false;
			
			try {
				setSelectedItems(getSelectedItems());
			} finally {
				fireSelectedNetworkViewsEvent = true;
			}
			
			if (currentNetworkView != null && !thumbnailPanels.containsKey(currentNetworkView))
				currentNetworkView = null;
		}
		
		dirty = false;
		updateToolBar();
		getGridPanel().updateUI();
		firePropertyChange("thumbnailPanels", null, getAllItems());
	}

	private GridPanel getGridPanel() {
		if (gridPanel == null) {
			gridPanel = new GridPanel();
			gridPanel.setBackground(getBackgroundColor());
			gridPanel.setBorder(BorderFactory.createLineBorder(getBackgroundColor(), 1));
			gridPanel.setOpaque(false);
		}
		
		return gridPanel;
	}
	
	protected JScrollPane getGridScrollPane() {
		if (gridScrollPane == null) {
			gridScrollPane = new JScrollPane(getGridPanel(),
					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			gridScrollPane.getViewport().setBackground(getBackgroundColor());
			gridScrollPane.setBorder(BorderFactory.createEmptyBorder());
			
			gridScrollPane.getViewport().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(final MouseEvent e) {
					if (!e.isPopupTrigger())
						deselectAll();
					
					final Collection<ThumbnailPanel> items = getItems();
					
					if (!items.isEmpty())
						items.iterator().next().requestFocusInWindow();
				}
			});
		}
		
		return gridScrollPane;
	}
	
	private JPanel getToolBar() {
		if (toolBar == null) {
			toolBar = new JPanel();
			toolBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
			
			final JSeparator sep1 = ViewUtil.createToolBarSeparator();
			final JSeparator sep2 = ViewUtil.createToolBarSeparator();
			final JSeparator sep3 = ViewUtil.createToolBarSeparator();
			final JSeparator sep4 = ViewUtil.createToolBarSeparator();
			
			final GroupLayout layout = new GroupLayout(toolBar);
			toolBar.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(gridViewTogglePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(RELATED)
					.addComponent(sep1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(RELATED)
					.addComponent(getDetachSelectedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(RELATED)
					.addComponent(getReattachAllViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(RELATED)
					.addComponent(sep2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 10, Short.MAX_VALUE)
					.addComponent(getViewSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 10, Short.MAX_VALUE)
					.addComponent(sep3, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(RELATED)
					.addComponent(getDestroySelectedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(RELATED)
					.addComponent(sep4, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(RELATED)
					.addComponent(getThumbnailSlider(), 100, 100, 100)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(1)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(gridViewTogglePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(sep1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getDetachSelectedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getReattachAllViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(sep2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getViewSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(sep3, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getDestroySelectedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(sep4, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getThumbnailSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGap(1)
			);
			
			equalizeSize(gridViewTogglePanel.getGridModeButton(), gridViewTogglePanel.getViewModeButton(),
					getDetachSelectedViewsButton(), getReattachAllViewsButton(), getDestroySelectedViewsButton());
		}
		
		return toolBar;
	}
	
	JButton getDetachSelectedViewsButton() {
		if (detachSelectedViewsButton == null) {
			detachSelectedViewsButton = new JButton(ICON_EXTERNAL_LINK_SQUARE);
			detachSelectedViewsButton.setToolTipText("Detach Selected Views");
			styleToolBarButton(detachSelectedViewsButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
		}
		
		return detachSelectedViewsButton;
	}
	
	JButton getReattachAllViewsButton() {
		if (reattachAllViewsButton == null) {
			reattachAllViewsButton = new JButton(IconUtil.PIN_ALL);
			reattachAllViewsButton.setToolTipText("Reattach All Views");
			styleToolBarButton(reattachAllViewsButton,
					serviceRegistrar.getService(IconManager.class).getIconFont(IconUtil.CY_FONT_NAME, 16.0f));
		}
		
		return reattachAllViewsButton;
	}
	
	JButton getDestroySelectedViewsButton() {
		if (destroySelectedViewsButton == null) {
			destroySelectedViewsButton = new JButton(ICON_TRASH_O);
			destroySelectedViewsButton.setToolTipText("Destroy Selected Views");
			styleToolBarButton(destroySelectedViewsButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
		}
		
		return destroySelectedViewsButton;
	}
	
	private JLabel getInfoLabel() {
		if (infoLabel == null) {
			infoLabel = new JLabel("No views");
			infoLabel.setFont(infoLabel.getFont().deriveFont(18.0f).deriveFont(Font.BOLD));
			infoLabel.setEnabled(false);
			
			Color c = UIManager.getColor("Label.disabledForeground");
			c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 120);
			infoLabel.setForeground(c);
		}
		
		return infoLabel;
	}
	
	private JLabel getViewSelectionLabel() {
		if (viewSelectionLabel == null) {
			viewSelectionLabel = new JLabel();
			viewSelectionLabel.setHorizontalAlignment(JLabel.CENTER);
			viewSelectionLabel.setFont(viewSelectionLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		}
		
		return viewSelectionLabel;
	}
	
	protected JSlider getThumbnailSlider() {
		if (thumbnailSlider == null) {
			final int value = Math.round(MIN_THUMBNAIL_SIZE + (MAX_THUMBNAIL_SIZE - MIN_THUMBNAIL_SIZE) / 3.0f);
			thumbnailSlider = new JSlider(MIN_THUMBNAIL_SIZE, MAX_THUMBNAIL_SIZE, value);
			thumbnailSlider.setToolTipText("Thumbnail Size");
			thumbnailSlider.putClientProperty("JComponent.sizeVariant", "mini"); // Aqua (Mac OS X) only
			
			thumbnailSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (!thumbnailSlider.getValueIsAdjusting()) {
						final int thumbSize = thumbnailSlider.getValue();
						update(thumbSize);
					}
				}
			});
		}
		
		return thumbnailSlider;
	}
	
	private void setSelectionKeyBindings(JComponent comp) {
		var actionMap = comp.getActionMap();
		var inputMap = comp.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		int CTRL = LookAndFeelUtil.isMac() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), KeyAction.VK_LEFT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), KeyAction.VK_RIGHT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KeyAction.VK_UP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KeyAction.VK_DOWN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_SHIFT_LEFT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_SHIFT_RIGHT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_SHIFT_UP);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_SHIFT_DOWN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, CTRL), KeyAction.VK_CTRL_A);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, CTRL + InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_CTRL_SHIFT_A);
		
		actionMap.put(KeyAction.VK_LEFT, new KeyAction(KeyAction.VK_LEFT));
		actionMap.put(KeyAction.VK_RIGHT, new KeyAction(KeyAction.VK_RIGHT));
		actionMap.put(KeyAction.VK_UP, new KeyAction(KeyAction.VK_UP));
		actionMap.put(KeyAction.VK_DOWN, new KeyAction(KeyAction.VK_DOWN));
		actionMap.put(KeyAction.VK_SHIFT_LEFT, new KeyAction(KeyAction.VK_SHIFT_LEFT));
		actionMap.put(KeyAction.VK_SHIFT_RIGHT, new KeyAction(KeyAction.VK_SHIFT_RIGHT));
		actionMap.put(KeyAction.VK_SHIFT_UP, new KeyAction(KeyAction.VK_SHIFT_UP));
		actionMap.put(KeyAction.VK_SHIFT_DOWN, new KeyAction(KeyAction.VK_SHIFT_DOWN));
		actionMap.put(KeyAction.VK_CTRL_A, new KeyAction(KeyAction.VK_CTRL_A));
		actionMap.put(KeyAction.VK_CTRL_SHIFT_A, new KeyAction(KeyAction.VK_CTRL_SHIFT_A));
	}
	
	private static int calculateColumns(int thumbnailSize, int gridWidth) {
		return thumbnailSize > 0 ? Math.floorDiv(gridWidth, thumbnailSize) : 0;
	}
	
	private static int calculateRows(int total, int cols) {
		return (int) Math.round(Math.ceil((float) total / (float) cols));
	}

	private static int maxThumbnailSize(int thumbnailSize, int gridWidth) {
		thumbnailSize = Math.max(thumbnailSize, MIN_THUMBNAIL_SIZE);
		thumbnailSize = Math.min(thumbnailSize, MAX_THUMBNAIL_SIZE);
		thumbnailSize = Math.min(thumbnailSize, gridWidth);

		return thumbnailSize;
	}
	
	private static Color getBackgroundColor() {
		return UIManager.getColor("Panel.background");
	}
	
	static List<CyNetworkView> getNetworkViews(final Collection<ThumbnailPanel> thumbnailPanels) {
		final List<CyNetworkView> views = new ArrayList<>();
		
		for (ThumbnailPanel tp : thumbnailPanels)
			views.add(tp.getNetworkView());
		
		return views;
	}
	
	private static class RenderingEngines {
		public final RenderingEngine<CyNetwork> networkEngine;
		public final Optional<RenderingEngineFactory<CyNetwork>> thumbnailEngineFactory;
		
		public RenderingEngines(RenderingEngine<CyNetwork> networkEngine, RenderingEngineFactory<CyNetwork> thumbnailEngineFactory) {
			this.networkEngine = networkEngine;
			this.thumbnailEngineFactory = Optional.ofNullable(thumbnailEngineFactory);
		}
	}
	
	class ThumbnailPanel extends JPanel {
		
		static final int PAD = 4;
		static final int GAP = 1;
		
		/** Margin Thickness */
		static final int MT = 1;
		/** Border Thickness */
		static final int BT = 1;
		/** Padding Thickness */
		static final int PT = 1;
		/** Total border thickness */
		static final int BORDER_WIDTH = MT + BT + PT;
		
		static final int IMG_BORDER_WIDTH = 1;
		
		private JLabel currentLabel;
		private JLabel titleLabel;
		private JRootPane imagePanel;
		private Optional<RenderingEngine<?>> thumbnailRenderer = Optional.empty();
		
		private boolean selected;
		
		private final RenderingEngines engines;
		
		private final Color BORDER_COLOR = UIManager.getColor("Separator.foreground");
		private final Color SEL_COLOR = UIManager.getColor("Table.focusCellBackground");
		
		private Border EMPTY_BORDER = BorderFactory.createEmptyBorder(PT, PT, PT, PT);
		private Border MARGIN_BORDER = BorderFactory.createLineBorder(getBackgroundColor(), MT);
		private Border SIMPLE_BORDER = BorderFactory.createLineBorder(BORDER_COLOR, BT);
		
		private Border DEFAULT_BORDER = BorderFactory.createCompoundBorder(
				MARGIN_BORDER,
				BorderFactory.createCompoundBorder(
						SIMPLE_BORDER,
						EMPTY_BORDER
				)
		);
		private Border DEFAULT_HOVER_BORDER = BorderFactory.createCompoundBorder(
				MARGIN_BORDER,
				BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(SEL_COLOR, BT),
						EMPTY_BORDER
				)
		);
		private Border MIDDLE_SIBLING_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(MT, 0, MT, 0, getBackgroundColor()),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(BT, 0, BT, 0, BORDER_COLOR),
						EMPTY_BORDER
				)
		);
		private Border FIRST_SIBLING_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(MT, MT, MT, 0, getBackgroundColor()),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(BT, BT, BT, 0, BORDER_COLOR),
						EMPTY_BORDER
				)
		);
		private Border LAST_SIBLING_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(MT, 0, MT, MT, getBackgroundColor()),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(BT, 0, BT, BT, BORDER_COLOR),
						EMPTY_BORDER
				)
		);
		private Border MIDDLE_SIBLING_SEL_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(MT, 0, MT, 0, getBackgroundColor()),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(BT, BT, BT, BT, SEL_COLOR),
						BorderFactory.createEmptyBorder(PT, 0, PT, 0)
				)
		);
		private Border FIRST_SIBLING_SEL_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(MT, MT, MT, 0, getBackgroundColor()),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(BT, BT, BT, BT, SEL_COLOR),
						BorderFactory.createEmptyBorder(PT, PT, PT, 0)
				)
		);
		private Border LAST_SIBLING_SEL_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(MT, 0, MT, MT, getBackgroundColor()),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(BT, BT, BT, BT, SEL_COLOR),
						BorderFactory.createEmptyBorder(PT, 0, PT, PT)
				)
		);
		
		ThumbnailPanel(RenderingEngines engines, int size, boolean selected) {
			this.engines = engines;
			this.selected = selected;
			
			this.setFocusable(true);
			this.setRequestFocusEnabled(true);
			
			this.setBorder(DEFAULT_BORDER);
			this.setBackground(UIManager.getColor("Table.background"));
			
			final Dimension d = new Dimension(size - BORDER_WIDTH, size - BORDER_WIDTH);
			this.setMinimumSize(d);
			this.setPreferredSize(d);
			
			final int CURR_LABEL_W = getCurrentLabel().getWidth();
			
			final GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addGroup(layout.createSequentialGroup()
							.addGap(PAD)
							.addComponent(getCurrentLabel(), CURR_LABEL_W, CURR_LABEL_W, CURR_LABEL_W)
							.addGap(GAP, GAP, Short.MAX_VALUE)
							.addComponent(getTitleLabel())
							.addGap(GAP, GAP, Short.MAX_VALUE)
							.addGap(CURR_LABEL_W)
							.addGap(PAD)
					)
					.addGroup(layout.createSequentialGroup()
							.addGap(PAD, PAD, PAD)
							.addComponent(getImagePanel(), PREFERRED_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addGap(PAD, PAD, PAD)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(GAP)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(getCurrentLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGap(GAP)
					.addComponent(getImagePanel(), PREFERRED_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(PAD, PAD, PAD)
			);
			
			this.update(true);
			
			this.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					updateTitleLabel();
				}
			});
		}
		
		@Override
		public Color getBackground() {
			return UIManager.getColor(selected ? "Table.focusCellBackground" : "Table.background");
		}
		
		private void setSelected(boolean newValue) {
			if (this.selected != newValue) {
				final boolean oldValue = this.selected;
				this.selected = newValue;
				this.updateBorder();
				this.repaint();
				
				firePropertyChange("selected", oldValue, newValue);
			}
		}
		
		boolean isSelected() {
			return selected;
		}
		
		boolean isCurrent() {
			return getNetworkView().equals(currentNetworkView);
		}
		
		boolean isFirstSibling() {
			final CyNetworkView netView = getNetworkView();
			final CyNetwork net = netView.getModel();
			final Entry<CyNetworkView, ThumbnailPanel> previous = thumbnailPanels.lowerEntry(netView);
			final Entry<CyNetworkView, ThumbnailPanel> next = thumbnailPanels.higherEntry(netView);
			
			return ((previous == null || !previous.getKey().getModel().equals(net))
					&& next != null && next.getKey().getModel().equals(net));
		}
		
		boolean isMiddleSibling() {
			final CyNetworkView netView = getNetworkView();
			final CyNetwork net = netView.getModel();
			final Entry<CyNetworkView, ThumbnailPanel> previous = thumbnailPanels.lowerEntry(netView);
			final Entry<CyNetworkView, ThumbnailPanel> next = thumbnailPanels.higherEntry(netView);
			
			return (previous != null && previous.getKey().getModel().equals(net)
					&& next != null && next.getKey().getModel().equals(net));
		}
		
		boolean isLastSibling() {
			final CyNetworkView netView = getNetworkView();
			final CyNetwork net = netView.getModel();
			final Entry<CyNetworkView, ThumbnailPanel> previous = thumbnailPanels.lowerEntry(netView);
			final Entry<CyNetworkView, ThumbnailPanel> next = thumbnailPanels.higherEntry(netView);
			
			return ((next == null || !next.getKey().getModel().equals(net))
					&& previous != null && previous.getKey().getModel().equals(net));
		}
		
		void update(final boolean redraw) {
			updateCurrentLabel();
			updateBorder();
			updateTitleLabel();
		}
		
		private void updateCurrentLabel() {
			getCurrentLabel().setText(isCurrent() ? IconManager.ICON_CIRCLE : " ");
			getCurrentLabel().setToolTipText(isCurrent() ? "Current View" : null);
		}
		
		private void updateBorder() {
			if (isFirstSibling())
				setBorder(selected ? FIRST_SIBLING_SEL_BORDER : FIRST_SIBLING_BORDER);
			else if (isMiddleSibling())
				setBorder(selected? MIDDLE_SIBLING_SEL_BORDER : MIDDLE_SIBLING_BORDER);
			else if (isLastSibling())
				setBorder(selected? LAST_SIBLING_SEL_BORDER : LAST_SIBLING_BORDER);
			else
				setBorder(selected ? DEFAULT_HOVER_BORDER : DEFAULT_BORDER);
		}
		
		private void updateTitleLabel() {
			var title = ViewUtil.getTitle(getNetworkView());
//			var netName = ViewUtil.getName(getNetworkView().getModel());
			setToolTipText(title);
// TODO Use this one when multiple views is supported, to show the network name			
//			setToolTipText("<html><center>" + title + "<br>(" + netName + ")</center></html>");
			getTitleLabel().setText(title);
		}
		
		CyNetworkView getNetworkView() {
			return (CyNetworkView) engines.networkEngine.getViewModel();
		}
		
		JLabel getCurrentLabel() {
			if (currentLabel == null) {
				currentLabel = new ThumbnailLabel(IconManager.ICON_CIRCLE, true); // Just to get the preferred size with the icon font
				currentLabel.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(10.0f));
				currentLabel.setMinimumSize(currentLabel.getPreferredSize());
				currentLabel.setMaximumSize(currentLabel.getPreferredSize());
				currentLabel.setSize(currentLabel.getPreferredSize());
			}
			
			return currentLabel;
		}
		
		JLabel getTitleLabel() {
			if (titleLabel == null) {
				titleLabel = new ThumbnailLabel(false);
				titleLabel.setHorizontalAlignment(JLabel.CENTER);
				titleLabel.setFont(titleLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
				titleLabel.setMinimumSize(titleLabel.getPreferredSize());
			}
			
			return titleLabel;
		}
		
		JRootPane getImagePanel() {
			if (imagePanel == null) {
				imagePanel = new JRootPane();
				imagePanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Label.foreground"), IMG_BORDER_WIDTH));
				
				imagePanel.getGlassPane().setVisible(true);
				Container contentPane = imagePanel.getContentPane();
				
				if (engines.thumbnailEngineFactory.isPresent()) {
					RenderingEngineFactory<CyNetwork> engineFactory = engines.thumbnailEngineFactory.get();
					CyNetworkView netView = getNetworkView();
					thumbnailRenderer = Optional.of(engineFactory.createRenderingEngine(contentPane, netView));
				} else {
					JLabel label = new JLabel(IconManager.ICON_SHARE_ALT_SQUARE);
					label.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(40.0f));
					Color c = UIManager.getColor("Label.disabledForeground");
					c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 40);
					label.setForeground(c);

					label.setHorizontalAlignment(JLabel.CENTER);
					label.setVerticalAlignment(JLabel.CENTER);
					contentPane.setLayout(new BorderLayout());
					contentPane.add(label, BorderLayout.CENTER);
					thumbnailRenderer = Optional.empty();
				}
			}
			
			return imagePanel;
		}
		
		public Optional<RenderingEngine<?>> getThumbnailRenderingEngine() {
			return thumbnailRenderer;
		}
		
		@Override
		public String toString() {
			return getNetworkView().getVisualProperty(NETWORK_TITLE);
		}

		private class ThumbnailLabel extends JLabel {

			private final boolean highlightWhenCurrent;

			public ThumbnailLabel(final boolean highlightWhenCurrent) {
				this.highlightWhenCurrent = highlightWhenCurrent;
			}

			public ThumbnailLabel(final String text, final boolean highlightWhenCurrent) {
				this(highlightWhenCurrent);
				setText(text);
			}
			
			@Override
			public Color getForeground() {
				final String defColor = highlightWhenCurrent ? "Focus.color" : "Label.foreground";
				return UIManager.getColor(selected ? "Table.focusCellForeground" : defColor);
			}
		}
	}
	
	private class GridPanel extends JPanel implements Scrollable {
		
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
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
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return thumbnailPanels == null || thumbnailPanels.isEmpty();
		}
	}
	
	private class KeyAction extends AbstractAction {

		final static String VK_LEFT = "VK_LEFT";
		final static String VK_RIGHT = "VK_RIGHT";
		final static String VK_UP = "VK_UP";
		final static String VK_DOWN = "VK_DOWN";
		final static String VK_SHIFT_LEFT = "VK_SHIFT_LEFT";
		final static String VK_SHIFT_RIGHT = "VK_SHIFT_RIGHT";
		final static String VK_SHIFT_UP = "VK_SHIFT_UP";
		final static String VK_SHIFT_DOWN = "VK_SHIFT_DOWN";		
		final static String VK_CTRL_A = "VK_CTRL_A";
		final static String VK_CTRL_SHIFT_A = "VK_CTRL_SHIFT_A";
		
		KeyAction(String actionCommand) {
			putValue(ACTION_COMMAND_KEY, actionCommand);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			var focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			
			if (focusOwner instanceof JTextComponent || focusOwner instanceof JTable ||
					!NetworkViewGrid.this.isVisible() || isEmpty())
				return; // We don't want to steal the key event from these components
			
			var cmd = e.getActionCommand();
			boolean shift = cmd.startsWith("VK_SHIFT_");
			int size = thumbnailPanels.size();
			int idx = selectionModel.getLeadSelectionIndex();
			int newIdx = idx;
			
			if (cmd.equals(VK_RIGHT) || cmd.equals(VK_SHIFT_RIGHT)) {
				newIdx = idx + 1;
			} else if (cmd.equals(VK_LEFT) || cmd.equals(VK_SHIFT_LEFT)) {
				newIdx = idx - 1;
			} else if (cmd.equals(VK_UP) || cmd.equals(VK_SHIFT_UP)) {
				newIdx = idx - cols < 0 ? idx : idx - cols;
			} else if (cmd.equals(VK_DOWN) || cmd.equals(VK_SHIFT_DOWN)) {
				boolean sameRow = Math.ceil(size / (double) cols) == Math.ceil((idx + 1) / (double) cols);
				newIdx = sameRow ? idx : Math.min(size - 1, idx + cols);
			} else if (cmd.equals(VK_CTRL_A)) {
				selectAll();
			} else if (cmd.equals(VK_CTRL_SHIFT_A)) {
				deselectAll();
			}
			
			if (newIdx != idx) {
				if (shift)
					shiftSelectTo(newIdx);
				else
					setSelectedIndex(newIdx);
			}
		}
	}
}
