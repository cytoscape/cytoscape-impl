/* vim: set ts=2: */
/**
 * Copyright (c) 2010 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.cytoscape.commandDialog.internal.handlers;

import java.io.File;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLevel;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskObserver;

public class CommandHandler implements PaxAppender, TaskObserver {
	boolean processingCommand = false;
	AvailableCommands availableCommands;
	CommandExecutorTaskFactory commandExecutor;
	MessageHandler resultsText = null;
	SynchronousTaskManager taskManager = null;

	public CommandHandler(AvailableCommands availableCommands, CommandExecutorTaskFactory commandExecutor,
	                      SynchronousTaskManager taskManager) {
		this.availableCommands = availableCommands;
		this.commandExecutor = commandExecutor;
		this.taskManager = taskManager;
	}

	public void handleCommand(MessageHandler resultsText, String input) {
		if (input.length() == 0 || input.startsWith("#")) return;
		this.resultsText = resultsText;

		try {
			// Handle our built-ins
			if (input.startsWith("help")) {
				getHelpReturn(input);
			} else {
				// processingCommand = true;
				// taskManager.execute(commandExecutor.createTaskIterator(Collections.singletonList(input)));
				// processingCommand = false;

				String ns = null;
	
				if ((ns = isNamespace(input)) != null) {
					handleCommand(input, ns);
				} else {
					throw new RuntimeException("Unknown command: "+input);
				}
			}
		} catch (RuntimeException e) {
			resultsText.appendError("  "+e.getMessage());
		}
		resultsText.appendMessage("");
	}

	private String isNamespace(String input) {
		String namespace = null;
		for (String ns: availableCommands.getNamespaces()) {
			if (input.toLowerCase().startsWith(ns.toLowerCase()) && 
			    (namespace == null || ns.length() > namespace.length()))
				namespace = ns;
		}
		return namespace;
	}

	private void handleCommand(String inputLine, String ns) {
		String sub = null;

		// Parse the input, breaking up the tokens into appropriate
		// commands, subcommands, and maps
		Map<String,Object> settings = new HashMap<String, Object>();
		String comm = parseInput(inputLine.substring(ns.length()).trim(), settings);

		for (String command: availableCommands.getCommands(ns)) {
			if (command.toLowerCase().equals(comm.toLowerCase())) {
				sub = command;
				break;
			}
		}

		if (sub == null && (comm != null && comm.length() > 0))
			throw new IllegalArgumentException("Unknown command: "+comm);
		
		// Now check the arguments
		List<String> argList = availableCommands.getArguments(ns,  comm);
		for (String inputArg: settings.keySet()) {
			boolean found = false;
			for (String arg: argList) {
				String[] bareArg = arg.split("=");
				if (bareArg[0].equalsIgnoreCase(inputArg)) {
					found = true;
					break;
				}
			}
			if (!found)
				throw new IllegalArgumentException("Unknown argument: "+inputArg);
		}
		
		processingCommand = true;
		/*
		System.out.println("Settings: ");
		for (String key: settings.keySet()) {
			System.out.println("   "+key+"="+settings.get(key));
		}
		*/
		// CyCommandManager.execute(ns, sub, settings);
		taskManager.execute(commandExecutor.createTaskIterator(ns, comm, settings, this), this);
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

	private void getHelpReturn(String input) {
		String tokens[] = input.split(" ");
		if (tokens.length == 1) {
			// Return all of the namespaces
			List<String> namespaces = availableCommands.getNamespaces();
			resultsText.appendMessage("Available namespaces:");
			for (String ns: namespaces) {
				resultsText.appendMessage("   "+ns);
			}
			return;
		} else if (tokens.length == 2) {
			if (tokens[1].equals("all")) {
				helpAll();
				return;
			}

			// Get all of the commands for the given namespace
			List<String> commands = availableCommands.getCommands(tokens[1]);
			if(commands.size() == 0) {
				resultsText.appendError("Can't find "+tokens[1]+" namespace");
				return;
			}
			resultsText.appendMessage("Available commands:");
			// TODO: Need to get the description for this command
			for (String command: commands) {
				resultsText.appendMessage("    "+tokens[1]+" "+command);
			}
		} else if (tokens.length > 2) {
			// Get all of the arguments for a specific command
			String command = "";
			for (int i = 2; i < tokens.length; i++) command += tokens[i]+" ";
			command = command.trim();
			// First, do a little sanity checking
			boolean found = false;
			List<String> commands = availableCommands.getCommands(tokens[1]);
			for (String c: commands) {
				if (c.equalsIgnoreCase(command)) {
					found = true;
					break;
				}
			}
			if (!found) {
				resultsText.appendError("Can't find command "+tokens[1]+" "+command);
				return;
			}

			List<String> argList = availableCommands.getArguments(tokens[1], command);
			String commandArgs = "    "+tokens[1]+" "+command+" ";
			for (String arg: argList) {
				commandArgs += arg+" ";
			}
			resultsText.appendMessage(commandArgs);
		}

	}

	private void helpAll() {
		for (String namespace: availableCommands.getNamespaces()) {
			resultsText.appendMessage(namespace);
			for (String command: availableCommands.getCommands(namespace)) {
				command = command.trim();
				List<String> argList = availableCommands.getArguments(namespace, command);
				String commandArgs = "    "+namespace+" "+command+" ";
				for (String arg: argList) {
					commandArgs += arg+" ";
				}
				resultsText.appendMessage(commandArgs);
			}
		}
	}

	public void doAppend(PaxLoggingEvent event) {
		// Get prefix
		// Handle levels
		if (!processingCommand) {
			return;
		}

		PaxLevel level = event.getLevel();
		if (level.toInt() == 40000)
			resultsText.appendError(event.getMessage());
		else if (level.toInt() == 30000)
			resultsText.appendWarning(event.getMessage());
		else
			resultsText.appendMessage(event.getMessage());
	}

	public void taskFinished(ObservableTask t) {
		Object res = t.getResults(String.class);
		if (res != null)
			resultsText.appendResult(res.toString());
	}

	public void allFinished() {
		processingCommand = false;
		resultsText.appendCommand("done");
	}
	
}
