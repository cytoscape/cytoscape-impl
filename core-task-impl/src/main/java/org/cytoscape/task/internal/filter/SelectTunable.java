package org.cytoscape.task.internal.filter;

import java.util.Optional;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class SelectTunable {

	public enum Action {
		SELECT,
		SHOW
	}
	
	@Tunable(description="Action to preform on nodes/edges that pass the filter, 'select'= set selected, 'show' = hide nodes/edges that do not pass the filter")
	public ListSingleSelection<String> action = new ListSingleSelection<>("select", "show");
	
	
	public Optional<Action> getAction() {
		String a = action.getSelectedValue();
		if("select".equalsIgnoreCase(a)) {
			return Optional.of(Action.SELECT);
		} else if("show".equalsIgnoreCase(a)) {
			return Optional.of(Action.SHOW);
		} else {
			return Optional.empty();
		}
	}
	
}
