package org.cytoscape.commandDialog.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.cytoscape.commandDialog.internal.handlers.CommandHandler;
import org.cytoscape.commandDialog.internal.tasks.CommandDialogTaskFactory;
import org.cytoscape.commandDialog.internal.tasks.PauseCommandTaskFactory;
import org.cytoscape.commandDialog.internal.tasks.RunCommandsTaskFactory;
import org.cytoscape.commandDialog.internal.tasks.RunCommandsTask;
import org.cytoscape.commandDialog.internal.tasks.QuitTaskFactory;
import org.cytoscape.commandDialog.internal.tasks.SleepCommandTaskFactory;
import org.cytoscape.commandDialog.internal.ui.CommandToolDialog;

import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyShutdown;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.logging.spi.PaxAppender;


public class CyActivator extends AbstractCyActivator {
	private static Logger logger = LoggerFactory
			.getLogger(org.cytoscape.commandDialog.internal.CyActivator.class);

	public CyActivator() {
		super();
	}

	static Properties ezProps(String... args) {
		final Properties props = new Properties();
		for (int i = 0; i < args.length; i += 2)
			props.setProperty(args[i], args[i + 1]);
		return props;
	}

	public void start(BundleContext bc) {

		// See if we have a graphics console or not
		boolean haveGUI = true;
		ServiceReference ref = bc.getServiceReference(CySwingApplication.class.getName());

		if (ref == null) {
			haveGUI = false;
		}

		// Get a handle on the CyServiceRegistrar
		AvailableCommands availableCommands = getService(bc, AvailableCommands.class);
		CommandExecutorTaskFactory commandExecutor = getService(bc, CommandExecutorTaskFactory.class);
		SynchronousTaskManager taskManager = getService(bc, SynchronousTaskManager.class);
		CommandHandler commandHandler = new CommandHandler(availableCommands, commandExecutor, taskManager);
		final CommandToolDialog dialog;

		// Register ourselves as a listener for CyUserMessage logs
		registerService(bc, commandHandler, PaxAppender.class, 
		                ezProps("org.ops4j.pax.logging.appender.name", 
		                        "TaskMonitorShowMessagesAppender"));

		// Get any command line arguments.  The "-S" and "-R" are ours
		CyProperty<Properties> commandLineProps = getService(bc, CyProperty.class, "(cyPropertyName=commandline.props)");
		Properties p = commandLineProps.getProperties();
		final String scriptFile;
		if (p.getProperty("scriptFile") != null)
			scriptFile = p.getProperty("scriptFile");
		else
			scriptFile = null;

		if (haveGUI) {
			CySwingApplication swingApp = (CySwingApplication) getService(bc, CySwingApplication.class);
			// Create our dialog -- we only want one of these instantiated
			dialog = new CommandToolDialog(swingApp.getJFrame(), commandHandler);
			// Menu task factories
			TaskFactory commandDialog = new CommandDialogTaskFactory(dialog);
			Properties commandDialogProps = new Properties();
			commandDialogProps.setProperty(PREFERRED_MENU, "Tools");
			commandDialogProps.setProperty(TITLE, "Command Line Dialog");
			commandDialogProps.setProperty(COMMAND, "open dialog");
			commandDialogProps.setProperty(COMMAND_NAMESPACE, "command");
			commandDialogProps.setProperty(COMMAND_DESCRIPTION, "Open the command line dialog");
			commandDialogProps.setProperty(IN_MENU_BAR, "true");
			registerService(bc, commandDialog, TaskFactory.class, commandDialogProps);

			TaskFactory pauseCommand = new PauseCommandTaskFactory(swingApp.getJFrame());
			Properties pauseProperties = new Properties();
			pauseProperties.setProperty(COMMAND_NAMESPACE, "command");
			pauseProperties.setProperty(COMMAND_DESCRIPTION, 
			                            "Display a message and pause command processing until the user continues it");
			pauseProperties.setProperty(COMMAND, "pause");
			registerService(bc, pauseCommand, TaskFactory.class, pauseProperties);
		} else {
			dialog = null;
		}

		TaskFactory runCommand = new RunCommandsTaskFactory(dialog, commandHandler);
		Properties runCommandProps = new Properties();
		runCommandProps.setProperty(PREFERRED_MENU, "Tools");
		runCommandProps.setProperty(TITLE, "Execute Command File");
		runCommandProps.setProperty(COMMAND, "run");
		runCommandProps.setProperty(COMMAND_NAMESPACE, "command");
		runCommandProps.setProperty(COMMAND_DESCRIPTION, "Run a series of commands from a file");
		runCommandProps.setProperty(IN_MENU_BAR, "true");
		registerService(bc, runCommand, TaskFactory.class, runCommandProps);

		CyShutdown shutdown = getService(bc, CyShutdown.class);
		TaskFactory quitCommand = new QuitTaskFactory(shutdown);
		Properties quitCommandProps = new Properties();
		quitCommandProps.setProperty(COMMAND, "quit");
		quitCommandProps.setProperty(COMMAND_NAMESPACE, "command");
		quitCommandProps.setProperty(COMMAND_DESCRIPTION, "Exit Cytoscape");
		registerService(bc, quitCommand, TaskFactory.class, quitCommandProps);

		TaskFactory sleepCommand = new SleepCommandTaskFactory();
		Properties sleepProperties = new Properties();
		sleepProperties.setProperty(COMMAND_NAMESPACE, "command");
		sleepProperties.setProperty(COMMAND_DESCRIPTION, "Stop command processing for a specified time");
		sleepProperties.setProperty(COMMAND, "sleep");
		registerService(bc, sleepCommand, TaskFactory.class, sleepProperties);

		if (scriptFile != null) {
			registerService(bc, new StartScript(scriptFile, commandHandler, dialog), 
			                AppsFinishedStartingListener.class, new Properties());
		}

	}

	class StartScript implements AppsFinishedStartingListener {
		String scriptFile;
		CommandToolDialog dialog;
		RunCommandsTask runTask;
		CommandHandler commandHandler;

		protected StartScript(String scriptFile, CommandHandler commandHandler, CommandToolDialog dialog)  {
			this.scriptFile = scriptFile;
			this.dialog = dialog;
			this.commandHandler = commandHandler;
			runTask = new RunCommandsTask(dialog, commandHandler);
		}

		public void handleEvent(AppsFinishedStartingEvent event) {
			if (dialog != null && scriptFile != null) {
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						// TODO: need a non-gui approach to this!
						dialog.executeCommand("command run file="+scriptFile);
					}
				});
			} else if (dialog == null) {
				runTask.executeCommandScript(scriptFile, null);
			}
		}
	}
}
