package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

@SuppressWarnings("serial")
public class NetworkViewGrid extends JPanel implements Scrollable {
	
	public static int MIN_THUMBNAIL_SIZE = 100;
	public static int MAX_THUMBNAIL_SIZE = 500;
	
	private static int BORDER_WIDTH = 2;
	private static int PAD = 10;
	
	private static Border DEF_BORDER = BorderFactory.createLineBorder(
			UIManager.getColor("Separator.foreground"), BORDER_WIDTH);
	
	private Set<RenderingEngine<CyNetwork>> engines;
	private final Map<CyNetworkView, ThumbnailPanel> thumbnailPanels;
	private CyNetworkView currentNetworkView;
	private int thumbnailSize;
	private boolean dirty;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public NetworkViewGrid(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		engines = new LinkedHashSet<>();
		thumbnailPanels = new HashMap<>();
		
		setBackground(UIManager.getColor("Separator.foreground"));
		
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
		
		update(thumbnailSize);
	}
	
	public ThumbnailPanel getThumbnailPanel(final CyNetworkView view) {
		return thumbnailPanels.get(view);
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
	
	protected void addThumbnail(final RenderingEngine<CyNetwork> re) {
		engines.add(re);
		dirty = true;
	}
	
	protected void removeThumbnail(final RenderingEngine<CyNetwork> re) {
		engines.remove(re);
		dirty = true;
	}
	
	protected CyNetworkView getCurrentNetworkView() {
		return currentNetworkView;
	}
	
	protected void setCurrentNetworkView(final CyNetworkView view) {
		if ((currentNetworkView == null && view == null) || 
				(currentNetworkView != null && currentNetworkView.equals(view)))
			return;
		
		currentNetworkView = view;
		
		for (ThumbnailPanel tp : thumbnailPanels.values())
			tp.update();
	}
	
	protected void update(final int thumbnailSize) {
		dirty = dirty || thumbnailSize != this.thumbnailSize; // TODO separate both conditions
		this.thumbnailSize = thumbnailSize;
		
		if (!dirty) // TODO: Only update images a few times a second or less;
			return;
		
		// TODO Do not recreate if only changing thumbnail size (always use same big image?)
		recreateThumbnails();
	}

	protected int getThumbnailSize() {
		return thumbnailSize;
	}
	
	protected Collection<ThumbnailPanel> getThumbnailPanels() {
		return thumbnailPanels.values();
	}
	
	private void recreateThumbnails() {
		final Dimension size = getSize();
		
		if (size == null)
			return;
		
		removeAll();
		
		// TODO Print some info? E.g. "No network views"
		if (engines != null && !engines.isEmpty()) {
			System.out.println("\n--> " + this.thumbnailSize);
			this.thumbnailSize = Math.max(this.thumbnailSize, MIN_THUMBNAIL_SIZE);
			this.thumbnailSize = Math.min(this.thumbnailSize, MAX_THUMBNAIL_SIZE);
			this.thumbnailSize = Math.min(this.thumbnailSize, size.width);
			
			int total = engines.size();
			int cols = Math.floorDiv(size.width, this.thumbnailSize);
			int rows = (int) Math.round(Math.ceil((float)total / (float)cols));
			System.out.println("\t" + size.width + " >>> " + total + " :: " + cols + "," + rows);
			
			setLayout(new GridLayout(rows, cols));
			
			for (RenderingEngine<CyNetwork> engine : engines) {
				final ThumbnailPanel tp = new ThumbnailPanel(engine, this.thumbnailSize);
				add(tp);
				thumbnailPanels.put(tp.getNetworkView(), tp);
			}
		}
		
		dirty = false;
		firePropertyChange("thumbnailPanels", null, thumbnailPanels.values());
	}
	
	class ThumbnailPanel extends JPanel {
		
		private JLabel currentLabel;
		private JLabel titleLabel;
		private JLabel imageLabel;
		private ImageIcon icon;
		
		private final RenderingEngine<CyNetwork> engine;

		ThumbnailPanel(final RenderingEngine<CyNetwork> engine, final int size) {
			this.engine = engine;
			this.setBorder(DEF_BORDER);
			
			final CyNetworkView netView = getNetworkView();
			final CyNetwork network = netView.getModel();
			
			final String title = netView.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
			final String netName = network.getRow(network).get(CyNetwork.NAME, String.class);
			
			setToolTipText("<html><center>" + title + "<br>(" + netName + ")</center></html>");
			
			titleLabel = new JLabel(title);
			titleLabel.setHorizontalAlignment(JLabel.CENTER);
			titleLabel.setFont(titleLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			
			currentLabel = new JLabel(IconManager.ICON_ASTERISK);
			currentLabel.setFont(iconManager.getIconFont(14.0f));
			currentLabel.setForeground(this.getBackground());
			
			final Dimension d = new Dimension(size - BORDER_WIDTH, size - BORDER_WIDTH);
			this.setMinimumSize(d);
			this.setPreferredSize(d);
			
			int imgSize = size - BORDER_WIDTH - 2 * PAD;
			final Image img = createThumbnail(imgSize, imgSize);
			icon = new ImageIcon(img);
			
			final GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(false);
			
			layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
					.addGroup(layout.createSequentialGroup()
							.addGap(0, PAD, Short.MAX_VALUE)
							.addComponent(getImageLabel(), imgSize, imgSize, imgSize)
							.addGap(0, PAD, Short.MAX_VALUE)
					)
					.addGroup(layout.createSequentialGroup()
							.addComponent(currentLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(titleLabel, DEFAULT_SIZE, DEFAULT_SIZE, size - BORDER_WIDTH)
							.addGap(currentLabel.getPreferredSize().width)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(0, PAD, Short.MAX_VALUE)
					.addComponent(getImageLabel(), imgSize, imgSize, imgSize)
					.addGroup(layout.createParallelGroup(CENTER, true)
							.addComponent(currentLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(titleLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
			);
			
			this.update();
		}
		
		void update() {
			final boolean isCurrent = engine.getViewModel().equals(currentNetworkView);
			currentLabel.setForeground(isCurrent ? UIManager.getColor("Focus.color") : this.getBackground());
		}
		
		CyNetworkView getNetworkView() {
			return (CyNetworkView) engine.getViewModel();
		}
		
		JLabel getImageLabel() {
			if (imageLabel == null) {
				imageLabel = new JLabel(icon);
				imageLabel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Label.foreground")));
			}
			
			return imageLabel;
		}
		
		private Image createThumbnail(final int width, final int height) {
//			final Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//			final Graphics2D g = (Graphics2D) image.getGraphics();
	//
//			try {
//				final Dimension size = new Dimension(width, height);
	//
//				JPanel panel = new JPanel();
//				panel.setPreferredSize(size);
//				panel.setSize(size);
//				panel.setMinimumSize(size);
//				panel.setMaximumSize(size);
//				panel.setBackground((Color) vs.getDefaultValue(NETWORK_BACKGROUND_PAINT));
	//
//				JWindow window = new JWindow();
//				window.getContentPane().add(panel, BorderLayout.CENTER);
	//
//				RenderingEngine<CyNetwork> re = engineFactory.createRenderingEngine(panel, networkView);

//				vs.apply(networkView);
//				networkView.fitContent();
//				networkView.updateView();
//				window.pack();
//				window.repaint();
	//
//				renderingEngine.createImage(width, height);
//				renderingEngine.printCanvas(g);
//				g.dispose();
//			} catch (Exception ex) {
//				throw new RuntimeException(ex);
//			}
			
			final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			final Graphics2D g = (Graphics2D) image.getGraphics();
			
//			final double scale = .2; // TODO
//			g.scale(scale, scale);
			
			engine.printCanvas(g);
			g.dispose();
			
//			return renderingEngine.createImage(width, height);
			return image;
		}
	}
}
