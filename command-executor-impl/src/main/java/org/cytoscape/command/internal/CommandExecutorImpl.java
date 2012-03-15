package org.cytoscape.command.internal;


import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.internal.tunables.CommandTunableInterceptorImpl;
import org.cytoscape.work.TaskMonitor;

public class CommandExecutorImpl {

	private final static Logger logger = LoggerFactory.getLogger(CommandExecutorImpl.class);

	private final Map<String, Map<String,Executor>> commandExecutorMap = 
	                                             new HashMap<String,Map<String,Executor>>();

	private final String commandRegex ="^(\\S+)\\s+(\\S+)(\\s+.+)?$";
	private final Pattern commandPattern = Pattern.compile(commandRegex);
	private final CommandTunableInterceptorImpl interceptor; 
	private final CyApplicationManager appMgr;

	public CommandExecutorImpl(CyApplicationManager appMgr, CommandTunableInterceptorImpl interceptor) {
		this.appMgr = appMgr;
		this.interceptor = interceptor;
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

	public void addNetworkViewTaskFactory(NetworkViewTaskFactory tf, Map props) {
		addTF(new NVTFExecutor(tf,interceptor,appMgr), props);
	}

	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory tf, Map props) {
		removeTF(props);
	}

	public void addTableTaskFactory(TableTaskFactory tf, Map props) {
		addTF(new TTFExecutor(tf,interceptor,appMgr), props);
	}

	public void removeTableTaskFactory(TableTaskFactory tf, Map props) {
		removeTF(props);
	}

	private void addTF(Executor ex, Map props) {
		String namespace = (String)props.get("commandNamespace");
		String command = (String)props.get("command");
		if ( command == null && namespace == null ) 
			return;

		Map<String, Executor> map = commandExecutorMap.get(namespace);
		if ( map == null ) {
			map = new HashMap<String,Executor>();
			commandExecutorMap.put(namespace, map);
		}
		map.put(command,ex);
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

	public void executeList(List<String> commandLines, TaskMonitor tm) throws Exception {

		double size = (double)commandLines.size();
		double count = 1.0;

		// begin iterating over the lines
		for ( String fullLine : commandLines ) { 
			String line = fullLine.trim();

			// ignore comments
			if ( line.startsWith("#"))
				continue;

		  	Matcher m = commandPattern.matcher(line);
			if ( !m.matches() )
				throw new RuntimeException("command line (" + line + ") does not match pattern: namespace command [args ...]");
			String all = m.group(0);
			String namespace = m.group(1); 
			String command = m.group(2); 
			String args = m.group(3); 

			Map<String,Executor> commandMap = commandExecutorMap.get(namespace);

			if ( commandMap == null )
				throw new RuntimeException("Failed to find command namespace: '" + namespace +"'");	

			Executor ex = commandMap.get(command);

			if ( ex == null )
				throw new RuntimeException("Failed to find command: '" + command +"' (from namespace: " + namespace + ")");	
			ex.execute(args);

			tm.setProgress(count/size);
			count += 1.0;
		}
	}
}
