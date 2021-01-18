package org.cytoscape.view.table.internal;

import java.util.Properties;

import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.view.model.table.CyTableViewFactory;
import org.cytoscape.view.model.table.CyTableViewFactoryProvider;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.table.CellCustomGraphics;
import org.cytoscape.view.presentation.property.table.CellCustomGraphicsFactory;
import org.cytoscape.view.table.internal.cg.CellCGManager;
import org.cytoscape.view.table.internal.cg.CellCGValueEditor;
import org.cytoscape.view.table.internal.cg.CellCGVisualPropertyEditor;
import org.cytoscape.view.table.internal.cg.sparkline.bar.BarSparklineFactory;
import org.cytoscape.view.table.internal.equation.EquationEditorDialogFactory;
import org.cytoscape.view.table.internal.equation.EquationEditorTaskFactory;
import org.cytoscape.view.table.internal.impl.PopupMenuHelper;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.work.ServiceProperties;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext bc) {
		var registrar = getService(bc, CyServiceRegistrar.class);
		var continuousMappingCellRendererFactory = getService(bc, ContinuousMappingCellRendererFactory.class);
		
		var popupMenuHelper = new PopupMenuHelper(registrar);
		registerServiceListener(bc, popupMenuHelper::addTableColumnTaskFactory, popupMenuHelper::removeTableColumnTaskFactory, TableColumnTaskFactory.class);
		registerServiceListener(bc, popupMenuHelper::addTableCellTaskFactory, popupMenuHelper::removeTableCellTaskFactory, TableCellTaskFactory.class);
//		{
//			var factory = new HideColumnTaskFactory(mediator);
//			var props = new Properties();
//			props.setProperty(TITLE, "Hide Column");
//			// Do not register the factory as an OSGI service unless it's necessary.
//			// We just need to add it to the menu helper for now.
//			popupMenuHelper.addTableColumnTaskFactory(factory, props);
//		}
		
		var lexicon = new BrowserTableVisualLexicon();
		
		var tableViewFactoryFactory = getService(bc, CyTableViewFactoryProvider.class);
		var tableViewFactory = tableViewFactoryFactory.createTableViewFactory(lexicon, TableViewRendererImpl.ID);
		
		var renderer = new TableViewRendererImpl(registrar, tableViewFactory, lexicon, popupMenuHelper);
		registerService(bc, renderer, TableViewRenderer.class);
		registerService(bc, tableViewFactory, CyTableViewFactory.class); // register the default CyTableViewFactory
		
		{	// Need to register the RenderingEngineFactory itself because the RenderingEngineManager is listening for this service.
			var factory = renderer.getRenderingEngineFactory(TableViewRenderer.DEFAULT_CONTEXT);
			var props = new Properties();
			props.setProperty(ServiceProperties.ID, TableViewRendererImpl.ID);
			registerService(bc, factory, RenderingEngineFactory.class, props);
		}
		
		// Custom Graphics Manager
		// (register this service listener so that app writers can provide their own CellCustomGraphics factories)
		var cgManager = new CellCGManager();
		registerServiceListener(bc, cgManager::addFactory, cgManager::removeFactory, CellCustomGraphicsFactory.class);
		
		// Custom Graphics Editors
		var cgValueEditor = new CellCGValueEditor(cgManager, registrar);
		registerAllServices(bc, cgValueEditor);

		var cgVisualPropertyEditor = new CellCGVisualPropertyEditor(CellCustomGraphics.class, cgValueEditor, continuousMappingCellRendererFactory, registrar);
		registerService(bc, cgVisualPropertyEditor, VisualPropertyEditor.class);
		
		// Register Sparkline Factories
		{
			var props = new Properties();
			props.setProperty(CellCustomGraphicsFactory.GROUP, CellCGManager.GROUP_CHARTS);
			{
				var factory = new BarSparklineFactory(registrar);
				registerService(bc, factory, CellCustomGraphicsFactory.class, props);
			}
		}
		
		// Equations
		{
			var factory = new EquationEditorDialogFactory(registrar);
			registerService(bc, factory, EquationEditorDialogFactory.class);
			
			var editorTaskFactory = new EquationEditorTaskFactory(registrar);
			var props = new Properties();
			props.setProperty("task", "equationEditor");
			registerService(bc, editorTaskFactory, TableTaskFactory.class, props);
		}
	}
}
