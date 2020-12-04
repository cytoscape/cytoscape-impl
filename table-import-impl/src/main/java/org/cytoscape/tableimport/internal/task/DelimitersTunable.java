package org.cytoscape.tableimport.internal.task;

import static org.cytoscape.tableimport.internal.reader.TextDelimiter.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.cytoscape.tableimport.internal.reader.TextDelimiter;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSelection;

public class DelimitersTunable {

	@Tunable(description = "Text delimiters", 
	         longDescription = "Select the delimiters to use to separate columns in the table, "+
					           "from the list '``,``',' ','``TAB``', or '``;``'.  ``TAB`` and '``,``' are used by default",
	         exampleStringValue = ";,\\,",
	         context = "both")
	public ListMultipleSelection<String> delimiters;

  static String[] defaults = {TAB.getDelimiter(),COMMA.getDelimiter()};
	
	
	public DelimitersTunable() {
		List<String> values = Arrays.asList(
			COMMA.getDelimiter(),
			SEMICOLON.getDelimiter(),
			SPACE.getDelimiter(),
			TAB.getDelimiter(),
			"\t" // to support escape sequences used in command arguments
		);
		delimiters = new ListMultipleSelection<>(values);
    // Initialize with the defaults
    delimiters.setSelectedValues(Arrays.asList(defaults));
	}
	
	public void setSelectedValues(List<TextDelimiter> values) {
		List<String> strVaues = values.stream().map(TextDelimiter::getDelimiter).collect(Collectors.toList());
		delimiters.setSelectedValues(strVaues);
	}
	
	public List<String> getSelectedValues() {
		Set<String> values = new HashSet<>(delimiters.getSelectedValues());
		if(values.remove("\t")) {
			values.add(TAB.getDelimiter());
		}
		return new ArrayList<>(values	);
	}
	
	ListSelection<String> getTunable() {
		return delimiters;
	}
}
