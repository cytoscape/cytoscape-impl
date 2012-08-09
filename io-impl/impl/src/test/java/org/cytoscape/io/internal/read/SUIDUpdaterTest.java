package org.cytoscape.io.internal.read;

import static org.junit.Assert.*;

import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyColumnTest;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;
import org.junit.Test;

public class SUIDUpdaterTest {

	@Test
	public void testIsUpdatableSUIDColumn() {
		assertTrue(SUIDUpdater.isUpdatableSUIDColumn(newColumn("name.SUID", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newColumn("name.SUID", Integer.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newColumn("name.SUID", Double.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newColumn("name.SUID", String.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newColumn("name.SUID", Boolean.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newColumn("name.SUID", Long.class, true, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newColumn("name.SUID", Long.class, false, true)));
		
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newColumn("name.suid", Long.class, false, false))); // Case sensitive!
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newColumn("name.Suid", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newColumn("name.id", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newColumn("SUID", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newColumn("suid", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newColumn("", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newColumn(null, Long.class, false, false)));
		
		// List column
		assertTrue(SUIDUpdater.isUpdatableSUIDColumn(newListColumn("list.SUID", Long.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newListColumn("list.SUID", Integer.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newListColumn("list.SUID", Double.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newListColumn("list.SUID", String.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newListColumn("list.SUID", Boolean.class, false, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newListColumn("list.SUID", Long.class, true, false)));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(newListColumn("list.SUID", Long.class, false, true)));
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
