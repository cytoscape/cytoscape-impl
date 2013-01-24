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


import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.cytoscape.work.TunableRecorder;


/**
 */
public class TunableRecorderManager {  
	private List<TunableRecorder> recorders = new ArrayList<TunableRecorder>();

	public void addTunableRecorder(TunableRecorder tr, Map props) {
		if ( !recorders.contains(tr) )
			recorders.add(tr);
	}

	public void removeTunableRecorder(TunableRecorder tr, Map props) {
		recorders.remove(tr);
	}

	public List<TunableRecorder> getRecorders() {
		return Collections.unmodifiableList(recorders);
	}
}

