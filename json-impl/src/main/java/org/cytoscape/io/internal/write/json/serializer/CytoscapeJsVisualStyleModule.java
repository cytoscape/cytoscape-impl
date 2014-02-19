package org.cytoscape.io.internal.write.json.serializer;

import org.cytoscape.application.CyVersion;
import org.cytoscape.view.model.VisualLexicon;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class CytoscapeJsVisualStyleModule extends SimpleModule {

	private static final long serialVersionUID = 4011839428484625600L;

		public CytoscapeJsVisualStyleModule(final VisualLexicon lexicon, final CyVersion version) {
			super("CytoscapejsVisualStyleModule", new Version(1, 0, 0, null, null, null));
			
			// For Visual Styles
		
			final ValueSerializerManager manager = new ValueSerializerManager();
			// Value serializers
			addSerializer(new ColorSerializer());
			addSerializer(new ShapeSerializer());
//			addSerializer(new OpacitySerializer());
			addSerializer(new ArrowShapeSerializer());
			addSerializer(new LineStyleSerializer());
			addSerializer(new FontFaceSerializer());
		
			
			// VIsual Style Serializers
			addSerializer(new CytoscapeJsVisualStyleSerializer(manager, lexicon, version));
			addSerializer(new CytoscapeJsVsiaulStyleSetSerializer());
		}
	}