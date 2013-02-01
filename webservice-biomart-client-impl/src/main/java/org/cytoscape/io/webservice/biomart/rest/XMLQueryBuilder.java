package org.cytoscape.io.webservice.biomart.rest;

/*
 * #%L
 * Cytoscape Biomart Webservice Impl (webservice-biomart-client-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 *
 */
public class XMLQueryBuilder {
	private static DocumentBuilderFactory factory;
	private static DocumentBuilder builder;

	static {
		factory = DocumentBuilderFactory.newInstance();

		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param dataset DOCUMENT ME!
	 * @param attrs DOCUMENT ME!
	 * @param filters DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public static String getQueryString(Dataset dataset, Attribute[] attrs, Filter[] filters) {
		final Document doc = builder.newDocument();
		Element query = doc.createElement("Query");
		query.setAttribute("virtualSchemaName", "default");
		query.setAttribute("header", "1");
		query.setAttribute("uniqueRows", "1");
		query.setAttribute("count", "");
		query.setAttribute("datasetConfigVersion", "0.6");
		query.setAttribute("formatter", "TSV");

		doc.appendChild(query);

		Element ds = doc.createElement("Dataset");
		ds.setAttribute("name", dataset.getName());
		query.appendChild(ds);

		for (Attribute attr : attrs) {
			Element at = doc.createElement("Attribute");
			at.setAttribute("name", attr.getName());
			ds.appendChild(at);
		}

		if ((filters != null) && (filters.length != 0)) {
			for (Filter filter : filters) {
				Element ft = doc.createElement("Filter");
				ft.setAttribute("name", filter.getName());
				if(filter.getValue() == null) {
					ft.setAttribute("excluded", "0");
				} else 
					ft.setAttribute("value", filter.getValue());
				ds.appendChild(ft);
			}
		}

		TransformerFactory tff = TransformerFactory.newInstance();
		Transformer tf;
		String result = null;

		try {
			tf = tff.newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			StringWriter strWtr = new StringWriter();
			StreamResult strResult = new StreamResult(strWtr);

			tf.transform(new DOMSource(doc.getDocumentElement()), strResult);

			result = strResult.getWriter().toString();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
}
