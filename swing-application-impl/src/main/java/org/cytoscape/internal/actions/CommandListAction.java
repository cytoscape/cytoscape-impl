
/*
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;

/**
 *
 */
public class CommandListAction extends AbstractCyAction {

	private final Map<TaskFactory,String> commands;
	private final CySwingApplication swingApp;

	public CommandListAction(CySwingApplication swingApp) {
		super("List All Commands...");
		setPreferredMenu("Tools");
	 	this.swingApp = swingApp;	
		this.commands = new HashMap<TaskFactory,String>();
	}

	public void actionPerformed(ActionEvent ae) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				List<String> commandList = new ArrayList<String>( commands.values() );
				Collections.sort(commandList);
				StringBuilder sb = new StringBuilder();
				for ( String comm : commandList ) {
					sb.append(comm);
					sb.append(System.getProperty("line.separator"));
				}
				JOptionPane.showMessageDialog(swingApp.getJFrame(), sb.toString());
			}
		});
	}

	public void addTaskFactory(TaskFactory tf, Map props) { addCommand(tf,props); }
	public void addNetworkTaskFactory(NetworkTaskFactory tf, Map props) { addCommand(tf,props); }
	public void addNetworkViewTaskFactory(NetworkViewTaskFactory tf, Map props) { addCommand(tf,props); }
	public void addTableTaskFactory(TableTaskFactory tf, Map props) { addCommand(tf,props); }

	public void removeTaskFactory(TaskFactory tf, Map props) { removeCommand(tf,props); }
	public void removeNetworkTaskFactory(NetworkTaskFactory tf, Map props) { removeCommand(tf,props); }
	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory tf, Map props) { removeCommand(tf,props); }
	public void removeTableTaskFactory(TableTaskFactory tf, Map props) { removeCommand(tf,props); }

	private void addCommand(TaskFactory tf, Map properties) {
		String invocation = getInvocation(properties);
		if (invocation != null)
			commands.put(tf, invocation);	
	}

	private void removeCommand(TaskFactory tf, Map properties) {
		String invocation = getInvocation(properties);
		if (invocation != null)
			commands.remove(tf);	
	}

	private String getInvocation(Map properties) {
		String command = (String)properties.get("command");
		String commandNamespace = (String)properties.get("commandNamespace");
		if ( command == null || command.equals("") || 
		     commandNamespace == null || commandNamespace.equals("") )
			 return null;
		
		return commandNamespace + " " + command;
	}
}
