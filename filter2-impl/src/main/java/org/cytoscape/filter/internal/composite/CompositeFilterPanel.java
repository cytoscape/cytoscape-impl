package org.cytoscape.filter.internal.composite;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.cytoscape.filter.internal.view.DynamicComboBoxModel;
import org.cytoscape.filter.internal.view.FilterPanel;
import org.cytoscape.filter.internal.view.FilterPanelController;
import org.cytoscape.filter.internal.view.TransformerElementViewModel;
import org.cytoscape.filter.internal.view.ViewUtil;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.CompositeFilter.Type;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

@SuppressWarnings("serial")
public class CompositeFilterPanel extends JPanel {
	private static final Border BORDER = BorderFactory.createMatteBorder(0, 0, 2, 0, Color.DARK_GRAY);
	private static final Border NO_BORDER = BorderFactory.createEmptyBorder();

	private Map<Filter<CyNetwork, CyIdentifiable>, TransformerElementViewModel<FilterPanel>> viewModels;
	private GroupLayout layout;
	private int depth;
	private JComboBox combiningMethodComboBox;
	private FilterPanelController filterPanelController;
	private final JComboBox addComboBox;
	private FilterPanel parent;
	private CompositeFilter<CyNetwork, CyIdentifiable> model;
	
	public CompositeFilterPanel(FilterPanel parent, FilterPanelController filterPanelController, final CompositeFilter<CyNetwork, CyIdentifiable> model, int depth) {
		this(parent, filterPanelController, new Controller(), model, depth);
	}
	
	public CompositeFilterPanel(FilterPanel parent, FilterPanelController filterPanelController, final Controller controller, final CompositeFilter<CyNetwork, CyIdentifiable> model, int depth) {
		this.parent = parent;
		this.filterPanelController = filterPanelController;
		this.depth = depth;
		this.model = model;

		ViewUtil.configureFilterView(this);
		
		viewModels = new WeakHashMap<Filter<CyNetwork,CyIdentifiable>, TransformerElementViewModel<FilterPanel>>();
		layout = new GroupLayout(this);
		setLayout(layout);
		updateBorder();
		
		combiningMethodComboBox = new JComboBox(controller.getCombiningMethodComboBoxModel());
		combiningMethodComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				controller.handleCombiningMethodSelected(combiningMethodComboBox, model);
			}
		});
		
		addComboBox = createFilterComboBox(filterPanelController.createFilterComboBoxModel());
		
		for (int i = 0; i < model.getLength(); i++) {
			Filter<CyNetwork, CyIdentifiable> filter = model.get(i);
			JComponent component = filterPanelController.createView(parent, filter, depth + 1);
			TransformerElementViewModel<FilterPanel> viewModel = new TransformerElementViewModel<FilterPanel>(component, filterPanelController, parent);
			viewModels.put(filter, viewModel);
		}
	}
	
	private void updateBorder() {
		if (depth > 0) {
			setBorder(BORDER);
		} else {
			setBorder(NO_BORDER);
		}
	}

	public void selectAll() {
		for (TransformerElementViewModel<FilterPanel> viewModel : viewModels.values()) {
			if (!viewModel.checkBox.isSelected()) {
				viewModel.checkBox.setSelected(true);
				viewModel.view.setBackground(CompositeTransformerPanel.SELECTED_BACKGROUND_COLOR);
			}
			if (viewModel.view instanceof CompositeFilterPanel) {
				CompositeFilterPanel panel = (CompositeFilterPanel) viewModel.view;
				panel.selectAll();
			}
		}
	}
	
	public void deselectAll() {
		for (TransformerElementViewModel<FilterPanel> viewModel : viewModels.values()) {
			if (viewModel.checkBox.isSelected()) {
				viewModel.checkBox.setSelected(false);
				viewModel.view.setBackground(CompositeTransformerPanel.UNSELECTED_BACKGROUND_COLOR);
			}
			if (viewModel.view instanceof CompositeFilterPanel) {
				CompositeFilterPanel panel = (CompositeFilterPanel) viewModel.view;
				panel.deselectAll();
			}
		}
	}

	public void deleteSelected() {
		int index = 0;
		while (index < model.getLength()) {
			TransformerElementViewModel<FilterPanel> viewModel = getViewModel(model.get(index));
			if (viewModel.checkBox.isSelected()) {
				removeFilter(index--);
			} else {
				if (viewModel.view instanceof CompositeFilterPanel) {
					CompositeFilterPanel panel = (CompositeFilterPanel) viewModel.view;
					panel.deleteSelected();
					if (panel.getModel().getLength() == 0) {
						removeFilter(index--);
					}
				}
			}
			index++;
		}
	}
	
	public int countSelected() {
		int count = 0, index = 0;
		while (index < model.getLength()) {
			TransformerElementViewModel<FilterPanel> viewModel = getViewModel(model.get(index));
			if (viewModel.checkBox.isSelected()) {
				count++;
			}
			index++;
		}
		return count;
	}
	
	public int countUnselected() {
		int count = 0, index = 0;
		while (index < model.getLength()) {
			TransformerElementViewModel<FilterPanel> viewModel = getViewModel(model.get(index));
			if (!viewModel.checkBox.isSelected()) {
				count++;
			}
			index++;
		}
		return count;
	}

	public void updateLayout() {
		removeAll();

		Group columns = layout.createParallelGroup(Alignment.LEADING, true);
		Group rows = layout.createSequentialGroup();
		
		if (depth > 0 || model.getLength() > 1) {
			columns.addComponent(combiningMethodComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
			rows.addComponent(combiningMethodComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
		}
		
		ParallelGroup checkBoxGroup = layout.createParallelGroup(Alignment.LEADING);
		ParallelGroup viewGroup = layout.createParallelGroup(Alignment.LEADING);
		columns.addGroup(layout.createSequentialGroup()
							   .addGroup(checkBoxGroup)
							   .addGroup(viewGroup));
		
		for (int i = 0; i < model.getLength(); i++) {
			TransformerElementViewModel<FilterPanel> viewModel = getViewModel(model.get(i));
			if (viewModel.view instanceof CompositeFilterPanel) {
				CompositeFilterPanel panel = (CompositeFilterPanel) viewModel.view;
				panel.updateLayout();
			}
			
			checkBoxGroup.addComponent(viewModel.checkBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE);
			viewGroup.addComponent(viewModel.view, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
			
			rows.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(viewModel.checkBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
								.addComponent(viewModel.view, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE));
		}
		
		viewGroup.addComponent(addComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
		rows.addComponent(addComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
		
		layout.setHorizontalGroup(columns);
		layout.setVerticalGroup(rows);
	}

	
	JComboBox createFilterComboBox(ComboBoxModel model) {
		final JComboBox comboBox = new JComboBox(model);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				filterPanelController.handleAddFilter(comboBox, CompositeFilterPanel.this, parent);
			}
		});
		return comboBox;
	}

	public void addViewModel(int index, Filter<CyNetwork, CyIdentifiable> filter, TransformerElementViewModel<FilterPanel> viewModel) {
		model.insert(index, filter);
		viewModels.put(filter, viewModel);
	}


	public void addViewModel(Filter<CyNetwork, CyIdentifiable> filter, TransformerElementViewModel<FilterPanel> viewModel) {
		model.append(filter);
		viewModels.put(filter, viewModel);
	}

	public void addFilter(Filter<CyNetwork, CyIdentifiable> filter) {
		JComponent component = filterPanelController.createView(parent, filter, depth + 1);
		final TransformerElementViewModel<FilterPanel> viewModel = new TransformerElementViewModel<FilterPanel>(component, filterPanelController, parent);
		addViewModel(filter, viewModel);
	}

	public TransformerElementViewModel<FilterPanel> getViewModel(Filter<CyNetwork, CyIdentifiable> filter) {
		return viewModels.get(filter);
	}

	public CompositeFilter<CyNetwork, CyIdentifiable> getModel() {
		return model;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void setDepth(int depth) {
		this.depth = depth;
		updateBorder();
		for (TransformerElementViewModel<FilterPanel> viewModel : viewModels.values()) {
			if (viewModel.view instanceof CompositeFilterPanel) {
				CompositeFilterPanel panel = (CompositeFilterPanel) viewModel.view;
				panel.setDepth(depth + 1);
			}
		}
	}

	public void removeFilter(int index) {
		Filter<CyNetwork, CyIdentifiable> filter = model.remove(index);
		viewModels.remove(filter);
	}

	public Collection<TransformerElementViewModel<FilterPanel>> getViewModels() {
		return viewModels.values();
	}
	
	public static class Controller implements CompositeFilterController {
		private static List<CombiningMethodElement> combiningMethods = createCombiningMethods();

		private static List<CombiningMethodElement> createCombiningMethods() {
			ArrayList<CombiningMethodElement> methods = new ArrayList<CombiningMethodElement>();
			methods.add(new CombiningMethodElement("Match all (AND)", CompositeFilter.Type.ALL));
			methods.add(new CombiningMethodElement("Match any (OR)", CompositeFilter.Type.ANY));
			return methods;
		}

		public void handleCombiningMethodSelected(JComboBox comboBox, CompositeFilter<CyNetwork, CyIdentifiable> model) {
			CombiningMethodElement selected = (CombiningMethodElement) comboBox.getSelectedItem();
			model.setType(selected.combiningMethod);
		}

		public ComboBoxModel getCombiningMethodComboBoxModel() {
			return new DynamicComboBoxModel<CombiningMethodElement>(combiningMethods);
		}
	}
	
	static class CombiningMethodElement {
		public final String name;
		public final CompositeFilter.Type combiningMethod;
		
		public CombiningMethodElement(String name, Type combiningMethod) {
			this.name = name;
			this.combiningMethod = combiningMethod;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
}