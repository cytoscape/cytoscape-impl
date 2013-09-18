package org.cytoscape.io.internal.write.json.serializer;
	import org.cytoscape.view.model.VisualLexicon;

	import com.fasterxml.jackson.core.Version;
	import com.fasterxml.jackson.databind.module.SimpleModule;

public class CytoscapeJsVisualStyleModule extends SimpleModule {

	private static final long serialVersionUID = 4011839428484625600L;

		public CytoscapeJsVisualStyleModule(final VisualLexicon lexicon) {
			super("CytoscapejsVisualStyleModule", new Version(1, 0, 0, null, null, null));
			
			// For Visual Styles
			
			// Value serializers
			addSerializer(new ColorSerializer());
			
			addSerializer(new CytoscapeJsVisualStyleSerializer(lexicon));
			addSerializer(new CytoscapeJsVsiaulStyleSetSerializer());
		}
	}