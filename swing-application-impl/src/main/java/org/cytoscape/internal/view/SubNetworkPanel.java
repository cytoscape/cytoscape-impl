package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT_SQUARE;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;

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
			nodeCountLabel.setFont(nodeCountLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			nodeCountLabel.setHorizontalAlignment(JLabel.RIGHT);
			nodeCountLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		}
		
		return nodeCountLabel;
	}
	
	protected JLabel getEdgeCountLabel() {
		if (edgeCountLabel == null) {
			edgeCountLabel = new JLabel();
			edgeCountLabel.setFont(edgeCountLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			edgeCountLabel.setHorizontalAlignment(JLabel.RIGHT);
			edgeCountLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		}
		
		return edgeCountLabel;
	}
}
