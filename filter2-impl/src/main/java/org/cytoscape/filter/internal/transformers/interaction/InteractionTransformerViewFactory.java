package org.cytoscape.filter.internal.transformers.interaction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
		Controller controller = new Controller(model);
		return new View(controller);
	}

	static class Controller {
		private InteractionTransformer model;

		public Controller(InteractionTransformer model) {
			this.model = model;
		}

		public void synchronize(View view) {
			view.getSourceCheckBox().setSelected(model.selectSource);
			view.getTargetCheckBox().setSelected(model.selectTarget);
		}
	}
	
	@SuppressWarnings("serial")
	class View extends JPanel {
		private JCheckBox sourceCheckBox;
		private JCheckBox targetCheckBox;

		JCheckBox getSourceCheckBox() {
			return sourceCheckBox;
		}
		
		JCheckBox getTargetCheckBox() {
			return targetCheckBox;
		}
		
		public View(final Controller controller) {
			ViewUtil.configureFilterView(this);
			
			JLabel label1 = style.createLabel("Add");
			sourceCheckBox = style.createCheckBox("source");
			sourceCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					controller.model.selectSource = sourceCheckBox.isSelected();
				}
			});
			sourceCheckBox.setOpaque(false);
			
			JLabel label2 = style.createLabel("and/or");
			targetCheckBox = style.createCheckBox("target");
			targetCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					controller.model.selectTarget = targetCheckBox.isSelected();
				}
			});
			targetCheckBox.setOpaque(false);
			
			JLabel label3 = style.createLabel("nodes from upstream edges.");
			
			GroupLayout layout = new GroupLayout(this);
			layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addComponent(label1)
					.addComponent(sourceCheckBox)
					.addComponent(label2)
					.addComponent(targetCheckBox))
				.addGroup(layout.createSequentialGroup()
					.addComponent(label3)));
			
			layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createBaselineGroup(false, false)
					.addComponent(label1)
					.addComponent(sourceCheckBox)
					.addComponent(label2)
					.addComponent(targetCheckBox))
				.addGroup(layout.createBaselineGroup(false, false)
					.addComponent(label3)));
			
			setLayout(layout);
			
			controller.synchronize(this);
		}
	}
}
