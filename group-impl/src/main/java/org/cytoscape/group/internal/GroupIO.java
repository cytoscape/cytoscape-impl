package org.cytoscape.group.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.internal.LockedVisualPropertiesManager.Key;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/*
 * #%L
 * Cytoscape Groups Impl (group-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

public class GroupIO implements SessionAboutToBeSavedListener, SessionAboutToBeLoadedListener, SessionLoadedListener {

	private static final String NAMESPACE = "org.cytoscape.group";
	private final static String FILENAME = "lockedVisualProperties.json";
	
	private final CyGroupManager groupMgr;
	private final LockedVisualPropertiesManager lvpMgr;
	private final CyServiceRegistrar serviceRegistrar;
	private final ObjectMapper mapper;
	
	private static final Logger logger = LoggerFactory.getLogger(GroupIO.class);
	
	public GroupIO(final CyGroupManager groupMgr,
					 final LockedVisualPropertiesManager lvpMgr,
					 final CyServiceRegistrar serviceRegistrar) {
		this.groupMgr = groupMgr;
		this.lvpMgr = lvpMgr;
		this.serviceRegistrar = serviceRegistrar;
		this.mapper = new ObjectMapper();
		
		final SimpleModule module = new SimpleModule();
		module.addSerializer(Map.class, new LockedVisualPropertiesMapSerializer());
		
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}
	
	@Override
	public void handleEvent(final SessionAboutToBeSavedEvent event) {
		final CyNetworkManager netMgr = serviceRegistrar.getService(CyNetworkManager.class);
		final Set<CyNetwork> networkSet = netMgr.getNetworkSet();
		final Set<CyGroup> allGroups = new LinkedHashSet<>();
		
		for (final CyNetwork net : networkSet) {
			final Set<CyGroup> groupSet = groupMgr.getGroupSet(net);
			
			if (groupSet != null)
				allGroups.addAll(groupSet);
		}
		
		if (allGroups.isEmpty())
			return;
		
		final List<File> files = new ArrayList<File>();
		
		try {
			final File root = File.createTempFile(NAMESPACE, ".temp");
			root.delete();
			root.mkdir();
			root.deleteOnExit();
			
			final File file = new File(root, FILENAME);
			writeLockedVisualPropertiesMap(file, allGroups);
			files.add(file);
			file.deleteOnExit();
			
			event.addAppFiles(NAMESPACE, files);
		} catch (Exception e) {
			logger.error("Unexpected error", e);
		}
	}
	
	@Override
	public void handleEvent(final SessionLoadedEvent event) {
		final List<File> files = event.getLoadedSession().getAppFileListMap().get(NAMESPACE);
		
		if (files == null)
			return;
		
		final CySession session = event.getLoadedSession();
		
		for (final File file : files) {
			try {
				if (file.getName().equals(FILENAME)) {
					final Map<Key, Map<VisualProperty<?>, Object>> map = readLockedVisualPropertiesMap(file, session);
					lvpMgr.addAll(map);
				}
			} catch (IOException e) {
				logger.error("Unexpected error", e);
			}
		}
	}

	@Override
	public void handleEvent(final SessionAboutToBeLoadedEvent event) {
		lvpMgr.reset();
	}
	
	private void writeLockedVisualPropertiesMap(final File file, final Set<CyGroup> groups)
			throws JsonGenerationException, JsonMappingException, IOException {
		final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		final Set<CyIdentifiable> grElements = new HashSet<>();
		
		for (final CyGroup gr : groups) {
			final Set<CyNetworkView> netViews = new HashSet<>();
			final Set<CyNetwork> networks = gr.getNetworkSet();
			
			for (final CyNetwork net : networks) {
				if (netViewMgr.viewExists(net))
					netViews.addAll(netViewMgr.getNetworkViews(net));
			}
			
			if (netViews.isEmpty())
				continue;
			
			grElements.add(gr.getGroupNode());
			grElements.addAll(gr.getRootNetwork().getAdjacentEdgeList(gr.getGroupNode(), CyEdge.Type.ANY));
			grElements.addAll(gr.getNodeList());
			grElements.addAll(gr.getExternalEdgeList());
			grElements.addAll(gr.getInternalEdgeList());
			
			for (final CyIdentifiable element : grElements)
				lvpMgr.saveLockedValues(element, netViews);
		}
		
		final Map<Key, Map<VisualProperty<?>, Object>> map = lvpMgr.getLockedVisualPropertiesMap();
		
		if (map != null)
			mapper.writeValue(file, map);
	}
	
	private Map<Key, Map<VisualProperty<?>, Object>> readLockedVisualPropertiesMap(final File file, final CySession session)
			throws JsonParseException, JsonMappingException, IOException {
		final Map<Key, Map<VisualProperty<?>, Object>> map = new LinkedHashMap<>();
		
		final JsonFactory factory = mapper.getFactory();
		final JsonParser parser = factory.createParser(file);
		
		try {
			if (parser.nextToken() != JsonToken.START_ARRAY)
				return map;
			
			while (parser.nextToken() != JsonToken.END_ARRAY) {
				if (parser.getCurrentToken() != JsonToken.START_OBJECT)
					continue;
				
				CyNetworkView netView = null;
				Class<? extends CyIdentifiable> elementType = null;
				long elementId = -1;
				final Map<VisualProperty<?>, Object> lockedValues = new LinkedHashMap<>();
				
				while (parser.nextToken() != JsonToken.END_OBJECT) {
					if (parser.getCurrentToken() != JsonToken.FIELD_NAME)
						continue;
					
					final String fieldName = parser.getCurrentName();
					final JsonToken valueToken = parser.nextToken();
					
					if ("networkView".equals(fieldName) && valueToken == JsonToken.VALUE_NUMBER_INT) {
						final long suid = parser.getLongValue();
						netView = session.getObject(suid, CyNetworkView.class);
					} else if ("element".equals(fieldName) && valueToken == JsonToken.VALUE_NUMBER_INT) {
						elementId = parser.getLongValue();
					} else if ("elementType".equals(fieldName) && valueToken == JsonToken.VALUE_STRING) {
						elementType = parser.getText().equalsIgnoreCase("CyNode") ? CyNode.class : CyEdge.class;
					} else if ("lockedValues".equals(fieldName) && valueToken == JsonToken.START_ARRAY) {
						final VisualLexicon lexicon = getVisualLexicon(netView);
						parseLockedValues(parser, lockedValues, lexicon, elementType);
					}
				}
				
				if (netView != null && elementId != -1 && elementType != null) {
					final CyIdentifiable element = session.getObject(elementId, elementType);
					
					if (element != null && !lockedValues.isEmpty()) {
						final Key key = new Key(netView, element);
						map.put(key, lockedValues);
					}
				}
			}
		} finally {
			parser.close();
		}
		
		return map;
	}
	
	private void parseLockedValues(final JsonParser parser, final Map<VisualProperty<?>, Object> lockedValues,
			final VisualLexicon lexicon, final Class<? extends CyIdentifiable> elementType)
					throws JsonParseException, IOException {
		while (parser.nextToken() != JsonToken.END_ARRAY) {
			if (parser.getCurrentToken() != JsonToken.START_OBJECT)
				continue;
			
			VisualProperty<?> vp = null;
			String stringValue = null;
			
			while (parser.nextToken() != JsonToken.END_OBJECT) {
				if (parser.getCurrentToken() != JsonToken.FIELD_NAME)
					continue;
				
				final String fieldName = parser.getCurrentName();
				final JsonToken valueToken = parser.nextToken();
				
				if ("visualProperty".equals(fieldName) && valueToken == JsonToken.VALUE_STRING) {
					final String id = parser.getText();
					vp = id != null ? lexicon.lookup(elementType, id) : null;
				} else if ("value".equals(fieldName) && valueToken == JsonToken.VALUE_STRING) {
					stringValue = parser.getText();
				}
			}
			
			if (vp != null && stringValue != null) {
    			final Object value = vp.parseSerializableString(stringValue);
    			lockedValues.put(vp, value);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private class LockedVisualPropertiesMapSerializer extends JsonSerializer<Map> {

		@Override
		@SuppressWarnings("unchecked")
		public void serialize(final Map map, final JsonGenerator jgen, final SerializerProvider provider)
				throws IOException, JsonProcessingException {
			jgen.writeStartArray();
			
			for (final Object mapKey : map.keySet()) {
				if (mapKey instanceof Key == false)
					continue;
				
				final Key key = (Key) mapKey;
				final Map<VisualProperty<?>, Object> lockedValues = (Map<VisualProperty<?>, Object>) map.get(key);
				
				if (lockedValues == null || lockedValues.isEmpty())
					continue;
				
				jgen.writeStartObject();
				jgen.writeNumberField("networkView", key.getNetworkView().getSUID());
				jgen.writeStringField("elementType", key.getElement() instanceof CyNode ? "CyNode" : "CyEdge");
				jgen.writeNumberField("element", key.getElement().getSUID());
				jgen.writeArrayFieldStart("lockedValues");
				
				for (Entry<?, ?> entry : lockedValues.entrySet()) {
					final VisualProperty vp = (VisualProperty)entry.getKey();
					
					jgen.writeStartObject();
					jgen.writeStringField("visualProperty", vp.getIdString());
					jgen.writeStringField("value", vp.toSerializableString(entry.getValue()));
					jgen.writeEndObject();
				}
				
				jgen.writeEndArray();
				jgen.writeEndObject();
			}
			
			jgen.writeEndArray();
		}
	}
	
	private VisualLexicon getVisualLexicon(final CyNetworkView netView) {
		final RenderingEngineManager rendererMgr = serviceRegistrar.getService(RenderingEngineManager.class);
    	final Collection<RenderingEngine<?>> renderingEngines = rendererMgr.getRenderingEngines(netView);
    	
    	if (renderingEngines != null && !renderingEngines.isEmpty())
    		return renderingEngines.iterator().next().getVisualLexicon();
    	
    	return rendererMgr.getDefaultVisualLexicon();
	}
}
