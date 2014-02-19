package org.cytoscape.work.internal.task;

import java.awt.GridBagConstraints;
import java.util.Map;
import java.util.HashMap;

/**
 * Uses the builder pattern for filling fields of a {@link GridBagConstraints}.
 */
class EasyGBC extends GridBagConstraints {
	final Map<String,Integer> anchors = new HashMap<String,Integer>(); // this should be populated staticly, but the GBC's constants are not available at compile-time
	
	public EasyGBC() {
		anchors.put("north", super.NORTH);
		anchors.put("northwest", super.NORTHWEST);
		anchors.put("west", super.WEST);
		anchors.put("south", super.SOUTH);
		anchors.put("east", super.EAST);
		reset();
	}

	public EasyGBC reset() {
		gridx = 0;			gridy = 0;
		gridwidth = 1;		gridheight = 1;
		weightx = 0.0;		weighty = 0.0;
		fill = GridBagConstraints.NONE;
		insets.set(0, 0, 0, 0);
		return this;
	}

	public EasyGBC noExpand() {
		weightx = 0.0;
		weighty = 0.0;
		fill = GridBagConstraints.NONE;
		return this;
	}

	public EasyGBC expandHoriz() {
		weightx = 1.0;
		weighty = 0.0;
		fill = GridBagConstraints.HORIZONTAL;
		return this;
	}

	public EasyGBC expandBoth() {
		weightx = 1.0;
		weighty = 1.0;
		fill = GridBagConstraints.BOTH;
		return this;
	}

	public EasyGBC right() {
		gridx++;
		return this;
	}

	public EasyGBC down() {
		gridx = 0;
		gridy++;
		return this;
	}

	public EasyGBC position(int x, int y) {
		gridx = x;
		gridy = y;
		return this;
	}

	public EasyGBC noSpan() {
		gridwidth = 1;
		gridheight = 1;
		return this;
	}

	public EasyGBC spanHoriz(int n) {
		gridwidth = n;
		gridheight = 1;
		return this;
	}

	public EasyGBC insets(int t, int l, int b, int r) {
		insets.set(t, l, b, r);
		return this;
	}

	public EasyGBC noInsets() {
		insets.set(0, 0, 0, 0);
		return this;
	}

	public EasyGBC anchor(String str) {
		anchor = anchors.get(str);
		return this;
	}
}