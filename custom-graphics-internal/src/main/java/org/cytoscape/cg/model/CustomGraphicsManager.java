package org.cytoscape.cg.model;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;

import org.cytoscape.cg.internal.image.MissingImageCustomGraphics;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;

@SuppressWarnings("rawtypes")
public interface CustomGraphicsManager {
	
	/**
	 * Use this as a property key to override the default behavior of using the factory's supported class name
	 * (see {@link CyCustomGraphicsFactory#getSupportedClass()}) as the key when registering a 
	 * {@link CyCustomGraphicsFactory} through {@link #addCustomGraphicsFactory(CyCustomGraphicsFactory, Map)}.
	 * The property value must also be a String.
	 */
	static final String SUPPORTED_CLASS_ID = "SUPPORTED_CLASS_ID";

	void addCustomGraphicsFactory(CyCustomGraphicsFactory factory, Map<?, ?> props);
	void removeCustomGraphicsFactory(CyCustomGraphicsFactory factory, Map<?, ?> props);
	CyCustomGraphicsFactory getCustomGraphicsFactory(Class<? extends CyCustomGraphics> cls);
	CyCustomGraphicsFactory getCustomGraphicsFactory(String className);
	Collection<CyCustomGraphicsFactory> getAllCustomGraphicsFactories();
	Collection<CyCustomGraphicsFactory> getCustomGraphicsFactories(Class<? extends CyIdentifiable> targetType);

	
	void addCustomGraphics(CyCustomGraphics cg, URL source);
	
	Collection<CyCustomGraphics> getAllCustomGraphics();
	Collection<CyCustomGraphics> getAllCustomGraphics(boolean sorted);
	Collection<CyCustomGraphics> getAllPersistantCustomGraphics();
	
	CyCustomGraphics getCustomGraphicsByID(Long id);
	CyCustomGraphics getCustomGraphicsBySourceURL(URL source);
	
	SortedSet<Long> getIDSet();
	
	Properties getMetadata();
	
	boolean isUsedInCurrentSession(CyCustomGraphics graphics);
	void setUsedInCurrentSession(CyCustomGraphics graphics, Boolean isUsed);
	
	void removeAllCustomGraphics();
	void removeCustomGraphics(Long id);
	
	/**
	 * Provides id available ID;
	 * @return 
	 */
	Long getNextAvailableID();
	
	void addMissingImageCustomGraphics(MissingImageCustomGraphics<?> cg);
	Collection<MissingImageCustomGraphics<?>> reloadMissingImageCustomGraphics();
	
	boolean isSupportedImageFile(File file);
}
