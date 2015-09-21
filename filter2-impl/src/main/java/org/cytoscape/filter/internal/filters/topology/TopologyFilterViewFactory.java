package org.cytoscape.filter.internal.filters.topology;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.filter.internal.view.BooleanComboBox;
import org.cytoscape.filter.internal.view.BooleanComboBox.StateChangeListener;
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.TransformerListener;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.filter.view.TransformerViewFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;

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
			view.getAtLeastComboBox().setState(model.getPredicate() == Predicate.GREATER_THAN_OR_EQUAL);
		}
		
		boolean hasChildren() {
			return model.getLength() > 0;
		}
		
		public void setGreaterThanOrEqual() {
			model.setPredicate(Predicate.GREATER_THAN_OR_EQUAL);
		}
		
		public void setLessThan() {
			model.setPredicate(Predicate.LESS_THAN);
		}
	}
	
	@SuppressWarnings("serial")
	class View extends JPanel implements TopologyFilterView {
		private JFormattedTextField thresholdField;
		private JFormattedTextField distanceField;

		private final Controller controller;
		private GroupLayout layout;
		private JLabel label1, label2, label3;
		private BooleanComboBox atLeastCombo;
		
		
		
		public View(final Controller controller) {
			this.controller = controller;
			
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
			
			label1 = new JLabel("Nodes with ");
			label2 = new JLabel(" neighbours within distance ");
			label3 = new JLabel("where the neighbours match");
			
			atLeastCombo = new BooleanComboBox("at least", "less than");
			
			atLeastCombo.addStateChangeListener(new StateChangeListener() {
				@Override
				public void stateChanged(boolean atLeast) {
					if(atLeast)
						controller.setGreaterThanOrEqual();
					else
						controller.setLessThan();
				}
			});
			
			layout = new GroupLayout(this);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
			setLayout(layout);
			updateLayout();
			
			controller.synchronize(this);
		}
		
		public void updateLayout() {
			ParallelGroup horizontalGroup = 
				layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
						.addComponent(label1)
						.addComponent(atLeastCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(layout.createSequentialGroup()
						.addComponent(thresholdField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label2)
						.addComponent(distanceField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
		
			SequentialGroup verticalGroup = 
				layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER)
						.addComponent(label1)
						.addComponent(atLeastCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(layout.createBaselineGroup(false, false)
						.addComponent(thresholdField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label2)
						.addComponent(distanceField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
			
			if(controller.hasChildren()) {
				horizontalGroup.addComponent(label3, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
				verticalGroup.addComponent(label3, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
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
		
		@Override
		public BooleanComboBox getAtLeastComboBox() {
			return atLeastCombo;
		}
	}
}
