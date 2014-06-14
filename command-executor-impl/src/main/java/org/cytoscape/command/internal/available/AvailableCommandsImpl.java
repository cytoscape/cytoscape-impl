package org.cytoscape.command.internal.available;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;


import org.cytoscape.application.CyApplicationManager;
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
import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;
import org.cytoscape.command.AvailableCommands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class AvailableCommandsImpl implements AvailableCommands {

	private final static Logger logger = LoggerFactory.getLogger(AvailableCommandsImpl.class);

	private final Map<String, TaskFactory> commands;
	// private final Map<String,Map<String,List<String>>> argStrings;
	private final Map<String,Map<String,Map<String, ArgHandler>>> argHandlers;
	private final ArgRecorder argRec;
	private final StaticTaskFactoryProvisioner factoryProvisioner;
	private final Map<Object, TaskFactory> provisioners;
	private final CyApplicationManager appMgr;

	public AvailableCommandsImpl(ArgRecorder argRec, CyApplicationManager appMgr) {
		this.argRec = argRec;
		this.commands = new HashMap<String, TaskFactory>();
		this.argHandlers = new HashMap<String,Map<String,Map<String, ArgHandler>>>();
	 	this.factoryProvisioner = new StaticTaskFactoryProvisioner();
		this.provisioners = new IdentityHashMap<Object, TaskFactory>();
		this.appMgr = appMgr;
	}

	@Override
	public List<String> getNamespaces() {
		List<String> l = new ArrayList<String>( argHandlers.keySet() );
		Collections.sort(l);
		return l;
	}

	@Override
	public List<String> getCommands(String namespace) {
		Map<String,Map<String, ArgHandler>> mm = argHandlers.get(namespace);
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
		Map<String,Map<String, ArgHandler>> mm = argHandlers.get(namespace);
		if ( mm == null ) {
			return Collections.emptyList();
		} else {
			Map<String, ArgHandler> ll = mm.get(command);
			if ( ll == null ) {
				if (commands.containsKey(namespace+" "+command)) {
					ll = getArgs(commands.get(namespace+" "+command));
					mm.put(command, ll);
				} else {
					return Collections.emptyList();
				}
			}

			// At this point, we should definitely have everything to create the arguments
			List<String> l = new ArrayList<String>();
			for (ArgHandler ah: ll.values()) {
				l.add(ah.getName());
			}
			Collections.sort(l);
			return l;
		}
	}

	@Override
	public boolean getArgRequired(String namespace,String command,String argument) {
		Map<String, ArgHandler> map = getArgMap(namespace, command, argument);
		if (map != null && map.containsKey(argument))
			return map.get(argument).getRequired();
		return false;
	}

	@Override
	public String getArgTooltip(String namespace,String command,String argument) {
		Map<String, ArgHandler> map = getArgMap(namespace, command, argument);
		if (map != null && map.containsKey(argument))
			return map.get(argument).getTooltip();
		return null;
	}

	@Override
	public String getArgDescription(String namespace,String command,String argument) {
		Map<String, ArgHandler> map = getArgMap(namespace, command, argument);
		if (map != null && map.containsKey(argument))
			return map.get(argument).getDescription();
		return null;
	}

	@Override
	public Class<?> getArgType(String namespace,String command,String argument) {
		Map<String, ArgHandler> map = getArgMap(namespace, command, argument);
		if (map != null && map.containsKey(argument))
			return map.get(argument).getType();
		return null;
	}

	@Override
	public Object getArgValue(String namespace,String command,String argument) {
		Map<String, ArgHandler> map = getArgMap(namespace, command, argument);
		if (map != null && map.containsKey(argument)) {
			try {
				return map.get(argument).getValue();
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public String getArgTypeString(String namespace,String command,String argument) {
		Map<String, ArgHandler> map = getArgMap(namespace, command, argument);
		if (map != null && map.containsKey(argument))
			return map.get(argument).getDesc();
		return null;
	}

	public void addTaskFactory(TaskFactory tf, Map props) { addCommand(tf,props); }
	
	public void addNetworkTaskFactory(NetworkTaskFactory tf, Map props) {
		TaskFactory provisioner = factoryProvisioner.createFor(tf);
		provisioners.put(tf, provisioner);
		addCommand(provisioner,props);
	}
	
	public void addNetworkViewTaskFactory(NetworkViewTaskFactory tf, Map props) {
		TaskFactory provisioner = factoryProvisioner.createFor(tf);
		provisioners.put(tf, provisioner);
		addCommand(provisioner,props);
	}

	public void addNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory tf, Map props) {
		TaskFactory provisioner = factoryProvisioner.createFor(tf);
		provisioners.put(tf, provisioner);
		addCommand(provisioner,props);
	}
	
	public void addTableTaskFactory(TableTaskFactory tf, Map props) {
		TaskFactory provisioner = factoryProvisioner.createFor(tf);
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

		commands.put(namespace+" "+command, tf);
		// List<String> args = getArgs(tf);
		Map<String, ArgHandler> args = null;
		Map<String,Map<String, ArgHandler>> mm = argHandlers.get(namespace);
		if ( mm == null ) {
			mm = new HashMap<String,Map<String, ArgHandler>>();
			argHandlers.put(namespace,mm);
		}
		mm.put(command,args);
	}


	private void removeCommand(TaskFactory tf, Map properties) {
		String namespace = (String)(properties.get("commandNamespace"));
		String command = (String)(properties.get("command"));

		TaskFactory l = commands.remove(namespace+" "+command);
		if (l == null)
			return;
		Map<String,Map<String, ArgHandler>> m = argHandlers.get(namespace);
		if ( m != null )
			m.remove(command);
		if (m.isEmpty())
			argHandlers.remove(namespace);
	}

	private List<ArgHandler> getHandlers(TaskFactory tf) {
		try { 
			TaskIterator ti = tf.createTaskIterator();
			List<ArgHandler> args = new ArrayList<ArgHandler>();
			while ( ti.hasNext() ) {	
				Object task = ti.next();
				List<ArgHandler> handlers = argRec.getHandlers(task);
				for (ArgHandler handler: handlers) {
					String context = handler.getContext();
					// Only add commands appropriate for nogui
					if (!context.equals(Tunable.GUI_CONTEXT))
						args.add(handler);
				}
			}
			return args;
		} catch (Exception e) {
			logger.debug("Could not get handler for command.",e);
			return new ArrayList<ArgHandler>();
		}
	}

	private Map<String, ArgHandler> getArgMap(String namespace, String command, String arg) {
		if (!argHandlers.containsKey(namespace))
			return null;

		if (!argHandlers.get(namespace).containsKey(command))
			return null;

		Map<String, ArgHandler> map = argHandlers.get(namespace).get(command);
		if (map == null) {
			map = getArgs(commands.get(namespace+" "+command));
			argHandlers.get(namespace).put(command, map);
		}
		return map;
	}

	private Map<String, ArgHandler> getArgs(TaskFactory tf) {
		try { 
			TaskIterator ti = tf.createTaskIterator();
			if (ti == null)
				return Collections.emptyMap();

			Map<String, ArgHandler> argMap = new HashMap<String, ArgHandler>();

			while ( ti.hasNext() ) {	
				List<ArgHandler> handlers = argRec.getHandlers(ti.next());
				for (ArgHandler h: handlers)
					argMap.put(h.getName(), h);
			}

			return argMap;	
		} catch (Exception e) {
			logger.debug("Could not create invocation string for command.",e);
			e.printStackTrace();
			return Collections.emptyMap();
		}
	}

	class StaticTaskFactoryProvisioner {
		TaskFactory createFor(final NetworkTaskFactory factory) {
			return new AbstractTaskFactory() {
				@Override
				public TaskIterator createTaskIterator() {
					final Reference<CyNetwork> reference = new WeakReference<CyNetwork>(appMgr.getCurrentNetwork());
					return factory.createTaskIterator(reference.get());
				}
			};
		}
		
		TaskFactory createFor(final NetworkViewTaskFactory factory) {
			return new AbstractTaskFactory() {
				@Override
				public TaskIterator createTaskIterator() {
					final Reference<CyNetworkView> reference = new WeakReference<CyNetworkView>(appMgr.getCurrentNetworkView());
					return factory.createTaskIterator(reference.get());
				}
			};
		}

		TaskFactory createFor(final NetworkViewCollectionTaskFactory factory) {
			return new AbstractTaskFactory() {
				@Override
				public TaskIterator createTaskIterator() {
					List<CyNetworkView> views = new ArrayList<CyNetworkView>();
					views.add(appMgr.getCurrentNetworkView());
					final Reference<List<CyNetworkView>> reference = new WeakReference<List<CyNetworkView>>(views);
					return factory.createTaskIterator(reference.get());
				}
			};
		}
		
		TaskFactory createFor(final TableTaskFactory factory) {
			return new AbstractTaskFactory() {
				@Override
				public TaskIterator createTaskIterator() {
					final Reference<CyTable> reference = new WeakReference<CyTable>(appMgr.getCurrentNetwork().getDefaultNetworkTable());
					return factory.createTaskIterator(reference.get());
				}
			};
		}
	}
}
