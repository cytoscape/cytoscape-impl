package org.cytoscape.task.internal.table;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2013 The Cytoscape Consortium
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


import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;


/** A helper class used by various "edit" classes. */
final class SaveColumn {
	final List<PrimaryKeyAndValue> keysAndValues;

	SaveColumn(final CyTable table, final String columnName) {
		keysAndValues = new ArrayList<>(table.getRowCount());
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