/*
 Copyright (c) 2008, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.model.internal; 


import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.lang.ref.WeakReference;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTable.SavePolicy;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.equations.Interpreter;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;


/**
 * An interface describing a factory used for creating 
 * {@link CyTable} objects.  This factory will be
 * provided as a service through Spring/OSGi.
 */
public class CyTableFactoryImpl implements CyTableFactory {
	private final CyEventHelper help;
	private final Interpreter interpreter;
	private final CyServiceRegistrar serviceRegistrar;
	private final WeakEventDelegator eventDelegator; 

	public CyTableFactoryImpl(final CyEventHelper help, final Interpreter interpreter,
	                          final CyServiceRegistrar serviceRegistrar)
	{
		this.help             = help;
		this.interpreter      = interpreter;
		this.serviceRegistrar = serviceRegistrar;
		this.eventDelegator = new WeakEventDelegator();
		this.serviceRegistrar.registerService(eventDelegator, TableAddedListener.class, new Properties()); 
	}

	public CyTable createTable(final String name, final String primaryKey, final Class<?> primaryKeyType,
				   final boolean pub, final boolean isMutable)
	{
		final CyTableImpl table = new CyTableImpl(name, primaryKey, primaryKeyType, pub, isMutable,
		                                      SavePolicy.SESSION_FILE, help, interpreter);
		eventDelegator.addListener(table);
		return table;
	}

	private class WeakEventDelegator implements TableAddedListener {
		List<WeakReference<TableAddedListener>> tables = new ArrayList<WeakReference<TableAddedListener>>();  

		public void addListener(TableAddedListener t) {
			tables.add(new WeakReference<TableAddedListener>(t));
		}
		
		public void handleEvent(TableAddedEvent e) {
			for ( WeakReference<TableAddedListener> ref : tables ) {
				TableAddedListener l = ref.get();
				if ( l != null )
					l.handleEvent(e);
			}
		}
	}
}
