package org.cytoscape.command.internal;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.internal.tunables.CommandTunableInterceptorImpl;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.TaskMonitor;

public class CommandExecutorImpl {

	private final static Logger logger = LoggerFactory.getLogger(CommandExecutorImpl.class);

	private final Map<String, Map<String,Executor>> commandExecutorMap = 
	                                             new HashMap<String,Map<String,Executor>>();

	private final String commandRegex ="^(\\S+)\\s+(\\S+)(\\s+.+)?$";
	private final Pattern commandPattern = Pattern.compile(commandRegex);
	private final CommandTunableInterceptorImpl interceptor; 
	private final CyApplicationManager appMgr;

	private final DynamicTaskFactoryProvisioner factoryProvisioner;
	
	public CommandExecutorImpl(CyApplicationManager appMgr, CommandTunableInterceptorImpl interceptor, DynamicTaskFactoryProvisioner factoryProvisioner) {
		this.appMgr = appMgr;
		this.factoryProvisioner = factoryProvisioner;
		this.interceptor = interceptor;
	}

	public void addTaskFactory(TaskFactory tf, Map props) {
		addTF(new TFExecutor(tf,interceptor), props);
	}

	public void removeTaskFactory(TaskFactory tf, Map props) {
		removeTF(props);
	}

	public void addNetworkTaskFactory(NetworkTaskFactory tf, Map props) {
		addTF(new TFExecutor(factoryProvisioner.createFor(tf), interceptor), props);
	}

	public void removeNetworkTaskFactory(NetworkTaskFactory tf, Map props) {
		removeTF(props);
	}

	public void addNetworkViewTaskFactory(NetworkViewTaskFactory tf, Map props) {
		addTF(new TFExecutor(factoryProvisioner.createFor(tf),interceptor), props);
	}

	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory tf, Map props) {
		removeTF(props);
	}

	public void addNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory tf, Map props) {
		addTF(new TFExecutor(factoryProvisioner.createFor(tf),interceptor), props);
	}

	public void removeNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory tf, Map props) {
		removeTF(props);
	}

	public void addTableTaskFactory(TableTaskFactory tf, Map props) {
		addTF(new TFExecutor(factoryProvisioner.createFor(tf),interceptor), props);
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
