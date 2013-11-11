package org.cytoscape.rest.internal.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.rest.RESTResource;
import org.cytoscape.rest.internal.handlers.MessageHandler;
import org.cytoscape.rest.internal.handlers.TextHTMLHandler;
import org.cytoscape.rest.internal.handlers.TextPlainHandler;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskObserver;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLevel;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/commands/")
public class NamespacesResource implements RESTResource, PaxAppender, TaskObserver {
	AvailableCommands available = null;
	CommandExecutorTaskFactory ceTaskFactory = null;
	CyServiceRegistrar serviceRegistrar = null;
	MessageHandler messageHandler = null;
	SynchronousTaskManager taskManager = null;
	boolean processingCommand = false;

	public NamespacesResource(CyServiceRegistrar serviceRegistrar, AvailableCommands available,
			SynchronousTaskManager taskManager, CommandExecutorTaskFactory ceTaskFactory) {
		this.available = available;
		this.ceTaskFactory = ceTaskFactory;
		this.serviceRegistrar = serviceRegistrar;
		this.taskManager = taskManager;
	}

	/**
	 * Method handling HTTP GET requests to enumerate all namespaces. The
	 * returned list will be sent to the client as "text/plain" media type.
	 * 
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String enumerateNamespaces() {
		MessageHandler handler = new TextPlainHandler();
		List<String> namespaces = available.getNamespaces();
		handler.appendCommand("Available namespaces:");
		for (String namespace : namespaces) {
			handler.appendMessage("  " + namespace);
		}
		return handler.getMessages();
	}

	/**
	 * Method handling HTTP GET requests to enumerate all namespaces. The
	 * returned list will be sent to the client as "text/html" media type.
	 * 
	 * @return String that will be returned as a text/html response.
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String enumerateNamespacesHtml() {
		MessageHandler handler = new TextHTMLHandler();
		List<String> namespaces = available.getNamespaces();
		handler.appendCommand("Available namespaces:");
		for (String namespace : namespaces) {
			handler.appendMessage(namespace);
		}
		return handler.getMessages();
	}

	/**
	 * Method to enumerate all commands for a given namespace
	 * 
	 * @param namespace
	 * @return list of commands as text/plain
	 */
	@GET
	@Path("/{namespace}")
	@Produces(MediaType.TEXT_PLAIN)
	public String enumerateCommands(@PathParam("namespace") String namespace) {
		MessageHandler handler = new TextPlainHandler();
		List<String> commands = available.getCommands(namespace);
		handler.appendCommand("Available commands for '" + namespace + "':");
		for (String command : commands) {
			handler.appendMessage("  " + command);
		}
		return handler.getMessages();
	}

	/**
	 * Method to enumerate all commands for a given namespace
	 * 
	 * @param namespace
	 * @return list of commands as text/html
	 */
	@GET
	@Path("/{namespace}")
	@Produces(MediaType.TEXT_HTML)
	public String enumerateHTMLCommands(@PathParam("namespace") String namespace) {
		MessageHandler handler = new TextHTMLHandler();
		List<String> commands = available.getCommands(namespace);
		handler.appendCommand("Available commands for '" + namespace + "':");
		for (String command : commands) {
			handler.appendMessage("  " + command);
		}
		return handler.getMessages();
	}

	/**
	 * Method to enumerate all arguments for a given namespace and command or
	 * execute a namespace and command if query strings are provided
	 * 
	 * @param namespace
	 * @param command
	 * @param uriInfo
	 *            this provides access to the query strings
	 * @return list of arguments as text/plain or the results of executing the
	 *         command
	 */
	@GET
	@Path("/{namespace}/{command}")
	@Produces(MediaType.TEXT_PLAIN)
	public String handleCommand(@PathParam("namespace") String namespace, @PathParam("command") String command,
			@Context UriInfo uriInfo) {
		MessageHandler handler = new TextPlainHandler();
		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters(true);
		return handleCommand(namespace, command, queryParameters, handler);
	}

	/**
	 * Method to enumerate all arguments for a given namespace and command or
	 * execute a namespace and command if query strings are provided
	 * 
	 * @param namespace
	 * @param command
	 * @param uriInfo
	 *            this provides access to the query strings
	 * @return list of arguments as text/html or the results of executing the
	 *         command
	 */
	@GET
	@Path("/{namespace}/{command}")
	@Produces(MediaType.TEXT_HTML)
	public String handleHTMLCommand(@PathParam("namespace") String namespace, @PathParam("command") String command,
			@Context UriInfo uriInfo) {
		MessageHandler handler = new TextHTMLHandler();
		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters(true);
		return handleCommand(namespace, command, queryParameters, handler);
	}

	private String handleCommand(String namespace, String command, MultivaluedMap<String, String> queryParameters,
			MessageHandler handler) {
		List<String> args = available.getArguments(namespace, command);

		if ((queryParameters != null && queryParameters.size() > 0) || (args == null || args.size() == 0)) {
			// Execute!
			return executeCommand(namespace, command, queryParameters, handler);
		}

		handler.appendCommand("Available arguments for '" + namespace + " " + command + "':");
		for (String arg : args) {
			handler.appendMessage("  " + arg);
		}
		return handler.getMessages();
	}

	private String executeCommand(String namespace, String command, MultivaluedMap<String, String> args,
			MessageHandler handler) {

		List<String> commands = available.getCommands(namespace);
		if (commands == null || commands.size() == 0) {
			handler.appendError("Error: no such namespace: '" + namespace + "'");
			return handler.getMessages();
		}

		boolean nocom = true;
		for (String com : commands) {
			if (com.equalsIgnoreCase(command)) {
				nocom = false;
				break;
			}
		}
		if (nocom) {
			handler.appendError("Error: no such command: '" + command + "'");
			return handler.getMessages();
		}

		List<String> argList = available.getArguments(namespace, command);
		Map<String, Object> modifiedSettings = new HashMap<String, Object>();

		for (String inputArg : args.keySet()) {
			boolean found = false;
			for (String arg : argList) {
				String[] bareArg = arg.split("=");
				if (bareArg[0].equalsIgnoreCase(inputArg)) {
					found = true;
					modifiedSettings.put(bareArg[0], stripQuotes(args.getFirst(inputArg)));
					break;
				}
			}
			if (!found) {
				handler.appendError("Error: can't find argument '" + inputArg + "'");
				return handler.getMessages();
			}
		}

		processingCommand = true;
		messageHandler = handler;

		taskManager.execute(ceTaskFactory.createTaskIterator(namespace, command, modifiedSettings, this), this);

		String messages = messageHandler.getMessages();
		System.out.println("Returning " + messages);
		return messages;
	}

	public void doAppend(PaxLoggingEvent event) {
		System.out.println(event.getLevel().toInt() + ": " + event.getMessage());
		// Get prefix
		// Handle levels
		if (!processingCommand) {
			return;
		}

		PaxLevel level = event.getLevel();
		if (level.toInt() == 40000)
			messageHandler.appendError(event.getMessage());
		else if (level.toInt() == 30000)
			messageHandler.appendWarning(event.getMessage());
		else
			messageHandler.appendMessage(event.getMessage());
	}

	public void taskFinished(ObservableTask t) {
		Object res = t.getResults(String.class);
		if (res != null)
			messageHandler.appendResult(res);
	}

	public void allFinished(FinishStatus status) {
		if (status.getType().equals(FinishStatus.Type.SUCCEEDED))
			messageHandler.appendMessage("Finished");
		else if (status.getType().equals(FinishStatus.Type.CANCELLED))
			messageHandler.appendWarning("Cancelled by user");
		else if (status.getType().equals(FinishStatus.Type.FAILED)) {
			if (status.getException() != null)
				messageHandler.appendError("Failed: " + status.getException().getMessage());
			else
				messageHandler.appendError("Failed");
		}
	}

	private String stripQuotes(String quotedString) {
		String tqString = quotedString.trim();
		if (tqString.startsWith("\"") && tqString.endsWith("\""))
			return tqString.substring(1, tqString.length() - 1);
		return tqString;
	}
}