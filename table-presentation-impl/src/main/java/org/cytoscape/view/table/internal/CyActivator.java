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
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.table.internal.equation.EquationEditorDialogFactory;
import org.cytoscape.view.table.internal.equation.EquationEditorTaskFactory;
import org.cytoscape.view.table.internal.impl.PopupMenuHelper;
import org.cytoscape.work.ServiceProperties;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext bc) {
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		
		PopupMenuHelper popupMenuHelper = new PopupMenuHelper(registrar);
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
		
		BasicTableVisualLexicon lexicon = new BrowserTableVisualLexicon();
		
		CyTableViewFactoryProvider tableViewFactoryFactory = getService(bc, CyTableViewFactoryProvider.class);
		CyTableViewFactory tableViewFactory = tableViewFactoryFactory.createTableViewFactory(lexicon, TableViewRendererImpl.ID);
		
		var renderer = new TableViewRendererImpl(registrar, tableViewFactory, lexicon, popupMenuHelper);
		registerService(bc, renderer, TableViewRenderer.class);
		registerService(bc, tableViewFactory, CyTableViewFactory.class); // register the default CyTableViewFactory
		
		
		{	// Need to register the RenderingEngineFactory itself because the RenderingEngineManager is listening for this service.
			var renderingEngineFactory = renderer.getRenderingEngineFactory(TableViewRenderer.DEFAULT_CONTEXT);
			var props = new Properties();
			props.setProperty(ServiceProperties.ID, TableViewRendererImpl.ID);
			registerService(bc, renderingEngineFactory, RenderingEngineFactory.class, props);
		}
		
		
		// Equations
		{
			var factory = new EquationEditorDialogFactory(registrar);
			registerService(bc, factory, EquationEditorDialogFactory.class);
			
			EquationEditorTaskFactory editorTaskFactory = new EquationEditorTaskFactory(registrar);
			Properties props = new Properties();
			props.setProperty("task", "equationEditor");
			registerService(bc, editorTaskFactory, TableTaskFactory.class, props);
		}
	}

}
