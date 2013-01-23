package org.cytoscape.app.internal.net.server;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;
import java.util.regex.Matcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import org.apache.http.HttpException;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.HttpStatus;

public class CyHttpdFactoryImpl implements CyHttpdFactory
{
    public CyHttpd createHttpd(final ServerSocketFactory serverSocketFactory)
    {
        return new CyHttpdImpl(serverSocketFactory);
    }
}

class CyHttpdImpl implements CyHttpd
{
    static final Logger logger = LoggerFactory.getLogger(CyHttpdImpl.class);

    final ServerSocketFactory serverSocketFactory;
    final List<CyHttpResponder> responders = new ArrayList<CyHttpResponder>();
    final List<CyHttpBeforeResponse> beforeResponses = new ArrayList<CyHttpBeforeResponse>();
    final List<CyHttpAfterResponse> afterResponses = new ArrayList<CyHttpAfterResponse>();

    boolean running = false;
    ExecutorService executor = null;

    final HttpParams params;
    final HttpService service;

    public CyHttpdImpl(final ServerSocketFactory serverSocketFactory)
    {
        if (serverSocketFactory == null)
            throw new IllegalArgumentException("serverSocketFactory == null");
        this.serverSocketFactory = serverSocketFactory;


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

    public void start()
    {
        synchronized (this) {
	        if (running)
	            throw new IllegalStateException("server is running");
	        executor = Executors.newCachedThreadPool();
	        executor.execute(new ServerThread());
	        while (!running) {
	        	try {
	        		wait(500);
	        	} catch (InterruptedException e) {
	        		throw new RuntimeException(e);
	        	}
	        }
        }
    }

    public void stop()
    {
        if (!running)
            throw new IllegalStateException("server is not running");
        running = false;
        executor.shutdown();
        executor = null;
    }

    public boolean isRunning()
    {
        return running;
    }

    public void addResponder(CyHttpResponder responder)
    {
        responders.add(responder);
    }

    public void removeResponder(CyHttpResponder responder)
    {
        responders.remove(responder);
    }

    public Collection<CyHttpResponder> getResponders()
    {
        return responders;
    }

    public void addBeforeResponse(CyHttpBeforeResponse beforeResponse)
    {
        beforeResponses.add(beforeResponse);
    }

    public void removeBeforeResponse(CyHttpBeforeResponse beforeResponse)
    {
        beforeResponses.remove(beforeResponse);
    }

    public Collection<CyHttpBeforeResponse> getBeforeResponses()
    {
        return beforeResponses;
    }

    public void addAfterResponse(CyHttpAfterResponse afterResponse)
    {
        afterResponses.add(afterResponse);
    }

    public void removeAfterResponse(CyHttpAfterResponse afterResponse)
    {
        afterResponses.remove(afterResponse);
    }

    public Collection<CyHttpAfterResponse> getAfterResponses()
    {
        return afterResponses;
    }

    class ServerThread implements Runnable
    {
        public void run()
        {
            // Create a server socket
            ServerSocket serverSocket = null;
            try
            {
                serverSocket = serverSocketFactory.createServerSocket();
            }
            catch (IOException e)
            {
                logger.error("Failed to create server socket", e);
                return;
            }
        
            logger.info("Server socket started on {}", String.format("%s:%d", serverSocket.getInetAddress().getHostAddress(), serverSocket.getLocalPort()));
            
            running = true;
        	synchronized (CyHttpdImpl.this) {
        		CyHttpdImpl.this.notifyAll();
        	}

        	// Keep servicing incoming connections until we're told to stop
            while (running)
            {
                
                // Create a new http server connection from the incoming socket
                DefaultHttpServerConnection connection = null;
                try {
                    final Socket socket = serverSocket.accept();
                    logger.info("Server socket received connection from {}", socket.getInetAddress().getHostAddress());
                    connection = new DefaultHttpServerConnection();
                    connection.bind(socket, params);
                } catch (IOException e) {
                    logger.error("Failed to initiate connection with client", e);
                    continue;
                }
        
                // Dispatch an incoming connection to ConnectionHandler
                final ConnectionHandler connectionHandler = new ConnectionHandler(service, connection);
                executor.execute(connectionHandler);
            }
        
            logger.info("Server socket stopped");
        }
    }

    class ConnectionHandler implements Runnable
    {
        final HttpService service;
        final HttpServerConnection connection;
        
        public ConnectionHandler(final HttpService service, final HttpServerConnection connection)
        {
		    this.service = service;
		    this.connection = connection;
        }
	
        public void run()
        {
            final HttpContext context = new BasicHttpContext(null);
            try
            {
                while (running && connection.isOpen())
                    service.handleRequest(connection, context);
            } catch (SocketTimeoutException e) {
                // ignore, this happens normally
            } catch (ConnectionClosedException e) {
                // ignore, this happens normally
            } catch (IOException e) {
                logger.warn("Failed to complete communication with client; connection closing", e);
            } catch (HttpException e) {
                logger.warn("Client violated http; connection closing", e);
            } finally {
                try
                {
                    connection.shutdown();
                }
                catch (IOException e)
                {
                    // We don't care if an exception happened during shutdown
                }
            }
        }
    }

    class RequestHandlerDispatcher implements HttpRequestHandler
    {
		public void handle(final HttpRequest httpRequest, final HttpResponse httpResponse, final HttpContext httpContext) throws HttpException
        {
            for (org.apache.http.Header header : httpRequest.getAllHeaders())
                logger.info("{}: {}", header.getName(), header.getValue());
            final CyHttpRequest request = new CyHttpRequestImpl(httpRequest);
            final CyHttpResponse response = handle(request);
            if (response == null)
            {
                setHttpResponse(HttpStatus.SC_NOT_FOUND, "<html><body><h1>404 Not Found</h1></body></html>", "text/html", httpResponse);
            }
            else
            {
                setHttpResponse(response, httpResponse);
            }
        }

        private CyHttpResponse handle(final CyHttpRequest request)
        {
            CyHttpResponse response = null;

            for (final CyHttpBeforeResponse beforeResponse : beforeResponses)
            {
                response = beforeResponse.intercept(request);
                if (response != null)
                    return response;
            }

            final String uri = request.getURI();
            logger.info("Received request: {}", uri);
            for (final CyHttpResponder responder : responders)
            {
                final Matcher matcher = responder.getURIPattern().matcher(uri);
                if (matcher.matches())
                {
                    response = responder.respond(request, matcher);
                    break;
                }
            }
            if (response == null)
                return null;

            for (final CyHttpAfterResponse afterResponse : afterResponses)
                response = afterResponse.intercept(request, response);

            return response;
        }
    }

    static final void setHttpResponse(final CyHttpResponse response, final HttpResponse httpResponse)
    {
        setHttpResponse(response.getStatusCode(), response.getContent(), response.getContentType(), httpResponse);
        for (final Map.Entry<String,String> entry : response.getHeaders().entrySet())
            httpResponse.addHeader(entry.getKey(), entry.getValue());
    }

    static final void setHttpResponse(final int statusCode, final String content, final String contentType, final HttpResponse httpResponse)
    {
		httpResponse.setStatusCode(statusCode);
        if (content != null && contentType != null)
            httpResponse.setEntity(new StringEntity(content, ContentType.create(contentType, "UTF-8")));
    }
}
