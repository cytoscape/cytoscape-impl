package org.cytoscape.internal.command;

import java.util.List;

import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.util.EdgeList;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.util.AbstractBounded;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedFloat;
import org.cytoscape.work.util.BoundedInteger;
import org.cytoscape.work.util.BoundedLong;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

public class HelpGenerator {
	
	private AvailableCommands availableCommands;

	public HelpGenerator(AvailableCommands availableCommands) {
		this.availableCommands = availableCommands;
	}
	
	
	public void generateHelpHTML(String input, MessageHandler resultsText) {
		String tokens[] = input.split(" ");
		
		if(tokens.length == 1)
			helpListNamespaces(resultsText);
		else if(tokens.length == 2 && tokens[1].equals("all"))
			helpAll(resultsText);
		else if(tokens.length == 2)
			helpNamespace(tokens[1], resultsText);
		else if(tokens.length > 2)
			helpCommand(tokens, resultsText);
	}
	
	
	private void helpListNamespaces(MessageHandler resultsText) {
		// Return all of the namespaces
		List<String> namespaces = availableCommands.getNamespaces();
		resultsText.appendMessage("Available namespaces:");
		for (String ns: namespaces) {
			resultsText.appendMessage("   " + ns);
		}
	}
	
	
	private void helpAll(MessageHandler resultsText) {
		for(String namespace: availableCommands.getNamespaces()) {
			resultsText.appendMessage(namespace);
			for (String command: availableCommands.getCommands(namespace)) {
				command = command.trim();
				generateArgumentHelp(namespace, command, resultsText);
				resultsText.appendMessage("<br/>");
			}
		}
	}
	
	
	private void helpNamespace(String namespace, MessageHandler resultsText) {
		// Get all of the commands for the given namespace
		List<String> commands = availableCommands.getCommands(namespace);
		if(commands.size() == 0) {
			resultsText.appendError("Can't find "+namespace+" namespace");
			return;
		}
		resultsText.appendMessage("Available commands:");
		// TODO: Need to get the description for this command
		for (String command: commands) {
			String desc = availableCommands.getDescription(namespace, command);
			if (desc != null && desc.length() > 0)
				resultsText.appendMessage("&nbsp;&nbsp;<b>"+namespace+" "+command+"</b>&nbsp;&nbsp;<i>"+desc+"</i>");
			else
				resultsText.appendMessage("&nbsp;&nbsp;<b>"+namespace+" "+command+"</b>");
		}
	}
	
	
	private void helpCommand(String[] tokens, MessageHandler resultsText) {
		// Get all of the arguments for a specific command
		String command = "";
		for (int i = 2; i < tokens.length; i++) command += tokens[i]+" ";
		command = command.trim();
		// First, do a little sanity checking
		boolean found = false;
		List<String> commands = availableCommands.getCommands(tokens[1]);
		for (String c: commands) {
			if (c.equalsIgnoreCase(command)) {
				found = true;
				break;
			}
		}
		if (!found) {
			resultsText.appendError("Can't find command "+tokens[1]+" "+command);
			return;
		}

		generateArgumentHelp(tokens[1], command, resultsText);
	}

	
	private void generateArgumentHelp(String namespace, String command, MessageHandler resultsText) {
		String longDescription = availableCommands.getLongDescription(namespace, command);
		String message = "";
		// System.out.println("generateArgumentHelp");
		if (longDescription != null) {
			// System.out.println("longDescription = "+longDescription);
			// Do we have an HTML string?
			if (longDescription.trim().startsWith("<html>") || longDescription.trim().startsWith("<HTML>")) {
				// Yes.  Strip off the "<html></html>" wrapper
				longDescription = longDescription.trim().substring(6);
				longDescription = longDescription.substring(0,longDescription.length()-7);
				// System.out.println("longDescription(html) = "+longDescription);
			} 
//			else {
//				// No, pass it through the markdown converter
//				Parser parser = Parser.builder().build();
//				Node document = parser.parse(longDescription);
//				HtmlRenderer renderer = HtmlRenderer.builder().build();
//				longDescription = renderer.render(document);
//				// System.out.println("longDescription(markdown) = "+longDescription);
//			}
			message += longDescription;
		}
		List<String> argList = availableCommands.getArguments(namespace, command);
		message += "<br/><br/><b>"+namespace+" "+command+"</b> arguments:";
		// resultsText.appendMessage(commandArgs);
		message += "<dl style='list-style-type:none;margin-top:0px;color:blue'>";
		for (String arg: argList) {
			message += "<dt>";
			if (availableCommands.getArgRequired(namespace, command, arg)) {
				message += "<b>"+arg+"</b>";
			} else {
				message += arg;
			}
			message += "="+getTypeString(namespace, command, arg);
			message += ": ";
			message += "</dt>";
			message += "<dd>";
			message += normalizeArgDescription(availableCommands.getArgDescription(namespace, command, arg),
			                                   availableCommands.getArgLongDescription(namespace, command, arg));
			message += "</dd>";
		}
		resultsText.appendMessage(message+"</dl>");
	}
	
	
	private String getTypeString(String namespace, String command, String arg) {
		Class<?> clazz = availableCommands.getArgType(namespace, command, arg);
		Object object = availableCommands.getArgValue(namespace, command, arg);
		String keywords = keyword("all")+"|"+keyword("selected")+"|"+keyword("unselected");
		// Special handling for various types
		if (clazz.equals(NodeList.class)) {
			String args = "["+variable("nodeColumn:value")+"|"+
			              variable("node name")+keyword(",")+"...]|"+keywords;
			return fixedSpan(args);
		} else if (clazz.equals(EdgeList.class)) {
			String args = "["+variable("edgeColumn:value")+"|"+
			              variable("edge name")+keyword(",")+"...]|"+keywords;
			return fixedSpan(args);
		} else if (clazz.equals(CyNetwork.class)) {
			return fixedSpan(keyword("current")+"|["+variable("column:value")+"|"+variable("network name")+"]");
		} else if (clazz.equals(CyTable.class)) {
			String args = keyword("Node:")+variable("network name")+"|"+
			              keyword("Edge:")+variable("network name")+"|"+
			              keyword("Network:")+variable("network name")+"|"+
			              variable("table name");
			return fixedSpan(args);
		} else if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
			return fixedSpan(keyword("true")+"|"+keyword("false"));
		} else if (clazz.equals(ListSingleSelection.class)) {
			if (object != null) {
				ListSingleSelection lss = (ListSingleSelection)object;
				String str = "&lt;"+classString(clazz.getSimpleName())+"&nbsp(";
				List<Object> list = lss.getPossibleValues();
				for (int index = 0; index < list.size()-1; index++) { 
					str += keyword(list.get(index).toString())+"|"; 
				}
				if (!list.isEmpty())
					str += keyword(list.get(list.size()-1).toString()); 
				str += ")&gt;";

				return fixedSpan(str);
			}
		} else if (clazz.equals(ListMultipleSelection.class)) {
			if (object != null) {
				ListMultipleSelection lss = (ListMultipleSelection)object;
				String str = "&lt;"+classString(clazz.getSimpleName())+"&nbsp[";
				List<Object> list = lss.getPossibleValues();
				for (int index = 0; index < list.size()-1; index++) { 
					str += keyword(list.get(index).toString())+","; 
				}
				if (!list.isEmpty())
					str += keyword(list.get(list.size()-1).toString());
				str += "]&gt;";

				return fixedSpan(str);
			}
		} else if (clazz.equals(BoundedDouble.class) || clazz.equals(BoundedFloat.class) ||
	 	          clazz.equals(BoundedInteger.class) || clazz.equals(BoundedLong.class)) {
			if (object != null)
				return boundedTypeString(clazz, object);
		}
		return fixedSpan("&lt;"+classString(clazz.getSimpleName())+"&gt;");
	}
	
	
	private String fixedSpan(String s) {
		return "<span style='font-family:Courier;color:black'>"+s+"</span>";
	}

	
	private String keyword(String s) {
		return "<span style='font-family:Courier;color:#CC00CC'>"+s+"</span>";
	}

	
	private String variable(String s) {
		return "<span style='font-family:Courier;color:#A000A0;font-style:italics'>"+s+"</span>";
	}

	
	private String classString(String s) {
		return "<span style='font-family:Courier;color:#FF00FF;font-style:italics'>"+s+"</span>";
	}
	
	
	private String boundedTypeString(Class<?> type, Object object) {
		if (object instanceof AbstractBounded) {
			AbstractBounded ab = (AbstractBounded)object;
			String str = "&lt;"+classString(type.getSimpleName())+"&nbsp;(";
			str += ab.getLowerBound().toString() + "&lt;";
			if (!ab.isLowerBoundStrict())
				str += "=";
			if (ab.getValue() != null) {
				str += ab.getValue().toString();
			} else {
				str += classString(ab.getLowerBound().getClass().getSimpleName());
			}
			str += "&lt;";
			if (!ab.isUpperBoundStrict())
				str += "=";
			str += ab.getUpperBound().toString() + ")&gt;";
			return fixedSpan(str);
		} else {
			return fixedSpan("&lt;"+classString(type.getSimpleName())+"&gt;");
		}
	}
	
	
	private String normalizeArgDescription(String desc, String longDesc) {
		if (longDesc != null && longDesc.length() > 0) {
			return longDesc;
		}

		if (desc != null) {
			desc = desc.trim();
			if (desc.endsWith(":")) desc = desc.substring(0, desc.length() - 1);
		}

		return desc;
	}
}
