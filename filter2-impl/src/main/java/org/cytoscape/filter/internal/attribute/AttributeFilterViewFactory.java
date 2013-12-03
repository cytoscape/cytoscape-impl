package org.cytoscape.filter.internal.attribute;

import java.awt.Color;
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
import org.cytoscape.filter.internal.attribute.AttributeFilterView.AttributeComboBoxElement;
import org.cytoscape.filter.internal.attribute.AttributeFilterView.PredicateElement;
import org.cytoscape.filter.internal.prefuse.NumberRangeModel;
import org.cytoscape.filter.internal.view.DynamicComboBoxModel;
import org.cytoscape.filter.internal.view.IconManager;
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

public class AttributeFilterViewFactory implements TransformerViewFactory {

	ModelMonitor modelMonitor;
	List<AttributeComboBoxElement> nameComboBoxModel;
	List<PredicateElement> predicateComboBoxModel;
	private IconManager iconManager;
	
	public AttributeFilterViewFactory(ModelMonitor modelMonitor, IconManager iconManager) {
		this.modelMonitor = modelMonitor;
		this.iconManager = iconManager;
		
		nameComboBoxModel = modelMonitor.getAttributeComboBoxModel();
		
		predicateComboBoxModel = new ArrayList<PredicateElement>();
		predicateComboBoxModel.add(new PredicateElement(Predicate.CONTAINS, "contains"));
		predicateComboBoxModel.add(new PredicateElement(Predicate.DOES_NOT_CONTAIN, "doesn't contain"));
		predicateComboBoxModel.add(new PredicateElement(Predicate.IS, "is"));
		predicateComboBoxModel.add(new PredicateElement(Predicate.IS_NOT, "is not"));
		predicateComboBoxModel.add(new PredicateElement(Predicate.REGEX, "matches regex"));
	}
	
	@Override
	public String getId() {
		return Transformers.ATTRIBUTE_FILTER;
	}

	@Override
	public JComponent createView(Transformer<?, ?> transformer) {
		AttributeFilter filter = (AttributeFilter) transformer;
		Controller controller = new Controller(filter);
		View view = new View(controller, iconManager);
		modelMonitor.registerAttributeFilterView(view, controller);
		return view;
	}

	class Controller implements AttributeFilterController {
		private AttributeFilter filter;
		private RangeChooserController chooserController;
		
		public Controller(final AttributeFilter filter) {
			this.filter = filter;

			if (filter.getPredicate() == null) {
				filter.setPredicate(Predicate.CONTAINS);
			}
			
			final Number[] range = new Number[2];
			chooserController = new RangeChooserController() {
				@Override
				public void handleRangeChanged(Number low, Number high) {
					range[0] = low;
					range[1] = high;
					filter.setCriterion(range);
				}
			};
		}
		
		@Override
		public AttributeFilter getFilter() {
			return filter;
		}

		public void setCaseSensitive(View view, boolean caseSensitive) {
			filter.setCaseSensitive(caseSensitive);
			updateArrowLabel(view.getArrowLabel());
		}

		private void updateArrowLabel(JLabel arrowLabel) {
			boolean notDefault = filter.getCaseSensitive() || filter.getPredicate() != Predicate.CONTAINS;
			if (notDefault) {
				arrowLabel.setForeground(Color.blue);
			} else {
				arrowLabel.setForeground(Color.black);
			}
		}

		public void setCriterion(View view, String text) {
			if (text != null && text.equals(filter.getCriterion())) {
				return;
			}
			filter.setCriterion(text);
		}
		
		public void setAttributeName(String name) {
			filter.setAttributeName(name);
		}
		
		public void updateRange() {
			String name = filter.getAttributeName();
			Class<? extends CyIdentifiable> type = filter.getAttributeType();
			if (name == null || type == null) {
				chooserController.setRange(0, 0, 0, 0);
				return;
			}
			modelMonitor.recomputeAttributeRange(name, type);
			Number[] range = modelMonitor.getAttributeRange(name, type);
			if (range == null) {
				chooserController.setRange(0, 0, 0, 0);
				return;
			}
			
			chooserController.setBounds(range[0], range[1]);
		}
		
		public void setMatchType(Class<?> type) {
			if (type == null) {
				filter.type.setSelectedValue(AttributeFilter.NODES_AND_EDGES);
			}
			if (CyNode.class.equals(type)) {
				filter.type.setSelectedValue(AttributeFilter.NODES);
			}
			if (CyEdge.class.equals(type)) {
				filter.type.setSelectedValue(AttributeFilter.EDGES);
			}
		}
		
		public void setPredicate(View view, Predicate predicate) {
			filter.setPredicate(predicate);
			updateArrowLabel(view.getArrowLabel());
		}

		public AttributeFilter getModel() {
			return filter;
		}

		void handleAttributeSelected(View view, JComboBox nameComboBox) {
			RangeChooser rangeChooser = view.rangeChooser;
			if (nameComboBox.getSelectedIndex() == 0) {
				view.handleNoAttributeSelected();
				chooserController.setInteractive(false, rangeChooser);
				return;
			}

			nameComboBox.setForeground(Color.black);
			
			AttributeComboBoxElement selected = (AttributeComboBoxElement) nameComboBox.getSelectedItem();
			setAttributeName(selected.name);
			setMatchType(selected.attributeType);
			
			if (modelMonitor.isString(selected.name, selected.attributeType)) {
				Predicate predicate = filter.getPredicate();
				if (predicate == null || predicate == Predicate.BETWEEN) {
					filter.setPredicate(Predicate.CONTAINS);
				}
				
				view.handleStringAttributeSelected();
				chooserController.setInteractive(false, rangeChooser);
			} else {
				filter.setPredicate(Predicate.BETWEEN);
				view.handleNumericAttributeSelected();
				chooserController.setInteractive(view.isInteractive, rangeChooser);
				updateRange();
			}
		}

		@Override
		public void synchronize(AttributeFilterView view) {
			Object criterion = filter.getCriterion();
			if (criterion instanceof String) {
				view.getField().setText((String) criterion);
			}
			if (criterion instanceof Number[]) {
				Number[] range = (Number[]) criterion;
				NumberRangeModel model = (NumberRangeModel) chooserController.getSliderModel();
				model.setLowValue(range[0]);
				model.setHighValue(range[1]);
			}
			
			view.getCaseSensitiveCheckBox().setSelected(filter.getCaseSensitive());
			
			DynamicComboBoxModel.select(view.getNameComboBox(), 0, new Matcher<AttributeComboBoxElement>() {
				@Override
				public boolean matches(AttributeComboBoxElement item) {
					return item.name.equals(filter.getAttributeName()) && item.attributeType.equals(filter.getAttributeType());
				}
			});
			
			DynamicComboBoxModel.select(view.getPredicateComboBox(), -1, new Matcher<PredicateElement>() {
				@Override
				public boolean matches(PredicateElement item) {
					return item.predicate.equals(filter.getPredicate());
				}
			});
			
			updateRange();
		}

		public void setInteractive(boolean isInteractive, View view) {
			view.isInteractive = isInteractive;
			switch (view.selectedAttribute) {
			case NONE:
				view.handleNoAttributeSelected();
				break;
			case NUMERIC:
				view.handleNumericAttributeSelected();
				chooserController.setInteractive(isInteractive, view.rangeChooser);
				break;
			case STRING:
				view.handleStringAttributeSelected();
				break;
			}
		}
		
		@Override
		public RangeChooserController getRangeChooserController() {
			return chooserController;
		}
	}
	
	@SuppressWarnings("serial")
	class View extends JPanel implements AttributeFilterView, InteractivityChangedListener {
		boolean optionsExpanded;
		private JTextField textField;
		private JLabel arrowLabel;
		private JCheckBox caseSensitiveCheckBox;
		private JComboBox predicateComboBox;
		private JComboBox nameComboBox;
		private JPanel spacerPanel;
		private JPanel predicatePanel;
		private boolean isInteractive;
		private SelectedAttributeType selectedAttribute;
		private Controller controller;
		private RangeChooser rangeChooser;
		
		public View(final Controller controller, IconManager iconManager) {
			this.controller = controller;
			selectedAttribute = SelectedAttributeType.NONE;
			
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
			
			arrowLabel = new JLabel(IconManager.ICON_CARET_LEFT);
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
			
			nameComboBox = new JComboBox(new DynamicComboBoxModel<AttributeComboBoxElement>(nameComboBoxModel));
			nameComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					controller.handleAttributeSelected(View.this, nameComboBox);
				}
			});
			
			spacerPanel = new JPanel();
			spacerPanel.setOpaque(false);
			
			predicatePanel = new JPanel();
			predicatePanel.setOpaque(false);
			predicatePanel.setLayout(new GridBagLayout());
			
			rangeChooser = new RangeChooser(controller.chooserController);
			
			setLayout(new GridBagLayout());
			controller.synchronize(this);
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
			handleStringAttributeSelected();
		}

		void handleNumericAttributeSelected() {
			selectedAttribute = SelectedAttributeType.NUMERIC;
			removeAll();
			add(nameComboBox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(rangeChooser, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			controller.chooserController.setInteractive(isInteractive, rangeChooser);
			revalidate();
			validate();
			repaint();
		}

		void handleStringAttributeSelected() {
			selectedAttribute = SelectedAttributeType.STRING;
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

		private void handleNoAttributeSelected() {
			selectedAttribute = SelectedAttributeType.NONE;
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
		public void handleInteractivityChanged(boolean isInteractive) {
			controller.setInteractive(isInteractive, this);
		}
	}
	
	private enum SelectedAttributeType {
		NONE,
		NUMERIC,
		STRING,
	}
}
