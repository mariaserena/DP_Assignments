package it.polito.dp2.NFV.sol3.service;

public class MyBadRequestException extends Exception {
	private static final long serialVersionUID = 1L;

	public MyBadRequestException() {
	}

	public MyBadRequestException(String message) {
		super(message);
	}

	public MyBadRequestException(Throwable cause) {
		super(cause);
	}

	public MyBadRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public MyBadRequestException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
