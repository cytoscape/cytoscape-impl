package org.cytoscape.filter.internal.view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.ModelMonitor;
import org.cytoscape.filter.internal.composite.CompositeFilterPanel;
import org.cytoscape.filter.internal.view.TransformerViewManager.TransformerComboBoxElement;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.view.InteractivityChangedListener;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class FilterPanelController extends AbstractPanelController<FilterElement, FilterPanel> {
	private TransformerManager transformerManager;
	private TransformerViewManager transformerViewManager;
	
	private ModelMonitor modelMonitor;

	public FilterPanelController(TransformerManager transformerManager, TransformerViewManager transformerViewManager, FilterWorker worker, ModelMonitor modelMonitor) {
		super(worker);
		worker.setController(this);
		
		this.transformerManager = transformerManager;
		this.transformerViewManager = transformerViewManager;
		this.modelMonitor = modelMonitor;
		
		worker.setController(this);
		addNewElement("Default filter");
	}

	@Override
	protected FilterElement createDefaultElement() {
		return new FilterElement("(Create New Filter...)", null);
	}
	
	void handleIndent(FilterPanel panel) {
		CompositeFilterPanel root = panel.getRootPanel();
		indent(panel, root);
		root.setDepth(0);
		root.updateLayout();
		updateEditPanel(panel);
		worker.handleFilterStructureChanged();
	}
	
	private void indent(FilterPanel parent, CompositeFilterPanel panel) {
		CompositeFilterPanel view = null;
		
		CompositeFilter<CyNetwork, CyIdentifiable> model = panel.getModel();
		int index = 0;
		while (index < model.getLength()) {
			Filter<CyNetwork, CyIdentifiable> filter = model.get(index);
			TransformerElementViewModel<FilterPanel> viewModel = panel.getViewModel(filter);
			if (viewModel.view instanceof CompositeFilterPanel) {
				indent(parent, (CompositeFilterPanel) viewModel.view);
			}
			if (viewModel.checkBox.isSelected()) {
				panel.removeFilter(index--);
				
				if (view == null) {
					CompositeFilter<CyNetwork, CyIdentifiable> childModel = transformerManager.createCompositeFilter(CyNetwork.class, CyIdentifiable.class);
					view = (CompositeFilterPanel) createView(parent, childModel, panel.getDepth() + 1);
					TransformerElementViewModel<FilterPanel> groupModel = new TransformerElementViewModel<FilterPanel>(view, this, parent);
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
		worker.handleFilterStructureChanged();
	}
	
	Map<Filter<CyNetwork, CyIdentifiable>, TransformerElementViewModel<FilterPanel>> outdent(CompositeFilterPanel panel, int depth) {
		Map<Filter<CyNetwork, CyIdentifiable>, TransformerElementViewModel<FilterPanel>> outdented = new LinkedHashMap<Filter<CyNetwork,CyIdentifiable>, TransformerElementViewModel<FilterPanel>>();
		CompositeFilter<CyNetwork, CyIdentifiable> model = panel.getModel();
		int index = 0;
		while (index < model.getLength()) {
			Filter<CyNetwork, CyIdentifiable> filter = model.get(index);
			TransformerElementViewModel<FilterPanel> viewModel = panel.getViewModel(filter);
			if (filter instanceof CompositeFilter) {
				CompositeFilter<CyNetwork, CyIdentifiable> child = (CompositeFilter<CyNetwork, CyIdentifiable>) filter;
				CompositeFilterPanel childPanel = (CompositeFilterPanel) viewModel.view;
				Map<Filter<CyNetwork, CyIdentifiable>, TransformerElementViewModel<FilterPanel>> toAdd = outdent(childPanel, depth + 1);
				if (child.getLength() == 0) {
					panel.removeFilter(index);
				}
				for (Entry<Filter<CyNetwork, CyIdentifiable>, TransformerElementViewModel<FilterPanel>> entry : toAdd.entrySet()) {
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
		for (TransformerElementViewModel<FilterPanel> viewModel : panel.getViewModels()) {
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
	
	@Override
	protected void handleDelete(FilterPanel panel) {
		CompositeFilterPanel root = panel.getRootPanel();
		root.deleteSelected();
		totalSelected = 0;
		updateEditPanel(panel);
		root.updateLayout();
		worker.handleFilterStructureChanged();
	}
	
	@Override
	protected void handleCancel(FilterPanel panel) {
		CompositeFilterPanel root = panel.getRootPanel();
		root.deselectAll();
		totalSelected = 0;
		updateEditPanel(panel);
	}
	
	@Override
	protected void validateEditPanel(FilterPanel panel) {
		CompositeFilterPanel root = panel.getRootPanel();
		JButton outdentButton = panel.getOutdentButton();
		outdentButton.setEnabled(canOutdent(root));
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
		
		TransformerComboBoxElement selectedItem = (TransformerComboBoxElement) comboBox.getSelectedItem();
		
		// Assume the factory makes filters
		Transformer<CyNetwork, CyIdentifiable> transformer = transformerManager.createTransformer(selectedItem.getId());
		Filter<CyNetwork, CyIdentifiable> filter = (Filter<CyNetwork, CyIdentifiable>) transformer;
		panel.addFilter(filter);
		panel.updateLayout();
		comboBox.setSelectedIndex(0);
		
		filter.addListener(worker);
		worker.handleFilterStructureChanged();
	}

	public ComboBoxModel createFilterComboBoxModel() {
		return new DynamicComboBoxModel<TransformerComboBoxElement>(transformerViewManager.getFilterComboBoxModel());
	}

	@Override
	protected FilterElement createElement(String name) {
		CompositeFilter<CyNetwork, CyIdentifiable> filter = transformerManager.createCompositeFilter(CyNetwork.class, CyIdentifiable.class);
		filter.addListener(worker);
		return new FilterElement(name, filter);
	}

	@Override
	protected void handleElementSelected(FilterElement selected, FilterPanel panel) {
		setFilter(selected.filter, panel);
		worker.handleFilterStructureChanged();
	}
	
	private void setFilter(CompositeFilter<CyNetwork, CyIdentifiable> filter, FilterPanel parent) {
		CompositeFilterPanel root = (CompositeFilterPanel) createView(parent, filter, 0);
		parent.setRootPanel(root);
	}

	public Filter<CyNetwork, CyIdentifiable> getFilter() {
		FilterElement selected = (FilterElement) namedElementComboBoxModel.getSelectedItem();
		return selected.filter;
	}

	public void setInteractive(boolean isInteractive, FilterPanel panel) {
		modelMonitor.setInteractive(isInteractive);
		worker.setInteractive(isInteractive);
		if (this.isInteractive == isInteractive) {
			return;
		}
		
		panel.getApplyAutomaticallyCheckBox().setSelected(isInteractive);
		
		this.isInteractive = isInteractive;
		CompositeFilterPanel root = panel.getRootPanel();
		setInteractive(isInteractive, root);
		setProgress(1.0, panel);
	}

	private void setInteractive(boolean isInteractive, CompositeFilterPanel panel) {
		for (TransformerElementViewModel<FilterPanel> viewModel : panel.getViewModels()) {
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

	@Override
	protected String getPrompt() {
		return "Please provide a name for your filter.";
	}
	
	@Override
	protected String getCreateElementTitle() {
		return "Create New Filter";
	}
	
	@Override
	protected String getRenameElementTitle() {
		return "Rename Filter";
	}
	
	@Override
	protected String getElementTemplate() {
		return "My filter %1$d";
	}
	
	@Override
	protected String getElementExistsWarningTemplate() {
		return "The name '%1$s' is already being used by another filter.  Please provide a different name.";
	}
}
