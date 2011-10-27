
package org.cytoscape.work.internal.submenu;

import org.cytoscape.work.TunableMutator;
import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.SubmenuTunableHandler;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class SubmenuItem extends JMenuItem implements ActionListener {

	private final DialogTaskManager dtm;
	private final TaskFactory tf;
	private final SubmenuTunableHandler handler;
	private final String menuName;

	SubmenuItem(String menuName, SubmenuTunableHandler handler, DialogTaskManager dtm, TaskFactory tf) {
		super(menuName);
		this.menuName = menuName;
		this.handler = handler;
		this.dtm = dtm;
		this.tf = tf;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		handler.chosenMenu(menuName);
		dtm.execute(tf,false);
	}
}
