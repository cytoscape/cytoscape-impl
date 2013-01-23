package org.cytoscape.cpath2.internal.view.model;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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

import org.cytoscape.cpath2.internal.schemas.search_response.ExtendedRecordType;

/**
 * Wrapper for ExtendedRecordType.
 *
 * @author Ethan Cerami.
 */
public class ExtendedRecordWrapper {
    private ExtendedRecordType record;

    public ExtendedRecordWrapper (ExtendedRecordType record) {
        this.record = record;
    }

    public ExtendedRecordType getRecord() {
        return this.record;
    }

    public String toString() {
        return record.getName();
    }
}
