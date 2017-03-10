package org.cytoscape.model.internal;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cytoscape.equations.Interpreter;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2016 The Cytoscape Consortium
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

/**
 * An interface describing a factory used for creating 
 * {@link CyTable} objects.  This factory will be
 * provided as a service through Spring/OSGi.
 */
public class CyTableFactoryImpl implements CyTableFactory {
	
	private final CyEventHelper eventHelper;
	private final CyServiceRegistrar serviceRegistrar;
	private final WeakEventDelegator eventDelegator; 

	public CyTableFactoryImpl(final CyEventHelper eventHelper, final CyServiceRegistrar serviceRegistrar) {
		this.eventHelper = eventHelper;
		this.serviceRegistrar = serviceRegistrar;
		this.eventDelegator = new WeakEventDelegator();
		this.serviceRegistrar.registerService(eventDelegator, TableAddedListener.class, new Properties());
	}

	@Override
	public CyTable createTable(final String name, final String primaryKey, final Class<?> primaryKeyType,
			final boolean pub, final boolean isMutable) {
		return createTable(name, primaryKey, primaryKeyType, pub, isMutable, CyTableFactory.InitialTableSize.MEDIUM);
	}

	@Override
	public CyTable createTable(final String name, final String primaryKey, final Class<?> primaryKeyType,
			final boolean pub, final boolean isMutable, final CyTableFactory.InitialTableSize size) {
		final CyTableImpl table = new CyTableImpl(name, primaryKey, primaryKeyType, pub, isMutable,
				SavePolicy.SESSION_FILE, eventHelper, serviceRegistrar.getService(Interpreter.class), size.getSize());
		eventDelegator.addListener(table);
		
		return table;
	}

	private class WeakEventDelegator implements TableAddedListener {
		
		List<WeakReference<TableAddedListener>> tables = new ArrayList<>();

		public void addListener(TableAddedListener t) {
			tables.add(new WeakReference<TableAddedListener>(t));
		}

		@Override
		public void handleEvent(TableAddedEvent e) {
			for (WeakReference<TableAddedListener> ref : tables) {
				TableAddedListener l = ref.get();
				
				if (l != null)
					l.handleEvent(e);
			}
		}
	}
}
