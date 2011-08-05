package org.cytoscape.io.webservice.biomart.rest;

public class Filter {
	private String name;
	private String value;
	
	
	public Filter(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

}
