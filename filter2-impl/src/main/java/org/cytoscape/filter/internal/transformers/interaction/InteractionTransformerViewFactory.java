package org.cytoscape.filter.internal.transformers.interaction;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.filter.internal.transformers.interaction.InteractionTransformer.Action;
import org.cytoscape.filter.internal.view.ComboItem;
import org.cytoscape.filter.internal.view.CompositeFilterLayoutUpdator;
import org.cytoscape.filter.internal.view.CompositeFilterLayoutUpdator.LayoutUpdatable;
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.internal.view.look.FilterPanelStyle;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.filter.view.TransformerViewFactory;

public class InteractionTransformerViewFactory implements TransformerViewFactory {
	
	private final FilterPanelStyle style;
	
	public InteractionTransformerViewFactory(FilterPanelStyle style) {
		this.style = style;
	}
	
	@Override
	public String getId() {
		return Transformers.INTERACTION_TRANSFORMER;
	}

	@Override
	public JComponent createView(Transformer<?, ?> transformer) {
		InteractionTransformer model = (InteractionTransformer) transformer;
		View view = new View(model);
		model.addListener(new CompositeFilterLayoutUpdator(view, model.getCompositeFilter()));
		return view;
	}

	
	
	@SuppressWarnings("serial")
	class View extends JPanel implements LayoutUpdatable {
		
		private final InteractionTransformer model;
		
		private GroupLayout layout;
		
		private JLabel label1, label2;
		private JComboBox<ComboItem<Action>> actionCombo;
		private JComboBox<ComboItem<Runnable>> selectCombo;
		
		
		public View(InteractionTransformer model) {
			this.model = model;
			ViewUtil.configureFilterView(this);
			
			label1 = style.createLabel("Take edges and");
			
			actionCombo = style.createCombo();
			actionCombo.addItem(new ComboItem<>(Action.ADD, "add"));
			actionCombo.addItem(new ComboItem<>(Action.REPLACE, "replace with"));
			
			selectCombo = style.createCombo();
			selectCombo.addItem(new ComboItem<>(this::selectSourceAndTarget, "source and target nodes"));
			selectCombo.addItem(new ComboItem<>(this::selectSource, "source nodes"));
			selectCombo.addItem(new ComboItem<>(this::selectTarget, "target nodes"));
			
			label2 = style.createLabel("where the nodes match the filter:");
			
			// Initialize UI
			if(model.selectSource && model.selectTarget) {
				selectCombo.setSelectedIndex(0);
			}
			else if(model.selectSource) {
				selectCombo.setSelectedIndex(1);
			}
			else if(model.selectTarget) {
				selectCombo.setSelectedIndex(2);
			}
			else {
				selectCombo.addItem(new ComboItem<>(this::selectNone, "---")); // don't show this unless we have to
				selectCombo.setSelectedIndex(3);
			}
			
			actionCombo.addActionListener(e -> model.setAction(actionCombo.getItemAt(actionCombo.getSelectedIndex()).getValue()));
			selectCombo.addActionListener(e -> selectCombo.getItemAt(selectCombo.getSelectedIndex()).getValue().run());
			
			layout = new GroupLayout(this);
			setLayout(layout);
			updateLayout();
		}
		
		
		private void selectSource() {
			model.selectSource = true;
			model.selectTarget = false;
		}
		
		private void selectTarget() {
			model.selectSource = false;
			model.selectTarget = true;
		}
		
		private void selectSourceAndTarget() {
			model.selectSource = true;
			model.selectTarget = true;
		}
		
		private void selectNone() {
			model.selectSource = false;
			model.selectTarget = false;
		}
		
		
		@Override
		public void updateLayout() {
			Group horizontalGroup =
				layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
						.addComponent(label1)
						.addComponent(actionCombo)
						.addComponent(selectCombo));
			
			Group verticalGroup =
				layout.createSequentialGroup()
					.addGroup(layout.createBaselineGroup(false, false)
						.addComponent(label1)
						.addComponent(actionCombo)
						.addComponent(selectCombo));

			if(model.hasSubfilters()) {
				horizontalGroup
					.addGroup(layout.createSequentialGroup()
							.addComponent(label2));
				
				verticalGroup
					.addGroup(layout.createBaselineGroup(false, false)
						.addComponent(label2));
			}
			
			layout.setHorizontalGroup(horizontalGroup);
			layout.setVerticalGroup(verticalGroup);
		}
	}
}
