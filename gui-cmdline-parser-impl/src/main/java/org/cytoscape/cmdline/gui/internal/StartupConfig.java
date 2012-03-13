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

import java.util.Properties;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
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

	public StartupConfig(Properties globalProps, StreamUtil streamUtil) {
		this.globalProps = globalProps;
		this.streamUtil = streamUtil;
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
		taskStart = true;
	}

	public void setNetworks(String[] args){
		taskStart = true;
	}

	public void setVizMapProps(String[] args){
		taskStart = true;
	}

	public void setNodeTables(String[] args){
		taskStart = true;
	}

	public void setEdgeTables(String[] args){
		taskStart = true;
	}

	public void setGlobalTables(String[] args){
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

	/*

		taskIterator.append( pluginManager.loadSimplifiedPlugins() );
		taskIterator.append( pluginManager.loadBundlePlugins() );
			
		if ( sessionName != null ) 	{
			taskIterator.append( sessionTaskFactory.loadSession( sessionName ) );

		} else {
			for ( String network : networkNames )
				taskIterator.append( loadNetwork.loadNetwork( network ) );
			for ( String nodeTable : nodeTables )
				taskIterator.append( loadTable.loadTable( nodeTable, CyNode.class ) );
			for ( String edgeTable : edgeTables )
				taskIterator.append( loadTable.loadTable( edgeTable, CyEdge.class ) );
			for ( String globalTable : globalTables )
				taskIterator.append( loadTable.loadTable( globalTable, null ) );
			for ( String vizmap : vizmapProps )
				taskIterator.append( vizmapLoader.loadVizmap( vizmap ) );
		}

		taskManager.execute(taskIterator);
		*/
	}
}
