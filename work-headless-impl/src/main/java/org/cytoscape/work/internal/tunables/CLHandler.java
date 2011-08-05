package org.cytoscape.work.internal.tunables;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.cytoscape.work.TunableHandler;


/**
 * <code>Handler</code> that provides access to informations from the commandline.
 * It will be created for each detected <code>Tunable</code> to make a link between Cytoscape core and the user
 * 
 * @author pasteur
 *
 */
public interface CLHandler extends TunableHandler {
	/**
	 * Provides an <code>Option</code> that enable access to arguments of this <code>CLHandler</code> when parsing
	 * 
	 * @return the option that is required to interact with this <code>Handler</code> through commandline
	 */
	public Option getOption();
	
	/**
	 * Detects the <code>option</code> specific to the <code>CLHandler</code> from the <code>line</code>, and 
	 * set the arguments detected by the <code>option</code> to the Object contained in the <code>CLHandler</code>
	 * 
	 * @param line parsed arguments that can be used to retrieve options and arguments of <code>CLHandler</code>
	 */
	public void handleLine(final CommandLine line);
}
