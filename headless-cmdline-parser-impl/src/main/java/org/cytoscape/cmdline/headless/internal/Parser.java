package org.cytoscape.cmdline.headless.internal;

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
		              .withLongOpt("command")
		              .withDescription("Load a cytoscape command file.")
		              .withValueSeparator('\0').withArgName("file").hasArgs()
		              .create("c"));

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

		// Either load the session ...
		if (line.hasOption("c")) {
			//startupConfig.setSession(line.getOptionValue("s"));
			startupConfig.setCommandFile(line.getOptionValue("c"));

		// ... or all the rest.
		}
	}

	private void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("cytoscape.{sh|bat} [OPTIONS]", options);
	}
}
