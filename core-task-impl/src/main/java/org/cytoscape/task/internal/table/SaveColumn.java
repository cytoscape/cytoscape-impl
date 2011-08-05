/*
 Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.task.internal.table;


import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;


/** A helper class used by various "edit" classes. */
final class SaveColumn {
	final List<PrimaryKeyAndValue> keysAndValues;

	SaveColumn(final CyTable table, final String columnName) {
		keysAndValues = new ArrayList<PrimaryKeyAndValue>(table.getRowCount());
		final String primarykeyName = table.getPrimaryKey().getName();
		for (final CyRow row : table.getAllRows()) {
			final Object primaryKey = row.getRaw(primarykeyName);
			final Object value      = row.getRaw(columnName);
			keysAndValues.add(new PrimaryKeyAndValue(primaryKey, value));
		}
	}

	void restoreColumn(final CyTable table, final String columnName) {
		for (final PrimaryKeyAndValue keyAndValue : keysAndValues)
			table.getRow(keyAndValue.getPrimaryKey()).set(columnName, keyAndValue.getValue());
	}
}


final class PrimaryKeyAndValue {
	final Object primaryKey;
	final Object value;

	PrimaryKeyAndValue(final Object primaryKey, final Object value) {
		this.primaryKey = primaryKey;
		this.value      = value;
	}

	Object getPrimaryKey() {
		return primaryKey;
	}

	Object getValue() {
		return value;
	}
}