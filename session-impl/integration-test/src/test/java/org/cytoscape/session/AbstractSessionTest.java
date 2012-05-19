package org.cytoscape.session;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.EquationParser;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.equations.internal.EquationCompilerImpl;
import org.cytoscape.equations.internal.EquationParserImpl;
import org.cytoscape.equations.internal.interpreter.InterpreterImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.event.internal.CyEventHelperImpl;
import org.cytoscape.event.internal.CyListenerAdapter;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.read.CyNetworkReaderManagerImpl;
import org.cytoscape.io.internal.read.CyPropertyReaderManagerImpl;
import org.cytoscape.io.internal.read.VizmapReaderManagerImpl;
import org.cytoscape.io.internal.read.datatable.CSVCyReaderFactory;
import org.cytoscape.io.internal.read.session.Cy3SessionReaderImpl;
import org.cytoscape.io.internal.read.vizmap.VizmapXMLFileFilter;
import org.cytoscape.io.internal.read.vizmap.VizmapXMLReaderFactory;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.io.internal.util.vizmap.CalculatorConverterFactory;
import org.cytoscape.io.internal.util.vizmap.VisualStyleSerializer;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.CySessionReader;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.internal.CyNetworkTableManagerImpl;
import org.cytoscape.model.internal.CyRootNetworkManagerImpl;
import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.service.util.internal.CyServiceRegistrarImpl;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

/**
 * Build real application with minimal functionality.
 * 
 */
public class AbstractSessionTest {

	private CySessionReader sessionReader;

	private URL session1;
	private TaskMonitor taskMonitor = mock(TaskMonitor.class);

	@Before
	public void setUp() throws Exception {
		buildHeadlessApplication();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		
		//sessionReader.run(taskMonitor);
	}

	private void buildHeadlessApplication() throws Exception {
		BundleContext bc = mock(BundleContext.class);
		CyListenerAdapter normal = new CyListenerAdapter(bc);
		CyEventHelper cyEventHelper = new CyEventHelperImpl(normal);

		final File sessionFile1 = new File("./src/test/resources/testData/" + "smallSession.cys");

		final URL sessionFileURL1 = sessionFile1.toURI().toURL();
		InputStream sourceInputStream = sessionFileURL1.openStream();

		final ReadCache cache = new ReadCache();
		CyProperty<Properties> prop = new CyProperty<Properties>() {

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Properties getProperties() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public org.cytoscape.property.CyProperty.SavePolicy getSavePolicy() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Class<? extends Properties> getPropertyType() {
				// TODO Auto-generated method stub
				return null;
			}

		};
		StreamUtil streamUtil = new StreamUtilImpl(prop);
		CyNetworkReaderManager networkReaderMgr = new CyNetworkReaderManagerImpl(streamUtil);
		CyPropertyReaderManager propertyReaderMgr = new CyPropertyReaderManagerImpl(streamUtil);
		VizmapReaderManager vizmapReaderMgr = new VizmapReaderManagerImpl(streamUtil);
		
		CalculatorConverterFactory calculatorConverterFactory = new CalculatorConverterFactory();

//		VisualStyleSerializer visualStyleSerializer = new VisualStyleSerializer(calculatorConverterFactory,visualStyleFactoryServiceRef,visualMappingManagerServiceRef,renderingEngineManagerServiceRef,discreteMappingFactoryServiceRef,continuousMappingFactoryServiceRef,passthroughMappingFactoryServiceRef);
//		VizmapXMLFileFilter vizmapXMLFilter = new VizmapXMLFileFilter(new String[]{"xml"}, new String[]{}, "Vizmap XML files",DataCategory.VIZMAP, streamUtil);
//		VizmapXMLReaderFactory vizmapXMLReaderFactory = new VizmapXMLReaderFactory(vizmapXMLFilter,visualStyleSerializer);

		
		

		Interpreter interpreter = new InterpreterImpl();
		CyServiceRegistrar serviceRegistrar = new CyServiceRegistrarImpl(bc);
		CyTableFactory tableFactory = new CyTableFactoryImpl(cyEventHelper, interpreter, serviceRegistrar);
		EquationParser parser = new EquationParserImpl();
		EquationCompiler compiler = new EquationCompilerImpl(parser);

		BasicCyFileFilter sessionTableFilter = new BasicCyFileFilter(new String[] { "cytable" },
				new String[] { "text/plain" }, "Session table file", DataCategory.TABLE, streamUtil);

		CSVCyReaderFactory sessionTableReaderFactory = new CSVCyReaderFactory(sessionTableFilter, true, true,
				tableFactory, compiler);

		CyNetworkTableManager networkTableMgr = new CyNetworkTableManagerImpl();
		CyRootNetworkManager rootNetworkMgr = new CyRootNetworkManagerImpl();
		sessionReader = new Cy3SessionReaderImpl(sourceInputStream, cache, networkReaderMgr, propertyReaderMgr,
				vizmapReaderMgr, sessionTableReaderFactory, networkTableMgr, rootNetworkMgr);
	}
}
