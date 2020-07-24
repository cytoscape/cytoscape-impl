package org.cytoscape.view.vizmap.gui.internal.view;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.util.swing.GravityTracker;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.PopupMenuGravityTracker;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;

public class OptionsButton {

	private DropDownMenuButton optionsBtn;
	
	/** Menu items under the options button */
	private JPopupMenu mainMenu;
	private PopupMenuGravityTracker mainMenuGravityTracker;
	
	private final ServicesUtil servicesUtil;
	
	
	public OptionsButton(ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}
	
	
	DropDownMenuButton getOptionsBtn() {
		if (optionsBtn == null) {
			var iconManager = servicesUtil.get(IconManager.class);
			optionsBtn = new DropDownMenuButton(getMainMenu(), false);
			optionsBtn.setToolTipText("Options...");
			optionsBtn.setFont(iconManager.getIconFont(12.0f));
			optionsBtn.setText(IconManager.ICON_BARS);
		}
		return optionsBtn;
	}
	
	JPopupMenu getMainMenu() {
		if (mainMenu == null) {
			mainMenu = new JPopupMenu();
		}
		return mainMenu;
	}
	
	private PopupMenuGravityTracker getMainMenuGravityTracker() {
		if (mainMenuGravityTracker == null) {
			mainMenuGravityTracker = new PopupMenuGravityTracker(getMainMenu());
		}
		return mainMenuGravityTracker;
	}
	

	public void addOption(JMenuItem menuItem, double gravity, boolean insertSeparatorBefore, boolean insertSeparatorAfter) {
		addMenuItem(getMainMenuGravityTracker(), menuItem, gravity, insertSeparatorBefore, insertSeparatorAfter);
		if (menuItem.getAction() instanceof CyAction)
			getMainMenu().addPopupMenuListener((CyAction)menuItem.getAction());
	}

	public void removeOption(JMenuItem menuItem) {
		getMainMenuGravityTracker().removeComponent(menuItem);
		if (menuItem.getAction() instanceof CyAction)
			getMainMenu().removePopupMenuListener((CyAction)menuItem.getAction());
	}
	
	
	private void addMenuItem(GravityTracker gravityTracker, JMenuItem menuItem, double gravity, boolean insertSeparatorBefore, boolean insertSeparatorAfter) {
		if (insertSeparatorBefore)
			gravityTracker.addMenuSeparator(gravity - .0001);
		gravityTracker.addMenuItem(menuItem, gravity);
		if (insertSeparatorAfter)
			gravityTracker.addMenuSeparator(gravity + .0001);
	}
}

