package org.cytoscape.cpath2.internal.web_service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.SocketException;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.cytoscape.work.TaskMonitor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility Class for Connecting to the cPath Web Service API.
 *
 * @author Ethan Cerami
 */
public class CPathProtocol {

    /**
     * The CPath Web Service Path.
     */
    public static final String WEB_SERVICE_PATH = "webservice.do";

    /**
     * Command Argument.
     */
    public static final String ARG_COMMAND = "cmd";

    /**
     * Query Argument.
     */
    public static final String ARG_QUERY = "q";

    /**
     * Format Argument.
     */
    public static final String ARG_FORMAT = "output";

    /**
     * Organism Argument.
     */
    public static final String ARG_ORGANISM = "organism";

    /**
     * Max Hits Argument.
     */
    public static final String ARG_MAX_HITS = "maxHits";

    /**
     * Start Index Argument.
     */
    public static final String ARG_START_INDEX = "startIndex";

    /**
     * Version Argument.
     */
    public static final String ARG_VERSION = "version";

    /**
     * Count Only Format.
     */
    public static final String FORMAT_COUNT_ONLY = "count_only";

    /**
     * Currently Supported Version.
     */
    public static final String CURRENT_VERSION = "3.0";

    /**
     * Get Records By Keyword.
     */
    public static final String COMMAND_SEARCH ="search";

    /**
     * Gets Parent Summaries.
     */
    public static final String COMMAND_GET_PARENTS = "get_parents";

    /**
     * Get Interactions By Interactor Name / Xref.
     */
    public static final String COMMAND_GET_BY_INTERACTOR_XREF =
            "get_by_interactor_name_xref";

    /**
     * Get Interactions By Organism.
     */
    public static final String COMMAND_GET_BY_ORGANISM =
            "get_by_organism";

    /**
     * Get Interactions By Experiment Type.
     */
    public static final String COMMAND_GET_BY_EXPERIMENT_TYPE =
            "get_by_experiment_type";

    /**
     * Get Interactions by PMID.
     */
    public static final String COMMAND_GET_BY_PMID =
            "get_by_pmid";

    /**
     * Get Interactions By Database.
     */
    public static final String COMMAND_GET_BY_DATABASE =
            "get_by_database";

    /**
     * Get Top Level Pathway List.
     */
    public static final String COMMAND_GET_TOP_LEVEL_PATHWAY_LIST =
            "get_top_level_pathway_list";

    /**
     * Get Patheway record by CPath ID.
     */
    public static final String COMMAND_GET_RECORD_BY_CPATH_ID =
            "get_record_by_cpath_id";

    /**
     * Default for Max Hits.
     */
    public static final int DEFAULT_MAX_HITS = 10;

    /**
     * Not Specified
     */
    public static final int NOT_SPECIFIED = -1;

    /**
     * XML Tag.
     */
    private static final String XML_TAG = "xml";

    /*** Default initial size of the response buffer if content length is unknown. */
    private static final int DEFAULT_INITIAL_BUFFER_SIZE = 4*1024; // 4 kB

    private String command;
    private String query;
    private int taxonomyId;
    private int maxHits;
    private int startIndex;
    private String format;
    private String baseUrl;
    private volatile HttpMethodBase method;
    private boolean cancelledByUser = false;
    private static boolean debug = false;
		private Logger logger = LoggerFactory.getLogger(CPathProtocol.class);

    /**
     * Constructor.
     */
    public CPathProtocol() {
        this.baseUrl = CPathProperties.getInstance().getCPathUrl();
        this.maxHits = DEFAULT_MAX_HITS;
        this.taxonomyId = NOT_SPECIFIED;
    }

    /**
     * Sets the Command Argument.
     *
     * @param command Command Argument.
     */
    public void setCommand (String command) {
        this.command = command;
    }

    /**
     * Sets the Query Argument.
     *
     * @param query Query Argument.
     */
    public void setQuery (String query) {
        this.query = query;
    }

    /**
     * Sets the Format Argument.
     *
     * @param format Format Argument.
     */
    public void setFormat (CPathResponseFormat format) {
        this.format = format.getFormatString();
    }

    /**
     * Sets the Organism Argument.
     *
     * @param taxonomyId NCBI TaxonomyID
     */
    public void setOrganism (int taxonomyId) {
        this.taxonomyId = taxonomyId;
    }

    /**
     * Sets the MaxHits Argument.
     *
     * @param maxHits Max Number of Hits.
     */
    public void setMaxHits (int maxHits) {
        this.maxHits = maxHits;
    }

    /**
     * Sets the StartIndex Argument.
     *
     * @param startIndex StartIndex Argument.
     */
    public void setStartIndex (int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * Abort the Request.
     */
    public void abort() {
        if (method != null) {
            cancelledByUser = true;
            method.abort();
        }
    }

    /**
     * Connects to cPath Web Service API.
     *
     * @return XML Document.
     * @throws CPathException    Indicates Error connecting.
     * @throws EmptySetException All went all, but no results found.
     */
    public String connect (TaskMonitor taskMonitor) throws CPathException, EmptySetException {
        try {
            NameValuePair[] nvps;

            // Create an instance of HttpClient.
            HttpClient client = new HttpClient();
            setProxyInfo(client);

            // Create a method instance.
            // If the query string is long, use POST.  Otherwise, use GET.
            if (query != null && query.length() > 100) {
                nvps =  createNameValuePairs(true);
                method = new PostMethod(baseUrl);
                ((PostMethod)(method)).addParameters(nvps);
                logger.info("Connect:  " + method.getURI() + " (via POST)");
            } else {
                nvps = createNameValuePairs(false);
                String liveUrl = createURI(baseUrl, nvps);
                method = new GetMethod(liveUrl);
                logger.info("Connect:  " + liveUrl);
            }

            int statusCode = client.executeMethod(method);

            //  Check status code
            checkHttpStatusCode(statusCode);

            //  Read in Content
            InputStream instream = method.getResponseBodyAsStream();
            long contentLength = method.getResponseContentLength();
            if (contentLength > 0) {
                if (taskMonitor != null) {
                    taskMonitor.setProgress(0);
                }
            }

            ByteArrayOutputStream outstream = new ByteArrayOutputStream(
                    contentLength > 0 ? (int) contentLength : DEFAULT_INITIAL_BUFFER_SIZE);
            byte[] buffer = new byte[4096];
            int len;
            int totalBytes = 0;
            while ((len = instream.read(buffer)) > 0) {
                outstream.write(buffer, 0, len);
                totalBytes = updatePercentComplete(contentLength, len, totalBytes, taskMonitor);
            }
            instream.close();

            String content = new String(outstream.toByteArray());
            if (content.toLowerCase().indexOf(XML_TAG) >= 0) {
                //  Check for protocol errors.
                if (content.indexOf("<error>") >=0) {
                    StringReader reader = new StringReader(content);
                    SAXBuilder builder = new SAXBuilder();
                    Document document = builder.build(reader);
                    checkForErrors(document);
                }
                return content.trim();
            } else {
                return content.trim();
            }
        } catch (UnknownHostException e) {
            throw new CPathException(CPathException.ERROR_UNKNOWN_HOST, e);
        } catch (SocketException e) {
            if (cancelledByUser) {
                throw new CPathException(CPathException.ERROR_CANCELED_BY_USER, e);
            } else {
                throw new CPathException(CPathException.ERROR_NETWORK_IO, e);
            }
        } catch (IOException e) {
            throw new CPathException(CPathException.ERROR_NETWORK_IO, e);
        } catch (JDOMException e) {
            throw new CPathException(CPathException.ERROR_XML_PARSING, e);
        }
    }

    /**
     * Sets Proxy Information (if set).
     */
    private void setProxyInfo(HttpClient client) {
    	// TODO: Port this.
//        Proxy proxyServer = ProxyHandler.getProxyServer();
    	Proxy proxyServer = null;

        //  The java.net.Proxy object does not provide getters for host and port.
        //  So, we have to hack it by using the toString() method.

        //  Note to self for future reference: I was able to test all this code
        //  by downloading and installing Privoxy, a local HTTP proxy,
        //  available at:  http://www.privoxy.org/.  Once it was running, I used the
        //  following props in ~/.cytoscape/cytoscape.props:
        //  proxy.server=127.0.0.1
        //  proxy.server.port=8118
        //  proxy.server.type=HTTP
        if (proxyServer != null) {
            String proxyAddress = proxyServer.toString();
            if (debug) logger.debug("full proxy string:  " + proxyAddress);
            String[] addressComponents = proxyAddress.split("@");
            if (addressComponents.length == 2) {
                String parts[] = addressComponents[1].split(":");
                if (parts.length == 2) {
                    String hostString = parts[0].trim();
                    String hostParts[] = hostString.split("/");
                    if (hostParts.length > 0) {
                        String host = hostParts[0].trim();
                        String port = parts[1].trim();
                        if (debug) logger.debug("proxy host: " + host);
                        if (debug) logger.debug("proxy port:  " + port);
                        client.getHostConfiguration().setProxy(host, Integer.parseInt(port));
                    }
                }
            }
        }
    }

    private int updatePercentComplete(long contentLength, int len, int totalBytes,
            TaskMonitor taskMonitor) {
        if (contentLength > 0) {
            totalBytes += len;
            double percentComplete = (int) ((totalBytes / (double) contentLength));
            if (taskMonitor != null) {
                taskMonitor.setProgress(percentComplete);
            }
            if (debug) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return totalBytes;
    }

    /**
     * Gets URI
     *
     * @return URI.
     */
    public String getURI () {
        NameValuePair[] nvps = createNameValuePairs(false);
        return createURI(baseUrl, nvps);
    }

    /**
     * Gets URI of cPath Call.
     *
     * @return URI for cPath Call.
     */
    private String createURI (String url, NameValuePair[] nvps) {
        StringBuffer buf = new StringBuffer(url);
        buf.append("?");
        for (int i = 0; i < nvps.length; i++) {
            buf.append(nvps[i].getName() + "=" + nvps[i].getValue() + "&");
        }
        return buf.toString();
    }

    private NameValuePair[] createNameValuePairs (boolean post) {
        NameValuePair nvps[] = null;
        if (taxonomyId == NOT_SPECIFIED) {
            nvps = new NameValuePair[6];
        } else {
            nvps = new NameValuePair[7];
            nvps[6] = new NameValuePair(ARG_ORGANISM,
                    Integer.toString(taxonomyId));
        }
        nvps[0] = new NameValuePair(ARG_COMMAND, command);
        try {
            if (!post) {
                nvps[1] = new NameValuePair(ARG_QUERY, URLEncoder.encode(query, "UTF-8"));
            } else {
                nvps[1] = new NameValuePair(ARG_QUERY, query); 
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        nvps[2] = new NameValuePair(ARG_FORMAT, format);
        nvps[3] = new NameValuePair(ARG_VERSION, CURRENT_VERSION);
        nvps[4] = new NameValuePair(ARG_MAX_HITS, Integer.toString(maxHits));
        nvps[5] = new NameValuePair(ARG_START_INDEX,
                Integer.toString(startIndex));
        return nvps;
    }

    private void checkHttpStatusCode (int statusCode)
            throws CPathException {
        if (statusCode != 200) {
            throw new CPathException(CPathException.ERROR_HTTP, "HTTP Status Code:  " + statusCode
                + ":  " + HttpStatus.getStatusText(statusCode) + ".");
        }
    }

    private void checkForErrors (Document document)
            throws CPathException, EmptySetException {
        Element element = document.getRootElement();
        String name = element.getName();
        if (name.equals("error")) {
            String errorCode = element.getChild("error_code").getText();
            String errorMsg = element.getChild("error_msg").getText();
            if (errorCode.equals("460")) {
                throw new EmptySetException();
            } else {
                throw new CPathException(CPathException.ERROR_WEB_SERVICE_API,
                    "Error Code:  " + errorCode + ", " + errorMsg + ".");
            }
        }
    }
}
