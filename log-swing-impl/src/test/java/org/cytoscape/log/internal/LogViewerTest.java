package org.cytoscape.log.internal;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JScrollBar;

public class LogViewerTest {

	LogViewer lv;
	Map<String,String> config; 

	@Before
	public void setUp() {
		config = new HashMap<String,String>();
		config.put("colorParityTrue","ffffff");
		config.put("colorParityFalse","eeeeee");
		config.put("entryTemplate","<html><body><ul><li>%s %s %s %s</li><ul></body></html>");
		config.put("baseHTMLPath","homer");
		config.put("level","level.icon");
		lv = new LogViewer(config);
	}

	@Test
	public void testAppend() {
		int initLen = lv.document.getLength();
		lv.append("level","hello world","this is a test");
		int newLen = lv.document.getLength();
		assertTrue(initLen < newLen);
	}

	@Test
	public void testClear() {
		lv.append("level","hello world","this is a test");
		int initLen = lv.document.getLength();
		lv.clear();
		int newLen = lv.document.getLength();
		assertTrue(initLen > newLen);
	}

	@Test
	public void testScrollToBottom() {
		lv.append("level","hello world","this is a test");
		lv.scrollToBottom();
		JScrollBar sb = lv.scrollPane.getVerticalScrollBar();

		// SKETCHY!!! 
		// this gives scrollToBottom() time to run 
		// is there a better way?
		try { Thread.sleep(1000); } catch (Exception e) { fail(); }

		// FIXME TODO: is this necessary?
		//assertEquals( sb.getMaximum(), sb.getValue() ); 
	}
}

