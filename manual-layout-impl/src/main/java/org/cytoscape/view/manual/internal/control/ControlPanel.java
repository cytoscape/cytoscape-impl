package org.cytoscape.view.manual.internal.control;

import java.awt.Dimension;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
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
import org.cytoscape.session.events.SessionLoadCancelledEvent;
import org.cytoscape.session.events.SessionLoadCancelledListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.manual.internal.Util;
import org.cytoscape.view.manual.internal.common.AbstractManualPanel;
import org.cytoscape.view.manual.internal.control.view.AlignPanel;
import org.cytoscape.view.manual.internal.control.view.DistPanel;
import org.cytoscape.view.manual.internal.control.view.StackPanel;
import org.cytoscape.view.manual.internal.rotate.RotatePanel;
import org.cytoscape.view.manual.internal.scale.ScalePanel;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
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

/**
 * GUI for Align and Distribute of manualLayout
 */
@SuppressWarnings("serial")
public class ControlPanel extends AbstractManualPanel implements SessionAboutToBeLoadedListener,
		SessionLoadCancelledListener, SessionLoadedListener, SetCurrentNetworkViewListener, RowsSetListener {
	
	private ScalePanel scalePanel;
	private AlignPanel alignPanel;
	private DistPanel distPanel;
	private StackPanel stackPanel;
	private RotatePanel rotatePanel;
	
	private boolean loadingSession;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ControlPanel(final CyServiceRegistrar serviceRegistrar) {
		super("Layout Tools");
		this.serviceRegistrar = serviceRegistrar;
		
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
		
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
		setLayout(new BoxLayout(this,  BoxLayout.PAGE_AXIS));

		add(scalePanel = new ScalePanel(appMgr));
		add(alignPanel = new AlignPanel(appMgr));
		add(distPanel = new DistPanel(appMgr));
		add(stackPanel = new StackPanel(appMgr));
		add(Box.createRigidArea(new Dimension(3, 20)));
		add(rotatePanel = new RotatePanel(appMgr));
		
		updatePanels();
	}

	@Override
	public void handleEvent(final SessionAboutToBeLoadedEvent e) {
		loadingSession = true;
	}
	
	@Override
	public void handleEvent(final SessionLoadCancelledEvent e) {
		loadingSession = false;
		updatePanels();
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
		final Collection<View<CyNode>> selectedNodeViews = view != null ? Util.findSelectedNodes(view) : null;
		
		scalePanel.setEnabled(view != null);
		rotatePanel.setEnabled(view != null);
		
		final boolean enabled = selectedNodeViews != null && !selectedNodeViews.isEmpty();
		
		alignPanel.setEnabled(enabled);
		distPanel.setEnabled(enabled);
		stackPanel.setEnabled(enabled);
	}
} 
