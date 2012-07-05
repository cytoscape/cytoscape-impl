package org.cytoscape.cmdline.headless.internal;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.CyVersion;
import org.cytoscape.command.AvailableCommands;

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
	private final AvailableCommands availableCommands; 

	public Parser(String[] args, CyShutdown shutdown, CyVersion version, StartupConfig startupConfig, AvailableCommands availableCommands) {
		this.args = args;
		this.shutdown = shutdown;
		this.version = version;
		this.startupConfig = startupConfig;
		this.availableCommands = availableCommands;
		options = initOptions(); 
		parseCommandLine(args);
	}

	private Options initOptions() {
		Options opt = new Options();

		opt.addOption("h", "help", false, "Print this message.");
		opt.addOption("v", "version", false, "Print the version number.");


		opt.addOption(OptionBuilder
		              .withLongOpt("command")
		              .withDescription("Cytoscape command file to be executed.")
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
			logger.error("Parsing command line failed.",e);
			printHelp();
			shutdown.exit(1);
			return;
		}
		
		if (line.hasOption("h")) {
			printHelp();
		} else if (line.hasOption("v")) {
			System.out.println("Cytoscape version: " + version.getVersion());
		} else if (line.hasOption("c")) {
			startupConfig.setCommandFile(line.getOptionValue("c"));
		} else {
			logger.error("No arguments specified. See usage for details.");
			printHelp();
		}
	}

	private void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		System.out.println();
		formatter.printHelp("cytoscape.{sh|bat} [OPTIONS]", options);
		printAvailableCommands();
	}

	private void printAvailableCommands() {
		StringBuilder sb = new StringBuilder();
		sb.append(System.getProperty("line.separator"));
		sb.append(" available commands:");
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		for ( String namespace : availableCommands.getNamespaces() ) {
			for ( String command : availableCommands.getCommands(namespace) ) {
				sb.append("  ");
				sb.append(namespace);
				sb.append(" ");
				sb.append(command);
				sb.append(" ");
				for ( String arg : availableCommands.getArguments(namespace,command) ) {
					sb.append(arg);
					sb.append(" ");
				}
				sb.append(System.getProperty("line.separator"));
			}
		}
		System.out.println(sb.toString());
	}
}
