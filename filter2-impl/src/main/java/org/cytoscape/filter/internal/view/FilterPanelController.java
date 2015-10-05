package org.cytoscape.filter.internal.view;

import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.FilterIO;
import org.cytoscape.filter.internal.ModelMonitor;
import org.cytoscape.filter.internal.ModelUtil;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterController;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterPanel;
import org.cytoscape.filter.internal.filters.composite.CompositeSeparator;
import org.cytoscape.filter.internal.view.look.FilterPanelStyle;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.view.InteractivityChangedListener;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.work.TaskManager;

public class FilterPanelController extends AbstractPanelController<FilterElement, FilterPanel> {
	private TransformerManager transformerManager;
	private TransformerViewManager transformerViewManager;
	
	private ModelMonitor modelMonitor;

	public FilterPanelController(TransformerManager transformerManager, TransformerViewManager transformerViewManager,
			FilterWorker worker, ModelMonitor modelMonitor, FilterIO filterIo, TaskManager<?, ?> taskManager,
			FilterPanelStyle style, IconManager iconManager) {
		super(worker, transformerManager, transformerViewManager, filterIo, taskManager, style, iconManager);
		worker.setController(this);
		
		this.transformerManager = transformerManager;
		this.transformerViewManager = transformerViewManager;
		this.modelMonitor = modelMonitor;
		
		worker.setController(this);
		addNewElement("Default filter");
	}

	@Override
	public JComponent createView(FilterPanel parent, Transformer<CyNetwork, CyIdentifiable> filter, int depth) {
		// view will be null for CompositeFilterImpl and that's ok
		JComponent view = transformerViewManager.createView(filter);
		
		if (view instanceof InteractivityChangedListener) {
			((InteractivityChangedListener) view).handleInteractivityChanged(isInteractive);
		}
		
		if (filter instanceof CompositeFilter) {
			final String addButtonTT = transformerViewManager.getAddButtonTooltip(filter);
			CompositeFilterController controller = CompositeFilterController.createFor(view, addButtonTT);
			return new CompositeFilterPanel<FilterPanel>(parent, this, controller, (CompositeFilter<CyNetwork, CyIdentifiable>) filter, depth);
		}
		
		return view;
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
		CompositeFilterPanel<FilterPanel> root = (CompositeFilterPanel<FilterPanel>) createView(parent, filter, 0);
		new TransformerElementViewModel<FilterPanel>(root, this, parent);
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
		CompositeFilterPanel<FilterPanel> root = panel.getRootPanel();
		setInteractive(isInteractive, root);
		setProgress(1.0, panel);
	}

	private void setInteractive(boolean isInteractive, CompositeFilterPanel<FilterPanel> panel) {
		for (TransformerElementViewModel<FilterPanel> viewModel : panel.getViewModels()) {
			if (viewModel.view instanceof InteractivityChangedListener) {
				((InteractivityChangedListener) viewModel.view).handleInteractivityChanged(isInteractive);
			}
			if (viewModel.view instanceof CompositeFilterPanel) {
				setInteractive(isInteractive, (CompositeFilterPanel<FilterPanel>) viewModel.view);
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
	public String getHandleToolTip() {
		return "Drag this condition to another condition to group them, or drop it in a gap to reorder.";
	}
	
	@Override
	public void addNamedTransformers(final FilterPanel panel, final NamedTransformer<CyNetwork, CyIdentifiable>... namedTransformers) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						addNamedTransformers(panel, namedTransformers);
					}
				});
			} catch (InterruptedException e) {
				logger.error("An unexpected error occurred", e);
			} catch (InvocationTargetException e) {
				logger.error("An unexpected error occurred", e);
			}
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
			List<Transformer<CyNetwork, CyIdentifiable>> transformers = namedTransformer.getTransformers();
			if (transformers.size() == 1) {
				Transformer<CyNetwork, CyIdentifiable> first = transformers.get(0);
				if (first instanceof CompositeFilter) {
					addCompositeFilter(element, (CompositeFilter<CyNetwork, CyIdentifiable>) first);
				} else {
					addTransformers(element, transformers);
				}
			} else {
				addTransformers(element, transformers);
			}
		}
		FilterElement selected = (FilterElement) namedElementComboBoxModel.getSelectedItem();
		if (selected == null) {
			return;
		}
		setFilter(selected.filter, panel);
	}
	
	private void addCompositeFilter(FilterElement element, CompositeFilter<CyNetwork, CyIdentifiable> composite) {
		element.filter.setType(composite.getType());
		for (int i = 0; i < composite.getLength(); i++) {
			Filter<CyNetwork, CyIdentifiable> filter = composite.get(i);
			addListeners(filter);
			element.filter.append(filter);
		}
	}

	private void addTransformers(FilterElement element, List<Transformer<CyNetwork, CyIdentifiable>> transformers) {
		for (Transformer<CyNetwork, CyIdentifiable> transformer : transformers) {
			if (!(transformer instanceof Filter)) {
				continue;
			}
			Filter<CyNetwork, CyIdentifiable> filter = (Filter<CyNetwork, CyIdentifiable>) transformer;
			addListeners(filter);
			element.filter.append(filter);
		}
	}
	
	private void addListeners(Filter<CyNetwork, CyIdentifiable> filter) {
		filter.addListener(worker);
		if (filter instanceof CompositeFilter) {
			CompositeFilter<CyNetwork, CyIdentifiable> composite = (CompositeFilter<CyNetwork, CyIdentifiable>) filter;
			for (int i = 0; i < composite.getLength(); i++) {
				addListeners(composite.get(i));
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public NamedTransformer<CyNetwork, CyIdentifiable>[] getNamedTransformers() {
		DynamicComboBoxModel<FilterElement> model = getElementComboBoxModel();
		
		NamedTransformer<CyNetwork, CyIdentifiable>[] namedTransformers = new NamedTransformer[model.getSize()];
		int i = 0;
		for (FilterElement element : model) {
			if (element.filter == null) {
				continue;
			}
			
			Transformer<CyNetwork, CyIdentifiable>[] transformers = new Transformer[] { element.filter };
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
	public boolean supportsDrop(FilterPanel parent, List<Integer> sourcePath, JComponent source, List<Integer> targetPath, JComponent target) {
		boolean droppingInParent = source.getParent() == target;
		boolean droppingAtEnd;
		if (target instanceof CompositeFilterPanel) {
			droppingAtEnd = ((CompositeFilterPanel) target).getModel().getLength() - 1 == sourcePath.get(sourcePath.size() - 1);
		} else {
			droppingAtEnd = false;
		}
		return !((droppingInParent && droppingAtEnd) || isParentOrSelf(source, target));
	}
	
	@Override
	public void handleDrop(FilterPanel parent, JComponent source, List<Integer> sourcePath, JComponent target, List<Integer> targetPath) {
		CompositeFilterPanel root = parent.getRootPanel();
		CompositeFilterPanel sourceParent = (CompositeFilterPanel) source.getParent();
		try {
			int sourceIndex = sourcePath.get(sourcePath.size() - 1);
			Filter<CyNetwork, CyIdentifiable> filter = sourceParent.getModel().get(sourceIndex);
			TransformerElementViewModel<FilterPanel> viewModel = sourceParent.getViewModel(filter);
			sourceParent.removeFilter(sourceIndex, false);
			
			if (targetPath.size() == 0) {
				// Target is end of root
				root.addViewModel(filter, viewModel);
				return;
			}
			
			CompositeFilterPanel targetParent = (CompositeFilterPanel) target.getParent();
			
			if (target instanceof CompositeSeparator) {
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
			} else if (target instanceof CompositeFilterPanel) {
				((CompositeFilterPanel) target).addViewModel(filter, viewModel);
			} else {
				// Drop causes grouping
				int targetIndex = targetPath.get(targetPath.size() - 1);
				if (sourceIndex < targetIndex) {
					targetIndex--;
				}
				Filter<CyNetwork, CyIdentifiable> targetFilter = targetParent.getModel().get(targetIndex);
				TransformerElementViewModel<FilterPanel> targetViewModel = targetParent.getViewModel(targetFilter);
				targetParent.removeFilter(targetIndex, false);
				
				CompositeFilter<CyNetwork, CyIdentifiable> group = transformerManager.createCompositeFilter(CyNetwork.class, CyIdentifiable.class);
				group.addListener(worker);
				
				CompositeFilterPanel groupView = (CompositeFilterPanel) createView(parent, group, targetParent.getDepth() + 1);
				TransformerElementViewModel<FilterPanel> groupViewModel = new TransformerElementViewModel<FilterPanel>(groupView, this, parent);
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
				parentPanel.removeFilter(index, true);
				removeOrphans(parentPanel);
				return;
			}
		}
	}
	
	@Override
	public void handleDelete(FilterPanel view, JComponent component) {
		if (component == null) {
			return;
		}
		
		List<Integer> path = getPath(view, component);
		CompositeFilterPanel parent = (CompositeFilterPanel) component.getParent();
		int sourceIndex = path.get(path.size() - 1);
		parent.removeFilter(sourceIndex, true);
		
		CompositeFilterPanel root = view.getRootPanel();
		root.updateLayout();
		worker.handleFilterStructureChanged();
	}
}
