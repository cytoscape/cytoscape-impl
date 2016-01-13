package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_TITLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;

@SuppressWarnings("serial")
public class NetworkViewGrid extends JPanel implements Scrollable {
	
	public static int MIN_THUMBNAIL_SIZE = 100;
	public static int MAX_THUMBNAIL_SIZE = 500;
	
	private static int BORDER_WIDTH = 3;
	private static int IMG_BORDER_WIDTH = 2;
	private static int PAD = 10;
	private static int GAP = 2;
	
	private Map<CyNetworkView, RenderingEngine<CyNetwork>> engines;
	private final TreeMap<CyNetworkView, ThumbnailPanel> thumbnailPanels;
	private CyNetworkView currentNetworkView;
	private int thumbnailSize;
	private boolean dirty;
	private Comparator<CyNetworkView> comparator;
	
	private ThumbnailPanel selectionHead;
	private ThumbnailPanel selectionTail;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	private final Object lock = new Object();
	
	public NetworkViewGrid(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		engines = new HashMap<>();
		thumbnailPanels = new TreeMap<>(comparator = new NetworkViewTitleComparator());
		
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
		
		update(thumbnailSize);
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
		synchronized (lock) {
			if (!contains(re)) {
				final Collection<CyNetworkView> oldViews = getNetworkViews();
				engines.put((CyNetworkView)re.getViewModel(), re);
				dirty = true;
				firePropertyChange("networkViews", oldViews, getNetworkViews());
			}
		}
	}
	
	public void removeItems(final Collection<RenderingEngine<CyNetwork>> enginesToRemove) {
		synchronized (lock) {
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
	}
	
	public Collection<CyNetworkView> getNetworkViews() {
		return new ArrayList<>(engines.keySet());
	}
	
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
	
	private boolean contains(final RenderingEngine<CyNetwork> re) {
		synchronized (lock) {
			return engines.containsKey(re.getViewModel());
		}
	}
	
	protected CyNetworkView getCurrentNetworkView() {
		synchronized (lock) {
			return currentNetworkView;
		}
	}
	
	protected void setCurrentNetworkView(final CyNetworkView newView) {
		synchronized (lock) {
			if ((currentNetworkView == null && newView == null) || 
					(currentNetworkView != null && currentNetworkView.equals(newView)))
				return;
			
			final CyNetworkView oldView = currentNetworkView;
			currentNetworkView = newView;
			
			for (ThumbnailPanel tp : thumbnailPanels.values())
				tp.update();
			
			firePropertyChange("currentNetworkView", oldView, newView);
		}
	}
	
	protected void setDetached(final CyNetworkView view, final boolean b) {
		getItem(view).setDetached(b);
	}
	
	protected void update(final int thumbnailSize) {
		synchronized (lock) {
			dirty = dirty || thumbnailSize != this.thumbnailSize; // TODO separate both conditions
			this.thumbnailSize = thumbnailSize;
			
			if (!dirty) // TODO: Only update images a few times a second or less;
				return;
		}
		
		// TODO Do not recreate if only changing thumbnail size (always use same big image?)
		recreateThumbnails();
	}
	
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
	
	private void recreateThumbnails() {
		final Dimension size = getSize();
		
		if (size == null)
			return;
		
		final List<ThumbnailPanel> previousSelection = getSelectedItems();
		
		removeAll();
		thumbnailPanels.clear();
		
		// TODO Print some info? E.g. "No network views"
		if (engines != null && !engines.isEmpty()) {
			this.thumbnailSize = Math.max(this.thumbnailSize, MIN_THUMBNAIL_SIZE);
			this.thumbnailSize = Math.min(this.thumbnailSize, MAX_THUMBNAIL_SIZE);
			this.thumbnailSize = Math.min(this.thumbnailSize, size.width);
			
			int total = engines.size();
			int cols = Math.floorDiv(size.width, this.thumbnailSize);
			int rows = (int) Math.round(Math.ceil((float)total / (float)cols));
			
			setLayout(new GridLayout(rows, cols));
			
			for (RenderingEngine<CyNetwork> engine : engines.values()) {
				final ThumbnailPanel tp = new ThumbnailPanel(engine, this.thumbnailSize);
				thumbnailPanels.put(tp.getNetworkView(), tp);
				
				tp.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(final MouseEvent e) {
						onMousePressedItem(e, tp);
					}
				});
				
				if (previousSelection.contains(tp))
					tp.setSelected(true);
			}
			
			for (Map.Entry<CyNetworkView, ThumbnailPanel> entry : thumbnailPanels.entrySet())
				add(entry.getValue());
			
			if (thumbnailPanels.size() < cols) {
				final int diff = cols - thumbnailPanels.size();
				
				for (int i = 0; i < diff; i++) {
					final JPanel filler = new JPanel();
					filler.setOpaque(false);
					add(filler);
				}
			}
		}
		
		dirty = false;
		updateUI();
		firePropertyChange("thumbnailPanels", null, thumbnailPanels.values());
	}
	
	class ThumbnailPanel extends JPanel {
		
		private JLabel titleLabel;
		private JLabel imageLabel;
		private JPanel imagePanel;
		private ThumbnailIcon icon;
		
		private boolean selected;
		private boolean hover;
		private boolean detached;
		
		private final RenderingEngine<CyNetwork> engine;
		
		private final Color BORDER_COLOR = UIManager.getColor("Separator.foreground");
		private final Color HOVER_COLOR = UIManager.getColor("Focus.color");
		
		private Border EMPTY_BORDER = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		private Border SIMPLE_BORDER = BorderFactory.createLineBorder(BORDER_COLOR, 1);
		private Border HOVER_BORDER = BorderFactory.createLineBorder(HOVER_COLOR, 1);
		
		private Border DEFAULT_BORDER = BorderFactory.createCompoundBorder(
				EMPTY_BORDER,
				BorderFactory.createCompoundBorder(
						SIMPLE_BORDER,
						EMPTY_BORDER
				)
		);
		private Border DEFAULT_HOVER_BORDER = BorderFactory.createCompoundBorder(
				EMPTY_BORDER,
				BorderFactory.createCompoundBorder(
						EMPTY_BORDER,
						HOVER_BORDER
				)
		);
		private Border MIDDLE_SIBLING_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(1, 0, 1, 0),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER_COLOR),
						EMPTY_BORDER
				)
		);
		private Border FIRST_SIBLING_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(1, 1, 1, 0),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(1, 1, 1, 0, BORDER_COLOR),
						EMPTY_BORDER
				)
		);
		private Border LAST_SIBLING_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(1, 0, 1, 1),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(1, 0, 1, 1, BORDER_COLOR),
						EMPTY_BORDER
				)
		);
		private Border MIDDLE_SIBLING_HOVER_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(1, 0, 1, 0),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER_COLOR),
						HOVER_BORDER
				)
		);
		private Border FIRST_SIBLING_HOVER_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(1, 1, 1, 0),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(1, 1, 1, 0, BORDER_COLOR),
						HOVER_BORDER
				)
		);
		private Border LAST_SIBLING_HOVER_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(1, 0, 1, 1),
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(1, 0, 1, 1, BORDER_COLOR),
						HOVER_BORDER
				)
		);
		
		ThumbnailPanel(final RenderingEngine<CyNetwork> engine, final int size) {
			this.engine = engine;
			this.setBorder(DEFAULT_BORDER);
			
			final Dimension d = new Dimension(size - BORDER_WIDTH, size - BORDER_WIDTH);
			this.setMinimumSize(d);
			this.setPreferredSize(d);
			
			final GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addGap(GAP, GAP, Short.MAX_VALUE)
							.addComponent(getTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addGap(GAP, GAP, Short.MAX_VALUE)
					)
					.addGroup(layout.createSequentialGroup()
							.addGap(PAD, PAD, Short.MAX_VALUE)
							.addComponent(getImagePanel())
							.addGap(PAD, PAD, Short.MAX_VALUE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(GAP)
					.addComponent(getTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(GAP, GAP, Short.MAX_VALUE)
					.addComponent(getImagePanel())
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
			
			this.updateIcon();
			this.update();
		}
		
		private void setSelected(boolean newValue) {
			if (this.selected != newValue) {
				final boolean oldValue = this.selected;
				this.selected = newValue;
				updateBackground();
				
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
			this.updateImageBorder();
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
		
		void update() {
			final CyNetworkView netView = getNetworkView();
			final CyNetwork network = netView.getModel();
			final String title = ViewUtil.getTitle(netView);
			final String netName = ViewUtil.getName(network);
			
			setToolTipText("<html><center>" + title + "<br>(" + netName + ")</center></html>");
			getTitleLabel().setText(title);
			getTitleLabel().setForeground(
					UIManager.getColor(isCurrent() ? "Label.foreground" : "Label.disabledForeground"));
			
			final int maxTitleWidth = (int) Math.round(this.getPreferredSize().getWidth() - 2 * BORDER_WIDTH - 2 * GAP);
			final Dimension titleSize = new Dimension(maxTitleWidth, getTitleLabel().getPreferredSize().height);
			getTitleLabel().setPreferredSize(titleSize);
			getTitleLabel().setMaximumSize(titleSize);
			getTitleLabel().setSize(titleSize);
			
			this.updateBackground();
			this.updateBorder();
			this.updateImageBorder();
		}
		
		void updateIcon() {
			final Dimension size = this.getSize();
			
			if (size != null && getTitleLabel().getSize() != null) {
				int lh = getTitleLabel().getSize().height;
				
				int imgWidth = size.width - 2 * BORDER_WIDTH - 2 * PAD - 2 * IMG_BORDER_WIDTH;
				int imgHeight = size.height - 2 * BORDER_WIDTH - 2 * GAP - lh - PAD - 2 * IMG_BORDER_WIDTH;
				
				if (imgWidth > 0 && imgHeight > 0) {
					final Image img = createThumbnail(imgWidth, imgHeight);
					icon = img != null ? new ThumbnailIcon(img) : null;
					getImageLabel().setIcon(icon);
					getImagePanel().setVisible(icon != null);
					
					if (icon != null) {
						getImagePanel().setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
						getImagePanel().setMaximumSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
						getImagePanel().setSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
					}
				
					updateUI();
				}
			}
		}
		
		private void updateBackground() {
			this.setBackground(getBackgroundColor());
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
		
		private void updateImageBorder() {
			final Border gapBorder = BorderFactory.createLineBorder(getBackgroundColor(), 1);
			final Border lineBorder;
			
			if (isDetached())
				lineBorder = BorderFactory.createDashedBorder(
						UIManager.getColor(isCurrent() ? "Table.focusCellBackground" : "Label.foreground"),
						1.0f, 2.0f, 2.0f, true
				);
			else
				lineBorder = BorderFactory.createLineBorder(
						UIManager.getColor(isCurrent() ? "Table.focusCellBackground" : "Separator.foreground"),
						1
				);
			
			final Border border;
			
			if (isCurrent() || isDetached())
				border = BorderFactory.createCompoundBorder(lineBorder, gapBorder);
			else
				border = BorderFactory.createCompoundBorder(gapBorder, lineBorder);
			
			getImagePanel().setBorder(border);
		}
		
		private Color getBackgroundColor() {
			return UIManager.getColor(selected ? "Table.selectionBackground" : "Panel.background");
		}
		
		CyNetworkView getNetworkView() {
			return (CyNetworkView) engine.getViewModel();
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
				imageLabel = new JLabel();
				imageLabel.setOpaque(true);
			}
			
			return imageLabel;
		}
		
		JPanel getImagePanel() {
			if (imagePanel == null) {
				imagePanel = new JPanel();
				imagePanel.setOpaque(false);
				imagePanel.setVisible(false);
				imagePanel.setLayout(new BorderLayout());
				imagePanel.add(getImageLabel(), BorderLayout.CENTER);
			}
			
			return imagePanel;
		}
		
		/**
		 * @param w Image width
		 * @param h Image height
		 * @return
		 */
		private Image createThumbnail(double w, double h) {
			final CyNetworkView netView = getNetworkView();
			netView.updateView();
			final double nw = netView.getVisualProperty(NETWORK_WIDTH);
			final double nh = netView.getVisualProperty(NETWORK_HEIGHT);
			
			final double imgRatio = w / h;
			final double viewRatio = nw / nh;
            final double scale = imgRatio < viewRatio ? w / nw  :  h / nh;
			
            final int iw = (int) Math.round(nw * scale);
            final int ih = (int) Math.round(nh * scale);
            
            if (iw <= 0 || ih <= 0)
            	return null; 
            
			final BufferedImage image = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);
			
			if (nw > 0 && nh > 0) {
				final Graphics2D g = (Graphics2D) image.getGraphics();
				g.scale(scale, scale);
				g.translate(0, 1);
				
				engine.printCanvas(g);
				g.dispose();
			}
			
			return image;
		}
		
		@Override
		public String toString() {
			return getNetworkView().getVisualProperty(NETWORK_TITLE);
		}
		
		private class ThumbnailIcon extends ImageIcon {
			
			private CyNetworkView networkView;
			
			ThumbnailIcon(final Image image) {
				super(image);
				this.networkView = getNetworkView();
			}
			
			@Override
			public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
				final Graphics2D g2 = (Graphics2D) g.create();
				
				g2.setPaint(networkView.getVisualProperty(NETWORK_BACKGROUND_PAINT));
				g2.drawRect(0,  0, c.getWidth(), c.getHeight());
				
				super.paintIcon(c, g, x, y);
				
				g2.setColor(UIManager.getColor("Separator.background"));
				
				if (isDetached()) {
					g2.setStroke(new BasicStroke(2));
					g2.drawRect(1,  1, c.getWidth() - 2, c.getHeight() - 2);
				} else {
					g2.setStroke(new BasicStroke(2));
					g2.drawRect(0,  0, c.getWidth() - 1, c.getHeight() - 1);
				}
				
				g2.dispose();
			}
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
