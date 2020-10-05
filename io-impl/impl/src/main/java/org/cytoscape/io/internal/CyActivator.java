package org.cytoscape.io.internal;

import org.cytoscape.filter.TransformerManager;
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
import org.cytoscape.io.internal.write.xgmml.GenericXGMMLWriterNoViewFactory;
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
import org.cytoscape.io.write.PresentationWriterFactory;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.io.write.VizmapWriterFactory;
import org.cytoscape.io.write.VizmapWriterManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		
		StreamUtilImpl streamUtil = new StreamUtilImpl(serviceRegistrar);
		BasicCyFileFilter expressionFilter = new BasicCyFileFilter(new String[]{"pvals"}, new String[]{"text/plain"},"Cytoscape Expression Matrix", DataCategory.TABLE, streamUtil);
		
		// Always register CYS filters from higher to lower version!
		BasicCyFileFilter cys3Filter = new SessionFileFilter(new String[]{"cys"}, new String[]{"application/zip"}, "Cytoscape 3 Session", DataCategory.SESSION, "3.0.0", streamUtil);
		BasicCyFileFilter cys2Filter = new SessionFileFilter(new String[]{"cys"}, new String[]{"application/zip"}, "Cytoscape 2 Session", DataCategory.SESSION, "2.0.0", streamUtil);
		
		BasicCyFileFilter pngFilter = new BasicCyFileFilter(new String[]{"png"}, new String[]{"image/png"}, "PNG", DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter jpegFilter = new BasicCyFileFilter(new String[]{"jpg","jpeg"}, new String[]{"image/jpeg"}, "JPEG", DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter pdfFilter = new BasicCyFileFilter(new String[]{"pdf"}, new String[]{"image/pdf"}, "PDF", DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter psFilter = new BasicCyFileFilter(new String[]{"ps"}, new String[]{"image/ps"}, "PostScript", DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter svgFilter = new BasicCyFileFilter(new String[]{"svg"}, new String[]{"image/svg"}, "SVG", DataCategory.IMAGE, streamUtil);
		BasicCyFileFilter attrsFilter = new BasicCyFileFilter(new String[]{"attrs"}, new String[]{"text/plain"}, "Text", DataCategory.TABLE, streamUtil);
		BasicCyFileFilter sifFilter = new BasicCyFileFilter(new String[]{"sif"}, new String[]{"text/sif"}, "SIF", DataCategory.NETWORK, streamUtil);
		BasicCyFileFilter nnfFilter = new BasicCyFileFilter(new String[]{"nnf"}, new String[]{"text/nnf"}, "NNF", DataCategory.NETWORK, streamUtil);
		BasicCyFileFilter csvFilter = new BasicCyFileFilter(new String[]{"csv"}, new String[]{"text/plain"}, "CSV", DataCategory.TABLE, streamUtil);
		BasicCyFileFilter sessionTableFilter = new BasicCyFileFilter(new String[]{"cytable"}, new String[]{"text/plain"}, "Session Table", DataCategory.TABLE, streamUtil);
		GenericXGMMLFileFilter xgmmlFilter = new GenericXGMMLFileFilter(new String[]{"xgmml","xml"}, new String[]{"text/xgmml","text/xgmml+xml"}, "XGMML",DataCategory.NETWORK, streamUtil);
		GenericXGMMLFileFilter xgmmlNoViewFilter = new GenericXGMMLFileFilter(new String[]{"xgmml","xml"}, new String[]{"text/xgmml","text/xgmml+xml"}, "XGMML without style data",DataCategory.NETWORK, streamUtil);
		SessionXGMMLFileFilter sessXgmmlFileFilter = new SessionXGMMLFileFilter(new String[]{"xgmml"}, new String[]{"text/xgmml","text/xgmml+xml"}, "Cy3 Session XGMML", DataCategory.NETWORK, streamUtil);
		SessionXGMMLNetworkFileFilter sessXgmmlNetFileFilter = new SessionXGMMLNetworkFileFilter(new String[]{"xgmml"}, new String[]{"text/xgmml","text/xgmml+xml"}, "CYS Network XGMML", DataCategory.NETWORK, streamUtil);
		SessionXGMMLNetworkViewFileFilter sessXgmmlViewFileFilter = new SessionXGMMLNetworkViewFileFilter(new String[]{"xgmml"}, new String[]{"text/xgmml","text/xgmml+xml"}, "CYS View XGMML",DataCategory.NETWORK, streamUtil);
		GMLFileFilter gmlFilter = new GMLFileFilter(new String[]{"gml"}, new String[]{"text/gml"}, "GML", DataCategory.NETWORK, streamUtil);
		CysessionFileFilter cysessionFilter = new CysessionFileFilter(new String[]{"xml"}, new String[]{}, "Cysession XML", DataCategory.PROPERTIES, streamUtil);
		BookmarkFileFilter bookmarksFilter = new BookmarkFileFilter(new String[]{"xml"}, new String[]{}, "Bookmark XML", DataCategory.PROPERTIES, streamUtil);
		PropertiesFileFilter propertiesFilter = new PropertiesFileFilter(new String[]{"props","properties"}, new String[]{}, "Java Properties", DataCategory.PROPERTIES, streamUtil);
		VizmapXMLFileFilter vizmapXMLFilter = new VizmapXMLFileFilter(new String[]{"xml"}, new String[]{}, "Style XML", DataCategory.VIZMAP, streamUtil);
		VizmapPropertiesFileFilter vizmapPropertiesFilter = new VizmapPropertiesFileFilter(new String[]{"props","properties"}, new String[]{}, "Vizmap Java Properties", DataCategory.VIZMAP, streamUtil);

		CyNetworkReaderManagerImpl networkReaderManager = new CyNetworkReaderManagerImpl(streamUtil);
		CyTableReaderManagerImpl tableReaderManager = new CyTableReaderManagerImpl(streamUtil);
		CySessionReaderManagerImpl sessionReaderManager = new CySessionReaderManagerImpl(streamUtil);
		VizmapReaderManagerImpl vizmapReaderManager = new VizmapReaderManagerImpl(streamUtil);
		CyPropertyReaderManagerImpl propertyReaderManager = new CyPropertyReaderManagerImpl(streamUtil);
		PresentationWriterManagerImpl viewWriterManager = new PresentationWriterManagerImpl(serviceRegistrar);
		CyNetworkViewWriterManagerImpl networkViewWriterManager = new CyNetworkViewWriterManagerImpl();
		SessionWriterManagerImpl sessionWriterManager = new SessionWriterManagerImpl();
		PropertyWriterManagerImpl propertyWriterManager = new PropertyWriterManagerImpl();
		CyTableWriterManagerImpl tableWriterManager = new CyTableWriterManagerImpl();
		VizmapWriterManagerImpl vizmapWriterManager = new VizmapWriterManagerImpl();

		CalculatorConverterFactory calculatorConverterFactory = new CalculatorConverterFactory();
		ExpressionReaderFactory expressionReaderFactory = new ExpressionReaderFactory(expressionFilter, serviceRegistrar);
		CyAttributesReaderFactory attrsDataReaderFactory = new CyAttributesReaderFactory(attrsFilter, serviceRegistrar);
		SIFNetworkReaderFactory sifNetworkViewReaderFactory = new SIFNetworkReaderFactory(sifFilter, serviceRegistrar);
		NNFNetworkReaderFactory nnfNetworkViewReaderFactory = new NNFNetworkReaderFactory(nnfFilter, serviceRegistrar);
		UnrecognizedVisualPropertyManager unrecognizedVisualPropertyManager = new UnrecognizedVisualPropertyManager(serviceRegistrar);
		GMLNetworkReaderFactory gmlNetworkViewReaderFactory = new GMLNetworkReaderFactory(gmlFilter, unrecognizedVisualPropertyManager, serviceRegistrar);
		
		ReadCache readCache = new ReadCache(serviceRegistrar);
		GroupUtil groupUtil = new GroupUtil(serviceRegistrar);
		SUIDUpdater suidUpdater = new SUIDUpdater();
		ReadDataManager readDataManager = new ReadDataManager(readCache, suidUpdater, groupUtil, serviceRegistrar);
		
		HandlerFactory handlerFactory = new HandlerFactory(readDataManager);
		XGMMLParser xgmmlParser = new XGMMLParser(handlerFactory,readDataManager);
		GenericXGMMLReaderFactory xgmmlReaderFactory = new GenericXGMMLReaderFactory(xgmmlFilter, readDataManager, xgmmlParser, unrecognizedVisualPropertyManager, serviceRegistrar);
		SessionXGMMLNetworkReaderFactory sessXgmmlNetReaderFactory = new SessionXGMMLNetworkReaderFactory(sessXgmmlNetFileFilter, readDataManager, xgmmlParser, unrecognizedVisualPropertyManager, serviceRegistrar);
		SessionXGMMLNetworkViewReaderFactory sessXgmmlViewReaderFactory = new SessionXGMMLNetworkViewReaderFactory(sessXgmmlViewFileFilter, readDataManager, xgmmlParser, unrecognizedVisualPropertyManager, serviceRegistrar);
		CSVCyReaderFactory sessionTableReaderFactory = new CSVCyReaderFactory(sessionTableFilter, true, true, serviceRegistrar);
		Cy3SessionReaderFactoryImpl cy3SessionReaderFactory = new Cy3SessionReaderFactoryImpl(cys3Filter, readCache, groupUtil, suidUpdater, networkReaderManager, propertyReaderManager, vizmapReaderManager, sessionTableReaderFactory, serviceRegistrar);
		Cy2SessionReaderFactoryImpl cy2SessionReaderFactory = new Cy2SessionReaderFactoryImpl(cys2Filter, readCache, groupUtil, networkReaderManager, propertyReaderManager, vizmapReaderManager, serviceRegistrar);
		CysessionReaderFactory cysessionReaderFactory = new CysessionReaderFactory(cysessionFilter);
		BookmarkReaderFactory bookmarkReaderFactory = new BookmarkReaderFactory(bookmarksFilter);
		PropertiesReaderFactory propertiesReaderFactory = new PropertiesReaderFactory(propertiesFilter);
		VisualStyleSerializer visualStyleSerializer = new VisualStyleSerializer(calculatorConverterFactory, serviceRegistrar);
		VizmapXMLReaderFactory vizmapXMLReaderFactory = new VizmapXMLReaderFactory(vizmapXMLFilter,visualStyleSerializer);
		VizmapPropertiesReaderFactory vizmapPropertiesReaderFactory = new VizmapPropertiesReaderFactory(vizmapPropertiesFilter, visualStyleSerializer);
		BitmapWriterFactory pngWriterFactory = new BitmapWriterFactory(pngFilter);
		BitmapWriterFactory jpegWriterFactory = new BitmapWriterFactory(jpegFilter);
		PDFWriterFactory pdfWriterFactory = new PDFWriterFactory(pdfFilter);
		PSWriterFactory psWriterFactory = new PSWriterFactory(psFilter);
		SVGWriterFactory svgWriterFactory = new SVGWriterFactory(svgFilter);
		SifNetworkWriterFactory sifNetworkViewWriterFactory = new SifNetworkWriterFactory(sifFilter);
		NnfNetworkWriterFactory nnfNetworkViewWriterFactory = new NnfNetworkWriterFactory(nnfFilter, serviceRegistrar);
		GenericXGMMLWriterFactory xgmmlWriterFactory = new GenericXGMMLWriterFactory(xgmmlFilter, unrecognizedVisualPropertyManager, groupUtil, serviceRegistrar);
		GenericXGMMLWriterNoViewFactory xgmmlWriterNoViewFactory = new GenericXGMMLWriterNoViewFactory(xgmmlNoViewFilter, unrecognizedVisualPropertyManager, groupUtil, serviceRegistrar);
		SessionXGMMLWriterFactory sessionXgmmlWriterFactory = new SessionXGMMLWriterFactory(sessXgmmlFileFilter, unrecognizedVisualPropertyManager, serviceRegistrar);
		CysessionWriterFactoryImpl cysessionWriterFactory = new CysessionWriterFactoryImpl(cysessionFilter);
		BookmarksWriterFactoryImpl bookmarksWriterFactory = new BookmarksWriterFactoryImpl(bookmarksFilter);
		PropertiesWriterFactoryImpl propertiesWriterFactory = new PropertiesWriterFactoryImpl(propertiesFilter);
		CSVTableWriterFactory csvTableWriterFactory = new CSVTableWriterFactory(csvFilter, false, false, false, true);
		CSVTableWriterFactory sessionTableWriterFactory = new CSVTableWriterFactory(sessionTableFilter, true, true, true, false);
		VizmapWriterFactoryImpl vizmapWriterFactory = new VizmapWriterFactoryImpl(vizmapXMLFilter, visualStyleSerializer);
		SessionWriterFactoryImpl sessionWriterFactory = new SessionWriterFactoryImpl(cys3Filter, bookmarksFilter, propertiesFilter, sessionTableFilter, vizmapXMLFilter, sessionXgmmlWriterFactory, propertyWriterManager, tableWriterManager, vizmapWriterManager, groupUtil, serviceRegistrar);
		RecentlyOpenedTrackerImpl recentlyOpenedTracker = new RecentlyOpenedTrackerImpl(serviceRegistrar);
		
		CyTransformerReaderImpl transformerReader = new CyTransformerReaderImpl();
		CyTransformerWriterImpl transformerWriter = new CyTransformerWriterImpl();
		registerService(bc, transformerReader, CyTransformerReader.class);
		registerService(bc, transformerWriter, CyTransformerWriter.class);
		registerServiceListener(bc, transformerReader::registerTransformerManager, transformerReader::unregisterTransformerManager, TransformerManager.class);
		
		registerService(bc, networkReaderManager, CyNetworkReaderManager.class);
		registerService(bc, tableReaderManager, CyTableReaderManager.class);
		registerService(bc, vizmapReaderManager, VizmapReaderManager.class);
		registerService(bc, viewWriterManager, PresentationWriterManager.class);
		registerService(bc, sessionReaderManager, CySessionReaderManager.class);
		registerService(bc, propertyReaderManager, CyPropertyReaderManager.class);
		registerService(bc, networkViewWriterManager, CyNetworkViewWriterManager.class);
		registerService(bc, sessionWriterManager, CySessionWriterManager.class);
		registerService(bc, propertyWriterManager, CyPropertyWriterManager.class);
		registerService(bc, tableWriterManager, CyTableWriterManager.class);
		registerService(bc, vizmapWriterManager, VizmapWriterManager.class);
		registerService(bc, sifNetworkViewReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, nnfNetworkViewReaderFactory, InputStreamTaskFactory.class);

		registerService(bc, xgmmlReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, sessXgmmlNetReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, sessXgmmlViewReaderFactory, InputStreamTaskFactory.class);

		registerService(bc, attrsDataReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, gmlNetworkViewReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, cy3SessionReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, cy2SessionReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, cysessionReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, bookmarkReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, propertiesReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, vizmapPropertiesReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, vizmapXMLReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, sessionTableReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, expressionReaderFactory, InputStreamTaskFactory.class);
		registerService(bc, streamUtil, StreamUtil.class);
		registerService(bc,unrecognizedVisualPropertyManager, NetworkViewAboutToBeDestroyedListener.class);
		registerService(bc, recentlyOpenedTracker, RecentlyOpenedTracker.class);
		
		registerServiceListener(bc, networkReaderManager::addInputStreamTaskFactory, networkReaderManager::removeInputStreamTaskFactory, InputStreamTaskFactory.class);
		registerServiceListener(bc, tableReaderManager::addInputStreamTaskFactory, tableReaderManager::removeInputStreamTaskFactory, InputStreamTaskFactory.class);
		registerServiceListener(bc, sessionReaderManager::addInputStreamTaskFactory, sessionReaderManager::removeInputStreamTaskFactory, InputStreamTaskFactory.class);
		registerServiceListener(bc, propertyReaderManager::addInputStreamTaskFactory, propertyReaderManager::removeInputStreamTaskFactory, InputStreamTaskFactory.class);
		registerServiceListener(bc, vizmapReaderManager::addInputStreamTaskFactory, vizmapReaderManager::removeInputStreamTaskFactory, InputStreamTaskFactory.class);
		registerServiceListener(bc, viewWriterManager::addCyWriterFactory, viewWriterManager::removeCyWriterFactory, PresentationWriterFactory.class);
		registerServiceListener(bc, networkViewWriterManager::addCyWriterFactory, networkViewWriterManager::removeCyWriterFactory, CyNetworkViewWriterFactory.class);
		registerServiceListener(bc, sessionWriterManager::addCyWriterFactory, sessionWriterManager::removeCyWriterFactory, CySessionWriterFactory.class);
		registerServiceListener(bc, propertyWriterManager::addCyWriterFactory, propertyWriterManager::removeCyWriterFactory, CyPropertyWriterFactory.class);
		registerServiceListener(bc, tableWriterManager::addCyWriterFactory, tableWriterManager::removeCyWriterFactory, CyTableWriterFactory.class);
		registerServiceListener(bc, vizmapWriterManager::addCyWriterFactory, vizmapWriterManager::removeCyWriterFactory, VizmapWriterFactory.class);

		registerAllServices(bc, pngWriterFactory);
		registerAllServices(bc, jpegWriterFactory);
		registerAllServices(bc, pdfWriterFactory);
		registerAllServices(bc, psWriterFactory);
		registerAllServices(bc, svgWriterFactory);
		registerAllServices(bc, sifNetworkViewWriterFactory);
		registerAllServices(bc, nnfNetworkViewWriterFactory);
		registerAllServices(bc, xgmmlWriterFactory);
		registerAllServices(bc, xgmmlWriterNoViewFactory);
		registerAllServices(bc, cysessionWriterFactory);
		registerAllServices(bc, bookmarksWriterFactory);
		registerAllServices(bc, propertiesWriterFactory);
		registerAllServices(bc, csvTableWriterFactory);
		registerAllServices(bc, sessionTableWriterFactory);
		registerAllServices(bc, vizmapWriterFactory);
		registerAllServices(bc, sessionWriterFactory);
	}
}
