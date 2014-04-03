package org.cytoscape.filter.internal.topology;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.filter.internal.view.DynamicComboBoxModel;
import org.cytoscape.filter.internal.view.FilterElement;
import org.cytoscape.filter.internal.view.Matcher;
import org.cytoscape.filter.internal.view.NamedElementListener;
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.view.TransformerViewFactory;

public class TopologyTransformerViewFactory implements TransformerViewFactory, NamedElementListener<FilterElement> {

	private DynamicComboBoxModel<FilterElement> filterComboBoxModel;

	public TopologyTransformerViewFactory() {
		List<FilterElement> items = new ArrayList<FilterElement>();
		items.add(new FilterElement("(Anything)", null));
		filterComboBoxModel = new DynamicComboBoxModel<FilterElement>(items);
	}
	
	@Override
	public String getId() {
		return TopologyTransformer.ID;
	}

	@Override
	public JComponent createView(Transformer<?, ?> transformer) {
		Controller controller = new Controller((TopologyTransformer) transformer);
		View view = new View(controller);
		return view;
	}

	@Override
	public void handleElementAdded(FilterElement element) {
		if (element.filter == null) {
			return;
		}
		filterComboBoxModel.add(element);
	}

	@Override
	public void handleElementRemoved(FilterElement element) {
		filterComboBoxModel.remove(element);
	}
	
	class Controller {
		private TopologyTransformer model;
		
		public Controller(TopologyTransformer model) {
			this.model = model;
		}
		
		public void setThreshold(Integer threshold) {
			model.setThreshold(threshold);
		}

		public void setDistance(Integer distance) {
			model.setDistance(distance);
		}

		public void setFilterName(String name) {
			model.setFilterName(name);
		}
		
		void synchronize(TopologyTransformerView view) {
			view.getDistanceField().setValue(model.getDistance());
			view.getThresholdField().setValue(model.getThreshold());
			
			final String filterName = model.getFilterName();
			JComboBox filterComboBox = view.getGetFilterComboBox();
			DynamicComboBoxModel.select(filterComboBox, 0, new Matcher<FilterElement>() {
				@Override
				public boolean matches(FilterElement item) {
					return !item.isPlaceholder() && item.name.equals(filterName);
				}
			});
		}
	}
	
	@SuppressWarnings("serial")
	class View extends JPanel implements TopologyTransformerView {
		private JFormattedTextField thresholdField;
		private JFormattedTextField distanceField;
		private JComboBox filterComboBox;

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
			
			filterComboBox = new JComboBox(filterComboBoxModel);
			filterComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					FilterElement selected = (FilterElement) filterComboBox.getSelectedItem();
					if (selected.isPlaceholder()) {
						controller.setFilterName(null);
					} else {
						controller.setFilterName(selected.name);
					}
				}
			});
			
			JLabel label1 = new JLabel("Node neighbourhoods with at least ");
			JLabel label2 = new JLabel(" neighbours");
			JLabel label3 = new JLabel("within distance ");
			JLabel label4 = new JLabel("where nodes match ");
			
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
						.addComponent(distanceField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(layout.createSequentialGroup()
						.addComponent(label4)
						.addComponent(filterComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));
		
			layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addGroup(layout.createBaselineGroup(false, false)
						.addComponent(label1)
						.addComponent(thresholdField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label2))
					.addGroup(layout.createBaselineGroup(false, false)
						.addComponent(label3)
						.addComponent(distanceField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(layout.createBaselineGroup(false, false)
						.addComponent(label4)
						.addComponent(filterComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));
			
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
		
		@Override
		public JComboBox getGetFilterComboBox() {
			return filterComboBox;
		}
	}
}
