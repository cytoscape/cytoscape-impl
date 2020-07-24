package org.cytoscape.view.table.internal;

import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.view.model.table.CyTableViewFactory;
import org.cytoscape.view.model.table.CyTableViewFactoryProvider;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.table.internal.impl.PopupMenuHelper;
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
		
		// MKTODO make a private lexicon similar to DVisualLexicon
		BasicTableVisualLexicon lexicon = BasicTableVisualLexicon.getInstance();
		
		CyTableViewFactoryProvider tableViewFactoryFactory = getService(bc, CyTableViewFactoryProvider.class);
		CyTableViewFactory tableViewFactory = tableViewFactoryFactory.createTableViewFactory(lexicon, TableViewRendererImpl.ID);
		
		TableViewRendererImpl renderer = new TableViewRendererImpl(registrar, tableViewFactory, lexicon, popupMenuHelper);
		registerService(bc, renderer, TableViewRenderer.class);
	}

}
