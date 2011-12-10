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


public class AppLoaderTask2 extends AbstractTask {
	
	private static final Logger logger = LoggerFactory.getLogger( AppLoaderTask2.class );
	private static final String APP_TAG = "Cytoscape-App";
	
	private  CyAppAdapter adapter;

	private File filename;
	
	// All app URLs are saved here.  This means app conflict can happen in this ClassLoader.
	// If app developers want to avoid it, they need to try regular bundle app.
	private Set<URL> urls= AppLoaderTaskFactory.urls;

	public AppLoaderTask2(CyAppAdapter adapter) {
		this.adapter = adapter;
	}

	public void setFile(File filename){
		this.filename = filename;
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		Object app = null;
		JarFile jar = null;
		
		try {
			jar = new JarFile(filename);
			logger.debug("attempting to load simple app jar: " + filename);
			
			final String name = jar.getManifest().getMainAttributes().getValue(APP_TAG);
			logger.debug("attempting to load CyApp class: " + name);
			if ( name == null || name.isEmpty() )
				throw new IllegalArgumentException("This app jar is missing the \"Cytoscape-App: your.package.YourCyApp\" entry in the META-INF/MANIFEST.MF file. Without that entry we can't start the app!");
			
			final URL jarurl = filename.toURI().toURL();
			final ClassLoader parentLoader = adapter.getClass().getClassLoader();
			urls.add(jarurl);
			final MyClassLoader ucl = new MyClassLoader(parentLoader);
			final Class<?> c = ucl.loadClass(name);
			
			final Constructor<?> con = c.getConstructor(CyAppAdapter.class);
			app = con.newInstance(adapter);
			logger.info("App loaded: " + app);
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
