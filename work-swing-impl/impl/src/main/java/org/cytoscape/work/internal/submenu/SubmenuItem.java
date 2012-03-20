
package org.cytoscape.work.internal.submenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.SubmenuTunableHandler;

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
		TaskIterator iterator = tf.createTaskIterator();
		dtm.execute(iterator);
	}
}
