package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.internal.util.ViewUtil.styleToolBarButton;
import static org.cytoscape.util.swing.IconManager.ICON_CARET_LEFT;
import static org.cytoscape.util.swing.IconManager.ICON_CARET_RIGHT;
import static org.cytoscape.util.swing.IconManager.ICON_EXTERNAL_LINK_SQUARE;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT_SQUARE;
import static org.cytoscape.util.swing.IconManager.ICON_THUMB_TACK;
import static org.cytoscape.util.swing.IconManager.ICON_TRASH_O;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_TITLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;

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
	
	public static final String GRID_NAME = "__NETWORK_VIEW_GRID__";
	
	public static int MIN_THUMBNAIL_SIZE = 100;
	public static int MAX_THUMBNAIL_SIZE = 500;
	
	private GridPanel gridPanel;
	private JScrollPane gridScrollPane;
	private JPanel toolBar;
	private JButton viewModeButton;
	private JButton comparisonModeButton;
	private JLabel viewSelectionLabel;
	private JButton detachSelectedViewsButton;
	private JButton reattachAllViewsButton;
	private JButton destroySelectedViewsButton;
	private JSlider thumbnailSlider;
	
	private Map<CyNetworkView, RenderingEngine<CyNetwork>> engines;
	private final TreeMap<CyNetworkView, ThumbnailPanel> thumbnailPanels;
	private CyNetworkView currentNetworkView;
	private int thumbnailSize;
	private int maxThumbnailSize;
	private boolean dirty;
	private Comparator<CyNetworkView> comparator;
	
	private ThumbnailPanel selectionHead;
	private ThumbnailPanel selectionTail;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public NetworkViewGrid(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		engines = new HashMap<>();
		thumbnailPanels = new TreeMap<>(comparator = new NetworkViewTitleComparator());
		
		init();
	}
	
	public ThumbnailPanel getItem(final CyNetworkView view) {
		return thumbnailPanels.get(view);
	}
	
	public Collection<ThumbnailPanel> getItems() {
		return thumbnailPanels.values();
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
	
	public void addItem(final RenderingEngine<CyNetwork> re) {
		if (!contains(re)) {
			final Collection<CyNetworkView> oldViews = getNetworkViews();
			engines.put((CyNetworkView)re.getViewModel(), re);
			dirty = true;
			firePropertyChange("networkViews", oldViews, getNetworkViews());
		}
	}
	
	public void removeItems(final Collection<RenderingEngine<CyNetwork>> enginesToRemove) {
		if (enginesToRemove != null && !enginesToRemove.isEmpty()) {
			final Collection<CyNetworkView> oldViews = getNetworkViews();
			boolean removed = false;
			
			for (RenderingEngine<CyNetwork> re : enginesToRemove) {
				if (engines.remove(re.getViewModel()) != null) {
					removed = true;
					dirty = true;
				}
			}
			
			if (removed)
				firePropertyChange("networkViews", oldViews, getNetworkViews());
		}
	}
	
	public Collection<CyNetworkView> getNetworkViews() {
		return new ArrayList<>(engines.keySet());
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
	
	protected void setDetached(final CyNetworkView view, final boolean b) {
		final ThumbnailPanel item = getItem(view);
		
		if (item != null)
			item.setDetached(b);
	}
	
	/** Updates the whole grid and recreate the thumbnails **/
	protected void update(final int thumbnailSize) {
		dirty = dirty || thumbnailSize < this.thumbnailSize || thumbnailSize > this.maxThumbnailSize;
		this.thumbnailSize = thumbnailSize;
		
		final Dimension size = getSize();
		
		if (!dirty && size != null && size.width > 0) {
			final int cols = calculateColumns(thumbnailSize, size.width);
			dirty = cols != ((GridLayout) getGridPanel().getLayout()).getColumns();
		}
		
		if (!dirty) // TODO: Only update images a few times a second or less;
			return;
		
		// TODO Do not recreate if only changing thumbnail size (always use same big image?)
		recreateThumbnails();
		updateToolBar();
	}
	
	protected void updateToolBar() {
		final Collection<ThumbnailPanel> items = getItems();
		final List<ThumbnailPanel> selectedItems = getSelectedItems();
		
		getViewModeButton().setEnabled(!items.isEmpty());
		getComparisonModeButton().setEnabled(selectedItems.size() == 2);
		getDestroySelectedViewsButton().setEnabled(!selectedItems.isEmpty());
		
		getDetachSelectedViewsButton().setEnabled(!selectedItems.isEmpty());
		
		if (items.isEmpty())
			getViewSelectionLabel().setText(null);
		else
			getViewSelectionLabel().setText(
					selectedItems.size() + " of " + 
							items.size() + " View" + (items.size() == 1 ? "" : "s") +
							" selected");
		
		getToolBar().updateUI();
	}
	
	/** Updates the image only */ 
	protected void updateThumbnail(final CyNetworkView view) {
		final ThumbnailPanel tp = getItem(view);
		
		if (tp != null)
			tp.updateIcon();
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
	private void onMousePressedItem(final MouseEvent e, final ThumbnailPanel item) {
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
					// Set this item as current first, otherwise the previous one will not be deselected
					if (!item.isCurrent())
						setCurrentNetworkView(item.getNetworkView());
					
					setSelectedItems((Set) (Collections.singleton(item)));
				}
				
				if (getSelectedItems().size() == 1)
					selectionHead = item;
			}
			
			if (changed)
				firePropertyChange("selectedItems", oldValue, getSelectedItems());
		}
	}
	
	protected void setSelectedItems(final Collection<ThumbnailPanel> selectedItems) {
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
		
		if (comparator.compare(item1.getNetworkView(), item2.getNetworkView()) <= 0)
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
	
	private void init() {
		setName(GRID_NAME);
		setFocusable(true);
		setRequestFocusEnabled(true);
		
		setLayout(new BorderLayout());
		add(getGridScrollPane(), BorderLayout.CENTER);
		add(getToolBar(), BorderLayout.SOUTH);
		
		// TODO: Listener to update when grip panel resized
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				update(thumbnailSize);
			}
			@Override
			public void componentResized(ComponentEvent e) {
				update(thumbnailSize);
			}
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				if (!e.isPopupTrigger())
					deselectAll();
			}
		});
		
		setGlobalKeyBindings(this);
		setSelectionKeyBindings(this);
		setSelectionKeyBindings(getGridScrollPane().getViewport());
		
		update(thumbnailSize);
	}
	
	private void recreateThumbnails() {
		final Dimension size = getSize();
		
		if (size == null || size.width <= 0)
			return;
		
		final List<ThumbnailPanel> previousSelection = getSelectedItems();
		
		getGridPanel().removeAll();
		thumbnailPanels.clear();
		
		// TODO Print some info? E.g. "No network views"
		if (engines != null && !engines.isEmpty()) {
			maxThumbnailSize = maxThumbnailSize(thumbnailSize, size.width);
			
			int cols = calculateColumns(maxThumbnailSize, size.width);
			int rows = calculateRows(engines.size(), cols);
			getGridPanel().setLayout(new GridLayout(rows, cols));
			
			for (RenderingEngine<CyNetwork> engine : engines.values()) {
				final ThumbnailPanel tp = new ThumbnailPanel(engine, maxThumbnailSize);
				thumbnailPanels.put(tp.getNetworkView(), tp);
				
				tp.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(final MouseEvent e) {
						onMousePressedItem(e, tp);
					}
				});
				
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
		}
		
		dirty = false;
		getGridPanel().updateUI();
		firePropertyChange("thumbnailPanels", null, thumbnailPanels.values());
	}

	private GridPanel getGridPanel() {
		if (gridPanel == null) {
			gridPanel = new GridPanel();
			gridPanel.setLayout(new GridLayout());
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
			
			final JSeparator sep = new JSeparator(JSeparator.VERTICAL);
			
			final GroupLayout layout = new GroupLayout(toolBar);
			toolBar.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getViewModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getComparisonModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDetachSelectedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getReattachAllViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 10, Short.MAX_VALUE)
					.addComponent(getViewSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 10, Short.MAX_VALUE)
					.addComponent(getDestroySelectedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getThumbnailSlider(), 100, 100, 100)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
					.addComponent(getViewModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getComparisonModeButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDetachSelectedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getReattachAllViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getViewSelectionLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDestroySelectedViewsButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getThumbnailSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return toolBar;
	}
	
	JButton getViewModeButton() {
		if (viewModeButton == null) {
			viewModeButton = new JButton(ICON_SHARE_ALT_SQUARE);
			viewModeButton.setToolTipText("Show View (V)");
			styleToolBarButton(viewModeButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
		}
		
		return viewModeButton;
	}
	
	JButton getComparisonModeButton() {
		if (comparisonModeButton == null) {
			comparisonModeButton = new JButton(ICON_CARET_RIGHT + ICON_CARET_LEFT);
			comparisonModeButton.setToolTipText("Compare 2 Views (C)");
			styleToolBarButton(comparisonModeButton, serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
		}
		
		return comparisonModeButton;
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
	
	private void setGlobalKeyBindings(final JComponent comp) {
		final ActionMap actionMap = comp.getActionMap();
		final InputMap inputMap = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), KeyAction.VK_V);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), KeyAction.VK_C);
		
		actionMap.put(KeyAction.VK_V, new KeyAction(KeyAction.VK_V));
		actionMap.put(KeyAction.VK_C, new KeyAction(KeyAction.VK_C));
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
		return UIManager.getColor("Label.disabledForeground");
	}
	
	class ThumbnailPanel extends JPanel {
		
		static final int PAD = 4;
		static final int GAP = 1;
		
		/** Margin Thickness */
		static final int MT = 3;
		/** Border Thickness */
		static final int BT = 1;
		/** Padding Thickness */
		static final int PT = 1;
		/** Total border thickness */
		static final int BORDER_WIDTH = MT + BT + PT;
		
		static final int IMG_BORDER_WIDTH = 1;
		
		private JLabel currentLabel;
		private JLabel titleLabel;
		private JLabel imageLabel;
		
		private boolean selected;
		private boolean hover;
		private boolean detached;
		
		private final RenderingEngine<CyNetwork> engine;
		
		private final Color BORDER_COLOR = UIManager.getColor("Label.foreground");
		private final Color HOVER_COLOR = UIManager.getColor("Focus.color");
		
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
						BorderFactory.createLineBorder(HOVER_COLOR, BT),
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
		private Border MIDDLE_SIBLING_HOVER_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(MT, 0, MT, 0, getBackgroundColor()),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(BT, BT, BT, BT, HOVER_COLOR),
						BorderFactory.createEmptyBorder(PT, 0, PT, 0)
				)
		);
		private Border FIRST_SIBLING_HOVER_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(MT, MT, MT, 0, getBackgroundColor()),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(BT, BT, BT, BT, HOVER_COLOR),
						BorderFactory.createEmptyBorder(PT, PT, PT, 0)
				)
		);
		private Border LAST_SIBLING_HOVER_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(MT, 0, MT, MT, getBackgroundColor()),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(BT, BT, BT, BT, HOVER_COLOR),
						BorderFactory.createEmptyBorder(PT, 0, PT, PT)
				)
		);
		
		ThumbnailPanel(final RenderingEngine<CyNetwork> engine, final int size) {
			this.engine = engine;
			
			this.setFocusable(true);
			this.setRequestFocusEnabled(true);
			
			this.setBorder(DEFAULT_BORDER);
			
			final Dimension d = new Dimension(size - BORDER_WIDTH, size - BORDER_WIDTH);
			this.setMinimumSize(d);
			this.setPreferredSize(d);
			
			final int CURR_LABEL_W = getCurrentLabel().getWidth();
			
			final GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
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
							.addGap(PAD, PAD, Short.MAX_VALUE)
							.addComponent(getImageLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addGap(PAD, PAD, Short.MAX_VALUE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(GAP)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(getCurrentLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGap(GAP)
					.addComponent(getImageLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(PAD, PAD, Short.MAX_VALUE)
			);
			
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					hover = true;
					updateBorder();
				}
				@Override
				public void mouseExited(MouseEvent e) {
					hover = false;
					updateBorder();
				}
			});
			
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
			return UIManager.getColor(selected ? "Table.selectionBackground" : "Panel.background");
		}
		
		private void setSelected(boolean newValue) {
			if (this.selected != newValue) {
				final boolean oldValue = this.selected;
				this.selected = newValue;
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
		
		boolean isDetached() {
			return detached;
		}
		
		void setDetached(boolean detached) {
			this.detached = detached;
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
			
			if (redraw)
				updateIcon();
		}

		void updateIcon() {
			final Dimension size = this.getSize();
			
			if (size != null && getTitleLabel().getSize() != null) {
				int lh = getTitleLabel().getHeight();
				
				int iw = size.width - 2 * BORDER_WIDTH - 2 * PAD - 2 * IMG_BORDER_WIDTH;
				int ih = size.height - 2 * BORDER_WIDTH - 2 * GAP - lh - PAD - 2 * IMG_BORDER_WIDTH;
				
				if (iw > 0 && ih > 0) {
					final Paint bgPaint = getNetworkView().getVisualProperty(NETWORK_BACKGROUND_PAINT);
					
					if (bgPaint instanceof Color)
						getImageLabel().setBackground((Color) bgPaint);
					
					final Image img = createThumbnail(iw, ih);
					final ImageIcon icon = img != null ? new ImageIcon(img) : null;
					getImageLabel().setIcon(icon);
					updateUI();
				}
			}
		}
		
		private void updateCurrentLabel() {
			getCurrentLabel().setText(isCurrent() ? IconManager.ICON_CIRCLE : " ");
			getCurrentLabel().setToolTipText(isCurrent() ? "Current View" : null);
		}
		
		private void updateBorder() {
			if (isFirstSibling())
				setBorder(hover? FIRST_SIBLING_HOVER_BORDER : FIRST_SIBLING_BORDER);
			else if (isMiddleSibling())
				setBorder(hover? MIDDLE_SIBLING_HOVER_BORDER : MIDDLE_SIBLING_BORDER);
			else if (isLastSibling())
				setBorder(hover? LAST_SIBLING_HOVER_BORDER : LAST_SIBLING_BORDER);
			else
				setBorder(hover ? DEFAULT_HOVER_BORDER : DEFAULT_BORDER);
		}
		
		private void updateTitleLabel() {
			final String title = ViewUtil.getTitle(getNetworkView());
			final String netName = ViewUtil.getName(getNetworkView().getModel());
			setToolTipText("<html><center>" + title + "<br>(" + netName + ")</center></html>");
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
			return (CyNetworkView) engine.getViewModel();
		}
		
		JLabel getCurrentLabel() {
			if (currentLabel == null) {
				currentLabel = new JLabel(IconManager.ICON_CIRCLE); // Just to get the preferred size with the icon font
				currentLabel.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(10.0f));
				currentLabel.setMinimumSize(currentLabel.getPreferredSize());
				currentLabel.setMaximumSize(currentLabel.getPreferredSize());
				currentLabel.setSize(currentLabel.getPreferredSize());
				currentLabel.setForeground(UIManager.getColor("Focus.color"));
			}
			
			return currentLabel;
		}
		
		JLabel getTitleLabel() {
			if (titleLabel == null) {
				titleLabel = new JLabel();
				titleLabel.setHorizontalAlignment(JLabel.CENTER);
				titleLabel.setFont(titleLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			}
			
			return titleLabel;
		}
		
		JLabel getImageLabel() {
			if (imageLabel == null) {
				final Color color = UIManager.getColor("Table.selectionBackground");
				final Color selColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 160);
				
				imageLabel = new JLabel() {
					@Override
					public void paint(Graphics g) {
						super.paint(g);
						
						if (selected) {
							g.setColor(selColor);
							g.fillRect(0, 0, getWidth(), getHeight());
						}
					};
				};
				imageLabel.setOpaque(true);
				imageLabel.setBorder(
						BorderFactory.createLineBorder(UIManager.getColor("Label.foreground"), IMG_BORDER_WIDTH));
			}
			
			return imageLabel;
		}
		
		/**
		 * @param w Image width
		 * @param h Image height
		 * @return
		 */
		private Image createThumbnail(double w, double h) {
			final int iw = (int) Math.round(w);
            final int ih = (int) Math.round(h);
            
            if (iw <= 0 || ih <= 0)
            	return null;
			
            BufferedImage image = null;
            
			final CyNetworkView netView = getNetworkView();
			
			// Fit network view image to available rectangle area
			final double vw = netView.getVisualProperty(NETWORK_WIDTH);
			final double vh = netView.getVisualProperty(NETWORK_HEIGHT);
			
			if (vw > 0 && vh > 0) {
				final double rectRatio = h / w;
				final double viewRatio = vh / vw;
	            final double scale = viewRatio > rectRatio ? w / vw  :  h / vh;
				
	            // Create scaled view image that is big enough to be clipped later
	            final int svw = (int) Math.round(vw * scale);
	            final int svh = (int) Math.round(vh * scale);
	            
	            if (svw > 0 && svh > 0) {
		            image = new BufferedImage(svw, svh, BufferedImage.TYPE_INT_ARGB);
		            
					final Graphics2D g = (Graphics2D) image.getGraphics();
					g.scale(scale, scale);
					engine.printCanvas(g);
					g.dispose();
					
					// Clip the image
					image = image.getSubimage((svw - iw) / 2, (svh - ih) / 2, iw, ih);
	            }
			}
			
			if (image == null) {
				image = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);
				
				final Graphics2D g2 = image.createGraphics();
				final Paint bg = netView.getVisualProperty(NETWORK_BACKGROUND_PAINT);
				g2.setPaint(bg);
				g2.drawRect(IMG_BORDER_WIDTH, IMG_BORDER_WIDTH, iw, ih);
				g2.dispose();
			}
            
			return image;
		}
		
		@Override
		public String toString() {
			return getNetworkView().getVisualProperty(NETWORK_TITLE);
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

		final static String VK_V = "VK_V";
		final static String VK_C = "VK_C";
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
			
			if (cmd.equals(VK_V))
				getViewModeButton().doClick();
			else if (cmd.equals(VK_C))
				getComparisonModeButton().doClick();
			else if (cmd.equals(VK_CTRL_A))
				selectAll();
			else if (cmd.equals(VK_CTRL_SHIFT_A))
				deselectAll();
		}
	}
	
	private class NetworkViewTitleComparator implements Comparator<CyNetworkView> {

		private Collator collator = Collator.getInstance(Locale.getDefault());
		
		@Override
		public int compare(final CyNetworkView v1, final CyNetworkView v2) {
			// Sort by view title, but group them by collection (root-network) and subnetwork
			Long rootId1 = ((CySubNetwork)v1.getModel()).getRootNetwork().getSUID();
			Long rootId2 = ((CySubNetwork)v1.getModel()).getRootNetwork().getSUID();
			int value = rootId1.compareTo(rootId2);
			
			if (value != 0) // Views from different collections
				return value;
			
			// Views from the same collection:
			value = v1.getModel().getSUID().compareTo(v2.getModel().getSUID());
			
			if (value != 0) // Views from different networks in the same collection
				return value;
			
			// Views from the same network:
			return collator.compare(ViewUtil.getTitle(v1), ViewUtil.getTitle(v2));
		}
	}
}
