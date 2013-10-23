package org.cytoscape.filter.internal.topology;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.filter.view.TransformerViewFactory;

public class TopologyFilterViewFactory implements TransformerViewFactory {

	@Override
	public String getId() {
		return Transformers.TOPOLOGY_FILTER;
	}

	@Override
	public JComponent createView(Transformer<?, ?> transformer) {
		TopologyFilter filter = (TopologyFilter) transformer;
		Controller controller = new Controller(filter);
		View view = new View(controller);
		return view;
	}

	class Controller implements TopologyFilterController {
		private TopologyFilter model;
		
		public Controller(TopologyFilter model) {
			this.model = model;
			
			if (model.getPredicate() == null) {
				model.setPredicate(Predicate.GREATER_THAN_OR_EQUAL);
			}
		}
		
		public void setThreshold(Integer threshold) {
			model.setThreshold(threshold);
		}

		public void setDistance(Integer distance) {
			model.setDistance(distance);
		}
		
		void synchronize(TopologyFilterView view) {
			view.getDistanceField().setValue(model.getDistance());
			view.getThresholdField().setValue(model.getThreshold());
		}
	}
	
	@SuppressWarnings("serial")
	class View extends JPanel implements TopologyFilterView {
		private JFormattedTextField thresholdField;
		private JFormattedTextField distanceField;

		public View(final Controller controller) {
			ViewUtil.configureFilterView(this);
			
			thresholdField = new JFormattedTextField(ViewUtil.createIntegerFormatter(0, Integer.MAX_VALUE));
			thresholdField.setHorizontalAlignment(JTextField.TRAILING);
			thresholdField.setColumns(3);
			thresholdField.addPropertyChangeListener("value", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					Integer value = (Integer) thresholdField.getValue();
					controller.setThreshold(value);
				}
			});
			
			distanceField = new JFormattedTextField(ViewUtil.createIntegerFormatter(1, Integer.MAX_VALUE));
			distanceField.setHorizontalAlignment(JTextField.TRAILING);
			distanceField.setColumns(3);
			distanceField.addPropertyChangeListener("value", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					Integer value = (Integer) distanceField.getValue();
					controller.setDistance(value);
				}
			});
			
			JLabel label1 = new JLabel("Nodes with at least ");
			JLabel label2 = new JLabel(" neighbours");
			JLabel label3 = new JLabel("within distance ");
			
			GroupLayout layout = new GroupLayout(this);
			setLayout(layout);
			
			layout.setHorizontalGroup(
				layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
						.addComponent(label1)
						.addComponent(thresholdField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label2))
					.addGroup(layout.createSequentialGroup()
						.addComponent(label3)
						.addComponent(distanceField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));
		
			layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addGroup(layout.createBaselineGroup(false, false)
						.addComponent(label1)
						.addComponent(thresholdField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label2))
					.addGroup(layout.createBaselineGroup(false, false)
						.addComponent(label3)
						.addComponent(distanceField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));
			
			controller.synchronize(this);
		}
		
		@Override
		public JFormattedTextField getThresholdField() {
			return thresholdField;
		}
		
		@Override
		public JFormattedTextField getDistanceField() {
			return distanceField;
		}
	}
}
