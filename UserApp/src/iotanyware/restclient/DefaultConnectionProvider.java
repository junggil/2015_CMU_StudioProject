package iotanyware.restclient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DefaultConnectionProvider implements IConnectionProvider {

	public HttpURLConnection getConnection(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		return (HttpURLConnection)url.openConnection();			
	}
	
	
}
