package org.cytoscape.filter.internal.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.FilterIO;
import org.cytoscape.filter.internal.ModelUtil;
import org.cytoscape.filter.internal.filters.composite.CompositeSeparator;
import org.cytoscape.filter.internal.filters.composite.CompositeTransformerPanel;
import org.cytoscape.filter.internal.view.TransformerViewManager.TransformerViewElement;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.work.TaskManager;

public class TransformerPanelController extends AbstractPanelController<TransformerElement, TransformerPanel> {
	private TransformerManager transformerManager;
	private TransformerViewManager transformerViewManager;
	private IconManager iconManager;
	private DynamicComboBoxModel<FilterElement> startWithComboBoxModel;
	
	public TransformerPanelController(TransformerManager transformerManager, 
			TransformerViewManager transformerViewManager, FilterPanelController filterPanelController, 
			TransformerWorker worker, FilterIO filterIo, TaskManager<?, ?> taskManager, IconManager iconManager) {
		super(worker, filterIo, taskManager);
		worker.setController(this);
		
		this.transformerManager = transformerManager;
		this.transformerViewManager = transformerViewManager;
		this.iconManager = iconManager;

		List<FilterElement> items = new ArrayList<FilterElement>();
		items.add(new FilterElement("(Current Selection)", null));
		startWithComboBoxModel = new DynamicComboBoxModel<FilterElement>(items);
		filterPanelController.addNamedElementListener(new NamedElementListener<FilterElement>() {
			@Override
			public void handleElementRemoved(FilterElement element) {
				startWithComboBoxModel.remove(element);
			}
			
			@Override
			public void handleElementAdded(FilterElement element) {
				if (element.filter == null) {
					return;
				}
				startWithComboBoxModel.add(element);
			}
		});
		
		addNewElement("Default chain");
	}
	
	@Override
	protected void handleElementSelected(TransformerElement selected, TransformerPanel panel) {
		CompositeTransformerPanel root = new CompositeTransformerPanel(panel, this, selected.chain, iconManager);
		panel.setRootPanel(root);
	}

	@Override
	protected void synchronize(TransformerPanel panel) {
	}

	@Override
	protected TransformerElement createElement(String name) {
		ArrayList<Transformer<CyNetwork, CyIdentifiable>> chain = new ArrayList<Transformer<CyNetwork, CyIdentifiable>>();
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
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleAddTransformer(element, panel, transformerPanel);
				}
			});
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

	public JComponent createView(TransformerPanel transformerPanel, Transformer<CyNetwork, CyIdentifiable> transformer) {
		return transformerViewManager.createView(transformer);
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
				if (!(transformer instanceof Filter)) {
					validated++;
				}
			}
			if (validated == 0) {
				continue;
			}
			
			String name = findUniqueName(namedTransformer.getName());
			TransformerElement element = addNewElement(name);
			element.chain = new ArrayList<Transformer<CyNetwork,CyIdentifiable>>();
			for (Transformer<CyNetwork, CyIdentifiable> transformer: namedTransformer.getTransformers()) {
				if (transformer instanceof Filter) {
					continue;
				}
				element.chain.add(transformer);
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
			if (element.chain == null) {
				continue;
			}
			
			Transformer<CyNetwork, CyIdentifiable>[] transformers = element.chain.toArray(new Transformer[element.chain.size()]);
			namedTransformers[i] = (NamedTransformer<CyNetwork, CyIdentifiable>) ModelUtil.createNamedTransformer(element.name, transformers);
			i++;
		}
		return namedTransformers;
	}
	
	@Override
	public void unregisterView(JComponent elementView) {
	}
	
	
	@Override
	public List<Integer> getPath(TransformerPanel view, JComponent component) {
		CompositeTransformerPanel root = view.getRootPanel();
		if (root == component) {
			return Collections.emptyList();
		}
		
		List<Transformer<CyNetwork, CyIdentifiable>> model = root.getModel();
		int index = 0;
		if (root.getSeparator() == component) {
			return Collections.singletonList(-1);
		}
		for (Transformer<CyNetwork, CyIdentifiable> transformer : model) {
			TransformerElementViewModel<TransformerPanel> viewModel = root.getViewModel(transformer);
			if (viewModel.view == component || viewModel.separator == component || viewModel.handle == component) {
				return Collections.singletonList(index);
			}
			index++;
		}
		return null;
	}
	
	@Override
	public JComponent getChild(TransformerPanel view, List<Integer> path) {
		CompositeTransformerPanel root = view.getRootPanel();
		if (path.isEmpty()) {
			return root;
		}
		if (path.size() > 1) {
			return null;
		}
		Transformer<CyNetwork, CyIdentifiable> transformer = root.getModel().get(path.get(0));
		TransformerElementViewModel<TransformerPanel> viewModel = root.getViewModel(transformer);
		return viewModel.view;
	}
	
	@Override
	public boolean supportsDrop(TransformerPanel parent, List<Integer> sourcePath, JComponent source, List<Integer> targetPath, JComponent target) {
		return target instanceof CompositeSeparator;
	}
	
	@Override
	public void handleDrop(TransformerPanel parent, JComponent source, List<Integer> sourcePath, JComponent target, List<Integer> targetPath) {
		CompositeTransformerPanel root = parent.getRootPanel();
		try {
			int sourceIndex = sourcePath.get(sourcePath.size() - 1);
			List<Transformer<CyNetwork, CyIdentifiable>> model = root.getModel();
			Transformer<CyNetwork, CyIdentifiable> transformer = model.remove(sourceIndex);
			
			int targetIndex = targetPath.get(targetPath.size() - 1) + 1;
			if (sourceIndex < targetIndex) {
				targetIndex--;
			}
			model.add(targetIndex, transformer);
		} finally {
			root.updateLayout();
		}
	}
	
	@Override
	public void handleDelete(TransformerPanel view, JComponent component) {
		List<Integer> path = getPath(view, component);
		CompositeTransformerPanel root = view.getRootPanel();
		int index = path.get(path.size() - 1);
		root.removeTransformer(index);
		
		root.updateLayout();
	}
}
