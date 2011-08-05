package org.cytoscape.work.internal.tunables;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.cytoscape.work.Tunable;


/**
 * Commandline handler for the type <i>Double</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class DoubleCLHandler extends AbstractCLHandler {
	/**
	 * Constructs the <code>CLHandler</code> for the <code>Double</code> type of a Field <code>f</code>
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	public DoubleCLHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
	}

	/**
	 * Constructs the <code>CLHandler</code> for the <code>Double</code> type of an Object managed by <i>get</i> and <i>set</i> methods
	 * @param gmethod method that returns the value of the Object <code>o</code> annotated as a <code>Tunable</code>
	 * @param smethod method that sets a value to the Object <code>o</code> annotated as a <code>Tunable</code>
	 * @param o Object whose value will be set and get by the methods
	 * @param t <code>Tunable</code> annotations of the Method <code>gmethod</code> annotated as <code>Tunable</code>
	 */
	public DoubleCLHandler(Method gmethod, Method smethod, Object o, Tunable t) {
		super(gmethod, smethod, o, t);
	}

	/**
	 *  If options/arguments are detected for this handler, it applies the argument :
	 * <p><pre>
	 * <ul>
	 * <li> display some specific informations if the argument is <code>--cmd</code> ,or</li>
	 * <li> apply the value to the Double Object <code>o</code> contained in <code>f</code> ,or</li>
	 * <li> set the argument as a double parameter for the <code>set</code> Method</li>
	 * </ul>
	 * </pre></p>
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
				setValue(Double.parseDouble(line.getOptionValue(fc)));
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
	 * <li> its description with its current value</li>
	 * </ul>
	 * </pre></p>
	 * @return option of the handler
	 */
	public Option getOption() {
		final String fc = getName();
		Double currentValue = null;

		try {
			currentValue = (Double)getValue();
		} catch(final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		return new Option(fc, true, "-- " + getDescription() + " --\n  current value: " + currentValue);
	}

	/**
	 * Display some detailed informations to the user for this particular handler
	 * @param name of the handler
	 */
	private void displayCmds(String fc) {
		HelpFormatter formatter = new HelpFormatter();
		Options options = new Options();
		options.addOption(this.getOption());
		formatter.setWidth(100);
		System.out.println("\n");
		formatter.printHelp("Detailed informations/commands for " + fc + " :", options);
	}
}
