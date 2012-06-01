
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

package org.cytoscape.command.internal.available;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;


import org.cytoscape.command.internal.available.dummies.DummyNetwork;
import org.cytoscape.command.internal.available.dummies.DummyNetworkView;
import org.cytoscape.command.internal.available.dummies.DummyTable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.command.AvailableCommands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class AvailableCommandsImpl implements AvailableCommands {

	private final static Logger logger = LoggerFactory.getLogger(AvailableCommandsImpl.class);

	private final Map<TaskFactory,String[]> commands;
	private final Map<String,Map<String,List<String>>> argStrings;
	private final ArgRecorder argRec;
	private final StaticTaskFactoryProvisioner factoryProvisioner;
	private final Map<Object, TaskFactory> provisioners;

	public AvailableCommandsImpl(ArgRecorder argRec) {
		this.argRec = argRec;
		this.commands = new HashMap<TaskFactory,String[]>();
		this.argStrings = new HashMap<String,Map<String,List<String>>>();
	 	this.factoryProvisioner = new StaticTaskFactoryProvisioner();
		this.provisioners = new IdentityHashMap<Object, TaskFactory>();
	}

	@Override
	public List<String> getNamespaces() {
		List<String> l = new ArrayList<String>( argStrings.keySet() );
		Collections.sort(l);
		return l;
	}

	@Override
	public List<String> getCommands(String namespace) {
		Map<String,List<String>> mm = argStrings.get(namespace);
		if ( mm == null ) {
			return Collections.emptyList();
		} else {
			List<String> l = new ArrayList<String>( mm.keySet() );
			Collections.sort(l);
			return l;
		}
	}

	@Override
	public List<String> getArguments(String namespace,String command) {
		Map<String,List<String>> mm = argStrings.get(namespace);
		if ( mm == null ) {
			return Collections.emptyList();
		} else {
			List<String> ll = mm.get(command);
			if ( ll == null ) {
				return Collections.emptyList();
			} else {
				List<String> l = new ArrayList<String>(ll);
				Collections.sort(l);
				return l;
			}
		}
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

	public void addNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory tf, Map props) {
		List<CyNetworkView> views = new ArrayList<CyNetworkView>();
		views.add(new DummyNetworkView());
		TaskFactory provisioner = factoryProvisioner.createFor(tf, views);
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
	public void removeNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory tf, Map props) { removeCommand(provisioners.remove(tf),props); }
	public void removeTableTaskFactory(TableTaskFactory tf, Map props) { removeCommand(provisioners.remove(tf),props); }

	private void addCommand(TaskFactory tf, Map properties) {
		String namespace = (String)(properties.get("commandNamespace"));
		String command = (String)(properties.get("command"));

		if (command == null || namespace == null) 
			return;
			
		commands.put(tf, new String[] {namespace,command});
		List<String> args = getArgs(tf);
		Map<String,List<String>> mm = argStrings.get(namespace);
		if ( mm == null ) {
			mm = new HashMap<String,List<String>>();
			argStrings.put(namespace,mm);
		}
		mm.put(command,args);
	}


	private void removeCommand(TaskFactory tf, Map properties) {
		String[] l = commands.remove(tf);
		if (l == null)
			return;
		Map<String,List<String>> m = argStrings.get(l[0]);
		if ( m != null )
			m.remove(l[1]);
		if (m.isEmpty())
			argStrings.remove(l[0]);
	}

	private List<String> getArgs(TaskFactory tf) {
		try { 
			TaskIterator ti = tf.createTaskIterator();
			List<String> args = new ArrayList<String>();

			while ( ti.hasNext() )	
				args.addAll( argRec.findArgs( ti.next() ) );

			return args;	
		} catch (Exception e) {
			logger.debug("Could not create invocation string for command.",e);
			return Arrays.asList("<unknown args>");
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

		TaskFactory createFor(final NetworkViewCollectionTaskFactory factory, List<CyNetworkView> networkViews) {
			final Reference<List<CyNetworkView>> reference = new WeakReference<List<CyNetworkView>>(networkViews);
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
