/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cytoscape.webservice.psicquic.simpleclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Simple rest client created by IntAct team.
 * 
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * Modified by Keiichiro Ono for 2.7
 */
public final class PSICQUICSimpleClient {

	public static final String XML25 = "xml25";
	public static final String MITAB25 = "tab25";
	public static final String MITAB27 = "tab27";
	public static final String MITAB25_COMPRESSED = "tab25-bin";
	public static final String COUNT = "count";

	private String serviceRestUrl;

	public PSICQUICSimpleClient(String serviceRestUrl) {
		this.serviceRestUrl = serviceRestUrl;
	}

	public InputStream getByQuery(String query) throws IOException {
		return getByQuery(query, MITAB25);
	}

	public InputStream getByInteractor(String query) throws IOException {
		return getByQuery(query, MITAB25);
	}

	public InputStream getByInteraction(String query) throws IOException {
		return getByQuery(query, MITAB25);
	}

	public InputStream getByQuery(String query, String format) throws IOException {
		return getByQuery(query, format, 0, Integer.MAX_VALUE);
	}

	public InputStream getByInteractor(String query, String format) throws IOException {
		return getByInteractor(query, format, 0, Integer.MAX_VALUE);
	}

	public InputStream getByInteraction(String query, String format) throws IOException {
		return getByInteraction(query, format, 0, Integer.MAX_VALUE);
	}

	public InputStream getByQuery(String query, String format, int firstResult, int maxResults) throws IOException {
		return getBy("query", query, format, firstResult, maxResults);
	}

	public InputStream getByInteractor(String query, String format, int firstResult, int maxResults) throws IOException {
		return getBy("interactor", query, format, firstResult, maxResults);
	}

	public InputStream getByInteraction(String query, String format, int firstResult, int maxResults)
			throws IOException {
		return getBy("interaction", query, format, firstResult, maxResults);
	}

	public long countByQuery(String query) throws IOException {
		return countBy("query", query);
	}

	public long countByInteractor(String query) throws IOException {
		return countBy("interactor", query);
	}

	public long countByInteraction(String query) throws IOException {
		return countBy("interaction", query);
	}

	private InputStream getBy(String queryType, String query, String format, int firstResult, int maxResults)
			throws IOException {
		final String encodedQuery = encodeQuery(query);

		URL url = createUrl(queryType, encodedQuery, format, firstResult, maxResults);

		return url.openStream();
	}

	private long countBy(String queryType, String query) throws IOException {
		InputStream result = getBy(queryType, query, COUNT, 0, 0);

		String strCount = streamToString(result);
		strCount = strCount.replaceAll("\n", "");

		result.close();
		
		return Long.parseLong(strCount);
	}

	private String encodeQuery(String query) {
		String encodedQuery;

		try {
			encodedQuery = URLEncoder.encode(query, "UTF-8");
			encodedQuery = encodedQuery.replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 should be supported");
		}
		return encodedQuery;
	}

	private URL createUrl(String queryType, String encodedQuery, String format, int firstResult, int maxResults) {
		String strUrl = serviceRestUrl + "/" + queryType + "/" + encodedQuery + "?format=" + format + "&firstResult="
				+ firstResult + "&maxResults=" + maxResults;
		strUrl = strUrl.replaceAll("//" + queryType, "/" + queryType);

		URL url;
		try {
			url = new URL(strUrl);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Problem creating URL: " + strUrl, e);
		}
		return url;
	}

	private String streamToString(InputStream is) throws IOException {
		if (is == null)
			return "";

		final StringBuilder sb = new StringBuilder();
		String line;

		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			reader.close();
		} finally {
			is.close();
		}
		return sb.toString();
	}
}