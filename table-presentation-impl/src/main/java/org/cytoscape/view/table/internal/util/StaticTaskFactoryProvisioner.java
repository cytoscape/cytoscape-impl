package org.cytoscape.view.table.internal.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.cytoscape.model.CyColumn;
import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.task.TogglableTableColumn;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Togglable;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class StaticTaskFactoryProvisioner {
	
	public TaskFactory createFor(TableCellTaskFactory factory, CyColumn column, Object primaryKeyValue) {
		var columnReference = new WeakReference<CyColumn>(column);
		var keyReference = new WeakReference<Object>(primaryKeyValue);
		
		return new TogglableTableCellTaskFactory(factory, columnReference, keyReference);
	}
	
	public TaskFactory createFor(TableColumnTaskFactory factory, CyColumn column) {
		var columnReference = new WeakReference<CyColumn>(column);

		return new TogglableTableColumnTaskFactory(factory, columnReference);
	}
	
	private class TogglableTableCellTaskFactory implements TaskFactory, Togglable {
		
		private final TableCellTaskFactory delegate;
		private final Reference<CyColumn> columnReference;
		private final Reference<Object> keyReference;
		
		TogglableTableCellTaskFactory(
				TableCellTaskFactory delegate,
				Reference<CyColumn> columnReference,
				Reference<Object> keyReference
		) {
			this.delegate = delegate;
			this.columnReference = columnReference;
			this.keyReference = keyReference;
		}

		@Override
		public TaskIterator createTaskIterator() {
			return delegate.createTaskIterator(columnReference.get(), keyReference.get());
		}

		@Override
		public boolean isReady() {
			return delegate.isReady(columnReference.get(), keyReference.get());
		}

		@Override
		public boolean isOn() {
			if (delegate instanceof Togglable)
				return ((Togglable) delegate).isOn();
			
			if (delegate instanceof TogglableTableColumn)
				return ((TogglableTableColumn) delegate).isOn(columnReference.get());
			
			return false;
		}
	}
	
	private class TogglableTableColumnTaskFactory implements TaskFactory, Togglable {
		
		private final TableColumnTaskFactory delegate;
		private final Reference<CyColumn> columnReference;
		
		TogglableTableColumnTaskFactory(TableColumnTaskFactory delegate, Reference<CyColumn> columnReference) {
			this.delegate = delegate;
			this.columnReference = columnReference;
		}

		@Override
		public TaskIterator createTaskIterator() {
			return delegate.createTaskIterator(columnReference.get());
		}

		@Override
		public boolean isReady() {
			return delegate.isReady(columnReference.get());
		}

		@Override
		public boolean isOn() {
			if (delegate instanceof Togglable)
				return ((Togglable) delegate).isOn();
			
			if (delegate instanceof TogglableTableColumn)
				return ((TogglableTableColumn) delegate).isOn(columnReference.get());
			
			return false;
		}
	}
}
