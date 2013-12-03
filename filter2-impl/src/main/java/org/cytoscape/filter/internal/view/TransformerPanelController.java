package org.cytoscape.filter.internal.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.FilterIO;
import org.cytoscape.filter.internal.ModelUtil;
import org.cytoscape.filter.internal.composite.CompositeTransformerPanel;
import org.cytoscape.filter.internal.view.TransformerViewManager.TransformerComboBoxElement;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskManager;

public class TransformerPanelController extends AbstractPanelController<TransformerElement, TransformerPanel> {
	private TransformerManager transformerManager;
	private TransformerViewManager transformerViewManager;
	private DynamicComboBoxModel<FilterElement> startWithComboBoxModel;
	
	public TransformerPanelController(TransformerManager transformerManager, TransformerViewManager transformerViewManager, FilterPanelController filterPanelController, TransformerWorker worker, FilterIO filterIo, TaskManager<?, ?> taskManager) {
		super(worker, filterIo, taskManager);
		worker.setController(this);
		
		this.transformerManager = transformerManager;
		this.transformerViewManager = transformerViewManager;

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
	protected TransformerElement createDefaultElement() {
		return new TransformerElement("(Create New Chain...)", null);
	}

	@Override
	protected void handleDelete(TransformerPanel panel) {
		CompositeTransformerPanel root = panel.getRootPanel();
		root.deleteSelected();
		updateEditPanel(panel);
		root.updateLayout();
	}

	@Override
	protected void handleSelectAll(TransformerPanel panel) {
		CompositeTransformerPanel root = panel.getRootPanel();
		root.selectAll();
		updateEditPanel(panel);
	}
	
	@Override
	protected void handleDeselectAll(TransformerPanel panel) {
		CompositeTransformerPanel root = panel.getRootPanel();
		root.deselectAll();
		updateEditPanel(panel);
	}

	@Override
	protected void handleElementSelected(TransformerElement selected, TransformerPanel panel) {
		CompositeTransformerPanel root = new CompositeTransformerPanel(panel, this, selected.chain);
		panel.setRootPanel(root);
	}

	@Override
	protected void synchronize(TransformerPanel panel) {
	}

	boolean canShiftUp(TransformerPanel panel) {
		int totalUnselected = 0;
		CompositeTransformerPanel root = panel.getRootPanel();
		for (Transformer<CyNetwork, CyIdentifiable> transformer : root.getModel()) {
			TransformerElementViewModel<TransformerPanel> viewModel = root.getViewModel(transformer);
			if (!viewModel.checkBox.isSelected()) {
				totalUnselected++;
			} else {
				if (totalUnselected > 0) {
					return true;
				}
			}
		}
		return false;
	}

	boolean canShiftDown(TransformerPanel panel) {
		int totalSelected = 0;
		CompositeTransformerPanel root = panel.getRootPanel();
		for (Transformer<CyNetwork, CyIdentifiable> transformer : root.getModel()) {
			TransformerElementViewModel<TransformerPanel> viewModel = root.getViewModel(transformer);
			if (viewModel.checkBox.isSelected()) {
				totalSelected++;
			} else {
				if (totalSelected > 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void handleShiftUp(TransformerPanel panel) {
		boolean canShift = false;
		CompositeTransformerPanel root = panel.getRootPanel();
		List<Transformer<CyNetwork, CyIdentifiable>> model = root.getModel();
		
		int index = 0;
		while (index < model.size()) {
			Transformer<CyNetwork, CyIdentifiable> transformer = model.get(index);
			TransformerElementViewModel<TransformerPanel> viewModel = root.getViewModel(transformer);
			if (viewModel.checkBox.isSelected()) {
				if (canShift) {
					model.add(index - 1, model.remove(index));
				}
			} else {
				canShift = true;
			}
			index++;
		}
		root.updateLayout();
		updateEditPanel(panel);
	}

	public void handleShiftDown(TransformerPanel panel) {
		boolean canShift = false;
		CompositeTransformerPanel root = panel.getRootPanel();
		List<Transformer<CyNetwork, CyIdentifiable>> model = root.getModel();
		
		int index = model.size() - 1;
		while (index >= 0) {
			Transformer<CyNetwork, CyIdentifiable> transformer = model.get(index);
			TransformerElementViewModel<TransformerPanel> viewModel = root.getViewModel(transformer);
			if (viewModel.checkBox.isSelected()) {
				if (canShift) {
					model.add(index + 1, model.remove(index));
				}
			} else {
				canShift = true;
			}
			index--;
		}
		root.updateLayout();
		updateEditPanel(panel);
	}
	
	@Override
	protected void validateEditPanel(TransformerPanel panel) {
		panel.getShiftUpButton().setEnabled(canShiftUp(panel));
		panel.getShiftDownButton().setEnabled(canShiftDown(panel));
		
		CompositeTransformerPanel root = panel.getRootPanel();
		int totalSelected = root.countSelected();
		int totalUnselected = root.countUnselected();
		
		panel.getDeleteButton().setEnabled(totalSelected > 0);
		panel.getSelectAllButton().setEnabled(totalUnselected > 0);
		panel.getDeselectAllButton().setEnabled(totalSelected > 0);
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
	protected String getExportLabel() {
		return "Export filter chains...";
	}
	
	@Override
	protected String getImportLabel() {
		return "Import filter chains...";
	}
	
	public ComboBoxModel createChainComboBoxModel() {
		return new DynamicComboBoxModel<TransformerComboBoxElement>(transformerViewManager.getChainComboBoxModel());
	}
	
	public ComboBoxModel getStartWithComboBoxModel() {
		return startWithComboBoxModel;
	}

	public void handleAddTransformer(JComboBox comboBox, CompositeTransformerPanel panel, TransformerPanel transformerPanel) {
		if (comboBox.getSelectedIndex() == 0) {
			return;
		}
		
		TransformerComboBoxElement selectedItem = (TransformerComboBoxElement) comboBox.getSelectedItem();
		
		Transformer<CyNetwork, CyIdentifiable> transformer = transformerManager.createTransformer(selectedItem.getId());
		panel.addTransformer(transformer);
		panel.updateLayout();
		comboBox.setSelectedIndex(0);
		
		validateEditPanel(transformerPanel);
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
		
		NamedTransformer<CyNetwork, CyIdentifiable>[] namedTransformers = new NamedTransformer[model.getSize() - 1];
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
}
