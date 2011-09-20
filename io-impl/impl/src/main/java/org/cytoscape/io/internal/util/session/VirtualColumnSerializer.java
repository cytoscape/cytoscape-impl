package org.cytoscape.io.internal.util.session;

import java.io.PrintWriter;


public class VirtualColumnSerializer {
	String name;
	String sourceTable;
	String targetTable;
	String sourceColumn;
	String sourceJoinKey;
	String targetJoinKey;
	boolean isImmutable;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSourceTable() {
		return sourceTable;
	}

	public void setSourceTable(String sourceTable) {
		this.sourceTable = sourceTable;
	}

	public String getTargetTable() {
		return targetTable;
	}

	public void setTargetTable(String targetTable) {
		this.targetTable = targetTable;
	}

	public String getSourceColumn() {
		return sourceColumn;
	}

	public void setSourceColumn(String sourceColumn) {
		this.sourceColumn = sourceColumn;
	}

	public String getSourceJoinKey() {
		return sourceJoinKey;
	}

	public void setSourceJoinKey(String sourceJoinKey) {
		this.sourceJoinKey = sourceJoinKey;
	}

	public String getTargetJoinKey() {
		return targetJoinKey;
	}

	public void setTargetJoinKey(String targetJoinKey) {
		this.targetJoinKey = targetJoinKey;
	}

	public boolean isImmutable() {
		return isImmutable;
	}

	public void setImmutable(boolean isImmutable) {
		this.isImmutable = isImmutable;
	}

	public VirtualColumnSerializer(String name, String sourceTable, String targetTable, String sourceColumn, String sourceJoinKey, String targetJoinKey, boolean isImmutable) {
		this.name = name;
		this.sourceTable = sourceTable;
		this.targetTable = targetTable;
		this.sourceColumn = sourceColumn;
		this.sourceJoinKey = sourceJoinKey;
		this.targetJoinKey = targetJoinKey;
		this.isImmutable = isImmutable;
	}
	
	public VirtualColumnSerializer(String line) {
		String[] data = line.split("\t");
		targetTable = data[0];
		name = SessionUtil.unescape(data[1]);
		sourceTable = data[2];
		sourceColumn = SessionUtil.unescape(data[3]);
		sourceJoinKey = SessionUtil.unescape(data[4]);
		targetJoinKey = SessionUtil.unescape(data[5]);
		isImmutable = data[6].equals("true");
	}
	
	public void serialize(PrintWriter writer) {
		writer.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
					  targetTable,
					  SessionUtil.escape(name),
					  sourceTable,
					  SessionUtil.escape(sourceColumn),
					  SessionUtil.escape(sourceJoinKey),
					  SessionUtil.escape(targetJoinKey),
					  String.valueOf(isImmutable));
		
	}
}