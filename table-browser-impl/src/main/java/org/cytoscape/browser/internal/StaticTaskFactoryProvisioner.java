package org.cytoscape.browser.internal;

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
