package org.cytoscape.filter.internal.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.FilterIO;
import org.cytoscape.filter.internal.ModelUtil;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterController;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterPanel;
import org.cytoscape.filter.internal.filters.composite.CompositeSeparator;
import org.cytoscape.filter.internal.filters.composite.CompositeTransformerPanel;
import org.cytoscape.filter.internal.view.TransformerViewManager.TransformerViewElement;
import org.cytoscape.filter.internal.view.look.FilterPanelStyle;
import org.cytoscape.filter.internal.work.TransformerWorker;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.filter.model.SubFilterTransformer;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.work.TaskManager;

public class TransformerPanelController extends AbstractPanelController<TransformerElement, TransformerPanel> {
	private TransformerManager transformerManager;
	private TransformerViewManager transformerViewManager;
	private DynamicComboBoxModel<FilterElement> startWithComboBoxModel;
	
	public TransformerPanelController(TransformerManager transformerManager, 
			TransformerViewManager transformerViewManager, FilterPanelController filterPanelController, 
			TransformerWorker worker, FilterIO filterIo, TaskManager<?, ?> taskManager, FilterPanelStyle style, IconManager iconManager) {
		super(worker, transformerManager, transformerViewManager, filterIo, taskManager, style, iconManager);
		worker.setController(this);
		
		this.transformerManager = transformerManager;
		this.transformerViewManager = transformerViewManager;

		List<FilterElement> items = new ArrayList<FilterElement>();
		items.add(new FilterElement("Current Selection", null));
		startWithComboBoxModel = new DynamicComboBoxModel<FilterElement>(items);
		filterPanelController.addNamedElementListener(new NamedElementListener<FilterElement>() {
			@Override
			public void handleElementRemoved(FilterElement element) {
				startWithComboBoxModel.remove(element);
			}
			
			@Override
			public void handleElementAdded(FilterElement element) {
				if (element.getFilter() == null) {
					return;
				}
				startWithComboBoxModel.add(element);
			}
		});
		
		addNewElement("Default chain");
	}
	
	@Override
	protected void handleElementSelected(TransformerElement selected, TransformerPanel panel) {
		CompositeTransformerPanel root = new CompositeTransformerPanel(panel, this, selected.getChain(), getIconManager());
		panel.setRootPanel(root);
	}

	@Override
	protected void synchronize(TransformerPanel panel) {
	}

	@Override
	protected TransformerElement createElement(String name) {
		ArrayList<Transformer<CyNetwork, CyIdentifiable>> chain = new ArrayList<>();
		return new TransformerElement(name, chain);
	}

	@Override
	protected String getPrompt() {
		return "Please provide a name for your filter chain.";
	}

	@Override
	protected String getCreateElementTitle() {
		return "Create New Filter Chain";
	}

	@Override
	protected String getRenameElementTitle() {
		return "Rename Filter Chain";
	}

	@Override
	protected String getElementTemplate() {
		return "My chain %1$d";
	}

	@Override
	protected String getElementExistsWarningTemplate() {
		return "The name '%1$s' is already being used by another filter chain.  Please provide a different name.";
	}

	@Override
	protected String getCreateMenuLabel() {
		return "Create new filter chain";
	}
	
	@Override
	protected String getDeleteMenuLabel() {
		return "Remove current filter chain";
	}
	
	@Override
	protected String getRenameMenuLabel() {
		return "Rename current filter chain";
	}
	
	@Override
	protected String getExportLabel() {
		return "Export filter chains...";
	}
	
	@Override
	protected String getImportLabel() {
		return "Import filter chains...";
	}
	
	@Override
	public String getHandleToolTip() {
		return "Drag this chain entry to reorder.";
	}
	
	public JPopupMenu createAddChainEntryMenu(final CompositeTransformerPanel panel, final TransformerPanel transformerPanel) {
		JPopupMenu menu = new JPopupMenu();
		
		for (final TransformerViewElement element : transformerViewManager.getChainTransformerViewElements()) {
			JMenuItem mi = new JMenuItem(element.toString());
			mi.addActionListener(e -> handleAddTransformer(element, panel, transformerPanel));
			menu.add(mi);
		}
		
		return menu;
	}
	
	public ComboBoxModel getStartWithComboBoxModel() {
		return startWithComboBoxModel;
	}

	private void handleAddTransformer(TransformerViewElement element, CompositeTransformerPanel panel, TransformerPanel transformerPanel) {
		Transformer<CyNetwork, CyIdentifiable> transformer = transformerManager.createTransformer(element.getId());
		panel.addTransformer(transformer);
		panel.updateLayout();
	}

	@Override
	public JComponent createView(TransformerPanel parent, Transformer<CyNetwork, CyIdentifiable> transformer, int depth) {
		// CompositeFilterImpl needs a CompositeFilterPanel but the top is blank so view will be null
		JComponent view = transformerViewManager.createView(transformer);
		
		if(transformer instanceof SubFilterTransformer || transformer instanceof CompositeFilter) {
			String addButtonTT = transformerViewManager.getAddButtonTooltip(transformer);
			CompositeFilterController controller = CompositeFilterController.createFor(view, addButtonTT);
			CompositeFilter<CyNetwork,CyIdentifiable> compositeFilter;
			
			if(transformer instanceof SubFilterTransformer)
				compositeFilter = ((SubFilterTransformer<CyNetwork, CyIdentifiable>) transformer).getCompositeFilter();
			else
				compositeFilter = (CompositeFilter<CyNetwork,CyIdentifiable>) transformer;
			
			return new CompositeFilterPanel<TransformerPanel>(parent, this, controller, compositeFilter, depth);
		}
		
		if(view == null)
			throw new IllegalArgumentException("view could not be created for: " + transformer.getId());
		return view;
	}

	public void handleApply(TransformerPanel transformerPanel) {
		worker.requestWork();
	}

	public List<Transformer<CyNetwork, CyIdentifiable>> getTransformers(TransformerPanel panel) {
		CompositeTransformerPanel root = panel.getRootPanel();
		return root.getModel();
	}
	
	@Override
	public void addNamedTransformers(final TransformerPanel panel, final NamedTransformer<CyNetwork, CyIdentifiable>... namedTransformers) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(() -> addNamedTransformers(panel, namedTransformers));
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
				if (!(transformer instanceof Filter)) {
					validated++;
				}
			}
			if (validated == 0) {
				continue;
			}
			
			String name = findUniqueName(namedTransformer.getName());
			TransformerElement element = addNewElement(name);
			for (Transformer<CyNetwork, CyIdentifiable> transformer: namedTransformer.getTransformers()) {
				if (transformer instanceof Filter) {
					continue;
				}
				element.getChain().add(transformer);
			}
		}
		TransformerElement selected = (TransformerElement) namedElementComboBoxModel.getSelectedItem();
		if (selected == null) {
			return;
		}
		handleElementSelected(selected, panel);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public NamedTransformer<CyNetwork, CyIdentifiable>[] getNamedTransformers() {
		DynamicComboBoxModel<TransformerElement> model = getElementComboBoxModel();
		
		NamedTransformer<CyNetwork, CyIdentifiable>[] namedTransformers = new NamedTransformer[model.getSize()];
		int i = 0;
		for (TransformerElement element : model) {
			if (element.getChain() == null) {
				continue;
			}
			
			Transformer<CyNetwork, CyIdentifiable>[] transformers = element.getChain().toArray(new Transformer[element.getChain().size()]);
			namedTransformers[i] = (NamedTransformer<CyNetwork, CyIdentifiable>) ModelUtil.createNamedTransformer(element.name, transformers);
			i++;
		}
		return namedTransformers;
	}
	
	@Override
	public void unregisterView(JComponent elementView) {
	}
	
	
	@Override
	public boolean supportsDrop(TransformerPanel parent, List<Integer> sourcePath, JComponent source, List<Integer> targetPath, JComponent target) {
		if(!super.supportsDrop(parent, sourcePath, source, targetPath, target))
			return false;
		
		boolean sourceIsFilter = sourcePath.size() > 1;
		boolean targetIsFilter = targetPath.size() > 1 || target instanceof CompositeFilterPanel;
		
		if(sourceIsFilter && targetIsFilter)
			return true; // create CompositeFilter or move filters
		if(!sourceIsFilter && !targetIsFilter)
			return true; // reorder top level transformers
		
		return false;
	}
	
	@Override
	public void handleDrop(TransformerPanel parent, JComponent source, List<Integer> sourcePath, JComponent target, List<Integer> targetPath) {
		CompositeTransformerPanel root = parent.getRootPanel();
		try {
			if(sourcePath.size() == 1 && targetPath.size() == 1) {
				int sourceIndex = sourcePath.get(0);
				
				List<Transformer<CyNetwork, CyIdentifiable>> model = root.getModel();
				Transformer<CyNetwork, CyIdentifiable> transformer = model.remove(sourceIndex);
				
				int targetIndex = targetPath.get(targetPath.size() - 1) + 1;
				if (sourceIndex < targetIndex) {
					targetIndex--;
				}
				model.add(targetIndex, transformer);
			}
			else {
				super.handleFilterDrop(parent, source, sourcePath, target, targetPath);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			root.updateLayout();
		}
	}
	
	@Override
	public boolean isDropMove(TransformerPanel view, JComponent source, List<Integer> sourcePath, JComponent target, List<Integer> targetPath) {
		if(sourcePath.size() == 1 && targetPath.size() == 1)
			return true;
		
		return target instanceof CompositeSeparator || target instanceof CompositeFilterPanel;
	}
}
