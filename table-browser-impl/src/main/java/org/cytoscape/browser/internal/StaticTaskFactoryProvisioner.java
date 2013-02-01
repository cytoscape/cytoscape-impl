package org.cytoscape.browser.internal;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.cytoscape.model.CyColumn;
import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class StaticTaskFactoryProvisioner {
	public  TaskFactory createFor(final TableCellTaskFactory factory, final CyColumn column, final Object primaryKeyValue) {
		final Reference<CyColumn> columnReference = new WeakReference<CyColumn>(column);
		final Reference<Object> keyReference = new WeakReference<Object>(primaryKeyValue);
		return new TaskFactory() {
			public TaskIterator createTaskIterator() {
				return factory.createTaskIterator(columnReference.get(), keyReference.get());
			}
			
			public boolean isReady() {
				return factory.isReady(columnReference.get(), keyReference.get());
			}
		};
	}
	
	public  TaskFactory createFor(final TableColumnTaskFactory factory, final CyColumn column) {
		final Reference<CyColumn> columnReference = new WeakReference<CyColumn>(column);
		return new TaskFactory() {
			public TaskIterator createTaskIterator() {
				return factory.createTaskIterator(columnReference.get());
			}
			
			public boolean isReady() {
				return factory.isReady(columnReference.get());
			}
		};
	}
}
