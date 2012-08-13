package org.cytoscape.app.internal.net.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.SocketTimeoutException;

import org.apache.http.protocol.HttpService;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;

import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultHttpServerConnection;

import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.ConnectionClosedException;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.RequestLine;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.cytoscape.app.internal.util.DebugHelper;

/**
 * Creates a server socket and serves http connections from localhost clients only.
 */
public class LocalHttpServer implements Runnable {
	
	// The list of addresses allowed to query the server
	public static final String[] ALLOWED_ORIGINS = new String[]{
		"http://apps3.nrnb.org", 
		"http://apps.cytoscape.org"
	};
	
    public static class Response {
		final String body;
		final String contentType;
	
		public Response(final String body, final String contentType) {
		    if (body == null) {
		    	throw new IllegalArgumentException("body == null");
		    }
		    
		    if (contentType == null) {
		    	throw new IllegalArgumentException("contentType == null");
		    }
		    
		    this.body = body;
		    this.contentType = contentType;
		}
	
		public String getBody() {
		    return body;
		}
	
		public String getContentType() {
		    return contentType;
		}
    }

    public interface GetResponder {
		public boolean canRespondTo(String url) throws Exception;
		public Response respond(String url) throws Exception;
    }

    public interface PostResponder {
		public boolean canRespondTo(String url) throws Exception;
		public Response respond(String url, String body) throws Exception;
    }

    private static final Logger logger = LoggerFactory.getLogger(LocalHttpServer.class);

    final List<GetResponder> getResponders = new ArrayList<GetResponder>(); 
    final List<PostResponder> postResponders = new ArrayList<PostResponder>(); 

    final ServerSocketFactory serverSocketFactory;
    final Executor connectionHandlerExecutor;

    final HttpParams params;
    final HttpService service;

    /**
     * Initializes local http server but does not start serving incoming connections.
     * @param port The port on which the http server should receive connections--should be greater
     * than 1024 if the server is being executed by a non-root user.
     * @param connectionHandlerExecutor executes a connection handler when an incoming socket connection is received
     */
    public LocalHttpServer(final ServerSocketFactory serverSocketFactory, final Executor connectionHandlerExecutor) {
        if (serverSocketFactory == null) {
            throw new IllegalArgumentException("serverSocketFactory == null");
        }

        this.serverSocketFactory = serverSocketFactory;

		if (connectionHandlerExecutor == null) {
		    throw new IllegalArgumentException("connectionHandlerExecutor == null");
		}
		
		this.connectionHandlerExecutor = connectionHandlerExecutor;
	
		// Setup params
	
		params = (new SyncBasicHttpParams())
		    .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
		    .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
		    .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
		    .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
		    .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");
	
		// Setup service
	
		final HttpProcessor proc = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
			new ResponseDate(),
			new ResponseServer(),
			new ResponseContent(),
			new ResponseConnControl()
	    });
	            
		final HttpRequestHandlerRegistry registry = new HttpRequestHandlerRegistry();
		registry.register("*", new RequestHandlerDispatcher());
	            
		service = new HttpService(proc, 
					  new DefaultConnectionReuseStrategy(), 
					  new DefaultHttpResponseFactory(),
					  registry,
					  params);
    }

    public void addGetResponder(final GetResponder getResponder) {
    	getResponders.add(getResponder);
    }

    public void addPostResponder(final PostResponder postResponder) {
    	postResponders.add(postResponder);
    }

    public void run() {
		// Create a server socket
		ServerSocket serverSocket = null;
		try {
		    serverSocket = serverSocketFactory.createServerSocket();
		} catch (IOException e) {
		    logger.error("Failed to create server socket", e);
		    return;
		}
	
		logger.info("Server socket started on {}", String.format("%s:%d", serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort()));
		
		// Keep servicing incoming connections until this thread is flagged as interrupted
		while (!Thread.interrupted()) { // TODO: **interrupted is deprecated?
			
		    // Create a new http server connection from the incoming socket
		    DefaultHttpServerConnection connection = null;
		    try {
				final Socket socket = serverSocket.accept();
				logger.info("Server socket received connection from {}", socket.getInetAddress().getHostAddress());
				//System.out.println("Server socket received connection from {}" + socket.getInetAddress().getHostAddress());
				connection = new DefaultHttpServerConnection();
				connection.bind(socket, params);
		    } catch (IOException e) {
				logger.error("Failed to initiate connection with client", e);
				continue;
		    }
	
		    // Dispatch an incoming connection to ConnectionHandler
		    final ConnectionHandler connectionHandler = new ConnectionHandler(service, connection);
		    connectionHandlerExecutor.execute(connectionHandler);
		}
	
		logger.info("Server socket stopped");
    }

    /**
     * Handles an incoming http connection when it is received by <code>ConnectionsReceiver</code>.
     */
    class ConnectionHandler implements Runnable {
        final HttpService service;
        final HttpServerConnection connection;
        
        public ConnectionHandler(final HttpService service, final HttpServerConnection connection) {
		    this.service = service;
		    this.connection = connection;
        }
	
		public void run() {
            final HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && connection.isOpen()) {
                    service.handleRequest(connection, context);
                }
		    } catch (SocketTimeoutException e) {
			// ignore, this happens normally
		    } catch (ConnectionClosedException e) {
			// ignore, this happens normally
	            } catch (IOException e) {
	                logger.warn("Failed to complete communication with client; connection closing", e);
	            } catch (HttpException e) {
	                logger.warn("Client violated http; connection closing", e);
	            } finally {
	                try {
	                	connection.shutdown();
	                } catch (IOException e) {
	                	
	                }
	            }
		}
    }

    class RequestHandlerDispatcher implements HttpRequestHandler {
		public void handle(final HttpRequest request, final HttpResponse httpResponse, final HttpContext context) throws HttpException {
	
		    // decode the uri
	
            final String uri = request.getRequestLine().getUri();
		    String url = null;
		    try {
		    	url = URLDecoder.decode(uri, "UTF-8");
		    } catch (UnsupportedEncodingException e) {
		    	throw new HttpException("unable to parse uri: " + uri, e);
		    }
	
		    // determine http method
	
            final String method = request.getRequestLine().getMethod().toLowerCase();
            DebugHelper.print("Request received. Method: " + method);

            // System.out.println("Request recvd: " + requestLine.toString());

            // Obtain origin
            Header originHeader = request.getFirstHeader("Origin");
            
            String originValue = null;
            if (originHeader != null) {
            	originValue = originHeader.getValue();
            }
            
            boolean originFound = false;

            for (int i = 0; i < ALLOWED_ORIGINS.length; i++) {
            	String allowedOrigin = ALLOWED_ORIGINS[i];
            	
            	if (originValue != null && allowedOrigin.equals(originValue)) {
            		originFound = true;
            	}
            }
            
            // System.out.println("Origin found? " + originFound);
                        
            if (!originFound) {
            	if (originValue != null) {
            		setHttpResponseToError(httpResponse, HttpStatus.SC_FORBIDDEN, "Access denied", 
            			"The app manager does not recognize the orgin " + originValue);
            	} else {
            		setHttpResponseToError(httpResponse, HttpStatus.SC_FORBIDDEN, "Access denied", 
                			"Origin not found.");
            	}
            	return;
            }
            
		    if (method.equals("options")) {
		    	// If the origin is on the allowed list, echo it back as the allowed origin
			    httpResponse.addHeader("Access-Control-Allow-Origin", originValue);
			    httpResponse.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
			    httpResponse.addHeader("Access-Control-Max-Age", "1");
			    httpResponse.addHeader("Access-Control-Allow-Headers", "origin, x-csrftoken, accept");
			    return;
		    }
	        
		    // loop thru responders and see if any of them produce a response
		    Response response = null;
		    if (method.equals("get") || method.equals("head")) {
		    	
		    	DebugHelper.print("Number of GET responders: " + getResponders.size());
				for (final GetResponder getResponder : getResponders) {
		
				    // catch any exceptions emitted by the responder so our server thread doesn't prematurely terminate
				    // and can send a coherent message back to the user
				    try {
						if (getResponder.canRespondTo(url)) {
							DebugHelper.print("Responder able to respond. Responding..");
						    response = getResponder.respond(url);
						    break;
						}
				    } catch (Exception e) {
						setHttpResponseToInternalError(httpResponse, e);		
						return;
				    }
				}
		    } else if (method.equals("post")) {
		
				// obtain the body of the post request
		
				String postBody = null;
				if (request instanceof HttpEntityEnclosingRequest) {
				    final HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
				    try {
				    	postBody = EntityUtils.toString(entity);
				    } catch (IOException e) {
				    	
				    }
				}
		
				DebugHelper.print("Number of POST responders: " + postResponders.size());
				for (final PostResponder postResponder : postResponders) {
		
				    // catch any exceptions emitted by the responder so our server thread doesn't prematurely terminate
				    // and can send a coherent message back to the user
		
				    try {
						if (postResponder.canRespondTo(uri)) {
							DebugHelper.print("Found responder. Responding..");
						    response = postResponder.respond(url, postBody);
						    break;
						}
				    } catch (Exception e) {
				    	DebugHelper.print("Found exception: " + e);
						setHttpResponseToInternalError(httpResponse, e);		
						return;
				    }
				}
		    } else {
		    	// none of the http methods are valid, so issue an error to the client
	            throw new MethodNotSupportedException(String.format("\"%s\" method not supported", method)); 
		    }
		    
		    if (response == null) {
		    	setHttpResponseToError(httpResponse, HttpStatus.SC_NOT_FOUND, "Resource not found", null);
		    } else {
		    	final String body = response.getBody();
				if (body == null) {
				    setHttpResponseToInternalError(httpResponse, "Responder \"%s\" returned null");
				} else {
				    setHttpResponse(httpResponse, HttpStatus.SC_OK, body, response.getContentType());
				    httpResponse.addHeader("Access-Control-Allow-Origin", originValue != null? originValue : "*");
				}
		    }
		}
    }

    private static void setHttpResponse(final HttpResponse httpResponse, final int code, final String msg, final String contentType) throws HttpException {
		httpResponse.setStatusCode(code);
		try {
		    httpResponse.setEntity(new StringEntity(msg, contentType, "UTF-8"));
		    
		} catch (UnsupportedEncodingException e) {
		    throw new HttpException("failed to issue output to client", e);
		}
    }

    private static void setHttpResponseToError(final HttpResponse httpResponse, final int code, final String title, final String msg) throws HttpException {
    	setHttpResponse(httpResponse, code, (msg == null ?
					     String.format("<html><body><h1>%s</h1></body></html>", title) :
					     String.format("<html><body><h1>%s</h1><pre>%s</pre></body></html>", title, msg)), "text/html");
    }

    private void setHttpResponseToInternalError(final HttpResponse httpResponse, final String msg) throws HttpException {
    	logger.warn("Failed to respond to client: {}", msg);
    	setHttpResponseToError(httpResponse, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Error", msg);
    }

    private void setHttpResponseToInternalError(final HttpResponse httpResponse, final Throwable throwable) throws HttpException {
		logger.warn("Failed to respond to client", throwable);
		final StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		final String stacktrace = stringWriter.toString();
		setHttpResponseToError(httpResponse, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Error", stacktrace);
    }
}
