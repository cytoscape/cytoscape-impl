package org.cytoscape.command.internal;


import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.internal.tunables.CommandTunableInterceptorImpl;

public class CommandExecutorImpl {

	private final static Logger logger = LoggerFactory.getLogger(CommandExecutorImpl.class);

	private final Map<String, Map<String,Executor>> commandExecutorMap = 
	                                             new HashMap<String,Map<String,Executor>>();

	private final CommandTunableInterceptorImpl interceptor = new CommandTunableInterceptorImpl(); 
	private final CyApplicationManager appMgr;

	public CommandExecutorImpl(CyApplicationManager appMgr) {
		this.appMgr = appMgr;
	}

	public void addTaskFactory(TaskFactory tf, Map props) {
		addTF(new TFExecutor(tf,interceptor), props);
	}

	public void removeTaskFactory(TaskFactory tf, Map props) {
		removeTF(props);
	}

	public void addNetworkTaskFactory(NetworkTaskFactory tf, Map props) {
		addTF(new NTFExecutor(tf,interceptor,appMgr), props);
	}

	public void removeNetworkTaskFactory(NetworkTaskFactory tf, Map props) {
		removeTF(props);
	}

	private void addTF(Executor ex, Map props) {
		String namespace = (String)props.get("commandNamespace");
		String command = (String)props.get("command");
		if ( command == null && namespace == null ) 
			return;

		if ( !commandExecutorMap.containsKey(namespace) )
			commandExecutorMap.put(namespace, new HashMap<String,Executor>());
		commandExecutorMap.get(namespace).put(command,ex);
	}

	private void removeTF(Map props) {
		String namespace = (String)props.get("commandNamespace");
		String command = (String)props.get("command");
		if ( command == null && namespace == null ) 
			return;

		Map<String,Executor> ce = commandExecutorMap.get(namespace);
		if ( ce != null ) {
			ce.remove(command);
			if ( ce.size() == 0 )
				commandExecutorMap.remove(namespace);
		}
	}

	// If the key matches the first chars of the line, then
	// return the remainder of the line. Otherwise return null.
	private String peel(String line, String key) {
		if ( line.length() < key.length() ) {
			return null;
		}
		final String match = line.substring(0,key.length());
		final String remainder = line.substring(key.length(),line.length()).trim(); 
		if ( match.equals(key) ) {
			return remainder;
		} else {
			return null;
		}
	}

	public void executeList(List<String> commandLines) {
		try {

			// begin iterating over the lines
			for ( String line : commandLines ) { 
				boolean finished = false;
				
				// match the namespace
				for ( String namespace : commandExecutorMap.keySet() ) {
					if ( finished ) return;

					String commLine = peel( line, namespace );
					if ( commLine != null ) {
						Map<String,Executor> commandMap = commandExecutorMap.get(namespace);
						if ( commandMap != null ) {

							// now match and execute the command
							for ( String command : commandMap.keySet() ) {
								String args = peel( commLine, command );
								if ( args != null ) {
									commandMap.get(command).execute(args);
									finished = true;
									break;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Command parsing error: ", e);
		}
	}
}
