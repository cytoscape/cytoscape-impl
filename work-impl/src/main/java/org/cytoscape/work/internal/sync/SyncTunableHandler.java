package org.cytoscape.work.internal.sync;

/*
 * #%L
 * org.cytoscape.work-impl
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.cytoscape.work.AbstractTunableHandler;
import org.cytoscape.work.Tunable;

public class SyncTunableHandler extends AbstractTunableHandler {

	private Map<String, Object> valueMap;

	public SyncTunableHandler(final Field field, final Object instance, final Tunable tunable) {
		super(field, instance, tunable);
	}

	public SyncTunableHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		super(getter, setter, instance, tunable);
	}

	@Override
	public void handle() {
		try {
			if (valueMap.containsKey(getName())) {
				setValue(valueMap.get(getName()));
			}
		} catch (Exception e) {
			throw new RuntimeException("Exception setting tunable value.", e);
		}
	}

	public void setValueMap(final Map<String, Object> valueMap) {
		this.valueMap = valueMap;
	}
}
