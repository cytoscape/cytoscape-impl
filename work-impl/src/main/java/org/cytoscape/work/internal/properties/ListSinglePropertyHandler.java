package org.cytoscape.work.internal.properties;

/*
 * #%L
 * org.cytoscape.work-impl
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2015 The Cytoscape Consortium
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
import java.util.Collections;
import java.util.List;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

@SuppressWarnings("rawtypes")
public class ListSinglePropertyHandler extends ParameterizedTypePropertyHandler<ListSingleSelection> {

	public ListSinglePropertyHandler(Field field, Object instance, Tunable tunable) {
		super(field, instance, tunable);
	}

	public ListSinglePropertyHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		super(getter, setter, instance, tunable);
	}

	@Override
	protected List<?> getElementValues() {
		return Collections.singletonList(getContainer().getSelectedValue());
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	protected void setElementValues(ListSingleSelection container, List<?> values) {
		container.setSelectedValue(values.get(0));
	}
	

}
