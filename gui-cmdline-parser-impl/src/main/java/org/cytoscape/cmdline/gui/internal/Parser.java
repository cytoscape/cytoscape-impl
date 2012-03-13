/*
 File: Parser.java

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

import java.util.Properties;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parser {
	private static final Logger logger = LoggerFactory.getLogger(Parser.class);

	private final String[] args;
	private final Options options;
	private final CyShutdown shutdown; 
	private final CyVersion version; 
	private final StartupConfig startupConfig; 
	private final Properties props; 

	public Parser(String[] args, CyShutdown shutdown, CyVersion version, StartupConfig startupConfig, Properties props) {
		this.args = args;
		this.shutdown = shutdown;
		this.version = version;
		this.startupConfig = startupConfig;
		this.props = props;
		options = initOptions(); 
		parseCommandLine(args);
	}

	private Options initOptions() {
		Options opt = new Options();

		opt.addOption("h", "help", false, "Print this message.");
		opt.addOption("v", "version", false, "Print the version number.");


		opt.addOption(OptionBuilder
		              .withLongOpt("session")
		              .withDescription("Load a cytoscape session (.cys) file.")
		              .withValueSeparator('\0').withArgName("file").hasArg() // only allow one session!!!
		              .create("s"));

		opt.addOption(OptionBuilder
		              .withLongOpt("network")
		              .withDescription( "Load a network file (any format).")
		              .withValueSeparator('\0').withArgName("file").hasArgs()
		              .create("N"));

		opt.addOption(OptionBuilder
		              .withLongOpt("edge-table")
		              .withDescription("Load an edge attributes table file (any table format).")
		              .withValueSeparator('\0').withArgName("file").hasArgs()
		              .create("e"));

		opt.addOption(OptionBuilder
		              .withLongOpt("node-table")
		              .withDescription("Load a node attributes table file (any table format).")
		              .withValueSeparator('\0').withArgName("file").hasArgs()
		              .create("n"));

		opt.addOption(OptionBuilder
		              .withLongOpt("global-table")
		              .withDescription("Load a global attributes table file (any table format).")
		              .withValueSeparator('\0').withArgName("file").hasArgs()
		              .create("g"));

		opt.addOption(OptionBuilder
		              .withLongOpt("plugin")
		              .withDescription("Load a SIMPLIFIED plugin jar file/URL.")
		              .withValueSeparator('\0').withArgName("file").hasArgs()
		              .create("p"));

		opt.addOption(OptionBuilder
		              .withLongOpt("bundle")
		              .withDescription("Load a BUNDLE plugin jar file or URL.")
		              .withValueSeparator('\0').withArgName("file").hasArgs()
		              .create("b"));

		opt.addOption(OptionBuilder
		              .withLongOpt("props")
		              .withDescription(
		              "Load cytoscape properties file (Java properties format) or individual property: -P name=value.")
		              .withValueSeparator('\0').withArgName("file").hasArgs()
		              .create("P"));

		opt.addOption(OptionBuilder
		              .withLongOpt("vizmap")
		              .withDescription("Load vizmap properties file (Cytoscape VizMap format).")
		              .withValueSeparator('\0').withArgName("file").hasArgs()
		              .create("V"));

		return opt;
	}

	private void parseCommandLine(String[] args) {

		// try to parse the cmd line
		CommandLineParser parser = new PosixParser();
		CommandLine line = null;

		// first load the simple exit options
		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
			logger.error("Parsing command line failed: " + e.getMessage());
			printHelp();
			props.setProperty("tempHideWelcomeScreen","true");
			shutdown.exit(1);
			return;
		}
		
		if (line.hasOption("h")) {
			printHelp();
			shutdown.exit(0);
			props.setProperty("tempHideWelcomeScreen","true");
			return;
		}

		if (line.hasOption("v")) {
			logger.info("Cytoscape version: " + version.getVersion());
			System.out.println("Cytoscape version: " + version.getVersion());
			props.setProperty("tempHideWelcomeScreen","true");
			shutdown.exit(0);
			return;
		}

		// always load any properties specified
		if (line.hasOption("P"))
			startupConfig.setProperties(line.getOptionValues("P"));

		// always load any plugins specified
		if (line.hasOption("p"))
			startupConfig.setSimplifiedPlugins(line.getOptionValues("p"));

		// always load any bundle plugins specified
		if (line.hasOption("b"))
			startupConfig.setBundlePlugins(line.getOptionValues("b"));

		// Either load the session ...
		if (line.hasOption("s")) {
			startupConfig.setSession(line.getOptionValue("s"));

		// ... or all the rest.
		} else {
			if (line.hasOption("N"))
				startupConfig.setNetworks(line.getOptionValues("N"));

			if (line.hasOption("V"))
				startupConfig.setVizMapProps(line.getOptionValues("V"));

			if (line.hasOption("n"))
				startupConfig.setNodeTables(line.getOptionValues("n"));

			if (line.hasOption("e"))
				startupConfig.setEdgeTables(line.getOptionValues("e"));

			if (line.hasOption("g"))
				startupConfig.setGlobalTables(line.getOptionValues("g"));
		}
	}

	private void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("cytoscape.{sh|bat} [OPTIONS]", options);
	}
}
