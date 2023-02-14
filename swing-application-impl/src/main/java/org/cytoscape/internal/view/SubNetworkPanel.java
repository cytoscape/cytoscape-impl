package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT_SQUARE;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;

import org.cytoscape.internal.view.util.ViewUtil;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

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
public class SubNetworkPanel extends AbstractNetworkPanel<CySubNetwork> {
	
	private static int INDENT_WIDTH = 20;
	
	private JLabel currentLabel;
	private JLabel indentLabel;
	private JLabel viewCountLabel;
	private JLabel viewIconLabel;
	private JLabel nodeCountLabel;
	private JLabel edgeCountLabel;
	
	private int depth;
	private boolean showIndentation;
	
	public SubNetworkPanel(SubNetworkPanelModel model, boolean showIndentation, CyServiceRegistrar serviceRegistrar) {
		super(model, serviceRegistrar);
		
		if (showIndentation != this.showIndentation)
			setShowIndentation(showIndentation);
	}
	
	@Override
	public void setModel(AbstractNetworkPanelModel<CySubNetwork> model) {
		if (model != null) {
			model.addPropertyChangeListener("viewCount", (PropertyChangeEvent evt) -> {
				updateViewInfo();
			});
		}
		
		super.setModel(model);
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void setDepth(int newValue) {
		if (newValue != depth) {
			int oldValue = depth;
			depth = newValue;
			firePropertyChange("depth", oldValue, newValue);
		}
	}
	
	public boolean isDescendantOf(CySubNetwork parentNet) {
		var net = getModel().getNetwork();
		
		while (net != null) {
			net = ViewUtil.getParent(net, serviceRegistrar);
			
			if (parentNet.equals(net))
				return true;
		}
		
		return false;
	}
	
	public void setShowIndentation(final boolean newValue) {
		if (newValue != showIndentation) {
			boolean oldValue = showIndentation;
			showIndentation = newValue;
			updateIndentation();
			firePropertyChange("showIndentation", oldValue, newValue);
		}
	}
	
	@Override
	public Dimension getMaximumSize() {
		var size = getPreferredSize();
	    size.width = Short.MAX_VALUE;
	    
	    return size;
	}
	
	@Override
	public void update() {
		super.update();
		
		updateCurrentLabel();
		updateViewInfo();
		updateIndentation();
		updateCountLabels();
		repaint();
	}
	
	private void updateViewInfo() {
		int viewCount = getModel().getViewCount();
		String viewCountText = " ";
		
		if (viewCount > 9)
			viewCountText = "\u208A"; // Subscript plus sign ('+')
		else if (viewCount > 1)
			viewCountText = Character.toString((char) (0x2080 + viewCount)); // Create a subscript number
		
		getViewCountLabel().setText(viewCountText);
		
		updateViewIconLabel();
		repaint();
	}
	
	private void updateViewIconLabel() {
		int viewCount = getModel().getViewCount();
		
		getViewIconLabel().setText(viewCount == 0 ? ICON_SHARE_ALT : ICON_SHARE_ALT_SQUARE);
		getViewIconLabel().setForeground(
				UIManager.getColor(viewCount == 0 ? "Label.disabledForeground" : "Label.foreground"));
		getViewIconLabel().setToolTipText((viewCount > 0 ? viewCount : "No") + " view" + (viewCount == 1 ? "" : "s"));
	}

	protected void updateCurrentLabel() {
		getCurrentLabel().setText(getModel().isCurrent() ? IconManager.ICON_CIRCLE : " ");
		getCurrentLabel().setToolTipText(getModel().isCurrent() ? "Current Network" : null);
	}
	
	protected void updateIndentation() {
		int indent = showIndentation ? depth * INDENT_WIDTH : 0;
		var d = new Dimension(indent, getIndentLabel().getPreferredSize().height);
		getIndentLabel().setPreferredSize(d);
		getIndentLabel().setMinimumSize(d);
		getIndentLabel().setMaximumSize(d);
		getIndentLabel().setSize(d);
		revalidate();
	}
	
	protected void updateCountLabels() {
		getNodeCountLabel().setText("" + getModel().getNodeCount());
		getEdgeCountLabel().setText("" + getModel().getEdgeCount());
	}
	
	@Override
	protected void init() {
		setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
		
		var layout = new GroupLayout(this);
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
				.addComponent(getNameLabel())
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
			
			var d = new Dimension(
					viewCountLabel.getPreferredSize().width,
					getViewIconLabel().getPreferredSize().height
			);
			viewCountLabel.setMinimumSize(d);
			viewCountLabel.setPreferredSize(d);
			viewCountLabel.setMaximumSize(d);
			viewCountLabel.setSize(d);
		}
		
		return viewCountLabel;
	}
	
	protected JLabel getViewIconLabel() {
		if (viewIconLabel == null) {
			var iconManager = serviceRegistrar.getService(IconManager.class);
			
			viewIconLabel = new JLabel(ICON_SHARE_ALT_SQUARE);
			viewIconLabel.setFont(iconManager.getIconFont(16.0f));

// TODO Uncomment when multiple views support is enabled
//			viewIconLabel.addMouseListener(new MouseAdapter() {
//				@Override
//				public void mouseEntered(MouseEvent e) {
//					int viewCount = getModel().getViewCount();
//					
//					if (viewCount > 0)
//						getViewIconLabel().setForeground(UIManager.getColor("Focus.color"));
//				}
//				@Override
//				public void mouseExited(MouseEvent e) {
//					int viewCount = getModel().getViewCount();
//					
//					if (viewCount > 0)
//						updateViewIconLabel();
//				}
//			});
		}
		
		return viewIconLabel;
	}
	
	protected JLabel getNodeCountLabel() {
		if (nodeCountLabel == null) {
			nodeCountLabel = new JLabel();
			nodeCountLabel.setToolTipText("Nodes");
			nodeCountLabel.setFont(nodeCountLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			nodeCountLabel.setHorizontalAlignment(JLabel.RIGHT);
			nodeCountLabel.setForeground(UIManager.getColor("Label.infoForeground"));
		}
		
		return nodeCountLabel;
	}
	
	protected JLabel getEdgeCountLabel() {
		if (edgeCountLabel == null) {
			edgeCountLabel = new JLabel();
			edgeCountLabel.setToolTipText("Edges");
			edgeCountLabel.setFont(edgeCountLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			edgeCountLabel.setHorizontalAlignment(JLabel.RIGHT);
			edgeCountLabel.setForeground(UIManager.getColor("Label.infoForeground"));
		}
		
		return edgeCountLabel;
	}
}
