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
	
	private JLabel viewCountLabel;
	private JLabel viewIconLabel;
	
	public SubNetworkPanel(final SubNetworkPanelModel model, final CyServiceRegistrar serviceRegistrar) {
		super(model, serviceRegistrar);
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
	}
	
	@Override
	public Dimension getMaximumSize() {
		final Dimension size = getPreferredSize();
	    size.width = Short.MAX_VALUE;
	    
	    return size;
	}
	
	@Override
	protected void init() {
		setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(!LookAndFeelUtil.isAquaLAF());
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGap(ExpandCollapseButton.WIDTH - getViewCountLabel().getPreferredSize().width)
				.addComponent(getViewCountLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getViewIconLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(getNameLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addContainerGap()
		);
		layout.setVerticalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(getViewCountLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getViewIconLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getNameLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
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
}
