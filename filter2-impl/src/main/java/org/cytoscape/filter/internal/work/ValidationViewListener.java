package org.cytoscape.filter.internal.work;

import java.util.Collections;
import java.util.List;

import org.cytoscape.filter.model.ValidationWarning;

public interface ValidationViewListener {

	public void handleValidated(ValidationEvent event);
	
	
	public static class ValidationEvent {
		
		private final List<ValidationWarning> warnings;
		
		ValidationEvent(List<ValidationWarning> warnings) {
			this.warnings = (warnings == null) ? Collections.emptyList() : warnings;
		}

		public boolean isValid() {
			return warnings.isEmpty();
		}
		
		public List<ValidationWarning> getWarnings() {
			return warnings;
		}
		
		public String getFormattedTooltip() {
			return formatTooltip(warnings);
		}
	}
	
	
	public static String formatTooltip(List<ValidationWarning> warnings) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("Filter cannot be applied to current network:<br>");
		for(ValidationWarning warning : warnings) {
			sb.append(warning.getWarning()).append("<br>");
		}
		sb.append("</html>");
		return sb.toString();
	}
}
