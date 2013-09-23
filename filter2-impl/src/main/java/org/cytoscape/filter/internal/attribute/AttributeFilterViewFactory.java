package org.cytoscape.filter.internal.attribute;

import java.awt.Color;
import java.awt.Component;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.filter.internal.ModelMonitor;
import org.cytoscape.filter.internal.attribute.AttributeFilterView.AttributeComboBoxElement;
import org.cytoscape.filter.internal.attribute.AttributeFilterView.PredicateElement;
import org.cytoscape.filter.internal.prefuse.JRangeSlider;
import org.cytoscape.filter.internal.prefuse.JRangeSliderExtended;
import org.cytoscape.filter.internal.prefuse.NumberRangeModel;
import org.cytoscape.filter.internal.view.DynamicComboBoxModel;
import org.cytoscape.filter.internal.view.Matcher;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.predicates.Predicate;
import org.cytoscape.filter.transformers.Transformers;
import org.cytoscape.filter.view.TransformerViewFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;

public class AttributeFilterViewFactory implements TransformerViewFactory {

	ModelMonitor modelMonitor;
	List<AttributeComboBoxElement> nameComboBoxModel;
	List<PredicateElement> predicateComboBoxModel;
	private boolean interactive;
	
	public AttributeFilterViewFactory(ModelMonitor modelMonitor) {
		this.modelMonitor = modelMonitor;
		interactive = true;
		
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
	public Component createView(Transformer<?, ?> transformer) {
		AttributeFilter filter = (AttributeFilter) transformer;
		Controller controller = new Controller(filter);
		View view = new View(controller);
		modelMonitor.registerAttributeFilterView(view, controller);
		return view;
	}

	class Controller implements AttributeFilterController {
		private AttributeFilter filter;
		private NumberRangeModel sliderModel;
		private Number[] range;
		
		private boolean sliderActive;
		
		public Controller(AttributeFilter filter) {
			this.filter = filter;

			if (filter.getPredicate() == null) {
				filter.setPredicate(Predicate.CONTAINS);
			}
			
			sliderModel = new NumberRangeModel(0, 0, 0, 0);
			
			// Reuse range object during notifications to
			// minimize heap allocations
			range = new Number[2];
		}
		
		@Override
		public NumberRangeModel getSliderModel() {
			return sliderModel;
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
		
		public void sliderChanged() {
			range[0] = (Number) sliderModel.getLowValue();
			range[1] = (Number) sliderModel.getHighValue();
			filter.setCriterion(range);
		}

		public void setAttributeName(String name) {
			filter.setAttributeName(name);
		}
		
		public void updateSliderModel() {
			if (!sliderActive) {
				return;
			}
			
			String name = filter.getAttributeName();
			Class<? extends CyIdentifiable> type = filter.getAttributeType();
			if (name == null || type == null) {
				sliderModel.setMinValue(0);
				sliderModel.setMaxValue(0);
				return;
			}
			Number[] range = modelMonitor.getAttributeRange(name, type);
			if (range == null) {
				sliderModel.setMinValue(0);
				sliderModel.setMaxValue(0);
				return;
			}
			
			sliderModel.setMinValue(range[0]);
			sliderModel.setMaxValue(range[1]);
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
			if (nameComboBox.getSelectedIndex() == 0) {
				view.handleNoAttributeSelected();
				sliderActive = false; 
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
				sliderActive = false;
			} else {
				filter.setPredicate(Predicate.BETWEEN);
				view.handleNumericAttributeSelected();
				sliderActive = true;
				updateSliderModel();
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
				NumberRangeModel model = (NumberRangeModel) view.getSlider().getModel();
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
			
			DynamicComboBoxModel.select(view.getPredicateComboBox(), 0, new Matcher<PredicateElement>() {
				@Override
				public boolean matches(PredicateElement item) {
					return item.predicate.equals(filter.getPredicate());
				}
			});
			
			updateSliderModel();
		}
	}
	
	@SuppressWarnings("serial")
	class View extends JPanel implements AttributeFilterView {
		boolean optionsExpanded;
		private JRangeSliderExtended slider;
		private JTextField field;
		private JLabel arrowLabel;
		private JCheckBox caseSensitiveCheckBox;
		private JComboBox predicateComboBox;
		private JComboBox nameComboBox;
		private JPanel spacerPanel;
		private JPanel predicatePanel;
		
		public View(final Controller controller) {
			slider = new JRangeSliderExtended(controller.getSliderModel(), JRangeSlider.HORIZONTAL, JRangeSlider.LEFTRIGHT_TOPBOTTOM);
			slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent event) {
					controller.sliderChanged();
				}
			});
			
			field = new JTextField();
			field.addFocusListener(new FocusListener() {

				@Override
				public void focusLost(FocusEvent event) {
					if (!interactive) {
						controller.setCriterion(View.this, field.getText());
					}
				}
				
				@Override
				public void focusGained(FocusEvent event) {
				}
			});
			field.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent event) {
					handleInteractiveUpdate(controller);
				}
				
				@Override
				public void insertUpdate(DocumentEvent event) {
					handleInteractiveUpdate(controller);
				}
				
				@Override
				public void changedUpdate(DocumentEvent event) {
					handleInteractiveUpdate(controller);
				}
			});
			
			arrowLabel = new JLabel("◀");
			Font arrowFont = arrowLabel.getFont().deriveFont(10.0f);
			arrowLabel.setFont(arrowFont);
			arrowLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent event) {
					handleArrowClicked(controller);
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
			predicatePanel = new JPanel();
			predicatePanel.setLayout(new GridBagLayout());
			
			arrowLabel.setBackground(Color.red);
			setLayout(new GridBagLayout());
			controller.synchronize(this);
		}

		void handleInteractiveUpdate(Controller controller) {
			if (!interactive) {
				return;
			}
			controller.setCriterion(View.this, field.getText());
		}

		public JLabel getArrowLabel() {
			return arrowLabel;
		}

		protected void handleArrowClicked(Controller controller) {
			optionsExpanded = !optionsExpanded;
			if (optionsExpanded) {
				arrowLabel.setText("▼");				
			} else {
				arrowLabel.setText("◀");				
			}
			controller.handleAttributeSelected(this, nameComboBox);
		}

		void handleNumericAttributeSelected() {
			removeAll();
			add(nameComboBox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(slider, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			slider.invalidate();
			validate();
		}

		void handleStringAttributeSelected() {
			removeAll();
			if (optionsExpanded) {
				predicatePanel.removeAll();
				predicatePanel.add(nameComboBox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				predicatePanel.add(predicateComboBox, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				predicatePanel.add(spacerPanel, new GridBagConstraints(2, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				predicatePanel.add(arrowLabel, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0, 0));
				
				add(predicatePanel, new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				add(field, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				add(caseSensitiveCheckBox, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			} else {
				predicatePanel.removeAll();
				predicatePanel.add(nameComboBox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				predicatePanel.add(spacerPanel, new GridBagConstraints(1, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				predicatePanel.add(arrowLabel, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, new Insets(0, 0, 0, 4), 0, 0));
				predicatePanel.add(field, new GridBagConstraints(0, 1, 3, 1, 1, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				
				add(predicatePanel, new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			}
			validate();
		}

		private void handleNoAttributeSelected() {
			removeAll();
			add(nameComboBox, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(spacerPanel, new GridBagConstraints(1, 0, 1, 1, Double.MIN_VALUE, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			validate();
		}
		
		@Override
		public JCheckBox getCaseSensitiveCheckBox() {
			return caseSensitiveCheckBox;
		}
		
		@Override
		public JTextField getField() {
			return field;
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
		public JRangeSliderExtended getSlider() {
			return slider;
		}
	}
}
