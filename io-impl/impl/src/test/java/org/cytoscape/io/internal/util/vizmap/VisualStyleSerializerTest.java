package org.cytoscape.io.internal.util.vizmap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.io.internal.util.vizmap.model.Vizmap;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class VisualStyleSerializerTest {

	private VisualStyleSerializer serializer;
	private Map<VisualStyle, String> styleNames;
	private Map<VisualStyle, Map<VisualProperty<?>, ?>> styleProperties;

	@Before
	public void setUp() throws Exception {
		styleNames = new Hashtable<VisualStyle, String>();
		styleProperties = new Hashtable<VisualStyle, Map<VisualProperty<?>, ? extends Object>>();

		final VisualMappingManager visualMappingManager = mock(VisualMappingManager.class);
		final VisualMappingFunctionFactory discreteMappingFactory = mock(VisualMappingFunctionFactory.class);
		final VisualMappingFunctionFactory continuousMappingFactory = mock(VisualMappingFunctionFactory.class);
		final VisualMappingFunctionFactory passthroughMappingFactory = mock(VisualMappingFunctionFactory.class);
		final VisualStyle dummyDefaultStyle = createDefaultStyle();

		final VisualStyleFactory visualStyleFactory = mock(VisualStyleFactory.class);

		when(visualMappingManager.getDefaultVisualStyle()).thenReturn(dummyDefaultStyle);
		when(visualStyleFactory.createVisualStyle(dummyDefaultStyle)).thenAnswer(new Answer<VisualStyle>() {

			public VisualStyle answer(InvocationOnMock invocation) throws Throwable {
				return createDefaultStyle();
			}
		});

		final RenderingEngineManager renderingEngineManager = mock(RenderingEngineManager.class);
		NullVisualProperty twoDRoot = new NullVisualProperty("TWO_D_ROOT", "2D Root Visual Property");
		when(renderingEngineManager.getDefaultVisualLexicon()).thenReturn(new BasicVisualLexicon(twoDRoot));

		final CalculatorConverterFactory calcFactory = new CalculatorConverterFactory();

		serializer = new VisualStyleSerializer(calcFactory, visualStyleFactory, visualMappingManager,
				renderingEngineManager, discreteMappingFactory, continuousMappingFactory, passthroughMappingFactory);
	}

	@Test
	public void testVisualStyleCollectionNotNullForEmptyProps() throws Exception {
		assertNotNull(serializer.createVisualStyles(new Properties()));
	}
	
	@Test
	public void testVisualStyleCollectionNotNullForEmptyVizmap() throws Exception {
		assertNotNull(serializer.createVisualStyles(new Vizmap()));
	}

	@Test
	public void testVizmapNotNullForNullVS() throws Exception {
		assertNotNull(serializer.createVizmap(null));
	}

	@Test
	public void testVizmapNotNullForEmptyVS() throws Exception {
		assertNotNull(serializer.createVizmap(new ArrayList<VisualStyle>()));
	}

	@Test
	public void tesCreateVisualStyles() throws Exception {
		// TODO: update
		//        Properties props = new Properties();
		//        // Style A:
		//        props.setProperty("globalAppearanceCalculator.Style A.defaultBackgroundColor", "255,255,255");
		//        props.setProperty("edgeAppearanceCalculator.Style A.defaultEdgeLineWidth", "2.0");
		//        // Style B:
		//        props.setProperty("globalAppearanceCalculator.Style B.defaultBackgroundColor", "0,255,0");
		//        // Should IGNORE this:
		//        props.setProperty("nodeLabelCalculator.Style C-Node Label-Passthrough Mapper.mapping.controller", "ID");
		//
		//        // TEST:
		//        Collection<VisualStyle> styles = serializer.createVisualStyles(props);
		//
		//        assertEquals(2, styles.size());
		//
		//        for (VisualStyle vs : styles) {
		//            String title = vs.getTitle();
		//            assertTrue(title.equals(styleNames.get(vs)));
		//
		//            if (title.equals("Style A")) {
		//                //                assertEquals(new Color(255, 255, 255), vs.getDefaultValue(TwoDVisualLexicon.NETWORK_BACKGROUND_PAINT));
		//                //                assertEquals(new Double(2), vs.getDefaultValue(TwoDVisualLexicon.EDGE_WIDTH));
		//            } else if (title.equals("Style B")) {
		//                //                assertEquals(new Color(0, 255, 0), vs.getDefaultValue(TwoDVisualLexicon.NETWORK_BACKGROUND_PAINT));
		//            }
		//        }
	}

	@SuppressWarnings("unchecked")
	private VisualStyle createDefaultStyle() {
		final VisualStyle vs = mock(VisualStyle.class);

		// stub get/setTitle
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				styleNames.put(vs, (String) args[0]);
				return null;
			}
		}).when(vs).setTitle(anyString());

		when(vs.getTitle()).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return styleNames.get(vs);
			}
		});

		// stub get/set default values
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				VisualProperty<?> vp = (VisualProperty) args[0];
				Object val = args[1];

				Map vpMap = styleProperties.get(vs);
				if (vpMap == null) {
					vpMap = new Hashtable();
					styleProperties.put(vs, vpMap);
				}

				vpMap.put(vp, val);

				return null;
			}
		}).when(vs).setDefaultValue(any(VisualProperty.class), anyObject());

		when(vs.getDefaultValue(any(VisualProperty.class))).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object val = null;
				Object[] args = invocation.getArguments();
				VisualProperty<?> vp = (VisualProperty) args[0];

				if (vp != null) {
					Map vpMap = styleProperties.get(vs);
					if (vpMap != null) val = vpMap.get(vp);
				}

				return val;
			}
		});

		return vs;
	}

}
