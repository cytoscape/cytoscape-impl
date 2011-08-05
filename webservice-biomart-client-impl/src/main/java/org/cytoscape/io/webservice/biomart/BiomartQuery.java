package org.cytoscape.io.webservice.biomart;


public final class BiomartQuery {
	
	private final String xmlQuery;
	private final String keyColumnName;
	private final String tableName;
	
	public BiomartQuery(final String xmlQuery, final String keyColumnName, final String tableName) {
		this.keyColumnName = keyColumnName;
		this.xmlQuery = xmlQuery;
		this.tableName = tableName;
	}
	
	public String getKeyColumnName () {
		return this.keyColumnName;
	}


	public String getQueryString() {
		return xmlQuery;
	}
	
	public String getTableName() {
		return tableName;
	}

}
