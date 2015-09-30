package org.cytoscape.filter.internal.filters.degree;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.cytoscape.filter.internal.ModelMonitor;
import org.cytoscape.filter.internal.range.RangeChooser;
import org.cytoscape.filter.internal.range.RangeChooserController;
import org.cytoscape.filter.internal.range.RangeListener;
import org.cytoscape.filter.internal.view.BooleanComboBox;
import org.cytoscape.filter.internal.view.BooleanComboBox.StateChangeListener;
import org.cytoscape.filter.internal.view.ComboItem;
import org.cytoscape.filter.internal.view.DynamicComboBoxModel;
import org.cytoscape.filter.internal.view.Matcher;
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.internal.view.look.FilterPanelStyle;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.filter.view.InteractivityChangedListener;
import org.cytoscape.filter.view.TransformerViewFactory;
import org.cytoscape.model.CyEdge.Type;

public class DegreeFilterViewFactory implements TransformerViewFactory {

	private final List<ComboItem<Type>> edgeTypeComboBoxModel;
	private final ModelMonitor modelMonitor;
	
	private final FilterPanelStyle style;
	
	public DegreeFilterViewFactory(FilterPanelStyle style, ModelMonitor modelMonitor) {
		this.modelMonitor = modelMonitor;
		this.style = style;
		
		edgeTypeComboBoxModel = new ArrayList<ComboItem<Type>>();
		edgeTypeComboBoxModel.add(new ComboItem<>(Type.ANY, "In + Out"));
		edgeTypeComboBoxModel.add(new ComboItem<>(Type.INCOMING, "In"));
		edgeTypeComboBoxModel.add(new ComboItem<>(Type.OUTGOING, "Out"));
	}
	
	@Override
	public String getId() {
		return Transformers.DEGREE_FILTER;
	}

	@Override
	public JComponent createView(Transformer<?, ?> transformer) {
		DegreeFilter filter = (DegreeFilter) transformer;
		filter.setEdgeType(Type.ANY);
		Controller controller = new Controller(filter);
		View view = new View(controller);
		modelMonitor.registerDegreeFilterView(view, controller);
		return view;
	}

	class Controller implements DegreeFilterController {
		private DegreeFilter filter;
		private RangeChooserController<Integer> chooserController;
		private boolean isInteractive;

		public Controller(final DegreeFilter filter) {
			this.filter = filter;
			
			DegreeRange degreeRange = modelMonitor.getDegreeRange();
			DegreeRange.Pair pair = degreeRange.getRange(filter.getEdgeType());
			
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
				lowValue = pair.getLow();
				highValue = pair.getHigh();
			}
			
			chooserController = RangeChooserController.forInteger(style, new RangeListener<Integer>() {
				public void rangeChanged(Integer low, Integer high) {
					Number[] range = { low, high };
					filter.setCriterion(range);
				}
			});
			
			chooserController.reset(lowValue.intValue(), highValue.intValue(), pair.getLow(), pair.getHigh());
		}

		public void synchronize(View view) {
			if(filter.getPredicate() == null)
				filter.setPredicate(Predicate.BETWEEN);
			BooleanComboBox combo = view.getIsOrIsNotCombo();
			combo.setState(filter.getPredicate() == Predicate.BETWEEN);
			
			Type edgeType = filter.getEdgeType();
			if (edgeType == null) {
				filter.setEdgeType(Type.ANY);
			}
			
			Object criterion = filter.getCriterion();
			if (criterion instanceof Number[]) {
				DegreeRange degreeRange = modelMonitor.getDegreeRange();
				DegreeRange.Pair pair = degreeRange.getRange(filter.getEdgeType());
				Number[] range = (Number[]) criterion;
				chooserController.reset(range[0].intValue(), range[1].intValue(), pair.getLow(), pair.getHigh());
			}
			
			// MKTODO below is the hack, should fix this
//			Number low  = chooserController.getLow().longValue();
//			Number high = chooserController.getHigh().longValue();
//			view.getRangeChooser().getLowField().setText(low.toString());
//			view.getRangeChooser().getHighField().setText(high.toString());
			
			DynamicComboBoxModel.select(view.edgeTypeComboBox, 0, new Matcher<ComboItem<Type>>() {
				@Override
				public boolean matches(ComboItem<Type> item) {
					return filter.getEdgeType().equals(item.getValue());
				}
			});
			
			setInteractive(isInteractive, view);
		}

		public void setIsOrIsNot(boolean is) {
			filter.setPredicate(is ? Predicate.BETWEEN : Predicate.IS_NOT_BETWEEN);
		}
		
		public void setInteractive(boolean isInteractive, View view) {
			this.isInteractive = isInteractive;
			chooserController.setInteractive(isInteractive);
		}

		@Override
		public void setDegreeBounds(DegreeRange range) {
			DegreeRange.Pair pair = range.getRange(filter.getEdgeType());
			int min = pair.getLow();
			int max = pair.getHigh();
			int low  = chooserController.getLow().intValue();
			int high = chooserController.getHigh().intValue();
			
			chooserController.reset(low, high, min, max);
		}
		
		public void setEdgeType(Type type) {
			DegreeRange range = modelMonitor.getDegreeRange();
			filter.setEdgeType(type);
			setDegreeBounds(range);
		}
		
	}
	
	@SuppressWarnings("serial")
	class View extends JPanel implements DegreeFilterView, InteractivityChangedListener {
		private JComboBox<ComboItem<Type>> edgeTypeComboBox;
		private Controller controller;
		private RangeChooser<Integer> chooser;
		private BooleanComboBox isOrIsNotCombo;
		
		public View(final Controller controller) {
			this.controller = controller;
			
			ViewUtil.configureFilterView(this);
			
			edgeTypeComboBox = style.createCombo(new DynamicComboBoxModel<ComboItem<Type>>(edgeTypeComboBoxModel));
			
			edgeTypeComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ComboItem<Type> type = edgeTypeComboBox.getItemAt(edgeTypeComboBox.getSelectedIndex());
					controller.setEdgeType(type.getValue());
				}
			});
			
			isOrIsNotCombo = new BooleanComboBox(style, "is", "is not");
			isOrIsNotCombo.addStateChangeListener(new StateChangeListener() {
				@Override
				public void stateChanged(boolean is) {
					controller.setIsOrIsNot(is);
				}
			});
			
			chooser = controller.chooserController.getRangeChooser();
			
			setLayout(new GridBagLayout());
			add(style.createLabel("Degree"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(edgeTypeComboBox, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(isOrIsNotCombo, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(chooser, new GridBagConstraints(0, 1, 3, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 3, 3), 0, 0));
			
			controller.synchronize(this);
		}
		
		@Override
		public void handleInteractivityChanged(boolean isInteractive) {
			controller.setInteractive(isInteractive, this);
		}
		
		RangeChooser<Integer> getRangeChooser() {
			return chooser;
		}
		
		public BooleanComboBox getIsOrIsNotCombo() {
			return isOrIsNotCombo;
		}
	}
}
