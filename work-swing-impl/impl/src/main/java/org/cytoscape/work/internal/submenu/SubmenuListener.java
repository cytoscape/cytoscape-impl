
package org.cytoscape.work.internal.submenu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.PopupMenuEvent;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DynamicSubmenuListener;

class SubmenuListener implements DynamicSubmenuListener {

	private final SubmenuTunableMutator stm;

	private String menuName;
	private JMenuItem lastMenuItem;
	private boolean enableState;
	private Object tunableContext;

	SubmenuListener(SubmenuTunableMutator stm, TaskFactory tf, Object tunableContext) {
		this.stm = stm;
		this.menuName = "None Specified";
		this.enableState = true;
		this.tunableContext = tunableContext;
		stm.registerTunableContext(tf, tunableContext);
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
		removePopupMenuItem((JPopupMenu)(e.getSource()));
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		removePopupMenuItem((JPopupMenu)(e.getSource()));
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e)  {
		JPopupMenu parentMenu = (JPopupMenu)(e.getSource());
		lastMenuItem = stm.buildConfiguration(tunableContext);
		if ( lastMenuItem != null ) {
			if ( lastMenuItem instanceof JMenu )
				lastMenuItem.setText(menuName);
			lastMenuItem.setEnabled(enableState);
			parentMenu.add( lastMenuItem );
		}
	}

	@Override
	public void menuCanceled(MenuEvent e) { 
		removeMenuItem((JMenu)(e.getSource()));
	}

	@Override
	public void menuDeselected(MenuEvent e) { 
		removeMenuItem((JMenu)(e.getSource()));
	}

	@Override
	public void menuSelected(MenuEvent e)  {
		JMenu parentMenu = (JMenu)(e.getSource());
		lastMenuItem = stm.buildConfiguration(tunableContext);
		if ( lastMenuItem != null ) {
			String title = lastMenuItem.getText(); 
			if (title == null || title.isEmpty()) {
				lastMenuItem.setText(menuName);
			}
			lastMenuItem.setEnabled(enableState);
			parentMenu.add( lastMenuItem );
		}
	}

	private void removeMenuItem(JMenu parentMenu) {
		if ( lastMenuItem != null ) {
			parentMenu.remove( lastMenuItem ); 
			lastMenuItem = null;
		}
	}

	private void removePopupMenuItem(JPopupMenu parentMenu) {
		if ( lastMenuItem != null ) {
			parentMenu.remove( lastMenuItem ); 
			lastMenuItem = null;
		}
	}

	@Override
	public void setMenuTitle(String name) {
		if ( name == null )
			return;
		menuName = name;
	}

	@Override
	public void setEnabled(boolean b) {
		enableState = b;
	}
}
