package org.cytoscape.io.internal.write;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.write.graphics.BitmapWriter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.PresentationWriterFactory;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */



import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
//import org.cytoscape.task.internal.io.ViewWriter;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.properties.TunablePropertySerializer;
import org.cytoscape.work.properties.TunablePropertySerializerFactory;


public class PresentationWriterManagerImpl extends AbstractWriterManager<PresentationWriterFactory> 
	implements PresentationWriterManager {
	
	private final CyServiceRegistrar registrar;
	private final Map<String,CyProperty<Properties>> registeredPropertyServices = new HashMap<>();

	public PresentationWriterManagerImpl(CyServiceRegistrar registrar) {
		super(DataCategory.IMAGE);
		this.registrar = registrar;
	}

	public CyWriter getWriter(View<?> view, RenderingEngine<?> re, CyFileFilter filter, File outFile) throws Exception {
		return getWriter(view,re,filter,new FileOutputStream(outFile));
	}

	public CyWriter getWriter(View<?> view, RenderingEngine<?> re, CyFileFilter filter, OutputStream os) throws Exception {
		PresentationWriterFactory tf = getMatchingFactory(filter);
		if ( tf == null )
			throw new NullPointerException("Couldn't find matching factory for filter: " + filter);
		
		CyWriter writer = tf.createWriter(os,re);
		loadTunablePropertyValues(writer);
		
		return new WrappedCyWriter(writer);
	}
	
	
	public class WrappedCyWriter implements CyWriter {
		
		@ContainsTunables
		public CyWriter cywriter; 
		
		public WrappedCyWriter(CyWriter writer) {
			this.cywriter = writer;
		}
		
		@Override
		public void run(TaskMonitor tm) throws Exception {
			cywriter.run(tm);
			saveTunablePropertyValues(cywriter);
		}
		@Override
		public void cancel() {
			cywriter.cancel();
		}
	}
	
	
	public void saveTunablePropertyValues(CyWriter writer) {
		TunablePropertySerializerFactory serializerFactory = registrar.getService(TunablePropertySerializerFactory.class);
		TunablePropertySerializer serializer = serializerFactory.createSerializer();
		Properties props = serializer.toProperties(writer);

		if(writer instanceof BitmapWriter) {
			// don't save the height and width of the network view, those are populated automatically
			props.remove("Height");
			props.remove("Width");
			props.remove("height");
			props.remove("width");
		}
		
		CyProperty<Properties> cyProperty = getPropertyService(writer);
		cyProperty.getProperties().clear();
		cyProperty.getProperties().putAll(props);
	}

	public void loadTunablePropertyValues(CyWriter writer) {
		CyProperty<Properties> cyProperty = getPropertyService(writer);
		Properties props = cyProperty.getProperties();
		
		if(!props.isEmpty()) {
			// use the Properties to restore the values of the Tunable fields
			TunablePropertySerializerFactory serializerFactory = registrar.getService(TunablePropertySerializerFactory.class);
			TunablePropertySerializer serializer = serializerFactory.createSerializer();
			serializer.setTunables(writer, props);
		}
	}
	
	private synchronized CyProperty<Properties> getPropertyService(CyWriter writer) {
		String key = writer.getClass().getCanonicalName();
		CyProperty<Properties> service = registeredPropertyServices.get(key);
		if(service == null) {
			service = PropsReader.forCyWriter(writer);
			Properties serviceProps = new Properties();
			serviceProps.setProperty("cyPropertyName", service.getName());
			registrar.registerAllServices(service, serviceProps);
			registeredPropertyServices.put(key, service);
		}
		return service;
	}
	
	
	private static class PropsReader extends AbstractConfigDirPropsReader {
		public PropsReader(String name, String fileName) {
			super(name, fileName, SavePolicy.CONFIG_DIR);
		}
		public static PropsReader forCyWriter(CyWriter writer) {
			String name = writer.getClass().getCanonicalName();
			if (name == null)
				return null;
			name = "writer." + name;
			return new PropsReader(name, name + ".props");
		}
	}
}
