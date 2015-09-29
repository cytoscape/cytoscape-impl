package org.cytoscape.filter.internal.filters.column;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import org.cytoscape.filter.internal.filters.column.ColumnComboBoxElement.SelectedColumnType;
import org.cytoscape.filter.internal.view.BooleanComboBox;
import org.cytoscape.filter.internal.view.BooleanComboBox.StateChangeListener;
import org.cytoscape.filter.internal.view.ComboItem;
import org.cytoscape.filter.internal.view.DocumentListenerAdapter;
import org.cytoscape.filter.internal.view.DynamicComboBoxModel;
import org.cytoscape.filter.internal.view.Matcher;
import org.cytoscape.filter.internal.view.RangeChooser;
import org.cytoscape.filter.internal.view.RangeChooserController;
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.internal.view.look.FilterPanelStyle;
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

	private final ModelMonitor modelMonitor;
	private final IconManager iconManager;
	private final FilterPanelStyle style;
	
	public ColumnFilterViewFactory(FilterPanelStyle style, ModelMonitor modelMonitor, IconManager iconManager) {
		this.modelMonitor = modelMonitor;
		this.style = style;
		this.iconManager = iconManager;
	}
	
	@Override
	public String getId() {
		return Transformers.COLUMN_FILTER;
	}

	@Override
	public JComponent createView(Transformer<?, ?> transformer) {
		ColumnFilter filter = (ColumnFilter) transformer;
		Controller controller = new Controller(filter);
		View view = new View(controller, filter);
		modelMonitor.registerColumnFilterView(view, controller);
		return view;
	}

	
	/**
	 * Controller mainly responsible for updating the range slider
	 * and column combo box in response to model change events.
	 */
	class Controller implements ColumnFilterController {

		private final ColumnFilter filter;
		private final RangeChooserController chooserController;
		
		public Controller(ColumnFilter filter) {
			this.filter = filter;
			
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

		@Override
		public void columnsChanged(ColumnFilterView view) {
			view.columnsChanged();
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
				low = min;
			if(high.compareTo(max) > 0)
				high = max;
			
			chooserController.setRange(low, high, min, max);
		}
	}
	
	
	
	@SuppressWarnings("serial")
	class View extends JPanel implements ColumnFilterView, InteractivityChangedListener {
		private final Controller controller;
		private final ColumnFilter filter;
		
		private boolean optionsExpanded;
		private boolean isInteractive;
		
		private JTextField textField;
		private JLabel arrowLabel;
		private JCheckBox caseSensitiveCheckBox;
		private JComboBox<ComboItem<Predicate>> predicateComboBox;
		private JComboBox<ColumnComboBoxElement> nameComboBox;
		private JPanel spacerPanel;
		private JPanel predicatePanel;
		
		private RangeChooser rangeChooser;
		private BooleanComboBox numericNegateComboBox;
		private BooleanComboBox booleanComboBox;
		private JLabel booleanLabel;
		private JPanel booleanPanel;
		
		private FocusListener textFieldFocusListener;
		private DocumentListener textFieldDocumentListener;
		private ActionListener caseSensitiveCheckBoxActionListener;
		private ActionListener predicateComboBoxActionListener;
		private ActionListener nameComboBoxActionListener;
		private StateChangeListener numericNegateComboBoxStateChangeListener;
		private StateChangeListener booleanComboBoxStateChangeListener;
		
		
		public View(Controller controller, ColumnFilter filter) {
			this.controller = controller;
			this.filter = filter;
			this.optionsExpanded = true;
			
			ViewUtil.configureFilterView(this);
			
			textField = style.createTextField();
			
			arrowLabel = style.createLabel(IconManager.ICON_CARET_DOWN);
			arrowLabel.setFont(iconManager.getIconFont(16.0f));
			arrowLabel.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent event) {
					handleArrowClicked();
				}
			});
			
			caseSensitiveCheckBox = style.createCheckBox("Case sensitive");
			caseSensitiveCheckBox.setOpaque(false);
			
			predicateComboBox = style.createCombo();
			predicateComboBox.addItem(new ComboItem<>(Predicate.CONTAINS, "contains"));
			predicateComboBox.addItem(new ComboItem<>(Predicate.DOES_NOT_CONTAIN, "doesn't contain"));
			predicateComboBox.addItem(new ComboItem<>(Predicate.IS, "is"));
			predicateComboBox.addItem(new ComboItem<>(Predicate.IS_NOT, "is not"));
			predicateComboBox.addItem(new ComboItem<>(Predicate.REGEX, "matches regex"));
			predicateComboBox.addItem(new ComboItem<>(Predicate.CONTAINS));
			
			List<ColumnComboBoxElement> nameComboBoxModel = modelMonitor.getColumnComboBoxModel();
			nameComboBox = style.createCombo(new DynamicComboBoxModel<ColumnComboBoxElement>(nameComboBoxModel));
			
			numericNegateComboBox = new BooleanComboBox(style, "is", "is not");
			booleanComboBox = new BooleanComboBox(style, "true", "false");
			
			booleanLabel = style.createLabel("is");
			booleanPanel = new JPanel();
			booleanPanel.setOpaque(false);
			booleanPanel.setLayout(new GridBagLayout());
			booleanPanel.add(booleanLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			booleanPanel.add(booleanComboBox, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			
			spacerPanel = new JPanel();
			spacerPanel.setOpaque(false);
			
			predicatePanel = new JPanel();
			predicatePanel.setOpaque(false);
			predicatePanel.setLayout(new GridBagLayout());
			
			rangeChooser = new RangeChooser(style, controller.chooserController);
			setLayout(new GridBagLayout());
			
			refreshUI(false, true);
		}
		
		@Override
		public void columnsChanged() {
			refreshUI(true, true);
		}
		
		
		/**
		 * Update the UI controls to reflect the state of the filter object.
		 * The nameComboBox and the filter object are kept in sync.
		 */
		private void refreshUI(boolean updateColumnModel, boolean selectColumn) {
			removeListeners();
			
			if(updateColumnModel) {
				// need to inform the nameComboBox that its model has changed
				DynamicComboBoxModel<?> model = (DynamicComboBoxModel<?>) nameComboBox.getModel();
				model.notifyChanged(0, model.getSize() - 1);
			}
			if(selectColumn) {
				String selectedColumn = filter.getColumnName();
				DynamicComboBoxModel.select(nameComboBox, 0, new Matcher<ColumnComboBoxElement>() {
					public boolean matches(ColumnComboBoxElement item) {
						return item.getName().equals(selectedColumn) && item.getTableType().equals(filter.getColumnType());
					}
				});
			}
			
			ColumnComboBoxElement column = (ColumnComboBoxElement)nameComboBox.getSelectedItem();
			Object criterion = filter.getCriterion();
			
			switch(column.getColType()) {
			case STRING:
				textField.setText((String)criterion);
				predicateComboBox.setSelectedItem(new ComboItem<>(filter.getPredicate()));
				handleStringColumnSelected();
				break;
			case NUMERIC:
				Number[] range = (Number[]) criterion;
				controller.updateRange(range);
				numericNegateComboBox.setState(filter.getPredicate() != Predicate.IS_NOT_BETWEEN);
				handleNumericColumnSelected();
				controller.chooserController.setInteractive(isInteractive, rangeChooser);
				// hack because the textboxes don't update properly on their own
				Number low = controller.chooserController.getLow();
				Number high = controller.chooserController.getHigh();
				rangeChooser.getMinimumField().setText(low.toString());
				rangeChooser.getMaximumField().setText(high.toString());
				break;
			case BOOLEAN:
				booleanComboBox.setState((boolean)criterion);
				handleBooleanColumnSelected();
				break;
			case NONE:
				handleNoColumnSelected();
				break;
			}
			caseSensitiveCheckBox.setSelected(filter.getCaseSensitive());
			
			addListeners();
		}
		
		
		private void handleColumnSelected() {
			if (nameComboBox.getSelectedIndex() == 0) {
				filter.setPredicateAndCriterion(null, null);
				handleNoColumnSelected();
				controller.chooserController.setInteractive(false, rangeChooser);
				return;
			}
			
			ColumnComboBoxElement selected = (ColumnComboBoxElement) nameComboBox.getSelectedItem();
			filter.setCaseSensitive(false);
			filter.setColumnName(selected.getName());
			setFilterDefaults(selected.getColType());
			
			Class<?> type = selected.getTableType();
			if (type == null)
				filter.type.setSelectedValue(ColumnFilter.NODES_AND_EDGES);
			else if (CyNode.class.equals(type))
				filter.type.setSelectedValue(ColumnFilter.NODES);
			else if (CyEdge.class.equals(type))
				filter.type.setSelectedValue(ColumnFilter.EDGES);
			
			refreshUI(false, false); // no need to select the column as it was just selected
		}
		
		
		private void setFilterDefaults(SelectedColumnType type) {
			if(type == null) {
				filter.setPredicateAndCriterion(null, null);
				return;
			}
			switch(type) {
			case BOOLEAN:
				filter.setPredicateAndCriterion(Predicate.IS, true);
				break;
			case NUMERIC:
				filter.setPredicateAndCriterion(Predicate.BETWEEN, null); // must be null!
				break;
			case STRING:
				filter.setPredicateAndCriterion(Predicate.CONTAINS, "");
				break;
			case NONE:
				filter.setPredicateAndCriterion(null, null);
				break;
			}
		}

		
		
		private void addListeners() {
			textField.addFocusListener(textFieldFocusListener = new FocusAdapter() {
				public void focusLost(FocusEvent event) {
					filter.setCriterion(textField.getText());
				}
			});
			textField.getDocument().addDocumentListener(textFieldDocumentListener = new DocumentListenerAdapter() {
				public void update(DocumentEvent event) {
					filter.setCriterion(textField.getText());
				}
			});
			
			caseSensitiveCheckBox.addActionListener(caseSensitiveCheckBoxActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					filter.setCaseSensitive(caseSensitiveCheckBox.isSelected());
				}
			});
			
			predicateComboBox.addActionListener(predicateComboBoxActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					ComboItem<Predicate> selected = predicateComboBox.getItemAt(predicateComboBox.getSelectedIndex());
					filter.setPredicate(selected.getValue());
				}
			});
			
			nameComboBox.addActionListener(nameComboBoxActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					handleColumnSelected();
				}
			});
			
			numericNegateComboBox.addStateChangeListener(numericNegateComboBoxStateChangeListener = new StateChangeListener() {
				public void stateChanged(boolean is) {
					filter.setPredicate(is ? Predicate.BETWEEN : Predicate.IS_NOT_BETWEEN);
				}
			});
			
			booleanComboBox.addStateChangeListener(booleanComboBoxStateChangeListener = new StateChangeListener() {
				public void stateChanged(boolean isTrue) {
					filter.setCriterion(isTrue);
				}
			});
		}
		

		private void removeListeners() {
			textField.removeFocusListener(textFieldFocusListener);
			textField.getDocument().removeDocumentListener(textFieldDocumentListener);
			caseSensitiveCheckBox.removeActionListener(caseSensitiveCheckBoxActionListener);
			predicateComboBox.removeActionListener(predicateComboBoxActionListener);
			nameComboBox.removeActionListener(nameComboBoxActionListener);
			numericNegateComboBox.removeStateChangeListener(numericNegateComboBoxStateChangeListener);
			booleanComboBox.removeStateChangeListener(booleanComboBoxStateChangeListener);
		}
		
		protected void handleArrowClicked() {
			arrowLabel.setText((optionsExpanded = !optionsExpanded) ? IconManager.ICON_CARET_DOWN : IconManager.ICON_CARET_LEFT);
			handleStringColumnSelected();
		}

		void handleNumericColumnSelected() {
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
			removeAll();
			add(nameComboBox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(spacerPanel, new GridBagConstraints(1, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			add(booleanPanel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			invalidate();
			validate();
		}
		
		private void handleNoColumnSelected() {
			removeAll();
			add(nameComboBox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(spacerPanel, new GridBagConstraints(1, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			invalidate();
			validate();
		}


		@Override
		public void handleInteractivityChanged(boolean isInteractive) {
			this.isInteractive = isInteractive;
			controller.chooserController.setInteractive(isInteractive, rangeChooser);
		}
		
	}
	
}
