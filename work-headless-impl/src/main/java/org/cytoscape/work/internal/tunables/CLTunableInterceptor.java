package org.cytoscape.work.internal.tunables;


import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.cytoscape.work.AbstractTunableInterceptor;
import org.cytoscape.cmdline.launcher.CommandLineProvider;


/**
 * Interceptor of <code>Tunable</code> that will be applied on <code>CLHandlers</code>.
 * 
 * <p><pre>
 * To intercept the new value that has to be set to the Object in the <code>CLHandler</code> : 
 * <ul>
 * <li> gets the specific arguments detected for the <code>TaskFactory</code> this Interceptor is applied to</li>
 * <li> gets the <code>option</code> for each <code>CLHandler</code> created</li>
 * <li> parses the arguments with the available <code>options</code></li>
 * <li> applies the previous parsed arguments to the Object contained in the <code>CLHandler</code></li>
 * </ul>
 * </pre></p>
 * 
 * 
 * @author pasteur
 *
 */
public class CLTunableInterceptor extends AbstractTunableInterceptor<CLHandler>{

	/**
	 * command line arguments
	 */
	private String[] args;
	
	/**
	 * Provider of commandline arguments in OSGi bundles
	 */
	private CommandLineProvider clp;
	
	
	/**
	 * Creates an Interceptor that will use a <code>CLHandlerFactory</code> to create <code>CLHandlers</code> for the intercepted <code>Tunables</code>.
	 * <code>CommandLineProvider</code> also gives access to commandline arguments for interception and parsing of <code>CLHandler</code>'s options and arguments
	 * @param clp Provider of commandline arguments
	 */
	public CLTunableInterceptor(CommandLineProvider clp ) {
		super(new CLHandlerFactory());
		this.clp = clp;
		this.args = clp.getCommandLineCompleteArgs();
	}

	/**
	 *  @return always null because we're not providing a GUI
	 */
	public JPanel getUI(final Object ...objs) {
		return null;
	}

	/**
	 * Parses and apply the right commandline arguments to the <code>Tunables</code>'s value that need to be modified.
	 * <p><pre>
	 * This method does the following actions :
	 * <ul>
	 * <li>Creates the <code>Option</code> for each <code>Handler</code> previously detected for the <code>Task</code> taken as an argument of <code>Object ... objs</code></li>
	 * <li>Retrieves the specific arguments that have been found for this <code>Task</code> in <code>CLTaskFactoryInterceptor</code></li>
	 * <li>Parses the previous options with these arguments</li>
	 * <li>Applies the arguments to perform the requested <code>Tunable</code> modification
	 * </ul>
	 * </pre></p>
	 * 
	 * Note : the option <code> -H </code> or <code> --fullHelp </code>  will automatically display a <i>Help</i> to the User that will show all the options for the <code>Handlers</code> found in the <code>Task</code>
	 * 
	 * @param an Object Array that contains <code>Tasks</code>
	 */
	public boolean execUI(Object ... objs) {

		List<CLHandler> lh = new ArrayList<CLHandler>();
		
		for (Object o : objs ) { 

			if ( !handlerMap.containsKey(o) )
				throw new IllegalArgumentException("Interceptor does not yet know about this object!");
		
			lh.addAll(handlerMap.get(o).values());
		}

		
		//to get the right arguments from the parser
		this.args = clp.getSpecificArgs();		
		
		
		//create the options for all the handlers
		Options options = new Options();

		if(lh.size()==0){
			if(args.length==0)args = new String[0];			
		}
		else{
			for ( CLHandler h : lh )
				options.addOption( h.getOption() );
			//add an "Help" option
			options.addOption("H", "fullHelp", false, "Display all the available Commands for this Task");			
		}

		
        //Try to parse the command line
        CommandLineParser parser = new PosixParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (ParseException pe) {
            System.err.println("Parsing command line failed: " + pe.getMessage()+"\n");
			
            if(options.getOptions().size()==0)
            	printHelp(options,"-"+objs[0].getClass().getSimpleName() + " does not take any argument\n",objs[0].getClass().getSimpleName(),false);
			else 
				printHelp(options,"Error in arguments of "+ objs[0].getClass().getSimpleName() + " -->  see options below",objs[0].getClass().getSimpleName(),true);
        }

        
        //Print the Help if -H is requested or if there is an error of parsing
        if (line.hasOption("H")) {
        	printHelp(options,"For the arguments of "+ objs[0].getClass().getSimpleName() + " -->  see options below",objs[0].getClass().getSimpleName(),true);
			System.exit(0);
        }

        //Set the new tunables with the arguments parsed for options
		for ( CLHandler h : lh )
			h.handleLine( line );
				
		return true;
	}
	
		
	/**
	 * Display the help to the user when it is requested ( <code> -H </code> or <code> --fullHelp </code> )or when the arguments can not be parsed with the options
	 * 
	 * @param options Handlers' options created for the intercepted <code>Task</code>
	 * @param info informative message displayed for the user
	 * @param taskName name of the <code>Task</code>
	 * @param hasOptions whether or not this <code>Task</code> has <code>Handlers</code> with options (that need to be displayed to the user)
	 * 
	 */
	private static void printHelp(Options options,String info,String taskName,boolean hasOptions) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(110);
		System.out.println(info);
		if(hasOptions)
			formatter.printHelp("\njava -Xmx512M -jar headless-cytoscape.jar -"+ taskName + " -<option> -<arg>","\noptions:", options,"\nTip : run \"java -jar headless-cytoscape.jar -<task> -<option> --cmd\" to get detailed help on this option");
		else
			formatter.printHelp("\njava -Xmx512M -jar headless-cytoscape.jar -"+ taskName,"",options,"");
		System.exit(1);
	}

	public boolean validateAndWriteBackTunables(Object... objs) {
		return false;
	}
}
