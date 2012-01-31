package org.cytoscape.internal.io;

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

/**
 * Responsible for writing and reading session state info as private app files.
 */
public class SessionStateIO {
	
	private static final String SESSION_STATE_PACKAGE = SessionState.class.getPackage().getName();
	private static final Logger logger = LoggerFactory.getLogger(SessionStateIO.class);

	public SessionState read(final File file) {
		SessionState sessionState = null;
		InputStream is = null;
		
		try {
			is = new FileInputStream(file);
			final JAXBContext jaxbContext = JAXBContext.newInstance(SESSION_STATE_PACKAGE, getClass().getClassLoader());
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			sessionState = (SessionState) unmarshaller.unmarshal(is);
		} catch (Exception e) {
			logger.error("SessionState Read error", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ioe) {
				}
			}
		}

		return sessionState;
	}

	public void write(final SessionState sessionState, final File file) {
		OutputStream out = null;

		try {
			out = new FileOutputStream(file);
			final JAXBContext jc = JAXBContext.newInstance(SessionState.class.getPackage().getName(), this.getClass()
					.getClassLoader());
			final Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(sessionState, out);
		} catch (Exception e) {
			logger.error("SessionState Write error", e);
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
