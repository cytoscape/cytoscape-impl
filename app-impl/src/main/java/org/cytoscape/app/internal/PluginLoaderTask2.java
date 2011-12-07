package org.cytoscape.app.internal;


import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.jar.JarFile;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PluginLoaderTask2 extends AbstractTask {
	
	private static final Logger logger = LoggerFactory.getLogger( PluginLoaderTask2.class );
	private static final String PLUGIN_TAG = "Cytoscape-Plugin";
	
	private  CyAppAdapter adapter;

	private File filename;
	
	// All plugin URLs are saved here.  This means plugin conflict can happen in this ClassLoader.
	// If plugin developers want to avoid it, they need to try regular bundle plugin.
	private Set<URL> urls= PluginLoaderTaskFactory.urls;

	public PluginLoaderTask2(CyAppAdapter adapter) {
		this.adapter = adapter;
	}

	public void setFile(File filename){
		this.filename = filename;
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		Object plugin = null;
		JarFile jar = null;
		
		try {
			jar = new JarFile(filename);
			logger.debug("attempting to load simple plugin jar: " + filename);
			
			final String name = jar.getManifest().getMainAttributes().getValue(PLUGIN_TAG);
			logger.debug("attempting to load CyPlugin class: " + name);
			if ( name == null || name.isEmpty() )
				throw new IllegalArgumentException("This plugin jar is missing the \"Cytoscape-Plugin: your.package.YourCyPlugin\" entry in the META-INF/MANIFEST.MF file. Without that entry we can't start the plugin!");
			
			final URL jarurl = filename.toURI().toURL();
			final ClassLoader parentLoader = adapter.getClass().getClassLoader();
			urls.add(jarurl);
			final MyClassLoader ucl = new MyClassLoader(parentLoader);
			final Class<?> c = ucl.loadClass(name);
			
			final Constructor<?> con = c.getConstructor(CyAppAdapter.class);
			plugin = con.newInstance(adapter);
			logger.info("Plugin loaded: " + plugin);
		} finally {
			if (jar != null) 
				jar.close();
		}
	}

	@Override
	public void cancel() {
		// TODO: Implement this!
	}
	
	private final class MyClassLoader extends URLClassLoader {
		MyClassLoader(ClassLoader parent) {
			super(urls.toArray(new URL[0]), parent);
		}	
	}
}
