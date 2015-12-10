package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_TITLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;

import java.awt.BasicStroke;
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

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;

@SuppressWarnings("serial")
public class NetworkViewGrid extends JPanel implements Scrollable {
	
	public static int MIN_THUMBNAIL_SIZE = 100;
	public static int MAX_THUMBNAIL_SIZE = 500;
	
	private static int BORDER_WIDTH = 2;
	private static int PAD = 5;
	
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
					setSelectedItems(Collections.emptySet());
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
			System.out.println("\n--> " + this.thumbnailSize);
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
		
		private JLabel currentLabel;
		private JLabel titleLabel;
		private JLabel imageLabel;
		private ThumbnailIcon icon;
		
		private boolean selected;
		
		private final RenderingEngine<CyNetwork> engine;
		
		private Border DEFAULT_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(1, 1, 1, 1),
				BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"), 1)
		);
		private Border HOVER_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(1, 1, 1, 1),
				BorderFactory.createLineBorder(UIManager.getColor("Focus.color"), 1)
		);

		ThumbnailPanel(final RenderingEngine<CyNetwork> engine, final int size) {
			this.engine = engine;
			this.setBorder(DEFAULT_BORDER);
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			
			currentLabel = new JLabel(IconManager.ICON_BOOKMARK);
			currentLabel.setFont(iconManager.getIconFont(24.0f));
			currentLabel.setMinimumSize(currentLabel.getPreferredSize());
			currentLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (!isCurrent())
						setCurrentNetworkView(getNetworkView());
				}
			});
			
			final Dimension d = new Dimension(size - BORDER_WIDTH, size - BORDER_WIDTH);
			this.setMinimumSize(d);
			this.setPreferredSize(d);
			
			final GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
					.addGroup(layout.createSequentialGroup()
							.addGap(2)
							.addComponent(currentLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addGap(2, 2, Short.MAX_VALUE)
							.addComponent(getTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addGap(2, 2, Short.MAX_VALUE)
							.addGap(currentLabel.getPreferredSize().width)
							.addGap(2)
					)
					.addGroup(layout.createSequentialGroup()
							.addGap(PAD)
							.addGap(0, 0, Short.MAX_VALUE)
							.addComponent(getImageLabel())
							.addGap(0, 0, Short.MAX_VALUE)
							.addGap(PAD)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(LEADING, true)
							.addComponent(currentLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addGroup(layout.createSequentialGroup()
									.addGap(PAD)
									.addComponent(getTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							)
					)
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(getImageLabel())
					.addGap(0, 0, Short.MAX_VALUE)
					.addGap(PAD)
			);
			
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					setBorder(HOVER_BORDER);
				}
				@Override
				public void mouseExited(MouseEvent e) {
					setBorder(DEFAULT_BORDER);
				}
			});
			
			this.updateIcon();
			this.update();
		}
		
		private void setSelected(boolean newValue) {
			if (this.selected != newValue) {
				final boolean oldValue = this.selected;
				this.selected = newValue;
				updateSelection();
				
				firePropertyChange("selected", oldValue, newValue);
			}
		}
		
		boolean isSelected() {
			return selected;
		}
		
		boolean isCurrent() {
			return getNetworkView().equals(currentNetworkView);
		}
		
		void update() {
			final CyNetworkView netView = getNetworkView();
			final CyNetwork network = netView.getModel();
			
			final String title = netView.getVisualProperty(NETWORK_TITLE);
			String netName = null;
			
			try {
				netName = network.getRow(network).get(CyNetwork.NAME, String.class);
			} catch (Exception e) {
				netName = "";
			}
			
			setToolTipText("<html><center>" + title + "<br>(" + netName + ")</center></html>");
			getTitleLabel().setText(title);
			
			currentLabel.setText(IconManager.ICON_BOOKMARK_O);
			currentLabel.setForeground(UIManager.getColor(isCurrent() ? "Focus.color" : "Table.background"));
			currentLabel.setToolTipText(isCurrent() ? "Current Network View" : "Set Current Network View");
			
			getTitleLabel().setForeground(
					UIManager.getColor(isCurrent() ? "Label.foreground" : "Label.disabledForeground"));
			
			this.updateSelection();
		}
		
		void updateIcon() {
			final Dimension size = this.getPreferredSize();
			
			if (size != null) {
				int imgWidth = size.width - BORDER_WIDTH - 2 * PAD;
				int imgHeight = imgWidth - getTitleLabel().getPreferredSize().height;
				final Image img = createThumbnail(imgWidth, imgHeight);
				icon = img != null ? new ThumbnailIcon(img, getNetworkView()) : null;
				imageLabel.setIcon(icon);
				
				updateUI();
			}
		}
		
		private void updateSelection() {
			this.setBackground(UIManager.getColor(selected ? "Table.selectionBackground" : "Panel.background"));
		}
		
		CyNetworkView getNetworkView() {
			return (CyNetworkView) engine.getViewModel();
		}
		
		JLabel getTitleLabel() {
			if (titleLabel == null) {
				titleLabel = new JLabel();
				titleLabel.setHorizontalAlignment(JLabel.CENTER);
				titleLabel.setFont(titleLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
				titleLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
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
		
		/**
		 * @param w Image width
		 * @param h Image height
		 * @return
		 */
		private Image createThumbnail(final int w, final int h) {
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
			
			if (w > 0 && h > 0 && nw > 0 && nh > 0) {
				final Graphics2D g = (Graphics2D) image.getGraphics();
				g.scale(scale, scale);
				g.translate(0, 1);
				
				engine.printCanvas(g);
				g.dispose();
			}
			
			return image;
		}
	}
	
	private class ThumbnailIcon extends ImageIcon {
		
		private CyNetworkView networkView;
		
		ThumbnailIcon(final Image image, final CyNetworkView networkView) {
			super(image);
			this.networkView = networkView;
		}
		
		@Override
		public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
			final Graphics2D g2 = (Graphics2D) g.create();
			
			g2.setPaint(networkView.getVisualProperty(NETWORK_BACKGROUND_PAINT));
			g2.drawRect(0,  0, c.getWidth(), c.getHeight());
			
			super.paintIcon(c, g, x, y);
			
			g2.setColor(UIManager.getColor("Label.foreground"));
			g2.setStroke(new BasicStroke(1));
			g2.drawRect(0,  0, c.getWidth() - 1, c.getHeight() - 1);
			
			g2.dispose();
		}
	}
	
	private class NetworkViewTitleComparator implements Comparator<CyNetworkView> {

		private Collator collator = Collator.getInstance(Locale.getDefault());
		
		@Override
		public int compare(final CyNetworkView v1, final CyNetworkView v2) {
			String t1 = v1.getVisualProperty(NETWORK_TITLE);
			String t2 = v2.getVisualProperty(NETWORK_TITLE);
			if (t1 == null) t1 = "";
			if (t2 == null) t2 = "";
			
			return collator.compare(t1, t2);
		}

	}
}
