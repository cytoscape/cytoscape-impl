package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_TITLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;

@SuppressWarnings("serial")
public class NetworkViewPreviewDialog extends JDialog {
	
	private static int MAX_VISIBLE_THUMBNAILS = 3;
	
	private static int DEFAULT_THUMBNAIL_SIZE = 120;
	private static int PAD = 0;
	private static int GAP = 0;
	private static int BORDER_WIDTH = 2;
	private static int IMG_BORDER_WIDTH = 0;
	
	private JScrollPane scrollPane;
	private JPanel gridPanel;
	
	private CyNetworkView currentNetworkView;
	
	private final CySubNetwork network;
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewPreviewDialog(final CySubNetwork network, final Window owner,
			final CyServiceRegistrar serviceRegistrar) {
		super(owner);
		this.network = network;
		this.serviceRegistrar = serviceRegistrar;
		init();
	}
	
	@Override
	public void setVisible(boolean b) {
		final Component[] components = getGridPanel().getComponents();
		
		if (components != null) {
			for (Component c : getGridPanel().getComponents()) {
				if (c instanceof ThumbnailPanel)
					((ThumbnailPanel) c).update();
			}
			
			if (components.length > MAX_VISIBLE_THUMBNAILS) {
				getScrollPane().setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
				
				final Dimension d = new Dimension(3 * DEFAULT_THUMBNAIL_SIZE, getScrollPane().getPreferredSize().height);
				getScrollPane().setPreferredSize(d);
				pack();
			}
		}
		
		super.setVisible(b);
	}

	public CySubNetwork getNetwork() {
		return network;
	}
	
	private void init() {
		setUndecorated(true);
		setBackground(getBackgroundColor());
		
		final GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(getScrollPane(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getScrollPane(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		pack();
	}
	
	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane(getGridPanel());
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		}
		
		return scrollPane;
	}
	
	private JPanel getGridPanel() {
		if (gridPanel == null) {
			gridPanel = new GridPanel();
			gridPanel.setBackground(getBackgroundColor());
			gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.X_AXIS));
			
			final CyNetworkViewManager netViewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
			final RenderingEngineManager engineManager = serviceRegistrar.getService(RenderingEngineManager.class);
			final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
			final Collection<CyNetworkView> netViews = netViewManager.getNetworkViews(network);
			
			for (CyNetworkView view : netViews) {
				final Collection<RenderingEngine<?>> engines = engineManager.getRenderingEngines(view);
				
				for (RenderingEngine<?> re : engines) {
					final ThumbnailPanel tp = new ThumbnailPanel(re, DEFAULT_THUMBNAIL_SIZE);
					gridPanel.add(tp);
					
					tp.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(final MouseEvent e) {
							// TODO do not set the current view directly to the OSGI manager!
							if (!view.equals(applicationManager.getCurrentNetworkView()))
								applicationManager.setCurrentNetworkView(view);
						}
					});
					
					break;
				}
			}
		}
		
		return gridPanel;
	}
	
	private static Color getBackgroundColor() {
		return UIManager.getColor("Table.background");
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
			return false;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return true;
		}
	}
	
	class ThumbnailPanel extends JPanel {
		
		private int MARGING_THICKNESS = 1;
		
		private JLabel currentLabel;
		private JLabel titleLabel;
		private JLabel imageLabel;
		
		private boolean hover;
		private boolean detached;
		
		private final RenderingEngine<?> engine;
		
		private final Color BORDER_COLOR = UIManager.getColor("Separator.foreground");
		private final Color HOVER_COLOR = UIManager.getColor("Focus.color");
		
		private Border MARGIN_BORDER = BorderFactory.createLineBorder(getBackgroundColor(), MARGING_THICKNESS);
		private Border SIMPLE_BORDER = BorderFactory.createLineBorder(BORDER_COLOR, 1);
		
		private Border DEFAULT_BORDER = BorderFactory.createCompoundBorder(
				MARGIN_BORDER,
				SIMPLE_BORDER
		);
		private Border DEFAULT_HOVER_BORDER = BorderFactory.createCompoundBorder(
				MARGIN_BORDER,
				BorderFactory.createLineBorder(HOVER_COLOR, 1)
		);
		
		ThumbnailPanel(final RenderingEngine<?> engine, final int size) {
			this.engine = engine;
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
							.addGap(GAP, GAP, Short.MAX_VALUE)
							.addComponent(getImageLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addGap(GAP, GAP, Short.MAX_VALUE)
					)
					.addGroup(layout.createSequentialGroup()
							.addGap(PAD)
							.addComponent(getCurrentLabel(), CURR_LABEL_W, CURR_LABEL_W, CURR_LABEL_W)
							.addGap(GAP, GAP, Short.MAX_VALUE)
							.addComponent(getTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addGap(GAP, GAP, Short.MAX_VALUE)
							.addGap(CURR_LABEL_W)
							.addGap(PAD)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(GAP)
					.addComponent(getImageLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
					.addGap(GAP)
					.addGroup(layout.createParallelGroup(CENTER, false)
							.addComponent(getCurrentLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGap(PAD)
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
			
			this.update();
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
		
		void update() {
			final CyNetworkView netView = getNetworkView();
			final String title = ViewUtil.getTitle(netView);
			
			getCurrentLabel().setText(isCurrent() ? IconManager.ICON_CIRCLE : " ");
			
			getTitleLabel().setText(title);
			getTitleLabel().setToolTipText(title);
			getImageLabel().setToolTipText(title);
			
			final int maxTitleWidth = (int) Math.round(
					getPreferredSize().getWidth()
					- 2 * BORDER_WIDTH
					- 2 * PAD
					- 2 * GAP 
					- 2 * getCurrentLabel().getWidth()
			);
			final Dimension titleSize = new Dimension(maxTitleWidth, getTitleLabel().getPreferredSize().height);
			getTitleLabel().setPreferredSize(titleSize);
			getTitleLabel().setMaximumSize(titleSize);
			getTitleLabel().setSize(titleSize);
			
			this.updateBorder();
			this.updateIcon();
		}
		
		void updateIcon() {
			final Dimension size = this.getSize();
			
			if (size != null && getTitleLabel().getSize() != null) {
				int lh = getTitleLabel().getHeight();
				
				int iw = size.width - 2 * BORDER_WIDTH - 4 * GAP - IMG_BORDER_WIDTH;
				int ih = size.height - 2 * BORDER_WIDTH - 2 * GAP - lh - PAD - IMG_BORDER_WIDTH;
				
				if (iw > 0 && ih > 0) {
					final Image img = createThumbnail(iw, ih);
					final ImageIcon icon = img != null ? new ImageIcon(img) : null;
					getImageLabel().setIcon(icon);
					updateUI();
				}
			}
		}
		
		private void updateBorder() {
			setBorder(hover ? DEFAULT_HOVER_BORDER : DEFAULT_BORDER);
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
				imageLabel = new JLabel();
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
			netView.updateView();
			
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
}
