package org.cytoscape.group.internal.data;

/*
 * #%L
 * Cytoscape Group Data Impl (group-data-impl)
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

import org.cytoscape.event.AbstractCyEvent;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupSettingsManager.GroupViewType;

public class GroupViewTypeChangedEvent extends AbstractCyEvent<CyGroup> {
		final GroupViewType oldType;
		final GroupViewType newType;

		public GroupViewTypeChangedEvent(CyGroup group, GroupViewType oldType, GroupViewType newType) {
			super(group, GroupViewTypeChangedListener.class);
			this.oldType = oldType;
			this.newType = newType;
		}

		public GroupViewType getOldType() {return oldType;}
		public GroupViewType getNewType() {return newType;}
		public CyGroup getGroup() {return getSource();}
}
