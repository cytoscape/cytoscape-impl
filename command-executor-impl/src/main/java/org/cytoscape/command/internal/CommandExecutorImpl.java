package org.cytoscape.command.internal;

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


import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.AvailableCommands;
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
import org.cytoscape.work.TaskObserver;

public class CommandExecutorImpl {

	private final static Logger logger = LoggerFactory.getLogger(CommandExecutorImpl.class);

	private final Map<String, Map<String,Executor>> commandExecutorMap = 
	                                             new HashMap<String,Map<String,Executor>>();

	private final CommandTunableInterceptorImpl interceptor; 
	private final CyApplicationManager appMgr;
	private final AvailableCommands availableCommands;

	private final DynamicTaskFactoryProvisioner factoryProvisioner;
	
	private final Object lock = new Object();
	
	public CommandExecutorImpl(CyApplicationManager appMgr, CommandTunableInterceptorImpl interceptor, 
	                           AvailableCommands avc, DynamicTaskFactoryProvisioner factoryProvisioner) {
		this.appMgr = appMgr;
		this.factoryProvisioner = factoryProvisioner;
		this.interceptor = interceptor;
		this.availableCommands = avc;
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

		synchronized (lock) {
			Map<String, Executor> map = commandExecutorMap.get(namespace);
			if ( map == null ) {
				map = new HashMap<String,Executor>();
				commandExecutorMap.put(namespace, map);
			}
			map.put(command,ex);
		}
	}

	private void removeTF(Map props) {
		String namespace = (String)props.get("commandNamespace");
		String command = (String)props.get("command");
		if ( command == null && namespace == null ) 
			return;

		synchronized (lock) {
			Map<String,Executor> ce = commandExecutorMap.get(namespace);
			if ( ce != null ) {
				ce.remove(command);
				if (ce.isEmpty())
					commandExecutorMap.remove(namespace);
			}
		}
	}

	public void executeList(List<String> commandLines, TaskMonitor tm, TaskObserver observer) throws Exception {

		double size = (double)commandLines.size();
		double count = 1.0;

		// begin iterating over the lines
		for ( String fullLine : commandLines ) { 
			String line = fullLine.trim();

			// ignore comments
			if ( line.startsWith("#") || line.length() == 0)
				continue;

			handleCommand(line, tm, observer);

			tm.setProgress(count/size);
			count += 1.0;
		}
	}

	public void executeCommand(String namespace, String command, Map<String, Object> args, 
	                           TaskMonitor tm, TaskObserver observer) throws Exception {
			
		Executor ex;
		synchronized (lock) {
			Map<String,Executor> commandMap = commandExecutorMap.get(namespace);

			if ( commandMap == null )
				throw new RuntimeException("Failed to find command namespace: '" + namespace +"'");	

			ex = commandMap.get(command);
		}
		
		if ( ex == null )
			throw new RuntimeException("Failed to find command: '" + command +"' (from namespace: " + namespace + ")");	
		ex.execute(args, observer);
	}

	private void handleCommand(String commandLine, TaskMonitor tm, TaskObserver observer) throws Exception {
		String ns = null;
		if ((ns = isNamespace(commandLine)) == null) {
			throw new RuntimeException("Failed to find command namespace: '"+commandLine+"'");
		}

		Map<String, Object> settings = new HashMap<String, Object>();
		String comm = parseInput(commandLine.substring(ns.length()).trim(), settings);

		String sub = null;
		// We do this rather than just looking it up so we can do case-independent matches
		for (String command: availableCommands.getCommands(ns)) {
			if (command.equalsIgnoreCase(comm)) {
				sub = command;
				break;
			}
		}

		if (sub == null && (comm != null && comm.length() > 0))
			throw new RuntimeException("Failed to find command: '" + comm +"' (from namespace: " + ns + ")");

		Map<String, Object> modifiedSettings = new HashMap<String, Object>();
		// Now check the arguments
		List<String> argList = availableCommands.getArguments(ns, comm);
		for (String inputArg: settings.keySet()) {
			boolean found = false;
			for (String arg: argList) {
				String[] bareArg = arg.split("=");
				if (bareArg[0].trim().equalsIgnoreCase(inputArg)) {
					found = true;
					modifiedSettings.put(bareArg[0].trim(), settings.get(inputArg));
					break;
				}
			}	
			if (!found)
				throw new RuntimeException("Argument: '"+inputArg+" isn't applicable to command: '"+ns+" "+comm+"'");	
		}

		executeCommand(ns, sub, modifiedSettings, tm, observer);
		return;
	}

	private String isNamespace(String input) {
		String namespace = null;
		// Namespaces must always be single word
		String [] splits = input.split(" ");
		for (String ns: availableCommands.getNamespaces()) {
			if (splits[0].equalsIgnoreCase(ns) && 
			    (namespace == null || ns.length() > namespace.length()))
				namespace = ns;
		}
		return namespace;
	}

	private String parseInput(String input, Map<String,Object> settings) {
		// Tokenize
		StringReader reader = new StringReader(input);
		StreamTokenizer st = new StreamTokenizer(reader);

		// We don't really want to parse numbers as numbers...
		st.ordinaryChar('/');
		st.ordinaryChar('_');
		st.ordinaryChar('-');
		st.ordinaryChar('.');
		st.ordinaryChars('0', '9');

		st.wordChars('/', '/');
		st.wordChars('_', '_');
		st.wordChars('-', '-');
		st.wordChars('.', '.');
		st.wordChars('0', '9');

		List<String> tokenList = new ArrayList<String>();
		int tokenIndex = 0;
		int i;
		try {
			while ((i = st.nextToken()) != StreamTokenizer.TT_EOF) {
				switch(i) {
					case '=':
						// Get the next token
						i = st.nextToken();
						if (i == StreamTokenizer.TT_WORD || i == '"') {
							tokenIndex--;
							String key = tokenList.get(tokenIndex);
							settings.put(key, st.sval);
							tokenList.remove(tokenIndex);
						}
						break;
					case '"':
					case StreamTokenizer.TT_WORD:
						tokenList.add(st.sval);
						tokenIndex++;
						break;
					default:
						break;
				}
			} 
		} catch (Exception e) { return ""; }

		// Concatenate the commands together
		String command = "";
		for (String word: tokenList) command += word+" ";

		// Now, the last token of the args goes with the first setting
		return command.trim();
	}
}
