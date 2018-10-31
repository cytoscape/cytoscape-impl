package org.cytoscape.cmdline.gui.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape GUI Command Line Parser Impl (gui-cmdline-parser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2018 The Cytoscape Consortium
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

public class StartupConfig implements AppsFinishedStartingListener {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	private final Properties globalProps;
	private final Properties localProps = new Properties();
	private final StreamUtil streamUtil;
	private boolean taskStart;
	private OpenSessionTaskFactory loadSession;
	private LoadNetworkFileTaskFactory networkFileLoader;
	private LoadNetworkURLTaskFactory networkURLLoader;
	private LoadVizmapFileTaskFactory visualStylesLoader;
	
	private final CyServiceRegistrar registrar;
	
	private File sessionName;
	private ArrayList<File> networkFiles;
	private ArrayList<URL> networkURLs;
	private ArrayList<File> vizmapFiles;
	private ArrayList<TaskIterator> taskIteratorList;

	public StartupConfig(
			Properties globalProps,
			StreamUtil streamUtil,
			OpenSessionTaskFactory loadSession,
			LoadNetworkFileTaskFactory networkFileLoader,
			LoadNetworkURLTaskFactory networkURLLoader,
			LoadVizmapFileTaskFactory visualStylesLoader,
			CyServiceRegistrar registrar
	) {
		this.globalProps = globalProps;
		this.streamUtil = streamUtil;
		this.loadSession = loadSession;
		this.networkFileLoader = networkFileLoader;
		this.networkURLLoader = networkURLLoader;
		this.visualStylesLoader = visualStylesLoader;
		this.registrar = registrar;
		networkFiles= new ArrayList<>();
		networkURLs = new ArrayList<>();
		vizmapFiles = new ArrayList<>();
		taskIteratorList = new ArrayList<>();
	}

	@Override
	public void handleEvent(AppsFinishedStartingEvent evt) {
		// We should only load sessions after Cytoscape and apps have been initialized!
		if (!taskIteratorList.isEmpty()) {
			Task initTask = new DummyTask();
			TaskIterator taskIterator = new TaskIterator(taskIteratorList.size(), initTask);

			for (int i = taskIteratorList.size() - 1; i >= 0; i--) {
				TaskIterator ti = taskIteratorList.get(i);
				taskIterator.insertTasksAfter(initTask, ti);
			}
			
			taskIteratorList.clear();
			
			TaskManager <?,?> taskManager = registrar.getService(TaskManager.class);
			taskManager.execute(taskIterator);
		}
	}
	
	public void setProperties(String[] potentialProps) {
		Properties argProps = new Properties();

		Matcher propPattern = Pattern.compile("^((\\w+\\.*)+)\\=(.+)$").matcher("");

		for ( String potential : potentialProps ) {
			propPattern.reset(potential);

			// check to see if the string is a key value pair
			if (propPattern.matches()) {
				argProps.setProperty(propPattern.group(1), propPattern.group(3));

			// otherwise assume it's a file/url
			} else {
				try {
					InputStream in = null;

                    try {
						in = streamUtil.getInputStream(potential);
                        if (in != null)
                            localProps.load(in);
                        else
                            logger.info("Couldn't load property: " + potential);
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                    }
				} catch (IOException e) {
					logger.warn("Couldn't load property '"+ potential + "' from file: "+e.getMessage(), e);
				}
			}
		}

		// Transfer argument properties into the full properties.
		// We do this so that anything specified on the command line
		// overrides anything specified in a file.
		localProps.putAll(argProps);
		taskStart = true;
	}

	public void setSession(String args){
		try{
			sessionName = new File(args);
		}catch(Exception e){
			logger.error(e.toString());
		}
		taskStart = true;
	}

	public void setCommandScript(String args) {
		try{
			File scriptName = new File(args);
		}catch(Exception e){
			System.err.println("Can't find script file: "+args+": "+e.toString());
			logger.error(e.toString());
			return;
		}
		localProps.setProperty("scriptFile", args);
		taskStart = true;
	}

	public void setRestPort(String args) {
		if (args == null || args.length() == 0)
			args = "1234";
		else {
			try {
				Integer port = Integer.valueOf(args);
			} catch(Exception e) {
				System.err.println("Rest port argument not an integer: "+args);
				logger.error(e.toString());
				return;
			}
		}
		localProps.setProperty("rest.port", args);
		taskStart = true;
	}

	public void setNetworks(String[] args){
		networkFiles = new ArrayList<>();
		networkURLs = new ArrayList<>();
		
		for (String name : args) {
			try {
				if (name.matches(StreamUtil.URL_PATTERN))
					networkURLs.add(new URL(name));
				else
					networkFiles.add(new File(name));
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}
		
		taskStart = true;
	}

	public void setVizMapProps(String[] args){
		vizmapFiles = new ArrayList<>();
		
		for (String name: args){
			try {
				vizmapFiles.add(new File(name));
			} catch (Exception e) {

			}
		}
		
		taskStart = true;
	}

	public void start() {
		// set the properties
		// no need to do this in a task since it's so fast
		//globalProps.putAll(localProps);
		
		CyProperty<Properties> commandline = new SimpleCyProperty<>("commandline", localProps,
				Properties.class, CyProperty.SavePolicy.DO_NOT_SAVE);
		Properties cmdlnProps = new Properties();
		cmdlnProps.setProperty("cyPropertyName","commandline.props");
		registrar.registerService(commandline, CyProperty.class, cmdlnProps);
		
		// Only proceed if we've specified tasks for execution
		// on the command line.
		if ( !taskStart )
			return;

		// Since we've set command line args we presumably
		// don't want to see the welcome screen, so we disable it here.
		globalProps.setProperty("tempHideWelcomeScreen", "true");

		if (sessionName != null) {
			taskIteratorList.add(loadSession.createTaskIterator(sessionName));

		} else {
			for (File network : networkFiles)
				taskIteratorList.add(networkFileLoader.createTaskIterator(network));
			for (URL network : networkURLs)
				taskIteratorList.add(networkURLLoader.loadCyNetworks(network));
			for (File vizmap : vizmapFiles)
				taskIteratorList.add(visualStylesLoader.createTaskIterator(vizmap));
		}
	}
	
	private class DummyTask extends AbstractTask{

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			//DO nothing it is a dummy task just to initiate the iterator
		}
		
	}
}
