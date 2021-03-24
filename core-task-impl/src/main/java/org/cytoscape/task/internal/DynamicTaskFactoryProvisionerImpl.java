package org.cytoscape.task.internal;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.RowTaskFactory;
import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Togglable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

public class DynamicTaskFactoryProvisionerImpl implements DynamicTaskFactoryProvisioner {

	private final CyServiceRegistrar serviceRegistrar;

	public DynamicTaskFactoryProvisionerImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public TaskFactory createFor(NetworkTaskFactory factory) {
		return factory instanceof Togglable ?
				new DynamicTogglableTaskFactory(factory) :
				new DynamicTaskFactory(factory);
	}

	@Override
	public  TaskFactory createFor(NetworkViewTaskFactory factory) {
		return factory instanceof Togglable ?
				new DynamicTogglableTaskFactory(factory) :
				new DynamicTaskFactory(factory);
	}

	@Override
	public  TaskFactory createFor(NetworkCollectionTaskFactory factory) {
		return factory instanceof Togglable ?
				new DynamicTogglableTaskFactory(factory) :
				new DynamicTaskFactory(factory);
	}

	@Override
	public  TaskFactory createFor(NetworkViewCollectionTaskFactory factory) {
		return factory instanceof Togglable ?
				new DynamicTogglableTaskFactory(factory) :
				new DynamicTaskFactory(factory);
	}

	@Override
	public  TaskFactory createFor(TableTaskFactory factory) {
		return factory instanceof Togglable ?
				new DynamicTogglableTaskFactory(factory) :
				new DynamicTaskFactory(factory);
	}
	
	@Override
	public TaskFactory createFor(TableColumnTaskFactory factory, CyColumn column) {
		return factory instanceof Togglable ? 
				new DynamicTogglableTaskFactory(factory, column) :
				new DynamicTaskFactory(factory, column);
	}

	@Override
	public TaskFactory createFor(RowTaskFactory factory, CyRow row) {
		return factory instanceof Togglable ?
				new DynamicTogglableTaskFactory(factory, row) :
				new DynamicTaskFactory(factory, row);
	}

	@Override
	public TaskFactory createFor(TableCellTaskFactory factory, CyColumn column, Object primaryKeyValue) {
		return factory instanceof Togglable ?
				new DynamicTogglableTaskFactory(factory, column, primaryKeyValue) :
				new DynamicTaskFactory(factory, column, primaryKeyValue);
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private CyApplicationManager getApplicationManager() {
		return serviceRegistrar.getService(CyApplicationManager.class);
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class DynamicTaskFactory implements TaskFactory {

		protected final Object factory;
		protected Reference<? extends CyIdentifiable> targetReference;
		protected Reference<Object> pkReference;

		DynamicTaskFactory(Object factory) {
			this.factory = factory;
		}
		
		DynamicTaskFactory(TableColumnTaskFactory factory, CyColumn column) {
			this.factory = factory;
			this.targetReference = new WeakReference<>(column);
		}
		
		DynamicTaskFactory(RowTaskFactory factory, CyRow row) {
			this.factory = factory;
			this.targetReference = new WeakReference<>(row);
		}
		
		DynamicTaskFactory(TableCellTaskFactory factory, CyColumn column, Object pkValue) {
			this.factory = factory;
			this.targetReference = new WeakReference<>(column);
			this.pkReference = new WeakReference<>(pkValue);
		}
		
		@Override
		public TaskIterator createTaskIterator() {
			if (factory instanceof NetworkTaskFactory)
				return ((NetworkTaskFactory) factory).createTaskIterator(getApplicationManager().getCurrentNetwork());
			
			if (factory instanceof NetworkViewTaskFactory)
				return ((NetworkViewTaskFactory) factory).createTaskIterator(getApplicationManager().getCurrentNetworkView());
			
			if (factory instanceof NetworkCollectionTaskFactory)
				return ((NetworkCollectionTaskFactory) factory).createTaskIterator(getApplicationManager().getSelectedNetworks());
			
			if (factory instanceof NetworkViewCollectionTaskFactory)
				return ((NetworkViewCollectionTaskFactory) factory).createTaskIterator(getApplicationManager().getSelectedNetworkViews());
			
			if (factory instanceof TableTaskFactory)
				return ((TableTaskFactory) factory).createTaskIterator(getApplicationManager().getCurrentTable());
			
			if (factory instanceof TableColumnTaskFactory)
				return ((TableColumnTaskFactory) factory).createTaskIterator((CyColumn) targetReference.get());
			
			if (factory instanceof RowTaskFactory)
				return ((RowTaskFactory) factory).createTaskIterator((CyRow) targetReference.get());
			
			if (factory instanceof TableCellTaskFactory)
				return ((TableCellTaskFactory) factory).createTaskIterator((CyColumn) targetReference.get(), pkReference.get());
			
			return new TaskIterator();
		}
		
		@Override
		public boolean isReady() {
			if (factory instanceof NetworkTaskFactory)
				return ((NetworkTaskFactory) factory).isReady(getApplicationManager().getCurrentNetwork());
			
			if (factory instanceof NetworkViewTaskFactory)
				return ((NetworkViewTaskFactory) factory).isReady(getApplicationManager().getCurrentNetworkView());
			
			if (factory instanceof NetworkCollectionTaskFactory)
				return ((NetworkCollectionTaskFactory) factory).isReady(getApplicationManager().getSelectedNetworks());
			
			if (factory instanceof NetworkViewCollectionTaskFactory)
				return ((NetworkViewCollectionTaskFactory) factory).isReady(getApplicationManager().getSelectedNetworkViews());
			
			if (factory instanceof TableTaskFactory)
				return ((TableTaskFactory) factory).isReady(getApplicationManager().getCurrentTable());
			
			if (factory instanceof TableColumnTaskFactory)
				return ((TableColumnTaskFactory) factory).isReady((CyColumn) targetReference.get());
			
			if (factory instanceof RowTaskFactory)
				return ((RowTaskFactory) factory).isReady((CyRow) targetReference.get());
			
			if (factory instanceof TableCellTaskFactory)
				return ((TableCellTaskFactory) factory).isReady((CyColumn) targetReference.get(), pkReference.get());
			
			return false;
		}
	}
	
	private class DynamicTogglableTaskFactory extends DynamicTaskFactory implements Togglable {
		
		DynamicTogglableTaskFactory(Object factory) {
			super(factory);
		}
		
		DynamicTogglableTaskFactory(TableColumnTaskFactory factory, CyColumn column) {
			super(factory, column);
		}
		
		DynamicTogglableTaskFactory(RowTaskFactory factory, CyRow row) {
			super(factory, row);
		}
		
		DynamicTogglableTaskFactory(TableCellTaskFactory factory, CyColumn column, Object pkValue) {
			super(factory, column, pkValue);
		}
		
		@Override
		public boolean isOn() {
			if (factory instanceof NetworkTaskFactory)
				return ((NetworkTaskFactory) factory).isOn(getApplicationManager().getCurrentNetwork());
			
			if (factory instanceof NetworkViewTaskFactory)
				return ((NetworkViewTaskFactory) factory).isOn(getApplicationManager().getCurrentNetworkView());
			
			if (factory instanceof NetworkCollectionTaskFactory)
				return ((NetworkCollectionTaskFactory) factory).isOn(getApplicationManager().getSelectedNetworks());
			
			if (factory instanceof NetworkViewCollectionTaskFactory)
				return ((NetworkViewCollectionTaskFactory) factory).isOn(getApplicationManager().getSelectedNetworkViews());
			
			if (factory instanceof TableTaskFactory)
				return ((TableTaskFactory) factory).isOn(getApplicationManager().getCurrentTable());
			
			if (factory instanceof TableColumnTaskFactory)
				return ((TableColumnTaskFactory) factory).isOn((CyColumn) targetReference.get());
			
			if (factory instanceof RowTaskFactory)
				return ((RowTaskFactory) factory).isOn((CyRow) targetReference.get());
			
			if (factory instanceof TableCellTaskFactory)
				return ((TableCellTaskFactory) factory).isOn((CyColumn) targetReference.get(), pkReference.get());
			
			return false;
		}
	}
}
