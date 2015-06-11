package iotanyware.restclient;

import java.io.IOException;
import java.net.HttpURLConnection;

public interface IConnectionProvider {
	public HttpURLConnection getConnection(String urlStr) throws IOException;
}
