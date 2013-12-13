package org.cytoscape.filter.internal.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.FilterIO;
import org.cytoscape.filter.internal.ModelMonitor;
import org.cytoscape.filter.internal.ModelUtil;
import org.cytoscape.filter.internal.composite.CompositeFilterPanel;
import org.cytoscape.filter.internal.composite.CompositeSeparator;
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
		new TransformerElementViewModel<FilterPanel>(root, this, parent, iconManager);
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
	protected String getCreateMenuLabel() {
		return "Create new filter";
	}
	
	@Override
	protected String getDeleteMenuLabel() {
		return "Remove current filter";
	}
	
	@Override
	protected String getRenameMenuLabel() {
		return "Rename current filter";
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
	public String getDeleteContextMenuLabel() {
		return "Delete this condition";
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
	
	@Override
	public void unregisterView(JComponent elementView) {
		modelMonitor.unregisterView(elementView);
	}
	
	@Override
	public List<Integer> getPath(FilterPanel view, JComponent component) {
		if (component == view.getRootPanel()) {
			return Collections.emptyList();
		}
		
		List<Integer> path = new ArrayList<Integer>();
		Component current = component;
		Container nextParent = component.getParent();
		while (true) {
			if (!(nextParent instanceof CompositeFilterPanel)) {
				break;
			}
			CompositeFilterPanel composite = (CompositeFilterPanel) nextParent;
			if (current == composite.getSeparator()) {
				path.add(0, -1);
			} else {
				CompositeFilter<CyNetwork, CyIdentifiable> compositeFilter = composite.getModel();
				boolean found = false;
				for (int i = 0; i < compositeFilter.getLength(); i++) {
					Filter<CyNetwork, CyIdentifiable> filter = compositeFilter.get(i);
					TransformerElementViewModel<FilterPanel> viewModel = composite.getViewModel(filter);
					if (current == viewModel.view || current == viewModel.separator || current == viewModel.handle) {
						path.add(0, i);
						found = true;
						break;
					}
				}
				
				if (!found) {
					return null;
				}
			}
			current = nextParent;
			nextParent = nextParent.getParent();
		}
		if (path.size() == 0) {
			return null;
		}
		return path;
	}
	
	@Override
	public JComponent getChild(FilterPanel view, List<Integer> path) {
		CompositeFilterPanel panel = view.getRootPanel();
		if (path.size() == 0) {
			return panel;
		}
		
		CompositeFilter<CyNetwork,CyIdentifiable> composite = panel.getModel();
		Filter<CyNetwork, CyIdentifiable> child = null;
		JComponent lastView = null;
		for (int index : path) {
			if (composite == null) {
				return null;
			}
			
			child = composite.get(index);
			lastView = panel.getViewModel(child).view;
			if (child instanceof CompositeFilter) {
				panel = (CompositeFilterPanel) lastView;
				composite = (CompositeFilter<CyNetwork, CyIdentifiable>) child;
			} else {
				composite = null;
			}
		}
		
		if (child == null) {
			return null;
		}
		return lastView;
	}

	@Override
	public boolean supportsDrop(FilterPanel parent, JComponent source, JComponent target) {
		return !(source.getParent() == target || isParentOrSelf(source, target));
	}
	
	@Override
	public void handleDrop(FilterPanel parent, JComponent source, List<Integer> sourcePath, JComponent target, List<Integer> targetPath) {
		CompositeFilterPanel root = parent.getRootPanel();
		CompositeFilterPanel sourceParent = (CompositeFilterPanel) source.getParent();
		try {
			int sourceIndex = sourcePath.get(sourcePath.size() - 1);
			Filter<CyNetwork, CyIdentifiable> filter = sourceParent.getModel().get(sourceIndex);
			TransformerElementViewModel<FilterPanel> viewModel = sourceParent.getViewModel(filter);
			sourceParent.removeFilter(sourceIndex);
			
			if (targetPath.size() == 0) {
				// Target is end of root
				root.addViewModel(filter, viewModel);
				return;
			}
			
			CompositeFilterPanel targetParent = (CompositeFilterPanel) target.getParent();
			
			if (target instanceof CompositeSeparator || target instanceof CompositeFilterPanel) {
				// Drop causes a move
				int targetIndex = targetPath.get(targetPath.size() - 1) + 1;
				if (sourcePath.size() == targetPath.size()) {
					// Source and target have same parent.  Need to adjust
					// Indices to account for removal of source
					if (sourceIndex < targetIndex) {
						targetIndex--;
					}
				}
				targetParent.addViewModel(targetIndex, filter, viewModel);
			} else {
				// Drop causes grouping
				int targetIndex = targetPath.get(targetPath.size() - 1);
				if (sourceIndex < targetIndex) {
					targetIndex--;
				}
				Filter<CyNetwork, CyIdentifiable> targetFilter = targetParent.getModel().get(targetIndex);
				TransformerElementViewModel<FilterPanel> targetViewModel = targetParent.getViewModel(targetFilter);
				targetParent.removeFilter(targetIndex);
				
				CompositeFilter<CyNetwork, CyIdentifiable> group = transformerManager.createCompositeFilter(CyNetwork.class, CyIdentifiable.class);
				group.addListener(worker);
				
				CompositeFilterPanel groupView = (CompositeFilterPanel) createView(parent, group, targetParent.getDepth() + 1);
				TransformerElementViewModel<FilterPanel> groupViewModel = new TransformerElementViewModel<FilterPanel>(groupView, this, parent, iconManager);
				targetParent.addViewModel(targetIndex, group, groupViewModel);
				
				groupView.addViewModel(targetFilter, targetViewModel);
				groupView.addViewModel(filter, viewModel);
			}
		} finally {
			// Delete chains of CompositeFilters that don't have
			// any real filters.
			removeOrphans(sourceParent);
			root.setDepth(0);
			root.updateLayout();
			worker.handleFilterStructureChanged();
		}
	}

	private void removeOrphans(CompositeFilterPanel panel) {
		CompositeFilter<CyNetwork, CyIdentifiable> model = panel.getModel();
		if (model.getLength() > 0) {
			return;
		}
		
		Container parent = panel.getParent();
		if (!(parent instanceof CompositeFilterPanel)) {
			return;
		}
		
		CompositeFilterPanel parentPanel = (CompositeFilterPanel) parent;
		CompositeFilter<CyNetwork, CyIdentifiable> parentModel = parentPanel.getModel();
		for (int index = 0; index < parentModel.getLength(); index++) {
			Filter<CyNetwork, CyIdentifiable> filter = parentModel.get(index);
			if (model == filter) {
				parentPanel.removeFilter(index);
				removeOrphans(parentPanel);
				return;
			}
		}
	}
	
	@Override
	public void handleContextMenuDelete(FilterPanel view) {
		List<Integer> path = lastSelectedPath;
		if (path == null) {
			return;
		}
		
		JComponent component = getChild(view, lastSelectedPath);
		if (component == null) {
			return;
		}
		
		CompositeFilterPanel parent = (CompositeFilterPanel) component.getParent();
		int sourceIndex = path.get(path.size() - 1);
		parent.removeFilter(sourceIndex);
		
		CompositeFilterPanel root = view.getRootPanel();
		root.updateLayout();
		worker.handleFilterStructureChanged();
	}
}
