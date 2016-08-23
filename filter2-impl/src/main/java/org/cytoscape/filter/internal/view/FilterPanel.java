package org.cytoscape.filter.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.cytoscape.filter.internal.filters.composite.CompositeFilterPanel;
import org.cytoscape.filter.internal.work.FilterWorker;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Filters 2 Impl (filter2-impl)
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
public class FilterPanel extends AbstractPanel<FilterElement, FilterPanelController> {
	
	private CompositeFilterPanel<FilterPanel> root;
	private JCheckBox applyAutomaticallyCheckBox;

	public FilterPanel(final FilterPanelController controller, final FilterWorker worker,
			final CyServiceRegistrar serviceRegistrar) {
		super(controller, serviceRegistrar);
		setOpaque(!isAquaLAF());
		
		worker.setView(this);
		
		applyAutomaticallyCheckBox = new JCheckBox("Apply when filter changes"); 
		applyAutomaticallyCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.setInteractive(applyAutomaticallyCheckBox.isSelected(), FilterPanel.this);
			}
		});
		applyAutomaticallyCheckBox.setOpaque(!isAquaLAF());
		
		applyAutomaticallyCheckBox.setToolTipText("<html>Apply the filter automatically when the filter definition changes.<br>(Turned off by default for networks that are very large.)</html>");
		
		final JPanel applyPanel = createApplyPanel();
		final Component editPanel = createEditPanel();
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(!isAquaLAF());
		layout.setAutoCreateGaps(!isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(namedElementComboBox, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(optionsButton, PREFERRED_SIZE, 64, PREFERRED_SIZE)
				)
				.addComponent(editPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(applyAutomaticallyCheckBox, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(applyPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(namedElementComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(optionsButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addComponent(editPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(applyAutomaticallyCheckBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(applyPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		ComboBoxModel model = controller.getElementComboBoxModel();
		FilterElement element = (FilterElement) model.getSelectedItem();
		createView(element.getFilter());
		
		controller.synchronize(this);
	}
	
	private void createView(CompositeFilter<CyNetwork, CyIdentifiable> filter) {
		if (filter == null) {
			setRootPanel(null);
			return;
		}
		
		// We're passing in a CompositeFilter so we can assume we're getting
		// back a CompositeFilterPanel.
		@SuppressWarnings("unchecked")
		CompositeFilterPanel<FilterPanel> panel = (CompositeFilterPanel<FilterPanel>) controller.createView(this, filter, 0);
		new TransformerElementViewModel<>(panel, controller, this);
		setRootPanel(panel);
	}

	@Override
	public CompositeFilterPanel<FilterPanel> getRootPanel() {
		return root;
	}

	public void setRootPanel(CompositeFilterPanel<FilterPanel> panel) {
		root = panel;
		scrollPane.setViewportView(root);
		
		if (root == null) {
			return;
		}
		root.updateLayout();
	}
	
	public JComboBox getFilterComboBox() {
		return namedElementComboBox;
	}
	
	public JCheckBox getApplyAutomaticallyCheckBox() {
		return applyAutomaticallyCheckBox;
	}
	
	@Override
	public void reset() {
		setRootPanel(null);
	}
}
