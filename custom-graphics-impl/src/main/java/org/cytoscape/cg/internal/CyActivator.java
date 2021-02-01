package org.cytoscape.cg.internal;

import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.cg.internal.action.CustomGraphicsManagerAction;
import org.cytoscape.cg.internal.charts.bar.BarChartFactory;
import org.cytoscape.cg.internal.charts.box.BoxChartFactory;
import org.cytoscape.cg.internal.charts.heatmap.HeatMapChartFactory;
import org.cytoscape.cg.internal.charts.line.LineChartFactory;
import org.cytoscape.cg.internal.charts.pie.PieChartFactory;
import org.cytoscape.cg.internal.charts.ring.RingChartFactory;
import org.cytoscape.cg.internal.editor.CustomGraphicsVisualPropertyEditor;
import org.cytoscape.cg.internal.editor.CyCustomGraphicsValueEditor;
import org.cytoscape.cg.internal.gradient.linear.LinearGradientFactory;
import org.cytoscape.cg.internal.gradient.radial.RadialGradientFactory;
import org.cytoscape.cg.internal.image.BitmapCustomGraphicsFactory;
import org.cytoscape.cg.internal.image.SVGCustomGraphicsFactory;
import org.cytoscape.cg.internal.model.CustomGraphicsManagerImpl;
import org.cytoscape.cg.internal.model.CustomGraphics2ManagerImpl;
import org.cytoscape.cg.internal.vector.GradientOvalFactory;
import org.cytoscape.cg.internal.vector.GradientRoundRectangleFactory;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.cg.model.CustomGraphicsRange;
import org.cytoscape.cg.model.CustomGraphicsTranslator;
import org.cytoscape.cg.model.CustomGraphics2Manager;
import org.cytoscape.cg.util.CustomGraphicsBrowser;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	
	private CustomGraphicsManager cgManager;
	private CustomGraphics2Manager cg2Manager;
	private CustomGraphicsBrowser cgBrowser;
	
	@Override
	public void start(BundleContext bc) {
		var serviceRegistrar = getService(bc, CyServiceRegistrar.class);

		startCustomGraphicsMgr(bc, serviceRegistrar);
		startCharts(bc, serviceRegistrar);
		startGradients(bc, serviceRegistrar);
		startPresentationImpl(bc, serviceRegistrar);
	}

	private void startCustomGraphicsMgr(BundleContext bc, CyServiceRegistrar serviceRegistrar) {
		cgBrowser = new CustomGraphicsBrowser(serviceRegistrar);
		registerAllServices(bc, cgBrowser);
		
		cgManager = new CustomGraphicsManagerImpl(getdefaultImageURLs(bc), serviceRegistrar);
		registerAllServices(bc, cgManager);
		
		CustomGraphicsRange.setManager(cgManager);

		var cgManagerAction = new CustomGraphicsManagerAction(cgManager, cgBrowser, serviceRegistrar);
		registerService(bc, cgManagerAction, CyAction.class);

		// Create and register our built-in factories.
		// TODO:  When the CustomGraphicsFactory service stuff is set up, just register these as services
		{
			var bitmapFactory = new BitmapCustomGraphicsFactory(cgManager, serviceRegistrar);
			var props = new Properties();
			props.setProperty(CustomGraphicsManager.SUPPORTED_CLASS_ID, BitmapCustomGraphicsFactory.SUPPORTED_CLASS_ID);
			cgManager.addCustomGraphicsFactory(bitmapFactory, props);
		}
		{
			var vectorFactory = new SVGCustomGraphicsFactory(cgManager, serviceRegistrar);
			var props = new Properties();
			props.setProperty(CustomGraphicsManager.SUPPORTED_CLASS_ID, SVGCustomGraphicsFactory.SUPPORTED_CLASS_ID);
			cgManager.addCustomGraphicsFactory(vectorFactory, props);
		}

		var ovalFactory = new GradientOvalFactory(cgManager);
		cgManager.addCustomGraphicsFactory(ovalFactory, new Properties());

		var rectangleFactory = new GradientRoundRectangleFactory(cgManager);
		cgManager.addCustomGraphicsFactory(rectangleFactory, new Properties());

		// Register this service listener so that app writers can provide their own CustomGraphics factories
		registerServiceListener(bc, cgManager::addCustomGraphicsFactory, cgManager::removeCustomGraphicsFactory,
				CyCustomGraphicsFactory.class);
		
		// Register this service listener so that app writers can provide their own CyCustomGraphics2 factories
		cg2Manager = CustomGraphics2ManagerImpl.getInstance();
		registerAllServices(bc, cg2Manager);
		registerServiceListener(bc, ((CustomGraphics2ManagerImpl) cg2Manager)::addFactory, 
				((CustomGraphics2ManagerImpl) cg2Manager)::removeFactory, CyCustomGraphics2Factory.class);
	}
	
	private void startCharts(BundleContext bc, CyServiceRegistrar serviceRegistrar) {
		// Register Chart Factories
		var props = new Properties();
		props.setProperty(CyCustomGraphics2Factory.GROUP, CustomGraphics2Manager.GROUP_CHARTS);
		{
			var factory = new BarChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
		{
			var factory = new BoxChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
		{
			var factory = new PieChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
		{
			var factory = new RingChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
		{
			var factory = new LineChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
		{
			var factory = new HeatMapChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
	}
	
	private void startGradients(BundleContext bc, CyServiceRegistrar serviceRegistrar) {
		// Register Gradient Factories
		var props = new Properties();
		props.setProperty(CyCustomGraphics2Factory.GROUP, CustomGraphics2Manager.GROUP_GRADIENTS);
		{
			var factory = new LinearGradientFactory();
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
		{
			var factory = new RadialGradientFactory();
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
	}
	
	private void startPresentationImpl(BundleContext bc, CyServiceRegistrar serviceRegistrar) {
		// Translators for Passthrough
		var cgTranslator = new CustomGraphicsTranslator(cgManager, cg2Manager);
		registerService(bc, cgTranslator, ValueTranslator.class);
		
		// Custom Graphics Editors
		var cgValueEditor = new CyCustomGraphicsValueEditor(cgBrowser, serviceRegistrar);
		registerAllServices(bc, cgValueEditor);

		var cmCellRendererFactory = getService(bc, ContinuousMappingCellRendererFactory.class);
		
		var cgVisualPropertyEditor = new CustomGraphicsVisualPropertyEditor(CyCustomGraphics.class, cgValueEditor,
				cmCellRendererFactory, serviceRegistrar);
		registerService(bc, cgVisualPropertyEditor, VisualPropertyEditor.class);
	}
	
	/**
	 * Get list of default images from resource.
	 */
	private Set<URL> getdefaultImageURLs(BundleContext bc) {
		var e = bc.getBundle().findEntries("images/sampleCustomGraphics", "*.png", true);
		var defaultImageUrls = new HashSet<URL>();
		
		while (e.hasMoreElements())
			defaultImageUrls.add(e.nextElement());
		
		return defaultImageUrls;
	}
}
