package org.cytoscape.filter.internal.view;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public abstract class AbstractPanelController<T extends NamedElement, V extends SelectPanelComponent> {
	public static final int PROGRESS_BAR_MAXIMUM = Integer.MAX_VALUE;
	
	static final Color SELECTED_BACKGROUND_COLOR = new Color(222, 234, 252);

	protected int totalSelected;
	int elementsCreated = 0;
	protected boolean isInteractive;

	private List<NamedElementListener<T>> namedElementListeners;
	
	protected DynamicComboBoxModel<T> namedElementComboBoxModel;
	
	protected AbstractWorker<?, ?> worker;

	public AbstractPanelController(AbstractWorker<?, ?> worker) {
		this.worker = worker;
		
		List<T> modelItems = new ArrayList<T>();
		modelItems.add(createDefaultElement());
		namedElementComboBoxModel = new DynamicComboBoxModel<T>(modelItems);
		namedElementListeners = new CopyOnWriteArrayList<NamedElementListener<T>>();
	}

	public void addNamedElementListener(NamedElementListener<T> listener) {
		if (namedElementListeners.contains(listener)) {
			return;
		}
		namedElementListeners.add(listener);
		
		// Notify new listener about current state
		for (T element : namedElementComboBoxModel) {
			listener.handleElementAdded(element);
		}
	}
	
	public void removeNamedElementListener(NamedElementListener<T> listener) {
		namedElementListeners.remove(listener);
	}
	
	void handleDelete() {
		int index = namedElementComboBoxModel.getSelectedIndex(); 
		if (index <= 0) {
			// If nothing or the "create new filter" item is selected, do nothing.
			return;
		}
		
		notifyRemoved(namedElementComboBoxModel.remove(index));
	}

	private void notifyRemoved(T element) {
		for (NamedElementListener<T> listener : namedElementListeners) {
			try {
				listener.handleElementRemoved(element);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void notifyAdded(T element) {
		for (NamedElementListener<T> listener : namedElementListeners) {
			try {
				listener.handleElementAdded(element);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void handleElementSelected(V panel) {
		if (namedElementComboBoxModel.getSelectedIndex() == 0) {
			String defaultName = String.format(getElementTemplate(), ++elementsCreated);
			String name;
			String message = getPrompt();
			while (true) {
				name = (String) JOptionPane.showInputDialog(null, message, getCreateElementTitle(), JOptionPane.QUESTION_MESSAGE, null, null, defaultName);
				if (name == null) {
					return;
				}
				if (validateName(null, name, namedElementComboBoxModel)) {
					break;
				}
				message = String.format(getElementExistsWarningTemplate(), name);
			}
			addNewElement(name);
		}
		T selected = (T) namedElementComboBoxModel.getSelectedItem();
		if (selected == null) {
			return;
		}
		handleElementSelected(selected, panel);
	}
	
	@SuppressWarnings("unchecked")
	void handleRename(V panel) {
		T selected = (T) namedElementComboBoxModel.getSelectedItem();
		String defaultName = selected.name;
		String name;
		String message = getPrompt();
		while (true) {
			name = (String) JOptionPane.showInputDialog(null, message, getRenameElementTitle(), JOptionPane.QUESTION_MESSAGE, null, null, defaultName);
			if (name == null) {
				return;
			}
			if (validateName(defaultName, name, namedElementComboBoxModel)) {
				break;
			}
			message = "The name '" + name + "' is already being used by another filter.  Please provide a different name.";
		}
		selected.name = name;
	}

	public DynamicComboBoxModel<T> getElementComboBoxModel() {
		return namedElementComboBoxModel;
	}

	protected boolean validateName(String oldName, String newName, DynamicComboBoxModel<T> comboBoxModel) {
		if (oldName != null && oldName.equalsIgnoreCase(newName)) {
			// Name didn't change.
			return true;
		}
		
		for (T element : comboBoxModel) {
			if (element.name.equalsIgnoreCase(newName)) {
				return false;
			}
		}
		return true;
	}

	void handleImport(V panel) {
		// TODO Auto-generated method stub
		showComingSoonMessage(panel.getComponent());
	}

	void handleExport(V panel) {
		// TODO Auto-generated method stub
		showComingSoonMessage(panel.getComponent());
	}
	
	private void showComingSoonMessage(Component parent) {
		JOptionPane.showMessageDialog(parent, "Coming soon!", "Not yet implemented", JOptionPane.INFORMATION_MESSAGE);
	}

	protected void addNewElement(String name) {
		T element = createElement(name);
		namedElementComboBoxModel.add(element);
		namedElementComboBoxModel.setSelectedItem(element);
		notifyAdded(element);
	}
	
	protected void handleCheck(V panel, JCheckBox checkBox, JComponent view) {
		if (checkBox.isSelected()) {
			view.setBackground(SELECTED_BACKGROUND_COLOR);
			totalSelected += 1;
		} else {
			view.setBackground(Color.WHITE);
			totalSelected -=1;
		}
		updateEditPanel(panel);
	}
	
	protected void updateEditPanel(V panel) {
		validateEditPanel(panel);
		
		Component editPanel = panel.getEditPanel();
		editPanel.setVisible(totalSelected > 0);
		panel.getComponent().validate();
	}

	public void setProgress(double progress, V panel) {
		boolean done = progress == 1.0;
		
		panel.getApplyButton().setEnabled(done && !isInteractive);
		panel.getCancelApplyButton().setEnabled(!done);
		
		JProgressBar progressBar = panel.getProgressBar();
		if (done) {
			progressBar.setValue(0);
		} else {
			progressBar.setValue((int) (progress * PROGRESS_BAR_MAXIMUM));
		}
	}

	public void handleCancelApply(V view) {
		worker.cancel();
	}

	public void handleApplyFilter(V view) {
		worker.requestWork();
	}
	
	protected abstract T createElement(String name);

	protected abstract T createDefaultElement();
	
	protected abstract void handleDelete(V view);

	protected abstract void handleCancel(V view);

	protected abstract void handleElementSelected(T selected, V view);

	protected abstract String getElementExistsWarningTemplate();

	protected abstract String getPrompt();

	protected abstract String getCreateElementTitle();

	protected abstract String getRenameElementTitle();

	protected abstract String getElementTemplate();
	
	protected abstract void synchronize(V view);

	protected abstract void validateEditPanel(V view);
}
