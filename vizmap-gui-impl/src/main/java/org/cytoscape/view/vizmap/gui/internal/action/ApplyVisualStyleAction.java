package org.cytoscape.view.vizmap.gui.internal.action;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.task.visualize.ApplyVisualStyleTaskFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VisualStyleSelector;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.ListSingleSelection;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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
public class ApplyVisualStyleAction extends AbstractCyAction {
	
	public static final String NAME = "Apply Style...";
	
	private boolean cancelled;
	private final ServicesUtil servicesUtil;
	
	public ApplyVisualStyleAction(ServicesUtil servicesUtil) {
		super(NAME);
		this.servicesUtil = servicesUtil;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		var views = servicesUtil.get(CyApplicationManager.class).getSelectedNetworkViews();

		if (views.isEmpty())
			return;

		var style = selectVisualStyle(views, evt);

		if (style == null)
			return;

		var tunables = new HashMap<String, Object>();
		tunables.put("styles", new ListSingleSelection<VisualStyle>(style));

		var factory = servicesUtil.get(ApplyVisualStyleTaskFactory.class);
		var tunableSetter = servicesUtil.get(TunableSetter.class);
		var taskIterator = tunableSetter.createTaskIterator(factory.createTaskIterator(views), tunables);

		var taskManager = servicesUtil.get(DialogTaskManager.class);
		taskManager.execute(taskIterator);
	}
	
	@Override
	public void updateEnableState() {
		var views = servicesUtil.get(CyApplicationManager.class).getSelectedNetworkViews();
		var factory = servicesUtil.get(ApplyVisualStyleTaskFactory.class);
		setEnabled(factory.isReady(views));
	}
	
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
		updateEnableState();
	}
	
	private VisualStyle selectVisualStyle(List<CyNetworkView> views, ActionEvent evt) {
		var c = evt.getSource() instanceof Component ? (Component) evt.getSource() : null;
		var owner = c != null ? SwingUtilities.getWindowAncestor(c)
				: servicesUtil.get(CySwingApplication.class).getJFrame();

		var dialog = new JDialog(owner, "Styles", ModalityType.APPLICATION_MODAL);
		
		var vmProxy = (VizMapperProxy) servicesUtil.getProxy(VizMapperProxy.NAME);
		var styles = vmProxy.getVisualStyles();
		var oldValue = getCommonVisualStyle(views);
		
		var styleSelector = new VisualStyleSelector(servicesUtil);
		styleSelector.update(styles);
		styleSelector.setSelectedItem(oldValue);
		
		var cancelBtn = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
				dialog.dispose();
			}
		});
		var okBtn = new JButton(new AbstractAction("Apply") {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelled = false;
				dialog.dispose();
			}
		});
		
		// I'm not sure we should allow the user to apply the same style again, but since the old UI
		// allowed it and disabling the Apply button when the old value is selected again may be confusing to the user, 
		// I'm only checking whether or not a style has been selected in order to enable the button
		okBtn.setEnabled(oldValue != null);
		
		styleSelector.addPropertyChangeListener("selectedItem", e -> {
			var newValue = (VisualStyle) e.getNewValue();
			okBtn.setEnabled(newValue != null/* && !newValue.equals(oldValue)*/);
		});
		
		var okCancelPanel = LookAndFeelUtil.createOkCancelPanel(okBtn, cancelBtn);
		
		var layout = new GroupLayout(dialog.getContentPane());
		dialog.getContentPane().setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER)
				.addComponent(styleSelector, 500, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(okCancelPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(styleSelector, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(okCancelPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), okBtn.getAction(), cancelBtn.getAction());
		dialog.getRootPane().setDefaultButton(okBtn);
		
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		
		var selectedStyle = cancelled ? null : styleSelector.getSelectedItem();
		styleSelector.dispose();
		
		return selectedStyle;
	}

	/**
	 * Returns null if the views do not share the same style.
	 */
	private VisualStyle getCommonVisualStyle(List<CyNetworkView> views) {
		var set = new HashSet<VisualStyle>();
		var vmManager = servicesUtil.get(VisualMappingManager.class);
		
		for (var v : views) {
			var style = vmManager.getVisualStyle(v);
			
			if (style != null)
				set.add(style);
		}
		
		return set.size() == 1 ? set.iterator().next() : null;
	}
}
