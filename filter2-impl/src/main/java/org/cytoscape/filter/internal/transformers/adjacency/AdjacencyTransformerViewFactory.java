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
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.TransformerListener;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.filter.view.TransformerViewFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

public class AdjacencyTransformerViewFactory implements TransformerViewFactory {

	private IconManager iconManager;
	
	public AdjacencyTransformerViewFactory(IconManager iconManager) {
		this.iconManager = iconManager;
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
		model.addListener(new UpdateLayoutListener(view, model));
		return view;
	}

	
	class UpdateLayoutListener implements TransformerListener {
		private final AdjacencyTransformer model;
		private final View view;
		private int savedLength;
		
		public UpdateLayoutListener(View view, AdjacencyTransformer model) {
			this.view = view;
			this.model = model;
			this.savedLength = model.getFilterCount();
		}
		
		@Override
		public synchronized void handleSettingsChanged() {
			if(savedLength != model.getFilterCount()) {
				view.updateLayout();
			}
			savedLength = model.getFilterCount();
		}
	}
	
	
	@SuppressWarnings("serial")
	class View extends JPanel {
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
			
			// Create UI controls
			label1 = new JLabel("Take nodes and");
			
			actionCombo = new JComboBox<>();
			actionCombo.addItem(new ComboItem<>(Action.ADD, "add"));
			actionCombo.addItem(new ComboItem<>(Action.REPLACE, "replace with"));
			
			outputCombo = new JComboBox<>();
			outputCombo.addItem(new ComboItem<>(What.NODES_AND_EDGES, "adjacent nodes and edges"));
			outputCombo.addItem(new ComboItem<>(What.NODES, "adjacent nodes"));
			outputCombo.addItem(new ComboItem<>(What.EDGES, "adjacent edges"));
			
			arrowLabel = new JLabel(IconManager.ICON_CARET_LEFT);
			arrowLabel.setFont(iconManager.getIconFont(16.0f));
			
			label2 = new JLabel("where the adjacent edges are");
			
			edgesAreCombo = new JComboBox<>();
			edgesAreCombo.addItem(new ComboItem<>(EdgesAre.INCOMING_AND_OUTGOING, "incoming and outgoing"));
			edgesAreCombo.addItem(new ComboItem<>(EdgesAre.INCOMING, "incoming"));
			edgesAreCombo.addItem(new ComboItem<>(EdgesAre.OUTGOING, "outgoing"));
			
			label3a = new JLabel("and the");
			label3b = new JLabel("where the");
			
			filterTargetCombo = new JComboBox<>();
			filterTargetCombo.addItem(new ComboItem<>(What.NODES_AND_EDGES, "adjacent nodes and edges"));
			filterTargetCombo.addItem(new ComboItem<>(What.NODES, "adjacent nodes"));
			filterTargetCombo.addItem(new ComboItem<>(What.EDGES, "adjacent edges"));
			
			label4 = new JLabel("match the filter:");
			
			
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
