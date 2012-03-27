/*
 File: StartupConfig.java

 Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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

package org.cytoscape.cmdline.gui.internal;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.CyVersion;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.task.loaddatatable.LoadAttributesFileTaskFactory;
import org.cytoscape.task.loaddatatable.LoadAttributesURLTaskFactory;
import org.cytoscape.task.loadnetwork.LoadNetworkFileTaskFactory;
import org.cytoscape.task.loadnetwork.LoadNetworkURLTaskFactory;
import org.cytoscape.task.loadvizmap.LoadVizmapFileTaskFactory;
import org.cytoscape.task.session.OpenSessionTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

import java.util.Properties;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class StartupConfig {
	private static final Logger logger = LoggerFactory.getLogger(StartupConfig.class);

	private final Properties globalProps; 
	private final Properties localProps = new Properties(); 
	private final StreamUtil streamUtil; 
	private boolean taskStart = false;
	private OpenSessionTaskFactory loadSession;
	private LoadNetworkFileTaskFactory networkFileLoader;
	private LoadNetworkURLTaskFactory networkURLLoader;
	private LoadVizmapFileTaskFactory visualStylesLoader;
	private LoadAttributesURLTaskFactory attributesURLLoader;
	private LoadAttributesFileTaskFactory attributesFileLoader;
	private final TaskManager taskManager;
	
	private File sessionName;
	private ArrayList<File> networkFiles;
	private ArrayList<URL> networkURLs;
	private ArrayList<File> vizmapFiles;
	private ArrayList<File> tableFiles;
	private ArrayList<URL> tableURLs;

	public StartupConfig(Properties globalProps, StreamUtil streamUtil, 
			OpenSessionTaskFactory loadSession, LoadNetworkFileTaskFactory networkFileLoader,
			LoadNetworkURLTaskFactory networkURLLoader, LoadVizmapFileTaskFactory visualStylesLoader, TaskManager taskManager) {
		this.globalProps = globalProps;
		this.streamUtil = streamUtil;
		this.loadSession = loadSession;
		this.networkFileLoader = networkFileLoader;
		this.networkURLLoader = networkURLLoader;
		this.visualStylesLoader = visualStylesLoader;
		this.taskManager = taskManager;
		networkFiles= new ArrayList<File>();
		networkURLs = new ArrayList<URL>();
		vizmapFiles = new ArrayList<File>();
		tableFiles = new ArrayList<File>();
		tableURLs = new ArrayList<URL>();
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
	}

	public void setSimplifiedPlugins(String[] args){
		taskStart = true;
	}

	public void setBundlePlugins(String[] args){
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

	public void setNetworks(String[] args){
		
		networkFiles = new ArrayList<File>();
		networkURLs = new ArrayList<URL>();
		
		for (String name : args){
			try{
			if (StreamUtil.URL_PATTERN.matches(name))
				networkURLs.add(new URL(name));
			else 
				networkFiles.add(new File(name));
			}catch (Exception e){
				logger.error(e.toString());
			}
		}
		
		taskStart = true;
	}

	public void setVizMapProps(String[] args){
		
		vizmapFiles = new ArrayList<File>();
		
		for (String name: args){
			try{
				vizmapFiles.add(new File(name));
			}catch(Exception e){
				
			}
		}
		
		taskStart = true;
	}


	public void setTables(String[] args){
		
		tableFiles = new ArrayList<File>();
		tableURLs = new ArrayList<URL>();
		
		for (String name : args){
			try{
			if (StreamUtil.URL_PATTERN.matches(name))
				tableURLs.add(new URL(name));
			else 
				tableFiles.add(new File(name));
			}catch (Exception e){
				logger.error(e.toString());
			}
		}
		
		taskStart = true;
	}

	public void start() {
		// set the properties
		// no need to do this in a task since it's so fast
		globalProps.putAll(localProps);

		// Only proceed if we've specified tasks for execution
		// on the command line.
		if ( !taskStart )
			return;

		// Since we've set command line args we presumably
		// don't want to see the welcome screen, so we
		// disable it here.
		globalProps.setProperty("tempHideWelcomeScreen","true");

		ArrayList<TaskIterator> taskIteratorList = new ArrayList<TaskIterator>();
		
	/*
		taskIterator.append( pluginManager.loadSimplifiedPlugins() );
		taskIterator.append( pluginManager.loadBundlePlugins() );
	 */			
		if ( sessionName != null ) 	{
			taskIteratorList.add( loadSession.createTaskIterator(sessionName));

		} else {
			for ( File network : networkFiles )
				taskIteratorList.add( networkFileLoader.creatTaskIterator(network) );
			for ( URL network : networkURLs )
				taskIteratorList.add( networkURLLoader.loadCyNetworks(network) );
			for ( File table : tableFiles )
				taskIteratorList.add( attributesFileLoader.createTaskIterator(table) );
			for ( URL table : tableURLs )
				taskIteratorList.add( attributesURLLoader.createTaskIterator(table) );
			for ( File vizmap : vizmapFiles )
				taskIteratorList.add( visualStylesLoader.createTaskIterator(vizmap));
		}

		Task initTask = new DummyTaks();
		TaskIterator taskIterator = new TaskIterator(taskIteratorList.size(), initTask);
		for (int i= taskIteratorList.size()-1; i>= 0 ; i--){
			TaskIterator ti = taskIteratorList.get(i);
			taskIterator.insertTasksAfter(initTask, ti);
		}
		
		taskManager.execute(taskIterator);
		
	}
	
	private class DummyTaks extends AbstractTask{

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			//DO nothing it is a dummy tas just to initiate the iterator
		}
		
	}
}
