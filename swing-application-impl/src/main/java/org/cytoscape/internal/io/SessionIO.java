package org.cytoscape.internal.io;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionIO {

	private static final Logger logger = LoggerFactory.getLogger(SessionIO.class);

	@SuppressWarnings("unchecked")
	public <T> T read(final File file, final Class<T> type) {
		T obj = null;
		InputStream is = null;
		
		try {
			is = new FileInputStream(file);
			final JAXBContext jaxbContext = JAXBContext.newInstance(type.getPackage().getName(), getClass().getClassLoader());
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			obj = (T) unmarshaller.unmarshal(is);
		} catch (Exception e) {
			logger.error("Read error for " + type, e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ioe) {
				}
			}
		}

		return obj;
	}

	public <T> void write(final T obj, final File file) {
		OutputStream out = null;

		try {
			out = new FileOutputStream(file);
			final JAXBContext jc = JAXBContext.newInstance(obj.getClass().getPackage().getName(), this.getClass()
					.getClassLoader());
			final Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(obj, out);
		} catch (Exception e) {
			logger.error("Write error for " + obj, e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ioe) {
				}
			}
		}
	}
}
