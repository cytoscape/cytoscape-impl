package org.cytoscape.linkout.internal;


import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LinkoutTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(LinkoutTask.class);

	private final String link;
	private final CyTableEntry[] tableEntries;
	private final OpenBrowser browser;

	private static final String REGEX = "%.+%"; 
	private static final Pattern regexPattern = Pattern.compile(REGEX); 

	public LinkoutTask(String link, OpenBrowser browser, CyTableEntry... tableEntries ) {
		this.link = link;
		this.tableEntries = tableEntries;
		this.browser = browser;
	}

	@Override
	public void run(TaskMonitor tm) {

		String url = link;

		// This absurdity is to support backwards compatibility
		// with 2.x formatted links.
		if ( tableEntries.length == 1 ) {
			url = substituteAttributes(url, tableEntries[0], "ID");
		} else if ( tableEntries.length == 2 ) {
			url = substituteAttributes(url, tableEntries[0], "ID1");
			url = substituteAttributes(url, tableEntries[1], "ID2");
		} else if ( tableEntries.length == 3 ) {
			url = substituteAttributes(url, tableEntries[0], "ID1");
			url = substituteAttributes(url, tableEntries[1], "ID2");
			url = substituteAttributes(url, tableEntries[2], "ID");
		}

		logger.debug("LinkOut opening url: " + url);
		if (!browser.openURL(url))
			throw new RuntimeException("Problem opening linkout URL: " + url);
	}

	private String substituteAttributes(String url, CyTableEntry tableEntry, String id) {
	 
		// Replace %ATTRIBUTE.NAME% mark with the value of the attribute final
		Matcher mat = regexPattern.matcher(url);
		  
		while (mat.find()) { 
			String attrName = url.substring(mat.start() + 1, mat.end() - 1);
			String replaceName = attrName; 
		
			// handle the default case where ID, ID1, ID2 is now the "name" column 
			if ( attrName.equals(id) )
				attrName = "name";
		
			Object raw = tableEntry.getCyRow().getRaw(attrName);
			if ( raw == null ) {
				continue;
			}

			String attrValue = raw.toString(); 
			url = url.replace("%" + replaceName + "%", attrValue); 
			mat = regexPattern.matcher(url); 
		}
	  
		return url; 
	}
}
