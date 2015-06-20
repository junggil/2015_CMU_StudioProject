package iotanyware;

import iotanyware.restclient.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;
import java.net.URLDecoder;

import org.json.JSONException;
import org.json.JSONObject;

public class HTTPServerAdapter implements ServerInterface{
	
	private static HTTPServerAdapter SingleInstance = null;	

	private static final String SERVER_URL = "http://54.166.26.101:8000";
	private static final String HEADER_CONTENT_KEY  = "Content-Type";
	private static final String HEADER_CONTENT_VAL  = "application/json";
	private static final String HEADER_CLIENTID_KEY  = "x-client-id";
	private static final String HEADER_CLIENTID_VAL  = "75f9e675-9db4-4d02-b523-37521ef656ea";	
	private static final String BODY_EMAIL_KEY = "email";
	private static final String BODY_PASSWORD_KEY = "password";
	private static final String BODY_SESSIONID_KEY = "session";
	private static final String BODY_NODEID_KEY = "nodeId";
	private static final String BODY_NICKNAME_KEY = "nickName";	
	private static final String BODY_LOGHOUR_KEY = "loggingHour";
	private static final String BODY_TARGETUSER_KEY = "targetUser";

	protected HTTPServerAdapter() {
	}
	
	public static HTTPServerAdapter getInstance() {
		if(SingleInstance == null) {
			SingleInstance = new HTTPServerAdapter();
		}
		return SingleInstance;
	}		
	
	@Override
	public boolean registerUser(String email, String password) {
		HTTPRequest httprequest = HTTPRequest.getInstance();
		HTTPResponse httpresponse;
		String uri = SERVER_URL + "/account/registerNewUser";

		HashMap<String, String> headers = new HashMap<String, String>(); 
		HashMap<String, String> body = new HashMap<String, String>();

		System.out.println(uri);
		try {
			
			// 1.Handle HTTP request
			headers.put(HEADER_CONTENT_KEY, HEADER_CONTENT_VAL);
			headers.put(HEADER_CLIENTID_KEY, HEADER_CLIENTID_VAL);
			
			body.put(BODY_PASSWORD_KEY, password);
			body.put(BODY_EMAIL_KEY, email);
			body.put(BODY_NICKNAME_KEY, "iot");			
			
			httpresponse = httprequest.post(uri, httprequest.propertyString(body), headers);

			// 2.Handle HTTP response
			String resString = URLDecoder.decode(httpresponse.getString(), "UTF-8");
    		System.out.println(resString);

			// 3. JSON parse the response
    		JSONObject jresString = new JSONObject(resString);
    		Object jstatusCode = jresString.get("statusCode");
    		
    		if((int)jstatusCode == 200)
    			return true;
    		else 
    			return false;
    		
		} catch (IOException | JSONException  e) {
			System.out.println(e);
			return false;
		}	
	}

	@Override
	public String loginProgess(String email, String password) {
		HTTPRequest httprequest = HTTPRequest.getInstance();
		HTTPResponse httpresponse;
		String uri = SERVER_URL + "/session/createUser";

		HashMap<String, String> headers = new HashMap<String, String>(); 
		HashMap<String, String> body = new HashMap<String, String>();

		System.out.println(uri);
		try {
			
			// 1.Handle HTTP request
			headers.put(HEADER_CONTENT_KEY, HEADER_CONTENT_VAL);
			headers.put(HEADER_CLIENTID_KEY, HEADER_CLIENTID_VAL);
			
			body.put(BODY_PASSWORD_KEY, password);
			body.put(BODY_EMAIL_KEY, email);			
			
			httpresponse = httprequest.post(uri, httprequest.propertyString(body), headers);

			// 2.Handle HTTP response
			String resString = httpresponse.getString();
			System.out.println("1."+ resString);
			
			String resStringUTF8 = URLDecoder.decode(resString, "UTF-8");
    		System.out.println("2."+ resStringUTF8);

			// 3. JSON parse the response
    		JSONObject jresString = new JSONObject(resStringUTF8);
    		Object jstatusCode = jresString.get("statusCode");
    		
    		if((int)jstatusCode == 200){
  	    		JSONObject getSth = jresString.getJSONObject("result");
    			Object sessionid = getSth.get(BODY_SESSIONID_KEY);
    			System.out.println(sessionid);

    			return (String) sessionid;
    		}
    		else {
    			return null;
    		}
  		} catch (IOException | JSONException  e) {
			System.out.println(e);
			return null;
		}
	}

	public String getNodeList(String sessionid) {
		HTTPRequest httprequest = HTTPRequest.getInstance();
		HTTPResponse httpresponse;

		HashMap<String, String> headers = new HashMap<String, String>(); 
		String uri = SERVER_URL + "/user/getNodeList?session=" + sessionid;
		
		try {
			// 1.Handle HTTP request
			headers.put(HEADER_CONTENT_KEY, HEADER_CONTENT_VAL);
			headers.put(HEADER_CLIENTID_KEY, HEADER_CLIENTID_VAL);
			
			System.out.println(uri);	
			
			httpresponse = httprequest.get(uri /*URLEncoder.encode(uri,"UTF-8")*/, headers);

			// 2.Handle HTTP response
			String resString = httpresponse.getString();
			System.out.println("1."+ resString);
			
			String resStringUTF8 = URLDecoder.decode(resString, "UTF-8");
    		System.out.println("2."+ resStringUTF8);
			
			return (String) resStringUTF8;
			
		} catch (IOException e) {
			System.out.println(e);
			return null;
		}
	}

	@Override
	public boolean registerNode(String sessionid, String nodeid, String nickName) {
		HTTPRequest httprequest = HTTPRequest.getInstance();
		HTTPResponse httpresponse;
		String uri = SERVER_URL + "/user/registerNode";

		HashMap<String, String> headers = new HashMap<String, String>(); 
		HashMap<String, String> body = new HashMap<String, String>();

		System.out.println(uri);
		try {
			
			// 1.Handle HTTP request
			headers.put(HEADER_CONTENT_KEY, HEADER_CONTENT_VAL);
			headers.put(HEADER_CLIENTID_KEY, HEADER_CLIENTID_VAL);
			
			body.put(BODY_SESSIONID_KEY, sessionid);
			body.put(BODY_NODEID_KEY, nodeid);
			body.put(BODY_NICKNAME_KEY, nickName);	
			
			httpresponse = httprequest.post(uri, httprequest.propertyString(body), headers);

			// 2.Handle HTTP response
			String resString = URLDecoder.decode(httpresponse.getString(), "UTF-8");
    		System.out.println(resString);

			// 3. JSON parse the response
    		JSONObject jresString = new JSONObject(resString);
    		Object jstatusCode = jresString.get("statusCode");
    		
    		if((int)jstatusCode == 200)
    			return true;
    		else 
    			return false;
    		
		} catch (IOException | JSONException  e) {
			System.out.println(e);
			return false;
		}
	}

	@Override
	public boolean unregisterNode(String sessionid, String nodeid) {
		HTTPRequest httprequest = HTTPRequest.getInstance();
		HTTPResponse httpresponse;
		String uri = SERVER_URL + "/user/unregisterNode";

		HashMap<String, String> headers = new HashMap<String, String>(); 
		HashMap<String, String> body = new HashMap<String, String>();

		System.out.println(uri);
		try {
			
			// 1.Handle HTTP request
			headers.put(HEADER_CONTENT_KEY, HEADER_CONTENT_VAL);
			headers.put(HEADER_CLIENTID_KEY, HEADER_CLIENTID_VAL);
			
			body.put(BODY_SESSIONID_KEY, sessionid);
			body.put(BODY_NODEID_KEY, nodeid);			
			
			httpresponse = httprequest.post(uri, httprequest.propertyString(body), headers);

			// 2.Handle HTTP response
			String resString = URLDecoder.decode(httpresponse.getString(), "UTF-8");
    		System.out.println(resString);

			// 3. JSON parse the response
    		JSONObject jresString = new JSONObject(resString);
    		Object jstatusCode = jresString.get("statusCode");
    		
    		if((int)jstatusCode == 200)
    			return true;
    		else 
    			return false;
    		
		} catch (IOException | JSONException  e) {
			System.out.println(e);
			return false;
		}
	}
	
	@Override
	public String getProfile(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String viewLog(String sessionid) {
		HTTPRequest httprequest = HTTPRequest.getInstance();
		HTTPResponse httpresponse;

		HashMap<String, String> headers = new HashMap<String, String>(); 
		String uri = SERVER_URL + "/log/getHistory?session=" + sessionid;
		
		try {
			// 1.Handle HTTP request
			headers.put(HEADER_CONTENT_KEY, HEADER_CONTENT_VAL);
			headers.put(HEADER_CLIENTID_KEY, HEADER_CLIENTID_VAL);
			
			System.out.println(uri);	
			httpresponse = httprequest.get(uri, headers);

			// 2.Handle HTTP response
			String resString = URLDecoder.decode(httpresponse.getString(), "UTF-8");
			System.out.println("String = " + resString);
			
			return (String) resString;
			
		} catch (IOException e) {
			System.out.println(e);
			return null;
		}
	}

	@Override
	public boolean setLogConfig(String sessionid, int loggingHour) {
		HTTPRequest httprequest = HTTPRequest.getInstance();
		HTTPResponse httpresponse;
		String uri = SERVER_URL + "/user/setConfiguration";

		HashMap<String, String> headers = new HashMap<String, String>(); 

		System.out.println(uri);
		try {
			
			// 1.Handle HTTP request
			headers.put(HEADER_CONTENT_KEY, HEADER_CONTENT_VAL);
			headers.put(HEADER_CLIENTID_KEY, HEADER_CLIENTID_VAL);
			
			String setLogStr = 	"{\"session\":" + "\""+sessionid+"\","+
								"\"loggingHour\":" + Integer.toString(loggingHour) +"}";

			httpresponse = httprequest.put(uri, setLogStr, headers);

			// 2.Handle HTTP response
			String resString = URLDecoder.decode(httpresponse.getString(), "UTF-8");
    		System.out.println(resString);

			// 3. JSON parse the response
    		JSONObject jresString = new JSONObject(resString);
    		Object jstatusCode = jresString.get("statusCode");
    		
    		if((int)jstatusCode == 200)
    			return true;
    		else 
    			return false;
    		
		} catch (IOException | JSONException  e) {
			System.out.println(e);
			return false;
		}
	}

	@Override
	public int getLogConfig(String sessioinid) {
		HTTPRequest httprequest = HTTPRequest.getInstance();	
		HTTPResponse httpresponse;

		HashMap<String, String> headers = new HashMap<String, String>(); 
		String uri = SERVER_URL + "/user/getConfiguration?session=" + sessioinid;
		
		try {
			// 1.Handle HTTP request
			headers.put(HEADER_CONTENT_KEY, HEADER_CONTENT_VAL);
			headers.put(HEADER_CLIENTID_KEY, HEADER_CLIENTID_VAL);
			
			System.out.println(uri);	
			httpresponse = httprequest.get(uri, headers);

			// 2.Handle HTTP response
			String resString = URLDecoder.decode(httpresponse.getString(), "UTF-8");
			System.out.println("String = " + resString);
			
			// 3. JSON parse the response
    		JSONObject jresString = new JSONObject(resString);
    		Object jstatusCode = jresString.get("statusCode");			
    		
    		if((int)jstatusCode == 200){
  	    		JSONObject getSth = jresString.getJSONObject("result");
    			Object loggingHour = getSth.get(BODY_LOGHOUR_KEY);
    			System.out.println(loggingHour);
    			return (int) loggingHour;
    		}
    		else {
    			return -1;
    		}
		} catch (IOException | JSONException e) {
			System.out.println(e);
			return -1;
		}
	}

	@Override
	public boolean sharingUser(String sessionid, String nodeid, String user) {
		HTTPRequest httprequest = HTTPRequest.getInstance();
		HTTPResponse httpresponse;
		String uri = SERVER_URL + "/user/shareNode";

		HashMap<String, String> headers = new HashMap<String, String>(); 
		HashMap<String, String> body = new HashMap<String, String>();

		System.out.println(uri);
		try {
			
			// 1.Handle HTTP request
			headers.put(HEADER_CONTENT_KEY, HEADER_CONTENT_VAL);
			headers.put(HEADER_CLIENTID_KEY, HEADER_CLIENTID_VAL);
			
			body.put(BODY_SESSIONID_KEY, sessionid);
			body.put(BODY_NODEID_KEY, nodeid);
			body.put(BODY_TARGETUSER_KEY, user);	
			
			httpresponse = httprequest.post(uri, httprequest.propertyString(body), headers);

			// 2.Handle HTTP response
			String resString = URLDecoder.decode(httpresponse.getString(), "UTF-8");
    		System.out.println(resString);

			// 3. JSON parse the response
    		JSONObject jresString = new JSONObject(resString);
    		Object jstatusCode = jresString.get("statusCode");
    		
    		if((int)jstatusCode == 200)
    			return true;
    		else 
    			return false;
    		
		} catch (IOException | JSONException  e) {
			System.out.println(e);
			return false;
		}
	}	
}
