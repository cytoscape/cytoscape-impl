package org.cytoscape.io.internal;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.filter.TransformerManager;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
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
import org.cytoscape.io.internal.read.nnf.NNFNetworkReaderFactory;
import org.cytoscape.io.internal.read.properties.PropertiesFileFilter;
import org.cytoscape.io.internal.read.properties.PropertiesReaderFactory;
import org.cytoscape.io.internal.read.session.Cy2SessionReaderFactoryImpl;
import org.cytoscape.io.internal.read.session.Cy3SessionReaderFactoryImpl;
import org.cytoscape.io.internal.read.session.SessionFileFilter;
import org.cytoscape.io.internal.read.sif.SIFNetworkReaderFactory;
import org.cytoscape.io.internal.read.transformer.CyTransformerReaderImpl;
import org.cytoscape.io.internal.read.vizmap.VizmapPropertiesFileFilter;
import org.cytoscape.io.internal.read.vizmap.VizmapPropertiesReaderFactory;
import org.cytoscape.io.internal.read.vizmap.VizmapXMLFileFilter;
import org.cytoscape.io.internal.read.vizmap.VizmapXMLReaderFactory;
import org.cytoscape.io.internal.read.xgmml.GenericXGMMLFileFilter;
import org.cytoscape.io.internal.read.xgmml.GenericXGMMLReaderFactory;
import org.cytoscape.io.internal.read.xgmml.HandlerFactory;
import org.cytoscape.io.internal.read.xgmml.SessionXGMMLFileFilter;
import org.cytoscape.io.internal.read.xgmml.SessionXGMMLNetworkFileFilter;
import org.cytoscape.io.internal.read.xgmml.SessionXGMMLNetworkReaderFactory;
import org.cytoscape.io.internal.read.xgmml.SessionXGMMLNetworkViewFileFilter;
import org.cytoscape.io.internal.read.xgmml.SessionXGMMLNetworkViewReaderFactory;
import org.cytoscape.io.internal.read.xgmml.XGMMLParser;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.RecentlyOpenedTrackerImpl;
import org.cytoscape.io.internal.util.SUIDUpdater;
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
import org.cytoscape.io.internal.write.nnf.NnfNetworkWriterFactory;
import org.cytoscape.io.internal.write.properties.PropertiesWriterFactoryImpl;
import org.cytoscape.io.internal.write.session.SessionWriterFactoryImpl;
import org.cytoscape.io.internal.write.sif.SifNetworkWriterFactory;
import org.cytoscape.io.internal.write.transformer.CyTransformerWriterImpl;
import org.cytoscape.io.internal.write.vizmap.VizmapWriterFactoryImpl;
import org.cytoscape.io.internal.write.xgmml.GenericXGMMLWriterFactory;
import org.cytoscape.io.internal.write.xgmml.SessionXGMMLWriterFactory;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.io.read.CyTransformerReader;
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
import org.cytoscape.io.write.CyTransformerWriter;
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
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
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

	@Override
	public void start(BundleContext bc) {
		CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		EquationCompiler compilerServiceRef = getService(bc,EquationCompiler.class);
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,CyApplicationConfiguration.class);
		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc,CyLayoutAlgorithmManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class);
		CyTableFactory cyTableFactoryServiceRef = getService(bc,CyTableFactory.class);
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
		CyRootNetworkManager cyRootNetworkManagerServiceRef = getService(bc,CyRootNetworkManager.class);		
		
		StreamUtilImpl streamUtil = new StreamUtilImpl(serviceRegistrar);
		BasicCyFileFilter expressionFilter = new BasicCyFileFilter(new String[]{"mrna","pvals"}, new String[]{"text/plain"},"Cytoscape Expression Matrix", DataCategory.TABLE, streamUtil);
		
		// Always register CYS filters from higher to lower version!
		BasicCyFileFilter cys3Filter = new SessionFileFilter(new String[]{"cys"}, new String[]{"application/zip"}, "Cytoscape 3 Session", DataCategory.SESSION, "3.0.0", streamUtil);
		BasicCyFileFilter cys2Filter = new SessionFileFilter(new String[]{"cys"}, new String[]{"application/zip"}, "Cytoscape 2 Session", DataCategory.SESSION, "2.0.0", streamUtil);
		
		BasicCyFileFilter pngFilter = new BasicCyFileFilter(new String[]{"png"}, new String[]{"image/png"}, "PNG",DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter jpegFilter = new BasicCyFileFilter(new String[]{"jpg","jpeg"}, new String[]{"image/jpeg"}, "JPEG", DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter pdfFilter = new BasicCyFileFilter(new String[]{"pdf"}, new String[]{"image/pdf"}, "PDF", DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter psFilter = new BasicCyFileFilter(new String[]{"ps"}, new String[]{"image/ps"}, "PostScript", DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter svgFilter = new BasicCyFileFilter(new String[]{"svg"}, new String[]{"image/svg"}, "SVG",DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter attrsFilter = new BasicCyFileFilter(new String[]{"attrs"}, new String[]{"text/plain"}, "Text",DataCategory.TABLE, streamUtil);
		BasicCyFileFilter sifFilter = new BasicCyFileFilter(new String[]{"sif"}, new String[]{"text/sif"}, "SIF",DataCategory.NETWORK, streamUtil);
		BasicCyFileFilter nnfFilter = new BasicCyFileFilter(new String[]{"nnf"}, new String[]{"text/nnf"}, "NNF", DataCategory.NETWORK, streamUtil);
		BasicCyFileFilter csvFilter = new BasicCyFileFilter(new String[]{"csv"}, new String[]{"text/plain"}, "CSV", DataCategory.TABLE, streamUtil);
		BasicCyFileFilter sessionTableFilter = new BasicCyFileFilter(new String[]{"cytable"}, new String[]{"text/plain"}, "Session Table",DataCategory.TABLE, streamUtil);
		GenericXGMMLFileFilter xgmmlFilter = new GenericXGMMLFileFilter(new String[]{"xgmml","xml"}, new String[]{"text/xgmml","text/xgmml+xml"}, "XGMML",DataCategory.NETWORK, streamUtil);
		SessionXGMMLFileFilter sessXgmmlFileFilter = new SessionXGMMLFileFilter(new String[]{"xgmml"}, new String[]{"text/xgmml","text/xgmml+xml"}, "Cy3 Session XGMML", DataCategory.NETWORK, streamUtil);
		SessionXGMMLNetworkFileFilter sessXgmmlNetFileFilter = new SessionXGMMLNetworkFileFilter(new String[]{"xgmml"}, new String[]{"text/xgmml","text/xgmml+xml"}, "CYS Network XGMML", DataCategory.NETWORK, streamUtil);
		SessionXGMMLNetworkViewFileFilter sessXgmmlViewFileFilter = new SessionXGMMLNetworkViewFileFilter(new String[]{"xgmml"}, new String[]{"text/xgmml","text/xgmml+xml"}, "CYS View XGMML",DataCategory.NETWORK, streamUtil);
		GMLFileFilter gmlFilter = new GMLFileFilter(new String[]{"gml"}, new String[]{"text/gml"}, "GML", DataCategory.NETWORK, streamUtil);
		CysessionFileFilter cysessionFilter = new CysessionFileFilter(new String[]{"xml"}, new String[]{}, "Cysession XML", DataCategory.PROPERTIES, streamUtil);
		BookmarkFileFilter bookmarksFilter = new BookmarkFileFilter(new String[]{"xml"}, new String[]{}, "Bookmark XML", DataCategory.PROPERTIES, streamUtil);
		PropertiesFileFilter propertiesFilter = new PropertiesFileFilter(new String[]{"props","properties"}, new String[]{}, "Java Properties", DataCategory.PROPERTIES, streamUtil);
		VizmapXMLFileFilter vizmapXMLFilter = new VizmapXMLFileFilter(new String[]{"xml"}, new String[]{}, "Style XML",DataCategory.VIZMAP, streamUtil);
		VizmapPropertiesFileFilter vizmapPropertiesFilter = new VizmapPropertiesFileFilter(new String[]{"props","properties"}, new String[]{}, "Vizmap Java Properties", DataCategory.VIZMAP, streamUtil);

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
		ExpressionReaderFactory expressionReaderFactory = new ExpressionReaderFactory(expressionFilter,cyTableFactoryServiceRef);
		CyAttributesReaderFactory attrsDataReaderFactory = new CyAttributesReaderFactory(attrsFilter,cyTableFactoryServiceRef,cyApplicationManagerServiceRef,cyNetworkManagerServiceRef,cyRootNetworkManagerServiceRef);
		SIFNetworkReaderFactory sifNetworkViewReaderFactory = new SIFNetworkReaderFactory(sifFilter,cyLayoutsServiceRef,cyApplicationManagerServiceRef,cyNetworkFactoryServiceRef,cyNetworkManagerServiceRef,cyRootNetworkManagerServiceRef);
		NNFNetworkReaderFactory nnfNetworkViewReaderFactory = new NNFNetworkReaderFactory(nnfFilter,cyLayoutsServiceRef,cyApplicationManagerServiceRef,cyNetworkFactoryServiceRef, cyNetworkManagerServiceRef, cyRootNetworkManagerServiceRef);
		UnrecognizedVisualPropertyManager unrecognizedVisualPropertyManager = new UnrecognizedVisualPropertyManager(cyTableFactoryServiceRef,cyTableManagerServiceRef);
		GMLNetworkReaderFactory gmlNetworkViewReaderFactory = new GMLNetworkReaderFactory(gmlFilter,cyApplicationManagerServiceRef,cyNetworkFactoryServiceRef,renderingEngineManagerServiceRef,unrecognizedVisualPropertyManager,cyNetworkManagerServiceRef,cyRootNetworkManagerServiceRef);
		CyGroupFactory cyGroupFactoryServiceRef = getService(bc,CyGroupFactory.class);
		CyGroupManager cyGroupManagerServiceRef = getService(bc,CyGroupManager.class);
		
		ReadCache readCache = new ReadCache(cyNetworkTableManagerServiceRef);
		GroupUtil groupUtil = new GroupUtil(cyGroupManagerServiceRef, cyGroupFactoryServiceRef);
		SUIDUpdater suidUpdater = new SUIDUpdater();
		ReadDataManager readDataManager = new ReadDataManager(readCache,suidUpdater,equationCompilerServiceRef,cyNetworkFactoryServiceRef,cyRootNetworkManagerServiceRef,groupUtil);
		
		HandlerFactory handlerFactory = new HandlerFactory(readDataManager);
		XGMMLParser xgmmlParser = new XGMMLParser(handlerFactory,readDataManager);
		GenericXGMMLReaderFactory xgmmlReaderFactory = new GenericXGMMLReaderFactory(xgmmlFilter,cyNetworkViewFactoryServiceRef,cyNetworkFactoryServiceRef,renderingEngineManagerServiceRef,readDataManager,xgmmlParser,unrecognizedVisualPropertyManager,cyNetworkManagerServiceRef,cyRootNetworkManagerServiceRef,cyApplicationManagerServiceRef);
		SessionXGMMLNetworkReaderFactory sessXgmmlNetReaderFactory = new SessionXGMMLNetworkReaderFactory(sessXgmmlNetFileFilter,cyNetworkFactoryServiceRef,cyRootNetworkManagerServiceRef,renderingEngineManagerServiceRef,readDataManager,xgmmlParser,unrecognizedVisualPropertyManager, cyNetworkManagerServiceRef, cyApplicationManagerServiceRef);
		SessionXGMMLNetworkViewReaderFactory sessXgmmlViewReaderFactory = new SessionXGMMLNetworkViewReaderFactory(sessXgmmlViewFileFilter,cyNetworkFactoryServiceRef,renderingEngineManagerServiceRef,readDataManager,xgmmlParser,unrecognizedVisualPropertyManager, cyNetworkManagerServiceRef,cyRootNetworkManagerServiceRef, cyApplicationManagerServiceRef);
		CSVCyReaderFactory sessionTableReaderFactory = new CSVCyReaderFactory(sessionTableFilter,true,true,cyTableFactoryServiceRef,compilerServiceRef);
		Cy3SessionReaderFactoryImpl cy3SessionReaderFactory = new Cy3SessionReaderFactoryImpl(cys3Filter,readCache,groupUtil,suidUpdater,cyNetworkReaderManager,cyPropertyReaderManager,vizmapReaderManager,sessionTableReaderFactory,cyNetworkTableManagerServiceRef,cyRootNetworkManagerServiceRef, equationCompilerServiceRef);
		Cy2SessionReaderFactoryImpl cy2SessionReaderFactory = new Cy2SessionReaderFactoryImpl(cys2Filter,readCache,groupUtil,cyNetworkReaderManager,cyPropertyReaderManager,vizmapReaderManager,cyRootNetworkManagerServiceRef);
		CysessionReaderFactory cysessionReaderFactory = new CysessionReaderFactory(cysessionFilter);
		BookmarkReaderFactory bookmarkReaderFactory = new BookmarkReaderFactory(bookmarksFilter);
		PropertiesReaderFactory propertiesReaderFactory = new PropertiesReaderFactory(propertiesFilter);
		VisualStyleSerializer visualStyleSerializer = new VisualStyleSerializer(calculatorConverterFactory,visualStyleFactoryServiceRef,renderingEngineManagerServiceRef,discreteMappingFactoryServiceRef,continuousMappingFactoryServiceRef,passthroughMappingFactoryServiceRef);
		VizmapXMLReaderFactory vizmapXMLReaderFactory = new VizmapXMLReaderFactory(vizmapXMLFilter,visualStyleSerializer);
		VizmapPropertiesReaderFactory vizmapPropertiesReaderFactory = new VizmapPropertiesReaderFactory(vizmapPropertiesFilter,visualStyleSerializer);
		BitmapWriterFactory pngWriterFactory = new BitmapWriterFactory(pngFilter);
		BitmapWriterFactory jpegWriterFactory = new BitmapWriterFactory(jpegFilter);
		PDFWriterFactory pdfWriterFactory = new PDFWriterFactory(pdfFilter);
		PSWriterFactory psWriterFactory = new PSWriterFactory(psFilter);
		SVGWriterFactory svgWriterFactory = new SVGWriterFactory(svgFilter);
		SifNetworkWriterFactory sifNetworkViewWriterFactory = new SifNetworkWriterFactory(sifFilter);
		NnfNetworkWriterFactory nnfNetworkViewWriterFactory = new NnfNetworkWriterFactory(cyNetworkManagerServiceRef,nnfFilter);
		GenericXGMMLWriterFactory xgmmlWriterFactory = new GenericXGMMLWriterFactory(xgmmlFilter,renderingEngineManagerServiceRef,unrecognizedVisualPropertyManager,cyNetworkManagerServiceRef,cyRootNetworkManagerServiceRef,visualMappingManagerServiceRef,groupUtil);
		SessionXGMMLWriterFactory sessionXgmmlWriterFactory = new SessionXGMMLWriterFactory(sessXgmmlFileFilter,renderingEngineManagerServiceRef,unrecognizedVisualPropertyManager,cyNetworkManagerServiceRef,cyRootNetworkManagerServiceRef,visualMappingManagerServiceRef);
		CysessionWriterFactoryImpl cysessionWriterFactory = new CysessionWriterFactoryImpl(cysessionFilter);
		BookmarksWriterFactoryImpl bookmarksWriterFactory = new BookmarksWriterFactoryImpl(bookmarksFilter);
		PropertiesWriterFactoryImpl propertiesWriterFactory = new PropertiesWriterFactoryImpl(propertiesFilter);
		CSVTableWriterFactory csvTableWriterFactory = new CSVTableWriterFactory(csvFilter,false,false, true);
		CSVTableWriterFactory sessionTableWriterFactory = new CSVTableWriterFactory(sessionTableFilter,true,true, false);
		VizmapWriterFactoryImpl vizmapWriterFactory = new VizmapWriterFactoryImpl(vizmapXMLFilter,visualStyleSerializer);
		SessionWriterFactoryImpl sessionWriterFactory = new SessionWriterFactoryImpl(cys3Filter,bookmarksFilter,propertiesFilter,sessionTableFilter,vizmapXMLFilter,sessionXgmmlWriterFactory,cyRootNetworkManagerServiceRef,propertyWriterManager,tableWriterManager,vizmapWriterManager,groupUtil);
		RecentlyOpenedTrackerImpl recentlyOpenedTracker = new RecentlyOpenedTrackerImpl("tracker.recent.sessions",cyApplicationConfigurationServiceRef);
		
		CyTransformerReaderImpl transformerReader = new CyTransformerReaderImpl();
		CyTransformerWriterImpl transformerWriter = new CyTransformerWriterImpl();
		registerService(bc, transformerReader, CyTransformerReader.class, new Properties());
		registerService(bc, transformerWriter, CyTransformerWriter.class, new Properties());
		registerServiceListener(bc, transformerReader, "registerTransformerManager", "unregisterTransformerManager", TransformerManager.class);
		
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
		registerService(bc,nnfNetworkViewReaderFactory,InputStreamTaskFactory.class, new Properties());

		registerService(bc,xgmmlReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,sessXgmmlNetReaderFactory,InputStreamTaskFactory.class, new Properties());
		registerService(bc,sessXgmmlViewReaderFactory,InputStreamTaskFactory.class, new Properties());

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
		registerAllServices(bc, nnfNetworkViewWriterFactory, new Properties());
		registerAllServices(bc, xgmmlWriterFactory, new Properties());
		registerAllServices(bc, cysessionWriterFactory, new Properties());
		registerAllServices(bc, bookmarksWriterFactory, new Properties());
		registerAllServices(bc, propertiesWriterFactory, new Properties());
		registerAllServices(bc, csvTableWriterFactory, new Properties());
		registerAllServices(bc, sessionTableWriterFactory, new Properties());
		registerAllServices(bc, vizmapWriterFactory, new Properties());
		registerAllServices(bc, sessionWriterFactory, new Properties());
	}
}

