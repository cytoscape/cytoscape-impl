package org.cytoscape.work.internal.tunables;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.AbstractBounded;


/**
 * Commandline handler for the type <i>Bounded</i> of <code>Tunable</code>
 *
 * @author pasteur
 */
public class BoundedCLHandler<T extends AbstractBounded<?>> extends AbstractCLHandler {
	/**
	 * An abstract bounded object
	 */
	private T bo;

	/**
	 * Lower or equal sign for the lower bound
	 */
	private String lbound="\u2264";

	/**
	 * Lower or equal sign for the upper bound
	 */
	private String ubound="\u2264";



	/**
	 * Constructs the <code>CLHandler</code> for the <code>Bounded</code> type of a Field <code>f</code>
	 *
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	@SuppressWarnings("unchecked")
	public BoundedCLHandler(Field f, Object o, Tunable t) {
		super(f,o,t);

		try {
			bo = (T)getValue();
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}


	/**
	 * Constructs the <code>CLHandler</code> for the <code>Bounded</code> type of an Object managed by <i>get</i> and <i>set</i> methods
	 * @param gmethod method that returns the value of the Object <code>o</code> annotated as a <code>Tunable</code>
	 * @param smethod method that sets a value to the Object <code>o</code> annotated as a <code>Tunable</code>
	 * @param o Object whose value will be set and get by the methods
	 * @param t <code>Tunable</code> annotations of the Method <code>gmethod</code> annotated as <code>Tunable</code>
	 */
	public BoundedCLHandler(Method gmethod, Method smethod, Object o, Tunable t) {
		super(gmethod, smethod, o, t);
	}

	/**
	 * If options/arguments are detected for this handler, it applies the argument :
	 * <p><pre>
	 * <ul>
	 * <li> display some specific informations if the argument is <code>--cmd</code> ,or</li>
	 * <li> set the value of <code>bo</code> with the argument, between the 2 bounds, and set the Bounded Object <code>o</code> contained in <code>f</code> with <code>bo</code> ,or</li>
	 * <li> set the value of <code>bo</code> with the argument, and set <code>bo</code> as a parameter for the <code>set</code> Method </li>
	 * </ul>
	 * </pre></p>
	 *
	 * @param commandline with arguments
	 */
	@SuppressWarnings("unchecked")
	public void handleLine(CommandLine line) {
		final String fc = getName();
		try {
			if (line.hasOption(fc)) {
				if (line.getOptionValue(fc).equals("--cmd")) {
					displayCmds(fc);
					System.exit(1);
				}
				bo.setValue(line.getOptionValue(fc));
				setValue(bo);
			}
		} catch(Exception e) {
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
	 * <li> its description with its current value between bounds</li>
	 * </ul>
	 * </pre></p>
	 * @return option of the handler
	 */
	@SuppressWarnings("unchecked")
	public Option getOption() {
		final String fc = getName();

		if (bo.isLowerBoundStrict())
			lbound = "<";
		if (bo.isUpperBoundStrict())
			ubound = "<";

		T currentValue = null;
		try {
			currentValue = (T)getValue();
		} catch (final Exception e) {
			e.printStackTrace();
                        System.exit(1);
		}

		return new Option(fc, true, "-- " + getDescription() + " --\n  current value : " + currentValue
		                  + "\n  possible value : (" + bo.getLowerBound() + " " + lbound + " x "
		                  + ubound + " " + bo.getUpperBound() + ")");
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