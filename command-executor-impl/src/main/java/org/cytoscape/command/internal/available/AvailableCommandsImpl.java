package org.cytoscape.command.internal.available;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Command Executor Impl (command-executor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class AvailableCommandsImpl implements AvailableCommands {

	private final static Logger logger = LoggerFactory.getLogger(AvailableCommandsImpl.class);

	private final Map<String, TaskFactory> commands;
	private final Map<String, String> descriptions;
	// private final Map<String,Map<String,List<String>>> argStrings;
	private final Map<String,Map<String,Map<String, ArgHandler>>> argHandlers;
	private final ArgRecorder argRec;
	private final StaticTaskFactoryProvisioner factoryProvisioner;
	private final Map<Object, TaskFactory> provisioners;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	private final Object lock = new Object();

	public AvailableCommandsImpl(final ArgRecorder argRec, final CyServiceRegistrar serviceRegistrar) {
		this.argRec = argRec;
		this.commands = new HashMap<>();
		this.descriptions = new HashMap<>();
		this.argHandlers = new HashMap<>();
	 	this.factoryProvisioner = new StaticTaskFactoryProvisioner();
		this.provisioners = new IdentityHashMap<>();
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public List<String> getNamespaces() {
		List<String> l;
		synchronized (lock) {
			l = new ArrayList<>( argHandlers.keySet() );
		}
		sort(l);
		
		return l;
	}

	@Override
	public List<String> getCommands(String namespace) {
		synchronized (lock) {
			Map<String,Map<String, ArgHandler>> mm = argHandlers.get(namespace);
			if ( mm == null ) {
				return Collections.emptyList();
			} else {
				List<String> l = new ArrayList<>( mm.keySet() );
				sort(l);
				
				return l;
			}
		}
	}

	@Override
	public String getDescription(String namespace, String command) {
		if (descriptions.containsKey(namespace+" "+command)) {
			return descriptions.get(namespace+" "+command);
		}
		return "";
	}

	@Override
	public List<String> getArguments(String namespace,String command) {
		synchronized (lock) {
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
				List<String> l = new ArrayList<>();
				for (ArgHandler ah: ll.values())
					l.add(ah.getName());
				sort(l);
				
				return l;
			}
		}
	}

	@Override
	public boolean getArgRequired(String namespace, String command, String argument) {
		Map<String, ArgHandler> map = getArgMap(namespace, command, argument);
		if (map != null && map.containsKey(argument))
			return map.get(argument).getRequired();
		return false;
	}

	@Override
	public String getArgTooltip(String namespace, String command, String argument) {
		Map<String, ArgHandler> map = getArgMap(namespace, command, argument);
		if (map != null && map.containsKey(argument))
			return map.get(argument).getTooltip();
		return null;
	}

	@Override
	public String getArgDescription(String namespace, String command, String argument) {
		Map<String, ArgHandler> map = getArgMap(namespace, command, argument);
		if (map != null && map.containsKey(argument))
			return map.get(argument).getDescription();
		return null;
	}

	@Override
	public Class<?> getArgType(String namespace, String command, String argument) {
		Map<String, ArgHandler> map = getArgMap(namespace, command, argument);
		if (map != null && map.containsKey(argument))
			return map.get(argument).getType();
		return null;
	}

	@Override
	public Object getArgValue(String namespace, String command, String argument) {
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
	public String getArgTypeString(String namespace, String command, String argument) {
		Map<String, ArgHandler> map = getArgMap(namespace, command, argument);
		if (map != null && map.containsKey(argument))
			return map.get(argument).getDesc();
		return null;
	}

	public void addTaskFactory(TaskFactory tf, Map<?, ?> props) {
		addCommand(tf, props);
	}
	
	public void addNetworkTaskFactory(NetworkTaskFactory tf, Map<?, ?> props) {
		TaskFactory provisioner = factoryProvisioner.createFor(tf);
		
		synchronized (lock) {
			provisioners.put(tf, provisioner);
			addCommand(provisioner,props);
		}
	}
	
	public void addNetworkViewTaskFactory(NetworkViewTaskFactory tf, Map<?, ?> props) {
		TaskFactory provisioner = factoryProvisioner.createFor(tf);
		
		synchronized (lock) {
			provisioners.put(tf, provisioner);
			addCommand(provisioner,props);
		}
	}

	public void addNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory tf, Map<?, ?> props) {
		TaskFactory provisioner = factoryProvisioner.createFor(tf);
		
		synchronized (lock) {
			provisioners.put(tf, provisioner);
			addCommand(provisioner,props);
		}
	}
	
	public void addTableTaskFactory(TableTaskFactory tf, Map<?, ?> props) {
		TaskFactory provisioner = factoryProvisioner.createFor(tf);
		
		synchronized (lock) {
			provisioners.put(tf, provisioner);
			addCommand(provisioner,props);
		}
	}

	public void removeTaskFactory(TaskFactory tf, Map<?, ?> props) {
		synchronized (lock) {
			removeCommand(provisioners.remove(tf),props);
		}
	}
	
	public void removeNetworkTaskFactory(NetworkTaskFactory tf, Map<?, ?> props) {
		synchronized (lock) {
			removeCommand(provisioners.remove(tf),props);
		}
	}
	
	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory tf, Map<?, ?> props) {
		synchronized (lock) {
			removeCommand(provisioners.remove(tf),props);
		}
	}
	public void removeNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory tf, Map<?, ?> props) {
		synchronized (lock) {
			removeCommand(provisioners.remove(tf),props);
		}
	}
	public void removeTableTaskFactory(TableTaskFactory tf, Map<?, ?> props) {
		synchronized (lock) {
			removeCommand(provisioners.remove(tf),props);
		}
	}

	private void addCommand(TaskFactory tf, Map<?, ?> properties) {
		String namespace = (String)(properties.get(ServiceProperties.COMMAND_NAMESPACE));
		String command = (String)(properties.get(ServiceProperties.COMMAND));
		String description = (String)(properties.get(ServiceProperties.COMMAND_DESCRIPTION));

		if (command == null || namespace == null) 
			return;

		synchronized (lock) {
			commands.put(namespace+" "+command, tf);
			descriptions.put(namespace+" "+command, description);
			// List<String> args = getArgs(tf);
			Map<String, ArgHandler> args = null;
			Map<String,Map<String, ArgHandler>> mm = argHandlers.get(namespace);
			if ( mm == null ) {
				mm = new HashMap<String,Map<String, ArgHandler>>();
				argHandlers.put(namespace,mm);
			}
			mm.put(command,args);
		}
	}


	private void removeCommand(TaskFactory tf, Map<?, ?> properties) {
		String namespace = (String)(properties.get(ServiceProperties.COMMAND_NAMESPACE));
		String command = (String)(properties.get(ServiceProperties.COMMAND));

		synchronized (lock) {
			descriptions.remove(namespace+" "+command);
			TaskFactory l = commands.remove(namespace+" "+command);
			
			if (l == null)
				return;
			
			Map<String,Map<String, ArgHandler>> m = argHandlers.get(namespace);
			
			if ( m != null )
				m.remove(command);
			if (m.isEmpty())
				argHandlers.remove(namespace);
		}
	}

	private Map<String, ArgHandler> getArgMap(String namespace, String command, String arg) {
		synchronized (lock) {
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
	}

	private Map<String, ArgHandler> getArgs(TaskFactory tf) {
		try { 
			TaskIterator ti = tf.createTaskIterator();
			if (ti == null)
				return Collections.emptyMap();

			Map<String, ArgHandler> argMap = new HashMap<String, ArgHandler>();

			while ( ti.hasNext() ) {	
				List<ArgHandler> handlers = argRec.getHandlers(ti.next());
				for (ArgHandler h: handlers) {
					String context = h.getContext();
					// Only add commands appropriate for nogui
					if (!context.equals(Tunable.GUI_CONTEXT))
						argMap.put(h.getName(), h);
				}
			}

			return argMap;	
		} catch (Exception e) {
			logger.debug("Could not create invocation string for command.",e);
			e.printStackTrace();
			return Collections.emptyMap();
		}
	}
	
	private void sort(final List<String> list) {
		final Collator collator = Collator.getInstance(Locale.getDefault());
		
		Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return collator.compare(s1, s2);
			}
		});
	}

	class StaticTaskFactoryProvisioner {
		TaskFactory createFor(final NetworkTaskFactory factory) {
			return new AbstractTaskFactory() {
				@Override
				public TaskIterator createTaskIterator() {
					final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
					final Reference<CyNetwork> reference = new WeakReference<>(appMgr.getCurrentNetwork());
					
					return factory.createTaskIterator(reference.get());
				}
			};
		}
		
		TaskFactory createFor(final NetworkViewTaskFactory factory) {
			return new AbstractTaskFactory() {
				@Override
				public TaskIterator createTaskIterator() {
					final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
					final Reference<CyNetworkView> reference = new WeakReference<>(appMgr.getCurrentNetworkView());
					
					return factory.createTaskIterator(reference.get());
				}
			};
		}

		TaskFactory createFor(final NetworkViewCollectionTaskFactory factory) {
			return new AbstractTaskFactory() {
				@Override
				public TaskIterator createTaskIterator() {
					List<CyNetworkView> views = new ArrayList<>();
					final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
					views.add(appMgr.getCurrentNetworkView());
					final Reference<List<CyNetworkView>> reference = new WeakReference<>(views);
					
					return factory.createTaskIterator(reference.get());
				}
			};
		}
		
		TaskFactory createFor(final TableTaskFactory factory) {
			return new AbstractTaskFactory() {
				@Override
				public TaskIterator createTaskIterator() {
					final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
					final Reference<CyTable> reference = new WeakReference<>(appMgr.getCurrentNetwork().getDefaultNetworkTable());
					
					return factory.createTaskIterator(reference.get());
				}
			};
		}
	}
}
