package org.cytoscape.filter.internal.view;

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
import org.cytoscape.filter.internal.work.FilterWorker;
import org.cytoscape.filter.internal.work.ValidationManager;
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
	
	private boolean isInteractive;

	public FilterPanelController(
			TransformerManager transformerManager, 
			TransformerViewManager transformerViewManager,
			ValidationManager validationManager,
			FilterWorker worker, 
			ModelMonitor modelMonitor, 
			FilterIO filterIo, 
			TaskManager<?, ?> taskManager,
			FilterPanelStyle style, 
			IconManager iconManager) 
	{
		super(worker, transformerManager, transformerViewManager, validationManager, filterIo, taskManager, style, iconManager);
		worker.setController(this);
		
		this.transformerManager = transformerManager;
		this.transformerViewManager = transformerViewManager;
		this.modelMonitor = modelMonitor;
		
		worker.setController(this);
		addNewElement("Default filter");
	}

	@Override
	public JComponent createView(FilterPanel parent, Transformer<CyNetwork, CyIdentifiable> filter, int depth) {
		// CompositeFilterImpl needs a CompositeFilterPanel but the top is blank so view will be null
		JComponent view = transformerViewManager.createView(filter);
		
		if (view instanceof InteractivityChangedListener) {
			((InteractivityChangedListener) view).handleInteractivityChanged(isInteractive);
		}
		
		if (filter instanceof CompositeFilter) {
			String addButtonTT = transformerViewManager.getAddButtonTooltip(filter);
			CompositeFilterController controller = CompositeFilterController.createFor(view, addButtonTT);
			return new CompositeFilterPanel<FilterPanel>(parent, this, controller, (CompositeFilter<CyNetwork, CyIdentifiable>) filter, depth);
		}
		
		if(view == null)
			throw new IllegalArgumentException("view could not be created for: " + filter.getId());
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
		setFilter(selected.getFilter(), panel);
		worker.handleFilterStructureChanged();
	}
	
	private void setFilter(CompositeFilter<CyNetwork, CyIdentifiable> filter, FilterPanel parent) {
		@SuppressWarnings("unchecked")
		CompositeFilterPanel<FilterPanel> root = (CompositeFilterPanel<FilterPanel>) createView(parent, filter, 0);
		new TransformerElementViewModel<>(root, this, parent);
		parent.setRootPanel(root);
	}

	public Filter<CyNetwork, CyIdentifiable> getFilter() {
		FilterElement selected = (FilterElement) namedElementComboBoxModel.getSelectedItem();
		return selected.getFilter();
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
	}

	@SuppressWarnings("unchecked")
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
	public void addNamedTransformers(final FilterPanel panel, @SuppressWarnings("unchecked") final NamedTransformer<CyNetwork, CyIdentifiable>... namedTransformers) {
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
		setFilter(selected.getFilter(), panel);
	}
	
	private void addCompositeFilter(FilterElement element, CompositeFilter<CyNetwork, CyIdentifiable> composite) {
		element.getFilter().setType(composite.getType());
		for (int i = 0; i < composite.getLength(); i++) {
			Filter<CyNetwork, CyIdentifiable> filter = composite.get(i);
			addListeners(filter);
			element.getFilter().append(filter);
		}
	}

	private void addTransformers(FilterElement element, List<Transformer<CyNetwork, CyIdentifiable>> transformers) {
		for (Transformer<CyNetwork, CyIdentifiable> transformer : transformers) {
			if (!(transformer instanceof Filter)) {
				continue;
			}
			Filter<CyNetwork, CyIdentifiable> filter = (Filter<CyNetwork, CyIdentifiable>) transformer;
			addListeners(filter);
			element.getFilter().append(filter);
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
			if (element.getFilter() == null) {
				continue;
			}
			
			Transformer<CyNetwork, CyIdentifiable>[] transformers = new Transformer[] { element.getFilter() };
			namedTransformers[i] = (NamedTransformer<CyNetwork, CyIdentifiable>) ModelUtil.createNamedTransformer(element.name, transformers);
			i++;
		}
		return namedTransformers;
	}
	
	@Override
	public void unregisterView(JComponent elementView) {
		modelMonitor.unregisterView(elementView);
	}
	
	
	/**
	 * The root panel is different for FilterPanel and TransformerPanel, so these need to be
	 * handled separately.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void handleDrop(FilterPanel parent, JComponent source, List<Integer> sourcePath, JComponent target, List<Integer> targetPath) {
		CompositeFilterPanel<FilterPanel> root = parent.getRootPanel();
		CompositeFilterPanel<FilterPanel> sourceParent = (CompositeFilterPanel<FilterPanel>) source.getParent();
		try {
			if (targetPath.isEmpty()) {
				int sourceIndex = sourcePath.get(sourcePath.size() - 1);
				Filter<CyNetwork, CyIdentifiable> filter = sourceParent.getTransformerAt(sourceIndex);
				TransformerElementViewModel<FilterPanel> viewModel = sourceParent.getViewModel(filter);
				sourceParent.removeTransformer(sourceIndex, false);
				// Target is end of root
				root.addViewModel(filter, viewModel);
			}
			else {
				super.handleFilterDrop(parent, source, sourcePath, target, targetPath);
			}
		} finally {
			root.setDepth(0);
			root.updateLayout();
			worker.handleFilterStructureChanged();
		}
	}

	
	
	@Override
	public void handleDelete(FilterPanel view, JComponent component) {
		try {
			super.handleDelete(view, component);
		} finally {
			worker.handleFilterStructureChanged();
		}
	}
	
	@Override
	public boolean isDropMove(FilterPanel view, JComponent source, List<Integer> sourcePath, JComponent target, List<Integer> targetPath) {
		return target instanceof CompositeSeparator || target instanceof CompositeFilterPanel;
	}
}
