package org.cytoscape.io.internal.read;

import static org.junit.Assert.*;
import org.junit.Test;

public class SUIDUpdaterTest {

	@Test
	public void testIsUpdatableSUIDColumn() {
		assertTrue(SUIDUpdater.isUpdatableSUIDColumn("name.SUID"));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn("name.suid")); // Case sensitive!
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn("name.Suid"));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn("name.id"));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn("SUID"));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn("suid"));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(""));
		assertFalse(SUIDUpdater.isUpdatableSUIDColumn(null));
	}
}
