
package org.cytoscape.io.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.read.CyNetworkReaderManagerImpl;
import org.cytoscape.io.internal.read.CyPropertyReaderManagerImpl;
import org.cytoscape.io.internal.read.CySessionReaderManagerImpl;
import org.cytoscape.io.internal.read.CyTableReaderManagerImpl;
import org.cytoscape.io.internal.read.VizmapReaderManagerImpl;
import org.cytoscape.io.internal.read.bookmarks.BookmarkFileFilter;
import org.cytoscape.io.internal.read.bookmarks.BookmarkReaderFactory;
import org.cytoscape.io.internal.read.cysession.CysessionFileFilter;
import org.cytoscape.io.internal.read.cysession.CysessionReaderFactory;
import org.cytoscape.io.internal.read.datatable.CSVCyReaderFactory;
import org.cytoscape.io.internal.read.datatable.CyAttributesReaderFactory;
import org.cytoscape.io.internal.read.expression.ExpressionReaderFactory;
import org.cytoscape.io.internal.read.gml.GMLFileFilter;
import org.cytoscape.io.internal.read.gml.GMLNetworkReaderFactory;
import org.cytoscape.io.internal.read.properties.PropertiesFileFilter;
import org.cytoscape.io.internal.read.properties.PropertiesReaderFactory;
import org.cytoscape.io.internal.read.session.Cy2SessionReaderFactoryImpl;
import org.cytoscape.io.internal.read.session.Cy3SessionReaderFactoryImpl;
import org.cytoscape.io.internal.read.session.SessionFileFilter;
import org.cytoscape.io.internal.read.sif.SIFNetworkReaderFactory;
import org.cytoscape.io.internal.read.vizmap.VizmapPropertiesFileFilter;
import org.cytoscape.io.internal.read.vizmap.VizmapPropertiesReaderFactory;
import org.cytoscape.io.internal.read.vizmap.VizmapXMLFileFilter;
import org.cytoscape.io.internal.read.vizmap.VizmapXMLReaderFactory;
import org.cytoscape.io.internal.read.xgmml.HandlerFactory;
import org.cytoscape.io.internal.read.xgmml.XGMMLFileFilter;
import org.cytoscape.io.internal.read.xgmml.XGMMLNetworkReaderFactory;
import org.cytoscape.io.internal.read.xgmml.XGMMLNetworkViewFileFilter;
import org.cytoscape.io.internal.read.xgmml.XGMMLNetworkViewReaderFactory;
import org.cytoscape.io.internal.read.xgmml.XGMMLParser;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.ReadUtils;
import org.cytoscape.io.internal.util.RecentlyOpenedTrackerImpl;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.internal.util.vizmap.CalculatorConverterFactory;
import org.cytoscape.io.internal.util.vizmap.VisualStyleSerializer;
import org.cytoscape.io.internal.write.CyNetworkViewWriterManagerImpl;
import org.cytoscape.io.internal.write.CyTableWriterManagerImpl;
import org.cytoscape.io.internal.write.PresentationWriterManagerImpl;
import org.cytoscape.io.internal.write.PropertyWriterManagerImpl;
import org.cytoscape.io.internal.write.SessionWriterManagerImpl;
import org.cytoscape.io.internal.write.VizmapWriterManagerImpl;
import org.cytoscape.io.internal.write.bookmarks.BookmarksWriterFactoryImpl;
import org.cytoscape.io.internal.write.cysession.CysessionWriterFactoryImpl;
import org.cytoscape.io.internal.write.datatable.csv.CSVTableWriterFactory;
import org.cytoscape.io.internal.write.graphics.BitmapWriterFactory;
import org.cytoscape.io.internal.write.graphics.PDFWriterFactory;
import org.cytoscape.io.internal.write.graphics.PSWriterFactory;
import org.cytoscape.io.internal.write.graphics.SVGWriterFactory;
import org.cytoscape.io.internal.write.properties.PropertiesWriterFactoryImpl;
import org.cytoscape.io.internal.write.session.SessionWriterFactoryImpl;
import org.cytoscape.io.internal.write.sif.SifNetworkWriterFactory;
import org.cytoscape.io.internal.write.vizmap.VizmapWriterFactoryImpl;
import org.cytoscape.io.internal.write.xgmml.XGMMLNetworkViewWriterFactory;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CyPropertyWriterFactory;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.io.write.CyTableWriterFactory;
import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.io.write.PresentationWriterFactory;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.io.write.VizmapWriterManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.osgi.framework.BundleContext;



public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		EquationCompiler compilerServiceRef = getService(bc,EquationCompiler.class);
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,CyApplicationConfiguration.class);
		CyEventHelper cyEventHelperRef = getService(bc,CyEventHelper.class);
		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc,CyLayoutAlgorithmManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class);
		CyTableFactory cyTableFactoryServiceRef = getService(bc,CyTableFactory.class);
		CyProperty cyPropertyServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyTableManager cyTableManagerServiceRef = getService(bc,CyTableManager.class);
		CyNetworkTableManager cyNetworkTableManagerServiceRef = getService(bc,CyNetworkTableManager.class);
		VisualStyleFactory visualStyleFactoryServiceRef = getService(bc,VisualStyleFactory.class);
		VisualMappingManager visualMappingManagerServiceRef = getService(bc,VisualMappingManager.class);
		RenderingEngineManager renderingEngineManagerServiceRef = getService(bc,RenderingEngineManager.class);
		VisualMappingFunctionFactory discreteMappingFactoryServiceRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=discrete)");
		VisualMappingFunctionFactory continuousMappingFactoryServiceRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=continuous)");
		VisualMappingFunctionFactory passthroughMappingFactoryServiceRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=passthrough)");
		EquationCompiler equationCompilerServiceRef = getService(bc,EquationCompiler.class);
		CyRootNetworkManager cyRootNetworkFactoryServiceRef = getService(bc,CyRootNetworkManager.class);
		
		StreamUtilImpl streamUtil = new StreamUtilImpl(cyPropertyServiceRef);
		BasicCyFileFilter expressionFilter = new BasicCyFileFilter(new String[]{"pvals"}, new String[]{"text/plain"},"Cytoscape Expression Matrix (.pvals) File", DataCategory.TABLE, streamUtil);
		
		// Always register CYS filters from higher to lower version!
		BasicCyFileFilter cys3Filter = new SessionFileFilter(new String[]{"cys"}, new String[]{"application/zip"}, "Cytoscape 3 Session (.cys) File", DataCategory.SESSION, "3.0.0", streamUtil);
		BasicCyFileFilter cys2Filter = new SessionFileFilter(new String[]{"cys"}, new String[]{"application/zip"}, "Cytoscape 2 Session (.cys) File", DataCategory.SESSION, "2.0.0", streamUtil);
		
		BasicCyFileFilter pngFilter = new BasicCyFileFilter(new String[]{"png"}, new String[]{"image/png"}, "Portable Network Graphics (PNG) File",DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter jpegFilter = new BasicCyFileFilter(new String[]{"jpg","jpeg"}, new String[]{"image/jpeg"}, "JPEG Image File",DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter pdfFilter = new BasicCyFileFilter(new String[]{"pdf"}, new String[]{"image/pdf"}, "PDF File",DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter psFilter = new BasicCyFileFilter(new String[]{"ps"}, new String[]{"image/ps"}, "Post Script (PS) File",DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter svgFilter = new BasicCyFileFilter(new String[]{"svg"}, new String[]{"image/svg"}, "Scalable Vector Graphics (SVG) File",DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter textTableFilter = new BasicCyFileFilter(new String[]{}, new String[]{"text/plain"}, "Any text file",DataCategory.TABLE, streamUtil);
		BasicCyFileFilter attrsFilter = new BasicCyFileFilter(new String[]{"attrs"}, new String[]{"text/plain"}, "Any text file",DataCategory.TABLE, streamUtil);
		BasicCyFileFilter sifFilter = new BasicCyFileFilter(new String[]{"sif"}, new String[]{"text/sif"}, "SIF files",DataCategory.NETWORK, streamUtil);
		BasicCyFileFilter csvFilter = new BasicCyFileFilter(new String[]{"csv"}, new String[]{"text/plain"}, "CSV file",DataCategory.TABLE, streamUtil);
		BasicCyFileFilter sessionTableFilter = new BasicCyFileFilter(new String[]{"cytable"}, new String[]{"text/plain"}, "Session table file",DataCategory.TABLE, streamUtil);
		XGMMLFileFilter xgmmlFilter = new XGMMLFileFilter(new String[]{"xgmml","xml"}, new String[]{"text/xgmml","text/xgmml+xml"}, "XGMML files",DataCategory.NETWORK, streamUtil);
		XGMMLNetworkViewFileFilter xgmmlViewFilter = new XGMMLNetworkViewFileFilter(new String[]{"xgmml","xml"}, new String[]{"text/xgmml","text/xgmml+xml"}, "View XGMML files",DataCategory.NETWORK, streamUtil);
		GMLFileFilter gmlFilter = new GMLFileFilter(new String[]{"gml"}, new String[]{"text/gml"}, "GML files",DataCategory.NETWORK, streamUtil);
		CysessionFileFilter cysessionFilter = new CysessionFileFilter(new String[]{"xml"}, new String[]{}, "Cysession XML files",DataCategory.PROPERTIES, streamUtil);
		BookmarkFileFilter bookmarksFilter = new BookmarkFileFilter(new String[]{"xml"}, new String[]{}, "Bookmark XML files",DataCategory.PROPERTIES, streamUtil);
		PropertiesFileFilter propertiesFilter = new PropertiesFileFilter(new String[]{"props","properties"}, new String[]{}, "Java Properties files",DataCategory.PROPERTIES, streamUtil);
		VizmapXMLFileFilter vizmapXMLFilter = new VizmapXMLFileFilter(new String[]{"xml"}, new String[]{}, "Vizmap XML files",DataCategory.VIZMAP, streamUtil);
		VizmapPropertiesFileFilter vizmapPropertiesFilter = new VizmapPropertiesFileFilter(new String[]{"props","properties"}, new String[]{}, "Vizmap Java Properties files",DataCategory.VIZMAP, streamUtil);

		CyNetworkReaderManagerImpl cyNetworkReaderManager = new CyNetworkReaderManagerImpl(streamUtil);
		CyTableReaderManagerImpl cyDataTableReaderManager = new CyTableReaderManagerImpl(streamUtil);
		CySessionReaderManagerImpl cySessionReaderManager = new CySessionReaderManagerImpl(streamUtil);
		VizmapReaderManagerImpl vizmapReaderManager = new VizmapReaderManagerImpl(streamUtil);
		CyPropertyReaderManagerImpl cyPropertyReaderManager = new CyPropertyReaderManagerImpl(streamUtil);
		PresentationWriterManagerImpl viewWriterManager = new PresentationWriterManagerImpl();
		CyNetworkViewWriterManagerImpl networkViewWriterManager = new CyNetworkViewWriterManagerImpl();
		SessionWriterManagerImpl sessionWriterManager = new SessionWriterManagerImpl();
		PropertyWriterManagerImpl propertyWriterManager = new PropertyWriterManagerImpl();
		CyTableWriterManagerImpl tableWriterManager = new CyTableWriterManagerImpl();
		VizmapWriterManagerImpl vizmapWriterManager = new VizmapWriterManagerImpl();

		CalculatorConverterFactory calculatorConverterFactory = new CalculatorConverterFactory();
		ReadUtils readUtil = new ReadUtils(streamUtil);
		ExpressionReaderFactory expressionReaderFactory = new ExpressionReaderFactory(expressionFilter,cyTableFactoryServiceRef,cyTableManagerServiceRef);
		CyAttributesReaderFactory attrsDataReaderFactory = new CyAttributesReaderFactory(attrsFilter,cyTableFactoryServiceRef,cyApplicationManagerServiceRef,cyNetworkManagerServiceRef,cyTableManagerServiceRef,cyRootNetworkFactoryServiceRef);
		SIFNetworkReaderFactory sifNetworkViewReaderFactory = new SIFNetworkReaderFactory(sifFilter,cyLayoutsServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkFactoryServiceRef,cyEventHelperRef);
		UnrecognizedVisualPropertyManager unrecognizedVisualPropertyManager = new UnrecognizedVisualPropertyManager(cyTableFactoryServiceRef,cyTableManagerServiceRef);
		GMLNetworkReaderFactory gmlNetworkViewReaderFactory = new GMLNetworkReaderFactory(gmlFilter,cyNetworkViewFactoryServiceRef,cyNetworkFactoryServiceRef,renderingEngineManagerServiceRef,unrecognizedVisualPropertyManager);
		ReadCache readCache = new ReadCache();
		ReadDataManager readDataManager = new ReadDataManager(readCache,equationCompilerServiceRef,cyNetworkFactoryServiceRef,cyRootNetworkFactoryServiceRef);
		
		HandlerFactory handlerFactory = new HandlerFactory(readDataManager);
		XGMMLParser xgmmlParser = new XGMMLParser(handlerFactory,readDataManager);
		XGMMLNetworkReaderFactory xgmmlNetworkReaderFactory = new XGMMLNetworkReaderFactory(xgmmlFilter,cyNetworkViewFactoryServiceRef,cyNetworkFactoryServiceRef,cyRootNetworkFactoryServiceRef,renderingEngineManagerServiceRef,readDataManager,xgmmlParser,unrecognizedVisualPropertyManager);
		XGMMLNetworkViewReaderFactory xgmmlNetworkViewReaderFactory = new XGMMLNetworkViewReaderFactory(xgmmlViewFilter,cyNetworkViewFactoryServiceRef,cyNetworkFactoryServiceRef,renderingEngineManagerServiceRef,readDataManager,xgmmlParser,unrecognizedVisualPropertyManager);
		CSVCyReaderFactory sessionTableReaderFactory = new CSVCyReaderFactory(sessionTableFilter,true,true,cyTableFactoryServiceRef,compilerServiceRef,cyTableManagerServiceRef);
		Cy3SessionReaderFactoryImpl cy3SessionReaderFactory = new Cy3SessionReaderFactoryImpl(cys3Filter,readCache,cyNetworkReaderManager,cyPropertyReaderManager,vizmapReaderManager,sessionTableReaderFactory,cyNetworkTableManagerServiceRef);
		Cy2SessionReaderFactoryImpl cy2SessionReaderFactory = new Cy2SessionReaderFactoryImpl(cys2Filter,readCache,cyNetworkReaderManager,cyPropertyReaderManager,vizmapReaderManager,cyRootNetworkFactoryServiceRef);
		CysessionReaderFactory cysessionReaderFactory = new CysessionReaderFactory(cysessionFilter);
		BookmarkReaderFactory bookmarkReaderFactory = new BookmarkReaderFactory(bookmarksFilter);
		PropertiesReaderFactory propertiesReaderFactory = new PropertiesReaderFactory(propertiesFilter);
		VisualStyleSerializer visualStyleSerializer = new VisualStyleSerializer(calculatorConverterFactory,visualStyleFactoryServiceRef,visualMappingManagerServiceRef,renderingEngineManagerServiceRef,discreteMappingFactoryServiceRef,continuousMappingFactoryServiceRef,passthroughMappingFactoryServiceRef);
		VizmapXMLReaderFactory vizmapXMLReaderFactory = new VizmapXMLReaderFactory(vizmapXMLFilter,visualStyleSerializer);
		VizmapPropertiesReaderFactory vizmapPropertiesReaderFactory = new VizmapPropertiesReaderFactory(vizmapPropertiesFilter,visualStyleSerializer);
		BitmapWriterFactory pngWriterFactory = new BitmapWriterFactory(pngFilter);
		BitmapWriterFactory jpegWriterFactory = new BitmapWriterFactory(jpegFilter);
		PDFWriterFactory pdfWriterFactory = new PDFWriterFactory(pdfFilter);
		PSWriterFactory psWriterFactory = new PSWriterFactory(psFilter);
		SVGWriterFactory svgWriterFactory = new SVGWriterFactory(svgFilter);
		SifNetworkWriterFactory sifNetworkViewWriterFactory = new SifNetworkWriterFactory(sifFilter);
		XGMMLNetworkViewWriterFactory xgmmlNetworkViewWriterFactory = new XGMMLNetworkViewWriterFactory(xgmmlFilter,renderingEngineManagerServiceRef,unrecognizedVisualPropertyManager,cyNetworkManagerServiceRef,cyRootNetworkFactoryServiceRef);
		CysessionWriterFactoryImpl cysessionWriterFactory = new CysessionWriterFactoryImpl(cysessionFilter);
		BookmarksWriterFactoryImpl bookmarksWriterFactory = new BookmarksWriterFactoryImpl(bookmarksFilter);
		PropertiesWriterFactoryImpl propertiesWriterFactory = new PropertiesWriterFactoryImpl(propertiesFilter);
		CSVTableWriterFactory csvTableWriterFactory = new CSVTableWriterFactory(csvFilter,false,false);
		CSVTableWriterFactory sessionTableWriterFactory = new CSVTableWriterFactory(sessionTableFilter,true,true);
		VizmapWriterFactoryImpl vizmapWriterFactory = new VizmapWriterFactoryImpl(vizmapXMLFilter,visualStyleSerializer);
		SessionWriterFactoryImpl sessionWriterFactory = new SessionWriterFactoryImpl(cys3Filter,xgmmlFilter,bookmarksFilter,cysessionFilter,propertiesFilter,sessionTableFilter,vizmapXMLFilter,networkViewWriterManager,cyRootNetworkFactoryServiceRef,propertyWriterManager,tableWriterManager,vizmapWriterManager);
		RecentlyOpenedTrackerImpl recentlyOpenedTracker = new RecentlyOpenedTrackerImpl("tracker.recent.sessions",cyApplicationConfigurationServiceRef);
		
		registerService(bc,cyNetworkReaderManager,CyNetworkReaderManager.class, new Properties());
		registerService(bc,cyDataTableReaderManager,CyTableReaderManager.class, new Properties());
		registerService(bc,vizmapReaderManager,VizmapReaderManager.class, new Properties());
		registerService(bc,viewWriterManager,PresentationWriterManager.class, new Properties());
		registerService(bc,cySessionReaderManager,CySessionReaderManager.class, new Properties());
		registerService(bc,cyPropertyReaderManager,CyPropertyReaderManager.class, new Properties());
		registerService(bc,networkViewWriterManager,CyNetworkViewWriterManager.class, new Properties());
		registerService(bc,sessionWriterManager,CySessionWriterManager.class, new Properties());
		registerService(bc,propertyWriterManager,CyPropertyWriterManager.class, new Properties());
		registerService(bc,tableWriterManager,CyTableWriterManager.class, new Properties());
		registerService(bc,vizmapWriterManager,VizmapWriterManager.class, new Properties());
		registerService(bc,sifNetworkViewReaderFactory,InputStreamTaskFactory.class, new Properties());

		registerService(bc,xgmmlNetworkReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,xgmmlNetworkViewReaderFactory,InputStreamTaskFactory.class, new Properties());

		registerService(bc,attrsDataReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,gmlNetworkViewReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,cy3SessionReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,cy2SessionReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,cysessionReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,bookmarkReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,propertiesReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,vizmapPropertiesReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,vizmapXMLReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,sessionTableReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,expressionReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,streamUtil,StreamUtil.class, new Properties());
		registerService(bc,unrecognizedVisualPropertyManager,NetworkViewAboutToBeDestroyedListener.class, new Properties());
		registerService(bc,recentlyOpenedTracker,RecentlyOpenedTracker.class, new Properties());
		
		registerServiceListener(bc,cyNetworkReaderManager,"addInputStreamTaskFactory","removeInputStreamTaskFactory",InputStreamTaskFactory.class);
		registerServiceListener(bc,cyDataTableReaderManager,"addInputStreamTaskFactory","removeInputStreamTaskFactory",InputStreamTaskFactory.class);
		registerServiceListener(bc,cySessionReaderManager,"addInputStreamTaskFactory","removeInputStreamTaskFactory",InputStreamTaskFactory.class);
		registerServiceListener(bc,cyPropertyReaderManager,"addInputStreamTaskFactory","removeInputStreamTaskFactory",InputStreamTaskFactory.class);
		registerServiceListener(bc,vizmapReaderManager,"addInputStreamTaskFactory","removeInputStreamTaskFactory",InputStreamTaskFactory.class);
		registerServiceListener(bc,viewWriterManager,"addCyWriterFactory","removeCyWriterFactory",PresentationWriterFactory.class,CyWriterFactory.class);
		registerServiceListener(bc,networkViewWriterManager,"addCyWriterFactory","removeCyWriterFactory",CyNetworkViewWriterFactory.class,CyWriterFactory.class);
		registerServiceListener(bc,sessionWriterManager,"addCyWriterFactory","removeCyWriterFactory",CySessionWriterFactory.class,CyWriterFactory.class);
		registerServiceListener(bc,propertyWriterManager,"addCyWriterFactory","removeCyWriterFactory",CyPropertyWriterFactory.class,CyWriterFactory.class);
		registerServiceListener(bc,tableWriterManager,"addCyWriterFactory","removeCyWriterFactory",CyTableWriterFactory.class,CyWriterFactory.class);
		registerServiceListener(bc,vizmapWriterManager,"addCyWriterFactory","removeCyWriterFactory",VizmapWriterFactory.class,CyWriterFactory.class);

		registerAllServices(bc, pngWriterFactory, new Properties());
		registerAllServices(bc, jpegWriterFactory, new Properties());
		registerAllServices(bc, pdfWriterFactory, new Properties());
		registerAllServices(bc, psWriterFactory, new Properties());
		registerAllServices(bc, svgWriterFactory, new Properties());
		registerAllServices(bc, sifNetworkViewWriterFactory, new Properties());
		registerAllServices(bc, xgmmlNetworkViewWriterFactory, new Properties());
		registerAllServices(bc, cysessionWriterFactory, new Properties());
		registerAllServices(bc, bookmarksWriterFactory, new Properties());
		registerAllServices(bc, propertiesWriterFactory, new Properties());
		registerAllServices(bc, csvTableWriterFactory, new Properties());
		registerAllServices(bc, sessionTableWriterFactory, new Properties());
		registerAllServices(bc, vizmapWriterFactory, new Properties());
		registerAllServices(bc, sessionWriterFactory, new Properties());
	}
}

