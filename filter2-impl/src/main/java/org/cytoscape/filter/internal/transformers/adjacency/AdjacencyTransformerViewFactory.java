package org.cytoscape.filter.internal.transformers.adjacency;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.filter.internal.transformers.adjacency.AdjacencyTransformer.Action;
import org.cytoscape.filter.internal.transformers.adjacency.AdjacencyTransformer.EdgesAre;
import org.cytoscape.filter.internal.transformers.adjacency.AdjacencyTransformer.What;
import org.cytoscape.filter.internal.view.ComboItem;
import org.cytoscape.filter.internal.view.CompositeFilterLayoutUpdator;
import org.cytoscape.filter.internal.view.CompositeFilterLayoutUpdator.LayoutUpdatable;
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.internal.view.look.FilterPanelStyle;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.filter.view.TransformerViewFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

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

public class AdjacencyTransformerViewFactory implements TransformerViewFactory {

	private final FilterPanelStyle style;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public AdjacencyTransformerViewFactory(final FilterPanelStyle style, final CyServiceRegistrar serviceRegistrar) {
		this.style = style;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	public static Properties getServiceProperties() {
		Properties props = new Properties();
		props.setProperty("addButtonTooltip", "Add condition on adjacent nodes/edges...");
		return props;
	}
	
	@Override
	public String getId() {
		return Transformers.ADJACENCY_TRANSFORMER;
	}

	@Override
	public JComponent createView(Transformer<?, ?> transformer) {
		AdjacencyTransformer model = (AdjacencyTransformer)transformer;
		View view = new View(model);
		model.addListener(new CompositeFilterLayoutUpdator(view, model.getCompositeFilter()));
		return view;
	}

	
	@SuppressWarnings("serial")
	class View extends JPanel implements LayoutUpdatable {
		private final AdjacencyTransformer model;
		
		private GroupLayout layout;
		
		private JLabel label1, label2, label3a, label3b, label4;
		private JLabel arrowLabel;
		private JComboBox<ComboItem<Action>> actionCombo;
		private JComboBox<ComboItem<What>> outputCombo;
		private JComboBox<ComboItem<EdgesAre>> edgesAreCombo;
		private JComboBox<ComboItem<What>> filterTargetCombo;
		
		private boolean extraExpanded = false;
		
		public View(AdjacencyTransformer model) {
			this.model = model;
			ViewUtil.configureFilterView(this);
			
			final IconManager iconManager = serviceRegistrar.getService(IconManager.class);
			
			// Create UI controls
			label1 = style.createLabel("Take nodes and");
			
			actionCombo = style.createCombo();
			actionCombo.addItem(new ComboItem<>(Action.ADD, "add"));
			actionCombo.addItem(new ComboItem<>(Action.REPLACE, "replace with"));
			
			outputCombo = style.createCombo();
			outputCombo.addItem(new ComboItem<>(What.NODES_AND_EDGES, "adjacent nodes and edges"));
			outputCombo.addItem(new ComboItem<>(What.NODES, "adjacent nodes"));
			outputCombo.addItem(new ComboItem<>(What.EDGES, "adjacent edges"));
			
			arrowLabel = style.createLabel(IconManager.ICON_CARET_LEFT);
			arrowLabel.setFont(iconManager.getIconFont(16.0f));
			
			label2 = style.createLabel("where the adjacent edges are");
			
			edgesAreCombo = style.createCombo();
			edgesAreCombo.addItem(new ComboItem<>(EdgesAre.INCOMING_AND_OUTGOING, "incoming and outgoing"));
			edgesAreCombo.addItem(new ComboItem<>(EdgesAre.INCOMING, "incoming"));
			edgesAreCombo.addItem(new ComboItem<>(EdgesAre.OUTGOING, "outgoing"));
			
			label3a = style.createLabel("and the");
			label3b = style.createLabel("where the");
			
			filterTargetCombo = style.createCombo();
			filterTargetCombo.addItem(new ComboItem<>(What.NODES_AND_EDGES, "adjacent nodes and edges"));
			filterTargetCombo.addItem(new ComboItem<>(What.NODES, "adjacent nodes"));
			filterTargetCombo.addItem(new ComboItem<>(What.EDGES, "adjacent edges"));
			
			label4 = style.createLabel("match the filter:");
			
			
			// Initialize UI
			actionCombo.setSelectedItem(ComboItem.of(model.getAction()));
			outputCombo.setSelectedItem(ComboItem.of(model.getOutput()));
			edgesAreCombo.setSelectedItem(ComboItem.of(model.getEdgesAre()));
			filterTargetCombo.setSelectedItem(ComboItem.of(model.getFilterTarget()));
			
			
			// now that UI has been initialized we can attach event handlers
			actionCombo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ComboItem<Action> item = actionCombo.getItemAt(actionCombo.getSelectedIndex());
					model.setAction(item.getValue());
				}
			});
			
			outputCombo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ComboItem<What> item = outputCombo.getItemAt(outputCombo.getSelectedIndex());
					model.setOutput(item.getValue());
				}
			});
			
			edgesAreCombo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ComboItem<EdgesAre> item = edgesAreCombo.getItemAt(edgesAreCombo.getSelectedIndex());
					model.setEdgesAre(item.getValue());
				}
			});
			
			filterTargetCombo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ComboItem<What> item = filterTargetCombo.getItemAt(filterTargetCombo.getSelectedIndex());
					model.setFilterTarget(item.getValue());
				}
			});
			
			arrowLabel.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent event) {
					handleArrowClicked();
				}
			});
			
			// Create layout
			layout = new GroupLayout(this);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
			setLayout(layout);
			updateLayout();
		}
		
		private void handleArrowClicked() {
			extraExpanded = !extraExpanded;
			if (extraExpanded) {
				arrowLabel.setText(IconManager.ICON_CARET_DOWN);
			} else {
				arrowLabel.setText(IconManager.ICON_CARET_LEFT);
			}
			updateLayout();
		}
		
		@Override
		public void updateLayout() {
			removeAll();
			
			Group horizontalGroup =
				layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
						.addComponent(label1)
						.addComponent(actionCombo)
						.addComponent(outputCombo)
						.addComponent(arrowLabel));
			
			Group verticalGroup =
				layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER)
						.addComponent(label1)
						.addComponent(actionCombo)
						.addComponent(outputCombo)
						.addComponent(arrowLabel));
					
			if(extraExpanded) {
				horizontalGroup
					.addGroup(layout.createSequentialGroup()
						.addComponent(label2)
						.addComponent(edgesAreCombo));
				
				verticalGroup
					.addGroup(layout.createParallelGroup(Alignment.CENTER)
						.addComponent(label2)
						.addComponent(edgesAreCombo));
			}
			
			if(model.hasSubfilters()) {
				horizontalGroup
					.addGroup(layout.createSequentialGroup()
						.addComponent(extraExpanded ? label3a : label3b)
						.addComponent(filterTargetCombo)
						.addComponent(label4));
				
				verticalGroup
					.addGroup(layout.createParallelGroup(Alignment.CENTER)
						.addComponent(extraExpanded ? label3a : label3b)
						.addComponent(filterTargetCombo)
						.addComponent(label4));
			}
			
			layout.setHorizontalGroup(horizontalGroup);
			layout.setVerticalGroup(verticalGroup);
		}
		
		
	}
}
