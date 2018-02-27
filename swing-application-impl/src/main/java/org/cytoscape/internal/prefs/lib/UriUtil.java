package org.cytoscape.internal.prefs.lib;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;

public class UriUtil {
	
	public static final String URI_SLASH = "/";
	public static final String FILE_SCHEME			= "file";
	public static final String FILE_SCHEME_PREFIX	= FILE_SCHEME + ":";
	public static final String HTTP_SCHEME		= "http";
	public static final String HTTP_SCHEME_PREFIX		= "http" + "://";
	public static final String HTTPS_SCHEME		= "https";
	public static final String HTTPS_SCHEME_PREFIX		= "https" + "://";
	public static final String FTP_SCHEME		= "ftp";
	public static final String SFTP_SCHEME		= "sftp";
	public static final String LAB_ARCHIVES_SCHEME	= "labarch";
	public static final String FJ_REMOTE_REF_SCHEME	= "fjref";
	public static final String FJ_REMOTE_REF_SCHEME_PREFIX	= FJ_REMOTE_REF_SCHEME + "://";
	public static final String FJ_ENGINE_PATH_COMPONENT	= "/Engine/";
	public static final String NETCDF_REF_SCHEME	= "netref";
	public static final String NOFILE_SCHEME	= "nofile";
    public static final String PATH_SEPARATOR_REPLACEMENT = "_~_";
    public static final String GZIP	= "gzip";

	private static final String[] REMOTE_SCHEMES = {
		HTTP_SCHEME, HTTPS_SCHEME, FTP_SCHEME, SFTP_SCHEME, LAB_ARCHIVES_SCHEME, FJ_REMOTE_REF_SCHEME
	};
	
	public static boolean isFile(URI uri) {
		String scheme = uri.getScheme();
		return scheme == null || scheme.isEmpty() || FILE_SCHEME.equalsIgnoreCase( scheme );
	}
	
	public static boolean isLabArchives(URI uri) {
		return schemeIs(uri, LAB_ARCHIVES_SCHEME);
	}
	
	public static boolean isHttp(URI uri) {
		return schemeIs(uri, HTTP_SCHEME, HTTPS_SCHEME);
	}
	
	public static final boolean isRemote(URI uri) {
		return schemeIs(uri, REMOTE_SCHEMES);
	}
	public static boolean isFJRemote(URI uri)
	{
		if (uri == null) return false;
		if (!uri.isAbsolute()) return false;		//as we have no other uri to compare to, assume local file reference
		return uri.getScheme().startsWith(FJ_REMOTE_REF_SCHEME);
	}
	public static boolean isRemote(String reference)
	{
		for (String scheme : REMOTE_SCHEMES)
			if (reference.startsWith(scheme)) return true;
		return false;
	}
	
	public static boolean schemeIs(URI uri, String...allowedSchemes)
	{
		if (uri == null)
			return false;
		String scheme = uri.getScheme();
		if (scheme != null)
			scheme = scheme.toLowerCase();
		for (String check : allowedSchemes)
			if (StringUtil.areEqual(scheme, check))
				return true;
		return false;
	}

	static public String getOS() 			{		return System.getProperty("os.name").toLowerCase();	}
	static public boolean isWindows() 		{		return getOS().contains("windows");	}

	public static String stripFileRef(String str)
	{
    	if (str.startsWith(UriUtil.FILE_SCHEME + ":"))
    		str = str.substring((UriUtil.FILE_SCHEME + ":").length());
    	if (isWindows() && str.charAt(0) == '/')
    		str = str.substring(1);
    	return str;
	}
	public static URI fromString(String uri)
	{
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String extractServerName(String serverURI)
	{
    	String path = "";
    	try {
			URI uri = new URI(serverURI);
			path = uri.getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return path;
		}
		if (path.startsWith(URI_SLASH))
			path = path.substring(1, path.length());
    	int index = path.indexOf(URI_SLASH);
    	if (index > 0)
    		path = path.substring(0, index);
    	return path;
    }
	
	public static String nameFromUri(URI uri) {
		if (uri == null) return "";
		String s = uri.getSchemeSpecificPart();
		if (StringUtil.isEmpty(s)) return "";
		int p = s.indexOf("?");
		if (p > 0) s = s.substring(0, p);
		p = s.lastIndexOf(URI_SLASH);
		if (p < 0) return s;
		return s.substring(p + 1);
	}
	
	public static String fileExtFromUri(URI uri) {
		return extFromFileName( nameFromUri( uri ) );
	}
	
	public static String extFromFileName(String fn) {
	int indx = fn.lastIndexOf('.');
	if (indx == -1)		return "";
	return fn.substring(indx + 1);
}
	
	public static String hostPortString(URI uri) {
		String host = uri.getHost();
		int port = uri.getPort();
		return hostPortString(host, port);
	}
	
	public static String hostPortString(String host, int port) {
		if (port >= 0)
			return host + ":" + port;
		return host;
	}
//	
//	public static String encodeDecodeURIPath(String path, boolean doEncode)
//	{
//		if (path == null) return null;
//		String end = stripFJRemoteRef(path);
//		int index = path.indexOf(end);
//		try {
//			end = doEncode ? URLEncoder.encode(end, "UTF-8") : URLDecoder.decode(end, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		String start = path.substring(0, index);
//		return start + end;	
//	}
	
//	public static String encode(String str) {
//		try {
//			return URLEncoder.encode(str, "UTF-8");
//		}	catch (UnsupportedEncodingException e) {
//			throw new RuntimeException("Encoding UTF-8 required");
//		}
//	}
//	
//	public static String decode(String str) {
//		try {
//			return URLDecoder.decode(str, "UTF-8");
//		}	catch (UnsupportedEncodingException e) {
//			throw new RuntimeException("Encoding UTF-8 required");
//		}
//	}
//	
//	public static String toDecodedString(URI uri) {
//		if (uri == null) return "";
//		return decode(uri.toString());
//	}

//	public static boolean equal( URI uri1, URI uri2 ) {
//		String 
//			scheme1 = uri1.getScheme(),
//			scheme2 = uri2.getScheme(),
//			schemeSpecificPart1 = uri1.getSchemeSpecificPart(),
//			schemeSpecificPart2 = uri2.getSchemeSpecificPart();
//		
//		scheme1 = (scheme1 == null || scheme1.isEmpty()) ? UriUtil.FILE_SCHEME : scheme1;
//		scheme2 = (scheme2 == null || scheme2.isEmpty()) ? UriUtil.FILE_SCHEME : scheme2;
//		
//		
//		if ( scheme1.equals(UriUtil.FILE_SCHEME) ) {
//			
//			if ( scheme2.equals(UriUtil.FILE_SCHEME)) 
//				return schemeSpecificPart1.equals(schemeSpecificPart2);
//			else
//				return false;
//		
//		} else {
//			return uri1.equals(uri2);
//		}
//	}
	
	/**
	 * Takes a URI parsed from a string, and makes an fixes, such as correcting windows drive
	 * names being set as scheme.
	 * Also fills in a default scheme of 'file' if one is not set.
	 * @param uri
	 * @return
	 */
//	public static URI fix(URI uri) {
//		
//		String scheme		= uri.getScheme(),
//				userInfo	= uri.getUserInfo(),
//				host		= uri.getHost(),
//				path		= uri.getPath(),
//				query		= uri.getQuery(),
//				fragment	= uri.getFragment() ;
//		
//		int	port		= uri.getPort();
//		
//		
//		if (scheme == null) {
//			scheme = FILE_SCHEME;
//		}
//		
//		if (scheme.length() == 1 && path.startsWith("\\")) {		//Parsed from a Windows file path
//			path = scheme + ":" + path;
//			scheme = FILE_SCHEME;
//		}
//		
//			
//		try {
//			return new URI( scheme, userInfo, host, port, path, query, fragment );
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//			throw new RuntimeException("Error fixing URI. Please report this issue. : "+uri);
//		}
//	}
	
	/**
	 * Returns true if the string is a Windows file path, starting with 'driveLetter:\'
	 * 
	 * @param strval
	 * @return
	 */
//	public static boolean isWindowFilePath(String strval) {
//		
//	}
	
	/**
	 * Returns true if the string is a Unix file path, starting with '/'
	 * 
	 * @param strval
	 * @return
	 * @throws URISyntaxException 
	 */
//	public static boolean isUnixFilePath(String strval) {
//		
//	}
	
	/**
	 * This is the preferred way to create a URI from a string.  It tries to handle
	 * any use cases which confuse URI's default parser, such as windows file paths.
	 * @throws MalformedURLException 
	 */
//	public static URI parse(String strval) throws URISyntaxException {
//		String cleanStr = strval.replace('\\', '/');		//Replace any backslashes with forward slashes, to be legal.
//		
////		if (isWindowsFilePath(strval)) {
////			
////		}
//		
//		return fix( new URI(cleanStr) );
//	}
//	
//	public static String checkForAltPathSeparator(String str)
//	{
//		str = UriUtil.stripFJRemoteRef(str);
//		str = decode(str);
//		str = str.replaceAll(PATH_SEPARATOR_REPLACEMENT, Matcher.quoteReplacement(File.separator));
//		return str;
//	}

	public static void main(String[] args) throws URISyntaxException {
		String uriStr1 = "C:\\Documents and Settings\\Jay\\Desktop\\20100916.wsp";
		String uriStr2 = "http://mynotebook.labarchives.com/share/LabArchives API/NDUuNXwyNy8zNS9UcmVlTm9kZS8xMnwxMTUuNQ==?myvar=39&othervar=yes#myurlfragment";
		String uriStr3 = "file://localhost:9302/file/on/my/system.txt";
		String uriStr4 = "file:/file/on/my/system.txt";
		String uriStr5 = "file:///file/on/my/system.txt";
		
		File f1 = new File(uriStr1);
		System.out.println("file to URI: " + f1.toURI());
		
		File f2 = new File(uriStr2);
		System.out.println("file to URI: " + f2.toURI());
		
		File f3 = new File(uriStr3);
		System.out.println("file to URI: " + f3.toURI());
		
		File f4 = new File(uriStr4);
		System.out.println("file to URI: " + f4.toURI());
		
		File f5 = new File(uriStr5);
		System.out.println("file to URI: " + f5.toURI());
		
		
		
		
//		URI uri1 = new URI(uriStr1);
//		System.out.println("URI default parse: " + uri1);
		
//		URI uri2 = new URI(uriStr2);
//		System.out.println("URI default parse: " + uri2);
		
		URI uri3 = new URI(uriStr3);
		System.out.println("URI default parse: " + uri3);
		
		System.out.println("scheme: " + uri3.getScheme());
		System.out.println("host: " + uri3.getHost());
		System.out.println("path: " + uri3.getPath());
		
		URI uri4 = new URI(uriStr4);
		System.out.println("URI default parse: " + uri4);
		
		System.out.println("scheme: " + uri4.getScheme());
		System.out.println("host: " + uri4.getHost());
		System.out.println("path: " + uri4.getPath());
		
		URI uri5 = new URI(uriStr5);
		System.out.println("URI default parse: " + uri5);
		
		System.out.println("scheme: " + uri5.getScheme());
		System.out.println("host: " + uri5.getHost());
		System.out.println("path: " + uri5.getPath());
		
		URI uri = new URI("http://somewhere.that/a/path/tothe/file-I/want");
		System.out.println("Resolved = " + uri.resolve("myfile"));
		
//		URI newUri = parse(uriStr1);
		
		
	}
	public static int sizeRecursive(Collection<URI> inURIs, boolean includeHidden) {
		int size = 0;
		for(URI uri : inURIs){
			if (UriUtil.isRemote(uri))
			{
				size++;
				continue;
			}
			File file = new File(uri);
			if(!includeHidden && file.isHidden())
				continue;
			if(file.isDirectory()){
				File[] files = file.listFiles();
				for (int i = 0; i < files.length; i++){
					//Recursive call
					size += sizeRecursive(Arrays.asList(files[i].toURI()), includeHidden);
				}
			}
			//Base case
			else if(file.isFile()){
				size ++;
			}
		}
		return size;
	} 
	
//	/** prepend host, for uri's without hosts, the first element will be missing from the path */
//	public static String getPath(URI uri, boolean prependHost)
//	{
//		String path = uri.getPath();
//		if (prependHost && uri.getHost() != null)
//			path = FileUtil.concat(uri.getHost(), path);
//		return path;
//	}
	
	public static String getDirName(URI uri)
	{
		if (uri == null) return "";
		String path = uri.getPath();
		if (path == null) return "";
		return path.substring(0,path.lastIndexOf('/')+1);
	}
//	public static String getBaseName(URI uri)
//	{
//		if (uri == null) return "";
//		String path = uri.toString();
//		path = UriUtil.encodeDecodeURIPath(path, false);
//		return path.substring(path.lastIndexOf(URI_SLASH)+1, path.length());
//	}
//	
//	public static URI resolve(URI uri, String fileName)
//	{
//		if (uri == null || fileName == null) return null;
////		if (fileName.equals(UriUtil.getBaseName( uri ) ))
////            return uri;
//        return uri.resolve(encodeDecodeURIPath(fileName, true));			//replaces the basename of uri with fileName
//	}
	
//	/** Attempts to append uri2 to uri1, keeping everything up through the directory of the path
//	 *  from uri1, and appending the path of uri2 along with its query and fragment. */
//	public static URI concat(URI uri1, URI uri2)
//	{
//		if (uri1 == null) return uri2;
//		if (uri2 == null) return uri1;
//		
//		try {
//			return new URI(uri1.getScheme(), uri1.getUserInfo(), uri1.getHost(),
//					uri1.getPort(), StringUtil.ensureSeparator(URI_SLASH, StringUtil.rtrim(getDirName(uri1), URI_SLASH), uri2.getPath()),
//					uri2.getQuery(), uri2.getFragment());
//		} catch (URISyntaxException e) {
//			Debug.p("Error joining URIs: " + uri1 + "  :::  " + uri2, e);
//		}
//		return null;
//	}
	
//	public static File toFile(URI uri)
//	{
//		return toFile(uri, null);
//	}
//	
//	public static File toFile(URI uri, File relativeRoot)
//	{
//		if (uri == null) return null;
//		
//		String scheme = uri.getScheme();
//		if (scheme == null || scheme.isEmpty() || scheme.equalsIgnoreCase(FILE_SCHEME))
//		{
//			String path = uri.getSchemeSpecificPart();
//			return new File(relativeRoot, path);		//File(URI) constructor has many issues...
//		}
//		
////		if (!uri.isAbsolute()) return new File(encodeDecodeURIPath(uri.getPath(), false));		//if scheme is null, just passing the URI to the File constructor will throw a URI-not-absolute-exception.
////		try
////		{
////			return new File(uri);
////		}
////		catch (IllegalArgumentException ex) {
//		Debug.p("Cannot get file from URI: " + uri);
////		}
//		return null;
//	}
//	
	public static boolean isSuccessResponse(int code)
	{
		return code < 400;			//NOTE AM 2/29/12 should possibly be 'code < 300'
	}
	
//	public static HttpURLConnection getConnection(URL url) throws IOException
//	{
//    	return getConnection(url, false);
//	}
//	
//	public static HttpURLConnection getConnection(URL url, boolean enableGzip) throws IOException
//	{
//		SApplication app = ApplicationInfo.getApplication();
//		ProxyConfig proxy = app == null ? null : app.getPrefs().getProxy();
//    	return getConnection(url, proxy, enableGzip);
//	}

	public static HttpURLConnection getConnection(URL url, ProxyConfig proxyConfig, boolean enableGzip) throws IOException
	{
		URLConnection urlConn;
    	if (proxyConfig != null && proxyConfig.isValid())
    	{
    		
    		InetSocketAddress sa = new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPortInt());
    		Proxy prox = new Proxy(Proxy.Type.HTTP, sa);
    		urlConn = url.openConnection(prox);
            if (proxyConfig.isAuthed())
            {
                urlConn.setRequestProperty("Proxy-Authorization", "Basic " + ParseUtil.base64Encode(proxyConfig.getUsername() + ":" + proxyConfig.getPassword()));
            }
            
            urlConn.setReadTimeout(10000);
    	}
    	else
    		urlConn = url.openConnection();
    	if (!(urlConn instanceof HttpURLConnection))
    		throw new IOException("Non-HTTP URL connection made");
//    	SApplication app = ApplicationInfo.getApplication();
//    	if (app != null && !StringUtil.isEmpty(app.getWebUserAgent()))
//    		urlConn.setRequestProperty("User-Agent", app.getWebUserAgent());
    	if (enableGzip)
    		urlConn.setRequestProperty("Accept-Encoding", GZIP);
    	return (HttpURLConnection) urlConn;
	}
	
//	public static String urlEncodeMap(Map<String, String> map) {
//		String str = "";
//		int index = 0;
//		
//		for (String key : map.keySet()) {
//			String value = map.get(key);
//			
//			if (index++ > 0)
//				str += "&";
//			
//			str += key+"="+encodeDecodeURIPath(value, true);
//		}
//
//		return str;
//	}
//	
//	
//	public static boolean isResponseGzipped(URLConnection cn)
//	{
//		return GZIP.equals(cn.getHeaderField("Content-Encoding"));
//	}

}
