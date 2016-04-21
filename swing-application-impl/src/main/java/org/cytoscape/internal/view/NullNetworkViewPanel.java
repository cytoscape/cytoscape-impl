package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.internal.util.ViewUtil.styleToolBarButton;
import static org.cytoscape.util.swing.IconManager.ICON_EXTERNAL_LINK_SQUARE;
import static org.cytoscape.util.swing.IconManager.ICON_SHARE_ALT;
import static org.cytoscape.util.swing.IconManager.ICON_THUMB_TACK;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;

import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;

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
public class NullNetworkViewPanel extends JPanel {

	public static final String NAME = "__NULL_VIEW_PANEL__";
	
	private JPanel centerPanel;
	private JPanel toolBar;
	private JLabel infoLabel;
	private JLabel infoIconLabel;
	private JLabel gapLabel;
	private JButton reattachViewButton;
	private JLabel titleLabel;
	private JButton createViewButton;
	private final GridViewTogglePanel gridViewTogglePanel;
	
	private final JSeparator sep1 = new JSeparator(JSeparator.VERTICAL);
	private final JSeparator sep2 = new JSeparator(JSeparator.VERTICAL);
	
	private CyNetworkView networkView;
	private CyNetwork network;
	
	private final CyServiceRegistrar serviceRegistrar;

	public NullNetworkViewPanel(
			final GridViewToggleModel gridViewToggleModel,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.gridViewTogglePanel = new GridViewTogglePanel(gridViewToggleModel, serviceRegistrar);
		this.serviceRegistrar = serviceRegistrar;
		
		init();
	}

	public CyNetwork getNetwork() {
		return network;
	}
	
	public CyNetworkView getNetworkView() {
		return networkView;
	}
	
	/**
	 * @param network Can be null
	 */
	public void update(final CyNetwork network) {
		this.network = network;
		this.networkView = null;
		update();
	}
	
	/**
	 * @param networkView Must not be null
	 */
	public void update(final CyNetworkView networkView) {
		this.networkView = networkView;
		this.network = networkView.getModel();
		update();
	}
	
	private void update() {
		if (networkView != null) {
			getInfoIconLabel().setText(ICON_EXTERNAL_LINK_SQUARE);
			getInfoIconLabel().setToolTipText("This view is detached");
		} else if (network != null) {
			getInfoIconLabel().setText(ICON_SHARE_ALT);
			getInfoIconLabel().setToolTipText(
					network instanceof CySubNetwork ?
							"This network has no views" : "A network collection cannot have views"
			);
		} else {
			getInfoIconLabel().setText(null);
			getInfoIconLabel().setToolTipText(null);
		}
		
		getInfoIconLabel().setVisible(networkView != null || network != null);
		getInfoLabel().setVisible(network == null && networkView == null);
		getCreateViewButton().setVisible(networkView == null && network instanceof CySubNetwork);
		getReattachViewButton().setVisible(networkView != null);
		sep2.setVisible(networkView != null);
		
		if (networkView != null)
			getTitleLabel().setText(ViewUtil.getTitle(networkView));
		else if (network != null)
			getTitleLabel().setText(ViewUtil.getName(network));
		else
			getTitleLabel().setText(null);
		
		final Dimension d = new Dimension(
				1,
				network != null && networkView == null ? getCreateViewButton().getPreferredSize().height : 0
		);
		getGapLabel().setPreferredSize(d);
		getGapLabel().setMinimumSize(d);
		getGapLabel().setMaximumSize(d);
		getGapLabel().setSize(d);
		
		getToolBar().updateUI();
	}
	
	private void init() {
		setName(NAME);
		
		setLayout(new BorderLayout());
		add(getCenterPanel(), BorderLayout.CENTER);
		add(getToolBar(), BorderLayout.SOUTH);
		
		update();
	}
	
	private JPanel getCenterPanel() {
		if (centerPanel == null) {
			centerPanel = new JPanel();
			centerPanel.setLayout(new BorderLayout());
			
			final GroupLayout layout = new GroupLayout(centerPanel);
			centerPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addGroup(layout.createParallelGroup(CENTER, true)
							.addComponent(getGapLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getInfoIconLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getInfoLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addComponent(getCreateViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					)
					.addGap(0, 0, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(getGapLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(getInfoIconLabel())
					.addComponent(getInfoLabel())
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(getCreateViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
			);
		}
		
		return centerPanel;
	}
	
	private JPanel getToolBar() {
		if (toolBar == null) {
			toolBar = new JPanel();
			toolBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
			
			final GroupLayout layout = new GroupLayout(toolBar);
			toolBar.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(!isAquaLAF());
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addContainerGap()
					.addComponent(gridViewTogglePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(sep1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getReattachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(sep2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(getTitleLabel())
					.addGap(0, 0, Short.MAX_VALUE)
					.addContainerGap()
			);
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, false)
					.addComponent(gridViewTogglePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getReattachViewButton(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(sep2, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getTitleLabel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		return toolBar;
	}
	
	private JLabel getInfoLabel() {
		if (infoLabel == null) {
			infoLabel = new JLabel("No networks selected");
			infoLabel.setFont(infoLabel.getFont().deriveFont(18.0f));
			infoLabel.setEnabled(false);
			infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		}
		
		return infoLabel;
	}
	
	JLabel getInfoIconLabel() {
		if (infoIconLabel == null) {
			infoIconLabel = new JLabel();
			infoIconLabel.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(96.0f));
			infoIconLabel.setHorizontalAlignment(JLabel.CENTER);
			infoIconLabel.setVerticalAlignment(JLabel.CENTER);
			
			Color c = UIManager.getColor("Label.disabledForeground");
			c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 40);
			infoIconLabel.setForeground(c);
		}
		
		return infoIconLabel;
	}
	
	private JLabel getGapLabel() {
		if (gapLabel == null) {
			gapLabel = new JLabel(" ");
		}
		
		return gapLabel;
	}
	
	JButton getReattachViewButton() {
		if (reattachViewButton == null) {
			reattachViewButton = new JButton(" " + ICON_THUMB_TACK + " ");
			reattachViewButton.setToolTipText("Reattach View");
			styleToolBarButton(reattachViewButton, serviceRegistrar.getService(IconManager.class).getIconFont(14.0f));
		}
		
		return reattachViewButton;
	}
	
	private JLabel getTitleLabel() {
		if (titleLabel == null) {
			titleLabel = new JLabel();
			titleLabel.setFont(titleLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			titleLabel.setEnabled(false);
		}
		
		return titleLabel;
	}
	
	JButton getCreateViewButton() {
		if (createViewButton == null) {
			createViewButton = new JButton("Create View");
			
			if (isAquaLAF()) {
				createViewButton.putClientProperty("JButton.buttonType", "gradient");
				createViewButton.putClientProperty("JComponent.sizeVariant", "small");
			}
		}
		
		return createViewButton;
	}
}
