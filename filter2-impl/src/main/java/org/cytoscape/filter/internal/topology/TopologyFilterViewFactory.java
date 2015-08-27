package org.cytoscape.filter.internal.topology;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.TransformerListener;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.filter.view.TransformerViewFactory;

public class TopologyFilterViewFactory implements TransformerViewFactory {

	public static Properties getServiceProperties() {
		Properties props = new Properties();
		props.setProperty("addButtonTooltip", "Add neighbour condition...");
		return props;
	}
	
	@Override
	public String getId() {
		return Transformers.TOPOLOGY_FILTER;
	}

	@Override
	public JComponent createView(Transformer<?, ?> transformer) {
		TopologyFilter filter = (TopologyFilter) transformer;
		Controller controller = new Controller(filter);
		View view = new View(controller);
		filter.addListener(new UpdateLayoutListener(view, filter));
		return view;
	}
	
	
	class UpdateLayoutListener implements TransformerListener {
		private final TopologyFilter model;
		private final View view;
		private int savedLength;
		
		public UpdateLayoutListener(View view, TopologyFilter model) {
			this.view = view;
			this.model = model;
			this.savedLength = model.getLength();
		}
		
		@Override
		public synchronized void handleSettingsChanged() {
			if(savedLength != model.getLength()) {
				view.updateLayout();
			}
			savedLength = model.getLength();
		}
	}

	class Controller implements TopologyFilterController {
		private final TopologyFilter model;
		
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
		
		boolean hasChildren() {
			return model.getLength() > 0;
		}
	}
	
	@SuppressWarnings("serial")
	class View extends JPanel implements TopologyFilterView {
		private JFormattedTextField thresholdField;
		private JFormattedTextField distanceField;

		private final Controller controller;
		private GroupLayout layout;
		private JLabel label1, label2, label3, label4;
		
		
		public View(final Controller controller) {
			this.controller = controller;
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
			
			label1 = new JLabel("Nodes with at least ");
			label2 = new JLabel(" neighbours");
			label3 = new JLabel("within distance ");
			label4 = new JLabel("where the neighbours match...");
			
			layout = new GroupLayout(this);
			setLayout(layout);
			updateLayout();
			
			controller.synchronize(this);
		}
		
		public void updateLayout() {
			ParallelGroup horizontalGroup = 
				layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
						.addComponent(label1)
						.addComponent(thresholdField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label2))
					.addGroup(layout.createSequentialGroup()
						.addComponent(label3)
						.addComponent(distanceField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
		
			SequentialGroup verticalGroup = 
				layout.createSequentialGroup()
					.addGroup(layout.createBaselineGroup(false, false)
						.addComponent(label1)
						.addComponent(thresholdField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label2))
					.addGroup(layout.createBaselineGroup(false, false)
						.addComponent(label3)
						.addComponent(distanceField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
			
			if(controller.hasChildren()) {
				horizontalGroup.addComponent(label4, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
				verticalGroup.addComponent(label4, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
			}
			
			layout.setHorizontalGroup(horizontalGroup);
			layout.setVerticalGroup(verticalGroup);
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
