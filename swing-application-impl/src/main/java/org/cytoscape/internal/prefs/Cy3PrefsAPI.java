package org.cytoscape.internal.prefs;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.cytoscape.internal.prefs.lib.ProxyConfig;

public class Cy3PrefsAPI extends Prefs {

	public Cy3PrefsAPI()
	{
		instance = this;
	}
	public ProxyConfig getProxy() {		return null;	}
	public void setProxyConfig(ProxyConfig config) {}

	@Override	public File getPrefsFile() {		return null;	}
	@Override	public boolean canSetPrefs() {		return false;	}
	
	public static void readProperties()
	{
		String startPath = System.getProperty("user.home") + "/CytoscapeConfiguration";
		File cytoDir = new File(startPath);
		if (cytoDir.exists())
		{
			List<File> propFiles = AbstractPrefsPanel.collectFiles(startPath, ".props");
			for (File f : propFiles)
			{
				String namespace = f.getName();
				namespace = namespace.substring(0, namespace.length() - 6);  // trim .props
				Map<String, String> list = AbstractPrefsPanel.getPropertyMap(f.getName());
				if (!list.isEmpty())
				{
					for (String key : list.keySet())
						instance.put(namespace + "." + key, list.get(key));		
				}
				System.out.println(f.getName() + " " + list.entrySet().size());
			}
		}
	}
}
