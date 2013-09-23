package org.cytoscape.filter.internal.degree;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.filter.internal.ModelMonitor;
import org.cytoscape.filter.internal.degree.DegreeFilterView.EdgeTypeElement;
import org.cytoscape.filter.internal.prefuse.JRangeSlider;
import org.cytoscape.filter.internal.prefuse.JRangeSliderExtended;
import org.cytoscape.filter.internal.prefuse.NumberRangeModel;
import org.cytoscape.filter.internal.view.DynamicComboBoxModel;
import org.cytoscape.filter.internal.view.Matcher;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.filter.view.TransformerViewFactory;
import org.cytoscape.model.CyEdge.Type;

public class DegreeFilterViewFactory implements TransformerViewFactory {

	List<EdgeTypeElement> edgeTypeComboBoxModel;
	ModelMonitor modelMonitor;
	
	public DegreeFilterViewFactory(ModelMonitor modelMonitor) {
		this.modelMonitor = modelMonitor;
		
		edgeTypeComboBoxModel = new ArrayList<EdgeTypeElement>();
		edgeTypeComboBoxModel.add(new EdgeTypeElement(Type.ANY, "In + Out"));
		edgeTypeComboBoxModel.add(new EdgeTypeElement(Type.INCOMING, "In"));
		edgeTypeComboBoxModel.add(new EdgeTypeElement(Type.OUTGOING, "Out"));
	}
	
	@Override
	public String getId() {
		return Transformers.DEGREE_FILTER;
	}

	@Override
	public Component createView(Transformer<?, ?> transformer) {
		DegreeFilter filter = (DegreeFilter) transformer;
		Controller controller = new Controller(filter);
		View view = new View(controller);
		modelMonitor.registerDegreeFilterView(view, controller);
		return view;
	}

	class Controller implements DegreeFilterController {
		private DegreeFilter filter;
		private NumberRangeModel sliderModel;
		private Number[] range;

		public Controller(DegreeFilter filter) {
			this.filter = filter;
			
			int minimum = modelMonitor.getMinimumDegree();
			int maximum = modelMonitor.getMaximumDegree();
			
			if (minimum == Integer.MAX_VALUE || maximum == Integer.MIN_VALUE) {
				minimum = 0;
				maximum = 0;
			}
			
			Number lowValue;
			Number highValue;
			Object criterion = filter.getCriterion();
			if (criterion instanceof Number) {
				lowValue = (Number) criterion;
				highValue = lowValue;
			} else if (criterion instanceof Number[]) {
				Number[] range = (Number[]) criterion;
				lowValue = range[0];
				highValue = range[1];
			} else {
				lowValue = minimum;
				highValue = maximum;
			}
			
			sliderModel = new NumberRangeModel(lowValue, highValue, minimum, maximum);
			range = new Number[2];
		}

		@Override
		public NumberRangeModel getSliderModel() {
			return sliderModel;
		}

		public void sliderChanged() {
			range[0] = (Number) sliderModel.getLowValue();
			range[1] = (Number) sliderModel.getHighValue();
			filter.setCriterion(range);
		}

		public void synchronize(View view) {
			filter.setPredicate(Predicate.BETWEEN);
			
			Type edgeType = filter.getEdgeType();
			if (edgeType == null) {
				filter.setEdgeType(Type.ANY);
			}
			
			Object criterion = filter.getCriterion();
			if (criterion instanceof Number[]) {
				Number[] range = (Number[]) criterion;
				sliderModel.setLowValue(range[0]);
				sliderModel.setHighValue(range[1]);
			}
			
			DynamicComboBoxModel.select(view.edgeTypeComboBox, 0, new Matcher<EdgeTypeElement>() {
				@Override
				public boolean matches(EdgeTypeElement item) {
					return filter.getEdgeType().equals(item.type);
				}
			});
		}
	}
	
	@SuppressWarnings("serial")
	class View extends JPanel implements DegreeFilterView {
		private JComboBox edgeTypeComboBox;

		public View(final Controller controller) {
			edgeTypeComboBox = new JComboBox(new DynamicComboBoxModel<EdgeTypeElement>(edgeTypeComboBoxModel));
			
			JRangeSliderExtended slider = new JRangeSliderExtended(controller.getSliderModel(), JRangeSlider.HORIZONTAL, JRangeSlider.LEFTRIGHT_TOPBOTTOM);
			slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent event) {
					controller.sliderChanged();
				}
			});
			
			setLayout(new GridBagLayout());
			add(new JLabel("Degree"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(edgeTypeComboBox, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(slider, new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			
			controller.synchronize(this);
		}
	}
}
