package org.cytoscape.work.internal.tunables;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.cytoscape.work.Tunable;


/**
 * Commandline handler for an <i>InputStream</i> type of <code>Tunable</code>
 *
 * @author pasteur
 */
public class InputStreamCLHandler extends AbstractCLHandler implements CLHandler{
	/**
	 * InputStream object whose path or url will be set by this handler, to modify the original <code>InputStream</code> contained in <code>o</code>
	 */
	private InputStream is;

	/**
	 * Constructs the <code>CLHandler</code> for an <code>InputStream</code> Object contained in a Field <code>f</code>
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	public InputStreamCLHandler(Field f, Object o, Tunable t) {
		super(f, o, t);

		try {
			this.is = (InputStream)getValue();
		} catch(final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Constructs the <code>CLHandler</code> for an <code>InputStream</code> Object managed by <i>get</i> and <i>set</i> methods
	 * @param gmethod method that returns the value of the Object <code>o</code> annotated as a <code>Tunable</code>
	 * @param smethod method that sets a value to the Object <code>o</code> annotated as a <code>Tunable</code>
	 * @param o Object whose value will be set and get by the methods
	 * @param t <code>Tunable</code> annotations of the Method <code>gmethod</code> annotated as <code>Tunable</code>
	 */
	public InputStreamCLHandler(Method gmethod, Method smethod, Object o,Tunable t) {
		super(gmethod, smethod, o, t);
	}

	/**
	 * If options/arguments are well detected for this handler, it applies the argument :
	 * <p><pre>
	 * <ul>
	 * <li> display some specific informations if the argument is <code>--cmd</code> , or</li>
	 * <li> with the argument, it creates a <code>FileInputStream</code>, or <code>URL</code> if it's not possible, and set the InputStream Object <code>o</code> contained in <code>f</code> with it , or</li>
	 * <li> with the argument, it creates a <code>FileInputStream</code>, or <code>URL</code> if it's not possible, and set this stream as a parameter for the <code>set</code> Method </li>
	 * </ul>
	 * </pre></p>
	 *
	 * @param commandline with arguments
	 */
	public void handleLine(CommandLine line) {
		final String fc = getName();
		try {
			if (line.hasOption(fc)) {
				if (line.getOptionValue(fc).equals("--cmd")) {
					displayCmds(fc);
					System.exit(1);
				}
				try {
					is = new BufferedInputStream(new FileInputStream(line.getOptionValue(fc)));
					setValue(is);
					System.out.println("Local InputStream choosen");
				} catch(final Exception e) {
					final URL url = new URL(line.getOptionValue(fc));
					if (url != null)
						setValue(url.openStream());
					System.out.println("URL InputStream choosen");
				}
			}
		} catch(final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Create an Option for the Object <code>o</code> contained in <code>f</code> or got from the <code>get</code> Method.
	 * The option has :
	 * <p><pre>
	 * <ul>
	 * <li> the name of the handler</li>
	 * <li> its description with the current inputstream or path</li>
	 * </ul>
	 * </pre></p>
	 * @return option of the handler
	 */
	public Option getOption() {
		final String fc = getName();
		InputStream currentValue = null;

		try {
			currentValue = (InputStream)getValue();
		} catch(final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return new Option(fc, true, "-- " + getDescription() + " --\n  currently selected value: " + currentValue.toString());
	}

	/**
	 * Display some detailed informations to the user for this particular handler
	 * @param name of the handler
	 */
	private void displayCmds(String fc){
		HelpFormatter formatter = new HelpFormatter();
		Options options = new Options();
		options.addOption(this.getOption());
		formatter.setWidth(100);
		System.out.println("\n");
		formatter.printHelp("Detailed informations/commands for " + fc + " :", options);
	}
}