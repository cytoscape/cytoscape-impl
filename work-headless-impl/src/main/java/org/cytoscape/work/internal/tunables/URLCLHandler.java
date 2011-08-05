package org.cytoscape.work.internal.tunables;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.cytoscape.work.Tunable;


/**
 * Commandline handler for a <i>URL</i> type of <code>Tunable</code>
 * 
 * @author pasteur
 */

public class URLCLHandler extends AbstractCLHandler implements CLHandler{
	
	
	/**
	 * URL object whose url will be set by this handler, to modify the original <code>URL</code> contained in <code>o</code>
	 */
	private URL url;
	
	
	/**
	 * Constructs the <code>CLHandler</code> for a <code>URL</code> Object contained in a Field <code>f</code>
	 * 
	 * @param f field that has been annotated
	 * @param o object contained in <code>f</code>
	 * @param t tunable associated to <code>f</code>
	 */
	public URLCLHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
		try{
			this.url = (URL) f.get(o);
		}catch(Exception e){e.printStackTrace();}
	}
	
	
	/**
	 * Constructs the <code>CLHandler</code> for a <code>URL</code> Object managed by <i>get</i> and <i>set</i> methods
	 * @param gmethod method that returns the value of the Object <code>o</code> annotated as a <code>Tunable</code>
	 * @param smethod method that sets a value to the Object <code>o</code> annotated as a <code>Tunable</code>
	 * @param o Object whose value will be set and get by the methods
	 * @param t <code>Tunable</code> annotations of the Method <code>gmethod</code> annotated as <code>Tunable</code>
	 */
	public URLCLHandler(Method gmethod, Method smethod, Object o, Tunable t) {
		super(gmethod, smethod, o, t);
	}
	
	/**
	 * If options/arguments are detected for this handler, it applies the argument : 
	 * <p><pre>
	 * <ul>
	 * <li> display some specific informations if the argument is <code>--cmd</code> ,or</li>
	 * <li> set the argument as a url for <code>url</code> and set the URL Object <code>o</code> contained in <code>f</code> with <code>url</code>,or</li>
	 * <li> set the argument as a url for <code>url</code> and set <code>url</code> as a parameter for the <code>set</code> Method</li>
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
				url = new URL(line.getOptionValue(fc));
				setValue(url);
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
	 * <li> its description with the current url</li>
	 * </ul>
	 * </pre></p>
	 * @return option of the handler
	 */
	public Option getOption() {
		final String fc = getName();
		URL currentValue = null;
		try {
			currentValue = (URL)getValue();
		} catch(final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return new Option(fc, true, "-- " + getDescription() + " --\n  current selected values : " + currentValue.toString());
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