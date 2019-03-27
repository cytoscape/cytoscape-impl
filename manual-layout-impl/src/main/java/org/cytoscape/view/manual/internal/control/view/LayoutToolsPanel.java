package org.cytoscape.view.manual.internal.control.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.view.manual.internal.util.Util.findSelectedNodes;
import static org.cytoscape.view.manual.internal.util.Util.invokeOnEDT;

import java.awt.Component;
import java.util.Collection;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.manual.internal.rotate.RotatePanel;
import org.cytoscape.view.manual.internal.scale.ScalePanel;
import org.cytoscape.view.manual.internal.util.IconUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

/**
 * GUI for Align and Distribute of manualLayout
 */
@SuppressWarnings("serial")
public class LayoutToolsPanel extends JPanel implements CytoPanelComponent2, SessionAboutToBeLoadedListener,
		SessionLoadedListener, SetCurrentNetworkViewListener, RowsSetListener {
	
	private static final String TITLE = "Node Layout Tools";
	private static final String ID = "org.cytoscape.NodeLayoutTools";
	
	private ScalePanel scalePanel;
	private AlignPanel alignPanel;
	private DistPanel distPanel;
	private StackPanel stackPanel;
	private RotatePanel rotatePanel;
	
	private TextIcon icon;
	
	private boolean loadingSession;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public LayoutToolsPanel(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		final IconManager iconMgr = serviceRegistrar.getService(IconManager.class);
		
		scalePanel = new ScalePanel(appMgr, iconMgr);
		alignPanel = new AlignPanel(appMgr);
		distPanel = new DistPanel(appMgr);
		stackPanel = new StackPanel(appMgr);
		rotatePanel = new RotatePanel(appMgr);
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(false);
		
		int w = 320;
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
						.addComponent(scalePanel, DEFAULT_SIZE, w, w)
						.addComponent(alignPanel, DEFAULT_SIZE, w, w)
						.addComponent(distPanel, DEFAULT_SIZE, w, w)
						.addComponent(stackPanel, DEFAULT_SIZE, w, w)
						.addComponent(rotatePanel, DEFAULT_SIZE, w, w)
				)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(scalePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(alignPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(distPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(stackPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(rotatePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		if (isAquaLAF())
			setOpaque(false);
		
		updatePanels();
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.SOUTH_WEST;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public Icon getIcon() {
		if (icon == null)
			icon = new TextIcon(IconUtil.RULER_COMBINED_SOLID,
					serviceRegistrar.getService(IconManager.class).getIconFont(IconUtil.CY_FONT_NAME, 14.0f), 16, 16);
		
		return icon;
	}
	
	@Override
	public String getIdentifier() {
		return ID;
	}
	
	@Override
	public void handleEvent(final SessionAboutToBeLoadedEvent e) {
		loadingSession = true;
	}
	
	@Override
	public void handleEvent(final SessionLoadedEvent e) {
		loadingSession = false;
		updatePanels();
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		if (!loadingSession)
			updatePanels();
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		if (loadingSession)
			return;
		
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		final CyNetworkView currentView = appMgr.getCurrentNetworkView();
		
		if (currentView == null)
			return;
		
		final CyTable tbl = e.getSource();
		final CyNetworkTableManager netTblMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		final CyNetwork net = netTblMgr.getNetworkForTable(tbl);
		
		if (net == null || !net.equals(currentView.getModel()) || !tbl.equals(net.getDefaultNodeTable()))
			return;
		
		final Collection<RowSetRecord> selectedRecords = e.getColumnRecords(CyNetwork.SELECTED);
		
		if (!selectedRecords.isEmpty())
			updatePanels();
	}

	private void updatePanels() {
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		final CyNetworkView view = appMgr.getCurrentNetworkView();
		final Collection<View<CyNode>> selectedNodeViews = view != null ? findSelectedNodes(view) : null;
		
		invokeOnEDT(() -> {
			scalePanel.setEnabled(view != null);
			rotatePanel.setEnabled(view != null);
			
			final boolean enabled = selectedNodeViews != null && !selectedNodeViews.isEmpty();
			
			alignPanel.setEnabled(enabled);
			distPanel.setEnabled(enabled);
			stackPanel.setEnabled(enabled);
		});
	}
} 
