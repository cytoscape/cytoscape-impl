package org.cytoscape.filter.internal.view;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.cytoscape.filter.internal.FilterIO;
import org.cytoscape.filter.internal.composite.CompositeTransformerPanel;
import org.cytoscape.filter.internal.tasks.ExportNamedTransformersTask;
import org.cytoscape.filter.internal.tasks.ImportNamedTransformersTask;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

public abstract class AbstractPanelController<T extends NamedElement, V extends SelectPanelComponent> {
	public static final int PROGRESS_BAR_MAXIMUM = Integer.MAX_VALUE;
	
	static final Pattern NAME_PATTERN = Pattern.compile("(.*?)( (\\d+))?");
	
	protected boolean isInteractive;

	private List<NamedElementListener<T>> namedElementListeners;
	
	protected DynamicComboBoxModel<T> namedElementComboBoxModel;
	
	protected AbstractWorker<?, ?> worker;
	protected FilterIO filterIo;
	TaskManager<?, ?> taskManager;

	public AbstractPanelController(AbstractWorker<?, ?> worker, FilterIO filterIo, TaskManager<?, ?> taskManager) {
		this.worker = worker;
		this.filterIo = filterIo;
		this.taskManager = taskManager;
		
		List<T> modelItems = new ArrayList<T>();
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
		if (index == -1) {
			// If nothing is selected, do nothing.
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
	
	protected void createNewElement(V panel) {
		String defaultName = findUniqueName(String.format(getElementTemplate(), 1));
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
		handleElementSelected(panel);
	}
	
	@SuppressWarnings("unchecked")
	protected void handleElementSelected(V panel) {
		T selected = (T) namedElementComboBoxModel.getSelectedItem();
		if (selected == null) {
			return;
		}
		handleElementSelected(selected, panel);
	}
	
	String findUniqueName(String name) {
		Integer largestSuffix = null;
		String prefix = getNamePrefix(name);
		Pattern pattern = Pattern.compile(String.format("%s( (\\d+))?", Pattern.quote(prefix)));
		for (NamedElement element : namedElementComboBoxModel) {
			if (element.name == null) {
				continue;
			}
			
			Matcher matcher = pattern.matcher(element.name);
			if (!matcher.matches()) {
				continue;
			}
			
			String rawSuffix = matcher.group(2);
			int suffix;
			if (rawSuffix == null) {
				suffix = 0;
			} else {
				suffix = Integer.parseInt(rawSuffix);
			}
			
			if (largestSuffix == null) {
				largestSuffix = suffix;
			} else {
				largestSuffix = Math.max(largestSuffix, suffix);
			}
		}
		
		if (largestSuffix == null) {
			return name;
		}
		return String.format("%s %d", prefix, largestSuffix + 1);
	}
	
	private String getNamePrefix(String name) {
		Matcher matcher = NAME_PATTERN.matcher(name);
		matcher.matches();
		return matcher.group(1);
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

	public T addNewElement(String name) {
		T element = createElement(name);
		namedElementComboBoxModel.add(element);
		namedElementComboBoxModel.setSelectedItem(element);
		notifyAdded(element);
		return element;
	}
	
	public void reset() {
		while (namedElementComboBoxModel.getSize() > 1) {
			notifyRemoved(namedElementComboBoxModel.items.remove(1)); 
		}
		namedElementComboBoxModel.notifyChanged(0, 0);
	}
	
	protected void handleCheck(V panel, JCheckBox checkBox, JComponent view) {
		if (checkBox.isSelected()) {
			view.setBackground(CompositeTransformerPanel.SELECTED_BACKGROUND_COLOR);
		} else {
			view.setBackground(CompositeTransformerPanel.UNSELECTED_BACKGROUND_COLOR);
		}
		updateEditPanel(panel);
	}
	
	protected void updateEditPanel(V panel) {
		validateEditPanel(panel);
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
	
	public int getElementCount() {
		return namedElementComboBoxModel.items.size();
	}
	
	protected void handleExport(V view) {
		Task task = new ExportNamedTransformersTask(filterIo, getNamedTransformers());
		taskManager.execute(new TaskIterator(task));
	}
	
	@SuppressWarnings("rawtypes")
	protected void handleImport(V view) {
		Task task = new ImportNamedTransformersTask(filterIo, (AbstractPanel) view);
		taskManager.execute(new TaskIterator(task));
	}

	protected abstract T createElement(String name);

	protected abstract void handleDelete(V view);

	protected abstract void handleSelectAll(V view);
	
	protected abstract void handleDeselectAll(V view);

	protected abstract void handleElementSelected(T selected, V view);

	protected abstract String getElementExistsWarningTemplate();

	protected abstract String getPrompt();

	protected abstract String getCreateElementTitle();

	protected abstract String getRenameElementTitle();

	protected abstract String getElementTemplate();
	
	protected abstract String getCreateMenuLabel();
	
	protected abstract String getRenameMenuLabel();
	
	protected abstract String getDeleteMenuLabel();
	
	protected abstract String getExportLabel();
	
	protected abstract String getImportLabel();
	
	protected abstract void synchronize(V view);

	protected abstract void validateEditPanel(V view);
	
	public abstract void addNamedTransformers(V view, NamedTransformer<CyNetwork, CyIdentifiable>... transformers);
	
	public abstract NamedTransformer<CyNetwork, CyIdentifiable>[] getNamedTransformers();
}

