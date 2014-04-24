package org.cytoscape.filter.internal.degree;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.filter.internal.ModelMonitor;
import org.cytoscape.filter.internal.degree.DegreeFilterView.EdgeTypeElement;
import org.cytoscape.filter.internal.view.DynamicComboBoxModel;
import org.cytoscape.filter.internal.view.Matcher;
import org.cytoscape.filter.internal.view.RangeChooser;
import org.cytoscape.filter.internal.view.RangeChooserController;
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.filter.view.InteractivityChangedListener;
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
	public JComponent createView(Transformer<?, ?> transformer) {
		DegreeFilter filter = (DegreeFilter) transformer;
		Controller controller = new Controller(filter);
		View view = new View(controller);
		modelMonitor.registerDegreeFilterView(view, controller);
		return view;
	}

	class Controller implements DegreeFilterController {
		private DegreeFilter filter;
		private RangeChooserController chooserController;
		private boolean isInteractive;

		public Controller(final DegreeFilter filter) {
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
			
			final Number[] range = new Number[2];
			chooserController = new RangeChooserController() {
				@Override
				protected void handleRangeChanged(Number low, Number high) {
					range[0] = low;
					range[1] = high;
					filter.setCriterion(range);
				}
			};
			chooserController.setRange(lowValue, highValue, minimum, maximum);
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
				chooserController.setSelection(range[0], range[1]);
			}
			if (criterion == null) {
				Number minimum = chooserController.getMinimum();
				Number maximum = chooserController.getMaximum();
				view.getRangeChooser().getMinimumField().setText(minimum.toString());
				view.getRangeChooser().getMaximumField().setText(maximum.toString());
			}
			
			DynamicComboBoxModel.select(view.edgeTypeComboBox, 0, new Matcher<EdgeTypeElement>() {
				@Override
				public boolean matches(EdgeTypeElement item) {
					return filter.getEdgeType().equals(item.type);
				}
			});
			
			setInteractive(isInteractive, view);
		}

		public void setInteractive(boolean isInteractive, View view) {
			this.isInteractive = isInteractive;
			chooserController.setInteractive(isInteractive, view.chooser);
		}
		
		@Override
		public RangeChooserController getRangeChooserController() {
			return chooserController;
		}
	}
	
	@SuppressWarnings("serial")
	class View extends JPanel implements DegreeFilterView, InteractivityChangedListener {
		private JComboBox edgeTypeComboBox;
		private Controller controller;
		private RangeChooser chooser;

		public View(final Controller controller) {
			this.controller = controller;
			
			ViewUtil.configureFilterView(this);
			
			edgeTypeComboBox = new JComboBox(new DynamicComboBoxModel<EdgeTypeElement>(edgeTypeComboBoxModel));
			
			chooser = new RangeChooser(controller.chooserController);
			
			setLayout(new GridBagLayout());
			add(new JLabel("Degree"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(edgeTypeComboBox, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(chooser, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 3, 3), 0, 0));
			
			controller.synchronize(this);
		}
		
		@Override
		public void handleInteractivityChanged(boolean isInteractive) {
			controller.setInteractive(isInteractive, this);
		}
		
		RangeChooser getRangeChooser() {
			return chooser;
		}
	}
}
