package org.cytoscape.filter.internal.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.FilterIO;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterPanel;
import org.cytoscape.filter.internal.tasks.ExportNamedTransformersTask;
import org.cytoscape.filter.internal.tasks.ImportNamedTransformersTask;
import org.cytoscape.filter.internal.view.TransformerViewManager.TransformerViewElement;
import org.cytoscape.filter.internal.view.look.FilterPanelStyle;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPanelController<T extends NamedElement, V extends SelectPanelComponent> {
	public static final int PROGRESS_BAR_MAXIMUM = Integer.MAX_VALUE;
	
	static final Pattern NAME_PATTERN = Pattern.compile("(.*?)( (\\d+))?");
	
	protected boolean isInteractive;

	private final TransformerManager transformerManager;
	private final TransformerViewManager transformerViewManager;
	private final IconManager iconManager;
	private final FilterPanelStyle style;
	
	private List<NamedElementListener<T>> namedElementListeners;
	
	protected DynamicComboBoxModel<T> namedElementComboBoxModel;
	
	protected AbstractWorker<?, ?> worker;
	protected FilterIO filterIo;
	TaskManager<?, ?> taskManager;
	JComponent lastHoveredComponent;
	
	final Logger logger;

	public AbstractPanelController(AbstractWorker<?, ?> worker, TransformerManager transformerManager, TransformerViewManager transformerViewManager,
			                       FilterIO filterIo, TaskManager<?, ?> taskManager,
			                       FilterPanelStyle style, IconManager iconManager) {
		this.worker = worker;
		this.filterIo = filterIo;
		this.taskManager = taskManager;
		this.transformerManager = transformerManager;
		this.transformerViewManager = transformerViewManager;
		this.style = style;
		this.iconManager = iconManager;
		
		logger = LoggerFactory.getLogger(getClass());
		
		List<T> modelItems = new ArrayList<T>();
		namedElementComboBoxModel = new DynamicComboBoxModel<T>(modelItems);
		namedElementListeners = new CopyOnWriteArrayList<NamedElementListener<T>>();
	}

	public JPopupMenu createAddConditionMenu(final CompositeFilterPanel<?> panel) {
		JPopupMenu menu = new JPopupMenu();
		
		for (final TransformerViewElement element : transformerViewManager.getFilterConditionViewElements()) {
			JMenuItem mi = new JMenuItem(element.toString());
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleAddCondition(element, panel);
				}
			});
			menu.add(mi);
		}
		
		return menu;
	}
	
	private void handleAddCondition(TransformerViewElement element, CompositeFilterPanel<?> panel) {
		// Assume the factory makes filters
		Transformer<CyNetwork, CyIdentifiable> transformer = transformerManager.createTransformer(element.getId());
		Filter<CyNetwork, CyIdentifiable> filter = (Filter<CyNetwork, CyIdentifiable>) transformer;
		panel.addFilter(filter);
		panel.updateLayout();
		
		filter.addListener(worker);
		worker.handleFilterStructureChanged();
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
	
	public T getElementByName(String name) {
		for (T element : namedElementComboBoxModel.items) {
			if (element.name.equals(name)) {
				return element;
			}
		}
		return null;
	}
	
	public void reset(V view) {
		view.reset();
		while (namedElementComboBoxModel.getSize() > 0) {
			notifyRemoved(namedElementComboBoxModel.items.remove(0)); 
		}
		namedElementComboBoxModel.notifyChanged(0, 0);
	}
	
	public void setProgress(double progress, V panel) {
		boolean done = progress == 1.0;
		
		panel.getApplyButton().setEnabled(done);
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

	public void setStatus(V view, String message) {
		view.setStatus(message);
	}

	public JComponent getLastHoveredComponent() {
		return lastHoveredComponent;
	}

	public void setLastHoveredComponent(JComponent component) {
		lastHoveredComponent = component;
	}

	boolean isParentOrSelf(JComponent source, Component target) {
		while (target != null) {
			if (source == target) {
				return true;
			}
			target = target.getParent();
		}
		return false;
	}
	
	public FilterPanelStyle getStyle() {
		return style;
	}
	
	public IconManager getIconManager() {
		return iconManager;
	}
	
	
	public List<Integer> getPath(SelectPanelComponent view, JComponent component) {
		if (component == view.getRootPanel()) {
			return Collections.emptyList();
		}
		
		LinkedList<Integer> path = new LinkedList<>();
		Component current = component;
		Container nextParent = component.getParent();
		while (true) {
			if (!(nextParent instanceof CompositePanelComponent)) {
				break;
			}

			CompositePanelComponent composite = (CompositePanelComponent) nextParent;
			if (current == composite.getSeparator()) {
				path.addFirst(-1);
			} else {
				boolean found = false;
				for (int i = 0; i < composite.getTransformerCount(); i++) {
					Transformer<CyNetwork, CyIdentifiable> filter = composite.getTransformerAt(i);
					TransformerElementViewModel<?> viewModel = composite.getViewModel(filter);
					if (current == viewModel.view || current == viewModel.separator || current == viewModel.handle) {
						path.addFirst(i);
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
		if (path.isEmpty()) {
			return null;
		}
		return path;
	}

	
	public void handleDelete(SelectPanelComponent view, JComponent component) {
		if (component == null)
			return;
			
		List<Integer> path = getPath(view, component);
		int index = path.get(path.size() - 1);
		
		Component parent = component.getParent();
		if(parent instanceof CompositePanelComponent) {
			CompositePanelComponent compositePanel = (CompositePanelComponent)parent;
			compositePanel.removeTransformer(index, true);
		}
		
		CompositePanelComponent root = view.getRootPanel();
		root.updateLayout();
	}
	
	
	

	protected abstract T createElement(String name);

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

	public abstract JComponent createView(V parent, Transformer<CyNetwork, CyIdentifiable> transformer, int depth);
	
	public abstract void unregisterView(JComponent elementView);
	
	public abstract void addNamedTransformers(V view, NamedTransformer<CyNetwork, CyIdentifiable>... transformers);
	
	public abstract NamedTransformer<CyNetwork, CyIdentifiable>[] getNamedTransformers();
	
	public abstract JComponent getChild(V view, List<Integer> path);
	
	public abstract boolean supportsDrop(V view, List<Integer> sourcePath, JComponent source, List<Integer> targetPath, JComponent target);
	
	public abstract void handleDrop(V view, JComponent source, List<Integer> sourcePath, JComponent target, List<Integer> targetPath);
	
	public abstract String getHandleToolTip();
}

