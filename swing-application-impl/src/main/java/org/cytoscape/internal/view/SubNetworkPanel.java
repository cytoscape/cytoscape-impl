package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT_SQUARE;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class SubNetworkPanel extends AbstractNetworkPanel<CySubNetwork> {
	
	private static int INDENT_WIDTH = 20;
	
	private JLabel currentLabel;
	private JLabel indentLabel;
	private JLabel viewCountLabel;
	private JLabel viewIconLabel;
	private JLabel nodeCountLabel;
	private JLabel edgeCountLabel;
	
	private int depth;
	private ViewIconMouseListener viewIconMouseListener;
	
	private NetworkViewPreviewDialog previewDialog;
	private PreviewAWTEventListener previewAWTEventListener;
	private long previewDialogClosingTime;
	
	public SubNetworkPanel(final SubNetworkPanelModel model, final CyServiceRegistrar serviceRegistrar) {
		super(model, serviceRegistrar);
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void setDepth(final int newValue) {
		if (newValue != depth) {
			final int oldValue = depth;
			depth = newValue;
			firePropertyChange("depth", oldValue, newValue);
		}
	}
	
	public boolean isDescendantOf(final CySubNetwork parentNet) {
		CySubNetwork net = getModel().getNetwork();
		
		while (net != null) {
			net = ViewUtil.getParent(net, serviceRegistrar);
			
			if (parentNet.equals(net))
				return true;
		}
		
		return false;
	}
	
	@Override
	public Dimension getMaximumSize() {
		final Dimension size = getPreferredSize();
	    size.width = Short.MAX_VALUE;
	    
	    return size;
	}
	
	@Override
	public void update() {
		super.update();
		
		final int viewCount = getModel().getViewCount();
		String viewCountText = " ";
		
		if (viewCount > 9)
			viewCountText = "\u208A"; // Subscript plus sign ('+')
		else if (viewCount > 1)
			viewCountText = Character.toString((char) (0x2080 + viewCount)); // Create a subscript number
		
		getViewCountLabel().setText(viewCountText);
		
		getViewIconLabel().setText(viewCount == 0 ? ICON_SHARE_ALT : ICON_SHARE_ALT_SQUARE);
		getViewIconLabel().setForeground(
				UIManager.getColor(viewCount == 0 ? "Label.disabledForeground" : "Label.foreground"));
		getViewIconLabel().setToolTipText((viewCount > 0 ? viewCount : "No") + " view" + (viewCount == 1 ? "" : "s"));
		
		updateCurrentLabel();
		updateIndentation();
		updateCountLabels();
		
		if (viewCount > 0) {
			if (viewIconMouseListener == null)
				getViewIconLabel().addMouseListener(viewIconMouseListener = new ViewIconMouseListener());
		} else if (viewIconMouseListener != null) {
			getViewIconLabel().removeMouseListener(viewIconMouseListener);
			viewIconMouseListener = null;
		}
	}
	
	protected void updateCurrentLabel() {
		getCurrentLabel().setText(getModel().isCurrent() ? IconManager.ICON_CIRCLE : " ");
		getCurrentLabel().setToolTipText(getModel().isCurrent() ? "Current Network" : null);
	}
	
	protected void updateIndentation() {
		final Dimension d = new Dimension(depth * INDENT_WIDTH, getIndentLabel().getPreferredSize().height);
		getIndentLabel().setPreferredSize(d);
		getIndentLabel().setMinimumSize(d);
		getIndentLabel().setMaximumSize(d);
		getIndentLabel().setSize(d);
	}
	
	protected void updateCountLabels() {
		getNodeCountLabel().setText("" + getModel().getNodeCount());
		getEdgeCountLabel().setText("" + getModel().getEdgeCount());
	}
	
	@Override
	protected void init() {
		setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		final int CURR_LABEL_W = getCurrentLabel().getWidth();
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(getCurrentLabel(), CURR_LABEL_W, CURR_LABEL_W, CURR_LABEL_W)
				.addComponent(getIndentLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGap(ExpandCollapseButton.WIDTH - CURR_LABEL_W - getViewCountLabel().getPreferredSize().width)
				.addComponent(getViewCountLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getViewIconLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(getNameLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(getNodeCountLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(getEdgeCountLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addContainerGap()
		);
		layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getCurrentLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getIndentLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getViewCountLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getViewIconLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getNameLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getNodeCountLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getEdgeCountLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
	}
	
	protected JLabel getCurrentLabel() {
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

	protected JLabel getIndentLabel() {
		if (indentLabel == null) {
			indentLabel = new JLabel(" ");
		}
		
		return indentLabel;
	}
	
	protected JLabel getViewCountLabel() {
		if (viewCountLabel == null) {
			viewCountLabel = new JLabel("\u2089"); // Set this initial text just to get the preferred size
			viewCountLabel.setFont(viewCountLabel.getFont().deriveFont(16.0f));
			viewCountLabel.setHorizontalAlignment(JLabel.RIGHT);
			
			final Dimension d = new Dimension(
					viewCountLabel.getPreferredSize().width,
					getViewIconLabel().getPreferredSize().height);
			viewCountLabel.setMinimumSize(d);
			viewCountLabel.setPreferredSize(d);
			viewCountLabel.setMaximumSize(d);
			viewCountLabel.setSize(d);
		}
		
		return viewCountLabel;
	}
	
	protected JLabel getViewIconLabel() {
		if (viewIconLabel == null) {
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			
			viewIconLabel = new JLabel(ICON_SHARE_ALT_SQUARE);
			viewIconLabel.setFont(iconManager.getIconFont(16.0f));
		}
		
		return viewIconLabel;
	}
	
	protected JLabel getNodeCountLabel() {
		if (nodeCountLabel == null) {
			nodeCountLabel = new JLabel();
			nodeCountLabel.setToolTipText("Nodes");
			nodeCountLabel.setFont(nodeCountLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			nodeCountLabel.setHorizontalAlignment(JLabel.RIGHT);
			nodeCountLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		}
		
		return nodeCountLabel;
	}
	
	protected JLabel getEdgeCountLabel() {
		if (edgeCountLabel == null) {
			edgeCountLabel = new JLabel();
			edgeCountLabel.setToolTipText("Edges");
			edgeCountLabel.setFont(edgeCountLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			edgeCountLabel.setHorizontalAlignment(JLabel.RIGHT);
			edgeCountLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		}
		
		return edgeCountLabel;
	}
	
	private class ViewIconMouseListener extends MouseAdapter {
		
		@Override
		public void mouseEntered(MouseEvent e) {
			if (previewDialog != null || previewDialogClosingTime >= System.currentTimeMillis() - 500)
				return;
			
			if (getModel().getViewCount() > 0) {
				if (previewDialog == null) {
					final Window windowAncestor = SwingUtilities.getWindowAncestor(SubNetworkPanel.this);
					previewDialog = new NetworkViewPreviewDialog(getModel().getNetwork(), windowAncestor, serviceRegistrar);
					
					if (previewAWTEventListener == null)
						previewAWTEventListener = new PreviewAWTEventListener();
					
					Toolkit.getDefaultToolkit().addAWTEventListener(previewAWTEventListener,
							MouseEvent.MOUSE_MOTION_EVENT_MASK);
				}
				
				final Point screenPt = getViewIconLabel().getLocationOnScreen();
				final Point compPt = getViewIconLabel().getLocation();
				int xOffset = screenPt.x - compPt.x - getViewIconLabel().getWidth() / 2;
				int yOffset = screenPt.y - compPt.y + getViewIconLabel().getBounds().height;
			    final Point pt = getViewIconLabel().getBounds().getLocation();
			    pt.translate(xOffset, yOffset);
			    
				previewDialog.setLocation(pt);
				previewDialog.setVisible(true);
				previewDialog.requestFocusInWindow();
			}
		}
	}
	
	private class PreviewAWTEventListener implements AWTEventListener {
		
        @Override
        public void eventDispatched(AWTEvent event) {
            if (event instanceof MouseEvent) {
                final MouseEvent me1 = (MouseEvent) event;
                
                if (previewDialog != null) {
                	final MouseEvent me2 = SwingUtilities.convertMouseEvent(me1.getComponent(), me1, previewDialog.getRootPane());
                	
                	// Check if cursor is outside the dialog
                	if (!previewDialog.getRootPane().getBounds().contains(me2.getPoint())) {
                		// Also check if not inside the icon label
                		final MouseEvent me3 = SwingUtilities.convertMouseEvent(me1.getComponent(), me1, getViewIconLabel().getParent());
                		
                		if (!getViewIconLabel().getBounds().contains(me3.getPoint())) {
	                		previewDialogClosingTime = System.currentTimeMillis();
		                    previewDialog.dispose();
							previewDialog = null;
							
							if (previewAWTEventListener != null)
								Toolkit.getDefaultToolkit().removeAWTEventListener(previewAWTEventListener);
                		}
                	}
                }
            }
        }
    }
}
