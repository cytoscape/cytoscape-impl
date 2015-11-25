package org.cytoscape.tableimport.internal.util;

import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_BOOLEAN_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_FLOATING_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_INTEGER_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_LONG;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_LONG_LIST;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING;
import static org.cytoscape.tableimport.internal.util.AttributeDataType.TYPE_STRING_LIST;
import static org.junit.Assert.*;

import org.junit.Test;

public class TypeUtilTest {

	@Test
	public void testParseDataTypeList() {
		AttributeDataType[] dataTypes = new AttributeDataType[] {
				TYPE_INTEGER,
				TYPE_LONG,
				TYPE_FLOATING,
				TYPE_BOOLEAN,
				TYPE_STRING,
				TYPE_INTEGER_LIST,
				TYPE_LONG_LIST,
				TYPE_FLOATING_LIST,
				TYPE_BOOLEAN_LIST,
				TYPE_STRING_LIST
		};
		
		assertArrayEquals(dataTypes, TypeUtil.parseDataTypeList("i,l,f,b,s,il,ll,fl,bl,sl"));
		assertArrayEquals(dataTypes, TypeUtil.parseDataTypeList("i,l,d,b,s,ii,ll,dd,bb,ss"));
		assertArrayEquals(dataTypes, TypeUtil.parseDataTypeList("i,l,d,b,t,li,ll,lf,lb,lt"));
		assertArrayEquals(dataTypes, TypeUtil.parseDataTypeList("Integer, Long, Double, Boolean, String, List<Integer>, List<Long>, List<Double>, List<Boolean>, List<String>"));
		assertArrayEquals(dataTypes, TypeUtil.parseDataTypeList("integer, long, decimal, boolean, string, integerlist, LONG_LIST, list-floating-point, BOOL_LIST, STRING_LIST"));
		assertArrayEquals(dataTypes, TypeUtil.parseDataTypeList("int,LONG,float,bool,text,intlist,long-list,floatList,booleanList,text list"));
	}
}
