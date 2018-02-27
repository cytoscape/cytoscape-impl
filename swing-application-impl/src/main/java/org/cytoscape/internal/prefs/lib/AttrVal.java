package org.cytoscape.internal.prefs.lib;

//---------------------------------------------------------------
public class AttrVal
{
	private String attribute;
	private String value;
	private String namespace;
	
	
	public AttrVal(String n, String a, String v)	{ 
		namespace = n; 
		attribute = a; 
		value = v;	
	}
	
	public void setNamespace(String a) 		{ namespace = a;  } 
	public String getNamespace() 			{ return namespace; } 
	public void setAttribute(String a) 		{ attribute = a;  } 
	public String getAttribute() 			{ return attribute; } 
	public void setValue(String v) 			{ value = v;} 
	public String getValue() 				{ return value; } 
	public String toString() 				{ return namespace + ":" + attribute + " = " + value; } 
}