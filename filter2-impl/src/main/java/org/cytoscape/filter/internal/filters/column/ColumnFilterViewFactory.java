package org.cytoscape.filter.internal.filters.column;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.filter.internal.ModelMonitor;
import org.cytoscape.filter.internal.filters.column.ColumnFilterView.ColumnComboBoxElement;
import org.cytoscape.filter.internal.filters.column.ColumnFilterView.PredicateElement;
import org.cytoscape.filter.internal.view.BooleanComboBox;
import org.cytoscape.filter.internal.view.BooleanComboBox.StateChangeListener;
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
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.IconManager;

public class ColumnFilterViewFactory implements TransformerViewFactory {

	ModelMonitor modelMonitor;
	List<ColumnComboBoxElement> nameComboBoxModel;
	List<PredicateElement> predicateComboBoxModel;
	private IconManager iconManager;
	
	public ColumnFilterViewFactory(ModelMonitor modelMonitor, IconManager iconManager) {
		this.modelMonitor = modelMonitor;
		this.iconManager = iconManager;
		
		nameComboBoxModel = modelMonitor.getColumnComboBoxModel();
		
		predicateComboBoxModel = new ArrayList<PredicateElement>();
		predicateComboBoxModel.add(new PredicateElement(Predicate.CONTAINS, "contains"));
		predicateComboBoxModel.add(new PredicateElement(Predicate.DOES_NOT_CONTAIN, "doesn't contain"));
		predicateComboBoxModel.add(new PredicateElement(Predicate.IS, "is"));
		predicateComboBoxModel.add(new PredicateElement(Predicate.IS_NOT, "is not"));
		predicateComboBoxModel.add(new PredicateElement(Predicate.REGEX, "matches regex"));
	}
	
	@Override
	public String getId() {
		return Transformers.COLUMN_FILTER;
	}

	@Override
	public JComponent createView(Transformer<?, ?> transformer) {
		ColumnFilter filter = (ColumnFilter) transformer;
		Controller controller = new Controller(filter);
		View view = new View(controller, iconManager);
		modelMonitor.registerColumnFilterView(view, controller);
		return view;
	}

	class Controller implements ColumnFilterController {
		private ColumnFilter filter;
		private RangeChooserController chooserController;
		private boolean listenersEnabled;
		
		public Controller(final ColumnFilter filter) {
			this.filter = filter;
			listenersEnabled = true;

			if (filter.getPredicate() == null) {
				filter.setPredicate(Predicate.CONTAINS);
				filter.setCriterion("");
			}
			
			chooserController = new RangeChooserController() {
				@Override
				public void handleRangeChanged(Number low, Number high) {
					Number[] range = new Number[2];
					range[0] = low;
					range[1] = high;
					filter.setCriterion(range);
				}
			};
		}
		
		@Override
		public ColumnFilter getFilter() {
			return filter;
		}

		public void setCaseSensitive(View view, boolean caseSensitive) {
			filter.setCaseSensitive(caseSensitive);
		}

		public void setCriterion(View view, String text) {
			if (text != null && text.equals(filter.getCriterion())) {
				return;
			}
			filter.setCriterion(text);
		}
		
		public void setBooleanCriterion(View view, Boolean value) {
			if (value != null && value.equals(filter.getCriterion())) {
				return;
			}
			filter.setCriterion(value);
		}
		
		public void setColumnName(String name) {
			filter.setColumnName(name);
		}
		
		private void updateRange(Number[] criterion) {
			String name = filter.getColumnName();
			Class<? extends CyIdentifiable> type = filter.getColumnType();
			if (name == null || type == null) {
				chooserController.setRange(0, 0, 0, 0);
				return;
			}
			Number[] range = modelMonitor.getColumnRange(name, type);
			if (range == null) {
				chooserController.setRange(0, 0, 0, 0);
				return;
			}
			
			if(range[0] instanceof Integer || range[0] instanceof Long) {
				long min = range[0].longValue();
				long max = range[1].longValue();
				long low  = criterion == null ? min : criterion[0].longValue();
				long high = criterion == null ? max : criterion[1].longValue();
				setRange(low, high, min, max);
			}
			else {
				double min = range[0].doubleValue();
				double max = range[1].doubleValue();
				double low  = criterion == null ? min : criterion[0].doubleValue();
				double high = criterion == null ? max : criterion[1].doubleValue();
				setRange(low, high, min, max);
			}
		}
		
		
		@Override
		public void setSliderBounds(long min, long max) {
			long low  = chooserController.getLow().longValue();
			long high = chooserController.getHigh().longValue();
			setRange(low, high, min, max);
		}
		
		@Override
		public void setSliderBounds(double min, double max) {
			double low  = chooserController.getLow().doubleValue();
			double high = chooserController.getHigh().doubleValue();
			setRange(low, high, min, max);
		}
				
		/**
		 * Sets the slider range, makes sure min and max encompass the range..
		 */
		private <N extends Number & Comparable<N>> void setRange(N low, N high, N min, N max) {
			// Clip low and high to be within the range, need to do this here because NumberRangeModel doesn't do it
			if(low.compareTo(min) < 0)
				min = low;
			if(high.compareTo(max) > 0)
				max = high;
			
			chooserController.setRange(low, high, min, max);
		}
		
		public void setMatchType(Class<?> type) {
			if (type == null) {
				filter.type.setSelectedValue(ColumnFilter.NODES_AND_EDGES);
			}
			if (CyNode.class.equals(type)) {
				filter.type.setSelectedValue(ColumnFilter.NODES);
			}
			if (CyEdge.class.equals(type)) {
				filter.type.setSelectedValue(ColumnFilter.EDGES);
			}
		}
		
		public void setPredicate(View view, Predicate predicate) {
			filter.setPredicate(predicate);
		}

		public ColumnFilter getModel() {
			return filter;
		}

		// Updates the filter object to display what's in the view, then shows the view
		void handleColumnSelected(View view, JComboBox<?> nameComboBox) {
			RangeChooser rangeChooser = view.rangeChooser;
			if (nameComboBox.getSelectedIndex() == 0) {
				filter.setCriterion(null);
				filter.setPredicate(null);
				view.handleNoColumnSelected();
				chooserController.setInteractive(false, rangeChooser);
				return;
			}

			ColumnComboBoxElement selected = (ColumnComboBoxElement) nameComboBox.getSelectedItem();
			setColumnName(selected.name);
			setMatchType(selected.columnType);
			
			// update the filter to match what's in the view, then show the view
			if (modelMonitor.checkType(selected.name, selected.columnType, String.class)) {
				Predicate predicate = ((PredicateElement)view.getPredicateComboBox().getSelectedItem()).predicate;
				filter.setPredicateAndCriterion(predicate, view.getField().getText());
				view.handleStringColumnSelected();
				chooserController.setInteractive(false, rangeChooser);
			} else if (modelMonitor.checkType(selected.name, selected.columnType, Boolean.class)) {
				filter.setPredicateAndCriterion(Predicate.IS, view.getBooleanComboBox().booleanValue());
				view.handleBooleanColumnSelected();
				chooserController.setInteractive(false, rangeChooser);
			} else { // numeric
				updateRange(null);
				boolean between = view.getNumericNegateComboBox().booleanValue();
				Predicate predicate = between ? Predicate.BETWEEN : Predicate.IS_NOT_BETWEEN;
				Number[] criterion = { chooserController.getLow(), chooserController.getHigh() };
				filter.setPredicateAndCriterion(predicate, criterion);
				chooserController.setInteractive(view.isInteractive, rangeChooser);
				view.handleNumericColumnSelected();
				
				// hack because the textboxes don't update properly on their own
				Number low = chooserController.getLow();
				Number high = chooserController.getHigh();
				view.getRangeChooser().getMinimumField().setText(low.toString());
				view.getRangeChooser().getMaximumField().setText(high.toString());
			}
		}
		
		private void initializeView(View view) {
			Object criterion = filter.getCriterion();
			
			// set the view based on the model
			if (criterion instanceof String) {
				String str = (String)criterion;
				view.getField().setText(str);
				DynamicComboBoxModel.select(view.getPredicateComboBox(), -1, new Matcher<PredicateElement>() {
					@Override
					public boolean matches(PredicateElement item) {
						return item.predicate.equals(filter.getPredicate());
					}
				});
				view.handleStringColumnSelected();
			}
			else if (criterion instanceof Number[]) {
				Number[] range = (Number[]) criterion;
				updateRange(range);
				view.getNumericNegateComboBox().setState(filter.getPredicate() != Predicate.IS_NOT_BETWEEN);
				view.handleNumericColumnSelected();
				chooserController.setInteractive(view.isInteractive, view.rangeChooser);
			}
			else if (criterion instanceof Boolean) {
				BooleanComboBox comboBox = view.getBooleanComboBox();
				comboBox.setState((boolean)criterion);
				view.handleBooleanColumnSelected();
			}
			else {
				view.handleNoColumnSelected();
			}
			
			view.getCaseSensitiveCheckBox().setSelected(filter.getCaseSensitive());
			columnModelChanged(view, true);
		}
		
		@Override
		public void synchronize(ColumnFilterView view) {
			columnModelChanged(view, false);
		}
		
		private void columnModelChanged(ColumnFilterView view, boolean initializing) {
			// update the view combo box to match the current columns
			JComboBox<?> nameComboBox = view.getNameComboBox();
			
			final String selectedColumn = filter.getColumnName();
			listenersEnabled = false;
			try {
				DynamicComboBoxModel<?> model = (DynamicComboBoxModel<?>) nameComboBox.getModel();
				model.notifyChanged(0, model.getSize() - 1);
				
				
			} finally {
				listenersEnabled = true;
			}
			
			listenersEnabled = !initializing;
			try {
				DynamicComboBoxModel.select(nameComboBox, 0, new Matcher<ColumnComboBoxElement>() {
					@Override
					public boolean matches(ColumnComboBoxElement item) {
						return item.name.equals(selectedColumn) && item.columnType.equals(filter.getColumnType());
					}
				});
			} finally {
				listenersEnabled = true;
			}
		}

		public void setInteractive(boolean isInteractive, View view) {
			view.isInteractive = isInteractive;
			if(view.selectedColumn == SelectedColumnType.NUMERIC) {
				chooserController.setInteractive(isInteractive, view.rangeChooser);
			}
		}
	}
	
	@SuppressWarnings("serial")
	class View extends JPanel implements ColumnFilterView, InteractivityChangedListener {
		boolean optionsExpanded;
		private JTextField textField;
		private JLabel arrowLabel;
		private JCheckBox caseSensitiveCheckBox;
		private JComboBox predicateComboBox;
		private JComboBox nameComboBox;
		private JPanel spacerPanel;
		private JPanel predicatePanel;
		private boolean isInteractive;
		private SelectedColumnType selectedColumn;
		private Controller controller;
		private RangeChooser rangeChooser;
		private BooleanComboBox numericNegateComboBox;
		private BooleanComboBox booleanComboBox;
		private JLabel booleanLabel;
		private JPanel booleanPanel;
		
		public View(final Controller controller, IconManager iconManager) {
			this.controller = controller;
			selectedColumn = SelectedColumnType.NONE;
			optionsExpanded = true;
			
			ViewUtil.configureFilterView(this);
			
			textField = new JTextField();
			textField.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent event) {
					controller.setCriterion(View.this, textField.getText());
				}
				
				@Override
				public void focusGained(FocusEvent event) {
				}
			});
			textField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent event) {
					handleInteractiveUpdate();
				}
				
				@Override
				public void insertUpdate(DocumentEvent event) {
					handleInteractiveUpdate();
				}
				
				@Override
				public void changedUpdate(DocumentEvent event) {
					handleInteractiveUpdate();
				}
			});
			
			arrowLabel = new JLabel(IconManager.ICON_CARET_DOWN);
			Font arrowFont = iconManager.getIconFont(16.0f);
			arrowLabel.setFont(arrowFont);
			arrowLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent event) {
					handleArrowClicked();
				}
			});
			
			caseSensitiveCheckBox = new JCheckBox("Case sensitive");
			caseSensitiveCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					controller.setCaseSensitive(View.this, caseSensitiveCheckBox.isSelected());
				}
			});
			caseSensitiveCheckBox.setOpaque(false);
			
			predicateComboBox = new JComboBox(new DynamicComboBoxModel<PredicateElement>(predicateComboBoxModel));
			predicateComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					PredicateElement selected = (PredicateElement) predicateComboBox.getSelectedItem();
					if (selected == null) {
						return;
					}
					controller.setPredicate(View.this, selected.predicate);
				}
			});
			
			nameComboBox = new JComboBox(new DynamicComboBoxModel<ColumnComboBoxElement>(nameComboBoxModel));
			nameComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					if (!controller.listenersEnabled) {
						return;
					}
					controller.handleColumnSelected(View.this, nameComboBox);
				}
			});
			
			numericNegateComboBox = new BooleanComboBox("is", "is not");
			numericNegateComboBox.addStateChangeListener(new StateChangeListener() {
				@Override
				public void stateChanged(boolean is) {
					controller.setPredicate(View.this, is ? Predicate.BETWEEN : Predicate.IS_NOT_BETWEEN);
				}
			});
			
			booleanComboBox = new BooleanComboBox("true", "false");
			booleanComboBox.addStateChangeListener(new StateChangeListener() {
				@Override
				public void stateChanged(boolean isTrue) {
					controller.setBooleanCriterion(View.this, isTrue);
				}
			});
			
			booleanLabel = new JLabel("is");
			booleanPanel = new JPanel();
			booleanPanel.setOpaque(false);
			booleanPanel.setLayout(new GridBagLayout());
			booleanPanel.add(booleanLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			booleanPanel.add(booleanComboBox, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			
			spacerPanel = new JPanel();
			spacerPanel.setOpaque(false);
			
			predicatePanel = new JPanel();
			predicatePanel.setOpaque(false);
			predicatePanel.setLayout(new GridBagLayout());
			
			rangeChooser = new RangeChooser(controller.chooserController);
			
			setLayout(new GridBagLayout());
			controller.initializeView(this);
		}

		void handleInteractiveUpdate() {
			String text = textField.getText();
			if (text == null || text.length() == 0) {
				return;
			}
			controller.setCriterion(View.this, text);
		}

		public JLabel getArrowLabel() {
			return arrowLabel;
		}

		protected void handleArrowClicked() {
			optionsExpanded = !optionsExpanded;
			if (optionsExpanded) {
				arrowLabel.setText(IconManager.ICON_CARET_DOWN);
			} else {
				arrowLabel.setText(IconManager.ICON_CARET_LEFT);
			}
			handleStringColumnSelected();
		}

		void handleNumericColumnSelected() {
			selectedColumn = SelectedColumnType.NUMERIC;
			removeAll();
			add(nameComboBox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(numericNegateComboBox, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(rangeChooser, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 3, 3), 0, 0));
			controller.chooserController.setInteractive(isInteractive, rangeChooser);
			revalidate();
			validate();
			repaint();
		}

		void handleStringColumnSelected() {
			selectedColumn = SelectedColumnType.STRING;
			removeAll();
			if (optionsExpanded) {
				predicatePanel.removeAll();
				predicatePanel.add(nameComboBox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				predicatePanel.add(predicateComboBox, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				predicatePanel.add(spacerPanel, new GridBagConstraints(2, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				predicatePanel.add(arrowLabel, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0, 0));
				
				add(predicatePanel, new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				add(textField, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				add(caseSensitiveCheckBox, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			} else {
				predicatePanel.removeAll();
				predicatePanel.add(nameComboBox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				predicatePanel.add(spacerPanel, new GridBagConstraints(1, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				predicatePanel.add(arrowLabel, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0, 0));
				predicatePanel.add(textField, new GridBagConstraints(0, 1, 3, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				
				add(predicatePanel, new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			}
			predicatePanel.invalidate();
			validate();
		}

		void handleBooleanColumnSelected() {
			selectedColumn = SelectedColumnType.BOOLEAN;
			removeAll();
			add(nameComboBox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(spacerPanel, new GridBagConstraints(1, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			add(booleanPanel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			invalidate();
			validate();
		}
		
		private void handleNoColumnSelected() {
			selectedColumn = SelectedColumnType.NONE;
			removeAll();
			add(nameComboBox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(spacerPanel, new GridBagConstraints(1, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			invalidate();
			validate();
		}
		
		@Override
		public JCheckBox getCaseSensitiveCheckBox() {
			return caseSensitiveCheckBox;
		}
		
		@Override
		public JTextField getField() {
			return textField;
		}
		
		@Override
		public JComboBox getNameComboBox() {
			return nameComboBox;
		}
		
		@Override
		public JComboBox getPredicateComboBox() {
			return predicateComboBox;
		}
		
		@Override
		public BooleanComboBox getBooleanComboBox() {
			return booleanComboBox;
		}
		
		@Override
		public void handleInteractivityChanged(boolean isInteractive) {
			controller.setInteractive(isInteractive, this);
		}
		
		@Override
		public RangeChooser getRangeChooser() {
			return rangeChooser;
		}

		@Override
		public BooleanComboBox getNumericNegateComboBox() {
			return numericNegateComboBox;
		}
	}
	
	private enum SelectedColumnType {
		NONE,
		NUMERIC,
		STRING,
		BOOLEAN,
	}
}
