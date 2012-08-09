package org.cytoscape.browser.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.browser.internal.util.TableColumnStat;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TableColumnStatFileIO {
	private static final Logger logger = LoggerFactory.getLogger(TableColumnStatFileIO.class);
	
	static String APP_NAME = "org.cytoscape.browser";
	
	public static void write (List<TableColumnStat> tableColStats, SessionAboutToBeSavedEvent e, String className){
		try {
			// Create an empty file on system temp directory
			File tmpFile = new File(System.getProperty("java.io.tmpdir"),  className);
			tmpFile.deleteOnExit();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
			for (TableColumnStat tcs : tableColStats){
					writer.write(tcs.toString());
				}
			writer.close();
			
			// Add it to the apps list
			List<File> fileList = new ArrayList<File>();
			boolean flag = false;
			if (e.getAppFileListMap().containsKey(APP_NAME)){
				fileList = e.getAppFileListMap().get(APP_NAME);
				flag = true;
			}
			fileList.add(tmpFile);
			if (!flag)
				e.addAppFiles(APP_NAME, fileList);
		} catch (Exception ex) {
			logger.error("Error adding table browser status files to be saved in the session.", ex);
		}
	}
	

	public static Map<String, TableColumnStat>  read ( SessionLoadedEvent e, String className){
		Map<String, TableColumnStat> tableColStats = new HashMap<String, TableColumnStat>();
		CySession sess = e.getLoadedSession();

		if (sess == null) 
			return null;
		Map<String, List<File>> filesMap = sess.getAppFileListMap();

		if (!filesMap.containsKey(APP_NAME))
			return null;

		List<File> files = filesMap.get(APP_NAME);
		if (files == null) 
			return null;

		for (File f : files) {
			if (f.getName().endsWith(className)) {

				try {
					InputStream is = new FileInputStream(f);

					final InputStreamReader reader = new InputStreamReader(is);
					final BufferedReader br = new BufferedReader(reader);
					String line = br.readLine();
					while (line != null){

						String[] split = line.split(",");
						if (split.length != 4)
							continue;

						String tableTitle = split[0];
						int colIndex = Integer.valueOf( split[1]);
						String colName = split[2];
						boolean visible = Boolean.valueOf(split[3]);
						
						if (!tableColStats.containsKey(tableTitle))
							tableColStats.put(tableTitle, new TableColumnStat(tableTitle));
						
						TableColumnStat tcs = tableColStats.get(tableTitle);
						tcs.addColumnStat(colName, colIndex, visible);
						line = br.readLine();

					}
				}catch(Exception ex){
					logger.error("Error reading table browser status files from session.", ex);
				}

				break;
			}
		}
		return tableColStats;
	}

}
