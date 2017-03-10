package org.cytoscape.filter.internal.filters.column;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.filter.internal.ModelMonitor;
import org.cytoscape.filter.internal.filters.column.ColumnElement.SelectedColumnType;
import org.cytoscape.filter.internal.range.RangeChooser;
import org.cytoscape.filter.internal.range.RangeChooserController;
import org.cytoscape.filter.internal.range.RangeListener;
import org.cytoscape.filter.internal.view.BooleanComboBox;
import org.cytoscape.filter.internal.view.BooleanComboBox.StateChangeListener;
import org.cytoscape.filter.internal.view.ComboItem;
import org.cytoscape.filter.internal.view.DocumentListenerAdapter;
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
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Filters 2 Impl (filter2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class ColumnFilterViewFactory implements TransformerViewFactory {

	private final ModelMonitor modelMonitor;
	private final FilterPanelStyle style;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ColumnFilterViewFactory(FilterPanelStyle style, ModelMonitor modelMonitor, final CyServiceRegistrar serviceRegistrar) {
		this.modelMonitor = modelMonitor;
		this.style = style;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public String getId() {
		return Transformers.COLUMN_FILTER;
	}

	@Override
	public JComponent createView(Transformer<?, ?> transformer) {
		ColumnFilter filter = (ColumnFilter) transformer;
		filter.setCaseSensitive(false); // case sensitivity no longer supported
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
		private final RangeChooserController<Integer> intChooserController;
		private final RangeChooserController<Double> doubleChooserController;
		
		public Controller(ColumnFilter filter) {
			this.filter = filter;
			
			intChooserController = RangeChooserController.forInteger(style, new RangeListener<Integer>() {
				public void rangeChanged(Integer low, Integer high) {
					Number[] range = { low, high };
					filter.setCriterion(range);
				}
			});
			doubleChooserController = RangeChooserController.forDouble(style, new RangeListener<Double>() {
				public void rangeChanged(Double low, Double high) {
					Number[] range = { low, high };
					filter.setCriterion(range);
				}
			});
		}

		@Override
		public ColumnFilter getFilter() {
			return filter;
		}

		@Override
		public void columnsChanged(ColumnFilterView view) {
			view.columnsChanged();
		}

		private void updateIntRange(Number[] criterion) {
			String name = filter.getColumnName();
			Class<? extends CyIdentifiable> type = filter.getTableType();
			if (name == null || type == null) {
				intChooserController.reset(0, 0, 0, 0);
				return;
			}
			Number[] range = modelMonitor.getColumnRange(name, type);
			if (range == null) {
				range = new Number[] {0,0};
			}
			
			int min = range[0].intValue();
			int max = range[1].intValue();
			int low  = criterion == null ? min : criterion[0].intValue();
			int high = criterion == null ? max : criterion[1].intValue();
			intChooserController.reset(low, high, min, max);
		}
		
		private void updateDoubleRange(Number[] criterion) {
			String name = filter.getColumnName();
			Class<? extends CyIdentifiable> type = filter.getTableType();
			if (name == null || type == null) {
				doubleChooserController.reset(0d, 0d, 0d, 0d);
				return;
			} 
			Number[] range = modelMonitor.getColumnRange(name, type);
			if (range == null) {
				range = new Number[] {0,0};
			}
			
			double min = range[0].doubleValue();
			double max = range[1].doubleValue();
			double low  = criterion == null ? min : criterion[0].doubleValue();
			double high = criterion == null ? max : criterion[1].doubleValue();
			doubleChooserController.reset(low, high, min, max);
		}
		
		@Override
		public void setSliderBounds(int min, int max) {
			int low  = intChooserController.getLow();
			int high = intChooserController.getHigh();
			intChooserController.reset(low, high, min, max);
		}
		
		@Override
		public void setSliderBounds(double min, double max) {
			double low  = doubleChooserController.getLow();
			double high = doubleChooserController.getHigh();
			doubleChooserController.reset(low, high, min, max);
		}
		
	}
	
	@SuppressWarnings("serial")
	class View extends JPanel implements ColumnFilterView, InteractivityChangedListener {
		private final Controller controller;
		private final ColumnFilter filter;
		
		private boolean isInteractive;
		
		private JTextField textField;
		private JComboBox<ComboItem<Predicate>> predicateComboBox;
		private ColumnChooser columnChooser;
		private JPanel spacerPanel;
		private JPanel predicatePanel;
		
		private RangeChooser<Integer> intRangeChooser;
		private RangeChooser<Double>  doubleRangeChooser;
		
		private BooleanComboBox numericNegateComboBox;
		private BooleanComboBox booleanComboBox;
		private JLabel booleanLabel;
		private JPanel booleanPanel;
		
		private DocumentListener textFieldDocumentListener;
		private ActionListener predicateComboBoxActionListener;
		private ActionListener nameComboBoxActionListener;
		private StateChangeListener numericNegateComboBoxStateChangeListener;
		private StateChangeListener booleanComboBoxStateChangeListener;
		
		
		public View(Controller controller, ColumnFilter filter) {
			this.controller = controller;
			this.filter = filter;
			
			ViewUtil.configureFilterView(this);
			
			textField = style.createTextField();
			
			predicateComboBox = style.createCombo();
			predicateComboBox.addItem(new ComboItem<>(Predicate.CONTAINS, "contains"));
			predicateComboBox.addItem(new ComboItem<>(Predicate.DOES_NOT_CONTAIN, "doesn't contain"));
			predicateComboBox.addItem(new ComboItem<>(Predicate.IS, "is"));
			predicateComboBox.addItem(new ComboItem<>(Predicate.IS_NOT, "is not"));
			predicateComboBox.addItem(new ComboItem<>(Predicate.REGEX, "matches regex"));
			
			columnChooser = new ColumnChooser(style, serviceRegistrar);
			
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
			
			intRangeChooser    = controller.intChooserController.getRangeChooser();
			doubleRangeChooser = controller.doubleChooserController.getRangeChooser();
			
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
				columnChooser.updateComboBox();
			}
			if(selectColumn) {
				String name = filter.getColumnName();
				Class<?> type = filter.getTableType();
				columnChooser.select(0, item -> item.getName().equals(name) && item.getTableType().equals(type));
			}
			
			ColumnElement column = columnChooser.getSelectedItem();
			Object criterion = filter.getCriterion();
			SelectedColumnType colType = column.getColType();
			
			switch(colType) {
			case STRING:
				textField.setText((String)criterion);
				predicateComboBox.setSelectedItem(new ComboItem<>(filter.getPredicate()));
				handleStringColumnSelected();
				break;
			case INTEGER:
				refreshRangeUI(true,  (Number[])criterion, column, controller.intChooserController);
				break;
			case DOUBLE:
				refreshRangeUI(false, (Number[])criterion, column, controller.doubleChooserController);
				break;
			case BOOLEAN:
				booleanComboBox.setState((boolean)criterion);
				handleBooleanColumnSelected();
				break;
			case NONE:
				handleNoColumnSelected();
				break;
			}
			
			addListeners();
		}
		
		private void refreshRangeUI(boolean isInt, Number[] criterion, ColumnElement column, RangeChooserController<? extends Number> rcc) {
			modelMonitor.recomputeColumnRange(column.getName(), column.getTableType());
			
			if(isInt)
				controller.updateIntRange(criterion);
			else
				controller.updateDoubleRange(criterion);
			
			if(criterion == null) {
				Number[] range = { rcc.getLow(), rcc.getHigh() };
				filter.setCriterion(range);
			}
			
			numericNegateComboBox.setState(filter.getPredicate() != Predicate.IS_NOT_BETWEEN);
			
			handleNumericColumnSelected(isInt);
		}
		
		private void handleColumnSelected() {
			ColumnElement selected = columnChooser.getSelectedItem();
			if(selected == null) {
				filter.setPredicateAndCriterion(null, null);
				handleNoColumnSelected();
				controller.intChooserController.setInteractive(false);
				controller.doubleChooserController.setInteractive(false);
				return;
			}
			
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
			case INTEGER: 
			case DOUBLE:
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
			textField.getDocument().addDocumentListener(textFieldDocumentListener = new DocumentListenerAdapter() {
				public void update(DocumentEvent event) {
					filter.setCriterion(textField.getText());
				}
			});
			
			predicateComboBox.addActionListener(predicateComboBoxActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					ComboItem<Predicate> selected = predicateComboBox.getItemAt(predicateComboBox.getSelectedIndex());
					filter.setPredicate(selected.getValue());
				}
			});
			
			columnChooser.addActionListener(nameComboBoxActionListener = new ActionListener() {
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
			textField.getDocument().removeDocumentListener(textFieldDocumentListener);
			predicateComboBox.removeActionListener(predicateComboBoxActionListener);
			columnChooser.removeActionListener(nameComboBoxActionListener);
			numericNegateComboBox.removeStateChangeListener(numericNegateComboBoxStateChangeListener);
			booleanComboBox.removeStateChangeListener(booleanComboBoxStateChangeListener);
		}
		
		void handleNumericColumnSelected(boolean isInt) {
			RangeChooser<?> rangeChooser = isInt ? intRangeChooser : doubleRangeChooser;
			RangeChooserController<?> chooserController = isInt ? controller.intChooserController : controller.doubleChooserController;
			removeAll();
			add(columnChooser, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(numericNegateComboBox, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(rangeChooser, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 3, 3), 0, 0));
			chooserController.setInteractive(isInteractive);
			revalidate();
			validate();
			repaint();
		}

		void handleStringColumnSelected() {
			removeAll();
			predicatePanel.removeAll();
			predicatePanel.add(columnChooser, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			predicatePanel.add(predicateComboBox, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			predicatePanel.add(spacerPanel, new GridBagConstraints(2, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			
			add(predicatePanel, new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			add(textField, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			predicatePanel.invalidate();
			validate();
		}

		void handleBooleanColumnSelected() {
			removeAll();
			add(columnChooser, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(spacerPanel, new GridBagConstraints(1, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			add(booleanPanel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			invalidate();
			validate();
		}
		
		private void handleNoColumnSelected() {
			removeAll();
			add(columnChooser, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(spacerPanel, new GridBagConstraints(1, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			invalidate();
			validate();
		}


		@Override
		public void handleInteractivityChanged(boolean isInteractive) {
			this.isInteractive = isInteractive;
			controller.intChooserController.setInteractive(isInteractive);
			controller.doubleChooserController.setInteractive(isInteractive);
		}
	}
}
