package org.cytoscape.view.vizmap.gui.internal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.action.VizMapUIAction;
import org.cytoscape.view.vizmap.gui.internal.task.generators.GenerateValuesTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.theme.IconManager;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * Manager for all Vizmap-local tasks (commands).
 * 
 * @author kono
 * 
 */
public class VizMapperMenuManager {

	private static final Logger logger = LoggerFactory
			.getLogger(VizMapperMenuManager.class);

	// Metadata
	private static final String METADATA_MENU_KEY = "menu";
	private static final String METADATA_TITLE_KEY = "title";

	private static final String MAIN_MENU = "main";
	private static final String CONTEXT_MENU = "context";

	// Menu items under the tool button
	private final JPopupMenu mainMenu;

	// Context menu
	private final JPopupMenu rightClickMenu;
	
	// Context Menu Preset items
	private final JMenu edit;
	private final JMenu generateValues;

	private IconManager iconManager;

	// Injected from resource file.
	private String generateMenuLabel;
	private String generateIconId;

	private String modifyMenuLabel;
	private String modifyIconId;

	private JMenu modifyValues;

	private final TaskManager taskManager;
	
	private final PropertySheetPanel panel;
	private final SelectedVisualStyleManager manager;
	private final CyApplicationManager appManager;

	public VizMapperMenuManager(final TaskManager taskManager, final PropertySheetPanel panel, final SelectedVisualStyleManager manager, final CyApplicationManager appManager) {

		if (taskManager == null)
			throw new NullPointerException("TaskManager is null.");

		this.taskManager = taskManager;
		this.appManager = appManager;
		this.manager = manager;
		this.panel = panel;

		// Will be shown under the button next to Visual Style Name
		mainMenu = new JPopupMenu();

		// Context menu
		rightClickMenu = new JPopupMenu();
		this.edit = new JMenu("Edit");
		rightClickMenu.add(edit);
		this.generateValues = new JMenu("Mapping Value Generators");
		this.rightClickMenu.add(generateValues);

		// modifyValues = new JMenu(modifyMenuLabel);
	}

	public void setIconManager(IconManager iconManager) {
		this.iconManager = iconManager;
	}

	public void setGenerateMenuLabel(String generateMenuLabel) {
		this.generateMenuLabel = generateMenuLabel;
	}

	public void setGenerateIconId(String generateIconId) {
		this.generateIconId = generateIconId;
	}

	public JPopupMenu getMainMenu() {
		return mainMenu;
	}

	public JPopupMenu getContextMenu() {
		return rightClickMenu;
	}

	/*
	 * Custom listener for dynamic menu management
	 * 
	 * (non-Javadoc)
	 * 
	 * @see cytoscape.view.ServiceListener#onBind(java.lang.Object,
	 * java.util.Map)
	 */
	public void onBind(VizMapUIAction action, Map properties) {
//		if (generateValues == null && iconManager != null) {
//			// for value generators.
//			generateValues = new JMenu(generateMenuLabel);
//			generateValues.setIcon(iconManager.getIcon(generateIconId));
//			rightClickMenu.add(generateValues);
//		}
//
//		final Object serviceType = properties.get("service.type");
//		if (serviceType != null
//				&& serviceType.toString().equals("vizmapUI.contextMenu")) {
//			edit.add(action.getMenu());
//		} else {
//			mainMenu.add(action.getMenu());
//		}
	}

	public void onUnbind(VizMapUIAction service, Map properties) {
	}

	/**
	 * Add menu items to proper locations.
	 * 
	 * @param taskFactory
	 * @param properties
	 */
	public void addTaskFactory(final TaskFactory taskFactory,
			@SuppressWarnings("rawtypes") Map properties) {

		final Object serviceType = properties.get(METADATA_MENU_KEY);
		if (serviceType == null)
			throw new NullPointerException(
					"Service Type metadata is null.  This value is required.");

		// This is a menu item for Main Command Button.
		final Object title = properties.get(METADATA_TITLE_KEY);
		if (title == null)
			throw new NullPointerException("Title metadata is missing.");

		// Add new menu to the pull-down
		final JMenuItem menuItem = new JMenuItem(title.toString());
		// menuItem.setIcon(iconManager.getIcon(iconId));
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				taskManager.execute(taskFactory);
			}
		});

		if(serviceType.toString().equals(MAIN_MENU))
			mainMenu.add(menuItem);
		else if(serviceType.toString().equals(CONTEXT_MENU))
			edit.add(menuItem);

	}

	public void removeTaskFactory(final TaskFactory taskFactory, Map properties) {

	}
	
	public void addMappingGenerator(final DiscreteMappingGenerator<?> generator, @SuppressWarnings("rawtypes") Map properties) {
		final Object serviceType = properties.get(METADATA_MENU_KEY);
		if (serviceType == null)
			throw new NullPointerException(
					"Service Type metadata is null.  This value is required.");

		// This is a menu item for Main Command Button.
		final Object title = properties.get(METADATA_TITLE_KEY);
		if (title == null)
			throw new NullPointerException("Title metadata is missing.");

		// Create mapping generator task factory
		final GenerateValuesTaskFactory taskFactory = new GenerateValuesTaskFactory(generator, panel, manager, appManager);
		
		// Add new menu to the pull-down
		final JMenuItem menuItem = new JMenuItem(title.toString());
		// menuItem.setIcon(iconManager.getIcon(iconId));
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				taskManager.execute(taskFactory);
			}
		});

		generateValues.add(menuItem);
		
	}
	
	public void removeMappingGenerator(final DiscreteMappingGenerator<?> generator, @SuppressWarnings("rawtypes") Map properties) {
		// FIXME
	}

}
