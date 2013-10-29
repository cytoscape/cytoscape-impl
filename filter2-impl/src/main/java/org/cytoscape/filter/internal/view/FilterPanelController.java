package org.cytoscape.filter.internal.view;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.ModelMonitor;
import org.cytoscape.filter.internal.composite.CompositeFilterPanel;
import org.cytoscape.filter.internal.view.TransformerViewManager.FilterComboBoxElement;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.TransformerFactory;
import org.cytoscape.filter.view.InteractivityChangedListener;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class FilterPanelController {
	private static final Color SELECTED_BACKGROUND_COLOR = new Color(222, 234, 252);

	private int totalSelected;
	private TransformerManager transformerManager;
	private TransformerViewManager transformerViewManager;
	private DynamicComboBoxModel<FilterElement> filterComboBoxModel;
	
	int filtersCreated = 0;
	private ViewUpdater viewUpdater;
	private ModelMonitor modelMonitor;

	private boolean isInteractive;

	
	public FilterPanelController(TransformerManager transformerManager, TransformerViewManager transformerViewManager, ViewUpdater viewUpdater, ModelMonitor modelMonitor) {
		this.transformerManager = transformerManager;
		this.transformerViewManager = transformerViewManager;
		this.viewUpdater = viewUpdater;
		this.modelMonitor = modelMonitor;
		
		viewUpdater.setController(this);
		
		List<FilterElement> modelItems = new ArrayList<FilterElement>();
		modelItems.add(new FilterElement("(Create New Filter...)", null));
		filterComboBoxModel = new DynamicComboBoxModel<FilterElement>(modelItems);
		addNewFilter("Default filter");
	}

	void handleIndent(FilterPanel panel) {
		CompositeFilterPanel root = panel.getRootPanel();
		indent(panel, root);
		root.setDepth(0);
		root.updateLayout();
		updateEditPanel(panel);
		viewUpdater.handleFilterStructureChanged();
	}
	
	private void indent(FilterPanel parent, CompositeFilterPanel panel) {
		CompositeFilterPanel view = null;
		
		CompositeFilter<CyNetwork, CyIdentifiable> model = panel.getModel();
		int index = 0;
		while (index < model.getLength()) {
			Filter<CyNetwork, CyIdentifiable> filter = model.get(index);
			FilterViewModel viewModel = panel.getViewModel(filter);
			if (viewModel.view instanceof CompositeFilterPanel) {
				indent(parent, (CompositeFilterPanel) viewModel.view);
			}
			if (viewModel.checkBox.isSelected()) {
				panel.removeFilter(index--);
				
				if (view == null) {
					CompositeFilter<CyNetwork, CyIdentifiable> childModel = transformerManager.createCompositeFilter(CyNetwork.class, CyIdentifiable.class);
					view = (CompositeFilterPanel) createView(parent, childModel, panel.getDepth() + 1);
					FilterViewModel groupModel = new FilterViewModel(view, this, parent);
					panel.addViewModel(++index, childModel, groupModel);
				}
				view.addViewModel(filter, viewModel);
			} else {
				view = null;
			}
			index++;
		}
	}

	void handleOutdent(FilterPanel panel) {
		CompositeFilterPanel root = panel.getRootPanel();
		outdent(root, 0);
		root.setDepth(0);
		root.updateLayout();
		updateEditPanel(panel);
		viewUpdater.handleFilterStructureChanged();
	}
	
	Map<Filter<CyNetwork, CyIdentifiable>, FilterViewModel> outdent(CompositeFilterPanel panel, int depth) {
		Map<Filter<CyNetwork, CyIdentifiable>, FilterViewModel> outdented = new LinkedHashMap<Filter<CyNetwork,CyIdentifiable>, FilterViewModel>();
		CompositeFilter<CyNetwork, CyIdentifiable> model = panel.getModel();
		int index = 0;
		while (index < model.getLength()) {
			Filter<CyNetwork, CyIdentifiable> filter = model.get(index);
			FilterViewModel viewModel = panel.getViewModel(filter);
			if (filter instanceof CompositeFilter) {
				CompositeFilter<CyNetwork, CyIdentifiable> child = (CompositeFilter<CyNetwork, CyIdentifiable>) filter;
				CompositeFilterPanel childPanel = (CompositeFilterPanel) viewModel.view;
				Map<Filter<CyNetwork, CyIdentifiable>, FilterViewModel> toAdd = outdent(childPanel, depth + 1);
				if (child.getLength() == 0) {
					panel.removeFilter(index);
				}
				for (Entry<Filter<CyNetwork, CyIdentifiable>, FilterViewModel> entry : toAdd.entrySet()) {
					panel.addViewModel(index++, entry.getKey(), entry.getValue());
				}
			}
			if (depth == 0) {
				index++;
				continue;
			}
			if (viewModel.checkBox.isSelected()) {
				panel.removeFilter(index--);
				outdented.put(filter, viewModel);
			}
			index++;
		}
		return outdented;
	}
	
	boolean canOutdent(CompositeFilterPanel panel) {
		return canOutdent(panel, 0);
	}
	
	boolean canOutdent(CompositeFilterPanel panel, int depth) {
		for (FilterViewModel viewModel : panel.getViewModels()) {
			if (viewModel.view instanceof CompositeFilterPanel) {
				if (canOutdent((CompositeFilterPanel) viewModel.view, depth + 1)) {
					return true;
				}
			}
			if (viewModel.checkBox.isSelected() && depth > 0) {
				return true;
			}
		}
		return false;
	}
	
	void handleDelete(FilterPanel panel) {
		CompositeFilterPanel root = panel.getRootPanel();
		root.deleteSelected();
		totalSelected = 0;
		updateEditPanel(panel);
		root.updateLayout();
		viewUpdater.handleFilterStructureChanged();
	}
	
	void handleCancel(FilterPanel panel) {
		CompositeFilterPanel root = panel.getRootPanel();
		root.deselectAll();
		totalSelected = 0;
		updateEditPanel(panel);
	}
	
	void handleCheck(FilterPanel panel, JCheckBox checkBox, JComponent view) {
		if (checkBox.isSelected()) {
			view.setBackground(SELECTED_BACKGROUND_COLOR);
			totalSelected += 1;
		} else {
			view.setBackground(Color.WHITE);
			totalSelected -=1;
		}
		updateEditPanel(panel);
	}
	
	void updateEditPanel(FilterPanel panel) {
		CompositeFilterPanel root = panel.getRootPanel();
		JButton outdentButton = panel.getOutdentButton();
		outdentButton.setEnabled(canOutdent(root));
		
		Component editPanel = panel.getEditPanel();
		editPanel.setVisible(totalSelected > 0);
		panel.validate();
	}

	public JComponent createView(FilterPanel parent, Filter<CyNetwork, CyIdentifiable> filter, int depth) {
		if (filter instanceof CompositeFilter) {
			return new CompositeFilterPanel(parent, this, (CompositeFilter<CyNetwork, CyIdentifiable>) filter, depth);
		}
		JComponent view = transformerViewManager.createView(filter);
		if (view instanceof InteractivityChangedListener) {
			((InteractivityChangedListener) view).handleInteractivityChanged(isInteractive);
		}
		return view;
	}

	public void handleAddFilter(JComboBox comboBox, CompositeFilterPanel panel) {
		if (comboBox.getSelectedIndex() == 0) {
			return;
		}
		
		@SuppressWarnings("unchecked")
		TransformerFactory<CyNetwork, CyIdentifiable> factory = (TransformerFactory<CyNetwork, CyIdentifiable>) comboBox.getSelectedItem();
		
		// Assume the factory makes filters
		Filter<CyNetwork, CyIdentifiable> filter = (Filter<CyNetwork, CyIdentifiable>) factory.createTransformer();
		panel.addFilter(filter);
		panel.updateLayout();
		comboBox.setSelectedIndex(0);
		
		filter.addListener(viewUpdater);
		viewUpdater.handleFilterStructureChanged();
	}

	public ComboBoxModel createFilterComboBoxModel() {
		return new DynamicComboBoxModel<FilterComboBoxElement>(transformerViewManager.getFilterComboBoxModel());
	}

	@SuppressWarnings("unchecked")
	void handleFilterSelected(JComboBox filterComboBox, FilterPanel panel) {
		if (filterComboBox.getSelectedIndex() == 0) {
			String defaultName = String.format("My filter %d", ++filtersCreated);
			String name;
			String message = "Please provide a name for your filter.";
			while (true) {
				name = (String) JOptionPane.showInputDialog(null, message, "Create New Filter", JOptionPane.QUESTION_MESSAGE, null, null, defaultName);
				if (name == null) {
					return;
				}
				if (validateFilterName(null, name, (DynamicComboBoxModel<FilterElement>) filterComboBox.getModel())) {
					break;
				}
				message = "The name '" + name + "' is already being used by another filter.  Please provide a different name.";
			}
			addNewFilter(name);
		}
		FilterElement selected = (FilterElement) filterComboBox.getSelectedItem();
		if (selected == null) {
			return;
		}
		setFilter(selected.filter, panel);
		viewUpdater.handleFilterStructureChanged();
	}
	
	private boolean validateFilterName(String oldName, String newName, DynamicComboBoxModel<FilterElement> comboBoxModel) {
		if (oldName != null && oldName.equalsIgnoreCase(newName)) {
			// Name didn't change.
			return true;
		}
		
		for (FilterElement element : comboBoxModel) {
			if (element.name.equalsIgnoreCase(newName)) {
				return false;
			}
		}
		return true;
	}

	private void addNewFilter(String name) {
		CompositeFilter<CyNetwork, CyIdentifiable> filter = transformerManager.createCompositeFilter(CyNetwork.class, CyIdentifiable.class);
		filter.addListener(viewUpdater);
		FilterElement element = new FilterElement(name, filter);
		filterComboBoxModel.add(element);
		filterComboBoxModel.setSelectedItem(element);
	}

	private void setFilter(CompositeFilter<CyNetwork, CyIdentifiable> filter, FilterPanel parent) {
		CompositeFilterPanel root = (CompositeFilterPanel) createView(parent, filter, 0);
		parent.setRootPanel(root);
	}

	void handleImport(FilterPanel panel) {
		// TODO Auto-generated method stub
		showComingSoonMessage(panel);
	}

	void handleExport(FilterPanel panel) {
		// TODO Auto-generated method stub
		showComingSoonMessage(panel);
	}

	private void showComingSoonMessage(Component parent) {
		JOptionPane.showMessageDialog(parent, "Coming soon!", "Not yet implemented", JOptionPane.INFORMATION_MESSAGE);
	}

	void handleDelete() {
		int index = filterComboBoxModel.getSelectedIndex(); 
		if (index <= 0) {
			// If nothing or the "create new filter" item is selected, do nothing.
			return;
		}
		
		filterComboBoxModel.remove(index);
	}

	@SuppressWarnings("unchecked")
	void handleRename(FilterPanel panel) {
		JComboBox comboBox = panel.getFilterComboBox();
		FilterElement selected = (FilterElement) comboBox.getSelectedItem();
		String defaultName = selected.name;
		String name;
		String message = "Please provide a name for your filter.";
		while (true) {
			name = (String) JOptionPane.showInputDialog(null, message, "Rename Filter", JOptionPane.QUESTION_MESSAGE, null, null, defaultName);
			if (name == null) {
				return;
			}
			if (validateFilterName(defaultName, name, (DynamicComboBoxModel<FilterElement>) comboBox.getModel())) {
				break;
			}
			message = "The name '" + name + "' is already being used by another filter.  Please provide a different name.";
		}
		selected.name = name;
	}

	public DynamicComboBoxModel<FilterElement> getFilterComboBoxModel() {
		return filterComboBoxModel;
	}
	
	static class FilterElement {
		public String name;
		public final CompositeFilter<CyNetwork, CyIdentifiable> filter;
		
		public FilterElement(String name, CompositeFilter<CyNetwork, CyIdentifiable> filter) {
			this.name = name;
			this.filter = filter;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}

	public Filter<CyNetwork, CyIdentifiable> getFilter() {
		FilterElement selected = (FilterElement) filterComboBoxModel.getSelectedItem();
		return selected.filter;
	}

	public void setUpdating(boolean updating, FilterPanel panel) {
		if (updating) {
			panel.setStatus("Applying...");
		} else {
			panel.setStatus("Apply Filter");
		}
		panel.getApplyFilterButton().setEnabled(!updating && !isInteractive);
		panel.getCancelApplyButton().setEnabled(updating);
	}

	public void setInteractive(boolean isInteractive, FilterPanel panel) {
		modelMonitor.setInteractive(isInteractive);
		viewUpdater.setInteractive(isInteractive);
		if (this.isInteractive == isInteractive) {
			return;
		}
		
		panel.getApplyAutomaticallyCheckBox().setSelected(isInteractive);
		
		this.isInteractive = isInteractive;
		CompositeFilterPanel root = panel.getRootPanel();
		setInteractive(isInteractive, root);
		setUpdating(false, panel);
	}

	private void setInteractive(boolean isInteractive, CompositeFilterPanel panel) {
		for (FilterViewModel viewModel : panel.getViewModels()) {
			if (viewModel.view instanceof InteractivityChangedListener) {
				((InteractivityChangedListener) viewModel.view).handleInteractivityChanged(isInteractive);
			}
			if (viewModel.view instanceof CompositeFilterPanel) {
				setInteractive(isInteractive, (CompositeFilterPanel) viewModel.view);
			}
		}
	}

	public void synchronize(FilterPanel panel) {
		setInteractive(panel.getApplyAutomaticallyCheckBox().isSelected(), panel);
	}

	public void handleCancelApply(FilterPanel filterPanel) {
		viewUpdater.cancel();
	}

	public void handleApplyFilter(FilterPanel filterPanel) {
		viewUpdater.requestWork();
	}
}
