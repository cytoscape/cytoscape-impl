
package org.cytoscape.work.internal.sync;


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

