package iotanyware.restclient;

import java.io.IOException;

public class HTTPException extends IOException {
	private static final long serialVersionUID = -4526324236776240815L;
	private int _httpErrorCode;
	
	public HTTPException(int errorCode, String error) {
		super(error);
		_httpErrorCode = errorCode;
	}
	
	public int getErrorCode() {
		return _httpErrorCode;
	}
}
