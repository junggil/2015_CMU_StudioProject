/*******************************************************************************
 * Copyright (c) 2008, 2009 Brian Ballantine and Bug Labs, Inc.
 * 
 * MIT License
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *  
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *******************************************************************************/
package iotanyware.restclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * class for dealing RESTfully with HTTP Requests
 * 
 * Example Usage:
 * HttpRequest req = new HttpRequest(myConnectionProvider)
 * HttpResponse resp = req.get("http://some.url")
 * System.out.println(resp.getString());
 * 
 * @author Brian
 * 
 * Revisions
 * 09-03-2008 AK added a Map header parameter to "put" and "post" to support http header 
 * 
 * 
 */
public class HTTPRequest {
	
	private IConnectionProvider _connectionProvider;

	/**
	 * constructor where client provides connectionProvider
	 * 	
	 */
	public HTTPRequest(IConnectionProvider connectionProvider) {
		_connectionProvider = connectionProvider;
	}
	
	/**
	 * constructor that uses default connection provider
	 */
	public HTTPRequest() {
		_connectionProvider = new DefaultConnectionProvider();
	}
	
    /**
     * Do an authenticated HTTP GET from url
     * 
     * @param url   String URL to connect to
     * @return      HttpURLConnection ready with response data
     */
	public HTTPResponse get(String url, Map headers) throws IOException {
		HttpURLConnection conn = _connectionProvider.getConnection(url);
		if (headers != null) {
			Iterator iterator = headers.keySet().iterator();  
			String key; 
			while(iterator.hasNext()) {
				key = iterator.next().toString(); 
				conn.setRequestProperty(key, headers.get(key).toString()); 
			}
		}			
		conn.setDoInput(true);
		conn.setDoOutput(false);
		return connect(conn);
	}
	
    /**
     * Do an HTTP POST to url
     * 
     * @param url   String URL to connect to
     * @param data  String data to post 
     * @return      HttpURLConnection ready with response data
     */
	public HTTPResponse post(String url, String data) throws IOException {
		return post(url, data, null);
	}

	/**
	 * Do an HTTP POST to url w/ extra http headers
	 * 
	 * @param url
	 * @param data
	 * @param headers
	 * @return
	 * @throws IOException
	 */
	public HTTPResponse post(String url, String data, Map headers) throws IOException {
		HttpURLConnection conn = _connectionProvider.getConnection(url);
		if (headers != null) {
			Iterator iterator = headers.keySet().iterator();  
			String key; 
			while(iterator.hasNext()) {
				key = iterator.next().toString(); 
				conn.setRequestProperty(key, headers.get(key).toString()); 
			}
		}		
		conn.setDoOutput(true);
		OutputStreamWriter osr = new OutputStreamWriter(conn.getOutputStream());
		osr.write(data);
		osr.flush();
		osr.close();
		return connect(conn);
	}
	
	/**
	 * Posts a Map of key, value pair properties, like a web form
	 * 
	 * @param url
	 * @param properties
	 * @param headers
	 * @return
	 * @throws IOException
	 */
	public HTTPResponse post(String url, Map properties, Map headers) throws IOException {
		String data = propertyString(properties);
		return post(url, data, headers);
	}
	
	
	/**
	 * Do an HTTP PUT to url
	 * 
	 * @param url  String URL to connect to
	 * @param data String data to post 
	 * @return     HttpURLConnection ready with response data
	 */
	public HTTPResponse put(String url, String data) throws IOException {
		return put(url, data, null);
	}

	/**
	 * Do an HTTP PUT to url with extra headers
	 * 
	 * @param url
	 * @param data
	 * @param headers
	 * @return
	 * @throws IOException
	 */
	public HTTPResponse put(String url, String data, Map headers) throws IOException{
		HttpURLConnection connection = _connectionProvider.getConnection(url);
		if (headers != null) {
			Iterator iterator = headers.keySet().iterator();  
			String key; 
			while(iterator.hasNext()) {
				key = iterator.next().toString(); 
				connection.setRequestProperty(key, headers.get(key).toString()); 
			}
		}
		connection.setDoOutput(true);
		connection.setRequestMethod("PUT");
		OutputStreamWriter osr = new OutputStreamWriter(connection.getOutputStream());
		osr.write(data);
		osr.flush();
		osr.close();
		return connect(connection);		
	}
	
    
	/**
	 * Connect to server, check the status, and return the new HTTPResponse
	 */
	private HTTPResponse connect(HttpURLConnection connection) throws HTTPException, IOException {
		HTTPResponse response = new HTTPResponse(connection);
		response.checkStatus();
		return response;
	}

	/**
	 *  turns a map into a key=value property string for sending to bugnet
	 */
	public String propertyString(Map props) throws IOException {
		String propstr = new String();
		String key;
		propstr = propstr + "{";
		for (Iterator i = props.keySet().iterator(); i.hasNext();) {
			key = (String) i.next();
//			propstr = propstr + "\"" + URLEncoder.encode(key, "UTF-8") + "\"" + ":" 
//								+ "\"" + URLEncoder.encode((String) props.get(key), "UTF-8") + "\"";
			propstr = propstr + "\"" + key + "\"" + ":" 
			+ "\"" + props.get(key) + "\"";
			
			if (i.hasNext()) {
				propstr = propstr + ",";
			}
		}
		//System.out.println(propstr);
		return propstr + "}";
	}
}
