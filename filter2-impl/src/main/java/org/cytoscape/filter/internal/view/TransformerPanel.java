package org.cytoscape.filter.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Component;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.cytoscape.filter.internal.filters.composite.CompositeTransformerPanel;
import org.cytoscape.filter.internal.work.TransformerWorker;
import org.cytoscape.filter.model.Transformer;
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
public class TransformerPanel extends AbstractPanel<TransformerElement, TransformerPanelController> {
	
	private CompositeTransformerPanel root;
	private JComboBox<FilterElement> startWithComboBox;
	
	@SuppressWarnings("unchecked")
	public TransformerPanel(final TransformerPanelController controller, TransformerWorker worker,
			final CyServiceRegistrar serviceRegistrar) {
		super(controller, serviceRegistrar);
		setOpaque(!isAquaLAF());

		worker.setView(this);
		
		final JPanel applyPanel = createApplyPanel();
		final Component editPanel = createEditPanel();
		final JLabel startWithLabel = new JLabel("Start with:");
		startWithComboBox = new JComboBox<>(controller.getStartWithComboBoxModel());
		startWithComboBox.setRenderer(ViewUtil.createElipsisRenderer(50));
		final JSeparator sep = new JSeparator();
		
		final GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setAutoCreateContainerGaps(!isAquaLAF());
		layout.setAutoCreateGaps(!isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(namedElementComboBox, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(optionsButton, PREFERRED_SIZE, 64, PREFERRED_SIZE)
				)
				.addComponent(sep, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(startWithLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(startWithComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(editPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(applyPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(namedElementComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(optionsButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addComponent(sep, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(startWithLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(startWithComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addComponent(editPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(applyPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		DynamicComboBoxModel<TransformerElement> model = controller.getElementComboBoxModel();
		TransformerElement element = (TransformerElement) model.getSelectedItem();
		createView(element.getChain());

		controller.synchronize(this);
	}

	private void createView(List<Transformer<CyNetwork, CyIdentifiable>> chain) {
		if (chain == null) {
			setRootPanel(null);
			return;
		}
		
		CompositeTransformerPanel panel = new CompositeTransformerPanel(this, controller, chain, serviceRegistrar);
		new TransformerElementViewModel<>(panel, controller, this);
		setRootPanel(panel);
	}

	void setRootPanel(CompositeTransformerPanel panel) {
		root = panel;
		scrollPane.setViewportView(root);
		
		if (root == null) {
			return;
		}
		root.updateLayout();
	}

	@Override
	public CompositeTransformerPanel getRootPanel() {
		return root;
	}
	
	@Override
	public void reset() {
		setRootPanel(null);
	}
}
