package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.*;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.util.ViewUtil.styleToolBarButton;
import static org.cytoscape.util.swing.IconManager.*;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_TITLE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
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
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import org.cytoscape.internal.util.Util;
import org.cytoscape.internal.util.ViewUtil;
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
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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
	
	private Map<CyNetworkView, RenderingEngines> engines;
	
	private final TreeMap<CyNetworkView, ThumbnailPanel> thumbnailPanels;
	private CyNetworkView currentNetworkView;
	private final List<CyNetworkView> selectedNetworkViews;
	private final Set<CyNetworkView> detachedViews;
	private int thumbnailSize;
	private int maxThumbnailSize;
	private boolean dirty = true;
	private boolean ignoreSelectedItemsEvent;
	private final Comparator<CyNetworkView> viewComparator;
	
	private ThumbnailPanel selectionHead;
	private ThumbnailPanel selectionTail;
	
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewGrid(
			final GridViewToggleModel gridViewToggleModel,
			final Comparator<CyNetworkView> viewComparator,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.viewComparator = viewComparator;
		this.serviceRegistrar = serviceRegistrar;
		
		engines = new HashMap<>();
		thumbnailPanels = new TreeMap<>(viewComparator);
		selectedNetworkViews = new ArrayList<>();
		detachedViews = new HashSet<>();
		
		gridViewTogglePanel = new GridViewTogglePanel(gridViewToggleModel, serviceRegistrar);
		
		init();
	}
	
	public ThumbnailPanel getItem(final CyNetworkView view) {
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
	
	public void addItem(final RenderingEngine<CyNetwork> re, final RenderingEngineFactory<CyNetwork> thumbnailFactory) {
		if (!contains(re)) {
			final Collection<CyNetworkView> oldViews = getNetworkViews();
			engines.put((CyNetworkView)re.getViewModel(), new RenderingEngines(re, thumbnailFactory));
			dirty = true;
			firePropertyChange("networkViews", oldViews, getNetworkViews());
		}
	}
	
	public void removeItems(final Collection<RenderingEngine<CyNetwork>> enginesToRemove) {
		if (enginesToRemove != null && !enginesToRemove.isEmpty()) {
			final Collection<CyNetworkView> oldViews = getNetworkViews();
			boolean removed = false;
			
			for (RenderingEngine<CyNetwork> re : enginesToRemove) {
				if (re != null && engines.remove(re.getViewModel()) != null) {
					removed = true;
					dirty = true;
				}
			}
			
			if (removed) {
				updateToolBar();
				firePropertyChange("networkViews", oldViews, getNetworkViews());
			}
		}
	}
	
	public Collection<CyNetworkView> getNetworkViews() {
		return new ArrayList<>(engines.keySet());
	}
	
	public void scrollToCurrentItem() {
		final ThumbnailPanel tp = getCurrentItem();
		
		if (tp != null && tp.getParent() instanceof JComponent) {
			if (!isValid()) // If invalid, the thumbnail panel may not be ready yet, usually with 0 width/height
				validate();
			
			((JComponent) tp.getParent()).scrollRectToVisible(tp.getBounds());
		}
	}
	
	private boolean contains(final RenderingEngine<CyNetwork> re) {
		return engines.containsKey(re.getViewModel());
	}
	
	protected CyNetworkView getCurrentNetworkView() {
		return currentNetworkView;
	}
	
	protected boolean setCurrentNetworkView(final CyNetworkView newView) {
		if ((currentNetworkView == null && newView == null) || 
				(currentNetworkView != null && currentNetworkView.equals(newView)))
			return false;
		
		final CyNetworkView oldView = currentNetworkView;
		currentNetworkView = newView;
		
		for (ThumbnailPanel tp : thumbnailPanels.values())
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
			final int cols = calculateColumns(thumbnailSize, size.width);
			
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
		setSelectedItems(getItems());
	}
	
	protected void deselectAll() {
		setCurrentNetworkView(null);
		setSelectedItems(Collections.emptyList());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void onMousePressedItem(final MouseEvent e, final ThumbnailPanel item) {
		item.requestFocusInWindow();
		
		if (e.isPopupTrigger()) {
			selectionHead = item;
		} else if (SwingUtilities.isLeftMouseButton(e)) {
			// LEFT-CLICK...
			final Set<ThumbnailPanel> oldValue = new HashSet<>(getSelectedItems());
			boolean changed = false;
			
			final boolean isMac = LookAndFeelUtil.isMac();
			
			if ((isMac && e.isMetaDown()) || (!isMac && e.isControlDown())) {
				// COMMAND button down on MacOS or CONTROL button down on another OS.
				// Toggle this item's selection state
				item.setSelected(!item.isSelected());
				changed = true;
				
				// Find new selection range head
				selectionHead = item.isSelected() ? item : findNextSelectionHead(selectionHead);
			} else {
				if (e.isShiftDown()) {
					if (selectionHead != null && selectionHead.isVisible() && selectionHead.isSelected()
							&& selectionHead != item) {
						// First deselect previous range, if there is a tail
						if (selectionTail != null)
							changeRangeSelection(selectionHead, selectionTail, false);
						// Now select the new range
						changeRangeSelection(selectionHead, (selectionTail = item), true);
					} else if (!item.isSelected()) {
						item.setSelected(true);
						changed = true;
					}
				} else {
					setSelectedItems((Set) (Collections.singleton(item)));
					
					if (!item.isCurrent())
						setCurrentNetworkView(item.getNetworkView());
				}
				
				if (getSelectedItems().size() == 1)
					selectionHead = item;
			}
			
			if (changed)
				firePropertyChange("selectedItems", oldValue, getSelectedItems());
		}
	}
	
	protected List<CyNetworkView> getSelectedNetworkViews() {
		return new ArrayList<>(selectedNetworkViews);
	}
	
	protected void setSelectedNetworkViews(final Collection<CyNetworkView> networkViews) {
		if (Util.equalSets(networkViews, selectedNetworkViews))
			return;
		
		final List<CyNetworkView> oldValue = new ArrayList<>(selectedNetworkViews);
		selectedNetworkViews.clear();
		
		if (networkViews != null)
			selectedNetworkViews.addAll(networkViews);
		
		final Set<ThumbnailPanel> selectedItems = new HashSet<>();
		
		for (ThumbnailPanel tp : getItems()) {
			if (selectedNetworkViews.contains(tp.getNetworkView()))
				selectedItems.add(tp);
		}
		
		ignoreSelectedItemsEvent = true;
		
		try {
			setSelectedItems(selectedItems);
		} finally {
			ignoreSelectedItemsEvent = false;
		}
		
		updateToolBar();
		firePropertyChange("selectedNetworkViews", oldValue, new HashSet<>(selectedNetworkViews));
	}
	
	private void setSelectedItems(final Collection<ThumbnailPanel> selectedItems) {
		final Set<ThumbnailPanel> oldValue = new HashSet<>(getSelectedItems());
		boolean changed = false;
		
		for (final ThumbnailPanel tp : thumbnailPanels.values()) {
			final boolean selected = selectedItems != null && selectedItems.contains(tp);
			
			if (tp.isSelected() != selected) {
				tp.setSelected(selected);
				changed = true;
			}

			if (!tp.isSelected()) {
				if (tp == selectionHead) selectionHead = null;
				if (tp == selectionTail) selectionTail = null;
			}
		}
		
		if (changed)
			firePropertyChange("selectedItems", oldValue, getSelectedItems());
	}
	
	protected List<ThumbnailPanel> getSelectedItems() {
		 final List<ThumbnailPanel> list = new ArrayList<>();
		 
		 for (Entry<CyNetworkView, ThumbnailPanel> entry : thumbnailPanels.entrySet()) {
			 final ThumbnailPanel tp = entry.getValue();
			 
			 if (tp.isSelected())
				 list.add(tp);
		 }
		 
		 return list;
	}
	
	private void changeRangeSelection(final ThumbnailPanel item1, final ThumbnailPanel item2,
			final boolean selected) {
		final Set<ThumbnailPanel> oldValue = new HashSet<>(getSelectedItems());
		boolean changed = false;
		
		final NavigableMap<CyNetworkView, ThumbnailPanel> subMap;
		
		if (viewComparator.compare(item1.getNetworkView(), item2.getNetworkView()) <= 0)
			subMap = thumbnailPanels.subMap(item1.getNetworkView(), false, item2.getNetworkView(), true);
		else
			subMap = thumbnailPanels.subMap(item2.getNetworkView(), true, item1.getNetworkView(), false);
				
		for (final Map.Entry<CyNetworkView, ThumbnailPanel> entry : subMap.entrySet()) {
			final ThumbnailPanel nextItem = entry.getValue();
			
			if (nextItem.isVisible()) {
				if (nextItem.isSelected() != selected) {
					nextItem.setSelected(selected);
					changed = true;
				}
			}
		}
		
		if (changed)
			firePropertyChange("selectedItems", oldValue, getSelectedItems());
	}
	
	private ThumbnailPanel findNextSelectionHead(final ThumbnailPanel fromItem) {
		ThumbnailPanel head = null;
		
		if (fromItem != null) {
			NavigableMap<CyNetworkView, ThumbnailPanel> subMap =
					thumbnailPanels.tailMap(fromItem.getNetworkView(), false);
			
			// Try with the tail subset first
			for (final Map.Entry<CyNetworkView, ThumbnailPanel> entry : subMap.entrySet()) {
				final ThumbnailPanel nextItem = entry.getValue();
				
				if (nextItem.isVisible() && nextItem.isSelected()) {
					head = nextItem;
					break;
				}
			}
			
			if (head == null) {
				// Try with the head subset
				subMap = thumbnailPanels.headMap(fromItem.getNetworkView(), false);
				final NavigableMap<CyNetworkView, ThumbnailPanel> descMap = subMap.descendingMap();
				
				for (final Map.Entry<CyNetworkView, ThumbnailPanel> entry : descMap.entrySet()) {
					final ThumbnailPanel nextItem = entry.getValue();
					
					if (nextItem.isVisible() && nextItem.isSelected()) {
						head = nextItem;
						break;
					}
				}
			}
		}
		
		return head;
	}
	
	@SuppressWarnings("unchecked")
	private void init() {
		setName(NAME);
		setFocusable(true);
		setRequestFocusEnabled(true);
		
		setLayout(new BorderLayout());
		add(getGridScrollPane(), BorderLayout.CENTER);
		add(getToolBar(), BorderLayout.SOUTH);
		
		addComponentListener(new ComponentAdapter() {
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
		
		addPropertyChangeListener("selectedItems", (PropertyChangeEvent e) -> {
			if (!ignoreSelectedItemsEvent)
				setSelectedNetworkViews(getNetworkViews((Collection<ThumbnailPanel>) e.getNewValue()));
		});
	}
	
	private void recreateThumbnails() {
		final Dimension size = getSize();
		
		if (size == null || size.width <= 0)
			return;
		
		final List<ThumbnailPanel> previousSelection = getSelectedItems();
		
		getGridPanel().removeAll();
		
		for(ThumbnailPanel tp : thumbnailPanels.values()) {
			tp.getThumbnailRenderingEngine().dispose();
		}
		thumbnailPanels.clear();
		
		if (engines == null || engines.isEmpty()) {
			// Just show an info label
			final GroupLayout layout = new GroupLayout(getGridPanel());
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
			
			detachedViews.clear();
		} else {
			maxThumbnailSize = maxThumbnailSize(thumbnailSize, size.width);
			
			int cols = calculateColumns(maxThumbnailSize, size.width);
			int rows = calculateRows(engines.size(), cols);
			getGridPanel().setLayout(new GridLayout(rows, cols));
			
			for (RenderingEngines engines : engines.values()) {
				final ThumbnailPanel tp = new ThumbnailPanel(engines, maxThumbnailSize);
				thumbnailPanels.put(tp.getNetworkView(), tp);
				
				setSelectionKeyBindings(tp);
				
				if (previousSelection.contains(tp))
					tp.setSelected(true);
			}
			
			for (Map.Entry<CyNetworkView, ThumbnailPanel> entry : thumbnailPanels.entrySet())
				getGridPanel().add(entry.getValue());
			
			if (thumbnailPanels.size() < cols) {
				final int diff = cols - thumbnailPanels.size();
				
				for (int i = 0; i < diff; i++) {
					final JPanel filler = new JPanel();
					filler.setOpaque(false);
					getGridPanel().add(filler);
				}
			}
			
			for (Iterator<CyNetworkView> iter = detachedViews.iterator(); iter.hasNext();) {
				if (!thumbnailPanels.containsKey(iter.next()))
					iter.remove();
			}
		}
		
		dirty = false;
		updateToolBar();
		getGridPanel().updateUI();
		firePropertyChange("thumbnailPanels", null, new ArrayList<>(thumbnailPanels.values()));
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
			
			final JSeparator sep1 = new JSeparator(JSeparator.VERTICAL);
			final JSeparator sep2 = new JSeparator(JSeparator.VERTICAL);
			final JSeparator sep3 = new JSeparator(JSeparator.VERTICAL);
			final JSeparator sep4 = new JSeparator(JSeparator.VERTICAL);
			
			final GroupLayout layout = new GroupLayout(toolBar);
			toolBar.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(gridViewTogglePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(sep1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getDetachSelectedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getReattachAllViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(sep2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 10, Short.MAX_VALUE)
					.addComponent(getViewSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 10, Short.MAX_VALUE)
					.addComponent(sep3, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getDestroySelectedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(sep4, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getThumbnailSlider(), 100, 100, 100)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
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
			);
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
			reattachAllViewsButton = new JButton(ICON_THUMB_TACK + " " + ICON_THUMB_TACK);
			reattachAllViewsButton.setToolTipText("Reattach All Views");
			styleToolBarButton(reattachAllViewsButton, serviceRegistrar.getService(IconManager.class).getIconFont(14.0f));
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
			infoLabel.setFont(infoLabel.getFont().deriveFont(18.0f));
			infoLabel.setEnabled(false);
			infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
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
	
	private void setSelectionKeyBindings(final JComponent comp) {
		final ActionMap actionMap = comp.getActionMap();
		final InputMap inputMap = comp.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		final int CTRL = LookAndFeelUtil.isMac() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, CTRL), KeyAction.VK_CTRL_A);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, CTRL + InputEvent.SHIFT_DOWN_MASK), KeyAction.VK_CTRL_SHIFT_A);
		
		actionMap.put(KeyAction.VK_CTRL_A, new KeyAction(KeyAction.VK_CTRL_A));
		actionMap.put(KeyAction.VK_CTRL_SHIFT_A, new KeyAction(KeyAction.VK_CTRL_SHIFT_A));
	}
	
	private static int calculateColumns(final int thumbnailSize, final int gridWidth) {
		return thumbnailSize > 0 ? Math.floorDiv(gridWidth, thumbnailSize) : 0;
	}
	
	private static int calculateRows(final int total, final int cols) {
		return (int) Math.round(Math.ceil((float)total / (float)cols));
	}
	
	private static int maxThumbnailSize(int thumbnailSize, final int gridWidth) {
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
		public final RenderingEngineFactory<CyNetwork> thumbnailEngineFactory;
		
		public RenderingEngines(RenderingEngine<CyNetwork> networkEngine, RenderingEngineFactory<CyNetwork> thumbnailEngineFactory) {
			this.networkEngine = networkEngine;
			this.thumbnailEngineFactory = thumbnailEngineFactory;
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
		private RenderingEngine<?> thumbnailRenderer;
		
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
		
		ThumbnailPanel(final RenderingEngines engines, final int size) {
			this.engines = engines;
			
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
							.addComponent(getTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
			
			if (selectedNetworkViews.contains(engines.networkEngine.getViewModel()))
				selected = true;
			
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
			final String title = ViewUtil.getTitle(getNetworkView());
			final String netName = ViewUtil.getName(getNetworkView().getModel());
			setToolTipText(title);
// TODO Use this one when multiple views is supported, to show the network name			
//			setToolTipText("<html><center>" + title + "<br>(" + netName + ")</center></html>");
			getTitleLabel().setText(title);
			
			Dimension size = getSize();
			
			if (size == null || size.width <= 0)
				size = getPreferredSize();
			
			final int maxTitleWidth = (int) Math.round(
					size.getWidth()
					- 2 * BORDER_WIDTH
					- 2 * PAD
					- 2 * GAP 
					- 2 * getCurrentLabel().getWidth()
			);
			final Dimension titleSize = new Dimension(maxTitleWidth, getTitleLabel().getPreferredSize().height);
			getTitleLabel().setPreferredSize(titleSize);
			getTitleLabel().setMaximumSize(titleSize);
			getTitleLabel().setSize(titleSize);
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
			}
			
			return titleLabel;
		}
		
		JRootPane getImagePanel() {
			if (imagePanel == null) {
				imagePanel = new JRootPane();
				imagePanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Label.foreground"), IMG_BORDER_WIDTH));
				
				imagePanel.getGlassPane().setVisible(true);
				
				Container contentPane = imagePanel.getContentPane();
				CyNetworkView netView = getNetworkView();
				thumbnailRenderer = engines.thumbnailEngineFactory.createRenderingEngine(contentPane, netView);
			}
			
			return imagePanel;
		}
		
		public RenderingEngine<?> getThumbnailRenderingEngine() {
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

		final static String VK_CTRL_A = "VK_CTRL_A";
		final static String VK_CTRL_SHIFT_A = "VK_CTRL_SHIFT_A";
		
		KeyAction(final String actionCommand) {
			putValue(ACTION_COMMAND_KEY, actionCommand);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			
			if (focusOwner instanceof JTextComponent || focusOwner instanceof JTable ||
					!NetworkViewGrid.this.isVisible() || isEmpty())
				return; // We don't want to steal the key event from these components
			
			final String cmd = e.getActionCommand();
			
			if (cmd.equals(VK_CTRL_A))
				selectAll();
			else if (cmd.equals(VK_CTRL_SHIFT_A))
				deselectAll();
		}
	}
}
