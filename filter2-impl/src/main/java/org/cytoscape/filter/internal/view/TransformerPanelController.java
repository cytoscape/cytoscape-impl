package org.cytoscape.filter.internal.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.composite.CompositeTransformerPanel;
import org.cytoscape.filter.internal.view.TransformerViewManager.TransformerComboBoxElement;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class TransformerPanelController extends AbstractPanelController<TransformerElement, TransformerPanel> {
	private TransformerManager transformerManager;
	private TransformerViewManager transformerViewManager;
	private DynamicComboBoxModel<FilterElement> startWithComboBoxModel;
	
	public TransformerPanelController(TransformerManager transformerManager, TransformerViewManager transformerViewManager, FilterPanelController filterPanelController, TransformerWorker worker) {
		super(worker);
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
		totalSelected = 0;
		updateEditPanel(panel);
		root.updateLayout();
	}

	@Override
	protected void handleCancel(TransformerPanel panel) {
		CompositeTransformerPanel root = panel.getRootPanel();
		root.deselectAll();
		totalSelected = 0;
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
		panel.shiftUpButton.setEnabled(canShiftUp(panel));
		panel.shiftDownButton.setEnabled(canShiftDown(panel));
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

	public ComboBoxModel createChainComboBoxModel() {
		return new DynamicComboBoxModel<TransformerComboBoxElement>(transformerViewManager.getChainComboBoxModel());
	}
	
	public ComboBoxModel getStartWithComboBoxModel() {
		return startWithComboBoxModel;
	}

	public void handleAddTransformer(JComboBox comboBox, CompositeTransformerPanel panel) {
		if (comboBox.getSelectedIndex() == 0) {
			return;
		}
		
		TransformerComboBoxElement selectedItem = (TransformerComboBoxElement) comboBox.getSelectedItem();
		
		Transformer<CyNetwork, CyIdentifiable> transformer = transformerManager.createTransformer(selectedItem.getId());
		panel.addTransformer(transformer);
		panel.updateLayout();
		comboBox.setSelectedIndex(0);
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
}
