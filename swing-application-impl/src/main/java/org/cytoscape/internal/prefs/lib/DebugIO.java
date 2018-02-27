package org.cytoscape.internal.prefs.lib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class DebugIO {
	
	public static List<String> readPropertyFile(String fname) 
	{
		List<String> lines = null;
		File propsFile = getPropertyFile(fname);
		try {
			lines = Files.readAllLines(propsFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	//---------------------------------------------------------------------------------------------
	public static File getPropertyFile(String fname)	
	{
		return new File(System.getProperty("user.home") + "/CytoscapeConfiguration/" + fname);
	}
	
	public static Map<String, String> getPropertyMap(String fname)	
	{
		return readMap(getPropertyFile(fname));
	}
  public void overwriteProperties(String fName, Map<String, String> attributes)
  {
  		Map<String, String> extant = getPropertyMap(fName);
  		for (String key : attributes.keySet())
  			extant.put(key,  attributes.get(key));
  	
	    	try
	    	{
	    		writeMap(extant, getPropertyFile(fName));
	    	}
	    	catch (Exception e)
	    	{
	    		System.err.println("Cannot write property file: " + fName);
	    		e.printStackTrace();
	    	}
  }
	//---------------------------------

	public static Map<String, String> readMap(File f)
	{
		Map<String, String> map = new HashMap<String, String>();
		boolean firstLine = true;
		String raw = readFile(f);
		if (raw != null) 
		{
			String[] lines = raw.split("\n"); 
			for (String line : lines)
			{
				if (firstLine) { firstLine = false;	 continue; }
				if (line.startsWith("#")) {  continue; }				
				int delim = line.indexOf("=");
				if (delim>0)
					map.put(line.substring(0,delim),  line.substring(delim+1));
			}
		}
		return map;
	}
//	
	public static String readFile(File f) {
	StringBuffer accum = new StringBuffer();
	BufferedReader buffer = null;
	FileReader reader = null;
	try {
		reader = new FileReader(f);
		buffer = new BufferedReader(reader);
		String line;
		while ((line = buffer.readLine()) != null) {
			accum.append(line + '\n');
		}
	} catch (Exception e) {
		System.out.println("Error reading file: " + f + " :: " + e);

	} finally {
		close(buffer);
	}
	return accum.toString();
}
	public static boolean close(BufferedReader o) {
	if (o == null)
		return true;
	try {
		o.close();
	} catch (Exception ex) {
		return false;
	}
	return true;
}

	public static String getTimestamp() {
		SimpleDateFormat fmt = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
		Date date = new Date(System.currentTimeMillis());
		String stringDate = fmt.format(date);
		return "#" + stringDate; 
	}
	
	public static void writeMap(Map<String, String> props, File f) throws IOException
	{
		String time = getTimestamp() + "\n";
		StringBuilder buff = new StringBuilder(time);
		for (String prop : props.keySet())
			buff.append(prop).append("=").append(props.get(prop)).append("\n");
		write(f, buff.toString());
	}
	public static void write(File f, String content) throws IOException {
	f.createNewFile(); // ensure file exists
	
	if (content == null || content.isEmpty())
		return;
	Writer output = new BufferedWriter(new FileWriter(f));
	try {
		output.write(content);
	} finally {
		output.close();
	}
}
	
	
	public static List<File> collectFiles(String path, String suffix) {

		File root = new File(path);
		List<File> list = new ArrayList<File>();

		for (File f : root.listFiles()) {
			if (f.isDirectory()) {
				list.addAll(collectFiles(f.getAbsolutePath(), suffix));
			} else {
				if (f.getName().toLowerCase().endsWith(suffix.toLowerCase())) {
					list.add(f);
				}
			}
		}
		return list;

	}

	// ---------------------------------------------------------------------------------------------------------
	private void dumpPrefs(Map<String,String> props) {			// TODO
		
		Set<Entry<String, String>> entries = props.entrySet();
		Map<String, String> sorted = new TreeMap<String, String>(); 
		for (Entry<String, String> e : entries )
			sorted.put(e.getKey(), e.getValue());
		
		for (Entry<String, String> e : sorted.entrySet() )
			System.out.println(e.getKey() + " ==>  " + e.getValue());
	
	}

}
