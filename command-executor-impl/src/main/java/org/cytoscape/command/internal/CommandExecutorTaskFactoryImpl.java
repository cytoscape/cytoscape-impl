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

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.command.CommandExecutorTaskFactory;
import java.io.File;
import java.util.List; 
import java.util.Map; 
import java.util.HashMap; 
import java.util.Arrays; 


public class CommandExecutorTaskFactoryImpl extends AbstractTaskFactory implements CommandExecutorTaskFactory {

	private final CommandExecutorImpl cei;
	private final TunableSetter tunableSetter;

	public CommandExecutorTaskFactoryImpl(CommandExecutorImpl cei, TunableSetter tunableSetter) {
		this.cei = cei;
		this.tunableSetter = tunableSetter;
	}

	public TaskIterator createTaskIterator() {
		return this.createTaskIterator(null);
	}

	public TaskIterator createTaskIterator(TaskObserver observer) {
		return new TaskIterator(new CommandFileExecutorTask(cei, observer));
	} 

	public TaskIterator createTaskIterator(File file, TaskObserver observer) {
        final Map<String, Object> m = new HashMap<String, Object>();
        m.put("file", file);
        return tunableSetter.createTaskIterator(this.createTaskIterator(observer), m, observer);
	} 

	public TaskIterator createTaskIterator(TaskObserver observer, String ... commands) {
		return createTaskIterator(Arrays.asList(commands), observer);
	} 

	public TaskIterator createTaskIterator(List<String> commands, TaskObserver observer) {
		return new TaskIterator(new CommandStringsExecutorTask(commands,cei,observer));
	} 

	public TaskIterator createTaskIterator(String namespace, String command, 
	                                       Map<String,Object> args, TaskObserver observer) {
		return new TaskIterator(new CommandExecutorTask(namespace, command, args, cei, observer));
	} 
}
