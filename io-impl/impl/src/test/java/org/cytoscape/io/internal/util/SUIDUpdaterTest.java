package org.cytoscape.io.internal.util;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import static org.junit.Assert.*;

import java.util.List;

import org.cytoscape.io.internal.util.SUIDUpdater;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyColumnTest;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;
import org.junit.Test;

public class SUIDUpdaterTest {

	@Test
	public void testIsUpdatableSUIDColumn() {
		assertTrue(SUIDUpdater.isUpdatable(newColumn("name.SUID", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newColumn("name.SUID", Integer.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newColumn("name.SUID", Double.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newColumn("name.SUID", String.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newColumn("name.SUID", Boolean.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newColumn("name.SUID", Long.class, true, false)));
		assertFalse(SUIDUpdater.isUpdatable(newColumn("name.SUID", Long.class, false, true)));
		
		assertFalse(SUIDUpdater.isUpdatable(newColumn("name.suid", Long.class, false, false))); // Case sensitive!
		assertFalse(SUIDUpdater.isUpdatable(newColumn("name.Suid", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newColumn("name.id", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newColumn("SUID", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newColumn("suid", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newColumn("", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newColumn(null, Long.class, false, false)));
		
		// List column
		assertTrue(SUIDUpdater.isUpdatable(newListColumn("list.SUID", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newListColumn("list.SUID", Integer.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newListColumn("list.SUID", Double.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newListColumn("list.SUID", String.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newListColumn("list.SUID", Boolean.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatable(newListColumn("list.SUID", Long.class, true, false)));
		assertFalse(SUIDUpdater.isUpdatable(newListColumn("list.SUID", Long.class, false, true)));
	}
	
	private CyColumn newColumn(final String name, final Class<?> type, final boolean isPK, final boolean isVirtual) {
		return new DummyColumn(name, type, null, isPK, new DummyVirtualColumnInfo(isVirtual));
	}
	
	private CyColumn newListColumn(final String name, final Class<?> type, final boolean isPK, final boolean isVirtual) {
		return new DummyColumn(name, List.class, type, isPK, new DummyVirtualColumnInfo(isVirtual));
	}
	
	static class DummyColumn implements CyColumn {
		private String name;
		private Class<?> type;
		private Class<?> listType;
		private boolean isPK;
		private VirtualColumnInfo virtualInfo;

		public DummyColumn(String name, Class<?> type, Class<?> listType, boolean isPK, VirtualColumnInfo virtualInfo) {
			this.name = name;
			this.type = type;
			this.listType = listType;
			this.isPK = isPK;
			this.virtualInfo = virtualInfo;
		}

		@Override
		public void setName(String newName) {
			this.name = newName;
		}
		
		@Override
		public boolean isPrimaryKey() {
			return isPK;
		}
		
		@Override
		public boolean isImmutable() {
			return false;
		}
		
		@Override
		public VirtualColumnInfo getVirtualColumnInfo() {
			return virtualInfo;
		}
		
		@Override
		public <T> List<T> getValues(Class<? extends T> type) {
			return null;
		}
		
		@Override
		public Class<?> getType() {
			return type;
		}
		
		@Override
		public CyTable getTable() {
			return null;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public Class<?> getListElementType() {
			return listType;
		}
		
		@Override
		public Object getDefaultValue() {
			return null;
		}
	};
	
	private static class DummyVirtualColumnInfo implements VirtualColumnInfo {

		private final boolean isVirtual;
		
		public DummyVirtualColumnInfo(boolean isVirtual) {
			this.isVirtual = isVirtual;
		}

		@Override
		public boolean isVirtual() {
			return isVirtual;
		}

		@Override
		public String getSourceColumn() {
			return null;
		}

		@Override
		public String getSourceJoinKey() {
			return null;
		}

		@Override
		public String getTargetJoinKey() {
			return null;
		}

		@Override
		public CyTable getSourceTable() {
			return null;
		}

		@Override
		public boolean isImmutable() {
			return false;
		}
	}
}
