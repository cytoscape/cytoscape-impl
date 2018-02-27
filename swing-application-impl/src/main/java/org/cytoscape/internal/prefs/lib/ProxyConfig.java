package org.cytoscape.internal.prefs.lib;

public class ProxyConfig {
	private String host, username, password, pac;
	private String port;
	
	public ProxyConfig(String host, String port)
	{
		this(host, port, null, null, null);
	}
	public ProxyConfig(String host)
	{
		String[] tokens = host.split("|");
		if (tokens.length > 1)
		{
			host = tokens[0];
			port = tokens[1];
		}
		if (tokens.length > 4)
		{
			username = tokens[2]; 
			password = tokens[3];
			pac = tokens[4];
		}
	}
	
	public String toString()		{ return host + "|" + port + "|" + username + "|" + password + "|" + pac; }
	public ProxyConfig(String h, String p, String user, String pw, String pacUrl)
	{
		this.host = h; 
		this.port = p; 
		this.username = user; 
		this.password = pw;
		pac = pacUrl;
	}
	
//	public ProxyConfig(SElement elem)
//	{
//    	if (elem == null) return;
//    	    	host		= elem.getString("host");
//    	port		= elem.getString("port");
//    	username	= elem.getString("username");
//    	password	= elem.getString("password");
//    	pac			= elem.getString("pac");

//    	if (!StringUtil.isEmpty(password))
//    		password = EngineManager.decodeText(password);
//		host = elem.getString("Host"); 
//		port = port; 
//		username = username; 
//		password = password;
//	}
//	
//	public SElement getElement(){
//		
//		SElement e = new SElement("ProxyConfiguration");
//		e.setString("host", host);
//		e.setString("port", port);
//		e.setString("username", username);
//		e.setString("password", password);
//		e.setString("pac", pac);
//		return e;
//		
//	}
	public ProxyConfig()	{	}
	public void setHost(String s)			{	host = s;	}
	public void setPort(String i)			{	port = i;	}
	public void setUsername(String s)		{	username = s;	}
	public void setPassword(String s)		{	password = s;	}
	public void setPac(String s)			{	pac = s;		}
	
	public String getHost()		{	return host;	}
	public String getPort()		{	return port;	}
	public String getUsername()	{	return username;	}
	public String getPassword()	{	return password;	}
	public String getPac()		{	return pac;			}
	
	public boolean isValid()		{	return !StringUtil.isEmpty(getHost());	}
	public boolean isAuthed()	{	return !StringUtil.isEmpty(username) && !StringUtil.isEmpty(password);	}
	public int getPortInt()	{		return ParseUtil.getInteger(port, 0);	}
}
