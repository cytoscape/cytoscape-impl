
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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.internal.actions.dummies.DummyNetwork;
import org.cytoscape.internal.actions.dummies.DummyNetworkView;
import org.cytoscape.internal.actions.dummies.DummyTable;
import org.cytoscape.internal.commands.ArgRecorder;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CommandListAction extends AbstractCyAction {

	private final static Logger logger = LoggerFactory.getLogger(CommandListAction.class);

	private final Map<TaskFactory,String> commands;
	private final Map<String,String> argStrings;
	private final CySwingApplication swingApp;
	private final ArgRecorder argRec;
	private final StaticTaskFactoryProvisioner factoryProvisioner;
	private final Map<Object, TaskFactory> provisioners;

	public CommandListAction(CySwingApplication swingApp, ArgRecorder argRec) {
		super("List All Commands...");
		setPreferredMenu("Tools");
	 	this.swingApp = swingApp;	
		this.argRec = argRec;
		this.commands = new HashMap<TaskFactory,String>();
		this.argStrings = new HashMap<String,String>();
	 	this.factoryProvisioner = new StaticTaskFactoryProvisioner();
		this.provisioners = new IdentityHashMap<Object, TaskFactory>();
	}

	public void actionPerformed(ActionEvent ae) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				List<String> commandList = new ArrayList<String>( commands.values() );
				Collections.sort(commandList);
				StringBuilder sb = new StringBuilder();
				for ( String comm : commandList ) {
					sb.append(comm);
					sb.append(" ");
					sb.append(argStrings.get(comm));
					sb.append(System.getProperty("line.separator"));
				}
				JOptionPane.showMessageDialog(swingApp.getJFrame(), sb.toString());
			}
		});
	}

	public void addTaskFactory(TaskFactory tf, Map props) { addCommand(tf,props); }
	
	public void addNetworkTaskFactory(NetworkTaskFactory tf, Map props) {
		TaskFactory provisioner = factoryProvisioner.createFor(tf, new DummyNetwork());
		provisioners.put(tf, provisioner);
		addCommand(provisioner,props);
	}
	
	public void addNetworkViewTaskFactory(NetworkViewTaskFactory tf, Map props) {
		TaskFactory provisioner = factoryProvisioner.createFor(tf, new DummyNetworkView());
		provisioners.put(tf, provisioner);
		addCommand(provisioner,props);
	}
	
	public void addTableTaskFactory(TableTaskFactory tf, Map props) {
		TaskFactory provisioner = factoryProvisioner.createFor(tf, new DummyTable());
		provisioners.put(tf, provisioner);
		addCommand(provisioner,props);
	}

	public void removeTaskFactory(TaskFactory tf, Map props) { removeCommand(provisioners.remove(tf),props); }
	public void removeNetworkTaskFactory(NetworkTaskFactory tf, Map props) { removeCommand(provisioners.remove(tf),props); }
	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory tf, Map props) { removeCommand(provisioners.remove(tf),props); }
	public void removeTableTaskFactory(TableTaskFactory tf, Map props) { removeCommand(provisioners.remove(tf),props); }

	private void addCommand(TaskFactory tf, Map properties) {
		String invocation = getInvocation(properties);
		if (invocation != null) {
			commands.put(tf, invocation);	
			String args = getArgs(tf);
			argStrings.put(invocation,args);
		}
	}

	private void removeCommand(TaskFactory tf, Map properties) {
		String invocation = getInvocation(properties);
		if (invocation != null) {
			commands.remove(tf);	
			argStrings.remove(invocation);
		}
	}

	private String getInvocation(Map properties) {
		String command = (String)properties.get("command");
		String commandNamespace = (String)properties.get("commandNamespace");
		if ( command == null || command.equals("") || 
		     commandNamespace == null || commandNamespace.equals("") )
			 return null;
		
		return commandNamespace + " " + command;
	}

	private String getArgs(TaskFactory tf) {
		try { 
			TaskIterator ti = tf.createTaskIterator();
			List<String> args = new ArrayList<String>();

			while ( ti.hasNext() )	
				args.addAll( argRec.findArgs( ti.next() ) );

			StringBuilder sb = new StringBuilder();
			for ( String s : args ) {
				sb.append(s);
				sb.append(" ");
			}

			return sb.toString();	
		} catch (Exception e) {
			logger.debug("Could not create invocation string for command.",e);
			return "<unknown args>";
		}
	}
	
	static class StaticTaskFactoryProvisioner {
		TaskFactory createFor(final NetworkTaskFactory factory, CyNetwork network) {
			final Reference<CyNetwork> reference = new WeakReference<CyNetwork>(network);
			return new AbstractTaskFactory() {
				@Override
				public TaskIterator createTaskIterator() {
					return factory.createTaskIterator(reference.get());
				}
			};
		}
		
		TaskFactory createFor(final NetworkViewTaskFactory factory, CyNetworkView networkView) {
			final Reference<CyNetworkView> reference = new WeakReference<CyNetworkView>(networkView);
			return new AbstractTaskFactory() {
				@Override
				public TaskIterator createTaskIterator() {
					return factory.createTaskIterator(reference.get());
				}
			};
		}
		
		TaskFactory createFor(final TableTaskFactory factory, CyTable table) {
			final Reference<CyTable> reference = new WeakReference<CyTable>(table);
			return new AbstractTaskFactory() {
				@Override
				public TaskIterator createTaskIterator() {
					return factory.createTaskIterator(reference.get());
				}
			};
		}
		
	}
}
