package org.cytoscape.filter.internal.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.FilterIO;
import org.cytoscape.filter.internal.ModelMonitor;
import org.cytoscape.filter.internal.ModelUtil;
import org.cytoscape.filter.internal.composite.CompositeFilterPanel;
import org.cytoscape.filter.internal.view.TransformerViewManager.TransformerViewElement;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.view.InteractivityChangedListener;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskManager;

public class FilterPanelController extends AbstractPanelController<FilterElement, FilterPanel> {
	private TransformerManager transformerManager;
	private TransformerViewManager transformerViewManager;
	private IconManager iconManager;
	
	private ModelMonitor modelMonitor;

	public FilterPanelController(TransformerManager transformerManager, TransformerViewManager transformerViewManager,
			FilterWorker worker, ModelMonitor modelMonitor, FilterIO filterIo, TaskManager<?, ?> taskManager,
			IconManager iconManager) {
		super(worker, filterIo, taskManager);
		worker.setController(this);
		
		this.transformerManager = transformerManager;
		this.transformerViewManager = transformerViewManager;
		this.modelMonitor = modelMonitor;
		this.iconManager = iconManager;
		
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
		updateEditPanel(panel);
		root.updateLayout();
		worker.handleFilterStructureChanged();
	}
	
	@Override
	protected void handleSelectAll(FilterPanel panel) {
		CompositeFilterPanel root = panel.getRootPanel();
		root.selectAll();
		updateEditPanel(panel);
	}
	
	@Override
	protected void handleDeselectAll(FilterPanel panel) {
		CompositeFilterPanel root = panel.getRootPanel();
		root.deselectAll();
		updateEditPanel(panel);
	}
	
	@Override
	protected void validateEditPanel(FilterPanel panel) {
		CompositeFilterPanel root = panel.getRootPanel();
		JButton outdentButton = panel.getOutdentButton();
		outdentButton.setEnabled(canOutdent(root));
		
		int totalSelected = root.countSelected();
		int totalUnselected = root.countUnselected();
		
		panel.getIndentButton().setEnabled(totalSelected > 0);
		panel.getDeleteButton().setEnabled(totalSelected > 0);
		panel.getSelectAllButton().setEnabled(totalUnselected > 0);
		panel.getDeselectAllButton().setEnabled(totalSelected > 0);
	}

	public JComponent createView(FilterPanel parent, Filter<CyNetwork, CyIdentifiable> filter, int depth) {
		if (filter instanceof CompositeFilter) {
			return new CompositeFilterPanel(parent, this, (CompositeFilter<CyNetwork, CyIdentifiable>) filter, depth,
					iconManager);
		}
		JComponent view = transformerViewManager.createView(filter);
		if (view instanceof InteractivityChangedListener) {
			((InteractivityChangedListener) view).handleInteractivityChanged(isInteractive);
		}
		return view;
	}

	public JPopupMenu createAddConditionMenu(final CompositeFilterPanel panel, final FilterPanel filterPanel) {
		JPopupMenu menu = new JPopupMenu();
		
		for (final TransformerViewElement element : transformerViewManager.getFilterConditionViewElements()) {
			JMenuItem mi = new JMenuItem(element.toString());
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleAddCondition(element, panel, filterPanel);
				}
			});
			menu.add(mi);
		}
		
		return menu;
	}
	
	private void handleAddCondition(TransformerViewElement element, CompositeFilterPanel panel, FilterPanel filterPanel) {
		// Assume the factory makes filters
		Transformer<CyNetwork, CyIdentifiable> transformer = transformerManager.createTransformer(element.getId());
		Filter<CyNetwork, CyIdentifiable> filter = (Filter<CyNetwork, CyIdentifiable>) transformer;
		panel.addFilter(filter);
		panel.updateLayout();
		
		filter.addListener(worker);
		worker.handleFilterStructureChanged();
		
		validateEditPanel(filterPanel);
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
	
	@Override
	protected String getExportLabel() {
		return "Export filters...";
	}
	
	@Override
	protected String getImportLabel() {
		return "Import filters...";
	}
	
	@Override
	public void addNamedTransformers(final FilterPanel panel, final NamedTransformer<CyNetwork, CyIdentifiable>... namedTransformers) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					addNamedTransformers(panel, namedTransformers);
				}
			});
			return;
		}
		
		for (NamedTransformer<CyNetwork, CyIdentifiable> namedTransformer : namedTransformers) {
			int validated = 0;
			for (Transformer<CyNetwork, CyIdentifiable> transformer : namedTransformer.getTransformers()) {
				if (transformer instanceof Filter) {
					validated++;
				}
			}
			if (validated == 0) {
				continue;
			}
			
			String name = findUniqueName(namedTransformer.getName());
			FilterElement element = addNewElement(name);
			for (Transformer<CyNetwork, CyIdentifiable> transformer : namedTransformer.getTransformers()) {
				if (!(transformer instanceof Filter)) {
					continue;
				}
				Filter<CyNetwork, CyIdentifiable> filter = (Filter<CyNetwork, CyIdentifiable>) transformer;
				element.filter.append(filter);
			}
		}
		FilterElement selected = (FilterElement) namedElementComboBoxModel.getSelectedItem();
		if (selected == null) {
			return;
		}
		setFilter(selected.filter, panel);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public NamedTransformer<CyNetwork, CyIdentifiable>[] getNamedTransformers() {
		DynamicComboBoxModel<FilterElement> model = getElementComboBoxModel();
		
		NamedTransformer<CyNetwork, CyIdentifiable>[] namedTransformers = new NamedTransformer[model.getSize() - 1];
		int i = 0;
		for (FilterElement element : model) {
			if (element.filter == null) {
				continue;
			}
			
			Transformer<CyNetwork, CyIdentifiable>[] transformers = new Transformer[element.filter.getLength()];
			for (int j = 0; j < transformers.length; j++) {
				transformers[j] = element.filter.get(j);
			}
			namedTransformers[i] = (NamedTransformer<CyNetwork, CyIdentifiable>) ModelUtil.createNamedTransformer(element.name, transformers);
			i++;
		}
		return namedTransformers;
	}
}
